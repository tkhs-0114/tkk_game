# KomaRule JOINクエリによるデータ取得方法調査

## 調査日時
2025年12月9日

## 調査目的
データベースから以下のSQLクエリを使用して情報を取得する方法を調査する：
```sql
SELECT KOMA_id, NAME, RULE_id
FROM KOMARULE
JOIN KOMA ON KOMARULE.koma_id = KOMA.id
JOIN RULE ON RULE_id = RULE.id;
```

## 現状のテーブル構造

### schema.sql
```sql
CREATE TABLE koma(
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE Rule(
  id INT PRIMARY KEY AUTO_INCREMENT,
  text VARCHAR(50) NOT NULL
);

CREATE TABLE KomaRule(
  id INT PRIMARY KEY AUTO_INCREMENT,
  koma_id INT,
  rule_id INT,
  FOREIGN KEY (koma_id) REFERENCES koma(id),
  FOREIGN KEY (rule_id) REFERENCES Rule(id)
);
```

### 現状のKomaMapperとKomaモデル

#### KomaMapper.java
```java
@Mapper
public interface KomaMapper {
  @Select("SELECT id,name FROM koma")
  List<Koma> selectAllKoma();
}
```

#### Koma.java
```java
public class Koma {
  String name;
  Integer id;
  List<Integer> rules = new ArrayList<>();
  // getter/setter...
}
```

## 調査結果

### 参考例：igakilab/springboot_samples のJOINクエリ実装

GitHubリポジトリ `igakilab/springboot_samples` に良い参考例がありました。

#### 参考例：ChamberMapper.java（JOINを使用）
```java
@Mapper
public interface ChamberMapper {
  /**
   * DBのカラム名とjavaクラスのフィールド名が同じ場合はそのまま代入してくれる（大文字小文字の違いは無視される）
   * カラム名とフィールド名が異なる場合の対応も可能だが，いきなり複雑になるので，
   * selectで指定するテーブル中のカラム名とクラスのフィールド名は同一になるよう設計することが望ましい
   */
  @Select("SELECT chamber.userName,chamber.chamberName,userinfo.age,userinfo.height from chamber JOIN userinfo ON chamber.userName=userinfo.userName;")
  ArrayList<ChamberUser> selectAllChamberUser();
}
```

#### 参考例：ChamberUser.java（JOINの結果を格納するモデルクラス）
```java
public class ChamberUser {
  String userName;
  String chamberName;
  int age;
  double height;

  // getter/setter...
}
```

### 実装方法

JOINクエリの結果を取得するには、以下の手順が必要です：

#### 1. JOINクエリの結果を格納する新しいモデルクラスを作成する

JOINクエリは複数のテーブルからデータを結合するため、結果を格納するための専用のモデルクラスを作成する必要があります。

**重要なポイント：**
- SELECTで指定するカラム名とJavaクラスのフィールド名は同一にすることが推奨される
- 大文字・小文字の違いは無視される（MyBatisが自動的にマッピング）

#### 2. 新しいモデルクラス例：KomaRule.java

```java
package team3.tkk_game.model;

/**
 * KomaRuleテーブルのJOIN結果を格納するモデルクラス
 * KOMARULE, KOMA, RULEテーブルを結合したデータを保持する
 */
public class KomaRule {
    private Integer komaId;      // KOMA_id に対応
    private String name;         // KOMA.name に対応
    private Integer ruleId;      // RULE_id に対応

    // getter/setter
    public Integer getKomaId() {
        return komaId;
    }

    public void setKomaId(Integer komaId) {
        this.komaId = komaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }
}
```

#### 3. Mapperへのメソッド追加例

```java
@Mapper
public interface KomaMapper {

  @Select("SELECT id,name FROM koma")
  List<Koma> selectAllKoma();

  /**
   * KomaRuleテーブルをKOMA、RULEテーブルと結合し、駒のIDと名前、ルールIDを取得する
   * @return 結合結果のリスト
   */
  @Select("SELECT KOMARULE.koma_id AS komaId, KOMA.name AS name, KOMARULE.rule_id AS ruleId " +
          "FROM KOMARULE " +
          "JOIN KOMA ON KOMARULE.koma_id = KOMA.id " +
          "JOIN RULE ON KOMARULE.rule_id = RULE.id")
  List<KomaRule> selectAllKomaRule();
}
```

### 重要な注意点

#### カラム名とフィールド名のマッピング

1. **AS句を使用してカラム名を明示的に指定する**
   - SQLで取得するカラム名とJavaクラスのフィールド名が異なる場合、`AS`句を使用してエイリアスを設定する
   - 例：`KOMARULE.koma_id AS komaId`

