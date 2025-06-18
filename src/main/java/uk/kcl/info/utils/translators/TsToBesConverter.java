package uk.kcl.info.utils.translators;

import be.vibes.ts.*;
import uk.kcl.info.bfm.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static uk.kcl.info.utils.translators.TranslationUtils.*;

public class TsToBesConverter {

    private final BundleEventStructureFactory factory;
    private final TransitionSystem ts;
    private final Map<Action, Event> eventMap = new HashMap<>();
    private final ConflictSet conflictSet = new ConflictSet();
    private final Set<CausalityRelation> candidateBundles = new HashSet<>();

    public TsToBesConverter(TransitionSystem ts) {
        this.ts = Objects.requireNonNull(ts);
        this.factory = new BundleEventStructureFactory();
    }

    public BundleEventStructure convert() {

        // Step 1: Collect actions & add events
        createEvents();
        // Step 2 & 3: Compute conflicts and (candidate) causality in a single loop
        computeConflictsAndCausality();
        // Step 4: Optimize non-conflicting bundle splitting
        optimizeBundles();

        candidateBundles.forEach(factory::addCausality);
        return factory.build();
    }

    private void createEvents() {
        for (Iterator<Action> it = ts.actions(); it.hasNext(); ) {
            Action a = it.next();
            Event e = new Event(a.getName());
            eventMap.put(a, e);
            factory.addEvent(e.getName());
        }
    }

    private void computeConflictsAndCausality() {
        for (Entry<Action, Event> entry1 : eventMap.entrySet()) {
            Action a1 = entry1.getKey();
            Event e1 = entry1.getValue();
            Set<Event> bundle = new HashSet<>();

            for (Entry<Action, Event> entry2 : eventMap.entrySet()) {
                Action a2 = entry2.getKey();
                if (!a1.equals(a2)) {
                    Event e2 = entry2.getValue();

                    boolean a1ToA2 = isReachable(ts, a1, a2);
                    boolean a2ToA1 = isReachable(ts, a2, a1);

                    if (!a1ToA2 && !a2ToA1) {
                        factory.addConflict(e1, e2);
                        conflictSet.addConflict(e1, e2);
                    }

                    if (isPredecessor(ts, a2, a1) && !a1ToA2) {
                        bundle.add(e2);
                    }
                }
            }

            if (!bundle.isEmpty()) {
                candidateBundles.add(new CausalityRelation(bundle, e1));
            }
        }
    }

    private void optimizeBundles() {
        Set<CausalityRelation> optimized = candidateBundles.stream()
                .flatMap(causality ->
                        conflictSet.findMaximalCliques(causality.getBundle())
                                .stream()
                                .map(clique -> new CausalityRelation(clique, causality.getTarget()))
                )
                .collect(Collectors.toSet());
        candidateBundles.clear();
        candidateBundles.addAll(optimized);
    }
}
