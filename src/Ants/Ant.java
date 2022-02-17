package Ants;

import UI.GUI;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 */
public class Ant {

    /**
     * total days of life expectancy of an ant
     */
    public Integer life;

    /**
     * the color used by UI.GUI to paint the {@code GUI.innerPanel} correctly
     */
    public final Color color = new Color(90, 90, 90);

    /**
     * the column number in the grid of this ant
     */
    private Integer xPos;

    /**
     * the row number on the grid of this ant
     */
    private Integer yPos;
    
    private Integer nextX;
    
    private Integer nextY;

    /**
     * random seed for stochastic actions
     */
    private Random random_seed;

    /**
     * the 8 cardinal directions
     */
    private enum Direction {N, NE, E, SE, S, SO, O, NO}

    /**
     * the chosen direction to follow
     * If no major events occur (other ant meeting, pheromone sniffing)
     * then every n random turns the ants changes direction randomly.
     *
     */
    private Direction chosenDir;

    /**
     * the counter for the change of the direction
     */
    private Integer countDir;

    /**
     * the max number of turns before an ant following a path changes mind and changes direction
     */
    private Integer changeDirection;

    private boolean onARandomPath = true;

    /**
     * the nest of this ant
     */
    private final AntsNest nest;

    /**
     * This constructor is called only by AntSimulator at the start of the simulation
     * For newborn ants ?? is called instead.
     * @param y the column number in the grid
     * @param x the row number in the grid
     */
    Ant (Integer y, Integer x, AntsNest nest) {
        yPos = y;
        xPos = x;
        random_seed = new Random();
        changeDirection = random_seed.nextInt(2, 11);
        countDir = changeDirection;
        this.life = 100;
        this.nest = nest;
    }

    /**
     * called by AntSimulator to activate an Ant.
     * The possible actions are:
     * <ol>
     *     <li>search the 8 squares around you</li>
     *     <li>interact with something around you</li>
     *     <li>decide if you want to follow something you found</li>
     *     <li>follow your trail</li>
     *     <li>after some time if you are on a random trail change it randomly>
     * </ol>
     */
    void action() {
        search(true, false);

        movement();
    }

    /**
     * the complete algorithm who controls the movement of an ant
     */
    void movement() {
        if (onARandomPath) {
            countDir += 1;
        }

        // follow your path
        if (countDir < changeDirection) {
            translateDirInPos(chosenDir);    // updates nextY and nextX
            if (move(nextY, nextX)) return;
        }

        // otherwise, search a random path
        search(false, true);

        move(nextY, nextX);
    }

    void search(boolean something, boolean newDirection) {
        random_seed = new Random();
        ArrayList<Direction> directionList = new ArrayList<>(List.of(Direction.values()));
        int index;
        for (int i = 0; i < 8; i++) {
            index = random_seed.nextInt(directionList.size());
            Direction dir = directionList.remove(index);
            if (something) {
                translateDirInPos(dir);     // updates nextY and nextX
                interact(nextY, nextX);
            }
            else if (newDirection) {
                if (findRandomDirection(dir)) {
                    return;
                }
            }
        }
    }

    void interact(Integer y, Integer x) {
        Object o = whoIsThere(y, x);
//        chooseWhatToDo(o);  // update chosenDir // update countDir  // update onARandomPath
    }

    /**
     *
     * @param y the column number in the grid
     * @param x the row number in the grid
     */
    private boolean move(Integer y, Integer x) {
        if (whoIsThere(y, x) == null && inBounds(y, x) && (!Objects.equals(yPos, y) || !Objects.equals(xPos, x))) {
            this.yPos = y;
            this.xPos = x;
            return true;
        }
        else countDir = changeDirection;
        return false;
    }

    /**
     * finds a new direction to follow
     * it must be free
     * safe_exit is for {@code movement()} to avoid keeping searching an available position forever
     * @return true if it finds a new direction, false otherwise
     */
    private boolean findRandomDirection(Direction dir) {
        translateDirInPos(dir);     // updates nextY and nextX

        if (whoIsThere(nextY, nextX) == null && inBounds(nextY, nextX) && (!Objects.equals(yPos, nextY) || !Objects.equals(xPos, nextX))) {
            chosenDir = dir;
            countDir = 0;
            onARandomPath = true;
            return true;
        }
        return false;
    }

    private void translateDirInPos(Direction direction) {
        nextX = this.xPos;
        nextY = this.yPos;
        switch (direction) {
            case N -> // Nord
                nextY -= 1;
            case NE -> {     // NordEst
                nextY -= 1;
                nextX += 1;
            }
            case E -> // Est
                nextX += 1;
            case SE -> {     // SudEst
                nextY += 1;
                nextX += 1;
            }
            case S -> // Sud
                nextY += 1;
            case SO -> {     // SudOvest
                nextY += 1;
                nextX -= 1;
            }
            case O -> // Ovest
                nextX -= 1;
            case NO -> {     // NordOvest
                nextY -= 1;
                nextX -= 1;
            }
        }
    }

    /**
     *
     * @param xPos the column number
     * @param yPos the row number
     * @return ??? to do with generics
     */
    private Object whoIsThere(Integer yPos, Integer xPos) {

        // another living ant
        Ant otherAnt = AntSimulator.getCurrentAlive().get(AntSimulator.key(yPos, xPos));
        if (otherAnt != null && otherAnt.getLife() > 0) return otherAnt;

        // your nest
        if (nest.inNest(AntSimulator.key(yPos, xPos))) return nest;

        // a food's source
        FoodSource food = AntSimulator.getCurrentFood().get(AntSimulator.key(yPos, xPos));
        if (food.getAmountLeft() > 0) return food;

        return null;
    }

    /**
     * to tell if the position given as a parameter is actually out of the bounds of the grid
     * @param xPos the column number
     * @param yPos the row number
     * @return false if it is out of the bounds, true otherwise
     */
    private boolean inBounds(Integer yPos, Integer xPos) {
        return (xPos >= 0 && yPos >= 0 && xPos < GUI.WIDTH && yPos < GUI.HEIGHT);
    }

    /**
     * kills an ant, mainly called by {@code AntSimulator}
     */
    void die() {
        life = 0;
    }  // maybe a call to the Java Garbage Collector

    /**
     * decrease the value of life of the ant, simulates aging
     */
    void age() {
        life -= 1;
    }

    public Color getColor() {
        return color;
    }

    public Integer getLife() {
        return life;
    }

    public Integer getPos() {
        return AntSimulator.key(yPos, xPos);
    }
}