2. **アンダースコア（snake_case）とキャメルケース（camelCase）の変換**
   - MyBatisはデフォルトでスネークケースをキャメルケースに自動変換する設定が可能
   - `application.properties`に以下を追加することで自動変換を有効化できる：
     ```properties
     mybatis.configuration.map-underscore-to-camel-case=true
     ```
   - この設定を有効にすると、`koma_id`が自動的に`komaId`にマッピングされる

#### 代替案：Ruleのテキスト情報も取得する場合

もしRuleテーブルのtext情報も必要な場合は、以下のようにモデルとクエリを拡張できます：

```java
// KomaRuleDetail.java
public class KomaRuleDetail {
    private Integer komaId;
    private String komaName;
    private Integer ruleId;
    private String ruleText;  // RULE.text の情報も取得

    // getter/setter...
}

// KomaMapper.java
@Select("SELECT KOMARULE.koma_id AS komaId, KOMA.name AS komaName, " +
        "KOMARULE.rule_id AS ruleId, RULE.text AS ruleText " +
        "FROM KOMARULE " +
        "JOIN KOMA ON KOMARULE.koma_id = KOMA.id " +
        "JOIN RULE ON KOMARULE.rule_id = RULE.id")
List<KomaRuleDetail> selectAllKomaRuleDetail();
```

## 推奨事項

1. **新しいモデルクラスの作成**
   - JOINクエリの結果を格納する専用のモデルクラス（例：`KomaRule.java`）を `tkk_game/src/main/java/team3/tkk_game/model/` に作成する

2. **カラム名のエイリアス設定**
   - SELECTクエリで`AS`句を使用し、Javaのフィールド名と一致させる
   - または `application.properties` でアンダースコアからキャメルケースへの自動変換を有効化する

3. **KomaMapperへのメソッド追加**
   - 新しいSELECTクエリを`@Select`アノテーションで追加する

4. **ファイル構成**
   - `tkk_game/src/main/java/team3/tkk_game/model/KomaRule.java` - 新規作成
   - `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java` - メソッド追加

## 参考URL

- GitHub igakilab/springboot_samples リポジトリ
  - ChamberMapper.java: https://github.com/igakilab/springboot_samples/blob/main/src/main/java/oit/is/inudaisuki/springboot_samples/model/ChamberMapper.java
  - ChamberUser.java: https://github.com/igakilab/springboot_samples/blob/main/src/main/java/oit/is/inudaisuki/springboot_samples/model/ChamberUser.java

## 追加調査：Komaクラスのrules配列にルールIDを代入する方法

### 現在のKomaクラスの構造

```java
public class Koma {
  String name;
  Integer id;
  List<Integer> rules = new ArrayList<>();  // ここにルールIDのリストを格納したい

  public Koma(String name, List<Integer> rules) {
    this.name = name;
    this.rules = rules;
  }
  // getter/setter...
}
```

### 課題

MyBatisの`@Select`アノテーションによる単純なSELECTクエリでは、1つのレコードが1つのオブジェクトにマッピングされるため、`List<Integer>`型のフィールドに直接複数の値を格納することはできません。

### 解決策

#### 方法1：Javaコード側で集約する（推奨）

JOINクエリで取得したフラットなデータをJavaコード側で集約してKomaオブジェクトを構築する方法です。

**Step 1: JOINの結果を格納するシンプルなクラスを作成**

```java
// KomaRuleRow.java - 1行分のデータを格納
package team3.tkk_game.model;

public class KomaRuleRow {
    private Integer komaId;
    private String name;
    private Integer ruleId;

    // getter/setter
    public Integer getKomaId() { return komaId; }
    public void setKomaId(Integer komaId) { this.komaId = komaId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getRuleId() { return ruleId; }
    public void setRuleId(Integer ruleId) { this.ruleId = ruleId; }
}
```

**Step 2: MapperでJOINクエリを定義**

```java
@Mapper
public interface KomaMapper {

  @Select("SELECT id, name FROM koma")
  List<Koma> selectAllKoma();

  /**
   * KomaRuleテーブルとKOMA、RULEテーブルをJOINし、全ての駒とルールの組み合わせを取得する
   * @return 結合結果のリスト（1駒1ルールで1行）
   */
  @Select("SELECT KOMARULE.koma_id AS komaId, KOMA.name AS name, KOMARULE.rule_id AS ruleId " +
          "FROM KOMARULE " +
          "JOIN KOMA ON KOMARULE.koma_id = KOMA.id " +
          "JOIN RULE ON KOMARULE.rule_id = RULE.id " +
          "ORDER BY KOMARULE.koma_id")
  List<KomaRuleRow> selectAllKomaWithRules();
}
```

