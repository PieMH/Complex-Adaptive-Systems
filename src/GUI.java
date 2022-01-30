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
import javax.swing.JSlider;
import javax.swing.JLabel;

public class GUI {

    static int WIDTH = 80;
    static int HEIGHT = 40;
    static int DIMENSION = WIDTH * HEIGHT;
    private JFrame frame;
    private JPanel innerPanel;
    private JPanel outerPanel;
    private JButton btnOptions;
    boolean[][] currentFrame = new boolean[HEIGHT][WIDTH];
    boolean[][] nextFrame = new boolean[HEIGHT][WIDTH];
    boolean play = false;
    private OptionsMenu optionsMenu;
    private JSlider slider;
    private Game gioco;
    private JLabel lblGameSpeed;
    private Giocatore focusedPlayer;
    private boolean displayAcquaintance = false;
    private boolean showLife = false;
    private boolean showWellness = false;
    private JButton btnReset;

    /**
     * Creates the application.
     */
    GUI(OptionsMenu optionsMenu) {
        this.optionsMenu = optionsMenu;
        initialize();
    }

    /**
     * Serve a far interfacciare più facilmente le classi GUI e TheGame
     * @param gioco: un istanza della classe TheGame
     */

    void setGioco(Game gioco) {
        this.gioco = gioco;
    }


    /**
     * Returns the main frame.
     */
    JFrame getFrame() {
        return frame;

    }

    public void resetFrames() {
        currentFrame = new boolean[HEIGHT][WIDTH];
    }

    /**
     * Returns the panel.
     */
    JPanel getPanel() {
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

        btnReset = new JButton("Reset");

        btnReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentFrame = new boolean[HEIGHT][WIDTH];
                gioco.resetMap();
                innerPanel.repaint();
            }
        });

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

        outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(Color.WHITE);

        btnOptions = new JButton("Options");
        btnOptions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    optionsMenu.getFrame().setVisible(!optionsMenu.getFrame().isVisible());
                }
            }
        });

        slider = new JSlider(10, 200, 100);

        slider.addChangeListener(e -> {
            if(!slider.getValueIsAdjusting()) {
                gioco.getTimer().setDelay(slider.getValue() + 1);
            }
        });

        lblGameSpeed = new JLabel("Game speed :");

        JButton btnShowLife = new JButton("Show Life");
        btnShowLife.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLife = !showLife;
                innerPanel.repaint();
            }
        });
        
        JButton btnShowWellness = new JButton("Show wellness");
        btnShowWellness.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		showWellness = !showWellness;
//        		System.out.println(showWellness);
        		innerPanel.repaint();
        	}
        });

        //************************************************************************************************************************************************************************/

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
        					.addComponent(lblGameSpeed)
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
        					.addComponent(lblGameSpeed))
        				.addComponent(slider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap())
        );
        //************************************************************************************************************************************************************************/

        frame.getContentPane().setLayout(groupLayout);

        innerPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if(OptionsMenu.meetingDistance == 1) {
                    paintOneColorPixel(g);
                }
                else {
                    if(focusedPlayer != null) {
                        paintAcquaintanceBasedPixels(g);
                    }
                    else {
                        if(showLife)
                            paintOneColorPixel(g);
                        else if(showWellness)
                        	paintOneColorPixel(g);
                        else
                        	paintPersonalityBasedPixel(g);
                    }
                }

                g.setColor(Color.BLACK);

                for(int i = 0; i <= GUI.HEIGHT; i++) {
                    int y = i * getHeight() / GUI.HEIGHT;
                    g.drawLine(0, y, getWidth(), y);
                }

                for( int j = 0; j <= GUI.WIDTH; j++) {
                    int x = j * getWidth() / GUI.WIDTH;
                    g.drawLine(x, 0, x, getHeight() );
                }
            }

            private void paintOneColorPixel(Graphics g) {
                for(int i = 0; i < GUI.HEIGHT; i++) {
                    for(int j = 0; j < GUI.WIDTH; j++) {
                        if(showLife) {
                            if(currentFrame[i][j]) {
                                g.setColor(new Color(0, 128, 0, ((gioco.getCurrentAlive().get(SocialGameSystem.key(i, j)).getLife()) * 255 / 100)));
                                g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                            }
                            else {
                                g.setColor(innerPanel.getBackground());
                                g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                            }
                        }

                        else if (showWellness) {
                            if(currentFrame[i][j]) {
                            	g.setColor(new Color(148, 0, 211, ((int)(Double.min(Double.max(((gioco.getCurrentAlive().get(SocialGameSystem.key(i, j)).getWellness())), 0), 100))) * 255 / 100));
                            	g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                            }
                            else {
                                g.setColor(innerPanel.getBackground());
                                g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                            }
                        }

                        else {
                            if(currentFrame[i][j]) {
                                g.setColor(Color.BLUE);
                                g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                            }
                            else {
                                g.setColor(innerPanel.getBackground());
                                g.fillRect(j * innerPanel.getWidth() / GUI.WIDTH , i * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                            }
                        }
                    }
                }
            }

            private void paintPersonalityBasedPixel(Graphics g) {
                for(int i = 0; i < GUI.HEIGHT; i++) {
                    for(int j = 0; j < GUI.WIDTH; j++) {
                        if(currentFrame[i][j]) {
                            g.setColor(gioco.getCurrentAlive().get(SocialGameSystem.key(i, j)).carattere.getMyColor());
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
                            g.setColor(gioco.getCurrentAlive().get(SocialGameSystem.key(y, x)).carattere.getMyColor());
                            g.fillRect(x * innerPanel.getWidth() / GUI.WIDTH , y * innerPanel.getHeight() / GUI.HEIGHT, innerPanel.getWidth() / GUI.WIDTH, innerPanel.getHeight() / GUI.HEIGHT);
                        }
                    }
                }
                int x = focusedPlayer.x_position;
                int y = focusedPlayer.y_position;
                g.setColor(gioco.getCurrentAlive().get(SocialGameSystem.key(y, x)).carattere.getMyColor());
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
                    if(!currentFrame[y][x])
                        currentFrame[y][x] = true;
                    gioco.setMap(y, x);
                    innerPanel.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if(!play && OptionsMenu.meetingDistance > 1) {
                    int x = e.getX() * WIDTH / innerPanel.getWidth();
                    int y = e.getY() * HEIGHT / innerPanel.getHeight();
                    callRepaint(y, x);
                }
            }

            void callRepaint(int y, int x) {
                focusedPlayer = gioco.getCurrentAlive().get(SocialGameSystem.key(y, x));
                innerPanel.repaint();
            }
        });


        innerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!displayAcquaintance) {
                    int x = e.getX() * WIDTH / innerPanel.getWidth();
                    int y = e.getY() * HEIGHT / innerPanel.getHeight();
                    currentFrame[y][x] = !currentFrame[y][x];
                    if(!currentFrame[y][x])
                        focusedPlayer = null;
                    gioco.setMap(y, x);
                    innerPanel.repaint();
                }
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