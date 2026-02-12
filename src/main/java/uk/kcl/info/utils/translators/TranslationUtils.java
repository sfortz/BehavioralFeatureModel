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

package uk.kcl.info.utils.translators;

import be.vibes.fexpression.FExpression;
import be.vibes.ts.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import uk.kcl.info.bfm.CausalityRelation;
import uk.kcl.info.bfm.ConflictSet;
import uk.kcl.info.bfm.Event;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to hold shared helper methods (like getSingleDifference, reachability, etc.)
 */
public class TranslationUtils {

    public static final String INITIAL_STATE = "State_0";

    public static BiMap<Set<Event>, String> indexConfigurationsAsStates(Collection<Set<Set<Event>>> configurations) {
        BiMap<Set<Event>, String> configToStateMap = HashBiMap.create();
        int stateCounter = 0;
        configToStateMap.put(Collections.emptySet(), INITIAL_STATE);
        stateCounter++;

        for (Set<Set<Event>> configSet : configurations) {
            for (Set<Event> config : configSet) {
                if (!config.isEmpty() && !configToStateMap.containsKey(config)) {
                    configToStateMap.put(config, "State_" + stateCounter++);
                }
            }
        }

        return configToStateMap;
    }

    public static boolean isSingleStepSuccessor(Set<Event> smaller, Set<Event> larger) {
        return larger.size() == smaller.size() + 1 && larger.containsAll(smaller);
    }

    public static Event getSingleDifference(Set<Event> smaller, Set<Event> larger) {
        Set<Event> diff = new HashSet<>(larger);
        diff.removeAll(smaller);
        return (diff.size() == 1) ? diff.iterator().next() : null;
    }

    public static boolean isPredecessor(Transition t1, Transition t2) {
        return t2.getSource().equals(t1.getTarget());
    }

    public static boolean isReachable(TransitionSystem ts, Transition from, Transition to) {
        State start = from.getTarget();
        State target = to.getSource();

        Set<State> visited = new HashSet<>();
        Deque<State> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            State current = stack.pop();
            if (!visited.add(current)) continue;
            if (current.equals(target)) return true;

            for (Iterator<Transition> it = ts.getOutgoing(current); it.hasNext(); ) {
                stack.push(it.next().getTarget());
            }
        }
        return false;
    }

    public static boolean isReachable(FeaturedTransitionSystem fts, Transition from, Transition to) {

        State start = from.getTarget();
        State target = to.getSource();
        FExpression startF = fts.getFExpression(from);

        record Node(State s, FExpression f) {}

        Map<State, Set<FExpression>> visited = new HashMap<>();
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(new Node(start, startF));

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            State current = node.s();
            FExpression currentF = node.f();

            if (current.equals(target)) return true;

            Set<FExpression> seen = visited.computeIfAbsent(current, k -> new HashSet<>());

            // Subsumption check
            boolean subsumed = seen.stream().anyMatch(oldF -> currentF.not().or(oldF).applySimplification().isTrue());

            if (subsumed) continue;
            seen.add(currentF);

            for (Iterator<Transition> it = fts.getOutgoing(current); it.hasNext(); ) {
                Transition t = it.next();
                FExpression newF = currentF.and(fts.getFExpression(t)) .applySimplification();

                if (!newF.isFalse()) {
                    stack.push(new Node(t.getTarget(), newF));
                }
            }
        }
        return false;
    }


    public static Set<CausalityRelation> splitBundlesOnConflicts(Set<CausalityRelation> bundles, ConflictSet conflicts) {
        return bundles.stream()
                .flatMap(causality ->
                        conflicts.findMaximalCliques(causality.getBundle()).stream()
                                .map(clique -> new CausalityRelation(clique, causality.getTarget()))
                )
                .collect(Collectors.toSet());
    }

}