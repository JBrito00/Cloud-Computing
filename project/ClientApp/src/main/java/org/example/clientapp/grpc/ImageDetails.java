package org.example.clientapp.grpc;

import org.example.clientapp.grpc.GrpcClient;
import servicesf.ImageDetailsRequest;
import servicesf.ImageDetailsResponse;

import java.util.Scanner;

/**
 * Class for handling image details retrieval.
 */
public class ImageDetails {

    /**
     * Retrieves and displays details of an image by its unique ID.
     *
     * @param grpcClient The gRPC client for making requests.
     * @param scan       Scanner object for user input.
     */
    public static void getImageDetails(GrpcClient grpcClient, Scanner scan) {
        System.out.print("Enter the unique ID of the image: ");
        String uniqueId = scan.next();

        // Create request object
        ImageDetailsRequest imageDetailsRequest = ImageDetailsRequest.newBuilder()
                .setUniqueId(uniqueId)
                .build();

        try {
            ImageDetailsResponse response = grpcClient.getBlockingStubSF().getImageDetails(imageDetailsRequest);

            System.out.println("Image Details:");
            System.out.println("Characteristics: " + response.getCharacteristicsList());
            System.out.println("Translations: " + response.getTranslationsList());
            System.out.println("Processed Date: " + response.getProcessedDate());
        } catch (Exception e) {
            System.out.println("Error fetching image details: " + e.getMessage());
        }
    }
}
