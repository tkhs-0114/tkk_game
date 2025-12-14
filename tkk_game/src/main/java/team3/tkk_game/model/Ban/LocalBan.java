package team3.tkk_game.model.Ban;

import team3.tkk_game.model.Player;

// ローカル盤面
// 各プレイヤーが持つ盤面、自分の駒だけを管理する
public class LocalBan extends Ban {
  private Player owner;

  public LocalBan(Player owner) {
    super();
    this.owner = owner;
  }

  public Player getOwner() {
    return owner;
  }
}
