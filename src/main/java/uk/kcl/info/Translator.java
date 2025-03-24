package uk.kcl.info;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.FeatureModelFactory;
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
            } else if (isReachable(ts, t.getTarget(), destination, visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean reachable(TransitionSystem ts, Action a1, Action a2) {
        Set<State> visited = new HashSet<>();

        List<Transition> transitions = Lists.newArrayList(ts.getTransitions(a1));
        Set<State> targets = transitions.stream().map(Transition::getTarget).collect(Collectors.toSet());

        for (State t:targets){
            if(isReachable(ts, t, a2, visited)){
                return true;
            }
        }
        return false;
    }

    public static BundleEventStructure ts2bes(TransitionSystem ts) {
        BundleEventStructureFactory factory = new BundleEventStructureFactory();

        // Step 1: Collect actions & add events
        Map<Action, Event> eventMap = new HashMap<>();
        for (Iterator<Action> it = ts.actions(); it.hasNext(); ) { //TODO: This loop only works for Linear BES!
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

        // Step 4: Optimize non-conflicting bundle splitting
        candidateBundles = candidateBundles.stream()
                .flatMap(causality -> ConflictPartition.findMaximalCliques(causality.getBundle(), conflicts)
                        .stream().map(newBundle -> new CausalityRelation(newBundle, causality.getTarget())))
                .collect(Collectors.toSet());

        candidateBundles.forEach(factory::addCausality);

        return factory.build();
    }

    private static FExpression isReachable(FeaturedTransitionSystem fts, State current, FExpression f1, Action destination, Set<State> visited) {
        if (visited.contains(current)) {
            return FExpression.falseValue();
        }
        visited.add(current);

        for (Iterator<Transition> it = fts.getOutgoing(current); it.hasNext(); ) {
            Transition t = it.next();
            FExpression f2 = fts.getFExpression(t);
            if (t.getAction().equals(destination)) {
                return f1.and(f2);
            } else {
                FExpression f3 = isReachable(fts, t.getTarget(), f1.and(f2), destination, visited);
                if (!f3.applySimplification().equals(FExpression.falseValue())) {
                    return f3;
                }
            }
        }
        return FExpression.falseValue();
    }

    private static boolean reachable(FeaturedTransitionSystem fts, Action a1, Action a2) {
        Set<State> visited = new HashSet<>();

        List<Transition> transitions = Lists.newArrayList(fts.getTransitions(a1));
        Set<State> targets = transitions.stream().map(Transition::getTarget).collect(Collectors.toSet());

        for (State t:targets){
            FExpression fexpr = isReachable(fts, t, FExpression.trueValue(), a2, visited);
            if(!fexpr.applySimplification().equals(FExpression.falseValue())){
                return true;
            }
        }
        return false;
    }

    public static FeaturedTransitionSystem fes2fts(FeaturedEventStructure<?> fes){
        TransitionSystem ts = bes2ts(fes);
        FeaturedTransitionSystemFactory factory = new FeaturedTransitionSystemFactory(ts.getInitialState().getName());

        for (Iterator<Transition> it = ts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            String source = t.getSource().getName();
            String action = t.getAction().getName();
            String target = t.getTarget().getName();
            List<Event> configSrc = configToStateMap.inverse().get(source);
            List<Event> configTrg = configToStateMap.inverse().get(target);
            FExpression f1 = fes.getFExpression(configSrc);
            FExpression f2 = fes.getFExpression(configTrg);
            FExpression fexpr = f1.and(f2);
            factory.addTransition(source, action, fexpr.applySimplification().toCnf(), target);
        }

        return factory.build();
    }

    public static FeaturedEventStructure<?> fts2fes(FeatureModel<?> fm, FeaturedTransitionSystem fts){
        BundleEventStructure bes = ts2bes(fts);
        FeaturedEventStructureFactory factory = new FeaturedEventStructureFactory(fm);

        for (Iterator<Event> it = bes.events(); it.hasNext(); ) {
            Event e = it.next();
            Action a = fts.getAction(e.getName());

            FExpression fexpr = FExpression.falseValue();
            List<FExpression> fexpList = new ArrayList<>();
            for (Iterator<Transition> transIt = fts.getTransitions(a); transIt.hasNext(); ) {
                Transition t = transIt.next();
                fexpList.add(fts.getFExpression(t));
                fexpr.orWith(fts.getFExpression(t));
            }

            Feature<?> f = fm.getLeastCommonAncestor(fexpList);
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

        Map<Event, FExpression> mu = new HashMap<>();
        for (BehavioralFeature bf : bfm.getFeatures()) {
            mu.putAll(bf.getEventMap());
        }

        Set<List<Event>> configurations = bfm.getAllConfigurations();
        Map<List<Event>, String> mapping = getConfigStateMapping(configurations);

        String initialState = mapping.get(new ArrayList<>());
        FeaturedTransitionSystemFactory factory = new FeaturedTransitionSystemFactory(initialState);

        for (Event ev : bfm.getAllEvents()) {
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
                        factory.addTransition(s1, e.getName(), mu.get(e), s2);
                    }
                    // Restore the last event back to c2
                    c2.add(e);
                }
            }
        }

        return factory.build();
    }

    public static FeatureModel<?> bfm2fm(BehavioralFeatureModel bfm){
        // TODO: This should probably not exist, but be a function "getUnderlyingFM" in the BFM.

        return new FeatureModelFactory<>(bfm).build();
    }

    public static BehavioralFeatureModel fts2bfm(FeatureModel<?> fm, FeaturedTransitionSystem fts){

        BehavioralFeatureModelFactory factory = new BehavioralFeatureModelFactory(fm);
        Map<Event, FExpression> fExprMap = new HashMap<>();
        Map<Transition, Event> tMap = new HashMap<>();

        // Step 1: Collect actions & add events
        for (Iterator<Action> it = fts.actions(); it.hasNext(); ) { //TODO: This loop only works for Linear BES!
            Action a = it.next();
            Event e = new Event(a.getName());

            FExpression fexpr = FExpression.falseValue();
            List<FExpression> fexpList = new ArrayList<>();
            for (Iterator<Transition> transIt = fts.getTransitions(a); transIt.hasNext(); ) {
                Transition t = transIt.next();
                tMap.put(t,e);
                fexpList.add(fts.getFExpression(t));
                fexpr.orWith(fts.getFExpression(t));
            }

            String ancestor = fm.getLeastCommonAncestor(fexpList).getFeatureName();
            BehavioralFeature f = factory.getFeature(ancestor);
            fExprMap.put(e,fexpr);
            factory.addEvent(f, e.getName(), fexpr.applySimplification().toCnf());
        }

        // Step 2 & 3: Compute conflicts and (candidate) causality in a single loop
        Set<ConflictRelation> conflicts = new HashSet<>();
        Set<CausalityRelation> candidateBundles = new HashSet<>();

        for (Map.Entry<Transition, Event> entry1: tMap.entrySet()){
            Action a1 = entry1.getKey().getAction();
            Event e1 = entry1.getValue();

            Set<Event> bundle = new HashSet<>();

            for (Map.Entry<Transition, Event> entry2: tMap.entrySet()){
                Action a2 = entry2.getKey().getAction();

                if(!a1.equals(a2)) {
                    Event e2 = entry2.getValue();

                    boolean a1ToA2 = reachable(fts, a1, a2);
                    boolean a2ToA1 = reachable(fts, a2, a1);

                    // Conflict relation
                    if (!a1ToA2 && !a2ToA1) {
                        List<FExpression> fexprs = new ArrayList<>();
                        fexprs.add(fExprMap.get(e1));
                        fexprs.add(fExprMap.get(e2));
                        String lca = fm.getLeastCommonAncestor(fexprs).getFeatureName();
                        factory.addConflict(lca, e1, e2);
                        conflicts.add(new ConflictRelation(e1, e2));
                    }

                    // Causality relation (candidate)
                    if (a2ToA1 && !a1ToA2) {  ///TODO: Optimise, only looping once for each pair
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

        // Step 4: Non-conflicting bundle splitting
        candidateBundles = candidateBundles.stream()
                .flatMap(causality -> ConflictPartition.findMaximalCliques(causality.getBundle(), conflicts)
                        .stream().map(newBundle -> new CausalityRelation(newBundle, causality.getTarget())))
                .collect(Collectors.toSet());

        for(CausalityRelation causality: candidateBundles){
            List<FExpression> fexprs = new ArrayList<>();
            fexprs.add(fExprMap.get(causality.getTarget()));
            for(Event e2: causality.getBundle()){
                fexprs.add(fExprMap.get(e2));
            }
            String lca = fm.getLeastCommonAncestor(fexprs).getFeatureName();
            factory.addCausality(lca, causality);
        }

        return factory.build();
    }

}