package Ants;

import UI.GUI;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 */
public class Ant {

    /**
     * total days of life expectancy of an ant
     */
    public Integer life;

    /**
     * the hunger level of the ant, if it reaches zero it dies
     */
    Integer hunger;

    /**
     * the first stomach of an ant. The food carried in the first stomach it is used to share it with other ants;
     * or it is passed to the second private stomach for foraging the single ant
     */
    Integer sharedStomach;

    /**
     * the second private stomach of an ant, the food stored in this stomach serves only to sustain its ant
     * If this is empty the ant will starve
     */
    private Integer privateStomach;

    /**
     * the color used by UI.GUI to paint the GUI.innerPanel correctly
     */
    public final Color color = new Color(90, 90, 90);

    /**
     * the column number in the grid of this ant
     */
    private Integer xPos;

    /**
     * the row number on the grid of this ant
     */
    private Integer yPos;

    /**
     * A nearby x position.
     * <b>DO NOT CHANGE THIS DIRECTLY</b>.
     * <p></p>
     * <b><u>The method {@code translateDirInPos()} is the only one who can modify this</u></b>
     * @see #translateDirInPos
     */
    private Integer nextX;

    /**
     * A nearby y position.
     * <b>DO NOT CHANGE THIS DIRECTLY</b>.
     * <p></p>
     * <b><u>The method {@code translateDirInPos()} is the only one who can modify this</u></b>
     * @see #translateDirInPos
     */
    private Integer nextY;

    /**
     * random seed for stochastic actions
     */
    private Random random_seed;

    /**
     * the 8 cardinal directions
     */
    private enum Direction {N, NE, E, SE, S, SO, O, NO}

    /**
     * the chosen direction to follow
     * If no major events occur (other ant meeting, pheromone sniffing)
     * then every n random turns the ants changes direction randomly.
     * @see #movement
     */
    private Direction chosenDir;

    /**
     * the counter for the change of the direction. When it reaches "changeDirection" triggers findRandomDirection
     * @see #movement
     */
    private Integer countDir;

    /**
     * the max number of turns before an ant following a path changes mind and changes direction
     * @see #movement
     */
    private final Integer changeDirection;

    /**
     * a simple flag use by movement() to know if the path the ant is following it was chosen randomly or not
     * @see #movement
     */
    private boolean onARandomPath;

    /**
     * a flag to let move() know whether to leave a trail of pheromones after encountering a food's source or the nest.
     * @see #move
     */
    private Integer leaveTrail;

    /**
     *
     */
    private final Integer maxLeaveTrail;

    /**
     * the strength of a new Trail type of Pheromone release
     */
    private final Integer strengthOfNewTrailPheromone;

    /**
     * the nest of this ant
     */
    private final AntsNest nest;

    final Pheromone personalPheromone;

    /**
     * This constructor is called only by AntSimulator at the start of the simulation
     * For newborn ants ?? is called instead.
     * @param y the column number in the grid
     * @param x the row number in the grid
     */
    Ant (Integer y, Integer x, AntsNest nest) {
        yPos = y;
        xPos = x;

        random_seed = new Random();
        int minChange = (int) Math.max(Math.floor(Math.log(GUI.DIMENSION / 5.0)) - 1, 2);
        int maxChange = (int) Math.ceil(minChange * Math.sqrt(minChange)) + 1;
        changeDirection = random_seed.nextInt(minChange, maxChange);

        int minTrail = 2 * minChange;
        int maxTrail = (int) Math.ceil(minTrail * 3.0 / 2.0 * minTrail);
        maxLeaveTrail = random_seed.nextInt(minTrail, maxTrail);
        leaveTrail = 0;

        strengthOfNewTrailPheromone = random_seed.nextInt(minTrail * 2, Math.min(100, maxTrail * 2));

        countDir = changeDirection;
        onARandomPath = true;
        this.life = 100;
        this.nest = nest;
        this.personalPheromone = new Pheromone(yPos, xPos, this);
    }

