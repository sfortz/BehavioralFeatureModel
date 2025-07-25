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
import java.util.Map.Entry;

import static uk.kcl.info.utils.translators.TranslationUtils.*;

public class TsToBesConverter implements ModelConverter<TransitionSystem, BundleEventStructure> {

    private static final Logger LOG = LoggerFactory.getLogger(TsToBesConverter.class);

    private final BundleEventStructureFactory factory;
    private final TransitionSystem ts;
    private final Map<Action, Event> eventMap = new HashMap<>();

    public TsToBesConverter(TransitionSystem ts) {
        this.ts = Objects.requireNonNull(ts);
        this.factory = new BundleEventStructureFactory();
    }

    public BundleEventStructure convert() {

        // Step 1: Collect actions & add events
        addEvents();
        // Step 2 & 3: Compute conflicts and (candidate) causality in a single loop
        Set<CausalityRelation> candidateBundles = computeConflictsAndCandidateBundles();
        // Step 4: Optimize non-conflicting bundle splitting
        addCausalities(candidateBundles);

        return factory.build();
    }

    private void addEvents() {
        int i = 0;
        for (Iterator<Action> it = ts.actions(); it.hasNext(); ) {
            Action a = it.next();
            Event e = new Event(a.getName());
            eventMap.put(a, e);
            factory.addEvent(e.getName());
            i++;
            LOG.trace("Actions to events: {}/{}", i, ts.getActionsCount());
        }
    }

    private Set<CausalityRelation> computeConflictsAndCandidateBundles() {
        int i = 0;
        ConflictSet conflicts = new ConflictSet();
        Set<CausalityRelation> candidateBundles = new HashSet<>();

        for (Entry<Action, Event> entry1 : eventMap.entrySet()) {
            Action a1 = entry1.getKey();
            Event e1 = entry1.getValue();
            Set<Event> bundle = new HashSet<>();

            for (Entry<Action, Event> entry2 : eventMap.entrySet()) {
                Action a2 = entry2.getKey();
                if (!a1.equals(a2)) {
                    Event e2 = entry2.getValue();

                    boolean a1ToA2 = isReachable(ts, a1, a2);
                    boolean a2ToA1 = isReachable(ts, a2, a1);

                    if (!a1ToA2 && !a2ToA1) {
                        factory.addConflict(e1, e2);
                        conflicts.addConflict(e1, e2);
                    }

                    if (isPredecessor(ts, a2, a1) && !a1ToA2) {
                        bundle.add(e2);
                    }
                }
            }

            if (!bundle.isEmpty()) {
                candidateBundles.add(new CausalityRelation(bundle, e1));
            }

            i++;
            LOG.trace("Adding conflicts and candidate causalities: {}/{}", i, eventMap.size());
        }

        return splitBundlesOnConflicts(candidateBundles, conflicts);
    }

    private void addCausalities(Set<CausalityRelation> bundles) {
        bundles.forEach(factory::addCausality);
    }

}
