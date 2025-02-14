package uk.kcl.info.bfm.exceptions;

public class BundleEventStructureExecutionException extends Exception {
    public BundleEventStructureExecutionException(String message) {
        super(message);
    }

    public BundleEventStructureExecutionException(String message, Exception cause) {
        super(message, cause);
    }
}
