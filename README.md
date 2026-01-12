# 創棋(しょうぎ)

## ユーザーマニュアル

[こちら](https://github.com/tkhs-0114/tkk_game/tree/main/manual/user-manual.md)からご覧になれます

## セットアップ

最低条件:Java21のインストール<br>
想定環境:UNIXベースのコマンドライン

### 利用者向け
1. [こちら](https://github.com/tkhs-0114/tkk_game/releases)から必要なバージョンを探します
2. バージョン情報に書かれている取得コマンドもしくは手動でダウンロードします
3. ダウンロードしたフォルダ内でバージョン情報に書かれている起動コマンドを実行します
4. ポート80番で開かれるのであとは接続するだけです

### 開発者向け
1. `git clone https://github.com/tkhs-0114/tkk_game.git`もしくは<br>`git clone git@github.com:tkhs-0114/tkk_game.git`でプロジェクトをダウンロードします
2. プロジェクト内の`cd tkk_game`と入力し、`/tkk_game/tkk_game`内で`gradle bootrun`もしくは`bash ./gradlew bootrun`を実行します
3. ポート80番で開かれるのであとは接続するだけです

また、ホットリロード用に`/tkk_game`内に`debug.sh`というスクリプトがあります
`./debug.sh`で起動できます

## 使用技術 / バージョン
- Java 21 (Gradle Toolchain)
- Spring Boot 3.5.7
- Spring Security
- Spring Web
- Thymeleaf
- Thymeleaf Extras SpringSecurity6
- Spring Boot DevTools
- MyBatis 3.0.5
- H2 Database
- Server-Sent Events
- Spring Async / Scheduling

詳しい仕様は[こちら](https://github.com/tkhs-0114/tkk_game/blob/main/docs/specs.md)から