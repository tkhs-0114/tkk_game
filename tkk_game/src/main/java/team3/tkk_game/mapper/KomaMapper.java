package team3.tkk_game.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;

@Mapper
public interface KomaMapper {
  @Select("SELECT id, name, skill, update_koma AS updateKoma FROM koma")
  List<KomaDB> selectAllKoma();

  @Select("SELECT id, name, skill, update_koma AS updateKoma FROM koma WHERE id = #{komaId}")
  KomaDB selectKomaById(Integer komaId);

  @Select("SELECT rule FROM komarule WHERE koma_id = #{komaId}")
  List<KomaRule> selectKomaRuleById(Integer komaId);

  @Insert("INSERT INTO koma(name, skill, update_koma) VALUES(#{name}, #{skill}, #{updateKoma})")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  int insertKoma(KomaDB koma);

  @Insert("INSERT INTO komarule(koma_id, rule) VALUES(#{komaId}, #{ruleName})")
  void insertKomaRule(int komaId, KomaRule ruleName);
}
