import java.awt.EventQueue;

public class GUITest {

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                OptionsMenu optionsMenu = new OptionsMenu();
                optionsMenu.createGuiAndGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}