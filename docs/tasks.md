# コストシステム実装計画

## 計画作成日
2026年01月05日

## 目的
デッキと駒にコストの概念を導入し、デッキ作成画面でコストを表示できるようにする。

## 背景・要件

### 基本要件
1. **駒のコスト**: 駒が持つすべての移動ルールのコスト合計 + スキルのコスト
2. **デッキのコスト**: デッキに含まれる駒のコストの合計
3. **表示場所**: デッキ作成画面（`deckmake.html`）のみ
4. **コスト制限**: 今回は実装せず、表示のみ

### 実装方針
- `KomaRule` と `KomaSkill` の列挙型にコストフィールドを追加（enum定数方式）
- データベーススキーマの変更は不要
- コスト値：単マス移動=1、直線移動=3、ジャンプ移動=2、NULL=0、STEALTH=5

## スコープ（含む）
- `KomaRule` enum へのコストフィールド追加
- `KomaSkill` enum へのコストフィールド追加
- `Koma` クラスへのコスト計算メソッド追加
- `KomaDB` クラスへのコスト計算メソッド追加
- デッキ作成画面でのコスト表示UI追加

## スコープ（含まない）
- コスト上限の制限機能
- デッキ選択画面でのコスト表示
- コスト値のデータベース保存
- 管理画面でのコスト値変更機能

---

## タスク一覧

### タスク1: KomaRule enum にコストフィールドを追加

**優先度**: 1（最優先）

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaRule.java`

**作業内容**:
1. `KomaRule` enum の各定数にコスト値をコンストラクタ引数として追加
   - 単マス移動（`UP`, `DOWN`, `LEFT`, `RIGHT`, `UP_LEFT`, `UP_RIGHT`, `DOWN_LEFT`, `DOWN_RIGHT`）: 1
   - 直線移動（`LINE_UP`, `LINE_DOWN`, `LINE_LEFT`, `LINE_RIGHT`, `LINE_UP_LEFT`, `LINE_UP_RIGHT`, `LINE_DOWN_LEFT`, `LINE_DOWN_RIGHT`）: 3
   - ジャンプ移動（`JUMP_UP_LEFT`, `JUMP_UP_RIGHT`, `JUMP_DOWN_LEFT`, `JUMP_DOWN_RIGHT`, `JUMP_LEFT_UP`, `JUMP_LEFT_DOWN`, `JUMP_RIGHT_UP`, `JUMP_RIGHT_DOWN`）: 2
2. `private final int cost;` フィールドを追加
3. コンストラクタ `KomaRule(int cost)` を追加
4. `public int getCost()` メソッドを追加

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle build` コマンドが成功することを確認

**入力**: なし（enum定義の修正）
**出力**: コストフィールドを持つKomaRule enum

---

### タスク2: KomaSkill enum にコストフィールドを追加

**優先度**: 2

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaSkill.java`

**作業内容**:
1. `KomaSkill` enum の各定数にコスト値をコンストラクタ引数として追加
   - `NULL`: 0
   - `STEALTH`: 5
2. `private final int cost;` フィールドを追加
3. コンストラクタ `KomaSkill(int cost)` を追加
4. `public int getCost()` メソッドを追加

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle build` コマンドが成功することを確認

**入力**: なし（enum定義の修正）
**出力**: コストフィールドを持つKomaSkill enum

---

### タスク3: Koma クラスにコスト計算メソッドを追加

**優先度**: 3

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/model/Koma/Koma.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaRule.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaSkill.java`

**作業内容**:
1. `Koma` クラスに `public int getCost()` メソッドを追加
2. メソッド内で以下の処理を実装:
   - 移動ルールのコスト合計を計算（`rules` リストをループして各ルールの `getCost()` を合計）
   - スキルのコストを加算（`skill.getCost()`）
   - 合計コストを返却

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle build` コマンドが成功することを確認

**入力**: なし（Komaオブジェクト内のrulesとskillを使用）
**出力**: 駒の合計コスト（int）

---

### タスク4: KomaDB クラスにコスト計算メソッドを追加

**優先度**: 4

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaDB.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaRule.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaSkill.java`

**作業内容**:
1. `KomaDB` クラスに `public int calculateCost(List<KomaRule> rules)` メソッドを追加
2. メソッド内で以下の処理を実装:
   - 引数で渡された移動ルールのコスト合計を計算
   - スキルのコストを加算（`skill` が null でない場合は `KomaSkill.valueOf(skill).getCost()` を加算）
   - 合計コストを返却
3. このメソッドは、データベースから取得した駒情報のコストを計算するために使用

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle build` コマンドが成功することを確認

