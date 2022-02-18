package Ants;

import UI.GUI;
import Interfaces.Game;

import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * The main class that delineate the algorithm of the Ant sim model.
 * It manages the general behavior of the sim and its core functionalities.
 * It also serves as a mainframe for the classes Ants, FoodSource, AntsNest, Pheromone.
 */
public class AntSimulator implements Game {

    /**
     * HasMap of the ant currently alive.
     * The key is calculated always by calling {@code key(y,x)},
     * the values are the objects Ants
     */
    private static Map<Integer, Ant> currentAlive;

    /**
     *
     */
    private static Map<Integer, FoodSource> currentFood;

    /**
     * the UI.GUI that it is used to display the model
     */
    private final GUI gui;

    /**
     * used to slow down or speed up the simulation
     */
    private Timer t;

    /**
     * delay on the repaint call of the current frame
     */
    private static final int delay = 100;

    /**
     * flag used when the user click on reset on the GUI
     */
    private boolean reset;

    /**
     * Given by UI.OptionsMenu: use to invoke the {@code SetRandomMap}
     */
    static boolean random = false;

    /**
     * default value or given by UI.OptionsMenu: set number of random agents spawning in the grid
     */
    static int n_starting_agents = 2000;

    /**
     * seed for pseudorandom uniformly distributed random values
     */
    private Random random_seed;

    /**
     * lock for MUTEX between this thread and the UI.GUI one
     * used for atomic changes on the hashMap currentAlive
     */
    private final Object lock = new Object();

    /**
     * total number of ants born in the simulation
     */
    private int born = 0;

    /**
     * total number of dead ants in the simulation
     */
    private int dead = 0;

    /**
     * the nest of the ants
     */
    static AntsNest nest;

    /**
     * the minimal value of elements in the grid if generated randomly.
     * It follows a logarithmic growth proportional to the GUI.DIMENSION value,
     * that is the total number of cells in the grid
     */
    int minimal = 1;

    /**
     *
     */
    int maximal;

//    private int statsDelayCounter = 0;

    public AntSimulator(GUI gui) {
        this.gui = gui;

        resetMap();
        resetStats();
        iterateMatrix(1);     // updateFrame
    }

    /**
     * called by UI.OptionsMenu on time = 0 or on a click of the button "Apply"
     */
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
                    reset = false;
                }
                iterateCurrentAlive(0);  // evolve
