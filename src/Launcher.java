import java.awt.EventQueue;

/**
 * The launcher for this application.
 * It instantiates OptionsMenu which then instantiates the GUI and a Game
 */
public class Launcher {

    public static void main(String[] args) {
        EventQueue.invokeLater( () -> {
            try {
                OptionsMenu optionsMenu = new OptionsMenu();
                optionsMenu.createGuiAndGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } );
    }
}