    /**
     * called by AntSimulator to activate an Ant.
     * The possible actions are:
     * <ol>
     *     <li>search the 8 squares around you</li>
     *     <li>interact with something around you</li>
     *     <li>decide if you want to follow something you found</li>
     *     <li>follow your trail</li>
     *     <li>after some time if you are on a random trail change it randomly>
     * </ol>
     */
    void action() {
        search(true, false);

//        decide();

        movement();
    }

    /**
     * Searches in the 8 nearby position for something to interact with or just for an empty space to go to.
     * This depends upon the value of two parameters. <b>NEVER CALL THIS METHOD WITH BOTH OF THEM AT TRUE</b>.
     * It would break the little mind of the ant. If you call this method with both parameters at false it is simply useless.
     * @param something true if you want to find something around you
     * @param newDirection true if you want to search for a new random direction to follow
     * @see #interact
     * @see #findRandomDirection
     */
    void search(boolean something, boolean newDirection) {
        random_seed = new Random();
        ArrayList<Direction> directionList = new ArrayList<>(List.of(Direction.values()));
        int index;
        for (int i = 0; i < 8; i++) {
            index = random_seed.nextInt(directionList.size());
            Direction dir = directionList.remove(index);
            if (something) {
                translateDirInPos(dir);     // updates nextY and nextX
                interact(nextY, nextX);
            }
            else if (newDirection) {
                if (findRandomDirection(dir)) {
                    return;
                }
            }
        }
    }

    /**
     * the complete algorithm who controls the movement of an ant
     */
    void movement() {
        if (onARandomPath) {
            countDir += 1;
        }

        // follow your path
        if (countDir < changeDirection) {
            translateDirInPos(chosenDir);    // updates nextY and nextX
            if (move(nextY, nextX)) return;
        }

        // otherwise, search a random path
        search(false, true);

        move(nextY, nextX);
    }

    /**
     *
     * @param y nextY
     * @param x nextX
     * @param <E> a generic element. Can be ONLY an Ant (different from this), a FoodSource, an AntsNest or a Pheromone.
     */
    <E> void interact(Integer y, Integer x) {
        E element = whoIsThere(y, x);   // called on nextY and nextX

        if (element == null) return;    // it there is no one

        if (element.getClass() == Ant.class) {
            antInteraction((Ant) element);
        }
        else if (element.getClass() == AntsNest.class) {
            nestInteraction((AntsNest) element);
        }
        else if (element.getClass() == FoodSource.class) {
            foodInteraction((FoodSource) element);
        }
        else if (element.getClass() == Pheromone.class) {
            pheromoneInteraction((Pheromone) element);
        }

//        chooseWhatToDo(o);  // update chosenDir // update countDir  // update onARandomPath
    }

    void antInteraction(Ant otherAnt) {
        // do action relative to meeting a new ant or a previously known ant
        // mating and pheromones sniffing
    }

    void nestInteraction(AntsNest nest) {
        // you have found your nest
        // do action relative to the nest encounter
        leaveTrail = maxLeaveTrail;
        // deposit eggs ??
        // deposit food or eat its reserves ??
    }

    void foodInteraction(FoodSource food) {
        // if you have already eaten the maximum food you can, AND you are carrying the maximum food you can ignore it
        // otherwise:
        boolean gatheredFood = food.gatherFood();
        if (gatheredFood) {
            // do action relative to gathering food and discovering a new food source
//          gatherFood(gatheredFood);
            leaveTrail = maxLeaveTrail;
        }
        else AntSimulator.foodFinished(food);
    }

    void pheromoneInteraction(Pheromone phe) {
        // you came across a pheromone trail
        // if it is yours ignore it
        if (phe.ant == this) {
            if (Math.random() < 0.1) { // with a P of < 0.1
                // follow it
            }
            return;
        }
        // else choose to follow it or not and in what direction
    }

