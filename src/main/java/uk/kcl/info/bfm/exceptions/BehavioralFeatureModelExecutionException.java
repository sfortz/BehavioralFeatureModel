package uk.kcl.info.bfm.exceptions;

public class BehavioralFeatureModelExecutionException extends Exception {
    public BehavioralFeatureModelExecutionException(String message) {
        super(message);
    }

    public BehavioralFeatureModelExecutionException(String message, Exception cause) {
        super(message, cause);
    }
}
