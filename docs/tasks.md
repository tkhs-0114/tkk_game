# KomaRule JOINクエリによる駒データ取得機能の実装計画

## 計画作成日
2025年12月9日

## 目的
データベースのKOMA、RULE、KOMAROULEテーブルをJOINし、各駒のルール情報（`List<Integer>`）を含むゲーム用`Koma`オブジェクトを構築できるようにする。DB用のエンティティクラスとゲーム用のクラスを分離し、責務を明確化する。

## 前提条件
- H2データベースが設定済み（`application.properties`）
- `schema.sql`でKOMA、RULE、KOMAROULEテーブルが定義済み
- `data.sql`で初期データが投入済み
- MyBatisが依存関係に設定済み
- 現在の`Koma.java`はゲームロジック用として使用されている

## スコープ（含む）
- DB用エンティティクラス（`KomaEntity`、`KomaRuleRow`）の作成
- `KomaMapper`へのJOINクエリメソッド追加
- `KomaService`サービスクラスの作成（データ集約ロジック）
- ゲーム用`Koma`クラスへのgetter/setter追加
- 動作確認用のデバッグ用コントローラー追加

## スコープ（含まない）
- ゲームロジックの変更
- 画面への組み込み
- 他のエンティティ（Rule、Deck等）の拡張

## 関連ファイル一覧

### 既存ファイル（参照）
| ファイルパス | 役割 |
|------------|------|
| `tkk_game/src/main/resources/schema.sql` | テーブル定義 |
| `tkk_game/src/main/resources/data.sql` | 初期データ |
| `tkk_game/src/main/resources/application.properties` | DB設定 |

### 既存ファイル（修正対象）
| ファイルパス | 役割 |
|------------|------|
| `tkk_game/src/main/java/team3/tkk_game/model/Koma.java` | ゲーム用駒クラス |
| `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java` | 駒Mapperインターフェース |

### 新規作成ファイル
| ファイルパス | 役割 |
|------------|------|
| `tkk_game/src/main/java/team3/tkk_game/model/KomaEntity.java` | DB用駒エンティティ |
| `tkk_game/src/main/java/team3/tkk_game/model/KomaRuleRow.java` | JOINクエリ結果格納用 |
| `tkk_game/src/main/java/team3/tkk_game/service/KomaService.java` | 駒サービスクラス |

---

## タスク1: KomaEntityクラスの作成（DB用駒エンティティ）

### 関連ファイル
- 新規作成: `tkk_game/src/main/java/team3/tkk_game/model/KomaEntity.java`

### 作業内容
1. `model`パッケージに`KomaEntity.java`を作成
2. フィールド定義:
   - `Integer id` - 駒ID（主キー）
   - `String name` - 駒名
3. デフォルトコンストラクタを定義（MyBatisで必須）
4. getter/setterを定義

### 確認手順
- コンパイルエラーがないことを確認
- `./gradlew compileJava` が成功すること

### 期待結果
- `KomaEntity.java`が作成され、コンパイルが通る

---

## タスク2: KomaRuleRowクラスの作成（JOINクエリ結果格納用）

### 関連ファイル
- 新規作成: `tkk_game/src/main/java/team3/tkk_game/model/KomaRuleRow.java`

### 作業内容
1. `model`パッケージに`KomaRuleRow.java`を作成
2. フィールド定義:
   - `Integer komaId` - 駒ID
   - `String name` - 駒名
   - `Integer ruleId` - ルールID
3. デフォルトコンストラクタを定義
4. getter/setterを定義

### 確認手順
- コンパイルエラーがないことを確認
- `./gradlew compileJava` が成功すること

### 期待結果
- `KomaRuleRow.java`が作成され、コンパイルが通る

---

## タスク3: KomaMapperへのJOINクエリメソッド追加

### 関連ファイル
- 修正: `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/KomaEntity.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/KomaRuleRow.java`

### 作業内容
1. `KomaEntity`のimportを追加
2. `KomaRuleRow`のimportを追加
3. 既存の`selectAllKoma`メソッドの戻り値を`List<KomaEntity>`に変更
4. 新しいメソッド`selectAllKomaWithRules`を追加:
   ```java
   @Select("SELECT KOMARULE.koma_id AS komaId, KOMA.name AS name, KOMARULE.rule_id AS ruleId " +
           "FROM KOMARULE " +
           "JOIN KOMA ON KOMARULE.koma_id = KOMA.id " +
           "JOIN RULE ON KOMARULE.rule_id = RULE.id " +
           "ORDER BY KOMARULE.koma_id")
   List<KomaRuleRow> selectAllKomaWithRules();
   ```

### 確認手順
- コンパイルエラーがないことを確認
- `./gradlew compileJava` が成功すること

