package uk.kcl.info.bfm.exceptions;

public class BundleEventStructureDefinitionException extends Exception {

    public BundleEventStructureDefinitionException(String message) {
        super(message);
    }
    public BundleEventStructureDefinitionException(String error, Exception cause) {
        super(error, cause);
    }

}