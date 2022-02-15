package Ants;

import UI.GUI;

import java.awt.*;
import java.util.Random;

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
    public final Color color = new Color(75, 75, 75);

    /**
     * the column number in the grid of this ant
     */
    private Integer xPos;

    /**
     * the row number on the grid of this ant
     */
    private Integer yPos;

    /**
     * random seed for stochastic actions
     */
    private Random random_seed;

    /**
     * for exiting move loopholes where no place is available,
     * after 5 trials quit trying and wait or do something else
     */
    private Integer safe_exit = 0;

    /**
     * the 4 cardinal directions
     */
    private enum Direction {N, E, S, O}

    /**
     * the chosen direction to follow
     * If no major events occur (other ant meeting, feromon sniffing)
     * then every n random turns the ants changes direction randomly.
     *
     */
    private Direction chosenDir = Direction.N;

    /**
     * the counter for the change of the direction
     */
    private Integer countDir = 0;

    /**
     * the max number of turns before an ant following a path changes mind and changes direction
     */
    private Integer changeDirection = 5;

    /**
     * This constructor is called only by AntSimulator at the start of the simulation
     * For newborn ants ?? is called instead.
     * @param y the column number in the grid
     * @param x the row number in the grid
     */
    Ant (Integer y, Integer x) {
        yPos = y;
        xPos = x;
        random_seed = new Random();
        changeDirection = random_seed.nextInt(2, 11);
        this.life = 100;
    }

    void action() {
        safe_exit = 0;
        movement();
    }

    /**
     * the complete algorithm who controls the movement of an ant
     */
    void movement() {
        // choose to move randomly or follow a scent of food or feromon trail
        // if you choose to move randomly
        countDir += 1;
        if (countDir > changeDirection) {
            boolean dirFound = false;
            while (safe_exit < 6 && !dirFound) {
                dirFound = findRandomDirection();
            }
            if (!dirFound) {
                // call the other action
            }
            countDir = 0;
        }
        move();
    }

    /**
     * move the ant in the chosen Direction
     */
    void move() {
        switch (chosenDir) {
            case N -> {
                if (posIsFree(xPos, yPos - 1)) this.yPos = yPos - 1;
                else countDir = changeDirection;
            }
            case E -> {
                if (posIsFree(xPos + 1, yPos)) this.xPos = xPos + 1;
                else countDir = changeDirection;
            }
            case S -> {
                if (posIsFree(xPos, yPos + 1)) this.yPos = yPos + 1;
                else countDir = changeDirection;
            }
            case O -> {
                if (posIsFree(xPos - 1, yPos)) this.xPos = xPos - 1;
                else countDir = changeDirection;
            }
        }
    }

    /**
     * finds a new direction to follow
     * it must be free
     * safe_exit is for {@code movement()} to avoid keeping searching an available position forever
     * @return true if it finds a new direction, false otherwise
     */
    private boolean findRandomDirection() {
        random_seed = new Random();
        int direction = random_seed.nextInt(4);
        switch (direction) {
            case 0 -> {     // Nord
                if (posIsFree(xPos, yPos - 1)) {
                    chosenDir = Direction.N;
                    return true;
                }
                else {
                    safe_exit += 1;
                    return false;
                }
            }
            case 1 -> {     // Est
                if (posIsFree(xPos + 1, yPos)) {
                    chosenDir = Direction.E;
                    return true;
                }
                else {
                    safe_exit += 1;
                    return false;
                }
            }
            case 2 -> {     // Sud
                if (posIsFree(xPos, yPos + 1)) {
                    chosenDir = Direction.S;
                    return true;
                }
                else {
                    safe_exit += 1;
                    return false;
                }
            }
            case 3 -> {     // Ovest
                if (posIsFree(xPos - 1, yPos)) {
                    chosenDir = Direction.O;
                    return true;
                }
                else {
                    safe_exit += 1;
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * control whether a position in the grid is free or out of bounds or already occupied
     * @param xPos the column number
     * @param yPos the row number
     * @return true if it is free, false otherwise
     */
    private boolean posIsFree(Integer xPos, Integer yPos) {
        boolean response = true;
        if (xPos < 0 || yPos < 0 || xPos >= GUI.WIDTH || yPos >= GUI.HEIGHT) response = false;
        Ant otherAnt = AntSimulator.getCurrentAlive().get(AntSimulator.key(yPos, xPos));
        if (otherAnt != null && otherAnt.getLife() > 0) response = false;
        return response;
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