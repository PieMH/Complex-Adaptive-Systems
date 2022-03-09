package Ants;

import java.awt.*;

/**
 * There are many types of Pheromones in a real world ant colony.
 * Giving the complexity of modelling every possible pheromone we decided to model only one type and the most used one the Trail Pheromone
 * <p>
 * Trail pheromones are very popular and widely used by ant to leave a trail of their passage in the natura.
 * They are secreted whenever an ant finds a FoodSource to let other ants know the trail to find the food's source.
 * They are added to AntSimulator.currentTrailPheromone whenever they are created and stay always in the same place.
 * After some type they vanish in the environment and no ant can no longer follow its scent.
 * The decay of Pheromone trails is control by AntsSimulator, every turn a trail lose value = 1 of its strength until it vanish into the air, and it's erased from the hashMap.
 * @see AntSimulator
 * @see AntsNest
 * @see FoodSource
 * @see Ant
 */
public class Pheromone {

    /**
     * the color for every food source
     */
    public final Color color = new Color(0, 50, 120);

    /**
     * the row number in the grid of this Food Source
     */
    Integer yPos;

    /**
     * the column number in the grid of this Food Source
     */
    Integer xPos;

    /**
     * all possible types of pheromones
     */
    enum pheType {Trail}   // only one type currently modeled but the enum is left for scalability reasons

    /**
     * the type of pheromone secreted
     */
    final pheType type;

    /**
     * the strength of this pheromone, min 0, max maxStrength which AntSimulator is responsible for deciding its value
     */
    private Integer strength;

    /**
     * the ant who secreted this pheromone
     */
    Ant ant;

    /**
     * maximum strength value for a pheromone trail, it depends on UI.GUI.DIMENSION
     * It is final but can't say final because it must change between different Interfaces.CASModel instances
     * It is used by {@code Ants.Ant(), Ants.Ant.pheromoneInteraction() and UI.GUI.paintPheromones(Graphics g)}
     */
    public static int maxStrength;

    /**
     * creates a new pheromone in the pos (y, x)
     * @param y column index
     * @param x row index
     * @param ant the ant who secreted this pheromone
     * @param type the type of this pheromone
     * @param strength the initial strength
     */
    Pheromone(Integer y, Integer x, Ant ant, pheType type, Integer strength) {
        this.yPos = y;
        this.xPos = x;
        this.ant = ant;
        this.type = type;
        this.strength = strength;
    }

    /**
     * called by AntSimulator.resetMap
     * @param strength the starting strength of this pheromone
     */
    public static void setMaxStrength(Integer strength) {
        maxStrength = strength;
    }

    /**
     * decrease the strength of this pheromone by one, if it reaches zero then will be eliminated from the environment
     */
    void decay() {
        strength -= 1;
        if (strength < 1) {
            AntSimulator.erasePheromone(this);
        }
    }

    public Integer getStrength() {
        return strength;
    }

    public Color getColor() {
        return color;
    }
}