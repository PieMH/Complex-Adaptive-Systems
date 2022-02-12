package UI;

import Interfaces.Game;
import SGS.Giocatore;
import SGS.SocialGameSystem;

import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridBagLayout;
import java.awt.event.MouseMotionAdapter;
import java.io.Serial;
import javax.swing.JSlider;
import javax.swing.JLabel;

/**
 * This is the class responsible for managing the UI.GUI for the simulation.
 * It is instantiated by UI.OptionsMenu and thus is strongly dependent by it.
 * Runs as a separate thread from UI.OptionsMenu and the Games it is displaying on the frame.
 * <p>
 * There are two panel, inner and outer. The most important is the centre where there is a rectangular frame, where the simulation is shown.
 * The bottom holds many control buttons and a slider.
 * <p>
 * <b>Notes:</b> maintain the proportions of width and height as 2:1 respectively.
 */
public class GUI {

    public static int WIDTH = 100;
    public static int HEIGHT = 50;
    public static int DIMENSION = WIDTH * HEIGHT;
    private JFrame frame;
    private JPanel innerPanel;
    private JPanel outerPanel;
    public boolean[][] currentFrame = new boolean[HEIGHT][WIDTH];   // a value is true if there is a living agent (depends on the game the logic behind a true value) inside a grid's square, false otherwise
    public boolean[][] nextFrame = new boolean[HEIGHT][WIDTH];  // the next rame boolean matrix, for the progress of the game through time
    public boolean play = false;   // state of the simulation
    private final OptionsMenu optionsMenu;  // the Options Menu object who holds this object
    private Game game;  // an instance of Interface.game interface
    // The next three are used only for SGS
    private Giocatore focusedPlayer;    // the square on the grid that has the mouse hovering on it
    private boolean showLife = false;
    private boolean showWellness = false;

    /**
     * Creates the UI.GUI for the simulation.
     * Called ONLY by Options Menu which is instantiated firstly.
     * @param optionsMenu the instantiation of UI.OptionsMenu class who is the father of all threads of the simulator
     */
    GUI(OptionsMenu optionsMenu) {
        this.optionsMenu = optionsMenu;
        initialize();
    }

    /**
     * Connects this class with the interface Interfaces.Game
     * @param game: instance of Interfaces.Game interface
     */
    void setGame(Game game) {
        this.game = game;
    }

    /**
     * Returns the main frame.
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Returns the panel.
     */
    public JPanel getPanel() {
        return innerPanel;
    }

    /**
     * Initializes the contents of the frame and the frame itself.
     */
    private void initialize() {
        frame = new JFrame();
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(780, 522));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(Color.WHITE);

        // keep title updated
        if (OptionsMenu.CAS_type == OptionsMenu.CAS.AntSimulator) {
            frame.setTitle("Ant Simulator");
        }
        else if (OptionsMenu.CAS_type == OptionsMenu.CAS.SocialGameSystem) {
            frame.setTitle("Social Game System");
        }
        else if (OptionsMenu.CAS_type == OptionsMenu.CAS.GameOfLIfe) {
            frame.setTitle("Game Of Life");
        }

