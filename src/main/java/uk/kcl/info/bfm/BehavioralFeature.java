package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.FExpressionVisitorWithReturn;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.exception.FExpressionException;
import be.vibes.solver.Group;
import com.google.common.base.Preconditions;

import java.util.*;

public class BehavioralFeature extends Feature<BehavioralFeature> {
    private final Map<Event, FExpression> events;
    private final Set<CausalityRelation> causalities;
    private final ConflictSet conflicts;

    public BehavioralFeature(String name, Map<Event, FExpression> events) {
        super(name);
        this.events = events;
        this.causalities = new HashSet<>();
        this.conflicts = new ConflictSet();
    }

    public BehavioralFeature(String name) {
        this(name, new HashMap<>());
    }

    public BehavioralFeature(Feature<?> old) {
        this(old.getFeatureName());
        this.setNameSpace(old.getNameSpace());
        this.setRelatedImport(old.getRelatedImport());
        this.setLowerBound(old.getLowerBound());
        this.setUpperBound(old.getUpperBound());
        this.setSubmodelRoot(old.isSubmodelRoot());
        this.setFeatureType(old.getFeatureType());
        this.getAttributes().putAll(old.getAttributes());

        for(Group<?> oldGroup : old.getChildren()) {

            Group<BehavioralFeature> group = new Group<>(oldGroup.GROUPTYPE);
            group.setUpperBound(oldGroup.getUpperBound());
            group.setLowerBound(oldGroup.getLowerBound());

            for (Feature<?> oldf : oldGroup.getFeatures()) {
                BehavioralFeature newf = new BehavioralFeature(oldf);
                group.getFeatures().add(newf);
                newf.setParentGroup(group);
            }

            this.getChildren().add(group);
            group.setParentFeature(this);
        }

        this.getConstraints().addAll(old.getConstraints());
    }

    protected Event addEvent(String eventName, FExpression fexpr) {
        Preconditions.checkNotNull(eventName, "Event name may not be null!");
        Preconditions.checkNotNull(fexpr, "FExpression name may not be null!");
        Event ev = new Event(eventName);
        FExpression fe = getBFexpFromFM(fexpr);
        this.events.put(ev,fe);
        return ev;
    }

    private BehavioralFeature getFeatureFromFM(Feature<?> feature){
        for(BehavioralFeature bf: this.getAllRecursiveFeatures()){
            if(bf.getFeatureName().equalsIgnoreCase(feature.getFeatureName())){
                return bf;
            }
        }
        return null;
    }

    private class BFexpFromFMBuilder implements FExpressionVisitorWithReturn<FExpression> {

        private FExpression fexp;

        public BFexpFromFMBuilder() {}

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
            return new FExpression(getFeatureFromFM(feature));
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

    private FExpression getBFexpFromFM(FExpression fexp) {
        try {
            return fexp.accept(new BFexpFromFMBuilder());
        } catch (FExpressionException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    protected CausalityRelation addCausality(Set<Event> bundle, Event target) {
        Preconditions.checkNotNull(bundle, "Bundle may not be null!");
        Preconditions.checkNotNull(target, "Targeted event may not be null!");

        Set<Event> ev = this.getAllRecursiveEvents();
        Preconditions.checkArgument(ev.contains(target), "Event does not belong to this behavioral feature model or any of its subtree!");
        Preconditions.checkArgument(ev.containsAll(bundle), "Some events in the bundle do not belong to this behavioral feature model or any of its subtree!");

        CausalityRelation causality = new CausalityRelation(bundle, target);
        this.causalities.add(causality);
        return causality;
    }

    protected CausalityRelation addCausality(CausalityRelation causality) {
        Preconditions.checkNotNull(causality, "Causality may not be null!");
        return addCausality(causality.getBundle(), causality.getTarget());
    }

    ConflictSet getConflictSet(){
        return this.conflicts;
    }

    public ConflictSet getRootConflictSetCopy() {
        ConflictSet copy = new ConflictSet();
        copy.addConflicts(this.conflicts);
        return copy;
    }

    public Set<CausalityRelation> getCausalities() {
        return causalities;
    }

    public Map<Event, FExpression> getEventMap() {
        return events;
    }

    public Set<BehavioralFeature> getAllRecursiveFeatures() {
        Set<BehavioralFeature> features = new HashSet<>();
        features.add(this);

        for (Group<BehavioralFeature> g : this.getChildren()) {
            for(BehavioralFeature f: g.getFeatures()){
                features.addAll((f).getAllRecursiveFeatures());
            }
        }

        return features;
    }

    public BehavioralFeature getFeature(String name){
        for(BehavioralFeature bf :this.getAllRecursiveFeatures()){
            if(bf.getFeatureName().equals(name)){
                return bf;
            }
        }
        return null;
    }

    public Set<Event> getAllRecursiveEvents() {
        Set<Event> ev = new HashSet<>(this.events.keySet());

        for (Group<BehavioralFeature> g : this.getChildren()) {
            for(BehavioralFeature f: g.getFeatures()){
                ev.addAll(f.getEventMap().keySet());
                ev.addAll(f.getAllRecursiveEvents());
            }
        }

        return ev;
    }

    public Set<CausalityRelation> getAllRecursiveCausalities() {
        Set<CausalityRelation> causes = new HashSet<>(this.causalities);

        for (Group<BehavioralFeature> g : this.getChildren()) {
            for(BehavioralFeature f: g.getFeatures()){
                causes.addAll(f.getCausalities());
                causes.addAll(f.getAllRecursiveCausalities());
            }
        }

        return causes;
    }

    public ConflictSet getAllRecursiveConflicts() {
        ConflictSet allConflicts = new ConflictSet();
        allConflicts.addConflicts(this.conflicts);

        this.getChildren().stream()
                .flatMap(group -> group.getFeatures().stream())
                .forEach(feature -> {
                    allConflicts.addConflicts(feature.getConflictSet());
                    allConflicts.addConflicts(feature.getAllRecursiveConflicts());
                });

        return allConflicts;
    }

    public FExpression getFExpression(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");

        if(this.getEventMap().containsKey(event)){
            return this.getEventMap().get(event);
        } else {
            for (Group<BehavioralFeature> g : this.getChildren()) {
                for(BehavioralFeature f: g.getFeatures()){
                    if(f.getFExpression(event) != null){
                        return f.getFExpression(event);
                    }
                }
            }
        }

        return null;
    }

    public int getEventsCount() {
        return this.events.size();
    }

    public int getCausalitiesCount() {
        return this.causalities.size();
    }

    public int getConflictsCount() {
        return conflicts.size();
    }

    public int getMaxConflictSize(){
        return conflicts.maxConflictSize();
    }

    //TODO: Override toString, equals and hashcode to Add the behavioural part
}
