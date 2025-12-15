package team3.tkk_game.model.Koma;

import java.util.ArrayList;
import java.util.List;

public class Koma {
  int id;
  String name;
  ArrayList<KomaRule> rules;
  int updateKoma; // 変化後の駒ID. -1なら変化しない

  public Koma(KomaDB komaDB, List<KomaRule> rules) {
    this.id = komaDB.getId();
    this.name = komaDB.getName();
    this.rules = new ArrayList<>(rules);
    this.updateKoma = komaDB.getUpdateKoma();
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ArrayList<KomaRule> getRules() {
    return rules;
  }

  public int getUpdateKoma() {
    return updateKoma;
  }

  public Boolean canMove(int fromX, int fromY, int toX, int toY) {
    return true;
  }

}
