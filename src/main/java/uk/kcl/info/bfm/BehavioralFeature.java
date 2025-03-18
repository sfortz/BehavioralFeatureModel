package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.Group;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import java.util.*;

public class BehavioralFeature extends Feature implements BundleEventStructure {
    private final Map<Event, FExpression> events;
    private final Set<CausalityRelation> allCausalities;
    private final Set<ConflictRelation> allConflicts;
    private final Table<Set<Event>, Event, CausalityRelation> causalities;

    public BehavioralFeature(String name, Map<Event, FExpression> events) {
        super(name);
        this.events = events;
        this.allCausalities = new HashSet<>();
        this.allConflicts = new HashSet<>();
        this.causalities = HashBasedTable.create();
    }

    public BehavioralFeature(String name) {
        this(name, new HashMap<>());
    }

    public BehavioralFeature(Feature old) {
        this(old.getFeatureName());
        this.setNameSpace(old.getNameSpace());
        this.setRelatedImport(old.getRelatedImport());
        this.setLowerBound(old.getLowerBound());
        this.setUpperBound(old.getUpperBound());
        this.setSubmodelRoot(old.isSubmodelRoot());
        this.setFeatureType(old.getFeatureType());
        this.getAttributes().putAll(old.getAttributes());

        for(Group<Feature> oldGroup : old.getChildren()) {

            Group<BehavioralFeature> group = new Group<>(oldGroup.GROUPTYPE);
            group.setUpperBound(oldGroup.getUpperBound());
            group.setLowerBound(oldGroup.getLowerBound());

            for (Feature oldf : oldGroup.getFeatures()) {
                BehavioralFeature newf = new BehavioralFeature(oldf);
                group.getFeatures().add(newf);
                newf.setParentGroup(group);
            }

            this.getChildren().add(group);
            group.setParentFeature(this);
        }

        this.setParentGroup(old.getParentGroup());
        this.getExclusions().addAll(old.getExclusions());
        this.getRequirements().addAll(old.getRequirements());
    }

    // TODO: Check if I have all functionalities from FES

