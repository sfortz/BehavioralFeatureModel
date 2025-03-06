package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.ConstraintIdentifier;
import be.vibes.solver.SolverFacade;
import be.vibes.solver.exception.ConstraintNotFoundException;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.exception.SolverFatalErrorException;
import be.vibes.solver.exception.SolverInitializationException;

import java.util.*;

public class BehavioralFeatureModel  extends de.vill.model.FeatureModel {

    private SolverFacade solver;

    protected BehavioralFeatureModel(de.vill.model.FeatureModel featureModel, SolverFacade solver) {
        super();
        this.getUsedLanguageLevels().addAll(featureModel.getUsedLanguageLevels());
        this.setNamespace(featureModel.getNamespace());
        this.getImports().addAll(featureModel.getImports());
        this.setRootFeature(featureModel.getRootFeature());
        this.getFeatureMap().putAll(featureModel.getFeatureMap());
        this.getImports().addAll(featureModel.getImports());
        this.getOwnConstraints().addAll(featureModel.getOwnConstraints());
        this.setExplicitLanguageLevels(featureModel.isExplicitLanguageLevels());
        this.getLiteralConstraints().addAll(featureModel.getLiteralConstraints());
        this.getLiteralExpressions().addAll(featureModel.getLiteralExpressions());
        this.getAggregateFunctionsWithRootFeature().addAll(featureModel.getAggregateFunctionsWithRootFeature());
        this.solver = solver;
    }

    protected BehavioralFeatureModel(SolverFacade solver) {
        super();
        this.solver = solver;
    }

    protected BehavioralFeatureModel() {
        super();
    }

    protected void setSolver(SolverFacade solver) {
        if(this.solver == null){
            this.solver = solver;
        } else {
            throw new RuntimeException("This Feature Model solver was already set.");
        }
    }

    public SolverFacade getSolver() {
        return solver;
    }

    /**
     * Set the root feature of the feature model
     * @param: rootFeature – the root feature
     */
    @Override
    public void setRootFeature(de.vill.model.Feature rootFeature) {
        super.setRootFeature(rootFeature);
        // TODO: Change the solver by getting the root feature Feature Diagram.
    }

    @Override
    public BehavioralFeature getRootFeature() {
        return BehavioralFeature.clone(super.getRootFeature());
    }

    public BehavioralFeature getFeature(String name) {
        if (name == null) {
            return null;
        }

        // Find matching feature in a case-insensitive way
        for (Map.Entry<String, de.vill.model.Feature> entry : this.getFeatureMap().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return BehavioralFeature.clone(entry.getValue());
            }
        }

        return null; // Return null if no match is found
    }

    public Set<BehavioralFeature> getFeatures() {
        Set<BehavioralFeature> features = new HashSet<>();
        for (de.vill.model.Feature f : this.getFeatureMap().values()) {
            features.add(BehavioralFeature.clone(f));
        }
        return features;
    }

    public ConstraintIdentifier addSolverConstraint(FExpression constraint)
            throws SolverInitializationException, SolverFatalErrorException {
        return this.solver.addConstraint(constraint);
    }

    public void removeSolverConstraint(ConstraintIdentifier id)
            throws SolverFatalErrorException, ConstraintNotFoundException {
        this.solver.removeConstraint(id);
    }

    public boolean isSatisfiable() throws ConstraintSolvingException {
        return this.solver.isSatisfiable();
    }

    public Iterator<Configuration> getSolutions() throws ConstraintSolvingException {
        return this.solver.getSolutions();
    }

    public void resetSolver() throws SolverInitializationException {
        this.solver.reset();
    }

    public double getNumberOfSolutions() throws ConstraintSolvingException {
        return this.solver.getNumberOfSolutions();
    }

}