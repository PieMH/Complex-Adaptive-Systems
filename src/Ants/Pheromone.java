package Ants;

import UI.GUI;

import java.awt.*;

/**
 * There are two types of Pheromones. The first one is the Personal one.
 * The personal pheromone is personal and unique to every ant, it is used when two ants meet.
 * It is not added to AntSimulator.currentTrailPheromone hence is not a trail pheromone.
 * The game knows its position because it is the one associated with the ant carrying it.
 * In fact if it is a personal pheromone this.yPos and this.xPos are useless, see Ant.xPos and Ant.yPos instead.
 * <p>
 * The other type is the Trail one. Trail pheromones are very popular and widely used by ant to leave a trail of their passage in the natura.
 * They are secreted whenever an ant finds a FoodSource (or even an AntsNest ??) to let other ants know the trail to find the food' source.
 * They are added to AntSimulator.currentTrailPheromone whenever they are created and stay always in the same place.
 * After some type they vanish in the environment and no ant can no longer follow its scent.
 * The decay of Pheromone trails is control by AntsSimulator, every turn a trail lose value = 1 of its strength until it vanish into the air, and it's erased from the hashMap.
 */
public class Pheromone {

    /**
     * the color for every food source
     */
    public final Color color = new Color(0, 90, 110);

    /**
     * the row number in the grid of this Food Source
     */
    Integer yPos;

    /**
     * the column number in the grid of this Food Source
     */
    Integer xPos;

    /**
     *
     */
    enum pheType {Trail, Personal}   // only Trail and Personal are used atm (18/02/22)

    /**
     *
     */
    final pheType type;

    /**
     *
     */
    private Integer strength;

    /**
     * the ant who secreted this pheromone
     */
    Ant ant;

    /**
     * maximum strength value for a pheromone trail, it depends on UI.GUI.DIMENSION
     * It is final but can't say final because it must change between different Interfaces.Game instances
     * It is used by {@code Ants.Ant(), Ants.Ant.pheromoneInteraction() and UI.GUI.paintPheromones(Graphics g)}
     */
    public static int maxStrength;

    Pheromone(Integer y, Integer x, Ant ant) {
        this(y, x, ant, pheType.Personal, 100);
    }

    Pheromone(Integer y, Integer x, Ant ant, pheType type, Integer strength) {
        this.yPos = y;
        this.xPos = x;
        this.ant = ant;
        this.type = type;
        this.strength = strength;
    }

    public static void setMaxStrength(Integer strength) {
        maxStrength = strength;
    }

    public Color getColor() {
        return color;
    }

    void decay() {
        strength -= 1;
        if (strength < 1) {
            AntSimulator.erasePheromone(this);
        }
    }

    public Integer getStrength() {
        return strength;
    }
}