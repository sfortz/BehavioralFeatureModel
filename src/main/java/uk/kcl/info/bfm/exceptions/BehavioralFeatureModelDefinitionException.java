package uk.kcl.info.bfm.exceptions;

public class BehavioralFeatureModelDefinitionException extends Exception {

    public BehavioralFeatureModelDefinitionException(String message) {
        super(message);
    }
    public BehavioralFeatureModelDefinitionException(String error, Exception cause) {
        super(error, cause);
    }

}