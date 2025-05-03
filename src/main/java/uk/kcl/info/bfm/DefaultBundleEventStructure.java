package uk.kcl.info.bfm;

import java.util.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

public class DefaultBundleEventStructure implements BundleEventStructure{

    private final Map<String, Event> events;
    private final Set<CausalityRelation> allCausalities;
    private final ConflictSet allConflicts;
    private final Table<Set<Event>, Event, CausalityRelation> causalities;

    protected DefaultBundleEventStructure() {
        this.events = new HashMap<>();
        this.allCausalities = new HashSet<>();
        this.allConflicts = new ConflictSet();
        this.causalities = HashBasedTable.create();
    }

    protected Event addEvent(String eventName) {
        return this.events.computeIfAbsent(eventName, Event::new);
    }

    protected CausalityRelation addCausality(Set<Event> bundle, Event target) {
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

    protected CausalityRelation addCausality(CausalityRelation causality) {
        Preconditions.checkNotNull(causality, "Causality may not be null!");
        return addCausality(causality.getBundle(), causality.getTarget());
    }

    protected ConflictSet getConflictSet(){
        return this.allConflicts;
    }

    @Override
    public ConflictSet getConflictSetCopy() {
        ConflictSet copy = new ConflictSet();
        copy.addConflicts(this.allConflicts);
        return copy;
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
    public Iterator<CausalityRelation> getAllCausalitiesOfEvent(Event event) {
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
    public Set<Event> getAllConflictsOfEvent(Event event) {
        return this.allConflicts.getConflicts(event);
    }

    @Override
    public Set<Event> getInitialEvents() {

        Set<Event> events = new HashSet<>();
        for(Event event: this.events.values()){
            if(!this.causalities.containsColumn(event)){
                events.add(event);
            }
        }
        return events;
    }

    protected boolean isConflictFree(Event e, Set<Event> config) {
        for (Event other : config) {
            if (this.areInConflict(e, other)) {
                return false;
            }
        }
        return true;
    }

    protected boolean respectsCausality(Event e, Set<Event> c1) {

        Set<Set<Event>> causes = this.getAllBundles(e); // All X such as X ↦ e
        for (Set<Event> bundle : causes) {
            if (Collections.disjoint(c1, bundle)) { // X inter {𝑒1, . . . , 𝑒𝑖−1} = ∅
                return false;
            }
        }
        return true;
    }

    @Override
    public TreeMap<Integer, Set<Set<Event>>> getAllConfigurations() {
        TreeMap<Integer, Set<Set<Event>>> configurationsBySize = new TreeMap<>();
        buildConfigurations(new LinkedHashSet<>(), new ArrayList<>(this.events.values()), configurationsBySize);
        return configurationsBySize;
    }

    private void buildConfigurations(Set<Event> currentConfig, List<Event> remainingEvents, TreeMap<Integer, Set<Set<Event>>> configurationsBySize) {

        // Store a copy of the current configuration
        Set<Event> configSet = new HashSet<>(currentConfig);
        // Add to TreeMap based on its size
        configurationsBySize.computeIfAbsent(configSet.size(), k -> new HashSet<>()).add(configSet);

        // Iterate over a new list (copy of remainingEvents) to avoid ConcurrentModificationException
        List<Event> remainingEventsList = new ArrayList<>(remainingEvents);
        for (Event e : remainingEventsList) {
            if (isConflictFree(e, currentConfig) && respectsCausality(e, currentConfig)) {
                // If both conditions are satisfied, add the event to the current configuration.
                currentConfig.add(e);
                // Remove event 'e' from remaining events to prevent re-selection in this configuration.
                remainingEvents.remove(e);
                // Recursively build configurations with the updated current configuration and remaining events.
                buildConfigurations(currentConfig, remainingEvents, configurationsBySize);
                // Backtrack: Remove event 'e' from the current configuration to explore other possible configurations.
                currentConfig.remove(e);
                // Add event 'e' back to the remaining events for further exploration.
                remainingEvents.add(e);
            }
        }
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
        return allConflicts.size();
    }

    @Override
    public boolean areInConflict(Event var1, Event var2){
        return this.allConflicts.areInConflict(var1, var2);
    }

    protected Set<Set<Event>> getAllBundles(Event var1){
        return this.causalities.column(var1).keySet();
    }

}
