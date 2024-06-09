package google.firestore.models;

import java.util.List;

public class TranslationInformation {
    private final List<String> details;

    public TranslationInformation(List<String> details) {
        this.details = details;
    }

    public List<String> getDetails() {
        return details;
    }
}
