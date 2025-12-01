package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.PlayerStatus;
import team3.tkk_game.model.Game;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.MatchChecker;
import team3.tkk_game.services.TurnChecker;

import org.springframework.ui.Model;

@Controller
public class GameController {

  @Autowired
  GameRoom gameRoom;
  @Autowired
  WaitRoom waitRoom;
  @Autowired
  MatchChecker matchChecker;
  @Autowired
  TurnChecker turnChecker;

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

  private String returnGame(Model model, Game game, String playerName) {
    model.addAttribute("gameId", game.getId());
    model.addAttribute("ban", game.getBan());
    model.addAttribute("playerStatus", game.getPlayerByName(playerName).getStatus());
    // デバッグ用
    model.addAttribute("game", game);
    return "game.html";
  }

  @GetMapping("/game/start")
  public String gameStart(Principal principal, Model model, @RequestParam(required = false) String player2Name) {
    String loginPlayerName = principal.getName();
    Game game;

    if (player2Name != null && !player2Name.isEmpty()) {
      // 自分から対戦リクエストを送信する
      waitRoom.rmPlayer(loginPlayerName);
      waitRoom.rmPlayer(player2Name);
      game = gameRoom.addGame(loginPlayerName, player2Name);
    } else {
      // 誰かに対戦リクエストを送られた場合
      game = gameRoom.inGamePlayer2(loginPlayerName);
    }
    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping("/game")
  public String gamePage(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping("/game/move")
  public String gameMove(Principal principal, Model model, @RequestParam int fromX, @RequestParam int fromY,
      @RequestParam int toX, @RequestParam int toY) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    Boolean isMyTurn = game.getPlayerByName(loginPlayerName).getStatus() == PlayerStatus.GAME_THINKING;
    Boolean canMove = game.getBan().getKomaAt(fromX, fromY).canMove(fromX, fromY, toX, toY);
    System.out.println("canMove:" + canMove);
    if (!isMyTurn || !canMove) {
      return returnGame(model, game, loginPlayerName);
    }

    Boolean isSuccess = game.getBan().moveKoma(fromX, fromY, toX, toY);
    System.out.println("isSuccess:" + isSuccess);
    if (!isSuccess) {
      return returnGame(model, game, loginPlayerName);
    }
    game.switchTurn();
    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping("/game/turn")
  public SseEmitter game(Principal principal, @RequestParam String gameId) {
    SseEmitter emitter = new SseEmitter();
    turnChecker.checkTurn(emitter, gameRoom.getGameById(gameId), principal.getName());
    return emitter;
  }

  @GetMapping("/debug")
  public String debug(Model model) {
    model.addAttribute("games", gameRoom.getGames());
    model.addAttribute("waitPlayers", waitRoom.getWaitRoom());
    return "debug.html";
  }
}
