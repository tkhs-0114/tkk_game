package team3.tkk_game.model.Koma;

public class KomaDB {
  Integer id;
  String name;
  Integer updateKoma;

  public KomaDB(Integer id, String name, Integer updateKoma) {
    this.id = id;
    this.name = name;
    this.updateKoma = updateKoma;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
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

  public void setUpdateKoma(Integer updateKoma) {
    this.updateKoma = updateKoma;
  }
}
