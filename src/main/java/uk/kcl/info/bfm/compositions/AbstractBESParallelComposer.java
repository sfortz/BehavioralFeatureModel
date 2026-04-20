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

package uk.kcl.info.bfm.compositions;

import uk.kcl.info.utils.Pair;
import uk.kcl.info.bfm.*;
import java.util.*;

public abstract class AbstractBESParallelComposer <T extends BundleEventStructure> implements Composition<T>{

    protected static final String STAR = "*";

    protected abstract BundleEventStructureFactory createFactory();

    @Override
    public T compose(T bes1, T bes2, boolean sync) {

        BundleEventStructureFactory factory = createFactory();

        // Cache events
        List<Event> events1 = new ArrayList<>(bes1.getAllEvents());
        List<Event> events2 = new ArrayList<>(bes2.getAllEvents());

        // Collect actions
        Set<String> actions1 = collectActions(events1);
        Set<String> actions2 = collectActions(events2);

        Map<String, List<Event>> index2 = sync ? indexByAction(events2) : Collections.emptyMap();

        Map<Pair<Event>, String> eventMap = new HashMap<>();

        /*
         * 1. CREATE EVENTS (delegated)
         */
        createEvents(bes1, bes2, sync, factory, events1, events2, actions1, actions2, index2, eventMap);

        /*
         * 2. BUILD INDEXES
         */
        Map<Event, Set<String>> byLeft = new HashMap<>();
        Map<Event, Set<String>> byRight = new HashMap<>();

        for (Map.Entry<Pair<Event>, String> entry : eventMap.entrySet()) {
            Event e = entry.getKey().getLeft();
            Event f = entry.getKey().getRight();

            if (e != null) {
                byLeft.computeIfAbsent(e, k -> new HashSet<>()).add(entry.getValue());
            }
            if (f != null) {
                byRight.computeIfAbsent(f, k -> new HashSet<>()).add(entry.getValue());
            }
        }

        /*
         * 3. CONFLICTS
         */
        // (1) Same-left conflicts: (e1 != null && e1.equals(e2) && !Objects.equals(f1, f2))
        addLocalConflicts(byLeft, factory);
        // (2) Same-right conflicts: (f1 != null && f1.equals(f2) && !Objects.equals(e1, e2))
        addLocalConflicts(byRight, factory);

        // (3) Lift BES1 conflicts: (e1 != null && e2 != null && bes1.areInConflict(e1, e2))
        liftConflicts(bes1, byLeft, factory);
        // (4) Lift BES2 conflicts: (f1 != null && f2 != null && bes2.areInConflict(f1, f2))
        liftConflicts(bes2, byRight, factory);

        /*
         * 4. CAUSALITIES
         */
        for (Map.Entry<Pair<Event>, String> entry : eventMap.entrySet()) {
            liftCausalities(bes1, entry.getKey().getLeft(), byLeft, entry.getValue(), factory);
            liftCausalities(bes2, entry.getKey().getRight(), byRight, entry.getValue(), factory);
        }

        return (T) factory.build();
    }

    /*
     * --- ABSTRACT HOOK: event creation differs for BES vs FES ---
     */
    protected abstract void createEvents(T bes1, T bes2, boolean sync, BundleEventStructureFactory factory,
                                         List<Event> events1, List<Event> events2, Set<String> actions1, Set<String> actions2,
                                         Map<String, List<Event>> index2, Map<Pair<Event>, String> map);

    /*
     * -----------------------------
     * Shared logic
     * -----------------------------
     */
    protected Set<String> collectActions(List<Event> events) {
        Set<String> actions = new HashSet<>();
        for (Event e : events) actions.add(e.getAction());
        return actions;
    }

    protected Map<String, List<Event>> indexByAction(List<Event> events) {
        Map<String, List<Event>> index = new HashMap<>();
        for (Event e : events) {
            index.computeIfAbsent(e.getAction(), k -> new ArrayList<>()).add(e);
        }
        return index;
    }

    /*
     * Add all unordered pairs as conflicts
     */
    protected void addLocalConflicts(Map<Event, Set<String>> grouped, BundleEventStructureFactory factory) {

        for (Set<String> group : grouped.values()) {
            //if (group.size() > 1) continue;

            List<String> list = new ArrayList<>(group);

            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    factory.addConflict(list.get(i), list.get(j));
                }
            }
        }
    }

    protected void liftConflicts(BundleEventStructure bes, Map<Event, Set<String>> index, BundleEventStructureFactory factory) {

        List<Event> events = new ArrayList<>(index.keySet());

        for (int i = 0; i < events.size(); i++) {
            for (int j = i + 1; j < events.size(); j++) {

                Event e1 = events.get(i);
                Event e2 = events.get(j);

                if (!bes.areInConflict(e1, e2)) continue;

                for (String n1 : index.get(e1)) {
                    for (String n2 : index.get(e2)) {
                        factory.addConflict(n1, n2);
                    }
                }
            }
        }
    }

    protected void liftCausalities(BundleEventStructure bes, Event source, Map<Event, Set<String>> index,
                                   String target, BundleEventStructureFactory factory) {

        if (source == null) return;

        Iterator<CausalityRelation> it = bes.getIncomingCausalities(source);

        while (it.hasNext()) {
            CausalityRelation rel = it.next();
            Set<String> bundle = new HashSet<>();

            for (Event cause : rel.getBundle()) {
                Set<String> lifted = index.get(cause);
                if (lifted != null) {
                    bundle.addAll(lifted);
                }
            }

            if (!bundle.isEmpty()) {
                factory.addCausality(bundle, target);
            }
        }
    }
}