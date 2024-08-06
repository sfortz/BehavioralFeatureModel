package uk.kcl.info.bfm;

import be.vibes.fexpression.DimacsModel;
import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.Sat4JSolverFacade;
import be.vibes.solver.exception.SolverInitializationException;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static be.vibes.fexpression.DimacsModel.createFromTvlParserMappingFile;

public class DefaultBehavioralFeatureModel extends Sat4JSolverFacade implements BehavioralFeatureModel{

    private final List<Feature> features;
    private final Map<String, Event> events;
    private final Map<Feature, Set<Event>> eventMapping;
    private final Set<CausalityRelation> allCausalities;
    private final Map<Event, Set<CausalityRelation>> causalityMapping;
    private final Set<ConflictRelation> allConflicts;
    private final Map<Event, Set<ConflictRelation>> conflictMapping;

    DefaultBehavioralFeatureModel(DimacsModel model) throws SolverInitializationException {
        super(model);
        this.features = model.getFeatures().stream().map(Feature::new).collect(Collectors.toList());
        this.events = new HashMap<>();
        this.allCausalities = new HashSet<>();
        this.allConflicts = new HashSet<>();
        this.eventMapping = new HashMap<>();
        this.causalityMapping= new HashMap<>();
        this.conflictMapping = new HashMap<>();
    }

    DefaultBehavioralFeatureModel(File featureMapping) throws SolverInitializationException, IOException {
        super(featureMapping);
        DimacsModel model = createFromTvlParserMappingFile(featureMapping);
        this.features = model.getFeatures().stream().map(Feature::new).collect(Collectors.toList());
        this.events = new HashMap<>();
        this.allCausalities = new HashSet<>();
        this.allConflicts = new HashSet<>();
        this.eventMapping = new HashMap<>();
        this.causalityMapping= new HashMap<>();
        this.conflictMapping = new HashMap<>();
    }

    DefaultBehavioralFeatureModel(File dimacsModel, File featureMapping) throws SolverInitializationException, IOException {
        super(dimacsModel, featureMapping);
        DimacsModel model = createFromTvlParserMappingFile(featureMapping);
        this.features = model.getFeatures().stream().map(Feature::new).collect(Collectors.toList());
        this.events = new HashMap<>();
        this.allCausalities = new HashSet<>();
        this.allConflicts = new HashSet<>();
        this.eventMapping = new HashMap<>();
        Set<Event> mapping = new HashSet<>();
        this.causalityMapping= new HashMap<>();
        this.conflictMapping = new HashMap<>();
    }

    DefaultBehavioralFeatureModel(String dimacsModel, String featureMapping) throws SolverInitializationException, IOException {
        this(new File(dimacsModel), new File(featureMapping));
    }

    Event addEvent(Feature feature, Event event) {
        Preconditions.checkNotNull(feature, "feature may not be null!");
        Preconditions.checkNotNull(event, "event may not be null!");
        Preconditions.checkArgument(this.features.contains(feature), "Feature " + feature + " does not belong to the FM!");

        if (this.events.containsValue(event)) {
            this.events.put(event.getName(), event);
            this.eventMapping.get(feature).add(event);
            return event;
        }
        return null;
    }

    CausalityRelation addCausality(Event source, Event target){
        Preconditions.checkNotNull(source, "Source event may not be null!");
        Preconditions.checkNotNull(target, "Target event not be null!");
        Preconditions.checkArgument(this.events.containsValue(source), "Source event does not belong to this BFM!");
        Preconditions.checkArgument(this.events.containsValue(target), "Target event does not belong to this BFM!");

        CausalityRelation causality = new CausalityRelation(source, target);
        this.allCausalities.add(causality);
        this.causalityMapping.get(source).add(causality);
        return causality;
    }

    ConflictRelation addConflict(Event ev1, Event ev2){
        Preconditions.checkNotNull(ev1, "The first event event may not be null!");
        Preconditions.checkNotNull(ev2, "The second event event not be null!");
        Preconditions.checkArgument(this.events.containsValue(ev1), "The first event does not belong to this BFM!");
        Preconditions.checkArgument(this.events.containsValue(ev2), "The second event does not belong to this BFM!");

        ConflictRelation conflict = new ConflictRelation(ev1, ev2);
        if (!this.allConflicts.contains(conflict)) {
            this.allConflicts.add(conflict);
            this.conflictMapping.get(ev1).add(conflict);
            this.conflictMapping.get(ev2).add(conflict);
        }
        return conflict;
    }

    @Override
    public Event getInitialEvent() {
        return initialEvent;
    }

    public Feature getFeature(String featureName){

        Feature feature = new Feature(featureName);
        if(this.features.contains(feature)){
            return feature;
        } else {
            return null;
        }
    }

    public List<Feature> getFeatures() {
        return features;
    }

    @Override
    public Iterator<Event> events() {
        return this.events.values().iterator();
    }

    @Override
    public Event getEvent(String var1) {
        return events.get(var1);
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
    public CausalityRelation getCausality(Event var1, Event var2) {

        for (CausalityRelation causality: causalityMapping.get(var1)){
            if(causality.getTarget().equals(var2)){
                return causality;
            }
        }
        return null;
    }

    @Override
    public ConflictRelation getConflict(Event var1, Event var2) {
        for (ConflictRelation conflict: conflictMapping.get(var1)){
            if(conflict.getEvent2().equals(var2)){
                return conflict;
            }
        }
        return null;
    }

    @Override
    public boolean hasCausality(Event var1, Event var2) {
        return !(this.getCausality(var1,var2) == null);
    }

    @Override
    public boolean hasConflict(Event var1, Event var2) {
        return !(this.getConflict(var1,var2) == null);
    }

    @Override
    public Iterator<CausalityRelation> getCausalities(Event var1) {
        return causalityMapping.get(var1).iterator();
    }

    @Override
    public Iterator<ConflictRelation> getConflicts(Event var1) {
        return conflictMapping.get(var1).iterator();
    }

    @Override
    public Iterator<Event> getOutgoing(Event var1) {

        Set<Event> targets = new HashSet<>();
        for (CausalityRelation causality : this.causalityMapping.get(var1)) {
            targets.add(causality.getTarget());
        }

        return targets.iterator();
    }

    @Override
    public int getOutgoingCount(Event var1) {

        Set<Event> targets = new HashSet<>();
        for (CausalityRelation causality : this.causalityMapping.get(var1)) {
            targets.add(causality.getTarget());
        }

        return targets.size();

    }

    @Override
    public Iterator<Event> getIncoming(Event var1) {

        Set<Event> sources = new HashSet<>();
        for (CausalityRelation causality : this.allCausalities) {
            if(causality.getTarget().equals(var1)){
                sources.add(causality.getSource());
            }
        }

        return sources.iterator();
    }

    @Override
    public int getIncomingCount(Event var1) {

        Set<Event> sources = new HashSet<>();
        for (CausalityRelation causality : this.allCausalities) {
            if(causality.getTarget().equals(var1)){
                sources.add(causality.getSource());
            }
        }

        return sources.size();
    }

    @Override
    public int getEventsCount() {
        return events.size();
    }

    @Override
    public int getCausalitiesCount() {
        return allCausalities.size();
    }

    @Override
    public int getConflictsCount() {
        return allConflicts.size();
    }

    @Override
    public FExpression getFExpression(Event event) {
        return event.getFexpr();
    }
}
