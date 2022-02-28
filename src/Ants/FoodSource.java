package Ants;

import UI.GUI;

import java.awt.*;
import java.util.Random;

public class FoodSource {

    /**
     * the color for every food source
     */
    public final Color color = new Color(230, 110, 0);

    /**
     * the row number in the grid of this Food Source
     */
    final Integer yPos;

    /**
     * the column number in the grid of this Food Source
     */
    final Integer xPos;

    /**
     * the amount of food left available for the ants to gather
     */
    private Double amountLeft;

    /**
     * the minimal value of elements in the grid if generated randomly.
     * It follows a logarithmic growth proportional to the GUI.DIMENSION value,
     * that is the total number of cells in the grid
     */
    int minimal;

    /**
     *
     */
    int maximal;

    FoodSource(Integer y, Integer x) {
        this.yPos = y;
        this.xPos = x;
        Random random_seed = new Random();
        /*
          we use a log function to set this minimal value for nextInt.
          The logic is to ensure the growth of the minimal value is logarithmic proportional
          to that of the number of cells in the grid, that is GUI.DIMENSION.
         */
        minimal = (int) Math.max(Math.floor(Math.sqrt(1.5 * GUI.DIMENSION)), 1);   // minimal number of quantity of food in this source
        maximal = minimal * 4;
        amountLeft = (double) random_seed.nextInt(minimal, maximal);
    }

    Double getAmountLeft() {
        return amountLeft;
    }

    Color getColor() {
        return color;
    }

    /**
     * called by an ant interacting with this food's source
     * @param amount the amount of food requested
     * @return the amount of food currently available in this source, if you request too much simply returns zero
     */
    double gathering(double amount) {
//        System.out.println("amountLeft:" + amountLeft);
        double available = Math.min(amount, amountLeft);
        amountLeft -= available;
        return available;
    }

    void reverseGathering(double amount) {
        amountLeft += amount;
    }
}