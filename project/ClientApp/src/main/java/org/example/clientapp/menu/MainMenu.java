package org.example.clientapp.menu;

import java.util.Scanner;

/**
 * Main menu class for displaying the main menu options.
 */
public class MainMenu {
    /**
     * Displays the main menu and returns the selected option.
     *
     * @param scan Scanner object for user input.
     * @return The selected option.
     */
    public static int display(Scanner scan) {
        return SubMenu.display("MENU", new String[]{
                "Operações funcionais (SF)",
                "Operações para gestão de elasticidade (SG)",
                "Exit"
        }, scan);
    }
}