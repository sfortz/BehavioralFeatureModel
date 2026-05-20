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

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.ts.*;
import be.vibes.ts.exception.*;
import be.vibes.ts.execution.Execution;
import be.vibes.ts.execution.FeaturedTransitionSystemExecutor;

import java.io.FileNotFoundException;
import java.util.*;

import static uk.kcl.info.utils.TSTraceUtils.*;

public class FTSTraceUtils {

    private static final long SEED = 155;

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

    public static Map<Configuration, Set<List<String>>> getRandomTraces(String name, FeatureModel<?> fm, FeaturedTransitionSystem fts, int maxConfig, long timeoutMillis, int maxTracesPerConfig, int maxAttempts) throws ConstraintSolvingException, UnresolvedFExpression, TransitionSystenExecutionException, FileNotFoundException {

        Projection proj = SimpleProjection.getInstance();
        Map<Configuration, Set<List<String>>> tracesMap = new HashMap<>();
        List<Configuration> configs = getRandomConfigs(fm, maxConfig, timeoutMillis);

        int i = 0;
        for(Configuration product : configs){
            TransitionSystem ts = proj.project(fts, product);

            String path = "src/test/resources/testcases/fts/ts_" + i + "_" + name + ".dot";
           // DotSavers.save(ts, path);

            tracesMap.put(product, TSTraceUtils.getRandomTraces(ts, maxTracesPerConfig, maxAttempts));
            i++;
        }

        return tracesMap;
    }

    public static Map<FExpression, Set<List<String>>> getRandomTraces(FeatureModel<?> fm, FeaturedTransitionSystem fts, int maxTraces, int maxAttempts) throws TransitionSystenExecutionException, ConstraintSolvingException {

        Map<FExpression, Set<List<String>>> tracesMap = new HashMap<>();
        FeaturedTransitionSystemExecutor executor = new FeaturedTransitionSystemExecutor(fts, fm);
        List<Action> allActions = actionsOf(fts);
        Random rand = new Random(SEED);
        maxAttempts = maxTraces * maxAttempts; // avoid infinite loops if many deadlocks
        int i = 0;

        while ((tracesMap.size() < maxTraces) && (i < maxAttempts)) {
            List<String> trace = getRandomTrace(executor, allActions, rand);
            executor.reset();
            FExpression fexpr = getTraceFexpression(executor, trace);
            tracesMap.computeIfAbsent(fexpr, c -> new HashSet<>()).add(trace);
            i++;
        }

        return tracesMap;
    }


    private static FExpression getTraceFexpression(FeaturedTransitionSystemExecutor executor, List<String> trace) throws TransitionSystenExecutionException {


        for (String action : trace) {
            if (!executor.canExecute(action)) {
                executor.reset();
                throw new TransitionSystenExecutionException("Invalid trace provided.");
            }
            executor.execute(action);
        }

        Iterator<Execution> execs = executor.getCurrentExecutions();

        if(!execs.hasNext()){
            executor.reset();
            throw new TransitionSystenExecutionException("Invalid trace provided.");
        }

        FExpression fexpr = FExpression.falseValue();

        while (execs.hasNext()) {
            fexpr.orWith(executor.getFexpression(execs.next()));
        }

        if(fexpr.isFalse()){
            executor.reset();
            throw new TransitionSystenExecutionException("Trace with invalid FExpression provided.");
        }

        executor.reset();
        return fexpr;
    }



    private static boolean executable(FeaturedTransitionSystemExecutor executor, FExpression productFexpr, List<String> trace) throws TransitionSystenExecutionException {

        executor.reset();

        for (String action : trace) {
            if (!executor.canExecute(action)) {
                executor.reset();
                return false;
            }
            executor.execute(action);
        }

        Iterator<Execution> execs = executor.getCurrentExecutions();

        if(execs.hasNext()){
            executor.reset();
            return true;
        } else {
            executor.reset();
            return false;
        }
    }

    public static Map<FExpression, Set<List<String>>> notExecutable(Map<FExpression, Set<List<String>>> traces,
                                                                    FeaturedTransitionSystemExecutor exec) throws TransitionSystenExecutionException {

        Map<FExpression, Set<List<String>>> nonExecutableTraces = new HashMap<>();

        for(Map.Entry<FExpression, Set<List<String>>> setOfTrace: traces.entrySet()){

            FExpression fexpr = setOfTrace.getKey().applySimplification().toCnf();
            /*
            Configuration config = setOfTrace.getKey();
            FExpression productFexpr = FExpression.trueValue();
            for(Feature<?> f: config.getFeatures()){
                productFexpr.andWith(FExpression.featureExpr(f));
            }
            productFexpr.applySimplification();*/

            for(List<String> t: setOfTrace.getValue()){
                if(!executable(exec, null, t)){
                    nonExecutableTraces.computeIfAbsent(fexpr, c -> new HashSet<>()).add(t);
                }
            }
        }

        return nonExecutableTraces;
    }

    /*
    public static Map<Configuration, Set<List<String>>> notExecutable(Map<Configuration, Set<List<String>>> traces,
                                                                      FeaturedTransitionSystemExecutor exec) throws TransitionSystenExecutionException {

        Map<Configuration, Set<List<String>>> nonExecutableTraces = new HashMap<>();

        for(Map.Entry<Configuration, Set<List<String>>> setOfTrace: traces.entrySet()){

            Configuration config = setOfTrace.getKey();
            FExpression productFexpr = FExpression.trueValue();
            for(Feature<?> f: config.getFeatures()){
                productFexpr.andWith(FExpression.featureExpr(f));
            }
            productFexpr.applySimplification();

            for(List<String> t: setOfTrace.getValue()){
                if(!executable(exec, productFexpr, t)){
                    nonExecutableTraces.computeIfAbsent(config, c -> new HashSet<>()).add(t);
                }
            }
        }

        return nonExecutableTraces;
    }*/

    public static List<Configuration> getRandomConfigs(FeatureModel<?> fm, int numConfig, long timeoutMillis) throws ConstraintSolvingException {

        List<Configuration> reservoir = new ArrayList<>(numConfig);
        Iterator<Configuration> it = fm.getSolutions();
        Random random = new Random(SEED);

        long deadline = System.currentTimeMillis() + timeoutMillis;
        int count = 0;

        while (it.hasNext() && System.currentTimeMillis() < deadline) {
            Configuration config = it.next();
            count++;

            if (reservoir.size() < numConfig) {
                reservoir.add(config); // fill reservoir
            } else {
                int j = random.nextInt(count); // 0 .. count-1
                if (j < numConfig) {
                    reservoir.set(j, config); // replace one element
                }
            }
        }

        return reservoir;
    }
}
