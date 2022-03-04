package Ants;

import UI.GUI;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The class who sets the laws under which every ant acts upon.
 * <p>
 * AntSimulator calls action() every turn, and it delegates every ant related law and logic to the current class.
 * Key methods are search(), whoIsThere(), interact(), movement() and translateDirInPos().
 * @see AntSimulator
 * @see AntsNest
 * @see FoodSource
 * @see Pheromone
 */
public class Ant {

    /**
     * total days of life expectancy of an ant
     */
    public Double life;

    /**
     * increases its value when hunger = 0 every turn,
     * turns to 0 if hunger > 0
     */
    private Double starvingMultiplier;

    /**
     * how much food you'll eat every day from your stomachs
     */
    private final Double foodToEatEveryDay;

    /**
     * how fast you transfer the food from your shared stomach to the private one
     */
    private final Double transferringSpeed;

    /**
     * the first stomach of an ant. The food carried in the first stomach it is used to share it with other ants;
     * or it is passed to the second private stomach for foraging the single ant
     */
    Double sharedStomach;

    /**
     * the second private stomach of an ant. The food stored in this stomach serves only to sustain its ant
     * If this is empty the ant will starve
     */
    private Double privateStomach;

    /**
     * simply the sum of the food inside both of the stomachs
     */
    private Double stomachSum;

    /**
     * my maximum capacity of food quantities I can carry with me.
     * This maximum is the same for both of my stomachs.
     */
    private final Integer maxStomachCapacity;

    /**
     * the color used by UI.GUI to paint the GUI.innerPanel correctly
     */
    public final Color color = new Color(0, 93, 0);

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
     * <p><b><i>THE MAIN STATE OF THE ANT</i></b></p>
     * This is used to orchestrate the whole logic of an ant.
     * Basically if true the ant will base its choice upon going fast as possible back to the nest,
     * if false the ant wants to explore the world.
     */
    private boolean toTheNest;

    /**
     * a flag to let move() know whether to leave a trail of pheromones after encountering a food's source or the nest.
     * @see #move
     */
    private Integer leaveTrail;

    /**
     * the maximum value of consecutive days an ant can leave pheromone on its path
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

    /**
     * to avoid interacting with the nest two times a day
     */
    private boolean nestAlreadyEncountered = false;

    /**
     * the y position on the grid of the first entrance of the nest
     */
    private final int nestY;

    /**
     * the x position on the grid of the first entrance of the nest
     */
    private final int nestX;

    /**
     * the array responsible for keeping the distances of the 8 squares around you sorted depending on the distances from them to the nest
     */
    private final ArrayList<Double> closestNestDistances;

    /**
     * the support array to closestNestDistances specifying the actual directions in order
     */
    private final ArrayList<Direction> nestDirections;

    /**
     * the maximum distance an ant can be from the nest
     */
    private static double dMax;

    /**
     * the minimum values of consecutive days an ant can be roaming on the external ambient of the model
     */
    private final int minRoaming;

    /**
     * counter for minRoaming
     */
    private int roaming;

    /**
     * seph stands for Lightest Encountered PHeromone
     * every day it returns to null but get updated on the pheromones an ant sniff around it
     */
    private Pheromone leph;

    /**
     * keep tracking the amount of pheromones around you, if too many the ant will ignore them
     */
    private int pheromoneCounter;

    /**
     * how many of the 8 traits, that make up an ant genetic code, an ant can transmit to its children
     */
    private final Integer nTraitsToTransmit;

    /**
     * the DNA of an ant
     */
    ArrayList<Double> antAttributes = new ArrayList<>(8);

