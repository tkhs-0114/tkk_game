package team3.tkk_game.model.Koma;

public enum KomaRule {
  UP(1),
  DOWN(1),
  LEFT(1),
  RIGHT(1),
  UP_LEFT(1),
  UP_RIGHT(1),
  DOWN_LEFT(1),
  DOWN_RIGHT(1),
  LINE_UP(3),
  LINE_DOWN(3),
  LINE_LEFT(3),
  LINE_RIGHT(3),
  LINE_UP_LEFT(3),
  LINE_UP_RIGHT(3),
  LINE_DOWN_LEFT(3),
  LINE_DOWN_RIGHT(3),
  JUMP_UP_LEFT(2),
  JUMP_UP_RIGHT(2),
  JUMP_DOWN_LEFT(2),
  JUMP_DOWN_RIGHT(2),
  JUMP_LEFT_UP(2),
  JUMP_LEFT_DOWN(2),
  JUMP_RIGHT_UP(2),
  JUMP_RIGHT_DOWN(2);

  private final int cost;

  /**
   * コンストラクタ
   *
   * @param cost 移動ルールのコスト
   */
  KomaRule(int cost) {
    this.cost = cost;
  }

  /**
   * 移動ルールのコストを取得
   *
   * @return コスト値
   */
  public int getCost() {
    return cost;
  }
}
