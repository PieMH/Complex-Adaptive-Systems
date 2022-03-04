package Ants;

import UI.GUI;
import Interfaces.CASModel;

import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * The main class that delineate the algorithm of the Ant sim model.
 * It manages the general behavior of the sim and its core functionalities.
 * It also serves as a mainframe for the classes Ants, FoodSource, AntsNest, Pheromone.
 * @see Ant
 * @see AntsNest
 * @see FoodSource
 * @see Pheromone
 */
public class AntSimulator implements CASModel {

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

    private static Map<Integer, Pheromone> currentTrailPheromones;

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
    static int n_starting_agents;

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

    //  Stats carrying variables to be passed to OutputManager

    OutputManager outputManager;

    static int day = 0;

    /**
     * total number of ants born in the simulation
     */
    static int totBorn = 0;

    /**
     * total number of dead ants in the simulation
     */
    static int totDead = 0;

    static int newBorn = 0;

    static int newDead = 0;

    static int totNewFood = 0;

    static int totFinishedFood = 0;

    static int newFood = 0;

    static int finishedFood = 0;

    static int totCreatedPheromones = 0;

    static int totDecayedPheromones = 0;

    static int newPheromones = 0;

    static int decayedPheromones = 0;

    public AntSimulator(GUI gui) {
        this.gui = gui;
        outputManager = new OutputManager();
        resetMap();
        iterateMatrix(1);     // updateFrame
    }

