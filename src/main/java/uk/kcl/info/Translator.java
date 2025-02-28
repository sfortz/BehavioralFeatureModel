package uk.kcl.info;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.ts.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import uk.kcl.info.bfm.*;
import uk.kcl.info.bfm.ConflictPartition;

import java.util.*;
import java.util.stream.Collectors;

public class Translator {

    private static BiMap<List<Event>, String> configToStateMap;

    private static BiMap<List<Event>, String> getConfigStateMapping(Set<List<Event>> configurations) {
        configToStateMap = HashBiMap.create();

        int stateCounter = 0;
        for (List<Event> config : configurations) {
            String stateName = "State_" + stateCounter++;
            configToStateMap.put(config, stateName);
        }

        return configToStateMap;
    }

    public static TransitionSystem bes2ts(BundleEventStructure bes) {

        Set<List<Event>> configurations = bes.getAllConfigurations();
        Map<List<Event>, String> mapping = getConfigStateMapping(configurations);
        String initialState = mapping.get(new ArrayList<>());
        TransitionSystemFactory factory = new TransitionSystemFactory(initialState);

        for (Event ev : bes.getAllEvents()) {
            factory.addAction(ev.getName());
        }

        factory.addStates(mapping.values().toArray(new String[0]));

        for (List<Event> c1 : configurations) {
            for (List<Event> c2 : configurations) {

                String s1 = mapping.get(c1);
                String s2 = mapping.get(c2);

                // If c2 is c1 with exactly one more event at the end
                if (c2.size() == c1.size() + 1) {
                    Event e = c2.removeLast();
                    // Check if the remaining part of c2 is equal to c1
                    if (c1.equals(c2)) {
                        factory.addTransition(s1, e.getName(), s2);
                    }
                    // Restore the last event back to c2
                    c2.add(e);
                }
            }
        }

        return factory.build();
    }

