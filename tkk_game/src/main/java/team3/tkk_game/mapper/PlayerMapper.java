package team3.tkk_game.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PlayerMapper {

  @Select("SELECT selected_deck_id FROM Player WHERE username = #{username}")
  Integer getSelectedDeckIdByName(@Param("username") String username);

  @Update("UPDATE Player SET selected_deck_id = #{deckId} WHERE username = #{username}")
  void updateSelectedDeckId(@Param("username") String username, @Param("deckId") Integer deckId);

  @Update("UPDATE Player SET selected_deck_id = NULL WHERE selected_deck_id = #{deckId}")
  void clearSelectedDeckId(@Param("deckId") Integer deckId);
}
