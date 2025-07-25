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
    private final Map<Event, F> featureMap = new HashMap<>();
    private final Map<Transition, Event> tMap = new HashMap<>();

    public FtsToBfmConverter(FeatureModel<F> fm, FeaturedTransitionSystem fts) {
        this.fm = Objects.requireNonNull(fm);
        this.fts = Objects.requireNonNull(fts);
    }

    public BehavioralFeatureModel convert() {
        this.factory = new BehavioralFeatureModelFactory(fm);

        addEvents();
        Set<CausalityRelation> candidateBundles = computeConflictsAndCandidateBundles();
        addCausalities(candidateBundles);

        return factory.build();
    }

    private void addEvents() {
        int i = 0;
        for (Iterator<Action> it = fts.actions(); it.hasNext(); ) {
            Action a = it.next();
            Event e = new Event(a.getName());

            FExpression combinedExpr = FExpression.falseValue();
            List<FExpression> exprList = new ArrayList<>();

            for (Iterator<Transition> transIt = fts.getTransitions(a); transIt.hasNext(); ) {
                Transition t = transIt.next();
                FExpression expr = fts.getFExpression(t);
                combinedExpr.orWith(expr);
                exprList.add(expr);
                tMap.put(t, e);
            }

            combinedExpr = combinedExpr.applySimplification().toCnf();
            F ancestor = fm.getLeastCommonAncestor(exprList);
            featureMap.put(e, ancestor);

            BehavioralFeature bf = factory.getFeature(ancestor.getFeatureName());
            factory.addEvent(bf, e.getName(), combinedExpr);

            i++;
            LOG.trace("Actions to events: {}/{}", i, fts.getActionsCount());
        }
    }

    private Set<CausalityRelation> computeConflictsAndCandidateBundles() {
        ConflictSet conflicts = new ConflictSet();
        Set<CausalityRelation> candidateBundles = new HashSet<>();
        int i = 0;

        for (Map.Entry<Transition, Event> entry1 : tMap.entrySet()) {
            Action a1 = entry1.getKey().getAction();
            Event e1 = entry1.getValue();

            Set<Event> bundle = new HashSet<>();

            for (Map.Entry<Transition, Event> entry2 : tMap.entrySet()) {
                Action a2 = entry2.getKey().getAction();

                if (!a1.equals(a2)) {
                    Event e2 = entry2.getValue();

                    boolean a1ToA2 = isReachable(fts, a1, a2);
                    boolean a2ToA1 = isReachable(fts, a2, a1);

                    if (!a1ToA2 && !a2ToA1) {
                        F lca = fm.getLeastCommonAncestor(featureMap.get(e1), featureMap.get(e2));
                        factory.addConflict(lca.getFeatureName(), e1, e2);
                        conflicts.addConflict(e1, e2);
                    }

                    if (isPredecessor(fts, a2, a1) && !a1ToA2) {
                        bundle.add(e2);
                    }
                }
            }

            if (!bundle.isEmpty()) {
                candidateBundles.add(new CausalityRelation(bundle, e1));
            }

            i++;
            LOG.trace("Transitions to conflicts and candidate causalities: {}/{}", i, tMap.size());
        }

        return splitBundlesOnConflicts(candidateBundles, conflicts);
    }

    private void addCausalities(Set<CausalityRelation> bundles) {
        int i = 0;
        for (CausalityRelation causality : bundles) {
            F lca = featureMap.get(causality.getTarget());
            for (Event e : causality.getBundle()) {
                lca = fm.getLeastCommonAncestor(lca, featureMap.get(e));
            }
            factory.addCausality(lca.getFeatureName(), causality);
            i++;
            LOG.trace("Adding causalities: {}/{}", i, bundles.size());
        }
    }
}
