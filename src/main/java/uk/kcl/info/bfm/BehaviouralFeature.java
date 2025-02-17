package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import de.vill.config.Configuration;
import de.vill.model.Feature;
import de.vill.model.Group;
import de.vill.util.Util;

import java.util.*;

public class BehaviouralFeature extends Feature implements BundleEventStructure {
    private final Map<Event, FExpression> events;
    private final Set<CausalityRelation> allCausalities;
    private final Set<ConflictRelation> allConflicts;
    private final Table<Set<Event>, Event, CausalityRelation> causalities;

    public BehaviouralFeature(String name, Map<Event, FExpression> events) {
        super(name);
        this.events = events;
        this.allCausalities = new HashSet<>();
        this.allConflicts = new HashSet<>();
        this.causalities = HashBasedTable.create();
    }

    public BehaviouralFeature(String name) {
        this(name, new HashMap<>());
    }

    public Map<Event, FExpression> getEvents() {
        return events;
    }

    Event addEvent(String eventName, FExpression fexpr) {

        Event ev = new Event(eventName);
        if (this.events.containsKey(ev)){
            this.setFExpression(ev,fexpr);
        } else {
            this.events.put(ev,fexpr);
        }
        return ev;
    }

    void setFExpression(Event event, FExpression fexpr) {
        Preconditions.checkArgument(this.events.containsKey(event), "Event does not belong to this behavioral feature model!");
        this.events.put(event,fexpr);
    }


    Set<BehaviouralFeature> getAllRecursiveFeatures() {
        Set<BehaviouralFeature> features = new HashSet<>();
        features.add(this);

        for (Group g : this.getChildren()) {
            g.getFeatures().forEach(f -> features.addAll(((BehaviouralFeature) f).getAllRecursiveFeatures()));
        }

        return features;
    }

    Set<Event> getAllRecursiveEvents() {
        Set<Event> ev = new HashSet<>(this.events.keySet());

        for (BehaviouralFeature f : this.getAllRecursiveFeatures()) {
            ev.addAll(f.getEvents().keySet());
        }

        return ev;
    }

    CausalityRelation addCausality(Set<Event> bundle, Event target) {
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

    ConflictRelation addConflict(Event event1, Event event2) {
        Preconditions.checkNotNull(event1, "Event may not be null!");
        Preconditions.checkNotNull(event2, "Event may not be null!");

        Set<Event> ev = this.getAllRecursiveEvents();
        Preconditions.checkArgument(ev.contains(event1), "Event does not belong to this behavioral feature model or any of its subtree!");
        Preconditions.checkArgument(ev.contains(event2), "Event does not belong to this behavioral feature model or any of its subtree!");

        ConflictRelation conflict = new ConflictRelation(event1, event2);
        this.allConflicts.add(conflict);

        return conflict;
    }

    @Override
    public Iterator<Event> events() {
        return this.events.keySet().iterator();
    }

    @Override
    public List<Event> getAllEvents() {
        return null;
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

    @Override
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

    private String cardinalityToString() {
        StringBuilder result = new StringBuilder();
        if (!(this.getUpperBound() == null & this.getLowerBound() == null)) {
            result.append(" cardinality [");
            if (this.getLowerBound().equals(this.getUpperBound())) {
                result.append(this.getLowerBound());
            } else {
                result.append(this.getLowerBound());
                result.append("..");
                result.append(this.getUpperBound());
            }

            result.append("] ");
        }

        return result.toString();
    }

    private String attributesToString(boolean withSubmodels, String currentAlias) {
        StringBuilder result = new StringBuilder();
        if (!this.getAttributes().isEmpty()) {
            result.append(" {");
            this.getAttributes().forEach((k, v) -> {
                result.append(Util.addNecessaryQuotes(k));
                result.append(' ');
                result.append(v.toString(withSubmodels, currentAlias));
                result.append(", ");
            });
            result.deleteCharAt(result.length() - 1);
            result.deleteCharAt(result.length() - 1);
            result.append("}");
        }

        return result.toString();
    }

    @Override
    public String toStringAsRoot(String currentAlias) {
        StringBuilder result = new StringBuilder();
        if (this.getFeatureType() != null) {
            result.append(this.getFeatureType().getName()).append(" ");
        }

        result.append(Util.addNecessaryQuotes(this.getFeatureName()));
        result.append(this.cardinalityToString());
        result.append(this.attributesToString(false, currentAlias));
        result.append(Configuration.getNewlineSymbol());
        Iterator var3 = this.getChildren().iterator();

        while(var3.hasNext()) {
            Group group = (Group)var3.next();
            result.append(Util.indentEachLine(group.toString(false, currentAlias)));
        }

        return result.toString();
    }

    @Override
    public String toString(boolean withSubmodels, String currentAlias) {
        StringBuilder result = new StringBuilder();
        if (this.getFeatureType() != null) {
            result.append(this.getFeatureType().getName()).append(" ");
        }

        if (withSubmodels) {
            result.append(Util.addNecessaryQuotes(this.getFullReference()));
        } else {
            result.append(Util.addNecessaryQuotes(this.getReferenceFromSpecificSubmodel(currentAlias)));
        }

        result.append(this.cardinalityToString());
        if (!this.isSubmodelRoot() || withSubmodels) {
            result.append(this.attributesToString(withSubmodels, currentAlias));
            result.append(Configuration.getNewlineSymbol());
            Iterator var4 = this.getChildren().iterator();

            while(var4.hasNext()) {
                Group group = (Group)var4.next();
                result.append(Util.indentEachLine(group.toString(withSubmodels, currentAlias)));
            }
        }

        return result.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BehaviouralFeature that = (BehaviouralFeature) o;
        return Objects.equals(events, that.events) && Objects.equals(allCausalities, that.allCausalities) && Objects.equals(allConflicts, that.allConflicts) && Objects.equals(causalities, that.causalities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events, allCausalities, allConflicts, causalities);
    }
}
