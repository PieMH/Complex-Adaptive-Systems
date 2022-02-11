import javax.swing.Timer;

import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Random;

public class GameOfLife implements Game {
	
    public static int delay = 100;
	private GUI gui;
	private Timer t;
	static boolean random = false;	// if there is random spawn of agents in the Game
	static int random_starting_players = 2000;	// if random is true this is the amount of agents spawned
	private boolean reset = true;	// notifies if the button reset is pressed, if random is true is needed to recreate random agents
	private final Object lock = new Object();	// lock per i thread gioco/GUI nella modifica contemporanea allo scorrimento sul dizionario

	public GameOfLife(GUI gui) {
		resetMap();
		this.gui = gui;
	}

	@ToDo ( toUpdate = "Add the possibility to randomly generate n living beings if n is given by the user through Options Menu")
	public void startGame() {
		ActionListener taskPerformer = e -> {
			if(gui.play) {
				if (reset) {	// is it possible to optimize this if section?
					if (random) generateRandomAgents();
					reset = false;
				}
				iterate();
				gui.currentFrame = gui.nextFrame;
				gui.nextFrame = new boolean[GUI.HEIGHT][GUI.WIDTH];
				gui.getPanel().repaint();
			}
		};
		t = new Timer(delay, taskPerformer);
		t.start();
	}

	private void iterate() {
		for(int i = 0; i < GUI.HEIGHT; i++) {
			for(int j = 0; j < GUI.WIDTH; j++) {
				liveOrDie(i, j);
			}
		}
	}

	private void liveOrDie(int i, int j) {
		int neighbors = countNeighbors(i, j);
		if(neighbors == 3)
			gui.nextFrame[i][j] = true;
		else gui.nextFrame[i][j] = gui.currentFrame[i][j] && neighbors == 2;
	}

	private int countNeighbors(int i , int j) {
		int neighbors = 0;
		for(int x = -1; x <= 1; x++) {
			for(int y = -1; y <= 1; y++) {
				int i2 = i + x;
				int j2 = j + y;
				if(i2 < 0 || i2 > GUI.HEIGHT - 1)
					continue;
				if(j2 < 0 || j2 > GUI.WIDTH - 1)
					continue;
				if(gui.currentFrame[i2][j2] && !(x == 0 && y == 0))
					neighbors += 1;
			}
		}
		return neighbors;
	}

	private void generateRandomAgents() {
		synchronized (lock) {
			Random random_seed = new Random();
			int y, x;
			for (int n = 0; n < random_starting_players; n++) {
				y = random_seed.nextInt(GUI.HEIGHT);
				x = random_seed.nextInt(GUI.WIDTH);
				if (gui.currentFrame[y][x]) {
					n--;
					continue;
				}
				gui.currentFrame[y][x] = true;
			}
		}
	}

	// overriding methods of Game Interface

	@Override
	public void setRandom(boolean r) {
		random = r;
	}

	@Override
	public void setStartingRandomAgents(int number) {
		random_starting_players = number;
	}

	@Override
	public void setMap(int y, int x) {}

	@Override
	public Timer getTimer() {
		return t;
	}

	@Override
	public int getDelay() {
		return delay;
	}

	@Override
	public void resetMap() {
		reset = true;
	}

	@Override
	public Map<Integer, Giocatore> getCurrentAlive() {
		return null;
	}
}