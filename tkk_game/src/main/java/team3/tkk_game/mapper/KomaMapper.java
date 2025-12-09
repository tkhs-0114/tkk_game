package team3.tkk_game.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import team3.tkk_game.model.Koma;

@Mapper
public interface KomaMapper {
  @Select("SELECT id FROM koma")
  List<Integer> selectIDKoma();

  @Select("SELECT name FROM koma")
  List<String> selectNameKoma();

  @Select("SELECT id,name FROM koma")
  List<Koma> selectAllKoma();
}
