package team3.tkk_game.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import team3.tkk_game.model.Deck;

@Mapper
public interface DeckMapper {
  @Insert("INSERT INTO Deck(name, sfen) VALUES(#{name}, #{sfen})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertDeck(Deck deck);

  @Select("SELECT id, name, sfen FROM Deck")
  java.util.List<Deck> selectAllDecks();

  @Select("SELECT id, name, sfen FROM Deck WHERE id = #{id}")
  Deck selectDeckById(int id);

  @Delete("DELETE FROM Deck WHERE id = #{id}")
  int deleteDeckById(int id);
}