    /**
     * This constructor is called only by AntSimulator at the start of the simulation or when you click or click and drag the mouse on the screen.
     * It creates an ant with random values at his attributes.
     * For newborn ants the next constructor is called instead.
     * @param y the column number in the grid
     * @param x the row number in the grid
     */
    Ant (Integer y, Integer x, AntsNest nest) {
        yPos = y;
        xPos = x;
        onARandomPath = true;
        toTheNest = false;
        this.life = 100.0;
        this.nest = nest;
        random_seed = new Random();

        // changeDirection random value extraction
        int minChange = (int) Math.max(2, Math.floor(Math.log(GUI.DIMENSION / 5.0)) - 1);
        int maxChange = (int) Math.ceil(minChange * Math.sqrt(minChange)) + 1;
        changeDirection = random_seed.nextInt(minChange, maxChange);
        countDir = 0;

        // maxLeaveTrail random value extraction
        maxLeaveTrail = random_seed.nextInt(maxChange + 2, 2 * (maxChange + 2));
        leaveTrail = 0;

        // strengthOfNewTrailPheromone random value extraction
        int minStrength = 3 * minChange;
        // for Pheromone.maxStrength calculation see AntSimulator.resetMap()
        strengthOfNewTrailPheromone = random_seed.nextInt(minStrength, Pheromone.maxStrength);

        // Stomachs capacities
        int minF = (int) Math.floor(minChange * 1.8);
        int maxF = (int) Math.floor(2.4 * Math.max(1, Math.pow(GUI.DIMENSION, 1 / 4.0) - 1 ) );
        maxStomachCapacity = random_seed.nextInt(minF, maxF);
        sharedStomach = maxStomachCapacity * 0.99;
        privateStomach = 0.0;
        stomachSum = sharedStomach + privateStomach;

        // food related attributes
        foodToEatEveryDay = Math.max(0.1, Math.random() * 1.2) / 4;
        starvingMultiplier = 1.0;
        transferringSpeed = Math.max(0.06, Math.random() * 1.1) / 2.8;

        // nest related attributes
        nestY = AntSimulator.coordinates(nest.nestEntrance1, 0);
        nestX = AntSimulator.coordinates(nest.nestEntrance1, 1);
        closestNestDistances = new ArrayList<>(8);
        nestDirections = new ArrayList<>(8);
        for (int i = 0; i < 8; i++) {
            closestNestDistances.add((double) GUI.WIDTH);
            nestDirections.add(null);
        }
        minRoaming = (int) random_seed.nextDouble(life * 0.3, life * 0.5);
        roaming = 0;

        // reproduction related attributes
        nTraitsToTransmit = random_seed.nextInt(1, 9);
        antAttributes.add(0, (double) nTraitsToTransmit);
        antAttributes.add(1, (double) changeDirection);
        antAttributes.add(2, (double) maxLeaveTrail);
        antAttributes.add(3, (double) strengthOfNewTrailPheromone);
        antAttributes.add(4, (double) maxStomachCapacity);
        antAttributes.add(5, foodToEatEveryDay);
        antAttributes.add(6, transferringSpeed);
        antAttributes.add(7, (double) minRoaming);
    }

     /**
     * This constructor is called only by AntSNest to generate an ant with the 8 main attributes given by the nest
     * @param y the column number in the grid
     * @param x the row number in the grid
     * @param nest an instance of your nest
     * @param attributes the array containing the DNA of you father and of the whole colony
     */
    Ant (Integer y, Integer x, AntsNest nest, ArrayList<Double> attributes) {
        yPos = y;
        xPos = x;
        onARandomPath = true;
        toTheNest = false;
        this.life = 100.0;
        this.nest = nest;
        random_seed = new Random();

        // changeDirection random value extraction
        changeDirection = (int) Math.floor(attributes.get(1));
        countDir = 0;

        // maxLeaveTrail random value extraction
        maxLeaveTrail = (int) Math.floor(attributes.get(2));
        leaveTrail = 0;

        // strengthOfNewTrailPheromone random value extraction
        strengthOfNewTrailPheromone = (int) Math.floor(attributes.get(3));

        // Stomachs capacities
        maxStomachCapacity = (int) Math.floor(attributes.get(4));
        sharedStomach = maxStomachCapacity * 0.99;
        privateStomach = 0.0;
        stomachSum = sharedStomach + privateStomach;

        // food related attributes
        foodToEatEveryDay = attributes.get(5);
        starvingMultiplier = 1.0;
        transferringSpeed = attributes.get(6);

        // nest related attributes
        nestY = AntSimulator.coordinates(nest.nestEntrance1, 0);
        nestX = AntSimulator.coordinates(nest.nestEntrance1, 1);
        closestNestDistances = new ArrayList<>(3);
        nestDirections = new ArrayList<>(3);
        for (int i = 0; i < 8; i++) {
            closestNestDistances.add((double) GUI.WIDTH);
            nestDirections.add(null);
        }
        minRoaming = (int) Math.floor(attributes.get(7));
        roaming = 0;

        // reproduction related attributes
        nTraitsToTransmit = (int) Math.floor(attributes.get(0));
        antAttributes.add(0, (double) nTraitsToTransmit);
        antAttributes.add(1, (double) changeDirection);
        antAttributes.add(2, (double) maxLeaveTrail);
        antAttributes.add(3, (double) strengthOfNewTrailPheromone);
        antAttributes.add(4, (double) maxStomachCapacity);
        antAttributes.add(5, foodToEatEveryDay);
        antAttributes.add(6, transferringSpeed);
        antAttributes.add(7, (double) minRoaming);
    }

