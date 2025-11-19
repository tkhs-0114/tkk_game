package team3.tkk_game.model;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import team3.tkk_game.model.Game;

@Component
public class GameRoom {
  ArrayList<Game> games = new ArrayList<>();
}
