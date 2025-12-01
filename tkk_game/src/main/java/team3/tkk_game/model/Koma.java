package team3.tkk_game.model;

import java.util.HashSet;
import java.util.Set;

public class Koma {
  String name;
  Set<KomaPattern> movePatterns = new HashSet<KomaPattern>();

  public Koma(String name, KomaPattern[] movePatterns) {
    this.name = name;
    for (KomaPattern pattern : movePatterns) {
      this.movePatterns.add(pattern);
    }
  }

  public String getName() {
    return name;
  }

  public Boolean canMove(int fromX, int fromY, int toX, int toY) {
    int diffX = toX - fromX;
    int diffY = toY - fromY;
    KomaPattern pattern = new KomaPattern(diffX, diffY);
    pattern.print();
    return movePatterns.contains(pattern);
  }
}
