package team3.tkk_game.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import team3.tkk_game.model.PlayerDeck;

/**
 * プレイヤーが使用可能なデッキを管理するMapper
 */
@Mapper
public interface PlayerDeckMapper {

  /**
   * プレイヤーにデッキの使用権限を付与
   * 
   * @param playerDeck プレイヤーとデッキの紐づけ情報
   * @return 挿入された行数
   */
  @Insert("INSERT INTO PlayerDeck(player_id, deck_id, is_owner) VALUES(#{playerId}, #{deckId}, #{isOwner})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertPlayerDeck(PlayerDeck playerDeck);

  /**
   * usernameからplayer_idを取得
   * 
   * @param username ユーザー名
   * @return プレイヤーID
   */
  @Select("SELECT id FROM Player WHERE username = #{username}")
  Integer selectPlayerIdByUsername(String username);

  /**
   * 特定のデッキに紐づく全てのPlayerDeckを削除
   * 
   * @param deckId デッキID
   * @return 削除された行数
   */
  @Delete("DELETE FROM PlayerDeck WHERE deck_id = #{deckId}")
  int deletePlayerDecksByDeckId(int deckId);

  /**
   * 共通デッキ用：全てのプレイヤーにデッキの使用権限を一括付与
   * 
   * @param deckId デッキID
   * @return 挿入された行数
   */
  @Insert("INSERT INTO PlayerDeck(player_id, deck_id, is_owner) SELECT id, #{deckId}, FALSE FROM Player")
  int insertPlayerDeckForAllPlayers(int deckId);

  /**
   * 指定されたプレイヤーが指定されたデッキの所有者かチェック
   * 
   * @param playerId プレイヤーID
   * @param deckId デッキID
   * @return 所有者の場合true
   */
  @Select("SELECT is_owner FROM PlayerDeck WHERE player_id = #{playerId} AND deck_id = #{deckId}")
  Boolean isOwner(int playerId, int deckId);

  /**
   * 新規プレイヤーを登録
   * 
   * @param username ユーザー名
   */
  @Insert("INSERT INTO Player(username) VALUES(#{username})")
  void insertPlayer(String username);

  /**
   * プレイヤーの選択中デッキを更新
   * 
   * @param playerId プレイヤーID
   * @param deckId デッキID
   */
  @Update("UPDATE Player SET selected_deck_id = #{deckId} WHERE id = #{playerId}")
  void updateSelectedDeckId(@Param("playerId") int playerId, @Param("deckId") int deckId);
}
