package Ants;

import java.awt.*;

public class Pheromone {

    /**
     * the color for every food source
     */
    public final Color color = new Color(10, 80, 90);

    /**
     * the row number in the grid of this Food Source
     */
    private Integer yPos;

    /**
     * the column number in the grid of this Food Source
     */
    private Integer xPos;

    Pheromone(Integer y, Integer x) {
        this.yPos = y;
        this.xPos = x;
    }

    Color getColor() {
        return color;
    }
}
