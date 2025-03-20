package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.solver.*;
import be.vibes.solver.XMLModelFactory;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import java.util.*;

public class BehavioralFeatureModelFactory extends XMLModelFactory<BehavioralFeature, FeatureModel<BehavioralFeature>> {

    public BehavioralFeatureModelFactory() {
        super(BehavioralFeatureModel::new);
    }

    public BehavioralFeatureModelFactory(SolverType type) {
        super(BehavioralFeatureModel::new, type);
    }

    public BehavioralFeatureModelFactory(FeatureModel<?> fm) {
        super(() -> new BehavioralFeatureModel(fm), fm.getSolver().getType());
    }

    public BehavioralFeature setRootFeature(String name){
        BehavioralFeature feature = new BehavioralFeature(name);
        return setRootFeature(feature, name);
    }

    public BehavioralFeature addFeature(Group<BehavioralFeature> group, String name){
        BehavioralFeature feature = new BehavioralFeature(name);
        return addFeature(feature, group, name);
    }

    public void addEvent(String featName, String event) {
        this.addEvent(featName, event, FExpression.trueValue());

    }

    public void addEvent(BehavioralFeature feat,  String event) {
        this.addEvent(feat, event, FExpression.trueValue());
    }

    public void addEvent(String featName, String event, FExpression fexpr) {

        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            feature.addEvent(event, fexpr);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Events should always be associated to one feature of the BFM.");
        }
    }

    public void addEvent(BehavioralFeature feat,  String event, FExpression fexpr) {

        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.addEvent(event, fexpr);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Events should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(String featName, Set<String> bundle, String target) {

        Event trg = new Event(target);
        Set<Event> bndl = new HashSet<>();
        for(String name: bundle) {
            Event event = new Event(name);
            bndl.add(event);
        }

        this.addCausality(featName,bndl,trg);
    }

    public void addCausality(String featName, Set<Event> bundle, Event target) {

        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            feature.addCausality(bundle, target);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(String featName, CausalityRelation causalityRelation) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            feature.addCausality(causalityRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(BehavioralFeature feat, Set<String> bundle, String target) {

        Event trg = new Event(target);
        Set<Event> bndl = new HashSet<>();
        for(String name: bundle) {
            Event event = new Event(name);
            bndl.add(event);
        }

        this.addCausality(feat,bndl,trg);
    }

    public void addCausality(BehavioralFeature feat, Set<Event> bundle, Event target) {

        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.addCausality(bundle, target);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(BehavioralFeature feat, CausalityRelation causalityRelation) {
        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.addCausality(causalityRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(String featName, String event1, String event2) {
        this.addConflict(featName, new Event(event1), new Event(event2));
    }

    public void addConflict(String featName, Event event1, Event event2) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            feature.addConflict(event1, event2);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(String featName, ConflictRelation conflictRelation) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            feature.addConflict(conflictRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(BehavioralFeature feat, String event1, String event2) {
        this.addConflict(feat, new Event(event1), new Event(event2));
    }

    public void addConflict(BehavioralFeature feat, Event event1, Event event2) {
        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.addConflict(event1, event2);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(BehavioralFeature feat, ConflictRelation conflictRelation) {
        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.addConflict(conflictRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    @Override
    public BehavioralFeatureModel build() {

        BehavioralFeatureModel bfm = (BehavioralFeatureModel) super.build();
        bfm.setCausalityTable();
        return bfm;
    }

}