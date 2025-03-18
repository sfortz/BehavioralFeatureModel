package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.Group;
import be.vibes.solver.SolverFacade;

import java.util.*;

public class BehavioralFeatureModel extends FeatureModel<BehavioralFeature>{

    public BehavioralFeatureModel() {
        super();
    }

    public BehavioralFeatureModel(SolverFacade solver) {
        super(solver);
    }

    public BehavioralFeatureModel(FeatureModel<Feature> fm) {

        super();
        this.setNamespace(fm.getNamespace());
        this.getOwnConstraints().addAll(fm.getOwnConstraints());

        for (Map.Entry<String, Feature> entry: fm.getFeatureMap().entrySet()){
            this.getFeatureMap().put(entry.getKey(), new BehavioralFeature(entry.getValue()));
        }

        this.setRootFeature(this.getFeature(fm.getRootFeature().getFeatureName()));
        this.setSolver(fm.getSolver());
    }

    private BehavioralFeature getRecursiveFeature(BehavioralFeature currentFeature, Event event){

        if(currentFeature.getEvents().containsKey(event)){
            return currentFeature;
        } else {
            for (Group<BehavioralFeature> group : currentFeature.getChildren()) {
                for (BehavioralFeature bf : group.getFeatures()) {
                    BehavioralFeature result = getRecursiveFeature(this.getFeature(bf.getFeatureName()), event);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }
    }

    public BehavioralFeature getFeature(Event event){
        return getRecursiveFeature(this.getRootFeature(), event);
    }

    public FExpression getFExpression(Event event){
        BehavioralFeature bf = getRecursiveFeature(this.getRootFeature(), event);
        return bf.getFExpression(event);
    }
}