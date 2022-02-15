package Ants;

import UI.GUI;
import Interfaces.Game;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * The main class that delineate the algorithm of the Ant sim model.
 * It manages the general behavior of the sim and its core functionalities.
 * It also serves as a mainframe for the classes Ants, Ambient, Feromon ??.
 */
public class AntSimulator implements Game {

    private static Map<Integer, Ant> currentAlive;  // dizionario di giocatori vivi momentaneo
    private final GUI gui;
    private Timer t;
    private boolean reset;                         // booleana per segnalare se resettare la map o no
    private Random random_seed;                    // seme di generazione di numeri random
    private final Object lock = new Object();      // lock per i thread gioco/UI.GUI nella modifica contemporanea allo scorrimento sul dizionario
    private static final int delay = 100;                // ritardo di repaint del current frame
    static boolean random = false;                 // default value or given by Options Menu: variabile di differenziazione dello spawn iniziale dei giocatori nell'arena
    static int n_starting_agents = 2000;          // default value or given by Options Menu: set number of random agents spawning in the grid
    private int born = 0;                              // n° giocatori nati nel ciclo corrente
    private int dead = 0;                              // n° giocatori morti nel ciclo corrente
    private int statsDelayCounter = 0;

    public AntSimulator(GUI gui) {
        resetMap();
        resetStats();
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
                        iterateMatrix(0);  // setMap
                    }
                    resetStats();
                    reset = false;
                }
                iterateHashMap(0);  // evolve
