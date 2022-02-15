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
     * This constructor is called only by AntSimulator at the start of the simulation
     * For newborn ants ?? is called instead.
     * @param y the column number in the grid
     * @param x the row number in the grid
     */
    Ant (Integer y, Integer x) {
        xPos = x;
        yPos = y;
        this.life = 100;
    }

    void action() {
        safe_exit = 0;
        if (move()) {

        }
    }

    boolean move() {
        boolean dirFound = false;
        while (safe_exit < 6 || !dirFound) {
            dirFound = findRandomDirection();
        }
        if (dirFound) {
            // call the other action
        }
        return dirFound;
    }

    private boolean findRandomDirection() {
        random_seed = new Random();
        int direction = random_seed.nextInt(4);
        switch (direction) {
            case 0 -> {     // up
                if (posIsFree(xPos, yPos - 1)) {
                    this.yPos = yPos - 1;
                    return true;
                }
                else {
                    safe_exit += 1;
                    return false;
                }
            }
            case 1 -> {     // right
                if (posIsFree(xPos + 1, yPos)) {
                    this.xPos = xPos + 1;
                    return true;
                }
                else {
                    safe_exit += 1;
                    return false;
                }
            }
            case 2 -> {     // down
                if (posIsFree(xPos, yPos + 1)) {
                    this.yPos = yPos + 1;
                    return true;
                }
                else {
                    safe_exit += 1;
                    return false;
                }
            }
            case 3 -> {     // left
                if (posIsFree(xPos - 1, yPos)) {
                    this.xPos = xPos - 1;
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

    private boolean posIsFree(Integer xPos, Integer yPos) {
        boolean response = true;
        if (xPos < 0 || yPos < 0 || xPos > GUI.WIDTH || yPos > GUI.HEIGHT) response = false;
        Ant otherAnt = AntSimulator.getCurrentAlive().get(AntSimulator.key(yPos, xPos));
        if (otherAnt != null && otherAnt.getLife() > 0) response = false;
        return response;
    }

    void die() {
        life = 0;
    }  // maybe a call to the Java Garbage Collector

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