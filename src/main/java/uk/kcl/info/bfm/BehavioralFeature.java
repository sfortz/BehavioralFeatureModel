package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.Group;
import com.google.common.base.Preconditions;

import java.util.*;

public class BehavioralFeature extends Feature<BehavioralFeature> {
    private final Map<Event, FExpression> events;
    private final Set<CausalityRelation> causalities;
    private final Set<ConflictRelation> conflicts;

    public BehavioralFeature(String name, Map<Event, FExpression> events) {
        super(name);
        this.events = events;
        this.causalities = new HashSet<>();
        this.conflicts = new HashSet<>();
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

        this.getExclusions().addAll(old.getExclusions());
        this.getRequirements().addAll(old.getRequirements());
    }

    protected Event addEvent(String eventName, FExpression fexpr) {

        Event ev = new Event(eventName);
        if (this.events.containsKey(ev)){
            this.setFExpression(ev,fexpr);
        } else {
            this.events.put(ev,fexpr);
        }
        return ev;
    }

    protected void setFExpression(Event event, FExpression fexpr) {
        Preconditions.checkArgument(this.events.containsKey(event), "Event does not belong to this behavioral feature model!");
        this.events.put(event,fexpr);
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

    protected ConflictRelation addConflict(Event event1, Event event2) {
        Preconditions.checkNotNull(event1, "Event may not be null!");
        Preconditions.checkNotNull(event2, "Event may not be null!");

        Set<Event> ev = this.getAllRecursiveEvents();
        Preconditions.checkArgument(ev.contains(event1), event1 + " does not belong to this behavioral feature model or any of its subtree!");
        Preconditions.checkArgument(ev.contains(event2), event2 + " does not belong to this behavioral feature model or any of its subtree!");

        ConflictRelation conflict = new ConflictRelation(event1, event2);
        this.conflicts.add(conflict);

        return conflict;
    }

    protected ConflictRelation addConflict(ConflictRelation conflict) {
        Preconditions.checkNotNull(conflict, "Conflict may not be null!");
        return addConflict(conflict.getEvent1(), conflict.getEvent2());
    }

    public Set<CausalityRelation> getCausalities() {
        return causalities;
    }

    public Set<ConflictRelation> getConflicts() {
        return conflicts;
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

    public Set<ConflictRelation> getAllRecursiveConflicts() {
        Set<ConflictRelation> conflicts = new HashSet<>(this.conflicts);

        for (Group<BehavioralFeature> g : this.getChildren()) {
            for(BehavioralFeature f: g.getFeatures()){
                conflicts.addAll(f.getConflicts());
                conflicts.addAll(f.getAllRecursiveConflicts());
            }
        }

        return conflicts;
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
        return this.conflicts.size();
    }

    //TODO: Override toString to Add the behavioural part

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BehavioralFeature that = (BehavioralFeature) o;
        return Objects.equals(events, that.events) && Objects.equals(causalities, that.causalities) && Objects.equals(conflicts, that.conflicts);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(events, causalities, conflicts);
    }
}
