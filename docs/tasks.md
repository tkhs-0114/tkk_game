# 切断処理基盤実装計画

## 計画作成日
2026年01月05日

## 目的
プレイヤーが切断した時（ブラウザクローズ、他サイトへの遷移、ネットワーク切断等）に、切断したプレイヤーと切断されたプレイヤーの両方に対して適切な処理を行うための基盤を構築する。

## 背景・要件

### 基本要件
1. **切断検知**: 様々な切断パターン（ブラウザクローズ、他サイト遷移、ネットワーク切断等）を検知
2. **切断通知**: 切断を検知したら、相手プレイヤーに通知
3. **リソースクリーンアップ**: SSE接続、ゲームオブジェクト等の適切な解放
4. **ゲームステータス管理**: 切断時のゲーム状態を適切に管理

### 切断の種類と検知方法
| 切断の種類 | 検知方法 | 検知タイミング |
|-----------|---------|--------------|
| ブラウザクローズ | `beforeunload` + `sendBeacon()` | 即時 |
| 他サイトへの遷移 | `beforeunload` + `sendBeacon()` | 即時 |
| タブを閉じる | `beforeunload` + `sendBeacon()` | 即時 |
| ネットワーク切断 | SSE `onError` + `IOException` | 次回 send 時 |
| ブラウザクラッシュ | SSE タイムアウト | タイムアウト時間経過後 |

### 実装方針
- `DisconnectionHandler` サービスを新設し、切断処理を一元管理
- クライアント側で `beforeunload` イベントを実装し、`navigator.sendBeacon()` でサーバーに通知
- SSE のタイムアウトを適切に設定（30秒程度）
- ハートビート機能で定期的にダミーデータを送信し、早期に切断を検知
- 相手プレイヤーへの切断通知は既存の SSE を活用

## スコープ（含む）
- `DisconnectionHandler` サービスの新設
- クライアント側の切断通知機能（`beforeunload` + `sendBeacon()`）
- サーバー側の切断通知エンドポイント
- SSE タイムアウトの設定
- ハートビート機能の実装
- 相手プレイヤーへの切断通知機能
- ゲーム画面、待機画面への適用

## スコープ（含まない）
- 切断後の再接続機能
- 切断履歴の保存・表示
- 切断に対するペナルティ機能
- 意図的な切断の検出・防止

---

## タスク一覧

### タスク1: DisconnectionHandler サービスの作成

**優先度**: 1（最優先）

**関連ファイル**:
- 新規作成: `tkk_game/src/main/java/team3/tkk_game/services/DisconnectionHandler.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/GameRoom.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/WaitRoom.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Game.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/WaitRoomEventEmitterManager.java`

**作業内容**:
1. `DisconnectionHandler` クラスを `@Service` アノテーションで作成
2. `GameRoom`, `WaitRoom`, `GameEventEmitterManager`, `WaitRoomEventEmitterManager` を `@Autowired` で注入
3. `handlePlayerDisconnection(String playerName, String reason)` メソッドを実装
   - `GameRoom.getGameByPlayerName()` でゲームを取得
   - ゲームが見つかった場合:
     - 相手プレイヤー名を取得 (`game.getEnemyPlayerByName()`)
     - 相手への切断通知を送信 (`GameEventEmitterManager.notifyPlayerDisconnection()`)
     - SSE接続のクリーンアップ (`GameEventEmitterManager.removePlayerEmittersByGameId()`)
     - ゲームステータスの更新（`game.setIsFinished()` など）
     - ゲームの削除 (`GameRoom.rmGameByName()`)
   - ログ出力（INFO レベル）
