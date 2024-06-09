package labels;

import com.google.cloud.Timestamp;
import com.google.protobuf.ByteString;
import google.firestore.FirestoreService;
import google.firestore.models.ImageInformation;
import google.firestore.models.TranslationInformation;
import google.firestore.models.VisionInformation;
import google.pubsub.PubSubService;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static labels.apiservice.ApiServices.detectLabels;
import static labels.apiservice.ApiServices.translateLabels;

/**
 * Application to process messages containing image data and save information to Firestore.
 */
public class LabelsApp {
    private final FirestoreService firestoreService;
    private final PubSubService pubSubService;
    private final Schema schema;

    /**
     * Constructor to initialize FirestoreService, PubSubService, and Avro schema.
     */
    public LabelsApp() {
        try {
            this.firestoreService = new FirestoreService();
            this.pubSubService = new PubSubService();
            this.schema = new Schema.Parser().parse(getClass().getResourceAsStream("/labels/messageSchema.avsc"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read schema file.");
        }
    }

    /**
     * Method to subscribe to Pub/Sub messages and process them.
     */
    public void checkSub() {
        pubSubService.subscribeMessageLabels((message, consumer) -> {
            ByteString data = message.getData();
            try {
                GenericRecord record = deserializeAvroSchema(data);
                String id = record.get("id").toString();
                String bucketName = record.get("bucketName").toString();
                String blobName = record.get("blobName").toString();
                Map<String, String> attributes = message.getAttributesMap();
                String timestamp = attributes.get("timestamp");

                Timestamp firestoreTimestamp = Timestamp.parseTimestamp(timestamp);

                List<String> labels = detectLabels(bucketName, blobName);
                List<String> labelsTranslated = translateLabels(labels);
                TranslationInformation translationInformation = new TranslationInformation(labelsTranslated);
                VisionInformation visionInformation = new VisionInformation(labels);
                ImageInformation imageInformation = new ImageInformation(id, firestoreTimestamp, translationInformation, visionInformation);
                firestoreService.saveImageInfo(imageInformation);
                consumer.ack();
            } catch (IOException | ExecutionException | InterruptedException e) {
                consumer.nack();
                System.err.println("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Method to deserialize Avro schema from ByteString.
     *
     * @param data ByteString containing Avro data.
     * @return Deserialized Avro record.
     * @throws IOException if an error occurs during deserialization.
     */
    private GenericRecord deserializeAvroSchema(ByteString data) throws IOException {
        String dataString = data.toStringUtf8();
        DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        Decoder decoder = DecoderFactory.get().jsonDecoder(schema, dataString);
        return reader.read(null, decoder);
    }

    /**
     * Main method to run the LabelsApp.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        LabelsApp app = new LabelsApp();
        app.checkSub();
        // Keep the application running to listen for messages
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}