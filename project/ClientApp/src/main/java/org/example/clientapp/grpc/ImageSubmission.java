package org.example.clientapp.grpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import servicesf.ImageSubmissionRequest;
import servicesf.ImageSubmissionResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Class for handling image submission.
 */
public class ImageSubmission {

    /**
     * Submits an image to the server for processing.
     *
     * @param grpcClient    The gRPC client for making requests.
     * @param operationLatch A latch to wait for the operation to complete.
     * @param scan          Scanner object for user input.
     */
    public static void submitImage(GrpcClient grpcClient, CountDownLatch operationLatch, Scanner scan) {
        try {
            System.out.print("Enter the path to the image file: ");
            String imagePathString = scan.next();
            Path imagePath = Paths.get(imagePathString);
            byte[] imageBytes = Files.readAllBytes(imagePath);

            // Create a stream observer to handle the response from the server
            StreamObserver<ImageSubmissionResponse> responseObserver = createResponseObserver(operationLatch);

            // Send the image bytes to the server
            StreamObserver<ImageSubmissionRequest> requestObserver = grpcClient.getStubSF().submitImage(responseObserver);
            requestObserver.onNext(ImageSubmissionRequest.newBuilder().setImageChunk(ByteString.copyFrom(imageBytes)).build());
            requestObserver.onCompleted();

            // Wait for the operation to complete
            operationLatch.await();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error reading image file: " + e.getMessage());
        }
    }

    /**
     * Creates a response observer to handle responses from the server.
     *
     * @param operationLatch A latch to wait for the operation to complete.
     * @return A StreamObserver for ImageSubmissionResponse.
     */
    private static StreamObserver<ImageSubmissionResponse> createResponseObserver(CountDownLatch operationLatch) {
        return new StreamObserver<ImageSubmissionResponse>() {
            @Override
            public void onNext(ImageSubmissionResponse response) {
                System.out.println("Image submitted successfully. Unique ID: " + response.getUniqueId());
                operationLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error submitting image: " + t.getMessage());
                operationLatch.countDown();
            }

            @Override
            public void onCompleted() {
                // The server has completed processing the request
            }
        };
    }
}
