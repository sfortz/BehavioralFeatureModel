package uk.kcl.info.bfm;

import java.util.*;
import java.util.stream.Collectors;

public class ConflictPartition {

    public static Set<Set<Event>> findMaximalCliques(Set<Event> events, Set<ConflictRelation> conflictGraph) {
        Set<Set<Event>> maximalCliques = new HashSet<>();
        bronKerbosch(new HashSet<>(), new HashSet<>(events), new HashSet<>(), conflictGraph, maximalCliques);
        return maximalCliques;
    }

    private static void bronKerbosch(Set<Event> R, Set<Event> P, Set<Event> X, Set<ConflictRelation> conflictGraph, Set<Set<Event>> maximalCliques) {
        if (P.isEmpty() && X.isEmpty()) {
            maximalCliques.add(new HashSet<>(R));
            return;
        }
        Event pivot = selectPivot(P, X, conflictGraph);
        Set<Event> nonNeighbors = new HashSet<>(P);
        if (pivot != null) {
            nonNeighbors.removeAll(getConflictingEvents(pivot, conflictGraph));
        }
        for (Event v : new HashSet<>(nonNeighbors)) {
            Set<Event> newR = new HashSet<>(R);
            newR.add(v);
            Set<Event> newP = new HashSet<>(P);
            Set<Event> newX = new HashSet<>(X);
            Set<Event> vConflictingEvents = getConflictingEvents(v, conflictGraph);
            newP.retainAll(vConflictingEvents);
            newX.retainAll(vConflictingEvents);
            bronKerbosch(newR, newP, newX, conflictGraph, maximalCliques);
            P.remove(v);
            X.add(v);
        }
    }

    private static Event selectPivot(Set<Event> P, Set<Event> X, Set<ConflictRelation> conflictGraph) {
        Set<Event> unionPX = new HashSet<>(P);
        unionPX.addAll(X);
        Event bestPivot = null;
        int maxNeighbors = -1;
        for (Event v : unionPX) {
            int neighbors = getConflictingEvents(v, conflictGraph).size();
            if (neighbors > maxNeighbors) {
                maxNeighbors = neighbors;
                bestPivot = v;
            }
        }
        return bestPivot;
    }

    private static Set<Event> getConflictingEvents(Event v, Set<ConflictRelation> conflictGraph) {
        return conflictGraph.stream()
                .filter(c -> c.getEvent1().equals(v) || c.getEvent2().equals(v))
                .map(c -> c.getEvent1().equals(v) ? c.getEvent2() : c.getEvent1())
                .collect(Collectors.toSet());
    }

}