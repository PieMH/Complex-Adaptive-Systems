import javax.swing.*;
import java.util.Map;

public class AntSimulator implements Game{

    private GUI gui;

    public AntSimulator(GUI gui) {
        resetMap();
        this.gui = gui;
    }

    @Override
    public void startGame() {

    }

    @Override
    public Map<Integer, Giocatore> getCurrentAlive() {
        return null;
    }

    @Override
    public void setRandom(boolean random) {

    }

    @Override
    public void setStartingRandomAgents(int number) {

    }

    @Override
    public void setMap(int y, int x) {

    }

    @Override
    public Timer getTimer() {
        return null;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public void resetMap() {

    }
}