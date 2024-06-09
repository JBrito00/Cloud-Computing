package org.example.clientapp.services;

import org.example.clientapp.grpc.FileNames;
import org.example.clientapp.grpc.GrpcClient;
import org.example.clientapp.grpc.ImageDetails;
import org.example.clientapp.grpc.ImageSubmission;
import org.example.clientapp.menu.SubMenu;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Class for handling functional operations (SF).
 */
public class FunctionalOperations {

    /**
     * Handles the functional operations menu and executes the selected option.
     *
     * @param grpcClient The gRPC client for making requests.
     * @param scan       Scanner object for user input.
     */
    public static void handle(GrpcClient grpcClient, Scanner scan) {
        int subOption = SubMenu.display("Operações funcionais (SF)", new String[]{
                "Submit a new image to detect characteristics",
                "Obtain characteristics and date of an image by its unique ID",
                "Obtain names of files in the system between two dates",
                "Exit"
        }, scan);

        CountDownLatch operationLatch = new CountDownLatch(1);

        switch (subOption) {
            case 0:
                ImageSubmission.submitImage(grpcClient, operationLatch, scan);
                break;
            case 1:
                ImageDetails.getImageDetails(grpcClient, scan);
                break;
            case 2:
                FileNames.getNamesBetweenDates(grpcClient, scan);
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid Option!");
        }
    }
}