    private static boolean isReachable(TransitionSystem ts, State current, Action destination, Set<State> visited) {
        if (visited.contains(current)) {
            return false;
        }
        visited.add(current);

        for (Iterator<Transition> it = ts.getOutgoing(current); it.hasNext(); ) {
            Transition t = it.next();
            if (t.getAction().equals(destination)) {
                return true;
            } else
            if (isReachable(ts, t.getTarget(), destination, visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean reachable(TransitionSystem ts, Action a1, Action a2) {
        Set<State> visited = new HashSet<>();

        List<Transition> transitions = Lists.newArrayList(ts.getTransitions(a1));
        Set<State> targets = transitions.stream().map(Transition::getTarget).collect(Collectors.toSet());

        boolean acc = false;

        for (State t:targets){
            acc = acc || isReachable(ts, t, a2, visited);
        }

        return acc;
    }

    public static BundleEventStructure ts2bes(TransitionSystem ts) {
        BundleEventStructureFactory factory = new BundleEventStructureFactory();

        // Step 1: Collect actions & add events
        Map<Action, Event> eventMap = new HashMap<>();
        for (Iterator<Action> it = ts.actions(); it.hasNext(); ) {
            Action a = it.next();
            String name = a.getName();
            Event e = new Event(name);
            eventMap.put(a, e);
            factory.addEvent(name);
        }

        // Step 2 & 3: Compute conflicts and (candidate) causality in a single loop
        Set<ConflictRelation> conflicts = new HashSet<>();
        Set<CausalityRelation> candidateBundles = new HashSet<>();

        for (Map.Entry<Action, Event> entry1: eventMap.entrySet()){
            Action a1 = entry1.getKey();
            Event e1 = entry1.getValue();

            Set<Event> bundle = new HashSet<>();

            for (Map.Entry<Action, Event> entry2: eventMap.entrySet()){
                Action a2 = entry2.getKey();
                if(!a1.equals(a2)) {
                    Event e2 = entry2.getValue();

                    boolean a1ToA2 = reachable(ts, a1, a2);
                    boolean a2ToA1 = reachable(ts, a2, a1);

                    // Conflict relation
                    if (!a1ToA2 && !a2ToA1) {
                        factory.addConflict(e1, e2);
                        conflicts.add(new ConflictRelation(e1, e2));
                    }

                    // Causality relation (candidate)
                    if (a2ToA1 && !a1ToA2) {
                        bundle.add(e2);
                    }
                }
            }

            if (!bundle.isEmpty()) {
                candidateBundles.add(new CausalityRelation(bundle, e1));
            }
        }

        System.out.println("Conflict Count: " + conflicts.size());
        System.out.println("Causality candidate count: " + candidateBundles.size());

        // Step 4: Optimize non-conflicting bundle splitting
        candidateBundles = candidateBundles.stream()
                .flatMap(causality -> ConflictPartition.findMaximalCliques(causality.getBundle(), conflicts)
                        .stream().map(newBundle -> new CausalityRelation(newBundle, causality.getTarget())))
                .collect(Collectors.toSet());

        candidateBundles.forEach(factory::addCausality);

        return factory.build();
    }

    public static FeaturedTransitionSystem fes2fts(FeaturedEventStructure fes){
        TransitionSystem ts = bes2ts(fes);
        FeaturedTransitionSystemFactory factory = new FeaturedTransitionSystemFactory(ts.getInitialState().getName());

        for (Iterator<Transition> it = ts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            String source = t.getSource().getName();
            String action = t.getAction().getName();
            String target = t.getTarget().getName();
            List<Event> config = configToStateMap.inverse().get(source);
            FExpression f1 = fes.getFexpression(config);
            FExpression f2 = fes.getFExpression(fes.getEvent(action));
            FExpression fexpr = f1.and(f2);
            factory.addTransition(source, action, fexpr.applySimplification().toCnf(), target);
        }

        return factory.build();
    }

    private static List<Feature> getAncestors(Feature feature) {
        List<Feature> ancestors = new ArrayList<>();
        while (feature != null) {
            ancestors.add(feature);
            feature = feature.getParentFeature();
        }
        return ancestors;
    }

    private static Feature leastCommonAncestor(Feature f1, Feature f2) {
        List<Feature> ancestorsF1 = getAncestors(f1);
        List<Feature> ancestorsF2 = getAncestors(f2);

        // Find the lowest common ancestor
        for (Feature ancestor : ancestorsF1) {
            if (ancestorsF2.contains(ancestor)) {
                return ancestor;
            }
        }

        return null;
    }

    private static Feature leastCommonAncestor (FeatureModel fm, List<FExpression> fExpressions){

        FExpression disjunction = FExpression.falseValue();

        for (FExpression fexp: fExpressions){
            disjunction.orWith(fexp);
        }

        disjunction = disjunction.toCnf().applySimplification();

        if (disjunction.isTrue()) {
            return fm.getRootFeature();
        }

        Set<Feature> features = disjunction.getFeatures().stream().map(f -> fm.getFeature(f.getFeatureName())).collect(Collectors.toSet());

        if (features.size() == 1) {
            return features.stream().toList().getFirst();
        }

        Iterator<Feature> iterator = features.iterator();
        Feature lca = iterator.next();

        while (iterator.hasNext()) {
            lca = leastCommonAncestor(lca, iterator.next());
        }
        return lca;
    }

    public static FeaturedEventStructure fts2fes(FeatureModel fm, FeaturedTransitionSystem fts){
        BundleEventStructure bes = ts2bes(fts);
        FeaturedEventStructureFactory factory = new FeaturedEventStructureFactory(fm);

        for (Iterator<Event> it = bes.events(); it.hasNext(); ) {
            Event e = it.next();
            Action a = fts.getAction(e.getName());

            List<Transition> transList = new ArrayList<>();
            List<FExpression> fexpList = new ArrayList<>();
            for (Iterator<Transition> transIt = fts.getTransitions(a); transIt.hasNext(); ) {
                Transition t = transIt.next();
                transList.add(t);
                fexpList.add(fts.getFExpression(t));
            }

            Feature f = leastCommonAncestor(fm, fexpList);
            FExpression fexpr = FExpression.falseValue();

            for (Transition t: transList){
                fexpr.orWith(fts.getFExpression(t));  //TODO: Check Algo in paper for non-linearity
            }
            factory.addEvent(e.getName(), f, fexpr.applySimplification());
        }

        for (Iterator<CausalityRelation> it = bes.causalities(); it.hasNext(); ) {
            CausalityRelation c = it.next();
            factory.addCausality(c.getBundle(),c.getTarget());
        }

        for (Iterator<ConflictRelation> it = bes.conflicts(); it.hasNext(); ) {
            ConflictRelation c = it.next();
            factory.addConflict(c.getEvent1(), c.getEvent2());
        }

        return factory.build();
    }

    public static FeaturedTransitionSystem bfm2fts(BehavioralFeatureModel bfm){
        // TODO: Maybe we should return the FM as well?
        return null;
    }

    public static FeatureModel bfm2fm(BehavioralFeatureModel bfm){
        // TODO: This should probably not exist, but be a function "getUnderlyingFM" in the BFM.
        return null;
    }

    public static BehavioralFeatureModel fts2bfm(FeatureModel fm, FeaturedTransitionSystem fts){
        return null;
    }

}