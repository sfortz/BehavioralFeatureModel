package uk.kcl.info.bfm;

import be.vibes.fexpression.DimacsModel;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.SolverInitializationException;

import java.io.File;
import java.io.IOException;

public class BehavioralFeatureModelFactory {
    protected final DefaultBehavioralFeatureModel bfm;
    protected BehavioralFeatureModelFactory(DefaultBehavioralFeatureModel bfm) {
        this.bfm = bfm;
    }

    public BehavioralFeatureModelFactory(DimacsModel model) throws SolverInitializationException {
        this(new DefaultBehavioralFeatureModel(model));
    }

    public BehavioralFeatureModelFactory(File featureMapping) throws SolverInitializationException, IOException {
        this(new DefaultBehavioralFeatureModel(featureMapping));
    }

    public BehavioralFeatureModelFactory(String dimacsModel, String featureMapping) throws SolverInitializationException, IOException {
        this(new DefaultBehavioralFeatureModel(dimacsModel, featureMapping));
    }

    public BehavioralFeatureModelFactory(File dimacsModel, File featureMapping) throws SolverInitializationException, IOException {
        this(new DefaultBehavioralFeatureModel(dimacsModel));
    }

    public void addEvent(String feature, String event){
        this.bfm.addEvent(new Feature(feature), new Event(event));
    }

    public void addEvent(Feature feature, Event event){
        this.bfm.addEvent(feature, event);
    }

    public void addCausality(String source, String target){
        this.addCausality(new Event(source), new Event(target));
    }

    public void addCausality(Event source, Event target){
        this.bfm.addCausality(source, target);
    }

    public void addCausality(Feature fsource, Event source, Feature ftarget, Event target){
        this.bfm.addEvent(fsource, source);
        this.bfm.addEvent(ftarget, target);
        this.bfm.addCausality(source, target);
    }

    public void addConflict(String ev1, String ev2){
        this.addConflict(new Event(ev1), new Event(ev2));
    }

    public void addConflict(Event ev1, Event ev2){
        this.bfm.addConflict(ev1, ev2);
    }

    public DefaultBehavioralFeatureModel build() {
        return this.bfm;
    }

    public void validate() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
    
}
