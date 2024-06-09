package org.example.clientapp.services;

import org.example.clientapp.grpc.GrpcClient;
import org.example.clientapp.menu.SubMenu;
import servicesg.ScaleImageProcessorRequest;
import servicesg.ScaleImageProcessorResponse;
import servicesg.ScaleServerInstancesRequest;
import servicesg.ScaleServerInstancesResponse;

import java.util.Scanner;

/**
 * Class for handling elasticity operations (SG).
 */
public class ElasticityOperations {

    /**
     * Handles the elasticity operations menu and executes the selected option.
     *
     * @param grpcClient The gRPC client for making requests.
     * @param scan       Scanner object for user input.
     */
    public static void handle(GrpcClient grpcClient, Scanner scan) {
        int subOption = SubMenu.display("Operações para gestão de elasticidade (SG)", new String[]{
                "Add or remove gRPC server instances",
                "Add or remove Image Processing server instances",
                "Exit"
        }, scan);

        switch (subOption) {
            case 0:
                scaleGrpcServers(grpcClient, scan);
                break;
            case 1:
                scaleImageProcessors(grpcClient, scan);
                break;
            case 2:
                return;
            default:
                System.out.println("Invalid Option!");
        }
    }

    /**
     * Scales the number of gRPC server instances.
     *
     * @param grpcClient The gRPC client for making requests.
     * @param scan       Scanner object for user input.
     */
    private static void scaleGrpcServers(GrpcClient grpcClient, Scanner scan) {
        System.out.print("Enter the number of gRPC server instances to scale to: ");
        int numServerInstances = scan.nextInt();
        ScaleServerInstancesRequest request = ScaleServerInstancesRequest.newBuilder()
                .setNumInstances(numServerInstances)
                .build();
        ScaleServerInstancesResponse response = grpcClient.getBlockingStubSG().scaleServerInstances(request);
        System.out.println(response.getMessage());
    }

    /**
     * Scales the number of image processing server instances.
     *
     * @param grpcClient The gRPC client for making requests.
     * @param scan       Scanner object for user input.
     */
    private static void scaleImageProcessors(GrpcClient grpcClient, Scanner scan) {
        System.out.print("Enter the number of Image Processing server instances to scale to: ");
        int numImageProcessingInstances = scan.nextInt();
        ScaleImageProcessorRequest request = ScaleImageProcessorRequest.newBuilder()
                .setNumInstances(numImageProcessingInstances)
                .build();
        ScaleImageProcessorResponse response = grpcClient.getBlockingStubSG().scaleImageProcessorsInstances(request);
        System.out.println(response.getMessage());
    }
}