4. `handleWaitRoomDisconnection(String playerName)` メソッドを実装
   - 待機室から削除 (`WaitRoom.rmWaitRoomByOwner()`)
   - SSE接続のクリーンアップ (`WaitRoomEventEmitterManager` は共通Emitterなので個別削除不要）
   - ログ出力（INFO レベル）

**動作確認手順**:
- `gradle build` でコンパイルエラーがないことを確認
- `gradle bootRun` でアプリケーションが正常に起動することを確認
- ログ出力で DisconnectionHandler が Spring コンテナに登録されたことを確認

**入力**: `playerName` (切断したプレイヤー名), `reason` (切断理由)
**出力**: なし（内部処理のみ、ログ出力あり）

---

### タスク2: GameController に切断通知エンドポイントを追加

**優先度**: 2

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/DisconnectionHandler.java`

**作業内容**:
1. `DisconnectionHandler` を `@Autowired` で注入
2. `/game/disconnect` エンドポイントを `@PostMapping` で作成
3. `@ResponseBody` アノテーションを付与（JSONレスポンス用）
4. `Principal` からプレイヤー名を取得 (`principal.getName()`)
5. `DisconnectionHandler.handlePlayerDisconnection(playerName, "INTENTIONAL")` を呼び出し
6. 成功時に `Map.of("message", "disconnection notified")` を返す
7. `import java.util.Map;` を追加

**動作確認手順**:
- `gradle build` でコンパイルエラーがないことを確認
- `gradle bootRun` でアプリケーションを起動
- ブラウザで `user1` / `p@ss` でログイン
- Postman や curl で以下のリクエストを送信:
  ```powershell
  curl -X POST http://localhost:80/game/disconnect -H "Cookie: JSESSIONID=YOUR_SESSION_ID"
  ```
- レスポンスが `{"message":"disconnection notified"}` であることを確認
- サーバーログで切断処理が実行されたことを確認

**入力**: なし（認証済みプレイヤー名を `Principal` から取得）
**出力**: `{ "message": "disconnection notified" }` (JSON)

---

### タスク3: MatchController に切断通知エンドポイントを追加

**優先度**: 3

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/controller/MatchController.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/DisconnectionHandler.java`

**作業内容**:
1. `DisconnectionHandler` を `@Autowired` で注入
2. `/match/disconnect` エンドポイントを `@PostMapping` で作成
3. `@ResponseBody` アノテーションを付与
4. `Principal` からプレイヤー名を取得 (`principal.getName()`)
5. `DisconnectionHandler.handleWaitRoomDisconnection(playerName)` を呼び出し
6. 成功時に `Map.of("message", "disconnection notified")` を返す
7. `import java.util.Map;` を追加

**動作確認手順**:
- `gradle build` でコンパイルエラーがないことを確認
- `gradle bootRun` でアプリケーションを起動
- ブラウザで `user1` / `p@ss` でログイン
- 待機画面に移動
- Postman や curl で以下のリクエストを送信:
  ```powershell
  curl -X POST http://localhost:80/match/disconnect -H "Cookie: JSESSIONID=YOUR_SESSION_ID"
  ```
- レスポンスが `{"message":"disconnection notified"}` であることを確認
- サーバーログで切断処理が実行されたことを確認

**入力**: なし（認証済みプレイヤー名を `Principal` から取得）
**出力**: `{ "message": "disconnection notified" }` (JSON)

---

### タスク4: GameEventEmitterManager に切断イベント送信機能を追加

**優先度**: 4

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Game.java`

**作業内容**:
1. `notifyPlayerDisconnection(String gameId, String disconnectedPlayerName)` メソッドを追加
2. ゲームIDから Emitter リストを取得 (`gameEmitters.get(gameId)`)
3. 全 Emitter に対して切断通知を送信
   - `emitter.send(SseEmitter.event().name("disconnect").data(disconnectedPlayerName))`
4. IOException 発生時は該当プレイヤーを削除（既存の `notifyTurnChange()` と同様の処理）
5. ログ出力（INFO レベル）

**動作確認手順**:
- `gradle build` でコンパイルエラーがないことを確認
- 次のタスク（クライアント側実装）と合わせて動作確認を実施

**入力**: `gameId` (ゲームID), `disconnectedPlayerName` (切断したプレイヤー名)
**出力**: なし（SSE経由で相手プレイヤーに通知）

---

### タスク5: SSE タイムアウトの設定変更

**優先度**: 5

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java`
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/WaitRoomEventEmitterManager.java`

**作業内容**:
1. `GameEventEmitterManager.registerPlayerEmitter()` の Emitter 生成を変更
   - `new SseEmitter(Long.MAX_VALUE)` → `new SseEmitter(30000L)` （30秒）
2. `WaitRoomEventEmitterManager.registerEmitter()` の Emitter 生成を変更
   - `new SseEmitter(Long.MAX_VALUE)` → `new SseEmitter(30000L)` （30秒）
3. タイムアウト時のコールバックが適切に実行されることを確認（既存の `onTimeout` がある）

**動作確認手順**:
- `gradle build` でコンパイルエラーがないことを確認
- `gradle bootRun` でアプリケーションを起動
- ブラウザでゲーム画面または待機画面を開く
- 30秒間何も操作せず待機
- タイムアウト後にブラウザコンソールでSSE接続エラーが表示されることを確認
- サーバーログで `onTimeout` コールバックが実行されたことを確認

**入力**: なし（タイムアウト設定の変更）
**出力**: なし（タイムアウト時にコールバック実行）

---

### タスク6: ハートビート機能の実装

**優先度**: 6

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/DisconnectionHandler.java`

**作業内容**:
1. `@Scheduled(fixedRate = 5000)` のハートビートメソッド `sendHeartbeat()` を追加
2. `DisconnectionHandler` を `@Autowired` で注入
3. すべてのアクティブな Emitter にハートビートメッセージを送信
   - `gameEmitters` の全エントリをループ
   - 各 Emitter に `emitter.send(SseEmitter.event().name("heartbeat").data(currentTime))` を送信
   - `currentTime` は `System.currentTimeMillis()` を文字列化したもの
4. IOException 発生時は `DisconnectionHandler.handlePlayerDisconnection()` を呼び出し
5. ログ出力（DEBUG レベル、頻繁に実行されるため）
6. `import org.springframework.scheduling.annotation.Scheduled;` を追加

**動作確認手順**:
- `gradle build` でコンパイルエラーがないことを確認
- `gradle bootRun` でアプリケーションを起動
- ブラウザでゲーム画面を開き、開発者ツールのコンソールを表示
- 5秒ごとにハートビートメッセージが受信されることを確認
- ネットワークタブで `event: heartbeat` のSSEイベントが5秒ごとに来ることを確認

**入力**: なし（定期実行）
**出力**: なし（SSE経由でハートビート送信）

---

### タスク7: game.html に beforeunload イベント実装

**優先度**: 7

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/templates/game.html`

**作業内容**:
1. ゲーム画面の JavaScript に、SSE接続用の変数 `let sse = null;` をグローバルスコープに追加
2. 既存の SSE 接続処理を修正し、`sse` 変数に代入
3. `window.addEventListener('beforeunload', ...)` を追加
   - SSE接続を `if(sse) sse.close();` で明示的に切断
   - `navigator.sendBeacon('/game/disconnect', ...)` でサーバーに切断を通知
   - Blob で JSON を送信: `new Blob([JSON.stringify({ action: 'disconnect' })], { type: 'application/json' })`
4. コメントを日本語で追加（他サイトへの遷移やブラウザクローズを検知）

**動作確認手順**:
- `gradle bootRun` でアプリケーションを起動
- 2つのブラウザでゲーム画面を開く（user1とuser2でマッチング）
- ブラウザ1のタブを閉じる
- サーバーログで `/game/disconnect` エンドポイントが呼ばれたことを確認
- サーバーログで切断処理が実行されたことを確認

**入力**: なし（ユーザーがブラウザを閉じる操作）
**出力**: サーバーへの切断通知（`sendBeacon`）

---

### タスク8: game.html に SSE 切断イベントハンドラ実装

**優先度**: 8

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/templates/game.html`

**作業内容**:
1. SSE接続時に `sse.addEventListener('disconnect', ...)` を追加
2. 切断イベント受信時の処理:
   - `alert('対戦相手が切断しました')` でアラート表示
   - `window.location.href = '/match';` でマッチング画面にリダイレクト
3. `sse.onerror` イベントハンドラを追加
   - エラー内容をコンソールに出力
   - `console.error('SSE connection error:', event);`

**動作確認手順**:
- `gradle bootRun` でアプリケーションを起動
- 2つのブラウザでゲーム画面を開く（user1とuser2でマッチング）
- ブラウザ1のタブを閉じる
- ブラウザ2にアラート「対戦相手が切断しました」が表示される
- アラートを閉じると、自動的にマッチング画面にリダイレクトされる

**入力**: SSE切断イベント
**出力**: アラート表示とページリダイレクト

---

### タスク9: waiting.html に beforeunload イベント実装

**優先度**: 9

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/templates/waiting.html`

**作業内容**:
1. 待機画面の JavaScript に、SSE接続用の変数 `let sse = null;` をグローバルスコープに追加
2. 既存の SSE 接続処理（`new EventSource('/match/waitRoom')`）を修正し、`sse` 変数に代入
3. `window.addEventListener('beforeunload', ...)` を追加
   - SSE接続を `if(sse) sse.close();` で明示的に切断
   - `navigator.sendBeacon('/match/disconnect', ...)` でサーバーに切断を通知
   - Blob で JSON を送信: `new Blob([JSON.stringify({ action: 'disconnect' })], { type: 'application/json' })`
4. コメントを日本語で追加（他サイトへの遷移やブラウザクローズを検知）

**動作確認手順**:
- `gradle bootRun` でアプリケーションを起動
- ブラウザで待機画面を開く（user1で部屋を作成）
- ブラウザのタブを閉じる
- サーバーログで `/match/disconnect` エンドポイントが呼ばれたことを確認
- サーバーログで待機室から削除されたことを確認

**入力**: なし（ユーザーがブラウザを閉じる操作）
**出力**: サーバーへの切断通知（`sendBeacon`）

---

### タスク10: match.html に beforeunload イベント実装

**優先度**: 10

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/templates/match.html`

**作業内容**:
1. マッチング画面の JavaScript に、SSE接続用の変数 `let sse = null;` をグローバルスコープに追加
2. 既存の SSE 接続処理（`new EventSource('/match/waitRoom')`）を修正し、`sse` 変数に代入
3. `window.addEventListener('beforeunload', ...)` を追加
   - SSE接続を `if(sse) sse.close();` で明示的に切断
4. コメントを日本語で追加（SSE接続のクリーンアップ）

**動作確認手順**:
- `gradle bootRun` でアプリケーションを起動
- ブラウザでマッチング画面を開く
- 開発者ツールのコンソールでSSE接続が確立されたことを確認
- ブラウザのタブを閉じる
- サーバーログでSSE接続が正常に切断されたことを確認（エラーログが出ないこと）

**入力**: なし（ユーザーがブラウザを閉じる操作）
**出力**: SSE接続の切断

---

## Definition of Done (DoD)

すべてのタスクが完了した後、以下の手順で動作を確認すること：

### 確認手順

1. **アプリケーション起動**
   ```powershell
   cd tkk_game
   gradle bootRun
   ```

2. **2つのブラウザで同時にアクセス**
   - ブラウザ1: `http://localhost/` にアクセスし、`user1 / p@ss` でログイン
   - ブラウザ2: `http://localhost/` にアクセスし、`user2 / p@ss` でログイン

3. **マッチング画面での切断テスト**
   - ブラウザ1でマッチング画面に移動
   - ブラウザ2でマッチング画面に移動
   - ブラウザ1のタブを閉じる
   - サーバーログで `/match/disconnect` が呼ばれたことを確認
   - サーバーログで `handleWaitRoomDisconnection()` が実行されたことを確認

4. **待機画面での切断テスト**
   - ブラウザ1で「部屋を作る」をクリックし、待機画面に移動
   - ブラウザ2でマッチング画面を開き、user1 の部屋が表示されることを確認
   - ブラウザ1のタブを閉じる
   - サーバーログで `/match/disconnect` が呼ばれたことを確認
   - ブラウザ2でページを更新し、user1 の部屋が消えたことを確認

5. **ゲーム画面での切断テスト**
   - ブラウザ1とブラウザ2でマッチングし、ゲーム画面に移動
   - ブラウザ1のタブを閉じる
   - サーバーログで `/game/disconnect` が呼ばれたことを確認
   - サーバーログで `handlePlayerDisconnection()` が実行されたことを確認
   - ブラウザ2にアラート「対戦相手が切断しました」が表示される
   - アラートを閉じると、自動的にマッチング画面にリダイレクトされる

6. **ネットワーク切断での切断テスト**
   - ブラウザ1とブラウザ2でゲーム画面に移動
   - ブラウザ1の開発者ツールを開き、Network タブで「Offline」を選択
   - 5秒以内に次のハートビート送信が失敗する
   - サーバーログでハートビート送信失敗により切断が検知される
   - サーバーログで `handlePlayerDisconnection()` が実行されたことを確認
   - ブラウザ2にアラート「対戦相手が切断しました」が表示される

7. **タイムアウトでの切断テスト**
   - ブラウザ1でゲーム画面に移動
   - ハートビート機能を一時的に無効化（コメントアウト）してアプリケーションを再起動
   - 30秒間何も操作せず待機
   - タイムアウト後にブラウザコンソールでSSE接続エラーが表示される
   - サーバーログで `onTimeout` コールバックが実行されたことを確認

8. **ハートビート機能の確認**
   - ブラウザでゲーム画面を開く
   - 開発者ツールのコンソールを開く
   - 5秒ごとにハートビートメッセージが受信されることを確認
   - ネットワークタブで `event: heartbeat` のSSEイベントが5秒ごとに来ることを確認

### 期待される動作

- すべての切断パターンで適切に切断が検知される
- 相手プレイヤーに切断通知が届く（ゲーム中の場合のみ）
- SSE接続が適切にクリーンアップされる（サーバーログにエラーが出ない）
- メモリリークが発生しない（長時間実行してもメモリ使用量が安定）
- ゲームステータスが適切に管理される（切断後にゲームが削除される）
- ブラウザクローズ、他サイト遷移、ネットワーク切断のすべてで正しく動作する
