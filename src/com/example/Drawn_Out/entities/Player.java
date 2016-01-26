package com.example.Drawn_Out.entities;

public class Player {
    String name;
    Integer score;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
