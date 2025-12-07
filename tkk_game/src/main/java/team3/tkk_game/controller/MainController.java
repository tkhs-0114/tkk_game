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
public class MainController {

  @Autowired
  WaitRoom waitRoom;
  @Autowired
  MatchChecker matchChecker;

  @GetMapping("/home")
  public String home(Principal principal, Model model) {
    waitRoom.rmRoom(principal.getName());
    return "home.html";
  }

  @GetMapping("/match")
  public String match(Principal principal, Model model) {
    waitRoom.rmRoom(principal.getName());
    model.addAttribute("playerName", principal.getName());

    return "match.html";
  }

  @GetMapping("/waitRoom")
  public SseEmitter waitRoom() {
    SseEmitter emitter = new SseEmitter();
    matchChecker.checkMatch(emitter, waitRoom);
    return emitter;
  }

  @GetMapping("/makeRoom")
  public String makeRoom(Principal principal, Model model) {
    waitRoom.addWaitRoom(principal.getName());
    model.addAttribute("playerName", principal.getName());
    return "waiting.html";
  }

}