//                if (statsDelayCounter == 1) {
//                    printStats(0);
//                    statsDelayCounter = 0;
//                }
//                statsDelayCounter += 1;
                printStats(1);  // print Hash Map
                iterateMatrix(1);     // updateFrame
                gui.currentFrame = gui.nextFrame;
                gui.nextFrame = new boolean[GUI.HEIGHT][GUI.WIDTH];
                gui.getPanel().repaint();
            }
        };
        t = new Timer(delay, taskPerformer);
        t.start();
    }

    /**
     * main algorithm of the model.
     * @param hashKey the kye for the hashmap currentAlive
     */
    private void evolve(Integer hashKey) {
        Ant currentAnt = currentAlive.get(hashKey);
        currentAnt.age();
        if (currentAnt.getLife() < 1) {
            death(hashKey);
            return;
        }

        currentAnt.action();
        Integer newKey = currentAnt.getPos();
        boolean moved = !(Objects.equals(newKey, hashKey));
        if (moved) {   // the ant may have moved !
            currentAlive.put(newKey, currentAnt);   // should be granted by Ant.move() that this doesn't throw error and don't replace any already existing keys
            currentAlive.remove(hashKey, currentAnt);
        }

        if (currentAnt.getLife() < 1) {
            if (moved) death(newKey);
            else death(hashKey);
        }
    }

    /**
     * Metodo di scorrimento delle chiavi del dizionario di appoggio che si riferisce a quello presente
     * il metodo è thread-safe in quanto vige mutua esclusione (mutex) tra il thread della gui e quello del gioco SGS
     * evitando che si modifichino le chiavi del dizionario mentre sto operando su di esso.
     * <p>
     * La "evolve" chiamata lavora su una fotografia della currentAlive e le modifiche lato user cliccando sul pannello della gui
     * non creano inconsistenze ma avvengono solo alla fine dell'evoluzione del ciclo di gioco.
     * <p>
     * Per un aspetto più realistico del gioco le chiavi del dizionario vengono accedute casualmente una per una, evitando
     * che i giocatori alla fine del dizionario sfruttino il vantaggio di subire più raramente gli effetti ambientali (carenza di cibo)
     * @param scelta scelta di funzione di scorrimento del dizionario
     */
    private void iterateHashMap(int scelta) {
        Map<Integer, Ant> nextAlive;                      // dizionario di appoggio
        random_seed = new Random();
        synchronized (lock) {                                   // mutex tra il thread della gui e quello del gioco
            nextAlive = new Hashtable<>(currentAlive);
            List<Integer> keylist = new ArrayList<>(nextAlive.keySet());
            int n = nextAlive.size();
            int m = keylist.size();
            for (int i = 0; i < n; i++) {
                int index = 0;
                if (m-1 > 0) {
                    index = random_seed.nextInt(m - 1);
                }
                Integer key = keylist.remove(index);
                m -= 1;
                if (scelta == 0) {
                    evolve(key);
                }
                else if (scelta == 1) {
                    printHashMap(key);
                }
            }
        }
    }

    /**
     * @param scelta scelta di funzione di scorrimento della matrice del pannello della gui
     */
    private void iterateMatrix(int scelta) {
        for (int i = 0; i < GUI.HEIGHT; i++) {
            for (int j = 0; j < GUI.WIDTH; j++) {
                if (scelta == 0) {
                    setMap(i, j);
                } else if (scelta == 1) {
                    updateFrame(i, j);
                } else if (scelta == 2) {
//                    printArena(i, j);
                }
            }
        }
    }

    /**
     * Aggiorna il nextFrame della gui rispetto alla situazione attuale della currentAlive
     * @param y indice di riga
     * @param x indice di colonna
     */
    private void updateFrame(int y, int x) {
        gui.nextFrame[y][x] = (currentAlive.containsKey(key(y, x)));
    }

    /**
     * Imposta la currentAlive casualmente generando posizioni casuali nella matrice,
     * tanti quanti dice n_starting_players
     * anche questa è thread-safe per non andare in conflitto con setMap potenzialmente chiamata dal thread della gui
     * quando si clicca su pannello
     */
    @Override
    public void setMapRandom() {
        synchronized (lock) {
            random_seed = new Random();
            int y, x;
            for (int n = 0; n < n_starting_agents; n++) {
                y = random_seed.nextInt(GUI.HEIGHT);
                x = random_seed.nextInt(GUI.WIDTH);
                Integer k = key(y, x);
                if (currentAlive.containsKey(k)) {
                    n--;
                    continue;
                }
                Ant ant = new Ant(y, x);
                currentAlive.put(k, ant);
                born += 1;
            }
        }
    }

    /**
     * called by UI.GUI on a click or a click and drag of the mouse on the grid
     * It generates new ants in the squares clicked on or kill them if the square was already filled
     * @param y row number
     * @param x column number
     */
    @Override
    public void setMap(int y, int x) {
        synchronized (lock) {
            Integer k = key(y, x);
            if (gui.currentFrame[y][x]) {
                if (!currentAlive.containsKey(k)) {     // inserisci chiave solo se libera, quindi senza sovrascrivere
                    Ant ant = new Ant(y, x);
                    currentAlive.put(k, ant);
                    born += 1;
                }
            } else {
                Ant ant = currentAlive.remove(k);     // se la chiave già non c'è remove non fa nulla
                if (ant != null) {
                    ant.die();
                    dead += 1;
                }
            }
        }
    }

    /**
     * @param y indice di riga
     * @param x indice di colonna
     * @return la chiave univoca associata alla coppia di coordinate y, x
     */
    public static Integer key(int y, int x) {
        return (y * GUI.WIDTH) + x;
    }

    /**
     * Funzione inversa della key
     * @param chiave una chiave del dizionario
     * @param columnOrRow 0 per sapere l'indice di colonna, 1 per sapere l'indice di riga
     * @return l'indice o di riga o di colonna associato alla chiave in funzione del parametro d'input
     */
    private static int coordinates(Integer chiave, int columnOrRow) {
        if (columnOrRow == 0) {
            return chiave / GUI.WIDTH;
        }
        return chiave % GUI.WIDTH;
    }

    private void death(Integer hashKey) {
        Ant ant = currentAlive.remove(hashKey);
        ant.die();
        dead += 1;
    }

    /**
     * metodo di generazione di una coppia di numeri pseudo-casuali all'interno dei range passati in input
     * inoltre sistema i numeri generati casualmente affinché rientrino all'interno della dimensione della gui
     * @param lowerbound_i estremo inferiore di riga
     * @param upperbound_i estremo superiore di riga
     * @param lowerbound_j estremo inferiore di colonna
     * @param upperbound_j estremo superiore di colonna
     * @return la coppia di numeri casuali
     */
    private int [] posizioniRandom(int lowerbound_i, int upperbound_i, int lowerbound_j, int upperbound_j) {
        random_seed = new Random();
        int[] pos = new int[2];
        pos[0] = random_seed.nextInt(upperbound_i - lowerbound_i) + lowerbound_i;
        pos[1] = random_seed.nextInt(upperbound_j - lowerbound_j) + lowerbound_j;
        pos[0] = Integer.max(Integer.min(pos[0], GUI.HEIGHT - 1), 0);
        pos[1] = Integer.max(Integer.min(pos[1], GUI.WIDTH - 1), 0);
        return pos;
    }

    public static Map<Integer, Ant> getCurrentAlive() {
        return currentAlive;
    }

    private void resetStats() {
        born = 0;
        dead = 0;
    }

    private void printStats(int choice) {
        if (choice == 0) {
            System.out.println("Total born is" + born);
            System.out.println("Total dead is" + dead);
        }
        else if (choice == 1) {
            iterateHashMap(1);
        }
    }

    private void printHashMap(Integer hashKey) {
        int i = coordinates(hashKey, 0);
        int j = coordinates(hashKey, 1);
        System.out.println("K:" + i + "," + j + " V:" + currentAlive.get(hashKey));
    }

    @Override
    public void resetMap() {
        currentAlive = new Hashtable<>();
        reset = true;
    }

    @Override
    public void setRandom(boolean r) {
        random = r;
    }

    @Override
    public void setStartingRandomAgents(int number) {
        n_starting_agents = number;
    }

    @Override
    public Timer getTimer() {
        return t;
    }
}