package Ants;

import UI.GUI;
import Interfaces.Game;
import SGS.Giocatore;

import javax.swing.*;
import java.util.Map;

public class AntSimulator implements Game {

    private final GUI gui;

    public AntSimulator(GUI gui) {
        resetMap();
        this.gui = gui;
    }

    @Override
    public void startGame() {
    }

    @Override
    public void setRandom(boolean random) {
    }

    @Override
    public void setStartingRandomAgents(int number) {
    }

    @Override
    public void setMap(int y, int x) {
    }

    @Override
    public Timer getTimer() {
        return null;
    }

    @Override
    public void resetMap() {

    }
}