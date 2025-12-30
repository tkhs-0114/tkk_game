package team3.tkk_game.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;

@Mapper
public interface KomaMapper {
  @Select("SELECT id,name,skill,update_koma FROM koma")
  List<KomaDB> selectAllKoma();

  @Select("SELECT id,name,skill,update_koma FROM koma WHERE id = #{komaId}")
  KomaDB selectKomaById(Integer komaId);

  @Select("SELECT rule FROM komarule WHERE koma_id = #{komaId}")
  List<KomaRule> selectKomaRuleById(Integer komaId);
}
