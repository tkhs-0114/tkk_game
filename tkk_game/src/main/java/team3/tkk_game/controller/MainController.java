package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.WaitRoom;
import org.springframework.ui.Model;

@Controller
public class MainController {

  @Autowired
  WaitRoom waitRoom;
  @Autowired
  GameRoom gameRoom;

  @GetMapping("/home")
  public String home(Principal principal, Model model) {
    String playerName = principal.getName();
    waitRoom.clearRequest(playerName);
    waitRoom.rmRoom(playerName);
    gameRoom.rmGameByName(playerName);
    return "home.html";
  }

}
