package team3.tkk_game.model.Ban;

import team3.tkk_game.model.Koma;
import team3.tkk_game.model.KomaPattern;
import team3.tkk_game.model.Player;

// ローカル盤面
// 各プレイヤーが持つ盤面、自分の駒だけを管理する
public class LocalBan extends Ban {
  Player owner;

  public LocalBan(Player owner) {
    super();
    this.owner = owner;
    putKoma(0, -2, new Koma("王", new KomaPattern[] {
        new KomaPattern(1, 0),
        new KomaPattern(0, 1),
        new KomaPattern(-1, 0),
        new KomaPattern(0, -1),
        new KomaPattern(1, 1),
        new KomaPattern(1, -1),
        new KomaPattern(-1, 1),
        new KomaPattern(-1, -1)
    }, owner));
  }

  public boolean moveKoma(int fromX, int fromY, int toX, int toY) {
    System.out.println("x:" + fromX + "  y:" + fromY + "  toX:" + toX + "  toY:" + toY);
    if (board[b2a(fromX)][b2a(fromY)] == null ||
        board[b2a(toX)][b2a(toY)] != null) {
      return false; // 移動元に駒がないor移動先に駒がある場合は失敗
    }
    board[b2a(toX)][b2a(toY)] = board[b2a(fromX)][b2a(fromY)];
    board[b2a(fromX)][b2a(fromY)] = null;
    return true;
  }
}
