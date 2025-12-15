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
    model.addAttribute("ban", game.getDisplayBan());
    model.addAttribute("playerStatus", game.getPlayerByName(playerName).getStatus());
    if (game.getPlayer2().getName().equals(playerName)) {
      model.addAttribute("isPlayer2",true);
    } else {
      model.addAttribute("isPlayer2", false);
    }
    // デバッグ用
    model.addAttribute("game", game);
    return "game.html";
  }

  private String returnGame(Model model, Game game, String playerName, String errMessage) {
    model.addAttribute("errMessage", errMessage);
    return returnGame(model, game, playerName);
  }

  @GetMapping("/start")
  public String gameStart(Principal principal, Model model) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);
    // ゲームが見つからない場合はマッチング画面に戻る
    if (game == null) {
      return "redirect:/match";
    }

    /*
     * ここにデッキ設定等のゲームの初期設定を記入
     */
    game.init_game();
    KomaDB koma1 = KomaMapper.selectKomaById(1); // 例: 駒ID1を選択
    List<KomaRule> koma1Rules = KomaMapper.selectKomaRuleById(1);
    Koma koma1Koma = new Koma(koma1, koma1Rules);
    
    //応急処置
    game.getPlayer1Ban().setKomaAt(0, 2, koma1Koma);
    game.getPlayer2Ban().setKomaAt(0, -2, koma1Koma);
    game.getDisplayBan().setKomaAt(0, 2, koma1Koma);
    game.getDisplayBan().setKomaAt(0, -2, koma1Koma);

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
  public String gameMove(Principal principal, Model model, @RequestParam int fromX, @RequestParam int fromY, @RequestParam int toX, @RequestParam int toY) {
    String loginPlayerName = principal.getName();
    Game game = gameRoom.getGameByPlayerName(loginPlayerName);

    // 自分のターンか確認
    Boolean isMyTurn = game.getPlayerByName(loginPlayerName).getStatus() == PlayerStatus.GAME_THINKING;
    if (!isMyTurn) {
      return returnGame(model, game, loginPlayerName, "自分のターンではありません");
    }
    
    // 自分の駒か確認（LocalBanには自分の駒しかない）
    Koma koma = game.getLocalBan(loginPlayerName).getKomaAt(fromX, fromY);
    if (koma == null) {
      return returnGame(model, game, loginPlayerName, "自分の駒ではありません");
    }
    
    // 移動可能か確認
    Boolean canMove = koma.canMove(fromX, fromY, toX, toY);
    System.out.println("canMove:" + canMove);
    if (!canMove) {
      return returnGame(model, game, loginPlayerName, "不正な手です");
    }

    // 駒を移動
    Boolean isSuccess = game.getDisplayBan().moveKoma(fromX, fromY, toX, toY);
    System.out.println("isSuccess:" + isSuccess);
    if (!isSuccess) {
      return returnGame(model, game, loginPlayerName, "移動に失敗しました");
    }
    game.getLocalBan(loginPlayerName).setKomaAt(toX, toY, game.getLocalBan(loginPlayerName).getKomaAt(fromX, fromY));
    game.getLocalBan(loginPlayerName).setKomaAt(fromX, fromY, null);
    game.switchTurn();
    return returnGame(model, game, loginPlayerName);
  }

  @GetMapping("/turn")
  public SseEmitter game(Principal principal, @RequestParam String gameId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    turnChecker.checkTurn(emitter, gameRoom.getGameById(gameId), principal.getName());
    return emitter;
  }

}
