# canMoveメソッドのサービスクラス分離計画

## 計画作成日
2025年12月23日

## 目的
`GameController` 内の `canMove` メソッドを `MoveValidator` サービスクラスに分離し、責務の分離とテスタビリティの向上を図る。

## 前提条件
- 既存のサービスクラス (`TurnChecker`, `MatchChecker`) が `tkk_game/src/main/java/team3/tkk_game/services/` に存在
- `canMove` メソッドは現在 `GameController` 内の private メソッドとして実装済み
- Spring の `@Service` アノテーションと `@Autowired` による DI を使用

## スコープ（含む）
- `MoveValidator` サービスクラスの新規作成
- `GameController` から `canMove` メソッドの移動
- `GameController` での `MoveValidator` の DI と利用

## スコープ（含まない）
- 移動ルールの変更・追加
- 駒を取る処理の実装
- テストコードの作成

---

## タスク一覧

### タスク1: MoveValidator サービスクラスの作成

**優先度**: 1（最優先）

**関連ファイル**:
- 新規作成: `tkk_game/src/main/java/team3/tkk_game/services/MoveValidator.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java` (canMove メソッド)
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Ban.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/Koma.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaRule.java`

**作業内容**:
1. `tkk_game/src/main/java/team3/tkk_game/services/` に `MoveValidator.java` を新規作成
2. `@Service` アノテーションを付与
3. `canMove(Ban ban, int fromX, int fromY, int toX, int toY)` メソッドを実装
   - `GameController` の既存 `canMove` メソッドのロジックをそのまま移植
   - JavaDoc コメントを追加
4. 必要な import 文を追加:
   - `org.springframework.stereotype.Service`
   - `team3.tkk_game.model.Ban`
   - `team3.tkk_game.model.Koma.Koma`
   - `team3.tkk_game.model.Koma.KomaRule`
   - `java.util.List`

**確認手順**:
- 最後にユーザーが確認します。次のステップに進んでください

**期待結果**:
- `MoveValidator.java` が作成され、コンパイルが成功する

---

### タスク2: GameController の修正

**優先度**: 2

**関連ファイル**:
- 編集: `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/services/MoveValidator.java`

**作業内容**:
1. `MoveValidator` の import 文を追加
   - `import team3.tkk_game.services.MoveValidator;`
2. `@Autowired` で `MoveValidator` を DI
   ```java
   @Autowired
   MoveValidator moveValidator;
   ```
3. `gameMove` メソッド内の `canMove()` 呼び出しを `moveValidator.canMove()` に変更
   - 変更前: `Boolean canMove = canMove(game.getBan(), fromX, fromY, toX, toY);`
   - 変更後: `boolean canMove = moveValidator.canMove(game.getBan(), fromX, fromY, toX, toY);`
4. `GameController` 内の private `canMove` メソッドを削除

**確認手順**:
- 最後にユーザーが確認します。次のステップに進んでください

**期待結果**:
- `GameController` から `canMove` メソッドが削除され、`MoveValidator` を利用するようになる

---

### タスク3: 動作確認

**優先度**: 3

**関連ファイル**:
- `tkk_game/src/main/java/team3/tkk_game/services/MoveValidator.java`
- `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java`

**作業内容**:
1. アプリケーションを起動して動作確認
2. 駒の移動が正常に動作することを確認

**確認手順**:
- ユーザーが手作業で確認します。完成した旨を伝えてください

**期待結果**:
- 駒の移動が従来通り正常に動作する

---

## 注意事項

- 移動ルールのロジックは変更しないこと（単純な移植のみ）
- 駒を取る処理は本計画のスコープ外（既存のまま）
- テストコードの作成は本計画のスコープ外
- 実装完了後、`docs/specs.md` のサービス層に `MoveValidator` の説明を追加すること

---

## 変更対象ファイル一覧

| ファイルパス | 変更種別 |
|-------------|---------|
| `tkk_game/src/main/java/team3/tkk_game/services/MoveValidator.java` | 新規作成 |
| `tkk_game/src/main/java/team3/tkk_game/controller/GameController.java` | 編集 |
| `docs/specs.md` | 編集（実装完了後） |