    /**
     * called by AntSimulator to activate an Ant.
     * The possible actions are:
     * <ol>
     *     <li>transfer the food you have in the shared stomach to the private one</li>
     *     <li>eat the food from your private stomach</li>
     *     <li>orient yourself to know where is you nest</li>
     *     <li>search the 8 squares around you</li>
     *     <li>interact with something around you</li>
     *     <li>decide what you want to do</li>
     *     <li>try to move in the direction you have chosen</li>
     *     <li>get older</li>
     * </ol>
     */
    void action() {

        leph = null;
        pheromoneCounter = 0;
        roaming += 1;
        nestAlreadyEncountered = false;
        stomachSum = sharedStomach + privateStomach;

        transferFood();

        eat();

        orientYourself();

        search(true, false);

        decide();

        searchFood();

        movement();

        age();
    }

    /**
     * Searches in the 8 nearby position for interact to interact with or just for an empty space to go to.
     * This depends upon the value of two parameters. <b>NEVER CALL THIS METHOD WITH BOTH OF THEM AT TRUE</b>.
     * It would break the little mind of the ant. If you call this method with both parameters at false it is simply useless.
     * @param interact true if you want to interact with something around you
     * @param nest true if you want to recompute the closest directions to follow to reach the nest
     * @see #interact
     * @see #computeNestDistance
     */
    void search(boolean interact, boolean nest) {
        random_seed = new Random();
        ArrayList<Direction> directionList = new ArrayList<>(List.of(Direction.values()));
        int index;
        for (int i = 0; i < 8; i++) {
            index = random_seed.nextInt(directionList.size());
            Direction dir = directionList.remove(index);
            if (interact) {
                translateDirInPos(dir);     // updates nextY and nextX
                interact(nextY, nextX);
            }
            else if (nest) {
                translateDirInPos(dir);
                computeNestDistance(nextY, nextX);
            }
        }
    }

    /**
     * little method that aims to enhance the smell sense of an ant.
     * With this an ant can smell food up to 3 squares from it, basically in a 7x7 square around it, in all 8 directions.
     */
    void searchFood() {
        if ((sharedStomach + privateStomach) < (maxStomachCapacity * 1.5)) {    // search food only if you are not already full
            random_seed = new Random();
            ArrayList<Direction> directionList = new ArrayList<>(List.of(Direction.values()));
            int index;
            for (int i = 0; i < 8; i++) {
                index = random_seed.nextInt(directionList.size());
                Direction dir = directionList.remove(index);
                translateDirInPos(dir);     // updates nextY and nextX
                int deltaY = nextY - yPos;
                int deltaX = nextX - xPos;
                boolean found = false;
                // first try at distance 2
                FoodSource food = AntSimulator.getCurrentFood().get(AntSimulator.key(nextY + deltaY, nextX + deltaX));
                if (food != null && notOpposite(dir)) {
                    chosenDir = dir;
                    found = true;
                }
                // distance 3 only if not going to the nest
                if (!found && !toTheNest && notOpposite(dir)) {
                    food = AntSimulator.getCurrentFood().get(AntSimulator.key(nextY + (2 * deltaY), nextX + (2 * deltaX)));
                    if (food != null) {
                        chosenDir = dir;
                        found = true;
                    }
                }
                if (found) break;
            }
        }
    }

