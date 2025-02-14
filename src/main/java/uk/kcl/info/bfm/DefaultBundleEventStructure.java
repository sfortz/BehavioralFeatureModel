package uk.kcl.info.bfm;

import java.util.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;

public class DefaultBundleEventStructure implements BundleEventStructure{

    private final Map<String, Event> events;
    private final Set<CausalityRelation> allCausalities;
    private final Set<ConflictRelation> allConflicts;
    private final Table<Set<Event>, Event, CausalityRelation> causalities;

    DefaultBundleEventStructure() {
        //Preconditions.checkNotNull(initialState, "InitialState may not be null!");
        //this.initialState = new State(initialState);
        this.events = new HashMap<>();
        //this.states.put(initialState, this.initialState);
        this.allCausalities = new HashSet<>();
        this.allConflicts = new HashSet<>();
        this.causalities = HashBasedTable.create();
    }

    Event addEvent(String eventName) {
        return this.events.computeIfAbsent(eventName, Event::new);
    }

    CausalityRelation addCausality(Set<Event> bundle, Event target) {
        Preconditions.checkNotNull(bundle, "Bundle may not be null!");
        Preconditions.checkNotNull(target, "Targeted event may not be null!");
        Preconditions.checkArgument(this.events.containsValue(target), "Event does not belong to this bundle event structure!");
        Preconditions.checkArgument(this.events.values().containsAll(bundle), "Some events in the bundle do not belong to this event structure!");

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
        Preconditions.checkArgument(this.events.containsValue(event1), "Event does not belong to this bundle event structure!");
        Preconditions.checkArgument(this.events.containsValue(event2), "Event does not belong to this bundle event structure!");

        ConflictRelation conflict = new ConflictRelation(event1, event2);
        this.allConflicts.add(conflict);

        return conflict;
    }

    @Override
    public Iterator<Event> events() {
        return this.events.values().iterator();
    }

    @Override
    public List<Event> getAllEvents() {
        return this.events.values().stream().toList();
    }

    @Override
    public Event getEvent(String name) {
        return this.events.get(name);
    }

    @Override
    public Iterator<CausalityRelation> causalities() {
        return this.allCausalities.iterator();
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
        for(Event event: this.events.values()){
            if(this.causalities.containsColumn(event)){
                events.add(event);
            }
        }
        return events;
    }

    private boolean isConflictFree(Event e, List<Event> config) {
        for (Event other : config) {
            if (this.allConflicts.contains(new ConflictRelation(e, other))) {
                return false;
            }
        }
        return true;
    }

    private boolean respectsCausality(Event e, List<Event> config) {
        Set<Set<Event>> causes = this.causalities.column(e).keySet(); // All X such as X ↦ e
        for (Set<Event> bundle : causes) {
            if (Collections.disjoint(config, bundle)) { // X inter {𝑒1, . . . , 𝑒𝑖−1} = ∅
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<List<Event>> getAllConfigurations() {
        Set<List<Event>> allConfigs = new HashSet<>();
        Set<Event> allEvents = new HashSet<>(this.getAllEvents());
        buildConfigurations(new ArrayList<>(), allEvents, allConfigs);
        return allConfigs;
    }

    private void buildConfigurations(List<Event> currentConfig, Set<Event> remainingEvents, Set<List<Event>> allConfigs) {
        // Add the current configuration to the allConfigs set. A new list is created to avoid modifying the original configuration.
        allConfigs.add(new ArrayList<>(currentConfig));

        // Iterate over a copy of remaining events to prevent concurrent modification issues.
        for (Event e : new HashSet<>(remainingEvents)) {
            if (isConflictFree(e, currentConfig) && respectsCausality(e, currentConfig)) {
                // If both conditions are satisfied, add the event to the current configuration.
                currentConfig.add(e);
                // Remove event 'e' from remaining events to prevent re-selection in this configuration.
                remainingEvents.remove(e);
                // Recursively build configurations with the updated current configuration and remaining events.
                buildConfigurations(currentConfig, remainingEvents, allConfigs);
                // Backtrack: Remove event 'e' from the current configuration to explore other possible configurations.
                currentConfig.remove(e);
                // Add event 'e' back to the remaining events for further exploration.
                remainingEvents.add(e);
            }
        }
    }

    /*
    public Set<List<Event>> getAllConfigurations() {
        Set<Event> allEvents = new HashSet<>(this.getAllEvents());
        Set<List<Event>> allConfigs = new HashSet<>();

        for (Set<Event> candidateConfig : powerSet(allEvents)) {
            List<Event> orderedConfig = new ArrayList<>(candidateConfig);
            if (isConflictFree(orderedConfig) && respectsCausality(orderedConfig)) {
                allConfigs.add(orderedConfig);
            }
        }

        return allConfigs;
    }

    private boolean isConflictFree(List<Event> config) {
        for (Event e1 : config) {
            for (Event e2 : config) {
                if (isInConflict(e1, e2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean respectsCausality(List<Event> config) {

        for (Event e : config) {
            Set<Set<Event>> causes = getCauses(e);  // All X such as X ↦ e
            int index = config.indexOf(e);
            List<Event> subConfig = config.subList(0, index);
            for(Set<Event> bundle: causes){
                return !Collections.disjoint(subConfig, bundle); // X inter {𝑒1, . . . , 𝑒𝑖−1} = ∅
            }
        }
        return true;
    }

    */

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
}
