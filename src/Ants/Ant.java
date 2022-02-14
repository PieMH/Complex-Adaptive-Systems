package Ants;

import java.awt.*;

/**
 *
 */
public class Ant {

    /**
     * total days of life expectancy of an ant
     */
    public Integer life = 100;

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
     * This constructor is called only by AntSimulator at the start of the simulation
     * For newborn ants ?? is called instead.
     * @param y the column number in the grid
     * @param x the row number in the grid
     */
    Ant (Integer y, Integer x) {
        xPos = x;
        yPos = y;
    }

    void action() {

    }

    void die() {
        life = 0;
    }

    void age() {
        life -= 1;
    }

    public Color getColor() {
        return color;
    }

    public Integer getLife() {
        return life;
    }
}