    /**
     * the algorithm that controls the movement of an ant
     */
    void movement() {
        if (onARandomPath) {
            countDir += 1;
        }

        // follow your path
        if (countDir < changeDirection && chosenDir != null) {
            translateDirInPos(chosenDir);    // updates nextY and nextX
            if (move(nextY, nextX)) {
                return;
            }
        }

        // otherwise, search a random path
        findRandomDirection();

        move(nextY, nextX);
    }

    /**
     * calls one of the method listed in "see also" depending on the object that is occupying the position (y, x).
     * @param y nextY
     * @param x nextX
     * @param <E> a generic element. Can ONLY be an Ant (different from this), a FoodSource, an AntsNest or a Pheromone.
     * @see #antInteraction
     * @see #nestInteraction
     * @see #pheromoneInteraction
     * @see #foodInteraction
     */
    <E> void interact(Integer y, Integer x) {
        E element = whoIsThere(y, x);   // called on nextY and nextX

        if (element == null) return;    // if there is no one

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
    }

    /**
     * trigger trophallaxis if conditions are met
     * @param otherAnt another ant you encountered
     */
    void antInteraction(Ant otherAnt) {

        // Trophallaxis
        double myDelta = this.maxStomachCapacity - this.sharedStomach;     // how much our stomach is empty
        double yourDelta = otherAnt.maxStomachCapacity - otherAnt.sharedStomach;
        if ((myDelta > yourDelta + 1) && (otherAnt.sharedStomach > 1)) {    // mine in emptier, so you shall give some food to me
            if (Math.random() < ((myDelta - yourDelta) / (double) this.maxStomachCapacity )) {      // with a P of the difference of our deltas / mine maximum capacity (which is always greater than the difference)
                this.sharedStomach += 1;
                otherAnt.sharedStomach -= 1;
            }
        }
        else if ((yourDelta > myDelta + 1) && (this.sharedStomach > 1)) {
            if (Math.random() < ((yourDelta - myDelta) / (double) otherAnt.maxStomachCapacity)) {
                this.sharedStomach -= 1;
                otherAnt.sharedStomach += 1;
            }
        }
    }

    /**
     * if you have plenty of food give some back to the nest, then lay two eggs with some of your genetics
     * if you don't have enough food even for yourself get some from the nest
     * either way toggle the key state "toTheNest"
     * @param nest your nest
     */
    void nestInteraction(AntsNest nest) {
        if (nestAlreadyEncountered) return;
        nestAlreadyEncountered = true;

        // you have found your nest
        // do action relative to the nest encounter
        toTheNest = false;  // change state and objective
        countDir = changeDirection; // change direction
        roaming = 0;

        // deposit food or eat its reserves
        // deposit an amount equal to a random value between this.sharedStomach - 1 and 0, with a continuous random variable that has exponential distribution
        double threshold = maxStomachCapacity * 0.9;
        if (stomachSum > threshold) {    // give

            double howMuch = (stomachSum - threshold);
            nest.addReserves(howMuch);
            sharedStomach -= howMuch;

            // deposit eggs
            if (life < 90 && life > 10) {   // if not too young and not too old
                nest.transmitGenetics(antAttributes);
                nest.triggerReproduction();
            }
        }
        else {  // take
            double howMuch = (threshold - stomachSum);
            if (nest.getFoodFromReserves(howMuch)) {
                sharedStomach += howMuch;
            }
        }
    }

    /**
     * get some food from the source you found, then start to release pheromone on your way from here
     * @param food the food's source found
     */
    void foodInteraction(FoodSource food) {
        // if you have already eaten the maximum food you can, AND you are carrying the maximum food you can ignore it
        // otherwise:
        double amount = maxStomachCapacity * 0.8;
        double available = food.gathering(amount);
        if (available > 0) {
            // do action relative to gathering food and discovering a new food source
            double left = gatherFood(available);
            food.reverseGathering(left);
            leaveTrail = maxLeaveTrail;
        }
        else AntSimulator.foodFinished(food);
    }

