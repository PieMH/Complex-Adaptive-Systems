package Interfaces;

import javax.swing.Timer;

/**
 * the Interface who sets the main method common to every possible CAS model.
 * See the following classes that implements this:
 * @see Ants.AntSimulator
 * @see SGS.SocialGameSystem
 * @see GOL.GameOfLife
 */
public interface CASModel {

	void startSimulation();

	void setRandom(boolean random);

	void setStartingRandomAgents(int number);

	void setMapRandom();

	void setMap(int y, int x);

	Timer getTimer();

	void resetMap();
}