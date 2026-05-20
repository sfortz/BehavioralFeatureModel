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

package uk.kcl.info.bfm.execution;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.ts.exception.UnresolvedFExpression;
import uk.kcl.info.bfm.*;

import java.util.*;

import static uk.kcl.info.bfm.execution.BundleEventStructureExecutor.getEnabledEvents;

public class FeaturedEventStructureExecutor {

    private final FeaturedEventStructure<?> fes;
    private final FeatureModel<?> fm;

    private static final long SEED = 155;

    public FeaturedEventStructureExecutor(FeaturedEventStructure<?> fes, FeatureModel<?> fm) {
        this.fes = fes;
        this.fm = fm;
    }

    public FeaturedEventStructureExecutor(BehavioralFeatureModel bfm) {
        this.fes = bfm;
        this.fm = bfm;
    }


    public Map<Configuration, Set<List<String>>> getAllActionTraces() throws ConstraintSolvingException, UnresolvedFExpression {

        BehavioralProduct proj = SimpleBehavioralProduct.getInstance();
        Map<Configuration, Set<List<String>>> tracesMap = new HashMap<>();
        Iterator<Configuration> it = fm.getSolutions();

        while(it.hasNext()){
            Configuration product = it.next();
            BundleEventStructure bes = proj.project(fes, fm.getFeatures(), product);
            BundleEventStructureExecutor exec = new BundleEventStructureExecutor(bes);
            tracesMap.put(product, exec.getAllActionTraces());
        }

        return tracesMap;
    }

    public Map<Configuration, Set<List<Event>>> getAllEventTraces() throws ConstraintSolvingException, UnresolvedFExpression {

        BehavioralProduct proj = SimpleBehavioralProduct.getInstance();
        Map<Configuration, Set<List<Event>>> tracesMap = new HashMap<>();
        Iterator<Configuration> it = fm.getSolutions();

        while(it.hasNext()){
            Configuration product = it.next();
            BundleEventStructure bes = proj.project(fes, fm.getFeatures(), product);
            BundleEventStructureExecutor exec = new BundleEventStructureExecutor(bes);
            tracesMap.put(product, exec.getAllEventTraces());
        }

        return tracesMap;
    }



    /**
     * Same as getRandomEventTraces but returns action traces.
     */
    public Map<FExpression, Set<List<String>>> getRandomActionTraces(int maxTraces, int maxAttempts) {

        Map<FExpression, Set<List<Event>>> eventMap = getRandomEventTraces(maxTraces, maxAttempts);
        Map<FExpression, Set<List<String>>> result = new HashMap<>();

        for (Map.Entry<FExpression, Set<List<Event>>> entry : eventMap.entrySet()) {

            Set<List<String>> actionTraces = new HashSet<>();

            for (List<Event> trace : entry.getValue()) {
                actionTraces.add(trace.stream().map(Event::getAction).toList());
            }

            result.put(entry.getKey(), actionTraces);
        }

        return result;
    }

    /**
     * Generate random traces grouped by their feature expressions.
     * Each trace is associated with the conjunction of the feature
     * expressions of all executed events.
     */
    public Map<FExpression, Set<List<Event>>> getRandomEventTraces(int maxTraces, int maxAttempts) {

        Map<FExpression, Set<List<Event>>> result = new HashMap<>();
        Random random = new Random(SEED);

        int attempts = 0;
        maxAttempts = maxTraces * maxAttempts;

        while (countTraces(result) < maxTraces && attempts < maxAttempts) {
            TraceWithExpression trace = generateRandomTrace(random);
            if(trace != null) {
                result.computeIfAbsent(trace.expression().applySimplification().toCnf(), k -> new HashSet<>()).add(trace.trace());
            }
            attempts++;
        }

        return result;
    }

    private record TraceWithExpression(List<Event> trace, FExpression expression) {}

