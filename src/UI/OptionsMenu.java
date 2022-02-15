package UI;

import Ants.AntSimulator;
import Interfaces.Game;
import GOL.*;
import SGS.SocialGameSystem;

import javax.swing.*;
import java.awt.ComponentOrientation;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * The Class responsible for managing the Menu of the Options.
 * In reality an instance of this class runs on a thread that is the father of all successive threads.
 * <p>
 * In particular from the thread associated with this class are created and forked the two main
 * threads that run the UI.GUI and an Interfaces.Game Interface instance.
 */
public class OptionsMenu {

	private JFrame frame;

	/**
	 * number of starting players iff checkBoxRandom is true
	 */
	private JTextField textNStartingPlayers;

	/**
	 * check if number of starting player is random or fixed from the above variable
	 */
	private JCheckBox checkBoxRandom;
	private JTextField textWidth;
	private JTextField textHeight;

	/**
	 * an instance of Interface.Game interface
	 */
	private Game game;

	/**
	 * the UI.GUI this object creates as a thread child
	 */
	private GUI gui;

	/**
	 * The enum that specifies all the CAS modelled
	 */
	protected enum CAS {AntSimulator, SocialGameSystem, GameOfLIfe}

	/**
	 * control which CAS to simulate
	 */
	protected static CAS CAS_type = CAS.AntSimulator;

	/**
	 * Constructor called ONLY by the Launcher.Launcher
	 */
	public OptionsMenu() {
		initialize();
	}

	/**
	 * @return the Options frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Connects this class with the interface Interfaces.Game
	 * @param game: instance of Interfaces.Game interface
	 */
	private void setGame(Game game) {
		this.game = game;
	}

	/**
	 * Instantiate the UI.GUI and one of three possible instances of Interfaces.Game interface.
     * Then runs the UI.GUI and the Interfaces.Game as two different threads.
	 */
	public void createGuiAndGame() {
		if (gui != null) {
			gui.getFrame().dispose();
		}
		gui = new GUI(this);

        if (CAS_type == CAS.AntSimulator) {
            game = new AntSimulator(gui);
        }
        else if (CAS_type == CAS.SocialGameSystem) {
    		game = new SocialGameSystem(gui);
    	}
    	else if (CAS_type == CAS.GameOfLIfe){
    		game = new GameOfLife(gui);
    	}
		this.setGame(game);
		gui.setGame(game);
    	game.startGame();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Options Menu");
		frame.setResizable(false);
		frame.setBounds(100, 100, 349, 296);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setVisible(false);

		JButton gameType = new JButton("Social Game System");
		gameType.setBounds(100, 79, 198, 26);
		gameType.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
                if (Objects.equals(gameType.getText(), "Ant Simulator")) {
                    gameType.setText("Social Game System");
                }
				else if (Objects.equals(gameType.getText(), "Social Game System")) {
					gameType.setText("Game Of Life");
				}
				else if (Objects.equals(gameType.getText(), "Game Of Life")) {
					gameType.setText("Ant Simulator");
				}
			}
		});
		frame.getContentPane().add(gameType);

		checkBoxRandom = new JCheckBox("Random");
		checkBoxRandom.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        checkBoxRandom.addChangeListener(e -> textNStartingPlayers.setEnabled(checkBoxRandom.isSelected()));
		checkBoxRandom.setBounds(6, 109, 83, 23);
		frame.getContentPane().add(checkBoxRandom);

		JLabel lblNStartingPlayers = new JLabel("Players initially alive :");
		lblNStartingPlayers.setBounds(101, 112, 135, 16);
		frame.getContentPane().add(lblNStartingPlayers);

		textNStartingPlayers = new JTextField();
		textNStartingPlayers.setBounds(249, 107, 49, 26);
		frame.getContentPane().add(textNStartingPlayers);
		textNStartingPlayers.setColumns(10);
		textNStartingPlayers.setEnabled(false);


		JLabel lblWidth = new JLabel("Set Grid Width:");
		lblWidth.setBounds(101, 28, 120, 16);
		frame.getContentPane().add(lblWidth);

		textWidth = new JTextField();
		textWidth.setBounds(249, 23, 50, 26);
		frame.getContentPane().add(textWidth);
		textWidth.setColumns(10);

		JLabel lblHeight = new JLabel("Set Grid Height:");
		lblHeight.setBounds(101, 56, 120, 16);
		frame.getContentPane().add(lblHeight);

		textHeight = new JTextField();
		textHeight.setBounds(249, 51, 50, 26);
		frame.getContentPane().add(textHeight);
		textHeight.setColumns(10);

		JButton btnApply = new JButton("Apply");
		btnApply.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					// get text from gameType button and set CAS_type accordingly, remember that the button shows the NEXT CAS type not the current one
					if (!textWidth.getText().isEmpty()) {
						GUI.WIDTH = Integer.parseInt(textWidth.getText());
					}
					if (!textHeight.getText().isEmpty()) {
						GUI.HEIGHT = Integer.parseInt(textHeight.getText());
					}
                    if (Objects.equals(gameType.getText(), "Ant Simulator")) {
                        CAS_type = CAS.GameOfLIfe;
                    }
                    else if (Objects.equals(gameType.getText(), "Social Game System")) {
                        CAS_type = CAS.AntSimulator;
                    }
                    else if (Objects.equals(gameType.getText(), "Game Of Life")){
                        CAS_type = CAS.SocialGameSystem;
                    }
                    createGuiAndGame();
                    game.setRandom(checkBoxRandom.isSelected());  // to be executed after "createGuiandGame" if you want to enable random from the very start for every game
					if (!textNStartingPlayers.getText().isEmpty()) {
						game.setStartingRandomAgents(Integer.parseInt(textNStartingPlayers.getText()));
					}
					frame.setVisible(false);
				}
			}
		});
		btnApply.setBounds(101, 152, 117, 29);
		frame.getContentPane().add(btnApply);
	}
}