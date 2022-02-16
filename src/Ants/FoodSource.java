package Ants;

import UI.GUI;

import java.awt.*;
import java.util.Random;

public class FoodSource {

    /**
     * the color for every food source
     */
    public final Color color = new Color(60, 60, 10);

    /**
     * the row number in the grid of this Food Source
     */
    private Integer yPos;

    /**
     * the column number in the grid of this Food Source
     */
    private Integer xPos;

    /**
     * the amount of food left available for the ants to gather
     */
    private Integer amountLeft;

    FoodSource(Integer y, Integer x) {
        this.yPos = y;
        this.xPos = x;
        Random random_seed = new Random();
        /*
          we use a log function to set this minimal value for nextInt.
          The logic is to ensure the growth of the minimal value is logarithmic proportional
          to that of the number of cells in the grid, that is GUI.DIMENSION.
         */
        int minimal = (int) Math.max(Math.floor(Math.log(GUI.DIMENSION * GUI.DIMENSION)), 1);
        amountLeft = random_seed.nextInt(minimal, minimal * 5);
    }

    Integer getAmountLeft() {
        return amountLeft;
    }

    Color getColor() {
        return color;
    }

    boolean gatherFood() {
        if (amountLeft > 1) {
           amountLeft -= 1;
           return true;
        }
        return false;
    }
}