    private TraceWithExpression generateRandomTrace(Random random) {

        List<Event> trace = new ArrayList<>();
        Set<Event> executed = new HashSet<>();

        FExpression expression = FExpression.trueValue();

        while (!expression.isFalse()) {

            List<Event> enabled = getEnabledEvents(fes, executed);

            if (enabled.isEmpty()) break;

            Event chosen = enabled.get(random.nextInt(enabled.size()));

            trace.add(chosen);
            executed.add(chosen);

            FExpression eventExpr = fes.getFExpression(chosen);

            if (eventExpr != null) {
                expression.andWith(eventExpr);
            }

            expression.applySimplification().toCnf();
        }

        if(expression.isFalse()) {return null;}
        return new TraceWithExpression(trace, expression);
    }

    private int countTraces(Map<FExpression, Set<List<Event>>> traces) {

        int count = 0;
        for (Set<List<Event>> set : traces.values()) {
            count += set.size();
        }
        return count;
    }

    public Map<FExpression, Set<List<Event>>> notExecutableEvents(Map<FExpression, Set<List<Event>>> traces) {

        Map<FExpression, Set<List<Event>>> nonExecutableTraces = new HashMap<>();

        for(Map.Entry<FExpression, Set<List<Event>>> entry: traces.entrySet()){
            FExpression expr = entry.getKey();
            for(List<Event> trace: entry.getValue()){
                if (!canExecuteEvents(trace)) {
                    nonExecutableTraces.computeIfAbsent(expr, k -> new HashSet<>()).add(trace);
                }
            }
        }

        return nonExecutableTraces;
    }

    public Map<FExpression, Set<List<String>>> notExecutableActions(Map<FExpression, Set<List<String>>> traces) {

        Map<FExpression, Set<List<String>>> nonExecutableTraces = new HashMap<>();

        for(Map.Entry<FExpression, Set<List<String>>> entry: traces.entrySet()){
            FExpression expr = entry.getKey();
            for(List<String> trace: entry.getValue()){
                if (!canExecuteActions(trace)) {
                    nonExecutableTraces.computeIfAbsent(expr, k -> new HashSet<>()).add(trace);
                }
            }
        }

        return nonExecutableTraces;
    }

    private boolean canExecuteActions(List<String> trace) {

        Map<String, List<Event>> mapping = fes.getActionEventMapping();

        Set<Event> executed = new HashSet<>();
        FExpression expression = FExpression.trueValue();

        for (String action : trace) {

            List<Event> candidateEvents = mapping.get(action);

            // no event corresponds to this action
            if (candidateEvents == null || candidateEvents.isEmpty()) {
                return false;
            }

            boolean executedOne = false;

            for (Event event : candidateEvents) {

                if (canExecute(event, executed, expression)) {
                    executed.add(event);
                    executedOne = true;
                    break;
                }
            }

            // no executable event for this action
            if (!executedOne) {
                return false;
            }
        }

        return true;
    }

    private boolean canExecuteEvents(List<Event> trace) {

        Set<Event> executed = new HashSet<>();
        FExpression expression = FExpression.trueValue();

        for (Event event : trace) {

            if (!canExecute(event, executed, expression)) {
                return false;
            }

            executed.add(event);
        }

        return true;
    }

    private boolean canExecute(Event event, Set<Event> executed, FExpression expression) {

        // event already executed
        if (executed.contains(event)) {
            return false;
        }

        // conflict check
        if (BundleEventStructureExecutor.isInConflictWithExecuted(fes, event, executed)) {
            return false;
        }

        // causality check
        if (!BundleEventStructureExecutor.areAllCausalPredecessorsExecuted(fes, event, executed)) {
            return false;
        }

        // feature constraint check
        FExpression updatedExpression = expression.copy();

        FExpression eventExpr = fes.getFExpression(event);

        if (eventExpr != null) {
            updatedExpression.andWith(eventExpr);
            updatedExpression.applySimplification().toCnf();

            // feature constraint became unsatisfiable
            if (updatedExpression.isFalse()) {
                return false;
            }

            // commit update
            expression.and(eventExpr).applySimplification().toCnf();
        }

        return true;
    }
}