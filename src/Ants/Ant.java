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

    /*
    /**
     * the hunger level of the ant, if it reaches zero the ant will starve
     */
//    Double hunger;


    /**
     * increases its value when hunger = 0 every turn,
     * turns to 0 if hunger > 0
     */
    private Double starvingMultiplier;

    private final Double foodToEatEveryDay;

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
     * my maximum capacity of food quantities I can carry with me.
     * This maximum is the same for both of my stomachs.
     */
    private final Integer maxStomachCapacity;

    /**
     * the color used by UI.GUI to paint the GUI.innerPanel correctly
     */
    public final Color color = new Color(0, 120, 0);

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

    private boolean toTheNest;

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

    private final int nestY;
    private final int nestX;

    private ArrayList<Double> closestNestDistances;

    private ArrayList<Direction> nestDirections;

    private static double dMax;

    private final int MaxRoaming;

    private int roaming;

    /**
     * my personal pheromone of type personal and strength 100
     */
    private Pheromone personalPheromone;

    private Ant child;

    /**
     * seph stands for Lightest Encountered PHeromone
     * every day it returns to null but get updated on the pheromones an ant sniff around it
     */
    private Pheromone leph;

    private int pheromoneCounter;

    private final Integer nTraitsToTransmit;

    ArrayList<Double> antAttributes = new ArrayList<>(8);

    /**
     * This constructor is called only by AntSimulator at the start of the simulation
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
        this.personalPheromone = new Pheromone(yPos, xPos, this);
        random_seed = new Random();

        // changeDirection random value extraction
        int minChange = (int) Math.max(2, Math.floor(Math.log(GUI.DIMENSION / 5.0)) - 1);
        int maxChange = (int) Math.ceil(minChange * Math.sqrt(minChange)) + 1;
        changeDirection = random_seed.nextInt(minChange, maxChange);
        countDir = 0;

        // maxLeaveTrail random value extraction
        maxLeaveTrail = random_seed.nextInt(maxChange, 2 * maxChange);
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
        MaxRoaming = (int) random_seed.nextDouble(life * 0.25, life * 0.40);
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
        antAttributes.add(7, (double) MaxRoaming);
    }

    Ant (Integer y, Integer x, AntsNest nest, ArrayList<Double> attributes) {
        yPos = y;
        xPos = x;
        onARandomPath = true;
        toTheNest = false;
        this.life = 100.0;
        this.nest = nest;
        this.personalPheromone = new Pheromone(yPos, xPos, this);
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
        MaxRoaming = (int) random_seed.nextDouble(life * 0.25, life * 0.40);
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
        antAttributes.add(7, (double) MaxRoaming);
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
     * @return if the ant encountered the nest and gave it some of its food then the nest will
     *         spawn an ant with some genetic code of this ant
     */
    Ant action() {

        child = null;
        leph = null;
        pheromoneCounter = 0;
        roaming += 1;

        transferFood();

        eat();

        orientYourself();

        search(true, false);

        decide();

        searchFood();

        movement();

        age();

//        printStats(false);

        return child;
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
//                System.out.println("dir:" + dir + " nextY:" + nextY + " nextX:" + nextX);
                interact(nextY, nextX);
            }
            else if (nest) {
                translateDirInPos(dir);
                computeNestDistance(nextY, nextX);
            }
        }
