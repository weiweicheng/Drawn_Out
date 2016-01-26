package com.example.Drawn_Out.entities;

import java.util.Date;
import java.util.List;

public class Game {
    String id;
    Date creationDate;
    List<Player> players;
    String currentWord;
    String currentArtist;
    byte[] currentPicture;
    Phase gamePhase;

    public Game(String id, Date creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    public String getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }

    public void setCurrentArtist(String currentArtist) {
        this.currentArtist = currentArtist;
    }

    public byte[] getCurrentPicture() {
        return currentPicture;
    }

    public void setCurrentPicture(byte[] currentPicture) {
        this.currentPicture = currentPicture;
    }

    public Phase getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(Phase gamePhase) {
        this.gamePhase = gamePhase;
    }
}
