package team3.tkk_game.model.Koma;

public class KomaDB {
  Integer id;
  String name;
  String skill;
  Integer updateKoma;

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
}
