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

package uk.kcl.info.bfm.translators;

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

import static uk.kcl.info.bfm.translators.TranslationUtils.*;

public class FtsToFesConverter implements ModelConverter<FeaturedTransitionSystem, FeaturedEventStructure<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(FtsToFesConverter.class);

    private final FeaturedTransitionSystem fts;
    private final FeatureModel<?> fm;

    private FeaturedEventStructureFactory factory;
    private final Map<Transition, Event> transitionEventMap = new HashMap<>();

    public FtsToFesConverter(FeatureModel<?> fm, FeaturedTransitionSystem fts) {
        this.fm = Objects.requireNonNull(fm);
        this.fts = Objects.requireNonNull(fts);
    }

    @Override
    public FeaturedEventStructure<?> convert() {
        this.factory = new FeaturedEventStructureFactory(fm);

        addEvents();
        computeConflictsAndCandidateBundles();
        // Set<CausalityRelation> candidateBundles = computeConflictsAndCandidateBundles();
        //addCausalities(candidateBundles);

        return factory.build();
    }

    private void addEvents() {
        Map<Action, Integer> actionCounter = new HashMap<>();

        int i = 0;

        for (Iterator<Transition> it = fts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            Action a = t.getAction();

            int j = actionCounter.getOrDefault(a, 0);
            String actionName = a.getName();
            String eventName = actionName + "_" + j;

            Event e = new Event(eventName, actionName);
            transitionEventMap.put(t, e);

            Feature<?> ancestor = fm.getRootFeature();

            FExpression fexpr = fts.getFExpression(t).applySimplification().toCnf();
            factory.addEvent(e.getName(), a.getName(), ancestor, fexpr);

            actionCounter.put(a, j + 1);
            i++;
            LOG.trace("Transitions to events: {}/{}", i, fts.getTransitionsCount());
        }
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
                        factory.addConflict(e1, e2);
                        conflicts.addConflict(e1, e2);
                    }

                    if (isPredecessor(t2, t1)) { //&& !t1ToT2 Only needed if non-linear
                        bundle.add(e2);
                    }
                }
            }

            if (!bundle.isEmpty()) {
                //candidateBundles.add(new CausalityRelation(bundle, e1));
                factory.addCausality(new CausalityRelation(bundle, e1));
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
            factory.addCausality(causality);
            i++;
            LOG.trace("Adding causalities: {}/{}", i, bundles.size());
        }
    }*/
}
