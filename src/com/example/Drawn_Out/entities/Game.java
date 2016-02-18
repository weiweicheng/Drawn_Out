package com.example.Drawn_Out.entities;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private String id;
    private List<String> players;
    private List<Integer> scores;
    private String currentWord;
    private String currentArtist;
    private byte[] currentPicture;
    private Phase gamePhase;
    private int playersWaiting;

    public Game(String id, String username) {
        this.id = id;
        this.currentArtist = username;
        this.gamePhase = Phase.PLAYERJOINING;
        this.playersWaiting = 1;
        this.scores = new ArrayList<>();
        this.scores.add(0);
        this.players = new ArrayList<>();
        this.players.add(username);
    }

    public String getId() {
        return id;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
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

    public int getPlayersWaiting() {
        return playersWaiting;
    }

    public void setPlayersWaiting(int playersWaiting) {
        this.playersWaiting = playersWaiting;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }
}
