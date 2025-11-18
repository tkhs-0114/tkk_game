package team3.tkk_game.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {
  @GetMapping("/home")
  public String home() {
    return "home.html";
  }

  @GetMapping("/match")
  public String match() {
    return "match.html";
  }
}
