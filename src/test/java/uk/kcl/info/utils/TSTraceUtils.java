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

package uk.kcl.info.utils;

import be.vibes.ts.*;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import be.vibes.ts.execution.TransitionSystemExecutor;

import java.util.*;

public class TSTraceUtils {

    private static final long SEED = 42; //System.currentTimeMillis();

    /**
     * Generates all possible execution traces of the given Transition System.
     * @param ts the transition system
     * @return a set of traces, each trace being a list of action names
     */
    public static Set<List<String>> getAllTsTraces(TransitionSystem ts) throws TransitionSystenExecutionException {
        Set<List<String>> traces = new HashSet<>();
        //TransitionSystemExecutor executor = new TransitionSystemExecutor(ts);
        //Set<String> visited = new HashSet<>();
        State initial = ts.getInitialState();
        explore(ts, initial, new ArrayList<>(), traces, new HashSet<>());
        //exploreTsTraces(ts, executor, new ArrayList<>(), traces, visited);
        return traces;
    }

    /**
     * Recursively explores the transition system to generate all execution traces.
     */
    private static void explore(TransitionSystem ts, State current, List<String> currentTrace, Set<List<String>> traces, Set<State> visited) {

        // Save current trace
        traces.add(new ArrayList<>(currentTrace));

        if (!visited.add(current)) return;

        Iterator<Transition> it = ts.getOutgoing(current);

        while (it.hasNext()) {
            Transition t = it.next();
            Action action = t.getAction();
            State target = t.getTarget();

            currentTrace.add(action.getName());

            explore(ts, target, currentTrace, traces, visited);

            currentTrace.removeLast();
        }

        visited.remove(current); // allow other paths
    }

    public static Set<List<String>> getRandomTraces(TransitionSystem ts, int maxTraces, int maxAttempts) throws TransitionSystenExecutionException {

        Set<List<String>> traces = new HashSet<>();
        Random rand = new Random(SEED);

        TransitionSystemExecutor executor = new TransitionSystemExecutor(ts);
        List<Action> allActions = actionsOf(ts);

        maxAttempts = maxTraces * maxAttempts; // avoid infinite loops if many deadlocks
        int attempts = 0; // Required for small systems, with few different traces

        while ((traces.size() < maxTraces) && attempts < maxAttempts) {
            List<String> trace = getRandomTrace(executor, allActions, rand); // , maxDepth, maxAttempts // required if cyclic FTS allowed
            //if (!(trace == null)) { // required if cyclic FTS allowed
            traces.add(trace);
            attempts++;
        }

        return traces;
    }

    protected static List<String> getRandomTrace(TransitionSystemExecutor executor, List<Action> allActions, Random random) throws TransitionSystenExecutionException {

        executor.reset();
        List<String> trace = new ArrayList<>();

        List<Action> enabled = getEnabledActions(executor, allActions);

        while(!enabled.isEmpty()){ //while(trace.size() < maxDepth) // required if cyclic FTS allowed
            // pick one action randomly
            Action chosen = enabled.get(random.nextInt(enabled.size()));
            executor.execute(chosen);
            trace.add(chosen.getName());
            enabled = getEnabledActions(executor, allActions);
        }

        return trace;
    }

    private static List<Action> getEnabledActions(TransitionSystemExecutor executor, List<Action> allActions) throws TransitionSystenExecutionException {
        // collect executable actions at this point
        List<Action> enabled = new ArrayList<>();
        for (Action a : allActions) {
            if (executor.canExecute(a)) {
                enabled.add(a);
            }
        }
        return enabled;
    }

    public static List<Action> actionsOf(TransitionSystem ts) {
        List<Action> actions = new ArrayList<>();
        for (Iterator<Action> it = ts.actions(); it.hasNext(); ) {
            actions.add(it.next());
        }
        return actions;
    }

}