    public Map<Event, FExpression> getEvents() {
        return events;
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

    public FExpression getFExpression(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        FExpression fexpr = this.events.get(event);
        if (fexpr == null) {
            fexpr = FExpression.trueValue();
        }

        return fexpr;
    }

    public Set<BehavioralFeature> getAllRecursiveFeatures() {
        Set<BehavioralFeature> features = new HashSet<>();
        features.add(this);

        for (Group<BehavioralFeature> g : this.getChildren()) {
            for(BehavioralFeature f: g.getFeatures()){
                features.addAll((f).getAllRecursiveFeatures());
            }
            //g.getFeatures().forEach(f -> features.addAll(((BehavioralFeature) (Feature) f).getAllRecursiveFeatures()));
        }

        return features;
    }

    public Set<Event> getAllRecursiveEvents() {
        Set<Event> ev = new HashSet<>(this.events.keySet());

        for (BehavioralFeature f : this.getAllRecursiveFeatures()) {
            ev.addAll(f.getEvents().keySet());
        }

        return ev;
    }

    protected CausalityRelation addCausality(Set<Event> bundle, Event target) {
        Preconditions.checkNotNull(bundle, "Bundle may not be null!");
        Preconditions.checkNotNull(target, "Targeted event may not be null!");

        Set<Event> ev = this.getAllRecursiveEvents();
        Preconditions.checkArgument(ev.contains(target), "Event does not belong to this behavioral feature model or any of its subtree!");
        Preconditions.checkArgument(ev.containsAll(bundle), "Some events in the bundle do not belong to this behavioral feature model or any of its subtree!");

        CausalityRelation causality = this.getCausality(bundle, target);

        if (causality == null) {
            causality = new CausalityRelation(bundle, target);
            if (!this.causalities.contains(bundle, target)) {
                this.causalities.put(bundle, target, causality);
            }
            this.allCausalities.add(causality);
        }

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
        this.allConflicts.add(conflict);

        return conflict;
    }

    protected ConflictRelation addConflict(ConflictRelation conflict) {
        Preconditions.checkNotNull(conflict, "Conflict may not be null!");
        return addConflict(conflict.getEvent1(), conflict.getEvent2());
    }

    @Override
    public Iterator<Event> events() {
        return this.events.keySet().iterator();
    }

    @Override
    public List<Event> getAllEvents() {
        return this.events.keySet().stream().toList();
    }

    @Override
    public Event getEvent(String name) {

        Event ev = new Event(name);
        if (this.events.containsKey(ev)){
            return ev;
        } else {return null;}
    }

    @Override
    public Iterator<CausalityRelation> causalities() {
        return this.allCausalities.iterator();
    }

    @Override
    public Iterator<ConflictRelation> conflicts() {
        return this.allConflicts.iterator();
    }

    @Override
    public Iterator<CausalityRelation> getCausalities(Event event) {
        return this.causalities.column(event).values().iterator();
    }

    @Override
    public CausalityRelation getCausality(Set<Event> bundle, Event event) {
        return this.causalities.row(bundle).get(event);
    }

    @Override
    public Iterator<CausalityRelation> getOutgoingCausalities(Event event) {

        Set<CausalityRelation> causalities = new HashSet<>();

        for(CausalityRelation causality: this.allCausalities){
            if(causality.getBundle().contains(event)){
                causalities.add(causality);
            }
        }

        return Iterables.concat(causalities).iterator();
    }

    @Override
    public int getOutgoingCausalityCount(Event event) {

        Set<CausalityRelation> causalities = new HashSet<>();

        for(CausalityRelation causality: this.allCausalities){
            if(causality.getBundle().contains(event)){
                causalities.add(causality);
            }
        }

        return causalities.size();
    }

    @Override
    public Iterator<CausalityRelation> getIncomingCausalities(Event target) {
        return Iterables.concat(this.causalities.column(target).values()).iterator();
    }

    @Override
    public int getIncomingCausalityCount(Event target) {
        return this.causalities.column(target).size();
    }

    @Override
    public ConflictRelation getConflict(Event var1, Event var2) {

        ConflictRelation conflict = new ConflictRelation(var1, var2);
        if(this.allConflicts.contains(conflict)){
            return conflict;
        }else{
            return null;
        }
    }

    @Override
    public Set<ConflictRelation> getConflicts(Event event) {

        Set<ConflictRelation> conflicts = new HashSet<>();

        for(ConflictRelation conflict: this.allConflicts){
            if(conflict.getEvent1().equals(event) || conflict.getEvent2().equals(event)){
                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    @Override
    public Set<Event> getInitialEvents() {

        Set<Event> events = new HashSet<>();
        for(Event event: this.events.keySet()){
            if(this.causalities.containsColumn(event)){
                events.add(event);
            }
        }
        return events;
    }

    @Override //TODO: Implement.
    public Set<List<Event>> getAllConfigurations() {
        return null;
    }

    @Override
    public int getEventsCount() {
        return this.events.size();
    }

    @Override
    public int getCausalitiesCount() {
        return this.allCausalities.size();
    }

    @Override
    public int getConflictsCount() {
        return this.allConflicts.size();
    }

    @Override
    public boolean isInConflict(Event var1, Event var2) {
        return this.allConflicts.contains(new ConflictRelation(var1, var2));
    }

    //TODO: Override toString to Add the behavioural part

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BehavioralFeature that = (BehavioralFeature) o;
        return Objects.equals(events, that.events) && Objects.equals(allCausalities, that.allCausalities) && Objects.equals(allConflicts, that.allConflicts) && Objects.equals(causalities, that.causalities);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(events, allCausalities, allConflicts, causalities);
    }
}
