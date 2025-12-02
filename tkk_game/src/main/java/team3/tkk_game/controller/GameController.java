package team3.tkk_game.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import team3.tkk_game.model.GameRoom;
import team3.tkk_game.model.WaitRoom;
import team3.tkk_game.services.MatchChecker;
import team3.tkk_game.model.Koma;
import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.model.Deck;
import team3.tkk_game.model.Placement;
import team3.tkk_game.mapper.DeckMapper;
import team3.tkk_game.service.DeckService;

import org.springframework.ui.Model;

@Controller
public class GameController {

  @Autowired
  GameRoom gameRoom;
  @Autowired
  WaitRoom waitRoom;
  @Autowired
  MatchChecker matchChecker;

  // KomaMapper を統合
  @Autowired
  private KomaMapper komaMapper;

  @Autowired
  private DeckMapper deckMapper;

  @Autowired
  private DeckService deckService;

  @GetMapping("/home")
  public String home(Principal principal, Model model) {
    waitRoom.addPlayer(principal.getName());
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

  // 新規: デッキ作成画面のマッピング（表示・配置のみ、保存は無効）
  @GetMapping("/deckmake")
  public String deckmake(Principal principal, Model model) {
    if (principal != null) {
      model.addAttribute("playerName", principal.getName());
    }
    return "deckmake.html";
  }

  @GetMapping("/deckchoose")
  public String deckchoose(Principal principal, Model model) {
    if (principal != null)
      model.addAttribute("playerName", principal.getName());
    return "deckchoose.html";
  }

  // 統合: /api/koma を提供（KomaController を統合）
  @GetMapping(value = "/api/koma", produces = "application/json; charset=UTF-8")
  @ResponseBody
  public List<Koma> apiKoma() {
    return komaMapper.selectAllKoma();
  }

  // Deck 用 API を DeckController から統合
  public static class SaveRequest {
    public String name;
    public List<Placement> placements;
  }

  @PostMapping(value = "/api/decks", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<team3.tkk_game.model.Deck> saveDeck(@RequestBody SaveRequest req) {
    // SFEN 生成
    String sfen = deckService.generateSfen(req.placements);

    // Deck を作成して保存（DeckMapper が自動採番で id をセットする想定）
    team3.tkk_game.model.Deck deck = new team3.tkk_game.model.Deck();
    deck.setName(req.name);
    deck.setSfen(sfen);

    deckMapper.insertDeck(deck); // insert 後に deck.getId() がセットされることを想定

    // 保存結果を取得して返す（確実に id/sfen を含める）
    team3.tkk_game.model.Deck saved = deckMapper.selectDeckById(deck.getId());
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/api/decks/{id}")
  @ResponseBody
  public ResponseEntity<Deck> getDeck(@PathVariable int id) {
    Deck deck = deckMapper.selectDeckById(id);
    if (deck == null)
      return ResponseEntity.notFound().build();
    return ResponseEntity.ok(deck);
  }

  @GetMapping("/api/decks")
  @ResponseBody
  public List<Deck> listDecks() {
    return deckMapper.selectAllDecks();
  }
}
