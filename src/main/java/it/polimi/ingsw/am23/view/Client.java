package it.polimi.ingsw.am23.view;

import it.polimi.ingsw.am23.view.cli.CLIView;
import it.polimi.ingsw.am23.view.gui.JavaFXView;

import java.util.Scanner;

public class Client{

    public static void main(String[] args) {
        System.out.println("Scegli UI:");
        System.out.println("1. CLI");
        System.out.println("2. GUI");

        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (true) {
            System.out.print("Inserisci 1 o 2: ");
            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                choice = 1;
                break;
            } else if (input.equals("2")) {
                choice = 2;
                break;
            } else {
                System.out.println("Scelta non valida. Inserisci 1 per CLI o 2 per GUI.");
            }
        }

        if (choice == 1) {
            System.out.println("Avvio della CLI in corso...");
            try {
                CLIView.main(args);
            } catch (Exception e) {
                System.err.println("Errore durante l'esecuzione della CLI:");
            }
        } else {
            System.out.println("Avvio della GUI in corso...");
            JavaFXView.main(args);
        }
    }
}