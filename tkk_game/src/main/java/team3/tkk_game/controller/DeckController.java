package team3.tkk_game.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import team3.tkk_game.mapper.DeckMapper;
import team3.tkk_game.mapper.KomaMapper;
import team3.tkk_game.model.Deck;
import team3.tkk_game.model.Koma.KomaDB;

@Controller
@RequestMapping("/deck")
public class DeckController {

  @Autowired
  KomaMapper komaMapper;

  @Autowired
  DeckMapper deckMapper;

  @GetMapping("/make")
  public String deckmake(Principal principal, Model model) {
    model.addAttribute("playerName", principal.getName());
    List<KomaDB> komas = komaMapper.selectAllKoma();
    model.addAttribute("komas", komas);
    return "deckmake.html";
  }

  @PostMapping("/save")
  public String saveDeck(@RequestParam String deckName, @RequestParam String sfen, Principal principal) {
    Deck deck = new Deck();
    deck.setName(deckName);
    deck.setSfen(sfen);
    deckMapper.insertDeck(deck);
    return "redirect:/deck/make";
  }

  @GetMapping("/select")
  public String selectDeck(Principal principal, Model model) {
    model.addAttribute("playerName", principal.getName());
    List<Deck> decks = deckMapper.selectAllDecks();
    model.addAttribute("decks", decks);
    return "deckselect.html";
  }

  @PostMapping("/choose")
  public String chooseDeck(@RequestParam int deckId, Principal principal, HttpSession session) {
    session.setAttribute("selectedDeckId", deckId);
    return "redirect:/home";
  }

  @GetMapping("/load/{id}")
  public String loadDeck(@PathVariable("id") int deckId, HttpSession session) {
    session.setAttribute("selectedDeckId", deckId);
    return "redirect:/home";
  }

  @GetMapping("/delete/{id}")
  public String deleteDeck(@PathVariable("id") int deckId) {
    deckMapper.deleteDeckById(deckId);
    return "redirect:/deck/select";
  }
}
