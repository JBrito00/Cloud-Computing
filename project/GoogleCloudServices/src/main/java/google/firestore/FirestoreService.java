package google.firestore;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import google.firestore.models.ImageInformation;
import google.firestore.models.LogEntry;
import google.firestore.models.TranslationInformation;
import google.firestore.models.VisionInformation;

import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Service class for handling operations related to Google Firestore.
 */
public class FirestoreService {

    private final CollectionReference logsCollection;
    private final CollectionReference characteristicsCollection;

    /**
     * Constructs a FirestoreService and initializes Firestore collections for logs and image details.
     */
    public FirestoreService() {
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath == null) {
            throw new IllegalStateException("Environment variable GOOGLE_APPLICATION_CREDENTIALS not set.");
        }

        GoogleCredentials credentials;
        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(serviceAccount);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read credentials file.");
        }

        Firestore db = FirestoreOptions
                .newBuilder()
                .setDatabaseId("db-name")
                .setCredentials(credentials)
                .build().getService();
        this.logsCollection = db.collection("Logs");
        this.characteristicsCollection = db.collection("ImagesDetails");
    }

    /**
     * Saves a log entry to the Firestore logs collection.
     *
     * @param logEntry the log entry to save.
     * @throws ExecutionException if an error occurs during execution.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void saveLog(LogEntry logEntry) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> future = logsCollection.add(logEntry);
        future.get();
    }

    /**
     * Saves image information to the Firestore characteristics collection.
     *
     * @param imageInfo the image information to save.
     * @throws ExecutionException   if an error occurs during Firestore operation.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void saveImageInfo(ImageInformation imageInfo) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> future = characteristicsCollection.add(imageInfo);
        future.get();
    }

    /**
     * Retrieves image information from Firestore based on a unique ID.
     *
     * @param uniqueID the unique ID of the image.
     * @return the image information, or null if not found.
     * @throws ExecutionException   if an error occurs during Firestore operation.
     * @throws InterruptedException if the operation is interrupted.
     */
    public ImageInformation getImageInfo(String uniqueID) throws ExecutionException, InterruptedException {
        Query query = characteristicsCollection.whereEqualTo("requestId", uniqueID);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        DocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
        if (document != null && document.exists()) {
            return fromDocumentSnapshot(document);
        } else {
            return null;
        }
    }

    /**
     * Retrieves a list of image file names between certain dates with a specific characteristic.
     *
     * @param startDate     the start date in "dd-MM-yyyy" format.
     * @param endDate       the end date in "dd-MM-yyyy" format.
     * @param characteristic the characteristic to filter by.
     * @return a list of image file names.
     * @throws ParseException       if the date format is incorrect.
     * @throws ExecutionException   if an error occurs during Firestore operation.
     * @throws InterruptedException if the operation is interrupted.
     */
    public List<String> getImageFileNameBetweenCertainDatesAndWith(String startDate, String endDate, String characteristic)
            throws ParseException, ExecutionException, InterruptedException {

        List<String> images = new java.util.ArrayList<>(List.of());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        Date startParsed = formatter.parse(startDate);
        Date endParsed = formatter.parse(endDate);

        Timestamp start = Timestamp.of(startParsed);
        Timestamp end = Timestamp.of(endParsed);

        Query query = characteristicsCollection
                .whereGreaterThan("timestamp", start)
                .whereLessThan("timestamp", end)
                .whereArrayContains("visionInfo", characteristic);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            images.add(doc.getString("requestId"));
        }
        return images;
    }

    /**
     * Converts a Firestore document snapshot to an ImageInformation object.
     *
     * @param document the Firestore document snapshot.
     * @return the ImageInformation object, or null if the document does not exist.
     */
    private static ImageInformation fromDocumentSnapshot(DocumentSnapshot document) {
        if (document != null && document.exists()) {
            String requestId = document.getString("requestId");
            Timestamp timestamp = document.getTimestamp("timestamp");
            List<String> translationInfoList = (List<String>) document.get("translationInfo");
            TranslationInformation translationInfo = new TranslationInformation(translationInfoList);
            List<String> visionInfoList = (List<String>) document.get("visionInfo");
            VisionInformation visionInfo = new VisionInformation(visionInfoList);
            return new ImageInformation(requestId, timestamp, translationInfo, visionInfo);
        } else {
            return null;
        }
    }

}