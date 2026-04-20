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

package uk.kcl.info.bfm.compositions;

import be.vibes.ts.*;
import uk.kcl.info.utils.Pair;

import java.util.*;

public abstract class AbstractTSParallelComposer<T extends TransitionSystem> implements Composition<T>{

    protected T left;
    protected T right;

    protected String nameOf(Pair<State> p) {return p.getLeft().getName() + "||" + p.getRight().getName();}

    @Override
    public T compose(T ts1, T ts2, boolean sync) {

        this.left = ts1;
        this.right = ts2;

        Pair<State> initPair = new Pair<>(ts1.getInitialState(), ts2.getInitialState());
        String initName = nameOf(initPair);
        TransitionSystemFactory factory = createFactory(initName);

        Set<Action> actions1 = collectActions(ts1);
        Set<Action> actions2 = collectActions(ts2);

        Set<Action> shared = new HashSet<>();
        if (sync) {
            shared.addAll(actions1);
            shared.retainAll(actions2);
        }

        Queue<Pair<State>> queue = new ArrayDeque<>();
        Set<Pair<State>> visited = new HashSet<>();
        Map<Pair<State>, String> nameCache = new HashMap<>();

        queue.add(initPair);
        visited.add(initPair);

        nameCache.put(initPair, initName);

        while (!queue.isEmpty()) {

            Pair<State> pair = queue.poll();
            State s1 = pair.getLeft();
            State s2 = pair.getRight();

            String srcName = nameCache.get(pair);
            factory.addState(srcName);

            // Cache outgoing transitions once
            List<Transition> out1 = toList(ts1.getOutgoing(s1));
            List<Transition> out2 = toList(ts2.getOutgoing(s2));

            // Build index for TS2 (used for synchronization)
            Map<Action, List<Transition>> index2 = buildIndex(out2);

            /*
             * TS1 transitions (independent + synchronized)
             */
            for (Transition t1 : out1) {
                Action action = t1.getAction();

                if (!sync || !shared.contains(action)) {
                    // Independent transition (TS1 only)
                    handleTransition(factory, queue, visited, nameCache, pair, t1, null, srcName, action, false);
                } else {
                    // Synchronized transitions
                    List<Transition> matches = index2.get(action);
                    if (matches == null) continue;

                    for (Transition t2 : matches) {
                        handleTransition(factory, queue, visited, nameCache, pair, t1, t2, srcName, action, true);
                    }
                }
            }

            /*
             * TS2 independent transitions
             */
            for (Transition t2 : out2) {
                Action action = t2.getAction();

                if (!sync || !shared.contains(action)) {
                    handleTransition(factory, queue, visited, nameCache, pair, null, t2, srcName, action, false);
                }
            }
        }

        return (T) factory.build();
    }

    protected abstract TransitionSystemFactory createFactory(String initName);

    /*
     * Build action → transitions index
     */
    private Map<Action, List<Transition>> buildIndex(List<Transition> transitions) {
        Map<Action, List<Transition>> index = new HashMap<>();
        for (Transition t : transitions) {
            index.computeIfAbsent(t.getAction(), k -> new ArrayList<>()).add(t);
        }
        return index;
    }

    /*
     * Convert iterator to list (single traversal)
     */
    private List<Transition> toList(Iterator<Transition> it) {
        List<Transition> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    protected void handleTransition(TransitionSystemFactory factory, Queue<Pair<State>> queue, Set<Pair<State>> visited,
                                    Map<Pair<State>, String> nameCache, Pair<State> sourcePair, Transition t1, Transition t2,
                                    String sourceName, Action action, boolean isSync) {

        Pair<State> targetPair = computeTargetStatePair(sourcePair, t1, t2, isSync);
        String targetName = nameCache.computeIfAbsent(targetPair, this::nameOf);
        factory.addTransition(sourceName, action.getName(), targetName);

        //processTransition(factory, queue, visited, nameCache, sourceName, action, targetPair, t1, t2, isSync);
        //addTransition(factory, queue, visited, nameCache, targetPair, sourceName, action);

        if (visited.add(targetPair)) {
            queue.add(targetPair);
        }
    }

    protected Pair<State> computeTargetStatePair(Pair<State> sourcePair, Transition t1, Transition t2, boolean isSync) {

        if (isSync) {
            return new Pair<>(t1.getTarget(), t2.getTarget());
        } else if (t1 != null) {
            return new Pair<>(t1.getTarget(), sourcePair.getRight());
        } else {
            return new Pair<>(sourcePair.getLeft(), t2.getTarget());
        }
    }

    /*
     * Collect actions once
     */
    private Set<Action> collectActions(TransitionSystem ts) {
        Set<Action> actions = new HashSet<>();
        Iterator<Action> it = ts.actions();
        while (it.hasNext()) {
            actions.add(it.next());
        }
        return actions;
    }
}
