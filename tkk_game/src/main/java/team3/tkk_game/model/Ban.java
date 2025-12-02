package team3.tkk_game.model;

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
    this.board[b2a(0)][b2a(-2)] = new Koma("王", new KomaPattern[] {
        new KomaPattern(1, 0),
        new KomaPattern(0, 1),
        new KomaPattern(-1, 0),
        new KomaPattern(0, -1),
        new KomaPattern(1, 1),
        new KomaPattern(1, -1),
        new KomaPattern(-1, 1),
        new KomaPattern(-1, -1)
    });
    this.board[b2a(2)][b2a(2)] = new Koma("飛", new KomaPattern[] {
        new KomaPattern(1, 0),
        new KomaPattern(2, 0),
        new KomaPattern(3, 0),
        new KomaPattern(4, 0),
        new KomaPattern(0, 1),
        new KomaPattern(0, 2),
        new KomaPattern(0, 3),
        new KomaPattern(0, 4),
        new KomaPattern(-1, 0),
        new KomaPattern(-2, 0),
        new KomaPattern(-3, 0),
        new KomaPattern(-4, 0),
        new KomaPattern(0, -1),
        new KomaPattern(0, -2),
        new KomaPattern(0, -3),
        new KomaPattern(0, -4),
    });
  }

  public Koma[][] getBoard() {
    return board;
  }

  // board配列のインデックスに変換する
  private int b2a(int boardIndex) {
    return boardIndex + (BAN_LENGTH - 1) / 2;
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

  public Koma getKomaAt(int x, int y) {
    return board[b2a(x)][b2a(y)];
  }
}
