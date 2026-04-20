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

import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.ts.*;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import be.vibes.ts.exception.UnresolvedFExpression;
import be.vibes.ts.execution.TransitionSystemExecutor;

import java.util.*;

public class TSTraceUtils {

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

    /**
     * Generates all possible execution traces (for all products) of the given Featured Transition System.
     * @param fm the feature model
     * @param fts the featured transition system
     * @return a set of traces, each trace being a list of action names
     */
    public static Map<Configuration, Set<List<String>>> getAllFtsTraces(FeatureModel<?> fm, FeaturedTransitionSystem fts) throws ConstraintSolvingException, UnresolvedFExpression, TransitionSystenExecutionException {

        Projection proj = SimpleProjection.getInstance();
        Map<Configuration, Set<List<String>>> tracesMap = new HashMap<>();
        Iterator<Configuration> it = fm.getSolutions();

        while(it.hasNext()){
            Configuration product = it.next();
            TransitionSystem ts = proj.project(fts, product);
            tracesMap.put(product, getAllTsTraces(ts));
        }

        return tracesMap;
    }

}
