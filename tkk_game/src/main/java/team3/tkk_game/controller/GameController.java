package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.MatchChecker;

import org.springframework.ui.Model;

@Controller
public class GameController {

  @Autowired
  GameRoom gameRoom;
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
    model.addAttribute("playerName", principal.getName());

    return "match.html";
  }

  @GetMapping("/waitRoom")
  public SseEmitter waitRoom() {
    SseEmitter emitter = new SseEmitter();
    matchChecker.checkMatch(emitter, waitRoom);
    return emitter;
  }

  @GetMapping("/gameStart")
  public String game(Principal principal, Model model, @RequestParam(required = false) String player2Name) {
    String loginPlayerName = principal.getName();
    String gameId = null;

    if (player2Name != null && !player2Name.isEmpty()) {
      // 自分から対戦リクエストを送信する
      waitRoom.rmPlayer(loginPlayerName);
      waitRoom.rmPlayer(player2Name);
      gameId = gameRoom.addGame(loginPlayerName, player2Name);
    } else {
      // 誰かに対戦リクエストを送られた場合
      gameId = gameRoom.inGamePlayer2(loginPlayerName);
    }
    model.addAttribute("gameId", gameId);

    return "game.html";
  }
}
