package google.scaling.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ScalingInstancesConfig loads configuration settings for scaling instances from a properties file.
 */
public class ScalingInstancesConfig {
    private final String projectId;
    private final String zone;
    private final String serverInstancesGroupName;
    private final String imageProcessingInstancesGroupName;

    /**
     * Constructs a new ScalingInstancesConfig, loading settings from the properties file.
     *
     * @throws IOException if there is an error loading the properties file
     */
    public ScalingInstancesConfig() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/scaling.properties")) {
            if (input == null) {
                throw new IOException("Unable to find pubsub.properties");
            }
            properties.load(input);
        }

        this.projectId = properties.getProperty("projectId");
        this.zone = properties.getProperty("zone");
        this.serverInstancesGroupName = properties.getProperty("serverInstancesGroupName");
        this.imageProcessingInstancesGroupName = properties.getProperty("imageProcessingInstancesGroupName");
    }

    public String getProjectId() {
        return projectId;
    }

    public String getZone() {
        return zone;
    }

    public String getServerInstancesGroupName() {
        return serverInstancesGroupName;
    }

    public String getImageProcessingInstancesGroupName() {
        return imageProcessingInstancesGroupName;
    }
}