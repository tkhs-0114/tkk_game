package team3.tkk_game.model.Koma;

import java.util.List;

public class KomaDB {
  Integer id;
  String name;
  String skill;
  Integer updateKoma;
  Boolean isOwner;

  public KomaDB() {
  }

  public KomaDB(String name, String skill, Integer updateKoma) {
    this.name = name;
    this.skill = skill;
    this.updateKoma = updateKoma;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSkill() {
    return skill;
  }

  public Integer getUpdateKoma() {
    return updateKoma;
  }

  public Boolean getIsOwner() {
    return isOwner;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSkill(String skill) {
    this.skill = skill;
  }

  public void setUpdateKoma(Integer updateKoma) {
    this.updateKoma = updateKoma;
  }

  public void setIsOwner(Boolean isOwner) {
    this.isOwner = isOwner;
  }

  /**
   * 駒のコストを計算
   *
   * @param rules 移動ルールのリスト
   * @return 駒の合計コスト（移動ルールのコスト + スキルのコスト）
   */
  public int calculateCost(List<KomaRule> rules) {
    int totalCost = 0;
    // 移動ルールのコスト合計
    if (rules != null) {
      for (KomaRule rule : rules) {
        totalCost += rule.getCost();
      }
    }
    // スキルのコスト
    if (skill != null) {
      totalCost += KomaSkill.valueOf(skill).getCost();
    }

    if (updateKoma != null) {
      // 成り先のコストを追加
      switch (updateKoma) {
        case 11: // 成金
          totalCost += 3;
          break;
        case 12: // 馬
          totalCost += 6;
          break;
        case 13: // 龍
          totalCost += 6;
          break;
        default:
          break;
      }
    }
    return totalCost;
  }
}