//                if (statsDelayCounter == 1) {
//                    printStats(0);
//                    statsDelayCounter = 0;
//                }
//                statsDelayCounter += 1;
//                printStats(1);  // print Hash Map
//                balanceFood();
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
     * @param hashKey the kye of the hashmap currentAlive
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
     * manages a new death of an ant
     * @param hashKey the key on the hashMap currentAlive
     */
    private void death(Integer hashKey) {
        Ant ant = currentAlive.remove(hashKey);
        if (ant != null) ant.die();
        dead += 1;
    }

    /**
     * invoked by an ant who is the last to access to a food's source before it is completely run out
     * @param k the key for currentFood
     */
    static void foodFinished(Integer k) {
        currentFood.remove(k);
    }

    /**
     * Called by startGame to balance the food in the environment of the model
     */
    private void balanceFood() {
        int fSize = currentFood.size();
        if (fSize < minimal) {
            random_seed = new Random();
            int maxFood = maximal - fSize;
            for (int m = 0; m < maxFood; m++) {
                int y = random_seed.nextInt(GUI.HEIGHT);
                int x = random_seed.nextInt(GUI.WIDTH);
                Integer k = key(y, x);
                if (!currentAlive.containsKey(k) && !nest.inNest(k)) {  // if it is a free spot ok, otherwise we'll try on the next frame
                    FoodSource food = new FoodSource(y, x);
                    currentFood.put(k, food);
                }
            }
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
    private void iterateCurrentAlive(int scelta) {
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
        gui.nextFrame[y][x] = (currentAlive.containsKey(key(y, x)) || nest.inNest(key(y, x)) || currentFood.containsKey(key(y, x)));
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
                if (currentAlive.containsKey(k) || nest.inNest(k)) {    // if it is already occupied
                    n--;
                    continue;
                }
                Ant ant = new Ant(y, x, nest);
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
                if (!currentAlive.containsKey(k) && !nest.inNest(k)) {     // only if it is empty
                    Ant ant = new Ant(y, x, nest);
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
     * @param y row number
     * @param x column number
     * @return the unique key associated to the coordinates y, x on the grid.
     *          Actually it is the position on the grid seen as one dimensional
     */
    public static Integer key(int y, int x) {
        return (y * GUI.WIDTH) + x;
    }

    /**
     * Inverse function of the {@code key()} function.
     * @param chiave the key on the hashMap calculated by {@code key()}.
     * @param columnOrRow 0 to know the column index on the 2 dimensional grid; 1 to know the row index on the 2 dimensional grid
     * @return the column or row index based on {@code columnOrRow}.
     */
    private static int coordinates(Integer chiave, int columnOrRow) {
        if (columnOrRow == 0) {
            return chiave / GUI.WIDTH;
        }
        return chiave % GUI.WIDTH;
    }

    /**
     * metodo di generazione di una coppia di numeri pseudo-casuali all'interno dei range passati in input
     * inoltre sistema i numeri generati casualmente affinché rientrino all'interno della dimensione della gui
     * @param lowerBound_i estremo inferiore di riga
     * @param upperBound_i estremo superiore di riga
     * @param lowerBound_j estremo inferiore di colonna
     * @param upperBound_j estremo superiore di colonna
     * @return la coppia di numeri casuali
     */
    private int [] posizioniRandom(int lowerBound_i, int upperBound_i, int lowerBound_j, int upperBound_j) {
        random_seed = new Random();
        int[] pos = new int[2];
        pos[0] = random_seed.nextInt(upperBound_i - lowerBound_i) + lowerBound_i;
        pos[1] = random_seed.nextInt(upperBound_j - lowerBound_j) + lowerBound_j;
        pos[0] = Integer.max(Integer.min(pos[0], GUI.HEIGHT - 1), 0);
        pos[1] = Integer.max(Integer.min(pos[1], GUI.WIDTH - 1), 0);
        return pos;
    }

    /**
     * reset the stats of the simulation
     */
    private void resetStats() {
        born = 0;
        dead = 0;
    }

    /**
     * the debugger function responsible for printing to stdout the stats of the simulation
     * @param choice which kind of prints do you want:
     *               0 is for displaying only the total ants born and dead;
     *               1 is for displaying the whole hashMap keys and values associated
     */
    private void printStats(int choice) {
        if (choice == 0) {
            System.out.println("Total born is" + born);
            System.out.println("Total dead is" + dead);
        }
        else if (choice == 1) {
            iterateCurrentAlive(1);
        }
    }

    /**
     * the auxiliary function of printStats, called by it when choice is on 1
     * @param hashKey the kye of the hashmap currentAlive
     */
    private void printHashMap(Integer hashKey) {
        int i = coordinates(hashKey, 0);
        int j = coordinates(hashKey, 1);
        System.out.println("K:" + i + "," + j + " V:" + currentAlive.get(hashKey));
    }

    /**
     * called by UI.GUI paintLife and UI.GUI paintAnts
     * @param y the row number value
     * @param x the column number value
     * @return the correct color of the object in this position
     */
    public static Color getColor(Integer y, Integer x) {
        Integer k = key(y, x);
        Ant a = currentAlive.get(k);
        if (a != null) {
            return a.getColor();
        }
        else if (nest.inNest(k)) {
            return nest.getColor();
        }
        FoodSource food = currentFood.get(k);
        if (food != null) {
            return food.getColor();
        }
        return new Color(200,0,0);
    }

    /**
     * called by UI.GUI paintLife to know whether to paint the life of an ant based on its age
     * or to paint another object normally
     * @param y the row number value
     * @param x the column number value
     * @return true if this position is currently occupied by a living ant, this is granted by {@code evolve()}
     */
    public static boolean isAnt(Integer y, Integer x) {
        return (currentAlive.get(key(y, x)) != null);
    }

    /**
     * invoked on a click on reset on the UI.GUI
     */
    @Override
    public void resetMap() {
        currentAlive = new Hashtable<>();
        currentFood = new Hashtable<>();
        reset = true;

        nest = new AntsNest();

        random_seed = new Random();
        System.out.println(GUI.DIMENSION);
//        minimal = (int) Math.max(Math.floor(Math.log(GUI.DIMENSION / 5.0)) - 1, 1);   // minimal number of food's sources generated on the grid
        minimal = (int) Math.max(Math.floor(Math.pow(GUI.DIMENSION / 5.0, 1.0 / 4.0)) - 1, 1);   // minimal number of food's sources generated on the grid
        maximal = (minimal * 2) + 1;
        int maxFood = random_seed.nextInt(minimal, maximal);
        for (int m = 0; m < maxFood; m++) {
            int y = random_seed.nextInt(GUI.HEIGHT);
            int x = random_seed.nextInt(GUI.WIDTH);
            Integer k = key(y, x);
            if (!currentAlive.containsKey(k) && !nest.inNest(k)) { // balanceFood() will add food's sources to the environment eventually
                FoodSource food = new FoodSource(y, x);
                currentFood.put(k, food);
            }
        }
        System.out.println(currentFood.size());
        System.out.println(GUI.HEIGHT + " x " + GUI.WIDTH);
    }

    public static Map<Integer, Ant> getCurrentAlive() {
        return currentAlive;
    }

    public static Map<Integer, FoodSource> getCurrentFood() {
        return currentFood;
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