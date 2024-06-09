package google.firestore.models;

import com.google.cloud.Timestamp;

public class LogEntry {
    private String requestId;
    private Timestamp timestamp;

    public LogEntry() {}

    public LogEntry(String requestId, Timestamp timestamp) {
        this.requestId = requestId;
        this.timestamp = timestamp;
    }

    // Getter and setter methods
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