    /**
     * see if the pheromone found is the less strong around you (hopefully it leads to a food's source),
     * but keep this information depending on "toTheNest"
     * @param phe the trail pheromone found
     */
    void pheromoneInteraction(Pheromone phe) {
        // you came across a pheromone trail
        if (phe.ant != this) {  // if it is yours ignore it
            pheromoneCounter += 1;
            Direction possibleDir = translatePosInDir(phe.yPos, phe.xPos);
            if (notOpposite(possibleDir)) {   // if it is from the opposite direction ignore it

                if (toTheNest) {
                    for (int i = 0; i < 3; i++) {   // go through the 3 directions closest to the nest
                        if (nestDirections.get(i) == possibleDir) {    // I am going to the nest
                            if (leph != null) {
                                if (phe.getStrength() < leph.getStrength()) {
                                    leph = phe;
                                }
                            } else leph = phe;
                            return;
                        }
                    }
                }
                else {  // I am going away from the nest
                    for (int i = 0; i < 3; i++) {   // go through the 3 directions closest to the nest
                        if (nestDirections.get(i) == possibleDir) {
                            return;
                        }
                    }
                    if (leph != null) {
                        if (phe.getStrength() < leph.getStrength()) {
                            leph = phe;
                        }
                    } else leph = phe;
                }
            }
        }
    }

    /**
     * initialize the two array responsible for keeping the 8 position around you sorted from the closer to the farthest to the nest
     * then calls computeNestDistance
     * @see #computeNestDistance
     */
    private void orientYourself() {
        // reinitialize the data structures for searching the nest
        for (int i = 0; i < 8; i++) {
            closestNestDistances.set(i, (double) GUI.WIDTH);
            nestDirections.set(i, null);
        }

        search(false, true);     // this updates nestDirections hashmap
    }

    /**
     *  mainly decide the direction to follow based on the information gathered until now
     */
    private <E> void decide() {
        // First key AI logic of an ant:
            // if I am starving or have little food go to you nest to feed
            // at the opposite if I have plenty of food with me, it is useless to continue finding food in the ambient so come back to the nest
            // if I have just enough quantity of food to go exploring then I will prefer to go outside and explore the ambient for food
        boolean found = false;
        // remember that maxStomachCapacity it's the maximum capacity of a single stomach
        // if I decided time ago to go to the nest don't change that decision until you come to the nest
        if (roaming > minRoaming && (toTheNest || ((stomachSum < maxStomachCapacity * 0.4)  || (stomachSum > maxStomachCapacity * 1.2)))) {    // little or much food condition
            double p = random_seed.nextDouble();
            int start;
            if (p < 0.6) start = 0;
            else if (p < 0.9) start = 1;
            else start = 2;
            for (int i = start; i < start + 3; i++) {
                if (!found) {
                    translateDirInPos(nestDirections.get(i % 3));
                    E el = whoIsThere(nextY, nextX);
                    if (el == null || el.getClass() == Pheromone.class) {
                        chosenDir = nestDirections.get(i % 3);
                        found = true;
                        toTheNest = true;
                    }
                }
            }
        }

        // Second key AI logic of an ant:
            // if I found a strong pheromone trail around me go to the lightest one, hopefully in the opposite direction of the strongest one
            // this will lead me to a food' source or the nest
        if (pheromoneCounter < 5) { // if there are too many pheromones around you, you'll get very confused, so ignore them
            if (leph != null) {
                if (Math.random() < ((Pheromone.maxStrength - leph.getStrength()) / (double) Pheromone.maxStrength) * 1.2) { // with a P of the strength of the strongest pheromone found
                    Direction desirableDir = translatePosInDir(leph.yPos, leph.xPos);
                    if (toTheNest) {
                        for (int i = 0; i < 3; i++) {
                            if (nestDirections.get(i) == desirableDir) {
                                chosenDir = desirableDir;
                                found = true;
                                break;
                            }
                        }
                    } else {
                        boolean ok = true;
                        for (int i = 0; i < 3; i++) {
                            if (nestDirections.get(i) == desirableDir) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            chosenDir = desirableDir;
                            found = true;
                        }
                    }

                }
            }
        }
        if (found) {
            onARandomPath = false;
            countDir = 0;
        }
    }

