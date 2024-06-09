package serviceimpl.servicesg;

import google.scaling.service.ScalingInstancesService;
import io.grpc.stub.StreamObserver;
import servicesg.*;

import java.io.IOException;

/**
 * gRPC service implementation for scaling server instances and image processing instances.
 */
public class ServiceSG extends ServiceSGGrpc.ServiceSGImplBase {

    private final ScalingInstancesService si;

    /**
     * Constructor to initialize the ScalingInstancesService.
     *
     * @param port The port number for the service (not used in this implementation).
     */
    public ServiceSG(int port) throws IOException {
        this.si = new ScalingInstancesService();
    }

    @Override
    public void scaleServerInstances(ScaleServerInstancesRequest request, StreamObserver<ScaleServerInstancesResponse> responseObserver) {
        int numInstances = request.getNumInstances();

        boolean success = si.scaleServerInstancesLogic(numInstances);

        String message = success ? "Successfully scaled server instances to " + numInstances
                : "Failed to scale server instances to " + numInstances;

        ScaleServerInstancesResponse response = ScaleServerInstancesResponse.newBuilder()
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void scaleImageProcessorsInstances(ScaleImageProcessorRequest request, StreamObserver<ScaleImageProcessorResponse> responseObserver) {
        int numInstances = request.getNumInstances();

        boolean success = si.scaleImageProcessingInstancesLogic(numInstances);

        String message = success ? "Successfully scaled image processing instances to " + numInstances
                : "Failed to scale image procesing instances to " + numInstances;

        ScaleImageProcessorResponse response = ScaleImageProcessorResponse.newBuilder()
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
