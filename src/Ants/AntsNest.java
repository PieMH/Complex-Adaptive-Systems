package Ants;

import UI.GUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * The class who sets the law on which an ant's nest operates.
 * Most important method is transmitGenetics() which implements a genetic algorithm.
 * @see AntSimulator
 * @see FoodSource
 * @see Ant
 * @see Pheromone
 */
public class AntsNest {

    /**
     * the color for the entrance of the ant's nest
     */
    public final Color color = new Color(30, 30, 30);

    /**
     * the amount of food stored in this nest
     */
    private Double reservoir;

    /**
     * the entrance of the ant's nest is composed of 4 squares in the centre of the grid
     * this is the top left entrance
     */
    public Integer nestEntrance1;

    /**
     * the entrance of the ant's nest is composed of 4 squares in the centre of the grid
     * this is the top right entrance
     */
    public Integer nestEntrance2;

    /**
     * the entrance of the ant's nest is composed of 4 squares in the centre of the grid
     * this is the bottom left entrance
     */
    public Integer nestEntrance3;

    /**
     * the entrance of the ant's nest is composed of 4 squares in the centre of the grid
     * this is the bottom right entrance
     */
    public Integer nestEntrance4;

    /**
     * the keys associated to the 12 positions around the nest
     */
    private final ArrayList<Integer> spawnPositions;

    /**
     * the counter to keep track of how many ants shall be born in the next days
     */
    int newBorn;

    /**
     * for every newborn ants this y pos will get updated
     * it is the translation from a key to the y position
     */
    private Integer spawnY;

    /**
     * for every newborn ants this x pos will get updated
     * it is the translation from a key to the x position
     */
    private Integer spawnX;

    /**
     * the 8 attributes that make up for the genetic code of an ant
     * This is actually the DNA of the nest, or of the colony.
     * Every newborn ant will be born with this genetic code which is a mix of the father DNA
     * and the DNA of all father-ants who previously came to the nest to deposit food and eggs.
     */
    ArrayList<Double> antAttributes = new ArrayList<>(8);

    /**
     * creates and spawn the entrance to the nest in the environment.
     * Sets the data structures to their starting values
     */
    AntsNest() {
        nestEntrance1 = AntSimulator.key(Math.floorDiv(GUI.HEIGHT, 2), Math.floorDiv(GUI.WIDTH - 1, 2)); // the top left square in the centre of the grid
        nestEntrance2 = nestEntrance1 + 1;  // the square on the right has exactly the next value
        nestEntrance3 = nestEntrance1 + GUI.WIDTH; // the square on the left bottom corner, just add the width of the grid
        nestEntrance4 = nestEntrance3 + 1;

        reservoir = 10000.0;

        newBorn = 0;

        spawnPositions = new ArrayList<>();
        // removing or adding a GUI.WIDTH is going respectively up or down a row in the grid
        // removing or adding a 1 is going respectively left or right a column in the grid
        // we are doing the 12 positions around a nest clockwise starting from the square on the above and left of nestEntrance1
        spawnPositions.add(0, nestEntrance1 - 1 - GUI.WIDTH);
        spawnPositions.add(1, nestEntrance1 - GUI.WIDTH);
        spawnPositions.add(2, nestEntrance2 - GUI.WIDTH);
        spawnPositions.add(3, nestEntrance2 + 1 - GUI.WIDTH);
        spawnPositions.add(4, nestEntrance2 + 1);
        spawnPositions.add(5, nestEntrance4 + 1);
        spawnPositions.add(6, nestEntrance4 + 1 + GUI.WIDTH);
        spawnPositions.add(7, nestEntrance4 + GUI.WIDTH);
        spawnPositions.add(8, nestEntrance3 + GUI.WIDTH);
        spawnPositions.add(9, nestEntrance3 - 1 + GUI.WIDTH);
        spawnPositions.add(10, nestEntrance3 - 1);
        spawnPositions.add(11, nestEntrance1 - 1);

        // the current ant traits from the Ant(y,x,nest) constructor. See that for reference.
        antAttributes.add(0, null); // nTraitsToTransmit
        antAttributes.add(1, null); // changeDirection
        antAttributes.add(2, null); // maxLeaveTrail
        antAttributes.add(3, null); // strengthOfNewTrailPheromone
        antAttributes.add(4, null); // maxStomachCapacity
        antAttributes.add(5, null); // foodToEatEveryDay
        antAttributes.add(6, null); // transferringSpeed
        antAttributes.add(7, null); // minRoaming
    }