    /**
     * Simply moves the ant to the positions given by parameters. They must be nearby but this is delegated to search(), movement() and translateDirInPos().
     * If the position given is not free then returning false will trigger to choose a random direction to follow
     * @param y the column number in the grid
     * @param x the row number in the grid
     * @see #search
     * @see #movement
     * @see #translateDirInPos
     * @param <E> an element of the model: an ant, a food's source, a nest, a pheromone
     * @return true if you actually moved, false if the position given is already occupied
     */
    private <E> boolean move(Integer y, Integer x) {
        E element = whoIsThere(y, x);
        if (element == null || element.getClass() == Pheromone.class) {
            if ((inBounds(y, x)) && (!Objects.equals(yPos, y) || !Objects.equals(xPos, x))) {
                if (leaveTrail > 1) {
                    Pheromone pheHere = AntSimulator.getCurrentTrailPheromones().get(AntSimulator.key(y, x));
                    if (pheHere != null) {  // then leave a trail with a strength the sum of mine new pheromone plus the strength of the one already here
                        AntSimulator.addPheromone(new Pheromone(yPos, xPos, this, Pheromone.pheType.Trail, Math.min(Pheromone.maxStrength, strengthOfNewTrailPheromone + pheHere.getStrength())));
                    }
                    else {
                        AntSimulator.addPheromone(new Pheromone(yPos, xPos, this, Pheromone.pheType.Trail, strengthOfNewTrailPheromone));
                    }
                    leaveTrail -= 1;
                }
                this.yPos = y;
                this.xPos = x;
                return true;
            }
        }
        return false;
    }

