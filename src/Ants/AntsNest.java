package Ants;

import UI.GUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class AntsNest {

    /**
     * the color for the entrance of the ant's nest
     */
    public final Color color = new Color(30, 30, 30);

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

    private final ArrayList<Integer> spawnPositions;

    private Integer spawnX;
    private Integer spawnY;

    ArrayList<Double> antAttributes = new ArrayList<>(8);

    AntsNest() {
        nestEntrance1 = AntSimulator.key(Math.floorDiv(GUI.HEIGHT, 2), Math.floorDiv(GUI.WIDTH - 1, 2)); // the top left square in the centre of the grid
        nestEntrance2 = nestEntrance1 + 1;  // the square on the right has exactly the next value
        nestEntrance3 = nestEntrance1 + GUI.WIDTH; // the square on the left bottom corner, just add the width of the grid
        nestEntrance4 = nestEntrance3 + 1;

        reservoir = 100000.0;

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
        antAttributes.add(7, null); // MaxRoaming
    }

    public Color getColor() {
        return color;
    }

    Integer getNestEntrance1() {return nestEntrance1;}

    boolean inNest(Integer key) {
        return (key.equals(nestEntrance1) || key.equals(nestEntrance2) || key.equals(nestEntrance3) || key.equals(nestEntrance4));
    }

    void addReserves(double quantity) {
        reservoir += quantity;
    }

    boolean getFoodFromReserves(double howMuch) {
        if (reservoir - howMuch > 1) {
            reservoir -= howMuch;
            return true;
        }
        return false;
    }

    double getReservoir() {
        return reservoir;
    }

    Ant reproduction() {
        if (searchSpawnPoint()) {   // this call updates spawnY and spawnY, to am available spot on the grid next to the nest
//                System.out.println("reservoir left:" + reservoir + ". spawnY:" + spawnY + ", spawnX:" + spawnX);
            return new Ant(spawnY, spawnX, this, antAttributes);
        }
        return null;
    }


    void transmitGenetics(ArrayList<Double> attributes) {
        // do some genetic algorithm code
        // with the crossover value "nTraitsToTransmit" and a random point to start copying
        Random r = new Random();
        int start = r.nextInt(0, 8);  // where to start copying genetics values
        for (int i = start; i < start + 8; i++) {
            if (i - start < attributes.get(0)) {    // nTraitsToTransmit
                antAttributes.set(i % 8, attributes.get(i % 8));
            }
            // if it's the first ant than get its whole genetic code
            else if (antAttributes.get(i % 8) == null) {
                antAttributes.set(i % 8, attributes.get(i % 8));
            }
        }

    }

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
        if (food != null && food.getAmountLeft() > 0) return false;

        return true;
    }
}