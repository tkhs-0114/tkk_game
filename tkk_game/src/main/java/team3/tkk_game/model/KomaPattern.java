package team3.tkk_game.model;

import java.util.Objects;

public class KomaPattern {
  public int dx;
  public int dy;

  public KomaPattern(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
  }

  public void print() {
    System.out.println("dx:" + dx + " dy:" + dy);
  }

  // Setで動作させるためにequalsとhashCodeをオーバーライド
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    KomaPattern other = (KomaPattern) obj;
    return dx == other.dx && dy == other.dy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dx, dy);
  }
}
