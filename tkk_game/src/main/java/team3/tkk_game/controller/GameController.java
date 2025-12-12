package team3.tkk_game.controller;

import java.security.Principal;
import java.util.List;

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

import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.model.Koma.Koma;
import team3.tkk_game.model.Koma.KomaDB;
import team3.tkk_game.model.Koma.KomaRule;

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
  @Autowired
  KomaMapper KomaMapper;

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
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    // ゲームが見つからない場合はマッチング画面に戻る
    if (game == null) {
      return "redirect:/match";
    }

    /*
     * ここにデッキ設定等のゲームの初期設定を記入
     */
    KomaDB koma1 = KomaMapper.selectKomaById(1); // 例: 駒ID1を選択
    List<KomaRule> koma1Rules = KomaMapper.selectKomaRuleById(1);
    Koma koma1Koma = new Koma(koma1, koma1Rules);
    game.getBan().setKomaAt(0, -2, koma1Koma);

    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping
  public String gamePage(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    // ゲームが見つからない場合はマッチング画面に戻る
    if (game == null) {
      return "redirect:/match";
    }
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
