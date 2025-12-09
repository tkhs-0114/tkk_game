package team3.tkk_game.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import team3.tkk_game.model.Deck;

@Mapper
public interface DeckMapper {
  @Insert("INSERT INTO Deck(name, sfen) VALUES(#{name}, #{sfen})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertDeck(Deck deck);
}
