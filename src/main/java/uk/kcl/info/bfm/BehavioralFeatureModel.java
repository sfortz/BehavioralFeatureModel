package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.Group;
import be.vibes.solver.SolverFacade;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;

import java.util.*;

public class BehavioralFeatureModel extends FeatureModel<BehavioralFeature> implements FeaturedEventStructure<BehavioralFeature> {

    private final Table<Set<Event>, Event, CausalityRelation> causalityTable;

    protected BehavioralFeatureModel() {
        super();
        this.causalityTable = HashBasedTable.create();
    }

    protected BehavioralFeatureModel(SolverFacade solver) {
        super(solver);
        this.causalityTable = HashBasedTable.create();
    }

    protected BehavioralFeatureModel(FeatureModel<?> fm) {

        super();
        this.setNamespace(fm.getNamespace());

        BehavioralFeature root = new BehavioralFeature(fm.getRootFeature());
        this.setRootFeature(root);

        Queue<BehavioralFeature> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {

            BehavioralFeature bf = queue.poll();
            this.getFeatureMap().put(bf.getFeatureName(), bf);

            for (Group<?> group: bf.getChildren()){
                queue.addAll((Collection<? extends BehavioralFeature>) group.getFeatures());
            }
        }

        this.getOwnConstraints().addAll(fm.getOwnConstraints());
        this.setSolver(fm.getSolver());
        this.causalityTable = HashBasedTable.create();
    }

    protected void setCausalityTable() {
        for(CausalityRelation causality: this.getRootFeature().getAllRecursiveCausalities()){
            this.causalityTable.put(causality.getBundle(), causality.getTarget(), causality);
        }
    }

    private BehavioralFeature getRecursiveFeature(BehavioralFeature currentFeature, Event event){

        if(currentFeature.getEventMap().containsKey(event)){
            return currentFeature;
        } else {
            for (Group<?> group : currentFeature.getChildren()) {
                for (Feature<?> bf : group.getFeatures()) {
                    BehavioralFeature result = getRecursiveFeature(this.getFeature(bf.getFeatureName()), event);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public BehavioralFeature getFeature(Event event){
        return getRecursiveFeature(this.getRootFeature(), event);
    }

    @Override
    public FExpression getFExpression(Event event){

        FExpression fexpr = this.getRootFeature().getFExpression(event);
        return Objects.requireNonNullElseGet(fexpr, FExpression::trueValue);
    }

    @Override
    public Event getEvent(String name) {
        Event ev = new Event(name);
        if (this.getRootFeature().getAllRecursiveEvents().contains(ev)){
            return ev;
        } else {return null;}
    }

    @Override
    public Set<Event> getInitialEvents() {
        Set<Event> events = new HashSet<>();
        for(Event event: this.getAllEvents()){
            if(!this.causalityTable.containsColumn(event)){
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public List<Event> getAllEvents() {
        return this.getRootFeature().getAllRecursiveEvents().stream().toList();
    }

    @Override
    public CausalityRelation getCausality(Set<Event> bundle, Event event) {
        return this.causalityTable.row(bundle).get(event);
    }

    @Override
    public Iterator<CausalityRelation> getAllCausalitiesOfEvent(Event event) {
        return this.causalityTable.column(event).values().iterator();
    }

    @Override
    public boolean isInConflict(Event var1, Event var2) {
        return this.getRootFeature().getAllRecursiveConflicts().contains(new ConflictRelation(var1, var2));
    }

    @Override
    public Set<ConflictRelation> getAllConflictsOfEvent(Event event) {

        Set<ConflictRelation> conflicts = new HashSet<>();

        for(ConflictRelation conflict: this.getRootFeature().getAllRecursiveConflicts()){
            if(conflict.getEvent1().equals(event) || conflict.getEvent2().equals(event)){
                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    @Override
    public ConflictRelation getConflict(Event var1, Event var2) {

        ConflictRelation conflict = new ConflictRelation(var1, var2);
        if(this.getRootFeature().getAllRecursiveConflicts().contains(conflict)){
            return conflict;
        }else{
            return null;
        }
    }

    @Override
    public Iterator<Event> events() {
        return this.getRootFeature().getAllRecursiveEvents().iterator();
    }
    @Override
    public Iterator<CausalityRelation> causalities() {
        return this.causalityTable.values().iterator();
    }

    @Override
    public Iterator<ConflictRelation> conflicts() {
        return this.getRootFeature().getAllRecursiveConflicts().iterator();
    }

    // TODO: Implement

    @Override
    public Set<List<Event>> getAllConfigurations() {
        return Set.of();
    }


    @Override
    public FExpression getFexpression(List<Event> config) { //TODO: getFExpr related to a specific config
        return null;
    }

    // TODO: END Implementation


    @Override
    public int getEventsCount() {
        return this.getAllEvents().size();
    }

    @Override
    public int getCausalitiesCount() {
        return this.getRootFeature().getAllRecursiveCausalities().size();
    }

    @Override
    public int getConflictsCount() {
        return this.getRootFeature().getAllRecursiveConflicts().size();
    }

    @Override
    public Iterator<CausalityRelation> getOutgoingCausalities(Event event) {

        Set<CausalityRelation> causalities = new HashSet<>();

        for(CausalityRelation causality: this.causalityTable.values()){
            if(causality.getBundle().contains(event)){
                causalities.add(causality);
            }
        }

        return Iterables.concat(causalities).iterator();
    }

    @Override
    public int getOutgoingCausalityCount(Event event) {

        int i = 0;

        for(CausalityRelation causality: this.causalityTable.values()){
            if(causality.getBundle().contains(event)){
                i++;
            }
        }

        return i;
    }

    @Override
    public Iterator<CausalityRelation> getIncomingCausalities(Event target) {
        return Iterables.concat(this.causalityTable.column(target).values()).iterator();
    }

    @Override
    public int getIncomingCausalityCount(Event target) {
        return this.causalityTable.column(target).size();
    }
}