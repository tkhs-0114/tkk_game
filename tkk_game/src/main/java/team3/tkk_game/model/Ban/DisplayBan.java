package team3.tkk_game.model.Ban;

// 画面に表示する盤面、一括管理
public class DisplayBan extends Ban {

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

