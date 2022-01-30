import java.util.Map;
import javax.swing.Timer;

public interface Game {

	void startGame();

	Map<Integer, Giocatore> getCurrentAlive();

	void setMap(int y, int x);

	Timer getTimer();

	int getDelay();

	void resetMap();
}
