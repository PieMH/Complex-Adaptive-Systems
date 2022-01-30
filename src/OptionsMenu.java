import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.ComponentOrientation;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OptionsMenu {

	private JFrame frame;
	private JTextField textMeetingDistance;
	private JTextField textNStartingPlayers;
	private JCheckBox chckbxRandom;
	private JLabel lblNStartingPlayers;
	private JLabel lblMeetingdistance; 
	private JButton btnApply;
	private JTextField textWidth;
	private JTextField textHeight;
	private JLabel lblHeight; 
	private JLabel lblWidth;
	private Game gioco;
	private GUI gui;
    public static int meetingDistance = 5;
    public static boolean random = false;

	/**
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
	
	public void createGuiAndGame() {
		if(gui != null) {
			gui.getFrame().dispose();
		}
		gui = new GUI(this);
		
		if(meetingDistance == 1) {
    		gioco = new GameOfLife(gui);
    	}
    	else {
    		gioco = new SocialGameSystem(gui);
    	}
		gui.setGioco(gioco);
    	gioco.startGame();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 349, 296);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setVisible(false);
		
		lblMeetingdistance = new JLabel("Meeting distance :");
		lblMeetingdistance.setBounds(101, 84, 136, 16);
		frame.getContentPane().add(lblMeetingdistance);
		
		textMeetingDistance = new JTextField();
		textMeetingDistance.setBounds(249, 79, 50, 26);
		frame.getContentPane().add(textMeetingDistance);
		textMeetingDistance.setColumns(10);
		
		lblNStartingPlayers = new JLabel("Players initially alive :");
		lblNStartingPlayers.setBounds(101, 112, 136, 16);
		frame.getContentPane().add(lblNStartingPlayers);
		
		textNStartingPlayers = new JTextField();
		textNStartingPlayers.setBounds(249, 107, 50, 26);
		frame.getContentPane().add(textNStartingPlayers);
		textNStartingPlayers.setColumns(10);
		
		chckbxRandom = new JCheckBox("Random");
		chckbxRandom.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        chckbxRandom.addChangeListener(e -> {
			if(chckbxRandom.isSelected()) {
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
      
		chckbxRandom.setBounds(6, 108, 83, 23);
		frame.getContentPane().add(chckbxRandom);
		
		btnApply = new JButton("Apply");
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
					if(!textMeetingDistance.getText().isEmpty()) {
						meetingDistance = Integer.parseInt(textMeetingDistance.getText());
						SocialGameSystem.meeting_distance = Integer.parseInt(textMeetingDistance.getText());
					}
					if(!textNStartingPlayers.getText().isEmpty()) {
						SocialGameSystem.n_starting_players = Integer.parseInt(textNStartingPlayers.getText());
					}
					createGuiAndGame();
					frame.setVisible(false);
				}
			}
		});
		btnApply.setBounds(101, 152, 117, 29);
		frame.getContentPane().add(btnApply);
		
		lblWidth = new JLabel("Width :");
		lblWidth.setBounds(101, 28, 61, 16);
		frame.getContentPane().add(lblWidth);
		
		lblHeight = new JLabel("Height :");
		lblHeight.setBounds(101, 56, 61, 16);
		frame.getContentPane().add(lblHeight);
		
		textWidth = new JTextField();
		textWidth.setBounds(249, 23, 50, 26);
		frame.getContentPane().add(textWidth);
		textWidth.setColumns(10);
		
		textHeight = new JTextField();
		textHeight.setBounds(249, 51, 50, 26);
		frame.getContentPane().add(textHeight);
		textHeight.setColumns(10);
	}
}
