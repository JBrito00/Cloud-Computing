package org.example.clientapp.menu;

import java.util.Scanner;

/**
 * Utility class for displaying submenus.
 */
public class SubMenu {
    /**
     * Displays a submenu with the given title and options.
     *
     * @param title   The title of the submenu.
     * @param options The options to display in the submenu.
     * @param scan    Scanner object for user input.
     * @return The selected option.
     */
    public static int display(String title, String[] options, Scanner scan) {
        int option;
        do {
            System.out.println("######## " + title + " ##########");
            for (int i = 0; i < options.length; i++) {
                System.out.println(" " + i + ": " + options[i]);
            }
            System.out.print("Enter an Option: ");
            option = scan.nextInt();
        } while (option < 0 || option >= options.length);
        return option;
    }
}
