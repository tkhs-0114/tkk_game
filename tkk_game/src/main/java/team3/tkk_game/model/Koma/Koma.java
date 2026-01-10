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
  int originalKoma;

  public Koma(KomaDB komaDB, List<KomaRule> rules, Player owner) {
    this.id = komaDB.getId();
    this.name = komaDB.getName();
    this.rules = new ArrayList<>(rules);
    this.owner = owner;
    this.skill = komaDB.getSkill() != null ? KomaSkill.valueOf(komaDB.getSkill()) : KomaSkill.NULL;
    this.updateKoma = komaDB.getUpdateKoma();
    this.originalKoma = -1;
  }

  public Koma(KomaDB komaDB, List<KomaRule> rules, Player owner, int originalKoma) {
    this.id = komaDB.getId();
    this.name = komaDB.getName();
    this.rules = new ArrayList<>(rules);
    this.owner = owner;
    this.skill = komaDB.getSkill() != null ? KomaSkill.valueOf(komaDB.getSkill()) : KomaSkill.NULL;
    this.updateKoma = komaDB.getUpdateKoma();
    this.originalKoma = originalKoma;
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

  public KomaSkill getSkill() {
    return skill;
  }

  public int getUpdateKoma() {
    return updateKoma;
  }

  public int getOriginalKoma() {
    return originalKoma;
  }

  /**
   * 駒のコストを計算
   * 移動ルールのコスト合計 + スキルのコスト
   *
   * @return 駒の合計コスト
   */
  public int getCost() {
    int totalCost = 0;
    // 移動ルールのコスト合計
    for (KomaRule rule : rules) {
      totalCost += rule.getCost();
    }
    // スキルのコスト
    totalCost += skill.getCost();
    return totalCost;
  }

  public Boolean canMove(int fromX, int fromY, int toX, int toY) {
    return true;
  }

}
