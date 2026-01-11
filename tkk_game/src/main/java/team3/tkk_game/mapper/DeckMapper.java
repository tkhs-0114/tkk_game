package team3.tkk_game.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import team3.tkk_game.model.Deck;

@Mapper
public interface DeckMapper {
  @Insert("INSERT INTO Deck(name, sfen, cost) VALUES(#{name}, #{sfen}, #{cost})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertDeck(Deck deck);

  @Select("SELECT id, name, sfen, cost FROM Deck")
  java.util.List<Deck> selectAllDecks();

  @Select("SELECT id, name, sfen, cost FROM Deck WHERE id = #{id}")
  Deck selectDeckById(int id);

  @Update("UPDATE Deck SET name = #{name}, sfen = #{sfen}, cost = #{cost} WHERE id = #{id}")
  int updateDeck(Deck deck);

  @Delete("DELETE FROM Deck WHERE id = #{id}")
  int deleteDeckById(int id);

  /**
   * プレイヤーが使用可能なデッキを取得（PlayerDeckテーブル経由）
   * 
   * @param username ユーザー名
   * @return デッキのリスト
   */
  @Select("SELECT d.id, d.name, d.sfen, d.cost, pd.is_owner AS isOwner " +
      "FROM Deck d " +
      "INNER JOIN PlayerDeck pd ON d.id = pd.deck_id " +
      "INNER JOIN Player p ON pd.player_id = p.id " +
      "WHERE p.username = #{username} " +
      "ORDER BY d.id ASC")
  java.util.List<Deck> selectDecksByPlayerUsername(String username);
}
