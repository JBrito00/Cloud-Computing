package google.firestore.models;

import java.util.List;

public class VisionInformation {
    private final List<String> details;

    public VisionInformation(List<String> details) {
        this.details = details;
    }

    public List<String> getDetails() {
        return details;
    }
}
