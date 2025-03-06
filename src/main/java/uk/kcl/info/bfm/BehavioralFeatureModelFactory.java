package uk.kcl.info.bfm;

import be.vibes.fexpression.DimacsModel;
import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.*;
import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import be.vibes.solver.exception.SolverInitializationException;
import de.vill.exception.ParseError;
import de.vill.model.Group;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;

import java.util.*;

public class BehavioralFeatureModelFactory {

    private final SolverType solverType;
    private final BehavioralFeatureModel bfm;

    public BehavioralFeatureModelFactory() {
        this(SolverType.SAT4J);
    }

    public BehavioralFeatureModelFactory(SolverType type) {
        switch (type) {
            case SAT4J -> this.solverType = SolverType.SAT4J;
            case BDD -> this.solverType = SolverType.BDD;
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
        this.bfm = new BehavioralFeatureModel();
    }

    public void setNamespace(String namespace) {
        bfm.setNamespace(namespace);
    }

    public BehavioralFeature setRootFeature(String name) {
        BehavioralFeature feature = new BehavioralFeature(name);
        feature.setParentGroup(null);
        bfm.getFeatureMap().put(name, feature);
        bfm.setRootFeature(feature);
        return feature;
    }

    public Group addChild(BehavioralFeature parent, Group.GroupType type) {
        Group group = new Group(type);
        parent.addChildren(group);
        group.setParentFeature(parent);
        return group;
    }

    public BehavioralFeature addFeature(Group group, String name) {
        BehavioralFeature feature = new BehavioralFeature(name);
        feature.setParentGroup(group);
        group.getFeatures().add(feature);
        bfm.getFeatureMap().put(name, feature);
        return feature;
    }

    public List<BehavioralFeature> addAllFeatures(Group group, Collection<String> names) {
        List<BehavioralFeature> features = new LinkedList<>();
        for (String name: names){
            BehavioralFeature feature = addFeature(group, name);
            features.add(feature);
        }
        return features;
    }

    //TODO: Exclusion and Requirements should be at the feature level (To change both here and in ViBeS)!
    public Constraint addExclusionConstraint(BehavioralFeature f1, BehavioralFeature f2) {
        return addExclusionConstraint(f1.getFeatureName(), f2.getFeatureName());
    }

    public Constraint addRequirementConstraint(BehavioralFeature feature, BehavioralFeature dependency){
        return addRequirementConstraint(feature.getFeatureName(), dependency.getFeatureName());
    }

    public Constraint addExclusionConstraint(String f1, String f2) {

        if(bfm.getFeature(f1) == null || bfm.getFeature(f2) == null){
            throw new BehavioralFeatureModelDefinitionException("Constraints should only refers to features belonging to the FM.");
        }

        LiteralConstraint c1 = new LiteralConstraint(f1);
        LiteralConstraint c2 = new LiteralConstraint(f2);
        Constraint constraint = new ExclusionConstraint(c1, c2);
        bfm.getOwnConstraints().add(constraint);
        return constraint;
    }

    public Constraint addRequirementConstraint(String feature, String dependency) {

        if(bfm.getFeature(feature) == null || bfm.getFeature(dependency) == null){
            throw new BehavioralFeatureModelDefinitionException("Constraints should only refers to features belonging to the FM.");
        }

        LiteralConstraint f = new LiteralConstraint(feature);
        LiteralConstraint d = new LiteralConstraint(dependency);
        Constraint constraint = new RequirementConstraint(d, f);
        bfm.getOwnConstraints().add(constraint);
        return constraint;
    }

    public void addEvent(String featName, String event, FExpression fexpr) {

        BehavioralFeature feature = bfm.getFeature(featName);
        if(feature != null){
            feature.addEvent(event, fexpr);
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

        BehavioralFeature feature = bfm.getFeature(featName);
        if(feature != null){
            feature.addCausality(bundle, target);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(String featName, CausalityRelation causalityRelation) {
        BehavioralFeature feature = bfm.getFeature(featName);
        if(feature != null){
            feature.addCausality(causalityRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(String featName, String event1, String event2) {
        this.addConflict(featName, new Event(event1), new Event(event2));
    }

    public void addConflict(String featName, Event event1, Event event2) {
        BehavioralFeature feature = bfm.getFeature(featName);
        if(feature != null){
            feature.addConflict(event1, event2);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(String featName, ConflictRelation conflictRelation) {
        BehavioralFeature feature = bfm.getFeature(featName);
        if(feature != null){
            feature.addConflict(conflictRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    private FExpression buildFExpression(BehavioralFeature feature) {

        FExpression featureExpression = FExpression.featureExpr(feature.getFeatureName());

        for(Group group : feature.getChildren()){

            FExpression groupExpression = null;
            List<FExpression> childrenExpressions = buildFExpression(group);

            switch (group.GROUPTYPE){
                case OR -> {
                    groupExpression = FExpression.falseValue();
                    for(FExpression child: childrenExpressions){
                        groupExpression.orWith(child);
                    }
                }
                case ALTERNATIVE -> {
                    groupExpression = FExpression.falseValue();
                    for(FExpression child: childrenExpressions){
                        FExpression c1 = groupExpression.or(child);
                        FExpression c2 = (groupExpression.not()).or(child.not());
                        groupExpression = c1.and(c2);
                    }
                }
                case MANDATORY -> {
                    if(childrenExpressions.size() == 1){
                        groupExpression = childrenExpressions.getFirst();
                    } else {
                        throw new BehavioralFeatureModelDefinitionException("A mandatory group can only contain one feature!");
                    }
                }
                case OPTIONAL -> {
                    if(childrenExpressions.size() == 1){
                        FExpression child = childrenExpressions.getFirst();
                        groupExpression = child.or(child.not());
                    } else {
                        throw new BehavioralFeatureModelDefinitionException("An optional group can only contain one feature!");
                    }
                }
            }

            assert groupExpression != null;
            featureExpression.andWith(groupExpression);
        }

        return featureExpression;
    }

    private List<FExpression> buildFExpression(Group group) {
        List<FExpression> childrenExpressions = new ArrayList<>();

        for (de.vill.model.Feature old : group.getFeatures()) {
            BehavioralFeature child = BehavioralFeature.clone(old);
            FExpression childExp = buildFExpression(child);
            childrenExpressions.add(childExp);
        }

        return childrenExpressions;
    }

    private FExpression buildFExpression(Constraint constraint) {

        FExpression featureExpression;

        switch (constraint) {
            case RequirementConstraint c -> {
                FExpression left = buildFExpression(c.getLeft());
                FExpression right = buildFExpression(c.getRight());
                featureExpression = left.not().or(right);
            }
            case ExclusionConstraint c -> {
                FExpression left = buildFExpression(c.getLeft());
                FExpression right = buildFExpression(c.getRight());
                featureExpression = (left.or(right)).and(left.not().or(right.not()));
            }
            case LiteralConstraint c -> {
                BehavioralFeature f = bfm.getFeature(c.getLiteral());
                featureExpression = FExpression.featureExpr(f);
            }
            default -> throw new BehavioralFeatureModelDefinitionException("This type of BFM constraint is not yet defined!");
        }

        return featureExpression;
    }

    private SolverFacade getSolverFacade(FExpression featureDiagram) {

        DimacsModel model;
        try {
            model = DimacsModel.createFromFeatureList(featureDiagram);
        } catch (DimacsFormatException e) {
            throw new ParseError("Unable to initialise the DimacsModel.");
        }

        SolverFacade solver;

        switch (this.solverType) {
            case SAT4J -> {
                try {
                    solver = new Sat4JSolverFacade(model);
                } catch (SolverInitializationException e) {
                    throw new ParseError("Unable to initialise the SAT4J solver.");
                }
            }
            case BDD -> solver = new BDDSolverFacade(featureDiagram);
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
        return solver;
    }

    public BehavioralFeatureModel build() {
        FExpression fexp = buildFExpression(bfm.getRootFeature());

        for(Constraint constr: bfm.getOwnConstraints()){
            fexp.andWith(buildFExpression(constr));
        }

        bfm.setSolver(getSolverFacade(fexp));
        return bfm;
    }
}