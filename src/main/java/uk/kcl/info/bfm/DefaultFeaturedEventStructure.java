package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.FExpressionVisitorWithReturn;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.fexpression.configuration.ConfigurationSet;
import be.vibes.fexpression.exception.FExpressionException;
import be.vibes.solver.ConstraintIdentifier;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import com.google.common.base.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultFeaturedEventStructure<F extends Feature<F>>  extends DefaultBundleEventStructure implements FeaturedEventStructure<F>{

    private final Map<Event, F> features = new HashMap<>();

    private final Map<Event, FExpression> eventFexpressions = new HashMap<>();

    private final Map<List<Event>, FExpression> configFexpressions = new HashMap<>();

    private final FeatureModel<F> fm;

    public DefaultFeaturedEventStructure(FeatureModel<F> fm) {
        super();
        this.fm = fm;
    }

    public FeatureModel<F> getFm() {
        return fm;
    }

    @Override
    public F getFeature(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        return this.features.get(event);
    }

    @Override
    public FExpression getFExpression(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        FExpression fexpr = this.eventFexpressions.get(event);
        if (fexpr == null) {
            fexpr = FExpression.trueValue();
        }

        return fexpr;
    }

    @Override
    public FExpression getFExpression(List<Event> config) {
        return configFexpressions.get(config);
    }

    private F getFeatureFromFM(F feature){
        return this.fm.getFeature(feature.getFeatureName());
    }

    private class FexpFromFMBuilder implements FExpressionVisitorWithReturn<FExpression> {

        private FExpression fexp;

        public FexpFromFMBuilder() {}

        @Override
        public FExpression constant(boolean val) {
            if (val) {
                return FExpression.trueValue();
            } else {
                return FExpression.falseValue();
            }
        }

        @Override
        public FExpression feature(Feature<?> feature) {
            return new FExpression(getFeatureFromFM((F) feature));
        }

        @Override
        public FExpression not(FExpression expr) {
            try {
                FExpression operand = expr.accept(this);
                return operand.not();
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

        @Override
        public FExpression and(List<FExpression> operands) {
            try {
                FExpression conj = FExpression.trueValue();
                for (FExpression e : operands) {
                    conj = conj.and(e.accept(this));
                }
                return conj;
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

        @Override
        public FExpression or(List<FExpression> operands) {
            try {
                FExpression disj = FExpression.falseValue();
                for (FExpression e : operands) {
                    disj = disj.or(e.accept(this));
                }
                return disj;
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

    }

    private FExpression getFexpFromFM(FExpression fexp) {
        try {
            return fexp.accept(new FexpFromFMBuilder());
        } catch (FExpressionException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    void addFeature(Event event, Feature<?> feature, FExpression fexpr) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        Preconditions.checkNotNull(feature, "Feature may not be null!");
        this.features.put(event, getFeatureFromFM((F) feature));
        this.eventFexpressions.put(event, getFexpFromFM(fexpr));
    }

    @Override
    public Set<List<Event>> getAllConfigurations() {
        Set<List<Event>> allConfigs = new HashSet<>();
        Queue<Event> allEvents = new LinkedList<>(this.getAllEvents());
        try {
            buildProductConfigurations(new ArrayList<>(), allEvents, allConfigs);
            this.fm.resetSolver();
        } catch (ConstraintSolvingException e) {
            throw new IllegalStateException("Error solving constraints: " + e.getMessage(), e.getCause());
        }
        return allConfigs;
    }

    private void buildProductConfigurations(List<Event> currentConfig, Queue<Event> remainingEvents, Set<List<Event>> allConfigs) throws ConstraintSolvingException {
        // Add the current configuration to the allConfigs set. A new list is created to avoid modifying the original configuration.
        allConfigs.add(new ArrayList<>(currentConfig));

        // Create a copy of remaining events to avoid concurrent modification
        Queue<Event> remainingCopy = new LinkedList<>(remainingEvents);

        while (!remainingCopy.isEmpty()) {
            Event e = remainingCopy.poll();

            if (isConflictFree(e, currentConfig)) {
                Set<Set<Event>> initialBundles = this.getAllBundles(e);

                ConfigurationSet products = getValidProducts(e);

                for (Configuration product : products) {

                    // Construct the feature expression
                    FExpression productFExp = FExpression.trueValue();
                    for (Feature<?> f : product.getFeatures()) {
                        productFExp.andWith(new FExpression(f));
                    }

                    ConstraintIdentifier constrId = this.fm.addSolverConstraint(productFExp);

                    if (this.fm.isSatisfiable() && respectsCausality(currentConfig, product, initialBundles)) {
                        // If valid, add event to current configuration and remove from remaining events
                        currentConfig.add(e);
                        remainingEvents.remove(e);
                        // Recursively build configurations
                        buildProductConfigurations(currentConfig, remainingEvents, allConfigs);
                        // Backtrack: remove the event and restore the queue
                        currentConfig.remove(e);
                        remainingEvents.offer(e);
                    }

                    this.fm.removeSolverConstraint(constrId);
                }
            }
        }

        FExpression currentFExpr = getCurrentFExpr();
        configFexpressions.merge(
                new ArrayList<>(currentConfig),
                currentFExpr.applySimplification(),
                FExpression::or
        );
    }

    private FExpression getCurrentFExpr() throws ConstraintSolvingException {
        Iterator<Configuration> solutions = this.fm.getSolutions();
        FExpression currentFExpr = FExpression.falseValue();

        while (solutions.hasNext()) {
            Configuration sol = solutions.next();
            FExpression acc = FExpression.trueValue();
            Set<F> selected = (Set<F>) Arrays.stream(sol.getFeatures()).collect(Collectors.toSet());
            selected = selected.stream().map(f -> { //TODO: To remove once Feature.hashcode() is debugged
                    for(F feat: this.fm.getFeatures()){
                        if (f.getFeatureName().equals(feat.getFeatureName())){
                            return feat;
                        }
                    }
                    return null;
            }).collect(Collectors.toSet());

            Set<F> deselected = new HashSet<>(this.fm.getFeatures());
            deselected.removeAll(selected);

            for (F f : selected) {
                acc.andWith(new FExpression(f));
            }
            for (F f : deselected) {
                acc.andWith(new FExpression(f).not());
            }

            currentFExpr.orWith(acc.applySimplification());
        }

        return currentFExpr.applySimplification().toCnf();
    }

    protected boolean respectsCausality(List<Event> currentConfig, Configuration product, Set<Set<Event>> initialBundles) {

        if(initialBundles.isEmpty() && currentConfig.isEmpty()){
            return true;
        }

        boolean causal = false;
        for (Set<Event> oldBundle: initialBundles){

            Set<Event> candidateBundle = oldBundle.stream()
                    .filter(ev -> {
                        try {
                            return respectsFM(product, ev);
                        } catch (ConstraintSolvingException ex) {
                            throw new IllegalStateException("No bundle created, Error solving constraints: " + ex.getMessage(), ex.getCause());
                        }
                    })
                    .collect(Collectors.toSet());

            if (currentConfig.isEmpty() && candidateBundle.isEmpty()) {
                causal = true;
                break;
            } else if (!Collections.disjoint(currentConfig, candidateBundle)) { // X inter {𝑒1, . . . , 𝑒𝑖−1} = ∅
                causal = true;
                break;
            }
        }
        return causal;
    }

    private boolean respectsFM(Configuration product, Event e) throws ConstraintSolvingException {

        F f = features.get(e);
        if(!product.isSelected(f)){
            return false;
        }
        ConstraintIdentifier constrId = this.fm.addSolverConstraint(eventFexpressions.get(e));
        boolean isSatisfiable = this.fm.isSatisfiable();
        this.fm.removeSolverConstraint(constrId);
        return isSatisfiable;
    }

    private ConfigurationSet getValidProducts(Event event) {

        F f = this.features.get(event);
        FExpression constraint = this.eventFexpressions.get(event).and(new FExpression(f));
        return new ConfigurationSet(this.fm, constraint);
    }

}