        JButton btnPlay = new JButton("Play");
        btnPlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!play) {
                    play = true;
                    btnPlay.setText("Pause");
                }
                else {
                    play = false;
                    btnPlay.setText("Play");
                }
            }
        });

        JButton btnReset = new JButton("Reset");
        btnReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentFrame = new boolean[HEIGHT][WIDTH];
                game.resetMap();
                innerPanel.repaint();
            }
        });

        JButton btnOptions = new JButton("Options");
        btnOptions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    optionsMenu.getFrame().setVisible(!optionsMenu.getFrame().isVisible());
                }
            }
        });

        JButton btnShowLife = new JButton("Life");
        btnShowLife.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLife = !showLife;
        		showWellness = false;
                innerPanel.repaint();
            }
        });

        JButton btnShowWellness = new JButton("Wellness");
        btnShowWellness.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		showWellness = !showWellness;
                showLife = false;
        		innerPanel.repaint();
        	}
        });

        if (OptionsMenu.CAS_type != OptionsMenu.CAS.SocialGameSystem) {
            btnShowLife.setVisible(false);
            btnShowWellness.setVisible(false);
        }

        JLabel labelStepDelay = new JLabel("Step delay in ms:");


        JSlider slider = new JSlider(10, 200, 100);
        slider.setMinorTickSpacing(10);
        slider.setMajorTickSpacing(30);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            if(!slider.getValueIsAdjusting()) {
                game.getTimer().setDelay(slider.getValue() + 1);
            }
        });

        //************************************************************************************************************/
        // Create layout
        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addGap(6)
        			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        				.addComponent(outerPanel, GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE)
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(13)
        					.addComponent(btnPlay, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnReset, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnOptions, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnShowLife, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnShowWellness, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        					.addComponent(labelStepDelay)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(slider, GroupLayout.PREFERRED_SIZE, 184, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap())
        );
        groupLayout.setVerticalGroup(
        	groupLayout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(outerPanel, GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
        			.addGap(27)
        			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        					.addComponent(btnPlay)
        					.addComponent(btnReset)
        					.addComponent(btnOptions)
        					.addComponent(btnShowLife)
        					.addComponent(btnShowWellness)
        					.addComponent(labelStepDelay))
        				.addComponent(slider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap())
        );

        frame.getContentPane().setLayout(groupLayout);
        //************************************************************************************************************/

        innerPanel = new JPanel() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (OptionsMenu.CAS_type == OptionsMenu.CAS.GameOfLIfe) {
                    paintGOL(g);
                }
                else if (OptionsMenu.CAS_type == OptionsMenu.CAS.SocialGameSystem){
                     if (focusedPlayer != null) {
                        paintAcquaintanceBasedPixels(g);
                    }
                    else {
                        if (showLife)
                            paintLife(g);
                        else if (showWellness)
                        	paintWellness(g);
                        else
                        	paintPersonalityBasedPixel(g);
                    }
                }
                else if (OptionsMenu.CAS_type == OptionsMenu.CAS.AntSimulator) {

                }

                g.setColor(Color.BLACK);

                // DRAW LINES
                for(int i = 0; i <= GUI.HEIGHT; i++) {
                    int y = i * getHeight() / GUI.HEIGHT;
                    g.drawLine(0, y, getWidth(), y);
                }

                for( int j = 0; j <= GUI.WIDTH; j++) {
                    int x = j * getWidth() / GUI.WIDTH;
                    g.drawLine(x, 0, x, getHeight() );
                }
            }

            private void paintLife(Graphics g) {
                for(int i = 0; i < GUI.HEIGHT; i++) {
                    for(int j = 0; j < GUI.WIDTH; j++) {
                        if(currentFrame[i][j]) {
                            g.setColor(new Color(0, 128, 0, ((SocialGameSystem.getCurrentAlive().get(SocialGameSystem.key(i, j)).getLife()) * 255 / 100)));
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                        else {
                            g.setColor(innerPanel.getBackground());
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                    }
                }
            }

            private void paintWellness(Graphics g) {
                for (int i = 0; i < GUI.HEIGHT; i++) {
                    for (int j = 0; j < GUI.WIDTH; j++) {
                        if (currentFrame[i][j]) {
                            g.setColor(new Color(160, 0, 200, ((int) (Double.min(Double.max(((SocialGameSystem.getCurrentAlive().get(SocialGameSystem.key(i, j)).getWellness())), 0), 100))) * 255 / 100));
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH, i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        } else {
                            g.setColor(innerPanel.getBackground());
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH, i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                    }
                }
            }

            private void paintGOL(Graphics g) {
                for(int i = 0; i < GUI.HEIGHT; i++) {
                    for (int j = 0; j < GUI.WIDTH; j++) {
                        if (currentFrame[i][j]) {
                            g.setColor(Color.BLUE);
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH, i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        } else {
                            g.setColor(innerPanel.getBackground());
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH, i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                    }
                }
            }

            private void paintPersonalityBasedPixel(Graphics g) {
                for(int i = 0; i < GUI.HEIGHT; i++) {
                    for(int j = 0; j < GUI.WIDTH; j++) {
                        if(currentFrame[i][j]) {
                            g.setColor(SocialGameSystem.getCurrentAlive().get(SocialGameSystem.key(i, j)).carattere.getMyColor());
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                        else {
                            g.setColor(innerPanel.getBackground());
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                    }
                }
            }

            private void paintAcquaintanceBasedPixels(Graphics g) {
                for(int i = 0; i < GUI.HEIGHT; i++) {
                    for(int j = 0; j < GUI.WIDTH; j++) {
                        if(currentFrame[i][j]) {
                            g.setColor(Color.GRAY);
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                        else {
                            g.setColor(innerPanel.getBackground());
                            g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                    }
                }
                for(int i = 0; i < focusedPlayer.acquaintances.length; i++) {
                    if(focusedPlayer.acquaintances[i] != null) {
                        int x = focusedPlayer.acquaintances[i].x_position;
                        int y = focusedPlayer.acquaintances[i].y_position;

                        if(currentFrame[y][x]) {
                            g.setColor(SocialGameSystem.getCurrentAlive().get(SocialGameSystem.key(y, x)).carattere.getMyColor());
                            g.fillRect(x * innerPanel.getWidth() / GUI.WIDTH , y * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                    }
                }
                int x = focusedPlayer.x_position;
                int y = focusedPlayer.y_position;
                g.setColor(SocialGameSystem.getCurrentAlive().get(SocialGameSystem.key(y, x)).carattere.getMyColor());
                g.fillRect(x * innerPanel.getWidth() / GUI.WIDTH , y * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
            }

            @Override
            public Dimension getPreferredSize() {
                int width = outerPanel.getWidth() / GUI.WIDTH;
                int height = outerPanel.getHeight() / GUI.HEIGHT;
                int d = Integer.min(width, height);
                return new Dimension(GUI.WIDTH * d, GUI.HEIGHT * d);
            }
        };

        innerPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    int x = e.getX() * WIDTH / innerPanel.getWidth();
                    int y = e.getY() * HEIGHT / innerPanel.getHeight();
                    if(!currentFrame[y][x])     // for painting the square in any game mode
                        currentFrame[y][x] = true;
                    if (OptionsMenu.CAS_type == OptionsMenu.CAS.SocialGameSystem) {
                        game.setMap(y, x);
                    }
                    innerPanel.repaint();
                }
            }

            /**
             * Called whenever you move the mouse onf the frame.
             * But actually the body is executed if only if the game is on pause and in mode Social Game System.
             * It is used to call the repaint method having a non-null focused player. It is needed to call {@code paintAcquaintanceBasedPixels}.
             * @param e MouseEvent
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                if(!play && OptionsMenu.CAS_type == OptionsMenu.CAS.SocialGameSystem) {
                    int x = e.getX() * WIDTH / innerPanel.getWidth();
                    int y = e.getY() * HEIGHT / innerPanel.getHeight();
                    focusedPlayer = SocialGameSystem.getCurrentAlive().get(SocialGameSystem.key(y, x));
                    innerPanel.repaint();
                }
            }

        } );

        innerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() * WIDTH / innerPanel.getWidth();
                int y = e.getY() * HEIGHT / innerPanel.getHeight();
                currentFrame[y][x] = !currentFrame[y][x];   // for painting the square in any game mode
                if(!currentFrame[y][x])
                    focusedPlayer = null;
                game.setMap(y, x);
                innerPanel.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                focusedPlayer = null;
                innerPanel.repaint();
            }
        });

        outerPanel.add(innerPanel);
    }
}