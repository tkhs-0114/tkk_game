package team3.tkk_game.model;

/**
 * JOINクエリ結果格納用クラス
 * KOMARULE、KOMA、RULEテーブルをJOINした1行分のデータを格納する
 */
public class KomaRuleRow {

    /** 駒ID */
    private Integer komaId;

    /** 駒名 */
    private String name;

    /** ルールID */
    private Integer ruleId;

    /**
     * デフォルトコンストラクタ（MyBatisで必須）
     */
    public KomaRuleRow() {
    }

    /**
     * 駒IDを取得する
     * @return 駒ID
     */
    public Integer getKomaId() {
        return komaId;
    }

    /**
     * 駒IDを設定する
     * @param komaId 駒ID
     */
    public void setKomaId(Integer komaId) {
        this.komaId = komaId;
    }

    /**
     * 駒名を取得する
     * @return 駒名
     */
    public String getName() {
        return name;
    }

    /**
     * 駒名を設定する
     * @param name 駒名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * ルールIDを取得する
     * @return ルールID
     */
    public Integer getRuleId() {
        return ruleId;
    }

    /**
     * ルールIDを設定する
     * @param ruleId ルールID
     */
    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }
}
