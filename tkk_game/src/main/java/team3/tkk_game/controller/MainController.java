package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import team3.tkk_game.model.Deck;
import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.WaitRoom;
import org.springframework.ui.Model;

import team3.tkk_game.mapper.DeckMapper;
import team3.tkk_game.mapper.PlayerMapper;

@Controller
public class MainController {

  @Autowired
  WaitRoom waitRoom;
  @Autowired
  GameRoom gameRoom;
  @Autowired
  DeckMapper deckMapper;
  @Autowired
  PlayerMapper playerMapper;

  @GetMapping("/home")
  public String home(Principal principal, Model model) {
    String playerName = principal.getName();
    Integer deckId = playerMapper.getSelectedDeckIdByName(playerName);
    if (deckId != null) {
      Deck deck = deckMapper.selectDeckById(deckId);
      model.addAttribute("deckname", deck.getName());
      model.addAttribute("sfen", deck.getSfen());
      model.addAttribute("selectedDeckId", deckId);
    }
    waitRoom.clearRequest(playerName);
    waitRoom.rmRoom(playerName);
    gameRoom.rmGameByName(playerName);
    return "home.html";
  }

}