    /**
     * compute a random direction to follow.
     * The logic is to give better chance to go to a direction depending on the distance of your position from the nest.
     * That is greater the distance the more you would want to go towards the nest, closer the distance the more you would want to go away from it.
     * @param <E> an element of the model: an ant, a food's source, a nest, a pheromone
     */
    private <E> void findRandomDirection() {
        random_seed = new Random();
        E obstacle;
        double halfDMax = dMax / 2;
        boolean found = false;

        // inside the halfway circle of ray = halfDMax
        if (closestNestDistances.get(4) < halfDMax) {
            for (int i = 7; i >= 0; i--) {
                double p = -((closestNestDistances.get(i) - halfDMax) / (2 * Math.log(GUI.DIMENSION) * (halfDMax) / (i + 1))) + 1 / 8.0;
                Direction possibleDir = nestDirections.get(i);
                translateDirInPos(possibleDir);
                obstacle = whoIsThere(nextY, nextX);
                if (obstacle == null || obstacle.getClass() == Pheromone.class) {
                    if (notOpposite(possibleDir)) {
                        if (random_seed.nextDouble() < p) {
                            if (inBounds(nextY, nextX)) {
                                chosenDir = possibleDir;
                                countDir = 0;
                                onARandomPath = true;
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!found) {   // re iterate but without the two inner if blocks, so that you will find a direction for sure, unless you don't have space to move at all
                for (int i = 7; i >= 0; i--) {
                    Direction possibleDir = nestDirections.get(i);
                    translateDirInPos(possibleDir);
                    obstacle = whoIsThere(nextY, nextX);
                    if (obstacle == null || obstacle.getClass() == Pheromone.class) {
                        if (inBounds(nextY, nextX)) {
                            chosenDir = possibleDir;
                            countDir = 0;
                            onARandomPath = true;
                            break;
                        }
                    }
                }
            }
        }
        // outside the halfway circle of ray = halfDMax
        else { // closestNestDistances.get(4) >= halfDMax
            for (int i = 0; i < 8; i++) {
                double p = ((closestNestDistances.get(i) - halfDMax) / (2 * Math.log(GUI.DIMENSION) * (halfDMax) / (8 - i))) + 1 / 8.0;
                Direction possibleDir = nestDirections.get(i);
                translateDirInPos(possibleDir);
                obstacle = whoIsThere(nextY, nextX);
                if (obstacle == null || obstacle.getClass() == Pheromone.class) {
                    if (notOpposite(possibleDir)) {
                        if (random_seed.nextDouble() < p) {
                            if (inBounds(nextY, nextX)) {
                                chosenDir = possibleDir;
                                countDir = 0;
                                onARandomPath = true;
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!found) {   // re iterate but without the two inner if blocks, so that you will find a direction for sure, unless you don't have space to move at all
                for (int i = 0; i < 8; i++) {
                    Direction possibleDir = nestDirections.get(i);
                    translateDirInPos(possibleDir);
                    obstacle = whoIsThere(nextY, nextX);
                    if (obstacle == null || obstacle.getClass() == Pheromone.class) {
                        if (inBounds(nextY, nextX)) {
                            chosenDir = possibleDir;
                            countDir = 0;
                            onARandomPath = true;
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     * An important method who controls if the given position is occupied or free.
     * @param yPos the row number
     * @param xPos the column number
     * @return the object who sits currently in the given position, if there is no one return null
     */
    private <E> E whoIsThere(Integer yPos, Integer xPos) {

        // another living ant
        Ant otherAnt = AntSimulator.getCurrentAlive().get(AntSimulator.key(yPos, xPos));
        if (otherAnt != null && otherAnt.getLife() > 0) return (E) otherAnt;

        // your nest
        if (nest.inNest(AntSimulator.key(yPos, xPos))) return (E) nest;

        // a food's source
        FoodSource food = AntSimulator.getCurrentFood().get(AntSimulator.key(yPos, xPos));
        if (food != null) return (E) food;

        // a pheromone trail
        Pheromone phe = AntSimulator.getCurrentTrailPheromones().get(AntSimulator.key(yPos, xPos));
        if (phe != null && phe.getStrength() > 0) return (E) phe;

        return null;
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
     * Tells, given a position next to you, what direction you should follow to reach that position
     * @param y the row where do you want to go
     * @param x the column where do you want to go
     * @return the Direction you should follow to reach y,x
     */
    private Direction translatePosInDir(int y, int x) {
        if ((yPos - 1 == y) && (xPos == x))     return Direction.N;
        else if ((yPos - 1 == y) && (xPos + 1 == x)) return Direction.NE;
        else if ((yPos == y)     && (xPos + 1 == x)) return Direction.E;
        else if ((yPos + 1 == y) && (xPos + 1 == x)) return Direction.SE;
        else if ((yPos + 1 == y) && (xPos == x))     return Direction.S;
        else if ((yPos + 1 == y) && (xPos - 1 == x)) return Direction.SO;
        else if ((yPos == y)     && (xPos - 1 == x)) return Direction.O;
        else if ((yPos - 1 == y) && (xPos - 1 == x)) return Direction.NO;
        return null;
    }

    /**
     * tell to the caller method if the desired direction is actually the opposite direction of the currently chosen one
     * @param desiredDir the direction you might want to go to
     * @return true if they are not the exact opposite, false if they are
     */
    private boolean notOpposite(Direction desiredDir) {
        if ((desiredDir == Direction.N)  && (chosenDir != Direction.S))  return true;
        else if ((desiredDir == Direction.NE) && (chosenDir != Direction.SO)) return true;
        else if ((desiredDir == Direction.E)  && (chosenDir != Direction.O))  return true;
        else if ((desiredDir == Direction.SE) && (chosenDir != Direction.NO)) return true;
        else if ((desiredDir == Direction.S)  && (chosenDir != Direction.N))  return true;
        else if ((desiredDir == Direction.SO) && (chosenDir != Direction.NE)) return true;
        else if ((desiredDir == Direction.O)  && (chosenDir != Direction.E))  return true;
        else if ((desiredDir == Direction.NO) && (chosenDir != Direction.SE)) return true;
        return false;
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
     * compute for every of the 8 directions around you the distance from the nest.
     * Then sort them in ascending order in the two ArrayLists: closestNestDistances and nestDirections
     * @param y the column number
     * @param x the row number
     */
    private void computeNestDistance(int y, int x) {
        double d = Math.sqrt(Math.pow(y - nestY, 2) + Math.pow(x - nestX, 2));  // two point distance equation
        for (int i = 0; i < 8; i++) {
            if (d < closestNestDistances.get(i)) {  // updates the data structures
                closestNestDistances.add(i, d);
                closestNestDistances.remove(8);
                nestDirections.add(i, translatePosInDir(y, x));
                break;
            }
        }
    }

    /**
     * control if the ant is full of food to carry or if it has some space left
     * if there is some space adds a new quantity to the correct stomach
     */
    private double gatherFood(double amount) {
        double delta = maxStomachCapacity - sharedStomach;
        double add = Math.max(0, Math.min(amount, delta));
        double left = amount;

        // firstly try to full the shared stomach
        sharedStomach += add;
        left -= add;
        if (left > 0) {     // if it is already full, move one quantity from the shared food to the private one and then gather the new food
            double delta2 = maxStomachCapacity - privateStomach;
            double add2 = Math.max(0, Math.min(left, delta2));
            privateStomach += add2;
            left -= add2;
        }
        return left;
    }

    /**
     * kills an ant, mainly called by {@code AntSimulator}
     */
    void die() {
        life = 0.0;
    }

    /**
     * decrease the value of life of the ant, simulates aging.
     * If the ant is starving of food the multiplier will be greater the 2, and it will grow every turn.
     * If the ant is not starving it is equal to 1, and it simulates simple aging
     */
    void age() {
        life -= starvingMultiplier;
    }

    /**
     * eat some food you are carrying.
     * First eat from the private stomach, if it is empty from the shared one
     * If you don't have any food you start to starve
     */
    void eat() {
        if (privateStomach - foodToEatEveryDay > 0) {   // firstly get your food from the private stomach
            privateStomach -= foodToEatEveryDay;
            starvingMultiplier = 1.0;
        }
        else if (sharedStomach - foodToEatEveryDay > 0) {   // if it is empty then try the shared one
            sharedStomach -= foodToEatEveryDay;
            starvingMultiplier = 1.0;
        }
        else {  // starve, increase the starvingMultiplier attribute
            starvingMultiplier += 0.5;
        }
    }

    /**
     * every turn transfer some food from the sharedStomach to the private one depending on the value of transferringSpeed
     * This only if the private one doesn't exceed its maximum capacity and the shared one doesn't go below zero
     */
    void transferFood() {
        if ((privateStomach + transferringSpeed < maxStomachCapacity) && (sharedStomach - transferringSpeed > 0)) {
            privateStomach += transferringSpeed;
            sharedStomach -= transferringSpeed;
        }
    }

    public Color getColor() {
        return color;
    }

    public Double getLife() {
        return life;
    }

    public Integer getPos() {
        return AntSimulator.key(yPos, xPos);
    }

    static void setDMax(double value) {
        dMax = value;
    }

    void printStats() {
        System.out.println("ANT ID: " + this);
        System.out.println("life:" + life + "  food in storage:" + (sharedStomach + privateStomach));
        System.out.println("sharedStomach:" + sharedStomach + "  privateStomach:" + privateStomach);
        System.out.println("yPos:" + yPos + "  xPos:" + xPos + "  chosenDir:" + chosenDir);
        System.out.println("toTheNest:" + toTheNest + "  onARandomPath:" + onARandomPath);
    }

    void printStats(boolean verbose) {
        printStats();
        if (verbose) {
            System.out.println("stomachCapacity:" + maxStomachCapacity + "  eat everyday:" + foodToEatEveryDay);
            System.out.println("threshold:" + (maxStomachCapacity * 0.7) + "  minToTheNest:" + (maxStomachCapacity * 0.5) + "  maxToTheNest:" + (maxStomachCapacity * 1.6));
            System.out.println("maxLeaveTrail:" + maxLeaveTrail + "  strengthOfNewTrailPheromone:" + strengthOfNewTrailPheromone);
            System.out.println("changeDirection:" + changeDirection + "  starvingMultiplier:" + starvingMultiplier);
            System.out.println("nest reserves:" + nest.getReservoir());

            if (leph != null) {
                System.out.println("lephY:" + leph.yPos + "  lephX:" + leph.xPos);
            }
            else
                System.out.println("leph is null");
            System.out.println("_____________________________________________________________");
        }
    }
}