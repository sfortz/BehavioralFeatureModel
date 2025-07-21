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

    public static boolean isPredecessor(TransitionSystem ts, Action source, Action target) {
        for (Iterator<Transition> it = ts.getTransitions(source); it.hasNext(); ) {
            Transition t = it.next();
            State intermediate = t.getTarget();

            for (Iterator<Transition> it2 = ts.getOutgoing(intermediate); it2.hasNext(); ) {
                Transition t2 = it2.next();
                if (t2.getAction().equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canReachActionFrom(TransitionSystem ts, State current, Action destination, Set<State> visited) {
        if (!visited.add(current)) return false;

        for (Iterator<Transition> it = ts.getOutgoing(current); it.hasNext(); ) {
            Transition t = it.next();
            if (t.getAction().equals(destination)) {
                return true;
            } else if (canReachActionFrom(ts, t.getTarget(), destination, visited)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isReachable(TransitionSystem ts, Action from, Action to) {
        Set<State> visited = new HashSet<>();
        List<Transition> transitions = new ArrayList<>();
        ts.getTransitions(from).forEachRemaining(transitions::add);
        Set<State> targets = transitions.stream().map(Transition::getTarget).collect(Collectors.toSet());

        for (State target : targets) {
            if (canReachActionFrom(ts, target, to, visited)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPredecessor(FeaturedTransitionSystem fts, Action source, Action target) {

        for (Iterator<Transition> it1 = fts.getTransitions(source); it1.hasNext(); ) {
            Transition t1 = it1.next();
            FExpression fexpr1 = fts.getFExpression(t1);
            State s = t1.getTarget();
            for (Iterator<Transition> it2 = fts.getOutgoing(s); it2.hasNext(); ) {
                Transition t2 = it2.next();

                if (t2.getAction().equals(target)) {
                    FExpression fexpr2 = fts.getFExpression(t2);
                    FExpression combined = fexpr1.and(fexpr2).applySimplification();
                    if (!combined.isFalse()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static FExpression canReachActionFrom(FeaturedTransitionSystem fts, State current, FExpression f1, Action destination, Set<State> visited) {
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
                FExpression f3 = canReachActionFrom(fts, t.getTarget(), f1.and(f2), destination, visited);
                if (!f3.applySimplification().isFalse()) {
                    return f3;
                }
            }
        }
        return FExpression.falseValue();
    }

    public static boolean isReachable(FeaturedTransitionSystem fts, Action a1, Action a2) {
        Set<State> visited = new HashSet<>();

        List<Transition> transitions = Lists.newArrayList(fts.getTransitions(a1));
        Set<State> targets = transitions.stream().map(Transition::getTarget).collect(Collectors.toSet());

        for (State t:targets){
            FExpression fexpr = canReachActionFrom(fts, t, FExpression.trueValue(), a2, visited);
            if(!fexpr.applySimplification().isFalse()){
                return true;
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