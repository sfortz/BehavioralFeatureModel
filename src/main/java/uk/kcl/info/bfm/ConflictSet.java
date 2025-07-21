/*
 *
 *  * Copyright 2025 Sophie Fortz
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package uk.kcl.info.bfm;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.*;

// Conflict set with directional mapping and compact printing
public class ConflictSet {
    // Each key maps to a set of events it conflicts with
    private final Map<Event, Set<Event>> conflictMap = new HashMap<>();

    public void addConflict(Event e1, Event e2) {
        Preconditions.checkNotNull(e1, "Event may not be null!");
        Preconditions.checkNotNull(e2, "Event may not be null!");
        if (e1.equals(e2)) return; // optional: ignore self-conflict

        conflictMap.computeIfAbsent(e1, k -> new HashSet<>()).add(e2);
        conflictMap.computeIfAbsent(e2, k -> new HashSet<>()).add(e1); // symmetrical
    }

    public void addConflicts(Event e1, Collection<Event> group) {
        for (Event e2 : group) {
                addConflict(e1, e2);
        }
    }

    public void addConflicts(Collection<Event> group1, Collection<Event> group2) {
        for (Event e1 : group1) {
            for (Event e2 : group2) {
                addConflict(e1, e2);
            }
        }
    }

    public void addConflicts(ConflictSet set) {
        for (Map.Entry<Event, Set<Event>> entry : set.conflictMap.entrySet()) {
            Event e1 = entry.getKey();
            this.addConflicts(e1, entry.getValue());
        }
    }

    public boolean areInConflict(Event e1, Event e2) {
        Set<Event> conflicts = conflictMap.get(e1);
        return conflicts != null && conflicts.contains(e2);
    }

    public Set<Event> getConflicts(Event e) {
        return conflictMap.getOrDefault(e, Collections.emptySet());
    }

    public Set<Event> getAllEvents() {
        Set<Event> allEvents = new HashSet<>();
        for (Map.Entry<Event, Set<Event>> entry : conflictMap.entrySet()) {
            allEvents.add(entry.getKey());
            allEvents.addAll(entry.getValue());
        }
        return allEvents;
    }


    public int size(){
        if(conflictMap.isEmpty()){
            return 0;
        } else {
            return findMinimalBicliqueEdgeCover().size();
        }
    }

    public int maxConflictSize() {
        if (conflictMap.isEmpty()) {
            return 0;
        }

        Set<Biclique> bicliques = findMinimalBicliqueEdgeCover();
        return bicliques.stream()
                .mapToInt(clique -> clique.getA().size() + clique.getB().size())
                .max()
                .orElse(0);
    }

    public int getTotalNumberOfConflictingEvents() {
        if (conflictMap.isEmpty()) {
            return 0;
        }

        Set<Biclique> bicliques = findMinimalBicliqueEdgeCover();
        return bicliques.stream()
                .mapToInt(clique -> clique.getA().size() + clique.getB().size())
                .sum();
    }

    @Override
    public String toString() {
        Set<Biclique> cliques = findMinimalBicliqueEdgeCover();
        StringBuilder sb = new StringBuilder("Conflicts:\n");
        for(Biclique c: cliques){
            sb.append(c.getA()).append(" # ").append(c.getB()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConflictSet that = (ConflictSet) o;
        return Objects.equal(conflictMap, that.conflictMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(conflictMap);
    }


    public Set<Set<Event>> findMaximalCliques(Set<Event> events) {
        Set<Set<Event>> maximalCliques = new HashSet<>();
        bronKerbosch(new HashSet<>(), new HashSet<>(events), new HashSet<>(), maximalCliques);
        return maximalCliques;
    }

    private void bronKerbosch(Set<Event> R, Set<Event> P, Set<Event> X, Set<Set<Event>> maximalCliques) {
        if (P.isEmpty() && X.isEmpty()) {
            maximalCliques.add(new HashSet<>(R));
            return;
        }
        Event pivot = selectPivot(P, X);
        Set<Event> nonNeighbors = new HashSet<>(P);
        if (pivot != null) {
            nonNeighbors.removeAll(getConflicts(pivot));
        }
        for (Event v : new HashSet<>(nonNeighbors)) {
            Set<Event> newR = new HashSet<>(R);
            newR.add(v);
            Set<Event> vConflicts = getConflicts(v);
            Set<Event> newP = new HashSet<>(P);
            newP.retainAll(vConflicts);
            Set<Event> newX = new HashSet<>(X);
            newX.retainAll(vConflicts);
            bronKerbosch(newR, newP, newX, maximalCliques);
            P.remove(v);
            X.add(v);
        }
    }

    private Event selectPivot(Set<Event> P, Set<Event> X) {
        Set<Event> unionPX = new HashSet<>(P);
        unionPX.addAll(X);
        Event bestPivot = null;
        int maxNeighbors = -1;
        for (Event v : unionPX) {
            int neighbors = getConflicts(v).size();
            if (neighbors > maxNeighbors) {
                maxNeighbors = neighbors;
                bestPivot = v;
            }
        }
        return bestPivot;
    }

    public Set<Biclique> findMinimalBicliqueEdgeCover() {
        Set<Event> allEvents = getAllEvents();
        Set<Edge> uncoveredEdges = new HashSet<>();

        // Step 1: create set of all edges in the conflict graph
        for (Event e1 : allEvents) {
            for (Event e2 : getConflicts(e1)) {
                if (e1.hashCode() < e2.hashCode()) { // avoid duplicates
                    uncoveredEdges.add(new Edge(e1, e2));
                }
            }
        }

        Set<Biclique> bicliqueCover = new HashSet<>();

        while (!uncoveredEdges.isEmpty()) {
            // Step 2: pick a candidate edge
            Edge seed = uncoveredEdges.iterator().next();
            Set<Event> left = new HashSet<>();
            Set<Event> right = new HashSet<>();
            left.add(seed.e1);
            right.add(seed.e2);

            // Step 3: expand both sides
            Set<Event> leftCandidates = new HashSet<>(getConflicts(seed.e2));
            leftCandidates.retainAll(allEvents);
            for (Event e : leftCandidates) {
                if (areAllInConflict(e, right)) {
                    left.add(e);
                }
            }

            Set<Event> rightCandidates = new HashSet<>(getConflicts(seed.e1));
            rightCandidates.retainAll(allEvents);
            for (Event e : rightCandidates) {
                if (areAllInConflict(e, left)) {
                    right.add(e);
                }
            }

            // Step 4: get edges of this biclique
            Set<Edge> bicliqueEdges = new HashSet<>();
            for (Event a : left) {
                for (Event b : right) {
                    if (areInConflict(a, b)) {
                        bicliqueEdges.add(new Edge(a, b));
                    }
                }
            }

            // Step 5: register this biclique as a cover component
            bicliqueCover.add(new Biclique(left, right));

            // Step 6: remove covered edges
            uncoveredEdges.removeAll(bicliqueEdges);
        }

        return bicliqueCover;
    }

    private boolean areAllInConflict(Event e, Set<Event> group) {
        for (Event other : group) {
            if (!areInConflict(e, other)) return false;
        }
        return true;
    }

    private static class Edge {
        final Event e1, e2;

        Edge(Event e1, Event e2) {
            this.e1 = e1;
            this.e2 = e2;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Edge other)) return false;
            return (e1.equals(other.e1) && e2.equals(other.e2)) || (e1.equals(other.e2) && e2.equals(other.e1));
        }

        @Override
        public int hashCode() {
            return e1.hashCode() + e2.hashCode(); // order-independent
        }
    }

    public static class Biclique {
        private final ImmutableSet<Event> A;
        private final ImmutableSet<Event> B;

        public Biclique(Set<Event> A, Set<Event> B) {
            this.A = ImmutableSet.copyOf(A);
            this.B = ImmutableSet.copyOf(B);
        }

        @Override
        public String toString() {
            return "Biclique{" + "A=" + A + ", B=" + B + '}';
        }

        public Set<Event> getA() {
            return A;
        }

        public Set<Event> getB() {
            return B;
        }
    }
}
