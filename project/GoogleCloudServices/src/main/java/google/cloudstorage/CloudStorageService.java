package google.cloudstorage;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Service class for handling operations related to Google Cloud Storage.
 */
public class CloudStorageService {
    private final Storage storage;
    private final String bucketName = "cn-tp-g09-bucket";

    /**
     * Constructs a CloudStorageService and initializes the Google Cloud Storage client.
     */
    public CloudStorageService() {
        storage = StorageOptions.getDefaultInstance().getService();
    }

    /**
     * Stores image bytes in a Google Cloud Storage bucket. If the blob does not exist, it creates a new one.
     * Appends new image data to existing data in the blob.
     *
     * @param imageData the image data to store.
     * @param blobName  the name of the blob.
     * @return the media link to the stored blob.
     */
    public String storeImageBytes(byte[] imageData, String blobName) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            blob = storage.create(blobInfo);
        }
        byte[] existingBytes = blob.getContent();
        byte[] newBytes = concatenateBytes(existingBytes, imageData);
        try (WriteChannel writer = blob.writer()) {
            writer.write(ByteBuffer.wrap(newBytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return blob.getMediaLink();
    }

    /**
     * Generates a unique blob name using a UUID.
     *
     * @return a unique blob name.
     */
    public String generateUniqueBlobName() {
        return bucketName + "-image-" + UUID.randomUUID();
    }

    /**
     * Concatenates two byte arrays into a single byte array.
     *
     * @param existingBytes the existing byte array.
     * @param newBytes      the new byte array to append.
     * @return a concatenated byte array.
     */
    private byte[] concatenateBytes(byte[] existingBytes, byte[] newBytes) {
        byte[] result = new byte[existingBytes.length + newBytes.length];
        System.arraycopy(existingBytes, 0, result, 0, existingBytes.length);
        System.arraycopy(newBytes, 0, result, existingBytes.length, newBytes.length);
        return result;
    }

}
