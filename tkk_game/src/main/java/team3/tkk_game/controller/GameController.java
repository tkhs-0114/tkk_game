package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import team3.tkk_game.controller.model.WaitRoom;

@Controller
public class GameController {

  @Autowired
  WaitRoom waitRoom;

  @GetMapping("/home")
  public String home(Principal principal, Model model) {
    waitRoom.rmPlayer(principal.getName());

    return "home.html";
  }

  @GetMapping("/match")
  public String match(Principal principal, Model model) {
    waitRoom.addPlayer(principal.getName());

    model.addAttribute("players", waitRoom.getPlayers());

    return "match.html";
  }
}
