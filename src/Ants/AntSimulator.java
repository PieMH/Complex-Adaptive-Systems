package Ants;

import UI.GUI;
import Interfaces.Game;
import SGS.Giocatore;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Random;

/**
 *
 */
public class AntSimulator implements Game {

    private static Map<Integer, Ant> currentAlive;  // dizionario di giocatori vivi momentaneo
    private final GUI gui;
    private Timer t;
    private boolean reset;                         // booleana per segnalare se resettare la map o no
    private Random random_seed;                    // seme di generazione di numeri random
    private final Object lock = new Object();      // lock per i thread gioco/UI.GUI nella modifica contemporanea allo scorrimento sul dizionario
    private static int delay = 100;                // ritardo di repaint del current frame
    static boolean random = false;                 // default value or given by Options Menu: variabile di differenziazione dello spawn iniziale dei giocatori nell'arena
    static int n_starting_players = 2000;          // default value or given by Options Menu: set number of random agents spawning in the grid

    public AntSimulator(GUI gui) {
        resetMap();
        this.gui = gui;
    }

    @Override
    public void startGame() {
        ActionListener taskPerformer = e -> {
            if (gui.play) {
                if (reset) {
                    if (random) {
                        setMapRandom();    // setMapRandom
                    }
                    else {
//                        scorriMatrice(0);  // setMap
                    }
                    reset = false;
                }
//                scorriDizionario(0);  // evolve
//                printStats(false, false, true);
//                resetStats();
//                scorriMatrice(1);     // updateFrame
                gui.currentFrame = gui.nextFrame;
                gui.nextFrame = new boolean[GUI.HEIGHT][GUI.WIDTH];
                gui.getPanel().repaint();
            }
        };
        t = new Timer(delay, taskPerformer);
        t.start();
    }

    @Override
    public void setRandom(boolean random) {
    }

    @Override
    public void setStartingRandomAgents(int number) {
    }

    @Override
    public void setMapRandom() {
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