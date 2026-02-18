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

import be.vibes.ts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

import java.util.*;

import static uk.kcl.info.utils.translators.TranslationUtils.*;

public class TsToBesConverter implements ModelConverter<TransitionSystem, BundleEventStructure> {

    private static final Logger LOG = LoggerFactory.getLogger(TsToBesConverter.class);

    private final TransitionSystem ts;;
    private final Map<Transition, Event> transitionEventMap = new HashMap<>();
    private final BundleEventStructureFactory factory = new BundleEventStructureFactory();

    public TsToBesConverter(TransitionSystem ts) {
        this.ts = Objects.requireNonNull(ts);
    }

    @Override
    public BundleEventStructure convert() {

        // Step 1: Collect actions & add events
        addEvents();
        // Step 2 & 3: Compute conflicts and (candidate) causality in a single loop
        computeConflictsAndCandidateBundles();
        // Set<CausalityRelation> candidateBundles = computeConflictsAndCandidateBundles();

        // Step 4: Optimize non-conflicting bundle splitting
        //addCausalities(candidateBundles);

        return factory.build();
    }

    private void addEvents() {
        Map<Action, Integer> actionCounter = new HashMap<>();
        int i = 0;

        for (Iterator<Transition> it = ts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            Action a = t.getAction();

            int j = actionCounter.getOrDefault(a, 0);
            String actionName = a.getName();
            String eventName = actionName + "_" + j;

            Event e = new Event(eventName, actionName);
            transitionEventMap.put(t, e);
            factory.addEvent(e);

            actionCounter.put(a, j + 1);
            i++;
            LOG.trace("Transitions to events: {}/{}", i, ts.getTransitionsCount());
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

                    boolean t1ToT2 = isReachable(ts, t1, t2);
                    boolean t2ToT1 = isReachable(ts, t2, t1);

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
            LOG.trace("Adding conflicts and candidate causalities: {}/{}", i, transitionEventMap.size());
        }

        //return candidateBundles; //splitBundlesOnConflicts(candidateBundles, conflicts);
    }
/*
    private void addCausalities(Set<CausalityRelation> bundles) { //Can be merged
        bundles.forEach(factory::addCausality);
    }*/
}
