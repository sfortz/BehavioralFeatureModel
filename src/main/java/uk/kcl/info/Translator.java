package uk.kcl.info;

import be.vibes.ts.*;
import com.google.common.collect.Lists;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.BundleEventStructureFactory;
import uk.kcl.info.bfm.Event;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.utilities.Pair;

import java.util.*;

public class Translator {

    public static Map<List<Event>, String> getConfigStateMapping(Set<List<Event>> configurations) {
        Map<List<Event>, String> configToStateMap = new HashMap<>();

        int stateCounter = 1;
        for (List<Event> config : configurations) {
            String stateName = "State_" + stateCounter++;
            configToStateMap.put(config, stateName);
        }

        return configToStateMap;
    }

    public static TransitionSystem bes2ts(BundleEventStructure bes) {

        TransitionSystemFactory factory = new TransitionSystemFactory("initial_State");

        for (Event ev : bes.getAllEvents()) {
            factory.addAction(ev.getName());
        }

        Set<List<Event>> configurations = bes.getAllConfigurations();
        Map<List<Event>, String> mapping = getConfigStateMapping(configurations);
        factory.addStates(mapping.values().toArray(new String[0]));

        for (List<Event> c1 : configurations) {
            for (List<Event> c2 : configurations) {

                // If c2 is c1 with exactly one more event at the end
                if (c2.size() == c1.size() + 1) {
                    Event e = c2.remove(c2.size() - 1);

                    // Check if the remaining part of c2 is equal to c1
                    if (c1.equals(c2)) {
                        String s1 = mapping.get(c1);
                        String s2 = mapping.get(c2);
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
            }
            if (isReachable(ts, t.getTarget(), destination, visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean reachable(TransitionSystem ts, Action a1, Action a2) {
        Set<State> visited = new HashSet<>();

        List<Transition> transitions = Lists.newArrayList(ts.getTransitions(a1));

        boolean acc = false;

        for (Transition t:transitions){
            acc = acc || isReachable(ts, t.getTarget(), a2, visited);
        }

        return acc;
    }


    public static BundleEventStructure ts2bes(TransitionSystem ts) {
        BundleEventStructureFactory factory = new BundleEventStructureFactory();

        // Step 1: Collect actions & add events
        List<Action> actions = new ArrayList<>();
        for (Iterator<Action> it = ts.actions(); it.hasNext(); ) {
            Action a = it.next();
            actions.add(a);
            factory.addEvent(a.getName());
        }

        // Step 2 & 3: Compute conflicts and (candidate) causality in a single loop
        Set<Pair<Event, Event>> conflicts = new HashSet<>();
        Map<Set<Event>, Event> candidateBundles = new HashMap<>();

        for (Action a1 : actions) {
            Event e1 = new Event(a1.getName());
            Set<Event> bundle = new HashSet<>();

            for (Action a2 : actions) {
                Event e2 = new Event(a2.getName());

                // Conflict relation
                if (!reachable(ts, a1, a2) && !reachable(ts, a2, a1)) {
                    factory.addConflict(e1, e2);
                    conflicts.add(new Pair<>(e1, e2));
                }

                // Causality relation (candidate)
                if (reachable(ts, a2, a1) && !reachable(ts, a1, a2)) {
                    bundle.add(e2);
                }
            }

            if (!bundle.isEmpty()) {
                candidateBundles.put(bundle, e1);
            }
        }

        System.out.println("Conflict Count: " + conflicts.size());
        System.out.println("Causality candidate count: " + candidateBundles.size());


        // Step 4: Optimize non-conflicting bundle splitting
        Queue<Map.Entry<Set<Event>, Event>> queue = new LinkedList<>(candidateBundles.entrySet());
        while (!queue.isEmpty()) {
            Map.Entry<Set<Event>, Event> entry = queue.poll();
            Set<Event> bundle = entry.getKey();
            Event event = entry.getValue();

            boolean split = false;
            for (Event e1 : bundle) {
                for (Event e2 : bundle) {
                    if (!e1.equals(e2) && !conflicts.contains(new Pair<>(e1, e2))) {
                        Set<Event> bundle1 = new HashSet<>(bundle);
                        Set<Event> bundle2 = new HashSet<>(bundle);
                        bundle1.remove(e1);
                        bundle2.remove(e2);

                        queue.add(new AbstractMap.SimpleEntry<>(bundle1, event));
                        queue.add(new AbstractMap.SimpleEntry<>(bundle2, event));
                        split = true;
                        break;
                    }
                }
                if (split) break;
            }

            if (!split) {
                factory.addCausality(bundle, event);
            }
        }

        return factory.build();
    }

}