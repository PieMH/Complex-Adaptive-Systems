import java.util.Map;
import javax.swing.Timer;

public interface Game {

	void startGame();

	Map<Integer, Giocatore> getCurrentAlive();  // NON MI CONVINCE

	void setRandom(boolean random);

	void setStartingRandomAgents(int number);

	void setMap(int y, int x);

	Timer getTimer();

	int getDelay();

	void resetMap();
}