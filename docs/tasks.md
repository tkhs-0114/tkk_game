# SSEイベント駆動化リファクタリング計画

## 計画作成日
2025年12月23日

## 目的
現在のポーリング方式のSSE通信をイベント駆動型に変更し、サーバーリソースの効率化とリアルタイム性の向上を図る。

## 背景・現状の問題点

### 現在のSSE実装の問題
| 問題 | 説明 | 影響 |
|------|------|------|
| リソース浪費 | 状態が変わらなくても1秒ごとにデータ送信 | サーバーCPU・ネットワーク帯域の無駄遣い |
| スレッド占有 | 各クライアント接続でスレッドが永続的に占有される | 同時接続数が増えるとスレッドプール枯渇 |
| 遅延 | 最大1秒の遅延が発生 | ターン切り替えがリアルタイムに感じられない |
| 不必要なデータ転送 | 変更がなくても同じデータを繰り返し送信 | クライアント側の処理負荷増加 |

### 対象ファイル（現在のポーリング実装）
- `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java`
- `tkk_game/src/main/java/team3/tkk_game/services/MatchChecker.java`

## 前提条件
- Spring Boot 3.5.7 のSSE機能を使用
- `@Async` アノテーションによる非同期処理が有効
- `ConcurrentHashMap` / `CopyOnWriteArrayList` によるスレッドセーフなコレクションを使用

## スコープ（含む）
- ゲームターン通知のイベント駆動化
- 待機室リスト通知のイベント駆動化
- Emitter管理サービスの新規作成
- コントローラーからのイベント発火処理追加

## スコープ（含まない）
- フロントエンド（JavaScript）の大幅変更
- 新しいSSEエンドポイントの追加
- 既存のSSEエンドポイントURL変更

---

## タスク一覧

### タスク1: GameEventEmitterManager サービスクラスの作成

**優先度**: 1（最優先）

**関連ファイル**:
- 新規作成: `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Game.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/PlayerStatus.java`

**作業内容**:
1. `tkk_game/src/main/java/team3/tkk_game/services/` に `GameEventEmitterManager.java` を新規作成
2. `@Component` アノテーションを付与
3. 以下のフィールドを定義:
   - `ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> gameEmitters` - ゲームID別のEmitter管理
   - `ConcurrentHashMap<String, String> emitterToPlayer` - Emitterとプレイヤー名の対応
4. 以下のメソッドを実装:
   - `registerPlayerEmitter(String playerName, String gameId)` - Emitter登録
   - `notifyTurnChange(String gameId, String currentTurnPlayerName)` - ターン変更通知
   - `removeEmitter(String playerName)` - Emitter削除
5. Emitterのコールバック設定（onCompletion, onTimeout, onError）

**動作確認手順**:
- ユーザーが最後に確認を実施するため、現段階では不要、次のステップに移ってください

**入力**: なし（新規作成）
**出力**: GameEventEmitterManager クラス

---

### タスク2: TurnChecker のイベント駆動化

**優先度**: 2

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java`

**作業内容**:
1. `TurnChecker` クラスに `GameEventEmitterManager` を `@Autowired` で注入
2. `checkTurn` メソッドを以下のように変更:
   - 無限ループを削除
   - `GameEventEmitterManager.registerPlayerEmitter()` を呼び出してEmitter登録
   - 登録したEmitterを返却
3. 既存の `while(true)` ループと `Thread.sleep(1000)` を削除

**動作確認手順**:
- タスク4完了後に結合テストで確認

**入力**: プレイヤー名、ゲームID
**出力**: 登録されたSseEmitter

---

### タスク3: GameController へのイベント発火処理追加

**優先度**: 3

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/GameEventEmitterManager.java`

**作業内容**:
1. `GameController` クラスに `GameEventEmitterManager` を `@Autowired` で注入
2. `gameMove()` メソッドの `game.switchTurn()` 直後にイベント発火処理を追加:
   ```java
   // 次のターンのプレイヤー名を取得して通知
   String nextTurnPlayerName = getNextTurnPlayerName(game);
   gameEventEmitterManager.notifyTurnChange(game.getId(), nextTurnPlayerName);
   ```
3. `gamePutKoma()` メソッドにも同様のイベント発火処理を追加
4. ヘルパーメソッド `getNextTurnPlayerName(Game game)` を追加:
   - GAME_THINKING ステータスのプレイヤー名を返却

**動作確認手順**:
- タスク4完了後に結合テストで確認

**入力**: 駒移動リクエスト（fromX, fromY, toX, toY）
**出力**: ターン変更イベントがSSEで送信される

