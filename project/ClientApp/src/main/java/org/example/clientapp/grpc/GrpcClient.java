package org.example.clientapp.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import servicesf.ServiceSFGrpc;
import servicesg.ServiceSGGrpc;

/**
 * gRPC client class for managing the gRPC channel and stubs.
 */
public class GrpcClient {
    private final ManagedChannel channel;
    private final ServiceSFGrpc.ServiceSFStub stubSF;
    private final ServiceSFGrpc.ServiceSFBlockingStub blockingStubSF;
    private final ServiceSGGrpc.ServiceSGBlockingStub blockingStubSG;

    /**
     * Constructs a GrpcClient with the given IP address.
     *
     * @param ip The IP address of the gRPC server.
     */
    public GrpcClient(String ip) {
        this.channel = ManagedChannelBuilder.forAddress(ip, 8000).usePlaintext().build();
        this.stubSF = ServiceSFGrpc.newStub(channel);
        this.blockingStubSF = ServiceSFGrpc.newBlockingStub(channel);
        this.blockingStubSG = ServiceSGGrpc.newBlockingStub(channel);
    }

    public ServiceSFGrpc.ServiceSFStub getStubSF() {
        return stubSF;
    }

    public ServiceSFGrpc.ServiceSFBlockingStub getBlockingStubSF() {
        return blockingStubSF;
    }

    public ServiceSGGrpc.ServiceSGBlockingStub getBlockingStubSG() {
        return blockingStubSG;
    }

    /**
     * Shuts down the gRPC channel.
     */
    public void shutdown() {
        channel.shutdown();
    }
}
