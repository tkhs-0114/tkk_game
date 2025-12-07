package team3.tkk_game.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.PlayerStatus;
import team3.tkk_game.model.Game;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.TurnChecker;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/game")
public class GameController {

  @Autowired
  GameRoom gameRoom;
  @Autowired
  WaitRoom waitRoom;
  @Autowired
  TurnChecker turnChecker;

  private String returnGame(Model model, Game game, String playerName) {
    model.addAttribute("gameId", game.getId());
    model.addAttribute("ban", game.getBan());
    model.addAttribute("playerStatus", game.getPlayerByName(playerName).getStatus());
    // デバッグ用
    model.addAttribute("game", game);
    return "game.html";
  }

  private String returnGame(Model model, Game game, String playerName, String errMessage) {
    model.addAttribute("errMessage", errMessage);
    return returnGame(model, game, playerName);
  }

  @GetMapping("/start")
  public String gameStart(Principal principal, Model model, @RequestParam(required = false) String player2Name) {
    String loginPlayerName = principal.getName();
    Game game;

    if (player2Name != null && !player2Name.isEmpty()) {
      // 自分から対戦リクエストを送信する
      game = gameRoom.addGame(waitRoom.getRoomByName(player2Name), loginPlayerName);
      waitRoom.rmRoom(player2Name);
    } else {
      // 誰かに対戦リクエストを送られた場合
      game = gameRoom.getGameByPlayerName(loginPlayerName);
    }
    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping
  public String gamePage(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping("/move")
  public String gameMove(Principal principal, Model model, @RequestParam int fromX, @RequestParam int fromY,
      @RequestParam int toX, @RequestParam int toY) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    Boolean isMyTurn = game.getPlayerByName(loginPlayerName).getStatus() == PlayerStatus.GAME_THINKING;
    Boolean canMove = game.getBan().getKomaAt(fromX, fromY).canMove(fromX, fromY, toX, toY);
    System.out.println("canMove:" + canMove);
    if (!isMyTurn || !canMove) {
      return returnGame(model, game, loginPlayerName, "不正な手です");
    }

    Boolean isSuccess = game.getBan().moveKoma(fromX, fromY, toX, toY);
    System.out.println("isSuccess:" + isSuccess);
    if (!isSuccess) {
      return returnGame(model, game, loginPlayerName, "移動に失敗しました");
    }
    game.switchTurn();
    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping("/turn")
  public SseEmitter game(Principal principal, @RequestParam String gameId) {
    SseEmitter emitter = new SseEmitter();
    turnChecker.checkTurn(emitter, gameRoom.getGameById(gameId), principal.getName());
    return emitter;
  }

}
