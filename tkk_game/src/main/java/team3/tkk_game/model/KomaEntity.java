package team3.tkk_game.model;

/**
 * DB用駒エンティティクラス
 * KOMAテーブルのレコードを格納する
 */
public class KomaEntity {

    /** 駒ID（主キー） */
    private Integer id;

    /** 駒名 */
    private String name;

    /**
     * デフォルトコンストラクタ（MyBatisで必須）
     */
    public KomaEntity() {
    }

    /**
     * 駒IDを取得する
     * @return 駒ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 駒IDを設定する
     * @param id 駒ID
     */
    public void setId(Integer id) {
        this.id = id;
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
}
