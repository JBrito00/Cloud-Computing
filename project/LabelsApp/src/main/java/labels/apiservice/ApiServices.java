package labels.apiservice;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.vision.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that provides access to Google Cloud Apis.
 */
public class ApiServices {

    /**
     * Method to detect labels in an image using the Vision API.
     *
     * @param bucketName Name of the Cloud Storage bucket containing the image.
     * @param blobName   Name of the image blob.
     * @return List of detected labels.
     * @throws IOException if an error occurs during the detection process.
     */
    public static List<String> detectLabels(String bucketName, String blobName) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        String gsURI = String.format("gs://%s/%s", bucketName, blobName);

        Image img = Image.newBuilder()
                .setSource(ImageSource.newBuilder().setImageUri(gsURI).build())
                .build();

        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();

        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                } else {
                    for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                        labels.add(annotation.getDescription());
                    }
                }
            }
        }
        return labels;
    }

    /**
     * Method to translate labels from one language to another.
     *
     * @param labels List of labels to translate.
     * @return Translated labels.
     */
    public static List<String> translateLabels(List<java.lang.String> labels) {
        List<java.lang.String> labelsTranslated = null;
        try {
            Translate translateService = TranslateOptions.getDefaultInstance().getService();
            labelsTranslated = new ArrayList<>();
            for (java.lang.String label : labels) {
                Translation translation = translateService.translate(
                        label,
                        Translate.TranslateOption.sourceLanguage("en"),
                        Translate.TranslateOption.targetLanguage("pt"));
                labelsTranslated.add(translation.getTranslatedText());
            }
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
        return labelsTranslated;
    }

}
