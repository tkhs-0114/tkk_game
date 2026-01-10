package team3.tkk_game.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

  /**
   * プレイヤーが使用可能な駒を取得（PlayerKomaテーブル経由）
   * 
   * @param username ユーザー名
   * @return 駒のリスト
   */
  @Select("SELECT k.id, k.name, k.skill, k.update_koma AS updateKoma, pk.is_owner AS isOwner " +
      "FROM koma k " +
      "INNER JOIN PlayerKoma pk ON k.id = pk.koma_id " +
      "INNER JOIN Player p ON pk.player_id = p.id " +
      "WHERE p.username = #{username} " +
      "ORDER BY k.id ASC")
  List<KomaDB> selectKomasByPlayerUsername(String username);

  /**
   * プレイヤーが所有する駒を取得（所有者のみ）
   * 
   * @param username ユーザー名
   * @return 駒のリスト
   */
  @Select("SELECT k.id, k.name, k.skill, k.update_koma AS updateKoma, pk.is_owner AS isOwner " +
      "FROM koma k " +
      "INNER JOIN PlayerKoma pk ON k.id = pk.koma_id " +
      "INNER JOIN Player p ON pk.player_id = p.id " +
      "WHERE p.username = #{username} AND pk.is_owner = TRUE " +
      "ORDER BY k.id ASC")
  List<KomaDB> selectOwnedKomasByPlayerUsername(String username);

  /**
   * 駒を削除
   * 
   * @param komaId 駒ID
   * @return 削除された行数
   */
  @Delete("DELETE FROM koma WHERE id = #{komaId}")
  int deleteKomaById(int komaId);

  /**
   * 駒に紐づくルールを全て削除
   * 
   * @param komaId 駒ID
   * @return 削除された行数
   */
  @Delete("DELETE FROM komarule WHERE koma_id = #{komaId}")
  int deleteKomaRulesByKomaId(int komaId);

  /**
   * 駒を更新
   * 
   * @param koma 更新する駒情報
   * @return 更新された行数
   */
  @Update("UPDATE koma SET name = #{name}, skill = #{skill}, update_koma = #{updateKoma} WHERE id = #{id}")
  int updateKoma(KomaDB koma);
}
