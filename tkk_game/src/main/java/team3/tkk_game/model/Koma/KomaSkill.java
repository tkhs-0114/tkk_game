package team3.tkk_game.model.Koma;

public enum KomaSkill {
  NULL(0),
  STEALTH(5),
  COPY(4);

  private final int cost;

  /**
   * コンストラクタ
   *
   * @param cost スキルのコスト
   */
  KomaSkill(int cost) {
    this.cost = cost;
  }

  /**
   * スキルのコストを取得
   *
   * @return コスト値
   */
  public int getCost() {
    return cost;
  }
}
