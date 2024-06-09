package serverapp;

import io.grpc.ServerBuilder;
import serviceimpl.servicesf.ServiceSF;
import serviceimpl.servicesg.ServiceSG;
import shutdownhook.ShutdownHook;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to start the gRPC server.
 */
public class ServerApp {
    private static final Logger logger = Logger.getLogger(ServerApp.class.getName());
    private static int svcPort = 8000;

    /**
     * Main method to start the gRPC server.
     *
     * @param args Command-line arguments. The first argument can be used to specify the port.
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0) svcPort = Integer.parseInt(args[0]);
            // Create and configure the gRPC server
            io.grpc.Server svc = ServerBuilder.forPort(svcPort) // Add services
                    .addService(new ServiceSF(svcPort))
                    .addService(new ServiceSG(svcPort))
                    .build();
            // Start the server
            svc.start();

            logger.log(Level.INFO, "Server started on port " + svcPort);

            // Add a shutdown hook to handle server termination
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(svc));

            // Waits for the server to become terminated
            svc.awaitTermination();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error starting server with exception: ", ex);
        }
    }
}
