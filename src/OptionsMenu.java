import javax.swing.*;
import java.awt.ComponentOrientation;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OptionsMenu {

	private JFrame frame;	// the GUI frame
//	private JTextField textMeetingDistance;		// TO BE DELETED
	private JTextField textNStartingPlayers;	// number of starting players iff checkBoxRandom is true
	private JCheckBox checkBoxRandom;			// check if number of starting player is random or fixed from the above variable
	private JTextField textWidth;
	private JTextField textHeight;
	private Game game;
	private GUI gui;
    public static int meetingDistance = 5;	// let's make it final? and move it from here
    public static boolean random = false;	// maybe redundant
	protected static boolean fullSim = true;

	/**
	 * Constructor.
	 * Create the application.
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
	
	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	/**
	 * Instantiate the GUI and the Game. Then runs the GUI and the Game as two different threads.
	 */
	protected void createGuiAndGame() {
		if (gui != null) {
			gui.getFrame().dispose();
		}
		gui = new GUI(this);

		if (fullSim) {
    		game = new SocialGameSystem(gui);
    	}
    	else {
    		game = new GameOfLife(gui);
    	}
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

		/*
		// to change in a checkBox or similar from here
		JLabel lblMeetingdistance = new JLabel("Meeting distance :");
		lblMeetingdistance.setBounds(101, 84, 136, 16);
		frame.getContentPane().add(lblMeetingdistance);
		
		textMeetingDistance = new JTextField();
		textMeetingDistance.setBounds(249, 79, 50, 26);
		frame.getContentPane().add(textMeetingDistance);
		textMeetingDistance.setColumns(10);
		// to here
		*/

		JButton gameType = new JButton("Game Of Life");
		gameType.setBounds(100, 79, 198, 26);
		gameType.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!fullSim) {
					fullSim = true;
					gameType.setText("Game Of Life");
				}
				else {
					fullSim = false;
					gameType.setText("Ant Simulator");
				}
			}
		});
		frame.getContentPane().add(gameType);

		checkBoxRandom = new JCheckBox("Random", false);
		checkBoxRandom.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        checkBoxRandom.addChangeListener(e -> {
			if(checkBoxRandom.isSelected()) {
				textNStartingPlayers.setEnabled(true);
				random = true;
				SocialGameSystem.random = true;
			}
			else {
				textNStartingPlayers.setEnabled(false);
				random = false;
				SocialGameSystem.random = false;
			}
		});
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


		JButton btnApply = new JButton("Apply");
		btnApply.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					if(!textWidth.getText().isEmpty()) {
						GUI.WIDTH = Integer.parseInt(textWidth.getText());
					}
					if(!textHeight.getText().isEmpty()) {
						GUI.HEIGHT = Integer.parseInt(textHeight.getText());
						
					}
				/*	if(!textMeetingDistance.getText().isEmpty()) {
						meetingDistance = Integer.parseInt(textMeetingDistance.getText());
						SocialGameSystem.meeting_distance = Integer.parseInt(textMeetingDistance.getText());
					}*/
					if(!textNStartingPlayers.getText().isEmpty()) {
						SocialGameSystem.n_starting_players = Integer.parseInt(textNStartingPlayers.getText());
					}
					SocialGameSystem.meeting_distance = meetingDistance;
					createGuiAndGame();
					frame.setVisible(false);
				}
			}
		});
		btnApply.setBounds(101, 152, 117, 29);
		frame.getContentPane().add(btnApply);

		JLabel lblWidth = new JLabel("Set Grid Width:");
		lblWidth.setBounds(101, 28, 61, 16);
		frame.getContentPane().add(lblWidth);
		
		textWidth = new JTextField();
		textWidth.setBounds(249, 23, 50, 26);
		frame.getContentPane().add(textWidth);
		textWidth.setColumns(10);

		JLabel lblHeight = new JLabel("Set Grid Height:");
		lblHeight.setBounds(101, 56, 61, 16);
		frame.getContentPane().add(lblHeight);

		textHeight = new JTextField();
		textHeight.setBounds(249, 51, 50, 26);
		frame.getContentPane().add(textHeight);
		textHeight.setColumns(10);
	}
}