    /**
     * Simply moves the ant to the positions given by parameters. They must be nearby but this is delegated to search(), movement() and translateDirInPos().
     * If the position given is not free then changes countDir to trigger a recalculation of the path to follow by the ant
     * @param y the column number in the grid
     * @param x the row number in the grid
     * @see #search
     * @see #movement
     * @see #translateDirInPos
     */
    private boolean move(Integer y, Integer x) {
        if (whoIsThere(y, x) == null && inBounds(y, x) && (!Objects.equals(yPos, y) || !Objects.equals(xPos, x))) {
            if (leaveTrail > 1) {
                AntSimulator.addPheromone(new Pheromone(yPos, xPos, this, Pheromone.pheType.Trail, strengthOfNewTrailPheromone));
                leaveTrail -= 1;
            }
            this.yPos = y;
            this.xPos = x;
            return true;
        }
        else countDir = changeDirection;
        return false;
    }

    /**
     * decide whether the direction given as a parameter can be a new direction to follow: it must be free.
     * @return true if it finds a new direction, false otherwise
     */
    private boolean findRandomDirection(Direction dir) {
        translateDirInPos(dir);     // updates nextY and nextX

        if (whoIsThere(nextY, nextX) == null && inBounds(nextY, nextX) && (!Objects.equals(yPos, nextY) || !Objects.equals(xPos, nextX))) {
            chosenDir = dir;
            countDir = 0;
            onARandomPath = true;
            return true;
        }
        return false;
    }

    /**
     * simply translate a cardinal direction in a pair of coordinates relative to that of this ant.
     * The translation is saved on the local attributes of this class called nextX and nextY,
     * they are the nearby position on the grid relative to this.xPos and this.yPos, translation from a Direction
     * @param direction the cardinal direction to translate
     */
    private void translateDirInPos(Direction direction) {
        nextX = this.xPos;
        nextY = this.yPos;
        switch (direction) {
            case N -> // Nord
                nextY -= 1;
            case NE -> {     // NordEst
                nextY -= 1;
                nextX += 1;
            }
            case E -> // Est
                nextX += 1;
            case SE -> {     // SudEst
                nextY += 1;
                nextX += 1;
            }
            case S -> // Sud
                nextY += 1;
            case SO -> {     // SudOvest
                nextY += 1;
                nextX -= 1;
            }
            case O -> // Ovest
                nextX -= 1;
            case NO -> {     // NordOvest
                nextY -= 1;
                nextX -= 1;
            }
        }
    }

    /**
     *
     * @param xPos the column number
     * @param yPos the row number
     * @return ??
     */
    private <E> E whoIsThere(Integer yPos, Integer xPos) {

        // another living ant
        Ant otherAnt = AntSimulator.getCurrentAlive().get(AntSimulator.key(yPos, xPos));
        if (otherAnt != null && otherAnt.getLife() > 0) return (E) otherAnt;

        // your nest
        if (nest.inNest(AntSimulator.key(yPos, xPos))) return (E) nest;

        // a food's source
        FoodSource food = AntSimulator.getCurrentFood().get(AntSimulator.key(yPos, xPos));
        if (food != null && food.getAmountLeft() > 0) return (E) food;

        return null;
    }

    /**
     * to tell if the position given as a parameter is actually out of the bounds of the grid
     * @param xPos the column number
     * @param yPos the row number
     * @return false if it is out of the bounds, true otherwise
     */
    private boolean inBounds(Integer yPos, Integer xPos) {
        return (xPos >= 0 && yPos >= 0 && xPos < GUI.WIDTH && yPos < GUI.HEIGHT);
    }

    /**
     * kills an ant, mainly called by {@code AntSimulator}
     */
    void die() {
        life = 0;
    }  // maybe a call to the Java Garbage Collector

    /**
     * decrease the value of life of the ant, simulates aging
     */
    void age() {
        life -= 1;
    }

//    boolean eat()

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