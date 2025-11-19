package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.MatchChecker;

import org.springframework.ui.Model;

@Controller
public class GameController {

  @Autowired
  WaitRoom waitRoom;
  @Autowired
  MatchChecker matchChecker;

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

  @GetMapping("/waitRoom")
  public SseEmitter waitRoom() {
    SseEmitter emitter = new SseEmitter();
    matchChecker.checkMatch(emitter);
    return emitter;
  }

  @GetMapping("/game")
  public String game(Principal principal, Model model) {
    return "game.html";
  }
}
