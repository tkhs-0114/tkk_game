#!/bin/bash

REPO_PATH="./"
CHECK_INTERVAL=60  # チェック間隔(秒) 60秒=1分
HASH_FILE="./.log/Git_last_hash.txt"

cd $REPO_PATH

# 終了時の処理を定義
cleanup() {
    echo ""
    echo "$(date): Stopping auto-pull service..."
    if [ ! -z "$SPRINGBOOT_PID" ]; then
        echo "$(date): Stopping Spring Boot application (PID: $SPRINGBOOT_PID)..."
        kill $SPRINGBOOT_PID 2>/dev/null
        
        # gradlewプロセスが確実に終了するまで待機
        sleep 3
        
        # まだ生きている場合は強制終了
        if ps -p $SPRINGBOOT_PID > /dev/null 2>&1; then
            echo "$(date): Force stopping Spring Boot application..."
            kill -9 $SPRINGBOOT_PID 2>/dev/null
        fi
        
        echo "$(date): Spring Boot application stopped"
    fi
    echo "$(date): Auto-pull service stopped"
    exit 0
}

# SIGINTとSIGTERMをトラップして終了処理を実行
trap cleanup SIGINT SIGTERM

echo "Starting auto-pull service..."
echo "Repository: $REPO_PATH"
echo "Check interval: ${CHECK_INTERVAL}s"
echo "Press Ctrl+C to stop"
echo ""

# 初回のハッシュを保存
git rev-parse HEAD > $HASH_FILE

# Spring Bootを起動
echo "$(date): Starting Spring Boot application..."
nohup bash ./gradlew bootRun > ./.log/springboot.log 2>&1 < /dev/null &
SPRINGBOOT_PID=$!
echo "Spring Boot PID: $SPRINGBOOT_PID"

# メインループ
while true; do
    sleep $CHECK_INTERVAL
    
    echo "$(date): Checking for updates..."
    
    # 現在のハッシュを取得
    CURRENT_HASH=$(cat $HASH_FILE)
    
    # pullを実行
    git pull origin main
    
    # 新しいハッシュを取得
    NEW_HASH=$(git rev-parse HEAD)
    
    # 変更があればアプリを再起動
    if [ "$NEW_HASH" != "$CURRENT_HASH" ]; then
        echo "$(date): Changes detected!"
        echo "  Old: $CURRENT_HASH"
        echo "  New: $NEW_HASH"
        echo "$(date): Restarting Spring Boot application..."
        
        # 既存のSpring Bootプロセスを停止
        kill $SPRINGBOOT_PID 2>/dev/null
        sleep 5
        
        # 再起動
        nohup bash ./gradlew bootRun > ./.log/springboot.log 2>&1 < /dev/null &
        SPRINGBOOT_PID=$!
        
        echo "$(date): Application restarted (PID: $SPRINGBOOT_PID)"
        
        # 新しいハッシュを保存
        echo $NEW_HASH > $HASH_FILE
    else
        echo "$(date): No changes detected"
    fi
done
