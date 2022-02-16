package Ants;

import UI.GUI;

import java.awt.*;

public class AntsNest {

    /**
     * the color for the entrance of the ant's nest
     */
    public final Color color = new Color(30, 30, 30);

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

    AntsNest() {
        nestEntrance1 = AntSimulator.key(Math.floorDiv(GUI.HEIGHT, 2), Math.floorDiv(GUI.WIDTH, 2)); // the top left square in the centre of the grid
        nestEntrance2 = nestEntrance1 + 1;  // the square on the right has exactly the next value
        nestEntrance3 = nestEntrance1 + GUI.WIDTH; // the square on the left bottom corner, just add the width of the grid
        nestEntrance4 = nestEntrance3 + 1;
    }

    public Color getColor() {
        return color;
    }

    Integer getNestEntrance1() {return nestEntrance1;}
    Integer getNestEntrance2() {return nestEntrance2;}
    Integer getNestEntrance3() {return nestEntrance3;}
    Integer getNestEntrance4() {return nestEntrance4;}

    boolean inNest(Integer key) {
        return (key.equals(nestEntrance1) || key.equals(nestEntrance2) || key.equals(nestEntrance3) || key.equals(nestEntrance4));
    }
}