    /**
     * called by UI.OptionsMenu on time = 0 or on a click of the button "Apply"
     */
    @Override
    public void startSimulation() {
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
                nestReproduction();
                if (Math.random() < 0.05) balanceFood();    // with a probability of 5% every turn add a food's source
                agePheromones();
//                printStats(2);
                iterateMatrix(1);     // updateFrame
                callsOutputManager();
                resetNewStats();
                day += 1;
                gui.currentFrame = gui.nextFrame;
                gui.nextFrame = new boolean[GUI.HEIGHT][GUI.WIDTH];
                gui.getPanel().repaint();
            }
        };
        t = new Timer(delay, taskPerformer);
        t.start();
    }

    /**
     * Activate an ant and keeps updated the currentAlive hashmap
     * @param hashKey the kye of the hashmap currentAlive
     */
    private void evolve(Integer hashKey) {
        Ant currentAnt = currentAlive.get(hashKey);

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
        totDead += 1;
        newDead += 1;
    }

    private void nestReproduction() {
        int i = nest.newBorn;
        for (; i > 0; i--) {
            Ant a = nest.reproduction();
            if (a != null) {    // redundant
                currentAlive.put(a.getPos(), a);
                totBorn += 1;
                newBorn += 1;
            }
        }
    }

    /**
     * invoked by an ant who is the last to access to a food's source before it is completely run out
     * @param food the FoodSource food object to erase from the currentFood hashmap because it is worn out
     */
    static void foodFinished(FoodSource food) {
        currentFood.remove(key(food.yPos, food.xPos));
        totFinishedFood += 1;
        finishedFood += 1;
    }

    /**
     * Called by startSimulation to balance the food in the environment of the model
     */
    private void balanceFood() {
        int fSize = currentFood.size();
        if (fSize < maximal) {
            random_seed = new Random();
            int y = random_seed.nextInt(GUI.HEIGHT);
            int x = random_seed.nextInt(GUI.WIDTH);
            Integer k = key(y, x);
            if (!currentAlive.containsKey(k) && !nest.inNest(k)) {  // if it is a free spot ok, otherwise we'll try on the next frame
                FoodSource food = new FoodSource(y, x);
                currentFood.put(k, food);
                newFood += 1;
                totNewFood += 1;
            }
        }
    }

    private void agePheromones() {
        Map<Integer, Pheromone> nextTrailPheromones;
        synchronized (lock) {
            nextTrailPheromones = new Hashtable<>(currentTrailPheromones);
            for (Pheromone phe : nextTrailPheromones.values()) {
                phe.decay();
            }
        }
    }

    static void erasePheromone(Pheromone trailPhe) {
        currentTrailPheromones.remove(key(trailPhe.yPos, trailPhe.xPos));
        decayedPheromones += 1;
        totDecayedPheromones += 1;
    }

    static void addPheromone(Pheromone trailPhe) {
        currentTrailPheromones.put(key(trailPhe.yPos, trailPhe.xPos), trailPhe);
        newPheromones += 1;
        totCreatedPheromones += 1;
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
     * @param choice choice di funzione di scorrimento del dizionario
     */
    private void iterateCurrentAlive(int choice) {
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
                if (choice == 0) {
                    evolve(key);
                }
                else if (choice == 1) {
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
        gui.nextFrame[y][x] = (currentAlive.containsKey(key(y, x)) ||
                                nest.inNest(key(y, x)) ||
                                currentFood.containsKey(key(y, x))
                              );
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
                if (currentAlive.containsKey(k) || nest.inNest(k) || currentFood.containsKey(k)) {    // if it is already occupied
                    n--;
                    continue;
                }
                Ant ant = new Ant(y, x, nest);
                currentAlive.put(k, ant);
                totBorn += 1;
                newBorn += 1;
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
                if (!currentAlive.containsKey(k) && !nest.inNest(k) && !currentFood.containsKey(k)) {     // only if it is empty
                    Ant ant = new Ant(y, x, nest);
                    currentAlive.put(k, ant);
                    totBorn += 1;
                    newBorn += 1;
                }
            } else {
                Ant ant = currentAlive.remove(k);     // se la chiave già non c'è remove non fa nulla
                if (ant != null) {
                    ant.die();
                    totDead += 1;
                    newDead += 1;
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
     * @param rowOrColumn 0 to know the row index on the 2 dimensional grid; 1 to know the column index on the 2 dimensional grid
     * @return the column or row index based on {@code rowOrColumn}.
     */
    public static int coordinates(Integer chiave, int rowOrColumn) {
        if (rowOrColumn == 0) {
            return chiave / GUI.WIDTH;
        }
        return chiave % GUI.WIDTH;
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
     * called by UI.GUI paintLife to know whether to paint the life of an ant based on its age
     * or to paint another object normally
     * @param y the row number value
     * @param x the column number value
     * @return true if this position is currently occupied by a living ant, this is granted by {@code evolve()}
     */
    public static boolean isPheromone(Integer y, Integer x) {
        return (currentTrailPheromones.get(key(y, x)) != null);
    }

    /**
     * invoked on a click on reset on the UI.GUI.
     * <ol>
     * <li>resets the three hashMaps
     * <li>creates a new nest
     * <li>recalculates Pheromone.maxStrength (it depends on UI.GUI.DIMENSION changes)
     * <li>spawns new food on the grid
     * </ol>
     */
    @Override
    public void resetMap() {
        currentAlive = new Hashtable<>();
        currentFood = new Hashtable<>();
        currentTrailPheromones = new Hashtable<>();
        nest = new AntsNest();
        n_starting_agents = Math.floorDiv(GUI.DIMENSION, 20);

        resetAllStats();

        // the following is the calculation of Pheromone.maxStrength, actually is derived from Ant calculation, in fact it is equal to = ceil(2 * Ant.minChange^2)
        Pheromone.setMaxStrength( (int) Math.ceil( (2 * Math.pow( (int) Math.max( 2, Math.floor(Math.log(GUI.DIMENSION / 5.0)) - 1), 2))));

        // two point distance function, the maximum distance of any ant it is from position (0,0) to nestEntrance1.y,nestEntrance1.x
        Ant.setDMax(Math.sqrt(Math.pow(coordinates(nest.getNestEntrance1(), 0), 2) + Math.pow(coordinates(nest.getNestEntrance1(), 1), 2)));

        reset = true;

        random_seed = new Random();
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
                newFood += 1;
                totNewFood += 1;
            }
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

    private void printFood() {
        currentFood.forEach((key, value) -> System.out.println(key + " " + value));
        System.out.println("_________________________________________________________");
    }

    /**
     * the debugger function responsible for printing to stdout the stats of the simulation
     * @param choice which kind of prints do you want:
     *               0 is for displaying only the total ants born and dead;
     *               1 is for displaying the whole hashMap keys and values associated
     */
    private void printStats(int choice) {
        if (choice == 0) {
            System.out.println("Total born is" + totBorn);
            System.out.println("Total dead is" + totDead);
        }
        else if (choice == 1) {
            iterateCurrentAlive(1);
        }
        else if (choice == 2) {
            printFood();
        }
    }

    /**
     * reset the stats of the simulation
     */
    private void resetAllStats() {
        day = 0;

        totBorn = 0;
        totDead = 0;
        newBorn = 0;
        newDead = 0;

        totNewFood = 0;
        totFinishedFood = 0;
        newFood = 0;
        finishedFood = 0;

        totCreatedPheromones = 0;
        totDecayedPheromones = 0;
        newPheromones = 0;
        decayedPheromones = 0;
    }

    private void resetNewStats() {
        newBorn = 0;
        newDead = 0;

        newFood = 0;
        finishedFood = 0;

        newPheromones = 0;
        decayedPheromones = 0;
    }

    private double[] doStats(Collection<?> dataset, String attribute) {
        if (dataset.isEmpty()) return null;

        Double [] values = new Double[dataset.size()];
        try {
            int i = 0;
            for (Object d : dataset) {
                Field field = d.getClass().getDeclaredField(attribute);
                field.setAccessible(true);
                double value = Double.parseDouble(field.get(d).toString());
                values[i] = value;
                i++;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Arrays.sort(values);

        double [] stats = new double[4];

        // min
        stats[0] = values[0];

        // max
        stats[1] = values[values.length - 1];

        // mean
        double sum = 0;
        for (double val : values) {
            sum += val;
        }
        stats[2] = sum / values.length;

        // median
        if (values.length % 2 == 0) {   // even
            int mid = values.length / 2;
            stats[3] = (values[mid - 1] + values[mid]) / 2;
        }
        else {  // odd
            stats[3] = values[((values.length + 1) / 2) - 1];
        }
        return stats;
    }

    private void callsOutputManager() {

        double [] lifeStats       = doStats(currentAlive.values(), "life");
        double [] starvingStats   = doStats(currentAlive.values(), "starvingMultiplier");
        double [] stomachSumStats = doStats(currentAlive.values(), "stomachSum");
        double [] roamingStats    = doStats(currentAlive.values(), "roaming");
        double [] amountLeftStats = doStats(currentFood.values(), "amountLeft");
        double [] strengthStats   = doStats(currentTrailPheromones.values(), "strength");

        double [] dna1 = doStats(currentAlive.values(), "nTraitsToTransmit");
        double [] dna2 = doStats(currentAlive.values(), "changeDirection");
        double [] dna3 = doStats(currentAlive.values(), "maxLeaveTrail");
        double [] dna4 = doStats(currentAlive.values(), "strengthOfNewTrailPheromone");
        double [] dna5 = doStats(currentAlive.values(), "maxStomachCapacity");
        double [] dna6 = doStats(currentAlive.values(), "foodToEatEveryDay");
        double [] dna7 = doStats(currentAlive.values(), "transferringSpeed");
        double [] dna8 = doStats(currentAlive.values(), "minRoaming");

        String[] dayHeader = {"Day", String.valueOf(day)};

        String[] title1 = {"FoodSource statistics"};
        String[] foodGeneralHeader = {"Total food's sources created", "Total food's sources finished", "Current number of food's sources", "New food's sources", "Finished food's sources"};
        String[] foodGeneralRecords = {String.valueOf(totNewFood), String.valueOf(totFinishedFood), String.valueOf(currentFood.size()), String.valueOf(newFood), String.valueOf(finishedFood)};
        String[] foodSpecificHeader1 = {"min amountLeft", "max amountLeft", "mean amountLeft", "median amountLeft"};
        String[] foodSpecificRecords1 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (amountLeftStats != null) foodSpecificRecords1[i] = String.valueOf(amountLeftStats[i]);
            else foodSpecificRecords1[i] = "null";
        }

        String[] title2 = {"Pheromone statistics"};
        String[] pheromonesGeneralHeader = {"Total trail pheromones created", "Total trail pheromones decayed", "Current number of trail pheromones", "New pheromones", "Decayed pheromones"};
        String[] pheromonesGeneralRecords = {String.valueOf(totCreatedPheromones), String.valueOf(totDecayedPheromones), String.valueOf(currentTrailPheromones.size()), String.valueOf(newPheromones), String.valueOf(decayedPheromones)};
        String[] pheromonesSpecificHeader1 = {"min strength", "max strength", "mean strength", "median strength"};
        String[] pheromonesSpecificRecords1 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (strengthStats != null) pheromonesSpecificRecords1[i] = String.valueOf(strengthStats[i]);
            else pheromonesSpecificRecords1[i] = "null";
        }

        String[] title3 = {"Nest statistics"};
        String[] nestGeneralHeader = {"Nest reservoirs", "Nest newborn queue"};
        String[] nestGeneralRecords = {String.valueOf(nest.getReservoir()), String.valueOf(nest.newBorn)};
        String[] title4 = {"Nest DNA code to give to the next newborn ant"};
        String[] nestDNAHeader = {"nTraitsToTransmit", "changeDirection", "maxLeaveTrail", "strengthOfNewTrailPheromone", "maxStomachCapacity", "foodToEatEveryDay", "transferringSpeed", "maxRoaming"};
        String[] nestDNARecord = {String.valueOf(nest.antAttributes.get(0)), String.valueOf(nest.antAttributes.get(1)), String.valueOf(nest.antAttributes.get(2)), String.valueOf(nest.antAttributes.get(3)), String.valueOf(nest.antAttributes.get(4)), String.valueOf(nest.antAttributes.get(5)), String.valueOf(nest.antAttributes.get(6)), String.valueOf(nest.antAttributes.get(7))};

        String[] title5 = {"Ant statistics"};
        String[] antGeneralHeader = {"Total born ants", "Total dead ants", "Current living ants", "Newborn ants", "New dead ants"};
        String[] antGeneralRecords = {String.valueOf(totBorn), String.valueOf(totDead), String.valueOf(currentAlive.size()), String.valueOf(newBorn), String.valueOf(newDead)};
        String[] antSpecificHeader1 = {"min life", "max life", "mean life", "median life"};
        String[] antSpecificRecords1 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (lifeStats != null) antSpecificRecords1[i] = String.valueOf(lifeStats[i]);
            else antSpecificRecords1[i] = "null";
        }
        String[] antSpecificHeader2 = {"min starvingMultiplier", "max starvingMultiplier", "mean starvingMultiplier", "median starvingMultiplier"};
        String[] antSpecificRecords2 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (starvingStats != null) antSpecificRecords2[i] = String.valueOf(starvingStats[i]);
            else antSpecificRecords2[i] = "null";
        }
        String[] antSpecificHeader3 = {"min stomachSum", "max stomachSum", "mean stomachSum", "median stomachSum"};
        String[] antSpecificRecords3 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (stomachSumStats != null) antSpecificRecords3[i] = String.valueOf(stomachSumStats[i]);
            else antSpecificRecords3[i] = "null";
        }
        String[] antSpecificHeader4 = {"min roaming", "max roaming", "mean roaming", "median roaming"};
        String[] antSpecificRecords4 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (roamingStats != null) antSpecificRecords4[i] = String.valueOf(roamingStats[i]);
            else antSpecificRecords4[i] = "null";
        }
        String[] title6 = {"Ant DNA code statistics:"};
        String[] antDNAHeader1 = {"min nTraitsToTransmit", "max nTraitsToTransmit", "mean nTraitsToTransmit", "median nTraitsToTransmit"};
        String[] antDNARecords1 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna1 != null) antDNARecords1[i] = String.valueOf(dna1[i]);
            else antDNARecords1[i] = "null";
        }
        String[] antDNAHeader2 = {"min changeDirection", "max changeDirection", "mean changeDirection", "median changeDirection"};
        String[] antDNARecords2 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna2 != null) antDNARecords2[i] = String.valueOf(dna2[i]);
            else antDNARecords2[i] = "null";
        }
        String[] antDNAHeader3 = {"min maxLeaveTrail", "max maxLeaveTrail", "mean maxLeaveTrail", "median maxLeaveTrail"};
        String[] antDNARecords3 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna3 != null) antDNARecords3[i] = String.valueOf(dna3[i]);
            else antDNARecords3[i] = "null";
        }
        String[] antDNAHeader4 = {"min strengthOfNewTrailPheromone", "max strengthOfNewTrailPheromone", "mean strengthOfNewTrailPheromone", "median strengthOfNewTrailPheromone"};
        String[] antDNARecords4 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna4 != null) antDNARecords4[i] = String.valueOf(dna4[i]);
            else antDNARecords4[i] = "null";
        }
        String[] antDNAHeader5 = {"min maxStomachCapacity", "max maxStomachCapacity", "mean maxStomachCapacity", "median maxStomachCapacity"};
        String[] antDNARecords5 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna5 != null) antDNARecords5[i] = String.valueOf(dna5[i]);
            else antDNARecords5[i] = "null";
        }
        String[] antDNAHeader6 = {"min foodToEatEveryDay", "max foodToEatEveryDay", "mean foodToEatEveryDay", "median foodToEatEveryDay"};
        String[] antDNARecords6 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna6 != null) antDNARecords6[i] = String.valueOf(dna6[i]);
            else antDNARecords6[i] = "null";
        }
        String[] antDNAHeader7 = {"min transferringSpeed", "max transferringSpeed", "mean transferringSpeed", "median transferringSpeed"};
        String[] antDNARecords7 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna7 != null) antDNARecords7[i] = String.valueOf(dna7[i]);
            else antDNARecords7[i] = "null";
        }
        String[] antDNAHeader8 = {"min minRoaming", "max minRoaming", "mean minRoaming", "median minRoaming"};
        String[] antDNARecords8 = new String[4];
        for (int i = 0; i < 4; i++) {
            if (dna8 != null) antDNARecords8[i] = String.valueOf(dna8[i]);
            else antDNARecords8[i] = "null";
        }
        String[] end = {"____________________________________________________________________________________________"};
        String[] space = {""};


        List<String[]> list = new ArrayList<>();
        list.add(dayHeader);
        list.add(title1);
        list.add(foodGeneralHeader);
        list.add(foodGeneralRecords);
        list.add(foodSpecificHeader1);
        list.add(foodSpecificRecords1);
        list.add(space);
        list.add(title2);
        list.add(pheromonesGeneralHeader);
        list.add(pheromonesGeneralRecords);
        list.add(pheromonesSpecificHeader1);
        list.add(pheromonesSpecificRecords1);
        list.add(space);
        list.add(title3);
        list.add(nestGeneralHeader);
        list.add(nestGeneralRecords);
        list.add(title4);
        list.add(nestDNAHeader);
        list.add(nestDNARecord);
        list.add(space);
        list.add(title5);
        list.add(antGeneralHeader);
        list.add(antGeneralRecords);
        list.add(antSpecificHeader1);
        list.add(antSpecificRecords1);
        list.add(antSpecificHeader2);
        list.add(antSpecificRecords2);
        list.add(antSpecificHeader3);
        list.add(antSpecificRecords3);
        list.add(antSpecificHeader4);
        list.add(antSpecificRecords4);
        list.add(space);
        list.add(title6);
        list.add(antDNAHeader1);
        list.add(antDNARecords1);
        list.add(antDNAHeader2);
        list.add(antDNARecords2);
        list.add(antDNAHeader3);
        list.add(antDNARecords3);
        list.add(antDNAHeader4);
        list.add(antDNARecords4);
        list.add(antDNAHeader5);
        list.add(antDNARecords5);
        list.add(antDNAHeader6);
        list.add(antDNARecords6);
        list.add(antDNAHeader7);
        list.add(antDNARecords7);
        list.add(antDNAHeader8);
        list.add(antDNARecords8);
        list.add(end);
        list.add(space);

        if (day % 10 == 0) outputManager.writeCSV(list);
        outputManager.writeLog(list);
    }

    public static Map<Integer, Ant> getCurrentAlive() {
        return currentAlive;
    }

    public static Map<Integer, Pheromone> getCurrentTrailPheromones() {
        return currentTrailPheromones;
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