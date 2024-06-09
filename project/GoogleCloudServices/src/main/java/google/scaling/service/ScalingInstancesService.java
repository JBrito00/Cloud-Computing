package google.scaling.service;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.Operation;
import google.scaling.config.ScalingInstancesConfig;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ScalingInstancesService handles the scaling of server instances
 * for different instance groups within a Google Cloud project.
 */
public class ScalingInstancesService {
    private final Logger logger = Logger.getLogger(ScalingInstancesService.class.getName());
    private final String project;
    private final String zone;
    private final String ServerInstancesGroupName;
    private final String ImageProcessingInstancesGroupName;

    /**
     * Constructs a new ScalingInstancesService.
     *
     * @throws IOException if there is an error loading the configuration or properties
     */
    public ScalingInstancesService() throws IOException {
        ScalingInstancesConfig sIC = new ScalingInstancesConfig();
        this.project = sIC.getProjectId();
        this.zone = sIC.getZone();
        ServerInstancesGroupName = sIC.getServerInstancesGroupName();
        ImageProcessingInstancesGroupName = sIC.getImageProcessingInstancesGroupName();
    }

    /**
     * Scales the server instances to the specified number.
     *
     * @param numInstances the desired number of instances
     * @return true if the scaling operation was successful, false otherwise
     */
    public boolean scaleServerInstancesLogic(int numInstances) {
        return scaleInstances(ServerInstancesGroupName, numInstances);
    }

    /**
     * Scales the image processing instances to the specified number.
     *
     * @param numInstances the desired number of instances
     * @return true if the scaling operation was successful, false otherwise
     */
    public boolean scaleImageProcessingInstancesLogic(int numInstances) {
        return scaleInstances(ImageProcessingInstancesGroupName, numInstances);
    }


    /**
     * Scales the instances in the specified instance group to the desired number.
     *
     * @param instanceGroupName the name of the instance group
     * @param numInstances the desired number of instances
     * @return true if the scaling operation was successful, false otherwise
     */
    private boolean scaleInstances(String instanceGroupName, int numInstances){
        try (InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create()) {
            OperationFuture<Operation, Operation> result = managersClient.resizeAsync(project, zone, instanceGroupName, numInstances);

            // Wait for the operation to complete and get the result
            Operation operation = result.get();
            if (operation != null) {
                logger.log(Level.INFO, "Successfully resized instances to " + numInstances);
                return true; // Operation completed successfully
            } else {
                logger.log(Level.WARNING, "Instance scaling failed");
                return false; // Operation failed
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, "Instance scaling failed with exception: " + e.getMessage());
            return false;
        }
    }
}