    /**
     * check if the integer passed is associated to a position in the grid occupied by a nest entrance
     * @param key the integer associated to a key in the grid
     * @return true if the position passed is within the nest, false otherwise
     */
    boolean inNest(Integer key) {
        return (key.equals(nestEntrance1) || key.equals(nestEntrance2) || key.equals(nestEntrance3) || key.equals(nestEntrance4));
    }

    /**
     * called by an ant when it wants to add some of its food to the nest's reservoir
     * @param quantity how much food to add to the reservoir
     */
    void addReserves(double quantity) {
        reservoir += quantity;
    }

    /**
     * called by a famished ant who interacts with the nest
     * @param howMuch how much food the ant want to take for itself
     * @return true if there is the required food in the reservoir
     */
    boolean getFoodFromReserves(double howMuch) {
        if (reservoir - howMuch > 1) {
            reservoir -= howMuch;
            return true;
        }
        return false;
    }

    /**
     * whenever an ant come tho the nest to drop some food to the reservoir,
     * then it will lay two eggs
     */
    void triggerReproduction() {
        newBorn += 2;
    }

    /**
     * called by AntSimulator.
     * Search for a free spawn point in the grid next to the nest,
     * then creates a new ant there with the latest DNA memorized in the nest
     * @return the newborn ant, can return null if conditions are not met
     */
    Ant reproduction() {
        if (searchSpawnPoint() && newBorn > 0) {   // this call updates spawnY and spawnY, to am available spot on the grid next to the nest
            newBorn -= 1;
            return new Ant(spawnY, spawnX, this, antAttributes);
        }
        return null;
    }

    /**
     * The method that implements a version of a generic genetic algorithm.
     * The method sets the genetic code to pass to newborn ants that is the fusion of two parts.
     * The first part of the genetic code comes directly from the father ant.
     * The second part is from the genetic code already present in the nest, which is a contribution of all
     * the ants in the history that have transmitted its genetic code to the nest.
     * @param attributes the genetic code of the father ant, which is made of 8 attributes
     */
    void transmitGenetics(ArrayList<Double> attributes) {
        // do some genetic algorithm code
        // with the crossover value "nTraitsToTransmit" and a random point to start copying
        Random r = new Random();
        int start = r.nextInt(0, 8);  // where to start copying genetics values
        for (int i = start; i < start + 8; i++) {
            if (i - start < attributes.get(0)) {    // nTraitsToTransmit between 1 and 8
                antAttributes.set(i % 8, attributes.get(i % 8));
            }
            // if it's the first ant than get its whole genetic code
            else if (antAttributes.get(i % 8) == null) {
                antAttributes.set(i % 8, attributes.get(i % 8));
            }
        }
    }

    /**
     * search a free point, next to the nest, to create a baby ant
     * @return true if a free point is found, ALSO updates spawnY and SpawnX as a collateral effect
     */
    private boolean searchSpawnPoint() {
        Random r = new Random();
        ArrayList<Integer> spawnKeys = new ArrayList<>(spawnPositions);
        int n = spawnKeys.size();
        for (int i = 0; i < 12; i++) {
            int index = r.nextInt(n);
            n -= 1;
            int k = spawnKeys.remove(index);
            int y = AntSimulator.coordinates(k, 0);
            int x = AntSimulator.coordinates(k, 1);
            if (isFree(y, x)) {
                spawnY = y;
                spawnX = x;
                return true;
            }
        }
        return false;
    }

    /**
     * An important method who controls if the given position is occupied or free.
     * @param yPos the row number
     * @param xPos the column number
     * @return false if there is an ant or a food's source who sits currently in the given position, if there is no one return true
     */
    private boolean isFree(Integer yPos, Integer xPos) {

        // a living ant
        Ant a = AntSimulator.getCurrentAlive().get(AntSimulator.key(yPos, xPos));
        if (a != null && a.getLife() > 0) return false;

        // a food's source
        FoodSource food = AntSimulator.getCurrentFood().get(AntSimulator.key(yPos, xPos));
        if (food != null) return false;

        return true;
    }

    double getReservoir() {
        return reservoir;
    }

    public Color getColor() {
        return color;
    }

    Integer getNestEntrance1() {return nestEntrance1;}
}