**入力**: 移動ルールのリスト（`List<KomaRule>`）
**出力**: 駒の合計コスト（int）

---

### タスク5: DeckController で駒リストにコスト情報を含めて渡す

**優先度**: 5

**関連ファイル**:
- 修正: `tkk_game/src/main/java/team3/tkk_game/controller/DeckController.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma/KomaDB.java`

**作業内容**:
1. `DeckController` の `deckmake()` メソッドを修正
2. `komaMapper.selectAllKoma()` で取得した駒リストの各駒について、コスト情報を計算
3. 駒リストと一緒に、各駒のコスト情報をモデルに追加する方法を検討:
   - 方法A: `KomaDB` に一時的にコストフィールドを追加し、計算結果を設定
   - 方法B: `Map<Integer, Integer>` でIDとコストの対応を作成し、別途モデルに追加
4. 駒のルールリストを `komaMapper.selectKomaRuleById()` で取得し、`KomaDB.calculateCost()` でコスト計算

**動作確認手順**:
- アプリケーションが正常にコンパイルできることを確認
- `gradle bootRun` でアプリケーションを起動
- ブラウザで `http://localhost:80/deck/make` にアクセス
- Thymeleaf でコスト情報が利用可能になっていることを確認（次のタスクで表示）

**入力**: なし（コントローラーメソッド内で駒情報を取得）
**出力**: 駒リストとコスト情報がモデルに設定される

---

### タスク6: deckmake.html にコスト表示UIを追加

**優先度**: 6

**関連ファイル**:
- 修正: `tkk_game/src/main/resources/templates/deckmake.html`

**作業内容**:
1. デッキ作成画面に以下のコスト表示要素を追加:
   - 駒選択セレクトボックスの隣に「選択中の駒のコスト」を表示
   - 盤面の上または下に「デッキの合計コスト」を表示
2. JavaScript で以下の機能を実装:
   - 駒選択時に、選択した駒のコストを表示エリアに表示
   - 盤面に駒を配置したら、配置されている全ての駒のコストを合計してデッキコストを更新
   - 駒のデータ属性に `data-cost` を追加して、コスト計算に使用
3. セレクトボックスの各 `<option>` にコスト情報を `data-cost` 属性として追加:
   ```html
   <option th:each="k : ${komas}" th:value="${k.id}" th:text="${k.name}" th:data-cost="${komaCosts[k.id]}">駒名</option>
   ```
4. CSS で見やすくスタイリング

**動作確認手順**:
- `gradle bootRun` でアプリケーションを起動
- ブラウザで `http://localhost:80/` にアクセスし、`user1`/`p@ss` でログイン
- ホーム画面から「デッキ作成」リンクをクリック
- デッキ作成画面で駒を選択した時に、その駒のコストが表示されることを確認
- 盤面に駒を配置すると、デッキの合計コストが更新されることを確認
- 複数の駒を配置して、合計コストが正しく計算されることを確認

**入力**: ユーザーの駒選択・配置操作
**出力**: 画面上にコスト情報がリアルタイム表示される

---

## Definition of Done (DoD)

すべてのタスクが正常に完了した後、以下の手順で動作確認を実施します。

### 確認手順
1. `gradle build` を実行し、コンパイルエラーがないことを確認
2. `gradle bootRun` でアプリケーションを起動
3. ブラウザで `http://localhost:80/` にアクセス
4. `user1` / `p@ss` でログイン
5. ホーム画面から「デッキ作成」をクリック
6. デッキ作成画面が表示される
7. 駒選択セレクトボックスで駒を選択
8. 選択した駒のコストが表示される（例：王将 = 8、歩兵 = 1、飛車 = 12 など）
9. 盤面のマス目をクリックして駒を配置
10. デッキの合計コストが更新される
11. 複数の駒を配置し、合計コストが正しく加算されることを確認
12. 既存のデッキ作成機能（デッキ名入力、SFEN生成、保存）が正常に動作することを確認

### 期待される動作
- 駒ごとに正しいコストが計算・表示される
- デッキの合計コストがリアルタイムに更新される
- コスト表示があっても、既存のデッキ作成・保存機能が正常に動作する
- コンパイルエラーや実行時エラーが発生しない
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
