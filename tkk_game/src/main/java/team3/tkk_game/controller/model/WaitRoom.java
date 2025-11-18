package team3.tkk_game.controller.model;

import java.util.ArrayList;

public class WaitRoom {
    ArrayList<String> players;

    public WaitRoom() {
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }

    public void addPlayer(String player) {
        if (players == null) {
            players = new ArrayList<>();
        }
        players.add(player);
    }

}