---

### タスク4: GameController の /game/turn エンドポイント修正

**優先度**: 4

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java`

**作業内容**:
1. `/game/turn` エンドポイントを修正:
   - 現在: `turnChecker.checkTurn(emitter, game, playerName)` を呼び出し
   - 変更後: `TurnChecker` 経由で `GameEventEmitterManager` にEmitter登録
2. 初回接続時に現在のターン状態を1回送信する処理を追加

**動作確認手順**:
- ユーザーが最後に確認を実施するため、現段階では不要、次のステップに移ってください

**入力**: プレイヤー名、ゲームID（URLパラメータ）
**出力**: 登録されたSseEmitter（ターン変更時に通知を受信）

---

### タスク5: WaitRoomEventEmitterManager サービスクラスの作成

**優先度**: 5

**関連ファイル**:
- 新規作成: `tkk_game/src/main/java/team3/tkk_game/services/WaitRoomEventEmitterManager.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/WaitRoom.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Game.java`

**作業内容**:
1. `tkk_game/src/main/java/team3/tkk_game/services/` に `WaitRoomEventEmitterManager.java` を新規作成
2. `@Component` アノテーションを付与
3. 以下のフィールドを定義:
   - `CopyOnWriteArrayList<SseEmitter> emitters` - 待機室監視用Emitter一覧
4. 以下のメソッドを実装:
   - `registerEmitter()` - Emitter登録
   - `notifyRoomListChange(ArrayList<Game> waitRoom)` - 部屋リスト変更通知
   - `removeEmitter(SseEmitter emitter)` - Emitter削除

**動作確認手順**:
- ユーザーが最後に確認を実施するため、現段階では不要、次のステップに移ってください

**入力**: なし（新規作成）
**出力**: WaitRoomEventEmitterManager クラス

---

### タスク6: WaitRoom へのイベント発火処理追加

**優先度**: 6

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/model/WaitRoom.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/WaitRoomEventEmitterManager.java`

**作業内容**:
1. `WaitRoom` クラスに `WaitRoomEventEmitterManager` を `@Autowired` で注入
2. `addWaitRoom()` メソッドの部屋追加後にイベント発火処理を追加:
   ```java
   waitRoomEventEmitterManager.notifyRoomListChange(waitRoom);
   ```
3. `rmRoom()` メソッドの部屋削除後に同様のイベント発火処理を追加
4. `sendRequest()` メソッドのリクエスト送信後に同様のイベント発火処理を追加
5. `clearRequest()` メソッドのリクエストクリア後に同様のイベント発火処理を追加

**動作確認手順**:
- タスク7完了後に結合テストで確認

**入力**: プレイヤー名（部屋操作パラメータ）
**出力**: 部屋リスト変更イベントがSSEで送信される

---

### タスク7: MatchChecker のイベント駆動化

**優先度**: 7

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/MatchChecker.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/WaitRoomEventEmitterManager.java`

**作業内容**:
1. `MatchChecker` クラスに `WaitRoomEventEmitterManager` を `@Autowired` で注入
2. `checkMatch` メソッドを以下のように変更:
   - 無限ループを削除
   - `WaitRoomEventEmitterManager.registerEmitter()` を呼び出してEmitter登録
   - 初回接続時に現在の待機室リストを1回送信
   - 登録したEmitterを返却
3. 既存の `while(true)` ループと `TimeUnit.SECONDS.sleep(1)` を削除

**動作確認手順**:
- ユーザーが最後に確認を実施するため、現段階では不要、次のステップに移ってください

**入力**: なし
**出力**: 登録されたSseEmitter（部屋リスト変更時に通知を受信）

---

### タスク8: 不要コードのクリーンアップ

**優先度**: 8

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/TurnChecker.java`
- 修正: `tkk_game/src/main/java/team3/tkk_game/services/MatchChecker.java`

**作業内容**:
1. `TurnChecker` から不要になったimport文を削除
2. `MatchChecker` から不要になったimport文を削除
3. 各ファイルのJavaDocコメントを更新（イベント駆動型であることを明記）

**入力**: なし
**出力**: クリーンアップされたソースコード

---

## 依存関係

```
タスク1 ──┬──▶ タスク2 ──┬──▶ タスク4
          │              │
          └──▶ タスク3 ──┘

タスク5 ──┬──▶ タスク6 ──┬──▶ タスク7
          │              │
          └──────────────┘

タスク4 ──┬──▶ タスク8
タスク7 ──┘
```