### 期待結果
- `KomaMapper`に2つのメソッドが定義される
  - `selectAllKoma()` - 全駒を取得（`List<KomaEntity>`）
  - `selectAllKomaWithRules()` - 駒とルールの組み合わせを取得（`List<KomaRuleRow>`）

---

## タスク4: Komaクラスへのgetter/setter追加

### 関連ファイル
- 修正: `tkk_game/src/main/java/team3/tkk_game/model/Koma.java`

### 作業内容
1. デフォルトコンストラクタを追加
2. `rules`フィールドのgetterを追加: `getRules()`
3. `rules`フィールドのsetterを追加: `setRules(List<Integer> rules)`

### 確認手順
- コンパイルエラーがないことを確認
- `./gradlew compileJava` が成功すること

### 期待結果
- `Koma`クラスに`getRules()`と`setRules()`が追加される
- 既存のゲームロジックに影響がない

---

## タスク5: KomaServiceクラスの作成

### 関連ファイル
- 新規作成: `tkk_game/src/main/java/team3/tkk_game/service/KomaService.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/Koma.java`
- 参照: `tkk_game/src/main/java/team3/tkk_game/model/KomaRuleRow.java`

### 作業内容
1. `service`パッケージに`KomaService.java`を作成
2. `@Service`アノテーションを付与
3. `KomaMapper`を`@Autowired`で注入
4. `getAllKomaWithRules()`メソッドを実装:
   - `komaMapper.selectAllKomaWithRules()`でJOINデータを取得
   - `LinkedHashMap<Integer, Koma>`を使って`komaId`ごとに集約
   - 各駒の`rules`リストにルールIDを追加
   - `List<Koma>`として返却

### 確認手順
- コンパイルエラーがないことを確認
- `./gradlew compileJava` が成功すること

### 期待結果
- `KomaService`が作成され、`getAllKomaWithRules()`メソッドが定義される

---

## タスク6: 動作確認用コントローラーの作成

### 関連ファイル
- 修正: `tkk_game/src/main/java/team3/tkk_game/controller/DebugController.java`（存在しない場合は新規作成）
- 参照: `tkk_game/src/main/java/team3/tkk_game/service/KomaService.java`

### 作業内容
1. `DebugController`クラスを作成（または既存を修正）
2. `@Controller`アノテーションを付与
3. `KomaService`を`@Autowired`で注入
4. `/debug/koma`エンドポイントを追加:
   - `KomaService.getAllKomaWithRules()`を呼び出し
   - 結果をモデルに追加
   - `debug.html`に遷移

### 確認手順
- `./gradlew bootRun` でアプリを起動
- ブラウザで `http://localhost/login` にアクセスし、`user1 / p@ss` でログイン
- `http://localhost/debug/koma` にアクセス

### 期待結果
- 駒の一覧とそれぞれのルールIDリストが表示される

---

## タスク7: デバッグ画面の更新

### 関連ファイル
- 修正: `tkk_game/src/main/resources/templates/debug.html`

### 作業内容
1. 駒一覧表示用のHTMLを追加
2. 各駒の`id`、`name`、`rules`を表示

### 確認手順
- `./gradlew bootRun` でアプリを起動
- ブラウザで `http://localhost/login` にアクセスし、`user1 / p@ss` でログイン
- `http://localhost/debug/koma` にアクセス
- 以下の情報が表示されることを確認:
  - 歩 (id=1): rules=[1]
  - 銀 (id=4): rules=[1, 9, 11, 13, 15]

### 期待結果
- 駒とルールの関連が正しく表示される

---

## Definition of Done (DoD)

1. `./gradlew bootRun` でアプリが正常起動する
2. ブラウザで `http://localhost/login` にアクセスし、`user1 / p@ss` でログインできる
3. `http://localhost/debug/koma` にアクセスすると駒一覧が表示される
4. 駒「歩」のルールリストに `[1]` が表示される
5. 駒「銀」のルールリストに `[1, 9, 11, 13, 15]` が表示される
6. 既存のゲーム機能（マッチング、ゲーム画面）が正常に動作する

---

## 実装の流れ

```
タスク1 → タスク2 → タスク3 → タスク4 → タスク5 → タスク6 → タスク7
   ↓          ↓          ↓          ↓          ↓          ↓          ↓
KomaEntity  KomaRuleRow  Mapper    Koma修正   Service   Controller  画面
  作成        作成       修正                  作成       修正      修正
```

## 注意事項

- 各タスクは順番に実施すること（依存関係があるため）
- タスク完了ごとにコンパイルエラーがないことを確認すること
- 既存のゲームロジックに影響を与えないこと
- DB用クラス（`KomaEntity`、`KomaRuleRow`）とゲーム用クラス（`Koma`）の役割を混同しないこと