//        if (nest) {
//            for (int i = 0; i < 8; i++) {
//                System.out.println("i:" + i + ", dist:" + closestNestDistances.get(i) + ", dir:" + nestDirections.get(i));
//            }
//            System.out.println("******************************************");
//        }
    }

    void searchFood() {
        if ((sharedStomach + privateStomach) < (maxStomachCapacity * 1.5)) {    // search food only if you are not already full
            random_seed = new Random();
            ArrayList<Direction> directionList = new ArrayList<>(List.of(Direction.values()));
            int index;
            for (int i = 0; i < 8; i++) {
                index = random_seed.nextInt(directionList.size());
                Direction dir = directionList.remove(index);
                translateDirInPos(dir);     // updates nextY and nextX
//                System.out.println("dir:" + dir + " nextY:" + nextY + " nextX:" + nextX);
                int deltaY = nextY - yPos;
                int deltaX = nextX - xPos;
                boolean found = false;
                FoodSource food = AntSimulator.getCurrentFood().get(AntSimulator.key(nextY + deltaY, nextX + deltaX));
                if (food != null && notOpposite(dir)) {
                    chosenDir = dir;
                    found = true;
                }
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
     * the complete algorithm who controls the movement of an ant
     */
    void movement() {
        if (onARandomPath) {
            countDir += 1;
        }

        // follow your path
        if (countDir < changeDirection && chosenDir != null) {   // I don't believe is it possible to reach changeDirection
            translateDirInPos(chosenDir);    // updates nextY and nextX
            if (move(nextY, nextX)) {
//                System.out.println("random?" + onARandomPath + " count:" + countDir + " changeDir:" + changeDirection);
//                System.out.println("chosenDir:" + chosenDir + " yPos:" + this.yPos + " xPos:" + this.xPos);
                return;
            }
        }

        // otherwise, search a random path
        findRandomDirection();

        move(nextY, nextX);
//        System.out.println("RandomDir:" + chosenDir + " yPos:" + this.yPos + " xPos:" + this.xPos);
    }

    /**
     *
     * @param y nextY
     * @param x nextX
     * @param <E> a generic element. Can be ONLY an Ant (different from this), a FoodSource, an AntsNest or a Pheromone.
     */
    <E> void interact(Integer y, Integer x) {
        E element = whoIsThere(y, x);   // called on nextY and nextX

        if (element == null) return;    // if there is no one
//        System.out.println("elementClass:" + element.getClass());

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

    void antInteraction(Ant otherAnt) {
        // do action relative to meeting a new ant or a previously known ant
        // mating and pheromones sniffing

        // Trophallaxis
        double myDelta = this.maxStomachCapacity - this.sharedStomach;     // how much our stomach is empty
        double yourDelta = otherAnt.maxStomachCapacity - otherAnt.sharedStomach;
        if ((myDelta > yourDelta + 1) && (otherAnt.sharedStomach > 1)) {    // mine in emptier, so you shall give some food to me
            if (Math.random() < ((myDelta - yourDelta) / (double) this.maxStomachCapacity )) {      // with a P of the difference of our deltas / mine maximum capacity (which is always greater than the difference)
//                System.out.println("myD:" + myDelta + " yourD:" + yourDelta + ". Mymaxcapacity:" + this.maxStomachCapacity + ". P:" + ((myDelta - yourDelta) / (double) this.maxStomachCapacity ));
                this.sharedStomach += 1;
                otherAnt.sharedStomach -= 1;
            }
//            else {
//                System.out.println("myD:" + myDelta + " yourD:" + yourDelta + ". Mymaxcapacity:" + this.maxStomachCapacity + ". Failed trophallaxis with a P:" + ((myDelta - yourDelta) / (double) this.maxStomachCapacity ));
//            }
        }
        else if ((yourDelta > myDelta + 1) && (this.sharedStomach > 1)) {
            if (Math.random() < ((yourDelta - myDelta) / (double) otherAnt.maxStomachCapacity)) {
//                System.out.println("yourD:" + yourDelta + " myD:" + myDelta + ". Yoursmaxcapacity:" + otherAnt.maxStomachCapacity + ". P:" + ((yourDelta - myDelta) / (double) otherAnt.maxStomachCapacity));
                this.sharedStomach -= 1;
                otherAnt.sharedStomach += 1;
            }
//            else {
//                System.out.println("yourD:" + yourDelta + " myD:" + myDelta + ". Yoursmaxcapacity:" + otherAnt.maxStomachCapacity + ". Failed trophallaxis with a P:" + ((yourDelta - myDelta) / (double) otherAnt.maxStomachCapacity));
//            }
        }
    }

    void nestInteraction(AntsNest nest) {
//        System.out.println("nest nearby");
        // you have found your nest
        // do action relative to the nest encounter
//        leaveTrail = maxLeaveTrail;
        toTheNest = false;  // change state and objective
        countDir = changeDirection; // change direction
        roaming = 0;

        // deposit eggs
        if (life < 90) {
            nest.transmitGenetics(antAttributes);
            child = nest.reproduction();
//            System.out.println("child:" + child);
        }

        // deposit food or eat its reserves
        // deposit an amount equal to a random value between this.sharedStomach - 1 and 0, with a continuous random variable that has exponential distribution
        double stomachsSum = sharedStomach  + privateStomach;
        double threshold = maxStomachCapacity * 0.99;
        if (stomachsSum > threshold) {    // give
//            double expDistributionQuantity = Math.min(1, 1 * (Math.log(1 - Math.random()) / (- sharedStomach)));
//            System.out.println("expdistr:" + expDistributionQuantity);
//            System.out.println("sharedStomach before:" + sharedStomach + " after:" + (sharedStomach * expDistributionQuantity));
//            double foodKept = sharedStomach * expDistributionQuantity;

            double howMuch = (stomachsSum - threshold);
            nest.addReserves(howMuch);
            sharedStomach -= howMuch;
//            System.out.println("nest reservoirs " + nest.getReservoir());
        }
        else {  // take
            double howMuch = (threshold - stomachsSum);
//            System.out.println("howmuch:" + howMuch);
            if (nest.getFoodFromReserves(howMuch)) {
                sharedStomach += howMuch;
            }
        }
    }

    void foodInteraction(FoodSource food) {
//        System.out.println("food nearby");
        // if you have already eaten the maximum food you can, AND you are carrying the maximum food you can ignore it
        // otherwise:
//        System.out.println("sharedStomach:" + sharedStomach + "privateStomach:" + privateStomach);
        double amount = maxStomachCapacity * 0.7;
        double available = food.gathering(amount);
//        System.out.println("foodID:" + food + ", available: " + available);
        if (available > 0) {
//            System.out.println("there is food");
            // do action relative to gathering food and discovering a new food source
            double left = gatherFood(available);
            food.reverseGathering(left);
//          System.out.println("sharedStomach:" + sharedStomach + "privateStomach:" + privateStomach);
            leaveTrail = maxLeaveTrail;
        }
        else AntSimulator.foodFinished(food);
    }

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
     *
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
     *
     */
    private <E> void decide() {
        // First key AI logic of an ant:
            // if I am starving or have little food go to you nest to feed
            // at the opposite if I have plenty of food with me, it is useless to continue finding food in the ambient so come back to the nest
            // if I have just enough quantity of food to go exploring then I will prefer to go outside and explore the ambient for food
        boolean found = false;
        double stomachSum = sharedStomach + privateStomach;
        // remember that maxStomachCapacity it's the maximum capacity of a single stomach
//        System.out.println("stomachSum:" + stomachSum + " max*0.4:" + (maxStomachCapacity * 0.4) + " max*1.6:" + (maxStomachCapacity * 1.6));
        // if I decided time ago to go to the nest don't change that decision until you come to the nest
        if (roaming > MaxRoaming && (toTheNest || ((stomachSum < maxStomachCapacity * 0.5)  || (stomachSum > maxStomachCapacity * 1.5)))) {    // little or much food condition
//            System.out.println("stomachSum" + stomachSum + " MaxCapacity:" + maxStomachCapacity);
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
//            System.out.println("tothenest:" + toTheNest + " chosenDir:" + chosenDir + " yPos:" + this.yPos + " xPos:" + this.xPos);

        // Second key AI logic of an ant:
            // if I found a strong pheromone trail around me go to the lightest one, hopefully in the opposite direction of the strongest one
            // this will lead me to a food' source or the nest
        if (pheromoneCounter < 4) { // if there are too many pheromones around you, you'll get very confused, so ignore them
            if (leph != null) {
                if (Math.random() < ((Pheromone.maxStrength - leph.getStrength()) / (double) Pheromone.maxStrength) * 1.2) { // with a P of the strength of the strongest pheromone found
                    Direction desirableDir = translatePosInDir(leph.yPos, leph.xPos);

                    if (toTheNest) {
                        for (int i = 0; i < 3; i++) {
                            if (nestDirections.get(i) == desirableDir) {
                                chosenDir = desirableDir;
                                found = true;
//                            System.out.println("tothenest:" + toTheNest + " chosenDir:" + chosenDir + " yPos:" + this.yPos + " xPos:" + this.xPos + " lephY:" + leph.yPos + " lephX:" + leph.xPos);
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
//                            System.out.println("tothenest:" + toTheNest + " chosenDir:" + chosenDir + " yPos:" + this.yPos + " xPos:" + this.xPos + " lephY:" + leph.yPos + " lephX:" + leph.xPos);
                    }

                }
            }
        }
        if (found) {
            onARandomPath = false;
            countDir = 0;
        }
//        System.out.println("chosenDir:" + chosenDir + " onaRandomPath:" + onARandomPath + " countDir:" + countDir);

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
    private <E> boolean move(Integer y, Integer x) {
        E element = whoIsThere(y, x);
        if (element == null || element.getClass() == Pheromone.class) {
            if ((inBounds(y, x)) && (!Objects.equals(yPos, y) || !Objects.equals(xPos, x))) {
                if (leaveTrail > 1) {
                    Pheromone pheHere = AntSimulator.getCurrentTrailPheromones().get(AntSimulator.key(y, x));
                    if (pheHere != null) {  // then leave a trail with a strength the sum of mine new pheromone plus the strength of the one already here
//                        System.out.println("maxStrength" + Pheromone.maxStrength + " mystrength:" + strengthOfNewTrailPheromone + " pheHerestrength:" + pheHere.getStrength());
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

    /*
    /**
     * decide whether the direction given as a parameter can be a new direction to follow: it must be free.
     * @return true if it finds a new direction, false otherwise
     */
   /* private <E> boolean findRandomDirection(Direction dir) {
        //  ADD THE PROBABILITY TO FIND A DIRECTION BASED ON THE STATE "toTheNest"
        translateDirInPos(dir);     // updates nextY and nextX
        E element = whoIsThere(nextY, nextX);
        if (element == null || element.getClass() == Pheromone.class) {
            if (inBounds(nextY, nextX) && (!Objects.equals(yPos, nextY) || !Objects.equals(xPos, nextX))) {
                if (toTheNest) {
                    for (int i = 0; i < 3; i++) {
                        if (nestDirections.get(i) == dir) {
                            chosenDir = dir;
                            countDir = 0;
                            onARandomPath = true;
                            return true;
                        }
                    }
                    if (Math.random() < 0.2) {
                        chosenDir = dir;
                        countDir = 0;
                        onARandomPath = true;
                        return true;
                    }
                    return false;
                }
                else {
                    for (int i = 0; i < 3; i++) {
                        if (nestDirections.get(i) == dir) {
                            if (Math.random() < 0.2) {
                                chosenDir = dir;
                                countDir = 0;
                                onARandomPath = true;
                                return true;
                            }
                            return false;
                        }
                    }
                    chosenDir = dir;
                    countDir = 0;
                    onARandomPath = true;
                    return true;
                }

            }
        }
        return false;
    }
    */

    // TO BE TESTED
    private <E> void findRandomDirection() {
//        System.out.println("FIND RANDOM DIRECTION");
        random_seed = new Random();
        E obstacle;
        double halfDMax = dMax / 2;
        boolean found = false;
//        System.out.println("D:" + dMax + " D/2:" + halfDMax);

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
//                                System.out.println("Inside of the midway circle");
//                                System.out.println("Within if blocks");
//                                System.out.println("dir:" + chosenDir + " P:" + p);
                                break;
                            }
                        }
                    }
                }
            }
            if (!found) {   // re iterate but without the two inner if blocks, so that you will find a direction for sure, unless you don't have space to move at all
                for (int i = 7; i >= 0; i--) {
                    double p = -((closestNestDistances.get(i) - halfDMax) / (2 * Math.log(GUI.DIMENSION) * (halfDMax) / (i + 1))) + 1 / 8.0;
                    Direction possibleDir = nestDirections.get(i);
                    translateDirInPos(possibleDir);
                    obstacle = whoIsThere(nextY, nextX);
                    if (obstacle == null || obstacle.getClass() == Pheromone.class) {
                        if (inBounds(nextY, nextX)) {
                            chosenDir = possibleDir;
                            countDir = 0;
                            onARandomPath = true;
//                            System.out.println("Inside of the midway circle");
//                            System.out.println("Without if blocks");
//                            System.out.println("dir:" + chosenDir + " P:" + p);
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
//                                System.out.println("Out of the midway circle");
//                                System.out.println("Within if blocks");
//                                System.out.println("dir:" + chosenDir + " P:" + p);
                                break;
                            }
                        }
                    }
                }
            }
            if (!found) {   // re iterate but without the two inner if blocks, so that you will find a direction for sure, unless you don't have space to move at all
                for (int i = 0; i < 8; i++) {
                    double p = ((closestNestDistances.get(i) - halfDMax) / (2 * Math.log(GUI.DIMENSION) * (halfDMax) / (8 - i))) + 1 / 8.0;
                    Direction possibleDir = nestDirections.get(i);
                    translateDirInPos(possibleDir);
                    obstacle = whoIsThere(nextY, nextX);
                    if (obstacle == null || obstacle.getClass() == Pheromone.class) {
                        if (inBounds(nextY, nextX)) {
                            chosenDir = possibleDir;
                            countDir = 0;
                            onARandomPath = true;
//                            System.out.println("Out of the midway circle");
//                            System.out.println("Without if blocks");
//                            System.out.println("dir:" + chosenDir + " P:" + p);
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

    private void computeNestDistance(int y, int x) {
//        System.out.println("nestY:" + nestY + " nestX:" + nestX);
//        System.out.println("y:" + y + " x:" + x);
        double d = Math.sqrt(Math.pow(y - nestY, 2) + Math.pow(x - nestX, 2));  // two point distance equation
        for (int i = 0; i < 8; i++) {
            if (d < closestNestDistances.get(i)) {
                // updates the data structures
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
        personalPheromone = null;
    }  // maybe a call to the Java Garbage Collector

    /**
     * decrease the value of life of the ant, simulates aging.
     * If the ant is starving of food the multiplier will be greater the 2, and it will grow every turn.
     * If the ant is not starving it is equal to 1, and it simulates simple aging
     */
    void age() {
        life -= starvingMultiplier;
//        System.out.println("life:" + life);
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
//        System.out.println("starvingMultiplier:" + starvingMultiplier + " food to eat everyday:" + foodToEatEveryDay);
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
//        System.out.println("shared:" + sharedStomach + " private:" + privateStomach + " speed:" + transferringSpeed);
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