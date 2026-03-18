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

import java.util.*;

public class TSParallelComposer implements Composition<TransitionSystem> {

    @Override
    public TransitionSystem compose(TransitionSystem ts1, TransitionSystem ts2, boolean sync) {

        String initName = ts1.getInitialState().getName() + "||" + ts2.getInitialState().getName();
        TransitionSystemFactory factory = new TransitionSystemFactory(initName);

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

        Pair<State> initPair = new Pair<>(ts1.getInitialState(), ts2.getInitialState());
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
                    // Independent transition
                    addTransition(factory, queue, visited, nameCache,
                            new Pair<>(t1.getTarget(), s2),
                            srcName, action);
                } else {
                    // Synchronized transitions
                    List<Transition> matches = index2.get(action);
                    if (matches == null) continue;

                    for (Transition t2 : matches) {
                        addTransition(factory, queue, visited, nameCache,
                                new Pair<>(t1.getTarget(), t2.getTarget()),
                                srcName, action);
                    }
                }
            }

            /*
             * TS2 independent transitions
             */
            for (Transition t2 : out2) {
                Action action = t2.getAction();

                if (!sync || !shared.contains(action)) {
                    addTransition(factory, queue, visited, nameCache,
                            new Pair<>(s1, t2.getTarget()),
                            srcName, action);
                }
            }
        }

        return factory.build();
    }

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

    /*
     * Add transition + BFS expansion + name caching
     */
    private void addTransition(TransitionSystemFactory factory,
                               Queue<Pair<State>> queue,
                               Set<Pair<State>> visited,
                               Map<Pair<State>, String> nameCache,
                               Pair<State> targetPair,
                               String sourceName,
                               Action action) {

        String targetName = nameCache.computeIfAbsent(targetPair, p -> p.getLeft().getName() + "||" + p.getRight().getName());

        factory.addTransition(sourceName, action.getName(), targetName);

        if (visited.add(targetPair)) {
            queue.add(targetPair);
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
