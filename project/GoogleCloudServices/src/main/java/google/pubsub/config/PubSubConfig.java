package google.pubsub.config;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PubSubConfig loads configuration settings for Pub/Sub from a properties file.
 */
public class PubSubConfig {
    private final String projectId;
    private final String topicId;
    private final String loggingAppSubscriptionID;
    private final String labelsAppSubscriptionID;
    private final ExecutorProvider executorProvider;

    /**
     * Constructs a new PubSubConfig, loading settings from the properties file.
     *
     * @throws IOException if there is an error loading the properties file
     */
    public PubSubConfig() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/pubsub.properties")) {
            if (input == null) {
                throw new IOException("Unable to find pubsub.properties");
            }
            properties.load(input);
        }

        this.projectId = properties.getProperty("projectId");
        this.topicId = properties.getProperty("topicId");
        this.loggingAppSubscriptionID = properties.getProperty("loggingAppSubscriptionID");
        this.labelsAppSubscriptionID = properties.getProperty("labelsAppSubscriptionID");

        int executorThreadCount = Integer.parseInt(properties.getProperty("executorThreadCount"));
        this.executorProvider = InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(executorThreadCount).build();
    }

    public String getProjectId() {
        return projectId;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getLoggingAppSubscriptionID() {
        return loggingAppSubscriptionID;
    }

    public String getLabelsAppSubscriptionID() {
        return labelsAppSubscriptionID;
    }

    public ExecutorProvider getExecutorProvider() {
        return executorProvider;
    }
}

