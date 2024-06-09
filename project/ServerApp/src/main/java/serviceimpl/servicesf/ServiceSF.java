package serviceimpl.servicesf;

import com.google.cloud.Timestamp;
import google.cloudstorage.CloudStorageService;
import google.firestore.FirestoreService;
import google.firestore.models.ImageInformation;
import google.pubsub.service.PubSubService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import servicesf.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * gRPC service implementation for handling image submissions and image details retrieval.
 */
public class ServiceSF extends ServiceSFGrpc.ServiceSFImplBase {

    private final CloudStorageService cs;
    private final FirestoreService fs;
    private final PubSubService pubSubService;

    private final Logger logger = Logger.getLogger(ServiceSF.class.getName());

    /**
     * Constructor to initialize required services.
     *
     * @param port The port number for the service.
     */
    public ServiceSF(int port) throws IOException {
        this.cs = new CloudStorageService();
        this.fs = new FirestoreService();
        this.pubSubService = new PubSubService();
    }

    @Override
    public StreamObserver<ImageSubmissionRequest> submitImage(StreamObserver<ImageSubmissionResponse> responseObserver) {
        return new StreamObserver<>() {
            private final String uniqueBlobId = cs.generateUniqueBlobName();
            private String blobLink = "";

            @Override
            public void onNext(ImageSubmissionRequest imageSubmissionRequest) {
                blobLink = cs.storeImageBytes(imageSubmissionRequest.getImageChunk().toByteArray(), uniqueBlobId);
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, t.getMessage());
                responseObserver.onError(Status.INTERNAL.withDescription(t.getMessage()).asRuntimeException());
            }

            @Override
            public void onCompleted() {
                try {
                    pubSubService.publishMessage(blobLink, "cn-tp-g09-bucket", uniqueBlobId);
                } catch (IOException | InterruptedException e) {
                    logger.log(Level.WARNING, e.getMessage());
                    responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
                }
                responseObserver.onNext(ImageSubmissionResponse.newBuilder().setUniqueId(blobLink).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void getImageDetails(ImageDetailsRequest request, StreamObserver<ImageDetailsResponse> responseObserver) {

        try {
            ImageInformation imageInfo = fs.getImageInfo(request.getUniqueId());

            ImageDetailsResponse.Builder responseBuilder = ImageDetailsResponse.newBuilder();

            // Add characteristics
            responseBuilder.addAllCharacteristics(imageInfo.getVisionInfo());

            // Add translations
            responseBuilder.addAllTranslations(imageInfo.getTranslationInfo());

            Timestamp timestamp = imageInfo.getTimestamp();

            // Add processed date (if available)
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String formattedDate = sdf.format(date);
                responseBuilder.setProcessedDate(formattedDate);
            }

            // Build the response
            ImageDetailsResponse response = responseBuilder.build();

            // Send the response back to the client
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAllFiles(AllFilesWithRequest request, StreamObserver<AllFilesWithResponse> responseObserver) {
        try {
            List<String> files = fs.getImageFileNameBetweenCertainDatesAndWith(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getCharacteristic()
            );

            AllFilesWithResponse response = AllFilesWithResponse.newBuilder()
                    .addAllFileNames(files)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ExecutionException | InterruptedException | ParseException e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}