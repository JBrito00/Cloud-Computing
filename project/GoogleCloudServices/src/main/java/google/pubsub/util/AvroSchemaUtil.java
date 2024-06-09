package google.pubsub.util;

import com.google.protobuf.ByteString;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;

/**
 * AvroSchemaUtil is a utility class for loading Avro schemas.
 */
public class AvroSchemaUtil {
    private final Schema schema = new Schema.Parser().parse(AvroSchemaUtil.class.getResourceAsStream("/google/pubsub/messageSchema.avsc"));

    public AvroSchemaUtil() throws IOException {
    }

    /**
     * Create a new Avro record to be sent as a message.
     *
     * @param id the id of the message
     * @param bucketName the name of the bucket of the message
     * @param blobName the name of the blob of the message
     * @return the message in a byte string
     * @throws IOException if there is an error reading the schema file
     */
    public ByteString createNewAvroRecord(String id, String bucketName, String blobName) throws IOException {
        GenericRecord record = new GenericData.Record(schema);
        record.put("id", id);
        record.put("bucketName", bucketName);
        record.put("blobName", blobName);

        return ByteString.copyFromUtf8(record.toString());
    }
}
