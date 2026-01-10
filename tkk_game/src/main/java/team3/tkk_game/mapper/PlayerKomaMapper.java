package team3.tkk_game.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import team3.tkk_game.model.PlayerKoma;

/**
 * プレイヤーが使用可能な駒を管理するMapper
 */
@Mapper
public interface PlayerKomaMapper {

  /**
   * プレイヤーに駒の使用権限を付与
   * 
   * @param playerKoma プレイヤーと駒の紐づけ情報
   * @return 挿入された行数
   */
  @Insert("INSERT INTO PlayerKoma(player_id, koma_id, is_owner) VALUES(#{playerId}, #{komaId}, #{isOwner})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertPlayerKoma(PlayerKoma playerKoma);

  /**
   * usernameからplayer_idを取得
   * 
   * @param username ユーザー名
   * @return プレイヤーID
   */
  @Select("SELECT id FROM Player WHERE username = #{username}")
  Integer selectPlayerIdByUsername(String username);

  /**
   * 特定の駒に紐づく全てのPlayerKomaを削除
   * 
   * @param komaId 駒ID
   * @return 削除された行数
   */
  @Delete("DELETE FROM PlayerKoma WHERE koma_id = #{komaId}")
  int deletePlayerKomasByKomaId(int komaId);

  /**
   * 指定されたプレイヤーが指定された駒の所有者かチェック
   * 
   * @param playerId プレイヤーID
   * @param komaId 駒ID
   * @return 所有者の場合true
   */
  @Select("SELECT is_owner FROM PlayerKoma WHERE player_id = #{playerId} AND koma_id = #{komaId}")
  Boolean isOwner(int playerId, int komaId);

  /**
   * 特定のプレイヤーと駒の紐づけを削除
   * 
   * @param playerId プレイヤーID
   * @param komaId 駒ID
   * @return 削除された行数
   */
  @Delete("DELETE FROM PlayerKoma WHERE player_id = #{playerId} AND koma_id = #{komaId}")
  int deletePlayerKoma(int playerId, int komaId);
}
