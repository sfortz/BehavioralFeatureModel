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
import be.vibes.ts.State;
import be.vibes.ts.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

import java.util.*;

import static uk.kcl.info.bfm.translators.TranslationUtils.*;

public class FtsToBfmConverter<F extends Feature<F>> implements ModelConverter<FeaturedTransitionSystem, BehavioralFeatureModel> {

    private static final Logger LOG = LoggerFactory.getLogger(FtsToBfmConverter.class);

    private final FeaturedTransitionSystem fts;
    private final FeatureModel<F> fm;

    private BehavioralFeatureModelFactory factory;
    private final Map<Transition, Event> transitionEventMap = new HashMap<>();

    public FtsToBfmConverter(FeatureModel<F> fm, FeaturedTransitionSystem fts) {
        this.fm = Objects.requireNonNull(fm);
        this.fts = Objects.requireNonNull(fts);
    }

    @Override
    public BehavioralFeatureModel convert() {
        this.factory = new BehavioralFeatureModelFactory(fm);

        addEvents();
        addConflicts();
        addCausalities();
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

            F ancestor = fm.getRootFeature();

            BehavioralFeature bf = factory.getFeature(ancestor.getFeatureName());
            FExpression fexpr = fts.getFExpression(t).applySimplification().toCnf();
            factory.addEvent(bf, e.getName(), a.getName(), fexpr);

            actionCounter.put(a, j + 1);
            i++;
            LOG.trace("Transitions to events: {}/{}", i, fts.getTransitionsCount());
        }
    }

    private void addConflicts() {

        String rootFeature= fm.getRootFeature().getFeatureName();

        // Liste indexée des transitions
        List<Map.Entry<Transition, Event>> entries = new ArrayList<>(transitionEventMap.entrySet());
        int n = entries.size();

        // Cache global des forward reachability
        Map<Transition, Map<State, Set<FExpression>>> reachCache = new HashMap<>();

        for (int i = 0; i < n; i++) {

            Transition t1 = entries.get(i).getKey();
            Event e1 = entries.get(i).getValue();
            Map<State, Set<FExpression>> r1 = reachCache.computeIfAbsent(t1, t -> forwardSymbolicReachable(fts, t));

            for (int j = i + 1; j < n; j++) {

                Transition t2 = entries.get(j).getKey();
                Event e2 = entries.get(j).getValue();

                boolean t1ToT2 = r1.getOrDefault(t2.getSource(), Set.of()).stream().anyMatch(f -> !f.isFalse());
                if (t1ToT2) continue;   // ← EARLY EXIT

                Map<State, Set<FExpression>> r2 = reachCache.computeIfAbsent(t2, t -> forwardSymbolicReachable(fts, t));

                boolean t2ToT1 = r2.getOrDefault(t1.getSource(), Set.of()).stream().anyMatch(f -> !f.isFalse());

                if (!t2ToT1) {
                    factory.addConflict(rootFeature, e1, e2);
                }
            }
            LOG.trace("Conflict matrix row: {}/{}", i, n);
        }
    }

    private void addCausalities() {

        String rootFeature= fm.getRootFeature().getFeatureName();

        Map<State, Set<Event>> incomingEvents = new HashMap<>();

        for (Map.Entry<Transition, Event> entry : transitionEventMap.entrySet()) {
            incomingEvents.computeIfAbsent(entry.getKey().getTarget(), k -> new HashSet<>()).add(entry.getValue());
        }

        int i = 0;

        for (Map.Entry<Transition, Event> entry1 : transitionEventMap.entrySet()) {

            Transition t1 = entry1.getKey();
            Event e1 = entry1.getValue();

            Set<Event> bundle = incomingEvents.getOrDefault(t1.getSource(), Set.of());

            if (!bundle.isEmpty()) {
                factory.addCausality(rootFeature, new CausalityRelation(bundle, e1));
            }

            i++;
            LOG.trace("Transitions to causalities: {}/{}", i, transitionEventMap.size());
        }
    }
}
