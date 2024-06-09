package org.example.clientapp.grpc;

import org.example.clientapp.grpc.GrpcClient;
import servicesf.AllFilesWithRequest;
import servicesf.AllFilesWithResponse;

import java.util.Iterator;
import java.util.Scanner;

/**
 * Class for handling retrieval of file names between two dates.
 */
public class FileNames {

    /**
     * Retrieves and displays file names between two dates.
     *
     * @param grpcClient The gRPC client for making requests.
     * @param scan       Scanner object for user input.
     */
    public static void getNamesBetweenDates(GrpcClient grpcClient, Scanner scan) {
        System.out.print("Enter the start date (dd-MM-yyyy): ");
        String startDate = scan.next();
        System.out.print("Enter the end date (dd-MM-yyyy): ");
        String endDate = scan.next();
        System.out.print("Enter the characteristic: ");
        String characteristic = scan.next();

        AllFilesWithRequest allFilesWithRequest = AllFilesWithRequest.newBuilder()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setCharacteristic(characteristic)
                .build();

        try {
            Iterator<AllFilesWithResponse> responseIterator = grpcClient.getBlockingStubSF().getAllFiles(allFilesWithRequest);

            // Process each response in the stream
            while (responseIterator.hasNext()) {
                AllFilesWithResponse res = responseIterator.next();
                // Print or process the file names from the response
                System.out.println("Received file names: " + res.getFileNamesList());
            }
        } catch (Exception e) {
            System.out.println("Error fetching file names: " + e.getMessage());
        }
    }
}
