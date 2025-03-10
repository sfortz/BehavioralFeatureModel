package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.ConstraintIdentifier;
import be.vibes.solver.SolverFacade;
import be.vibes.solver.exception.ConstraintNotFoundException;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.exception.SolverFatalErrorException;
import be.vibes.solver.exception.SolverInitializationException;

import java.util.*;
import java.util.stream.Collectors;

public class BehavioralFeatureModel  extends de.vill.model.FeatureModel {

    private SolverFacade solver;
    private final Map<String, BehavioralFeature> featureMap = new HashMap<>();

    protected BehavioralFeatureModel(de.vill.model.FeatureModel featureModel, SolverFacade solver) {
        super();
        this.getUsedLanguageLevels().addAll(featureModel.getUsedLanguageLevels());
        this.setNamespace(featureModel.getNamespace());
        this.getImports().addAll(featureModel.getImports());
        this.setRootFeature(featureModel.getRootFeature());
        this.getFeatureMap().putAll(featureModel.getFeatureMap());
        for(Map.Entry<String, de.vill.model.Feature> entry: featureModel.getFeatureMap().entrySet()){
            this.featureMap.put(entry.getKey(), BehavioralFeature.clone(entry.getValue()));
        }
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
        return getFeature(super.getRootFeature().getFeatureName());
    }

    public BehavioralFeature getFeature(String name) {
        if (name == null) {
            return null;
        }

        // Find matching feature in a case-insensitive way
        for (Map.Entry<String, BehavioralFeature> entry : this.featureMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        return null; // Return null if no match is found
    }

    public Collection<BehavioralFeature> getFeatures() {
        return this.featureMap.values();
    }

    protected  Map<String, BehavioralFeature> getNewFeatureMap(){
        return featureMap;
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

    private static List<BehavioralFeature> getAncestors(BehavioralFeature feature) {
        List<BehavioralFeature> ancestors = new ArrayList<>();
        while (feature != null) {
            ancestors.add(feature);
            feature = (BehavioralFeature) feature.getParentFeature();
        }
        return ancestors;
    }

    private static BehavioralFeature leastCommonAncestor(BehavioralFeature f1, BehavioralFeature f2) {
        List<BehavioralFeature> ancestorsF1 = getAncestors(f1);
        List<BehavioralFeature> ancestorsF2 = getAncestors(f2);

        // Find the lowest common ancestor
        for (BehavioralFeature ancestor : ancestorsF1) {
            if (ancestorsF2.contains(ancestor)) {
                return ancestor;
            }
        }

        return null;
    }

    public BehavioralFeature leastCommonAncestor (List<FExpression> fExpressions){

        FExpression disjunction = FExpression.falseValue();

        for (FExpression fexp: fExpressions){
            disjunction.orWith(fexp);
        }

        disjunction = disjunction.toCnf().applySimplification();

        if (disjunction.isTrue()) {
            return this.getRootFeature();
        }

        Set<BehavioralFeature> features = disjunction.getFeatures().stream().map(f -> this.getFeature(f.getFeatureName())).collect(Collectors.toSet());

        Iterator<BehavioralFeature> iterator = features.iterator();
        BehavioralFeature lca = iterator.next();

        while (iterator.hasNext()) {
            lca = leastCommonAncestor(lca, iterator.next());
        }
        return lca;
    }
}