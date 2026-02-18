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
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.ts.Action;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

import java.util.*;

import static uk.kcl.info.utils.translators.TranslationUtils.*;

public class FtsToBfmConverter<F extends Feature<F>> implements ModelConverter<FeaturedTransitionSystem, BehavioralFeatureModel> {

    private static final Logger LOG = LoggerFactory.getLogger(FtsToBfmConverter.class);

    private final FeaturedTransitionSystem fts;
    private final FeatureModel<F> fm;

    private BehavioralFeatureModelFactory factory;
    //private final Map<Event, F> featureMap = new HashMap<>();
    private final Map<Transition, Event> transitionEventMap = new HashMap<>();

    public FtsToBfmConverter(FeatureModel<F> fm, FeaturedTransitionSystem fts) {
        this.fm = Objects.requireNonNull(fm);
        this.fts = Objects.requireNonNull(fts);
    }

    @Override
    public BehavioralFeatureModel convert() {
        this.factory = new BehavioralFeatureModelFactory(fm);

        addEvents();
        computeConflictsAndCandidateBundles();
        // Set<CausalityRelation> candidateBundles = computeConflictsAndCandidateBundles();
        //addCausalities(candidateBundles);

        return factory.build();
    }

    private void addEvents() {
        Map<Action, Integer> actionCounter = new HashMap<>();
        //Map<Action, List<FExpression>> exprListMap = new HashMap<>();

        int i = 0;

        for (Iterator<Transition> it = fts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            Action a = t.getAction();

            int j = actionCounter.getOrDefault(a, 0);
            String actionName = a.getName();
            String eventName = actionName + "_" + j;

            Event e = new Event(eventName, actionName);
            transitionEventMap.put(t, e);

            /*
            // Accumulate expressions
            FExpression expr = fts.getFExpression(t);
            combinedExprMap.computeIfAbsent(a, k -> FExpression.falseValue()).orWith(expr);
            exprListMap.computeIfAbsent(a, k -> new ArrayList<>()).add(expr);
            transitionEventMap.put(t, e);

            FExpression combinedExpr = combinedExprMap.get(a).applySimplification().toCnf();*/

            // F ancestor = fm.getLeastCommonAncestor(exprListMap.get(a));
            F ancestor = fm.getRootFeature();
            //featureMap.put(e, ancestor);

            BehavioralFeature bf = factory.getFeature(ancestor.getFeatureName());
            FExpression fexpr = fts.getFExpression(t).applySimplification().toCnf();
            factory.addEvent(bf, e.getName(), a.getName(), fexpr);

            actionCounter.put(a, j + 1);
            i++;
            LOG.trace("Transitions to events: {}/{}", i, fts.getTransitionsCount());
        }

        /*
        i = 0;

        // Finalise per action
        for (Map.Entry<Action, Event> entry : actionEventMap.entrySet()) {
            Action a = entry.getKey();
            Event e = entry.getValue();

            FExpression combinedExpr = combinedExprMap.get(a).applySimplification().toCnf();
            // F ancestor = fm.getLeastCommonAncestor(exprListMap.get(a));
            F ancestor = fm.getRootFeature();
            featureMap.put(e, ancestor);

            BehavioralFeature bf = factory.getFeature(ancestor.getFeatureName());
            factory.addEvent(bf, e.getName(), combinedExpr);

            i++;
            LOG.trace("Actions to events: {}/{}", i, actionEventMap.size());
        }*/
    }

    //private Set<CausalityRelation> computeConflictsAndCandidateBundles() {
    private void computeConflictsAndCandidateBundles() {
        ConflictSet conflicts = new ConflictSet();
        Set<CausalityRelation> candidateBundles = new HashSet<>();
        int i = 0;

        for (Map.Entry<Transition, Event> entry1 : transitionEventMap.entrySet()) {
            Transition t1 = entry1.getKey();
            Event e1 = entry1.getValue();
            Set<Event> bundle = new HashSet<>();

            for (Map.Entry<Transition, Event> entry2 : transitionEventMap.entrySet()) {
                Transition t2 = entry2.getKey();
                if (!t1.equals(t2)) {
                    Event e2 = entry2.getValue();

                    boolean t1ToT2 = isReachable(fts, t1, t2);
                    boolean t2ToT1 = isReachable(fts, t2, t1);

                    if (!t1ToT2 && !t2ToT1) {
                        //F lca = fm.getLeastCommonAncestor(featureMap.get(e1), featureMap.get(e2));
                        //factory.addConflict(lca.getFeatureName(), e1, e2);
                        factory.addConflict(fm.getRootFeature().getFeatureName(), e1, e2);
                        conflicts.addConflict(e1, e2);
                    }

                    if (isPredecessor(t2, t1)) { //&& !t1ToT2 Only needed if non-linear
                        bundle.add(e2);
                    }
                }
            }

            if (!bundle.isEmpty()) {
                //candidateBundles.add(new CausalityRelation(bundle, e1));
                factory.addCausality(fm.getRootFeature().getFeatureName(), new CausalityRelation(bundle, e1));
            }

            i++;
            LOG.trace("Transitions to conflicts and candidate causalities: {}/{}", i, transitionEventMap.size());
        }

        //return candidateBundles; //splitBundlesOnConflicts(candidateBundles, conflicts);
    }

    /*
    private void addCausalities(Set<CausalityRelation> bundles) {
        int i = 0;
        for (CausalityRelation causality : bundles) {
            /*F lca = featureMap.get(causality.getTarget());
            for (Event e : causality.getBundle()) {
                lca = fm.getLeastCommonAncestor(lca, featureMap.get(e));
            }
            factory.addCausality(lca.getFeatureName(), causality);

     *//*
            factory.addCausality(fm.getRootFeature().getFeatureName(), causality);
            i++;
            LOG.trace("Adding causalities: {}/{}", i, bundles.size());
        }
    }*/
}
