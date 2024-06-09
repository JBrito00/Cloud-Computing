package org.example.clientapp;

import org.example.clientapp.grpc.GrpcClient;
import org.example.clientapp.menu.MainMenu;
import org.example.clientapp.services.ElasticityOperations;
import org.example.clientapp.services.FunctionalOperations;
import org.example.clientapp.utils.IpFetcher;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class for the client application.
 * Handles the main loop for IP selection and menu navigation.
 */
public class ClientApp {
    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        IpFetcher iF = new IpFetcher();
        while (true) {
            // Fetch IP addresses at the start
            List<String> ips = iF.fetchIPAddresses();
            if (ips.isEmpty()) {
                System.out.println("No IP addresses found. Exiting...");
                return;
            }

            // Display IP addresses and let the user choose one
            String chosenIP = IpFetcher.chooseIPAddress(ips, scan);
            GrpcClient grpcClient = new GrpcClient(chosenIP);

            boolean exitToIPSelection = false;
            while (!exitToIPSelection) {
                int option = MainMenu.display(scan);
                switch (option) {
                    case 0:
                        FunctionalOperations.handle(grpcClient, scan);
                        break;
                    case 1:
                        ElasticityOperations.handle(grpcClient, scan);
                        break;
                    case 2:
                        grpcClient.shutdown();
                        exitToIPSelection = true;
                        break;
                    default:
                        System.out.println("Invalid Option!");
                }
            }
        }
    }
}