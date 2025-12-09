package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.Game;
import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.MatchChecker;

import org.springframework.ui.Model;

@Controller
public class MainController {

  @Autowired
  WaitRoom waitRoom;
  @Autowired
  GameRoom gameRoom;
  @Autowired
  MatchChecker matchChecker;

  @GetMapping("/home")
  public String home(Principal principal, Model model) {
    String playerName = principal.getName();
    waitRoom.clearRequest(playerName);
    waitRoom.rmRoom(playerName);
    gameRoom.rmGameByName(playerName);
    return "home.html";
  }

  @GetMapping("/match")
  public String match(Principal principal, Model model) {
    waitRoom.rmRoom(principal.getName());
    model.addAttribute("playerName", principal.getName());

    return "match.html";
  }

  // SSE用
  @GetMapping("/waitRoom")
  public SseEmitter waitRoom() {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    matchChecker.checkMatch(emitter, waitRoom);
    return emitter;
  }

  // 部屋作成
  @GetMapping("/makeRoom")
  public String makeRoom(Principal principal, Model model) {
    String playerName = principal.getName();
    waitRoom.clearRequest(playerName);
    waitRoom.rmRoom(playerName);
    gameRoom.rmGameByName(playerName);
    waitRoom.addWaitRoom(playerName);
    model.addAttribute("playerName", playerName);
    return "waiting.html";
  }

  // 対戦リクエストを送信する
  @PostMapping("/sendRequest")
  public String sendRequest(Principal principal, Model model, @RequestParam String Player1Name) {
    String Player2Name = principal.getName();
    waitRoom.sendRequest(Player2Name, Player1Name);
    model.addAttribute("playerName", Player2Name);
    return "match.html";
  }

  // 対戦リクエストを承認する
  @PostMapping("/acceptMatch")
  public String acceptMatch(Principal principal) {
    String Player1Name = principal.getName();
    Game room = waitRoom.getRoomByName(Player1Name);
    
    if (room != null && room.getPlayer2() != null) {
      String Player2Name = room.getPlayer2().getName();
      gameRoom.addGame(room, Player2Name);
      waitRoom.rmRoom(Player1Name);
      return "redirect:/game/start";
    }
    // リクエストがない場合は待機画面に戻る
    return "waiting.html";
  }

  // 対戦リクエストを拒否する
  @PostMapping("/rejectMatch")
  public String rejectMatch(Principal principal, Model model) {
    String Player1Name = principal.getName();
    waitRoom.clearRequest(Player1Name);
    model.addAttribute("playerName", Player1Name);
    return "waiting.html";
  }

}
