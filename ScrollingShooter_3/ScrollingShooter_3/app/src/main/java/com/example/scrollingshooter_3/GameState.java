package com.example.scrollingshooter_3;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum State {
        MENU,
        GAME_RUNNING,
        GAME_PAUSED,
        GAME_OVER,
        LEVEL_COMPLETE
    }

    private State currentState;
    private int score;
    private int level;

    public GameState() {
        currentState = State.MENU;
        score = 0;
        level = 1;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}