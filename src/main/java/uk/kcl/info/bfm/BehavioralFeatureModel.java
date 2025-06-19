package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.ConfigurationSet;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.Group;
import be.vibes.solver.SolverFacade;
import be.vibes.solver.exception.ConstraintSolvingException;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;

import java.util.*;
import java.util.stream.Collectors;

public class BehavioralFeatureModel extends FeatureModel<BehavioralFeature> implements FeaturedEventStructure<BehavioralFeature> {

    private final Table<Set<Event>, Event, CausalityRelation> causalityTable;

    private Map<Set<Event>, FExpression> configFexpressions;

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
        Preconditions.checkNotNull(event, "Event may not be null!");
        return getRecursiveFeature(this.getRootFeature(), event);
    }

    @Override
    public FExpression getFExpression(Event event){
        Preconditions.checkNotNull(event, "Event may not be null!");
        BehavioralFeature bf = this.getFeature(event);
        return bf.getFExpression(event);
    }

    @Override
    public FExpression getFExpression(Set<Event> config) {
        return this.configFexpressions.get(config);
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
    public boolean areInConflict(Event var1, Event var2) {
        return this.getRootFeature().getAllRecursiveConflicts().areInConflict(var1, var2);
    }

    @Override
    public Set<Event> getAllConflictsOfEvent(Event event) {
        return this.getRootFeature().getAllRecursiveConflicts().getConflicts(event);
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
    public ConflictSet getConflictSetCopy() {
        return this.getRootFeature().getRootConflictSetCopy();
    }

    @Override
    public TreeMap<Integer, Set<Set<Event>>> getAllConfigurations() {
        TreeMap<Integer, Set<Set<Event>>> configurationsBySize = new TreeMap<>();
        this.configFexpressions = new HashMap<>();
        this.configFexpressions.put(new HashSet<>(), FExpression.trueValue());
        try {
            buildProductConfigurations(new LinkedHashSet<>(), new ArrayList<>(this.getAllEvents()), configurationsBySize);
            this.resetSolver();
        } catch (ConstraintSolvingException e) {
            throw new IllegalStateException("Error solving constraints: " + e.getMessage(), e.getCause());
        }
        return configurationsBySize;
    }

    private void buildProductConfigurations(Set<Event> currentConfig, List<Event> remainingEvents, TreeMap<Integer, Set<Set<Event>>> configurationsBySize) throws ConstraintSolvingException {

        // Store a copy of the current configuration
        Set<Event> configSet = new HashSet<>(currentConfig);
        // Add to TreeMap based on its size
        configurationsBySize.computeIfAbsent(configSet.size(), k -> new HashSet<>()).add(configSet);

        // Create a copy of remaining events to avoid concurrent modification
        List<Event> remainingEventsList = new ArrayList<>(remainingEvents);
        for (Event e : remainingEventsList) {
            if (isConflictFree(e, currentConfig)) {
                List<FExpression> products = getValidProducts(e, currentConfig); // The products containing e parent feature,  satisfying the fexpr associated to e AND the fexpr associated to currentConfig
                for (FExpression productFExp : products) {
                    if (respectsCausality(e, currentConfig, productFExp)) {
                        // If both conditions are satisfied, add the event to the current configuration.
                        currentConfig.add(e);
                        // Remove event 'e' from remaining events to prevent re-selection in this configuration.
                        remainingEvents.remove(e);
                        // Concatenate FExpression
                        this.configFexpressions.merge(new HashSet<>(currentConfig), productFExp, (oldValue, newValue) -> oldValue.or(newValue).applySimplification().toCnf());
                        // Recursively build configurations with the updated current configuration and remaining events.
                        buildProductConfigurations(currentConfig, remainingEvents, configurationsBySize);
                        // Backtrack: Remove event 'e' from the current configuration to explore other possible configurations.
                        currentConfig.remove(e);
                        // Add event 'e' back to the remaining events for further exploration.
                        remainingEvents.add(e);
                    }
                }
            }
        }
    }

    //TODO: Check correctness
    protected boolean isConflictFree(Event e, Set<Event> config) {
        for (Event other : config) {
            if (this.areInConflict(e, other)) {
                return false;
            }
        }
        return true;
    }

    protected boolean respectsCausality(Event e, Set<Event> config, FExpression productFexpr) {
        Set<Set<Event>> causes = this.getAllBundles(e); // All X such as X ↦ e

        for (Set<Event> bundle : causes) {

            // Restrict bundle to only events whose features are in the product
            Set<Event> restrictedBundle = bundle.stream()
                    .filter(event -> {
                        FExpression fexpr = this.getFExpression(event).and(productFexpr);
                        return !fexpr.applySimplification().isFalse();
                    })
                    .collect(Collectors.toSet());

            // If the intersection is empty, causality is not respected
            if (!restrictedBundle.isEmpty() & Collections.disjoint(config, restrictedBundle)) { // X inter {𝑒1, . . . , 𝑒𝑖−1} = ∅
                return false;
            }
        }
        return true;
    }

    private List<FExpression> getValidProducts(Event event, Set<Event> config) {

        Collection<BehavioralFeature> allFeatures = this.getFeatures();
        FExpression configFexpr = this.configFexpressions.get(config);


        FExpression constraint = this.getFExpression(event).and(configFexpr);



        ConfigurationSet allProducts = new ConfigurationSet(this, constraint);

        List<FExpression> allFExps = allProducts.stream().map(product -> {
            // Construct the product feature expression
            FExpression productFExp = FExpression.trueValue();

            List<BehavioralFeature> featuresTMP = new ArrayList<>();
            BehavioralFeature f1 = null;
            for (Feature<?> f : product) { //TODO: To remove once Feature.hashcode() is debugged
                for(BehavioralFeature f2: this.getFeatures()){
                    if (f.getFeatureName().equals(f2.getFeatureName())){
                        f1 = f2;
                    }
                }
                assert (f1 != null);
                featuresTMP.add(f1);
            }

            for (BehavioralFeature f : allFeatures) {
                FExpression fFexpr= new FExpression(f);
                if(featuresTMP.contains(f)){
                    //if(product.isSelected(f)){
                    productFExp.andWith(fFexpr);
                } else {
                    productFExp.andWith(fFexpr.not());
                }
            }
            return productFExp.applySimplification();
        }).toList();

        return allFExps;
    }

    protected Set<Set<Event>> getAllBundles(Event var1){
        return this.causalityTable.column(var1).keySet();
    }

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
        return this.getRootFeature().getAllRecursiveFeatures().stream().mapToInt(BehavioralFeature::getConflictsCount).sum();
    }

    @Override
    public int getMaxConflictSize() {
        return this.getRootFeature().getAllRecursiveFeatures().stream().mapToInt(BehavioralFeature::getMaxConflictSize).max().orElse(0);
    }

    @Override
    public int getTotalNumberOfConflictingEvents() {
        return this.getRootFeature().getAllRecursiveFeatures().stream().mapToInt(BehavioralFeature::getTotalNumberOfConflictingEvents).sum();
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