**Step 3: Serviceクラスでデータを集約**

```java
// KomaService.java
package team3.tkk_game.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.model.Koma;
import team3.tkk_game.model.KomaRuleRow;

@Service
public class KomaService {

    @Autowired
    KomaMapper komaMapper;

    /**
     * 全ての駒をルール付きで取得する
     * JOINクエリの結果を駒ごとに集約し、各駒のrulesリストにルールIDを格納する
     * @return 駒のリスト（各駒にルールIDのリストが設定されている）
     */
    public List<Koma> getAllKomaWithRules() {
        // JOINクエリで全データを取得
        List<KomaRuleRow> rows = komaMapper.selectAllKomaWithRules();

        // komaIdをキーにしてKomaオブジェクトを集約
        Map<Integer, Koma> komaMap = new LinkedHashMap<>();

        for (KomaRuleRow row : rows) {
            Integer komaId = row.getKomaId();

            if (!komaMap.containsKey(komaId)) {
                // 新しい駒を作成
                List<Integer> rules = new ArrayList<>();
                rules.add(row.getRuleId());
                Koma koma = new Koma(row.getName(), rules);
                koma.setId(komaId);
                komaMap.put(komaId, koma);
            } else {
                // 既存の駒にルールを追加
                komaMap.get(komaId).getRules().add(row.getRuleId());
            }
        }

        return new ArrayList<>(komaMap.values());
    }
}
```

**Step 4: Komaクラスにrulesのgetterを追加**

現在のKomaクラスには`rules`のgetterがないため、追加が必要です：

```java
public class Koma {
  String name;
  Integer id;
  List<Integer> rules = new ArrayList<>();

  public Koma() {
    // デフォルトコンストラクタ（MyBatisで必要）
  }

  public Koma(String name, List<Integer> rules) {
    this.name = name;
    this.rules = rules;
  }

  // ...既存のgetter/setter...

  public List<Integer> getRules() {
    return rules;
  }

  public void setRules(List<Integer> rules) {
    this.rules = rules;
  }
}
```

#### 方法2：XMLマッパーを使用する（高度）

MyBatisのXMLマッパーと`<resultMap>`、`<collection>`タグを使用すると、ネストしたオブジェクトを直接マッピングできます。ただし、アノテーションベースからXMLベースに変更が必要となり、設定が複雑になります。

```xml
<!-- KomaMapper.xml -->
<resultMap id="komaWithRulesResultMap" type="team3.tkk_game.model.Koma">
    <id property="id" column="koma_id"/>
    <result property="name" column="name"/>
    <collection property="rules" ofType="java.lang.Integer">
        <result column="rule_id"/>
    </collection>
</resultMap>

<select id="selectAllKomaWithRules" resultMap="komaWithRulesResultMap">
    SELECT KOMA.id AS koma_id, KOMA.name AS name, KOMARULE.rule_id AS rule_id
    FROM KOMA
    LEFT JOIN KOMARULE ON KOMA.id = KOMARULE.koma_id
    ORDER BY KOMA.id
</select>
```

**この方法の注意点：**
- XMLファイルの設定が必要
- `resources`フォルダにMapperと同じパッケージ構造でXMLを配置する必要がある
- 現在のアノテーションベースのMapperとの混在は可能

### 推奨する実装方法

**方法1（Javaコード側で集約）を推奨します。**

理由：
1. 現在のアノテーションベースのMapper構造を維持できる
2. 処理が明確で理解しやすい
3. デバッグが容易
4. 将来の拡張が容易

### 実装に必要なファイル

| ファイルパス | 操作 | 内容 |
|------------|------|------|
| `tkk_game/src/main/java/team3/tkk_game/model/KomaRuleRow.java` | 新規作成 | JOINクエリの1行分を格納するクラス |
| `tkk_game/src/main/java/team3/tkk_game/model/Koma.java` | 修正 | デフォルトコンストラクタとrules用getter/setterを追加 |
| `tkk_game/src/main/java/team3/tkk_game/mapper/KomaMapper.java` | 修正 | selectAllKomaWithRulesメソッドを追加 |
| `tkk_game/src/main/java/team3/tkk_game/service/KomaService.java` | 新規作成 | データを集約するサービスクラス |

## 次のアクション

1. ユーザーは上記の実装方針を確認する
2. 計画フェーズで具体的な実装計画を立てる
3. 実装フェーズで以下を実施：
   - `KomaRuleRow.java` モデルクラスの作成
   - `Koma.java` への getter/setter 追加
   - `KomaMapper.java` へのメソッド追加
   - `KomaService.java` サービスクラスの作成
