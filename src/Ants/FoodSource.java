package Ants;

import UI.GUI;

import java.awt.*;
import java.util.Random;

/**
 * The class who sets minimal attributes and methods shared by every food' source
 * <p>
 * @see AntSimulator
 * @see AntsNest
 * @see Ant
 * @see Pheromone
 */
public class FoodSource {

    /**
     * the color for every food source
     */
    public final Color color = new Color(255, 188, 17);

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
     * the maximal value of elements in the grid if generated randomly.
     * It follows a logarithmic growth proportional to the GUI.DIMENSION value,
     * that is the total number of cells in the grid
     */
    int maximal;

    /**
     * spawn a new FoodSource object in position (y, x)
     * then assign to it a starting value of food's resources in this source
     * @param y row spawn index
     * @param x column spawn index
     */
    FoodSource(Integer y, Integer x) {
        this.yPos = y;
        this.xPos = x;
        Random random_seed = new Random();
        /*
          we use a log function to set this minimal value for nextInt.
          The logic is to ensure the growth of the minimal value is logarithmic proportional
          to that of the number of cells in the grid, that is GUI.DIMENSION.
         */
        minimal = (int) Math.max(Math.floor(Math.sqrt(3 * GUI.DIMENSION)), 1);   // minimal number of quantity of food in this source
        maximal = minimal * 4;
        amountLeft = (double) random_seed.nextInt(minimal, maximal);
    }

    Color getColor() {
        return color;
    }

    /**
     * called by an ant interacting with this food's source.
     * Returns to the ant the minimum quantity of resources between the one requested and the maximum currently available
     * @param amount the amount of food requested
     * @return the amount of food currently available in this source, if you request too much simply returns zero
     */
    double gathering(double amount) {
        double available = Math.min(amount, amountLeft);
        amountLeft -= available;
        return available;
    }

    /**
     * called by avery ant when it realizes it got too much food from this food's source
     * @param amount the amount of food to put back to the source
     */
    void reverseGathering(double amount) {
        amountLeft += amount;
    }
}