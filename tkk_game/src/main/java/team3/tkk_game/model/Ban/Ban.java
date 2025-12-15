package team3.tkk_game.model.Ban;

import team3.tkk_game.model.Koma.Koma;

public class Ban {
  static final int BAN_LENGTH = 5;

  // board[x][y]で盤面のマスを表す.左下が(0,0)
  Koma[][] board = new Koma[BAN_LENGTH][BAN_LENGTH];

  public Ban() {
    for (int x = 0; x < BAN_LENGTH; x++) {
      for (int y = 0; y < BAN_LENGTH; y++) {
        this.board[x][y] = null;
      }
    }
  }

  public Koma[][] getBoard() {
    return board;
  }

  // board配列のインデックスに変換する
  protected int b2a(int boardIndex) {
    return boardIndex + (BAN_LENGTH - 1) / 2;
  }

  public Koma getKomaAt(int x, int y) {
    return board[b2a(x)][b2a(y)];
  }

  public boolean setKomaAt(int x, int y, Koma koma) {
    if (board[b2a(x)][b2a(y)] != null && koma != null) {
      return false;
    }
    board[b2a(x)][b2a(y)] = koma;
    return true;
  }
}
