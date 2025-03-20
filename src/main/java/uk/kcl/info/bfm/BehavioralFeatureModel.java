package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.Group;
import be.vibes.solver.SolverFacade;

import java.util.*;

public class BehavioralFeatureModel extends FeatureModel<BehavioralFeature>{

    protected BehavioralFeatureModel() {
        super();
    }

    protected BehavioralFeatureModel(SolverFacade solver) {
        super(solver);
    }

    protected BehavioralFeatureModel(FeatureModel<?> fm) {

        super();
        this.setNamespace(fm.getNamespace());

        BehavioralFeature root = new BehavioralFeature(fm.getRootFeature());
        this.setRootFeature(root);

        Queue<BehavioralFeature> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {

            BehavioralFeature bf = queue.poll();
            this.getFeatureMap().put(bf.getFeatureName(), bf);

            for (Group<?> group: bf.getChildren()){
                queue.addAll((Collection<? extends BehavioralFeature>) group.getFeatures());
            }
        }

        this.getOwnConstraints().addAll(fm.getOwnConstraints());
        this.setSolver(fm.getSolver());
    }

    private BehavioralFeature getRecursiveFeature(BehavioralFeature currentFeature, Event event){

        if(currentFeature.getEvents().containsKey(event)){
            return currentFeature;
        } else {
            for (Group<?> group : currentFeature.getChildren()) {
                for (Feature<?> bf : group.getFeatures()) {
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
        assert bf != null;
        return bf.getFExpression(event);
    }
}