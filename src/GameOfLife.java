import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class GameOfLife implements Game {
	
    public static int delay = 100;
	private GUI gui;
	private Timer t;
	
	public GameOfLife(GUI gui) {
		this.gui = gui;
	}
	
	public void startGame() {
		
		ActionListener taskPerformer = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gui.play) {
					compute();
					gui.currentFrame = gui.nextFrame;					
					gui.nextFrame = new boolean[GUI.HEIGHT][GUI.WIDTH];
					gui.getPanel().repaint();	
				}
			}
		};
		t = new Timer(delay, taskPerformer);
		t.start();
	}
	
	private void compute() {
		
		for(int i = 0; i < GUI.HEIGHT; i++) {
			for(int j = 0; j < GUI.WIDTH; j++) {
				check(i, j);
			}
		}
	}
	
	private void check(int i, int j) {
		
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
		
		if(neighbors == 3) 
			gui.nextFrame[i][j] = true;
		
		else gui.nextFrame[i][j] = gui.currentFrame[i][j] && neighbors == 2;
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
	public void resetMap() {}

	@Override
	public Map<Integer, Giocatore> getCurrentAlive() {
		return null;
	}
}
