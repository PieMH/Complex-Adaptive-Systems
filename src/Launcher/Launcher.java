package Launcher;

import UI.OptionsMenu;

import java.awt.EventQueue;

/**
 * The launcher for this application.
 * It instantiates UI.OptionsMenu which then instantiates the UI.GUI and an Interfaces.Game
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