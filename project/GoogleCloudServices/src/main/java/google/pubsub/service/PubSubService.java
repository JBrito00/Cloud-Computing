package google.pubsub.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.core.ApiService.Listener;
import com.google.api.core.ApiService.State;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.Timestamp;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import google.pubsub.config.PubSubConfig;
import google.pubsub.util.AvroSchemaUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PubSubService is responsible for publishing and subscribing messages
 * to and from Google Cloud Pub/Sub.
 */
public class PubSubService {
    private static final Logger logger = Logger.getLogger(PubSubService.class.getName());
    private final String projectId;
    private final String topicId;
    private final String loggingAppSubscriptionID;
    private final String labelsAppSubscriptionID;
    private final ExecutorProvider executorProvider;
    private final AvroSchemaUtil avroSchemaUtil;

    /**
     * Constructs a new PubSubService, initializing configuration and schema.
     *
     * @throws IOException if there is an error loading the configuration or schema
     */
    public PubSubService() throws IOException {
        PubSubConfig config = new PubSubConfig();
        this.avroSchemaUtil = new AvroSchemaUtil();
        this.projectId = config.getProjectId();
        this.topicId = config.getTopicId();
        this.loggingAppSubscriptionID = config.getLoggingAppSubscriptionID();
        this.labelsAppSubscriptionID = config.getLabelsAppSubscriptionID();
        this.executorProvider = config.getExecutorProvider();
    }

    /**
     * Publishes a message to a Pub/Sub topic.
     *
     * @param id the ID of the message
     * @param bucketName the name of the bucket
     * @param blobName the name of the blob
     * @throws IOException if there is an error during publishing
     * @throws InterruptedException if the thread is interrupted during shutdown
     */
    public void publishMessage(String id, String bucketName, String blobName) throws IOException, InterruptedException {
        ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            ByteString data = avroSchemaUtil.createNewAvroRecord(id, bucketName, blobName);

            Timestamp timestamp = Timestamp.now();

            // Create Pub/Sub message
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(data)
                    .putAttributes("timestamp", timestamp.toString())
                    .build();

            // Publish message
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {
                @Override
                public void onFailure(Throwable t) {
                    if (t instanceof ApiException) {
                        ApiException apiException = (ApiException) t;
                        logger.log(Level.WARNING, "Error publishing message: " + apiException.getStatusCode().getCode());
                        logger.log(Level.WARNING, apiException.getMessage());
                    }
                }

                @Override
                public void onSuccess(String messageId) {
                    logger.log(Level.INFO, "Published message ID: " + messageId);
                }
            }, executorProvider.getExecutor());
        } finally {
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }

    /**
     * Subscribes to the logging application Pub/Sub subscription.
     *
     * @param receiver the message receiver
     */
    public void subscribeMessageLogging(MessageReceiver receiver) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, loggingAppSubscriptionID);
        subscribeMessage(subscriptionName, receiver);
    }

    /**
     * Subscribes to the labels application Pub/Sub subscription.
     *
     * @param receiver the message receiver
     */
    public void subscribeMessageLabels(MessageReceiver receiver) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, labelsAppSubscriptionID);
        subscribeMessage(subscriptionName, receiver);
    }

    /**
     * Subscribes to a Pub/Sub subscription with the given name and message receiver.
     *
     * @param subName the subscription name
     * @param receiver the message receiver
     */
    private void subscribeMessage(ProjectSubscriptionName subName, MessageReceiver receiver) {
        Subscriber subscriber = null;

        try {
            subscriber = Subscriber.newBuilder(subName, receiver)
                    .setExecutorProvider(executorProvider)
                    .build();
            subscriber.addListener(new Listener() {
                @Override
                public void failed(State from, Throwable failure) {
                    logger.log(Level.WARNING, "Error with subscriber: " + failure.getMessage());
                }
            }, MoreExecutors.directExecutor());
            subscriber.startAsync().awaitRunning();
            logger.info("Listening for messages on " + subName);
        } catch (Exception e) {
            if (subscriber != null) {
                subscriber.stopAsync();
            }
            logger.log(Level.WARNING, "Error during subscriber setup: " + e.getMessage(), e);
        }
    }
}