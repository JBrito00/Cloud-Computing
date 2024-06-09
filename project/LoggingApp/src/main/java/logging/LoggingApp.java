package logging;

import com.google.cloud.Timestamp;
import com.google.protobuf.ByteString;
import google.firestore.FirestoreService;
import google.firestore.models.LogEntry;
import google.pubsub.service.PubSubService;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Application to process log messages and save them to Firestore.
 */
public class LoggingApp {
    private final FirestoreService firestoreService;
    private final PubSubService pubSubService;
    private final Schema schema;

    /**
     * Constructor to initialize FirestoreService, PubSubService, and Avro schema.
     */
    public LoggingApp() {
        try {
            this.firestoreService = new FirestoreService();
            this.pubSubService = new PubSubService();
            this.schema = new Schema.Parser().parse(getClass().getResourceAsStream("/logging/messageSchema.avsc"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read schema file.");
        }
    }

    /**
     * Method to subscribe to Pub/Sub messages and process them.
     */
    public void checkSub() {
        pubSubService.subscribeMessageLogging((message, consumer) -> {
            ByteString data = message.getData();
            try {
                GenericRecord record = deserializeAvroSchema(data);

                Map<String, String> attributes = message.getAttributesMap();
                String timestamp = attributes.get("timestamp");

                Timestamp firestoreTimestamp = Timestamp.parseTimestamp(timestamp);

                firestoreService.saveLog(new LogEntry(record.get("id").toString(), firestoreTimestamp));
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
     * Main method to run the LoggingApp.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        LoggingApp app = new LoggingApp();
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