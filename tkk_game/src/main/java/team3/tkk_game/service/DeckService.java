package team3.tkk_game.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import team3.tkk_game.model.Placement;

@Service
public class DeckService {

  public String generateSfen(List<Placement> placements) {
    char[][] board = new char[5][5];
    for (int r = 0; r < 5; r++) {
      for (int f = 0; f < 5; f++) {
        board[r][f] = '.';
      }
    }
    for (Placement p : placements) {
      int r = 5 - p.getRank();
      int f = p.getFile() - 1;
      char piece = p.getType().charAt(0);
      if ("W".equals(p.getOwner())) piece = Character.toLowerCase(piece);
      board[r][f] = piece;
    }
    StringBuilder sfenBoard = new StringBuilder();
    for (int r = 0; r < 5; r++) {
      int empty = 0;
      for (int f = 0; f < 5; f++) {
        char c = board[r][f];
        if (c == '.') { empty++; }
        else {
          if (empty > 0) { sfenBoard.append(empty); empty = 0; }
          sfenBoard.append(c);
        }
      }
      if (empty > 0) sfenBoard.append(empty);
      if (r < 4) sfenBoard.append('/');
    }
    return sfenBoard.toString();
  }

  public List<Placement> parseSfen(String sfen) {
    List<Placement> res = new ArrayList<>();
    String[] rows = sfen.split("/");
    for (int r = 0; r < rows.length && r < 5; r++) {
      String row = rows[r];
      int file = 1;
      for (int i = 0; i < row.length(); i++) {
        char c = row.charAt(i);
        if (Character.isDigit(c)) {
          int cnt = c - '0';
          file += cnt;
        } else {
          Placement p = new Placement();
          p.setFile(file);
          int rank = 5 - r;
          p.setRank(rank);
          p.setType(String.valueOf(Character.toUpperCase(c)));
          p.setOwner(Character.isLowerCase(c) ? "W" : "B");
          res.add(p);
          file++;
        }
      }
    }
    return res;
  }
}
