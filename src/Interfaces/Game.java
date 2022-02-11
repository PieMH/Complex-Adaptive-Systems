package Interfaces;

import javax.swing.Timer;

public interface Game {

	void startGame();

	void setRandom(boolean random);

	void setStartingRandomAgents(int number);

	void setMap(int y, int x);

	Timer getTimer();

	void resetMap();
}