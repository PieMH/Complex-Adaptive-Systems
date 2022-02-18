package Ants;

import java.awt.*;

/**
 * There are two types of Pheromones. The first one is the Personal one.
 * The personal pheromone is personal and unique to every ant, it is used when two ants meet.
 * It is not added to AntSimulator.currentTrailPheromone hence is not a trail pheromone.
 * The game knows its position because it is the one associated with the ant carying it.
 * In fact if it is a personal pheromone this.yPos and this.xPos are useless, see Ant.xPos and Ant.yPos instead
 * <p>
 * The other type is the Trail one. Trail pheromones are very popular and widely used by ant to leave a trail of their passage in the natura.
 * They are secerned whenever an ant finds a FoodSource (or even an AntsNest ??) to let other ants know the trail to find the food' source.
 * They are added to AntSimulator.currentTrailPheromone whenever they are created and stay always in the same place.
 * After some type they vanish in the environment and no ant can no longer follow its scent.
 * The decay of Pheromone trails is control by AntsSimulator, every turn a trail lose value = 1 of its strenght until it vanish into the air, and it's erased from the hashMap.
 */
public class Pheromone {

    /**
     * the color for every food source
     */
    public final Color color = new Color(10, 80, 90);

    /**
     * the row number in the grid of this Food Source
     */
    Integer yPos;

    /**
     * the column number in the grid of this Food Source
     */
    Integer xPos;

    private enum phrmnType {Trail, Personal}   // only Trail and Personal are used atm (18/02/22)

    final phrmnType type;

    private Integer strenght;

    Pheromone(Integer y, Integer x) {
        this(y, x, phrmnType.Personal, 100);
    }

    Pheromone(Integer y, Integer x, phrmnType type, Integer strenght) {
        this.yPos = y;
        this.xPos = x;
        this.type = type;
        this.strenght = strenght;
    }

    Color getColor() {
        return color;
    }

    void decay() {
        strenght -= 1;
        if (strenght < 1) {
            AntSimulator.erasePheromone(this);
        }
    }
}