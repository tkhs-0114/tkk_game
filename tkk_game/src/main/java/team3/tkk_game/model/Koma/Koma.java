package team3.tkk_game.model.Koma;

import java.util.ArrayList;
import java.util.List;

import team3.tkk_game.model.Player;

public class Koma {
  int id;
  String name;
  ArrayList<KomaRule> rules;
  Player owner;
  KomaSkill skill;
  int updateKoma; // 変化後の駒ID. -1なら変化しない

  public Koma(KomaDB komaDB, List<KomaRule> rules, Player owner) {
    this.id = komaDB.getId();
    this.name = komaDB.getName();
    this.rules = new ArrayList<>(rules);
    this.owner = owner;
    this.skill = komaDB.getSkill() != null ? KomaSkill.valueOf(komaDB.getSkill()) : KomaSkill.NULL;
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

  public Player getOwner() {
    return owner;
  }

  public void setOwner(Player owner) {
    this.owner = owner;
  }

  public KomaSkill getskill() {
    return skill;
  }

  public int getUpdateKoma() {
    return updateKoma;
  }

  public Boolean canMove(int fromX, int fromY, int toX, int toY) {
    return true;
  }

}
