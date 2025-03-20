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

    public static FeaturedTransitionSystem fes2fts(FeaturedEventStructure<?> fes){
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

    public static FeaturedEventStructure<?> fts2fes(FeatureModel<?> fm, FeaturedTransitionSystem fts){
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

            Feature<?> f = fm.getLeastCommonAncestor(fexpList);
            FExpression fexpr = FExpression.falseValue();

            for (Transition t: transList){
                fexpr.orWith(fts.getFExpression(t));
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

    /*
    *
    * Input: BFM (f : E, C, FC, EC)
Output: linear FTS (M, T , μ) with T = (S, ˆE, so, δ) and M = (f ′, G, FC′)
1: M ← GetFM ((f : E, C, FC, EC)) ▷ Build the feature model
2: let Conf, Cause, μ′ ← ∅
3: ( ˆE, Conf, Cause, μ′) ← GetEventConstr ((f : E, C, FC, EC), ˆE, Conf, Cause, μ′) ▷ Get the events, constraints and μ
4: ▷ Build the transition system
5: initialize set S ⊆ P( ˆE)
6: set s0 ← ∅
7: for {e1, . . . , en} ⊆ P( ˆE) do ▷ Build set of states (the set of configurations)
8: for ei ∈ {e1, . . . , en} do ▷ Get the sets of events that are not in conflict
9: for ej ∈ {ei+1, . . . , en} do
10: if (ei, ej ) ∈ Conf then
11: skip ▷ Skip this set of events and try another one
12: for (X, ei) ∈ Cause do ▷ Only take the sets that satisfy the causality relation condition
13: if {e1, . . . , ei} ∩ X = ∅ then
14: skip ▷ Skip this set of events and try another one
15: S ← S ∪ {e1, . . . , en}
16: initialize relation δ ⊆ S × E × S
17: for s, s′ ∈ S, e ∈ E do ▷ Build the transition relation
18: if s′ \ s = {e} then
19: δ ← δ ∪ {(s, e, s′)} ▷ Include transition
20: μ ← μ ∪ {((s, e, s′), μ′(e))} ▷ Include feature expression label
21: return (M, T , μ) with T = (S, ˆE, so, δ) and M = (f ′, G, FC′)
    *
    *
    * */

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

            List<Transition> transList = new ArrayList<>();
            List<FExpression> fexpList = new ArrayList<>();
            for (Iterator<Transition> transIt = fts.getTransitions(a); transIt.hasNext(); ) {
                Transition t = transIt.next();
                transList.add(t);
                tMap.put(t,e);
                fexpList.add(fts.getFExpression(t));
            }

            String ancestor = fm.getLeastCommonAncestor(fexpList).getFeatureName();
            BehavioralFeature f = factory.getFeature(ancestor);
            FExpression fexpr = FExpression.falseValue();

            for (Transition t: transList){
                fexpr.orWith(fts.getFExpression(t));
            }

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

                    boolean a1ToA2 = reachable(fts, a1, a2);  //Should reachable be about Transitions or Actions?
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

        /*
        Input: feature f' and linear FTS (M, T, H) over features N and events E with T = (S, E, so, 8) and f' € features (M)
        Output: BFM (f : E,C, FC, EC)

        7: EC « ECUflé, e") | f'= Ica (M, V(s,e',') EsM(s, e, 8)))
        • The set of conflicts associated to f
        " Get the feature f' that e' is associated to
        " Get the feature f" that e" is associated to
        • The conflict cannot be added further down the tree
        • Both events are not reachable from one another
        • Beginning causalities associated to f
        12: EC+ ECUUéeêf(X, é) |f'= Ica(M, V(s,e',s') EsM((s, é', s')))
        "Get the feature f' that e' is associated to
        X=fe" | f"= Ica(M, V(s,e",s')EsM((s,e", s')))
        » Get the feature f" that e" is associated to
        If = Ica(M,f'^f")
        • The causality cannot be added further down the tree
        ^ reachable(e"
        ", e') A → reachable(e', e")}} • The events in X can reach e' but not the other way around
        16: while (D,e) E EC do
        17: EC+ EC\(0,e)}
        • Discard empty causalities
        18: while (X, e) € EC such that e1, ez E X and e1 # ez and (e1, e2) & EC do
        19: let X1+ X| {e17, X24X1{22}
        20: EC< (EC|(X, e)}) U{(X1, e), (X2, e)}
         */

        return (BehavioralFeatureModel) factory.build();
    }

}