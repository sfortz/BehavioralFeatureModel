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

import uk.kcl.info.bfm.*;
import java.util.*;

public class BESParallelComposer implements Composition<BundleEventStructure> {

    private static final String STAR = "*";

    @Override
    public BundleEventStructure compose(BundleEventStructure bes1, BundleEventStructure bes2, boolean sync) {

        BundleEventStructureFactory factory = new BundleEventStructureFactory();

        // Cache events
        List<Event> events1 = new ArrayList<>(bes1.getAllEvents());
        List<Event> events2 = new ArrayList<>(bes2.getAllEvents());

        // Collect actions
        Set<String> actions1 = new HashSet<>();
        Set<String> actions2 = new HashSet<>();

        for (Event e : events1) {
            actions1.add(e.getAction());
        }

        for (Event f : events2) {
            actions2.add(f.getAction());
        }

        // Index BES2 by action for sync events
        Map<String, List<Event>> index2 = new HashMap<>();
        if (sync) {
            for (Event f : events2) {
                index2.computeIfAbsent(f.getAction(), k -> new ArrayList<>()).add(f);
            }
        }

        Map<Pair<Event>, String> map = new HashMap<>();

        /*
         * --------------------------------
         * 1 CREATE EVENTS
         * --------------------------------
         */
        // Independent events from BES1
        for (Event e : events1) {
            if (!sync || !actions2.contains(e.getAction())) {
                String name = e.getName() + "||" + STAR;
                factory.addEvent(name, e.getAction());
                map.put(new Pair<>(e, null), name);
            }
        }

        // Independent events from BES2
        for (Event f : events2) {
            if (!sync || !actions1.contains(f.getAction())) {
                String name = STAR + "||" + f.getName();
                factory.addEvent(name, f.getAction());
                map.put(new Pair<>(null, f), name);
            }
        }

        // Synchronized events (ONLY if sync = true)
        if (sync) {
            for (Event e : events1) {
                List<Event> matches = index2.get(e.getAction());
                if (matches == null) continue;

                for (Event f : matches) {
                    String name = e.getName() + "||" + f.getName();
                    factory.addEvent(name, e.getAction());
                    map.put(new Pair<>(e, f), name);
                }
            }
        }

        /*
         * --------------------------------
         * 2 BUILD INDEXES (for conflicts + causality)
         * --------------------------------
         */
        Map<Event, Set<String>> byLeft = new HashMap<>();
        Map<Event, Set<String>> byRight = new HashMap<>();

        for (Map.Entry<Pair<Event>, String> entry : map.entrySet()) {
            Pair<Event> pair = entry.getKey();
            String name = entry.getValue();

            Event e = pair.getLeft();
            Event f = pair.getRight();

            if (e != null) {
                byLeft.computeIfAbsent(e, k -> new HashSet<>()).add(name);
            }
            if (f != null) {
                byRight.computeIfAbsent(f, k -> new HashSet<>()).add(name);
            }
        }

        /*
         * --------------------------------
         * 3 ADD CONFLICTS
         * --------------------------------
         */
        // (1) Same-left conflicts: (e1 != null && e1.equals(e2) && !Objects.equals(f1, f2))
        for (Set<String> group : byLeft.values()) {
            addAllPairsAsConflicts(group, factory);
        }

        // (2) Same-right conflicts: (f1 != null && f1.equals(f2) && !Objects.equals(e1, e2))
        for (Set<String> group : byRight.values()) {
            addAllPairsAsConflicts(group, factory);
        }

        // (3) Lift BES1 conflicts: (e1 != null && e2 != null && bes1.areInConflict(e1, e2))
        liftConflicts(bes1, byLeft, factory);

        // (4) Lift BES2 conflicts: (f1 != null && f2 != null && bes2.areInConflict(f1, f2))
        liftConflicts(bes2, byRight, factory);

        /*
         * --------------------------------
         * 4 ADD CAUSALITIES
         * --------------------------------
         */
        for (Map.Entry<Pair<Event>, String> entry : map.entrySet()) {
            Pair<Event> pair = entry.getKey();
            String target = entry.getValue();

            liftCausalities(bes1, pair.getLeft(), byLeft, target, factory);
            liftCausalities(bes2, pair.getRight(), byRight, target, factory);
        }

        return factory.build();
    }

    /*
     * Add all unordered pairs as conflicts
     */
    private void addAllPairsAsConflicts(Set<String> group, BundleEventStructureFactory factory) {

        if (group.size() > 1) return;

        List<String> list = new ArrayList<>(group);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            String a = list.get(i);
            for (int j = i + 1; j < size; j++) {
                factory.addConflict(a, list.get(j));
            }
        }
    }

    private void liftConflicts(BundleEventStructure bes, Map<Event, Set<String>> sidedEvents, BundleEventStructureFactory factory) {

        List<Event> events = new ArrayList<>(sidedEvents.keySet());

        for (int i = 0; i < events.size(); i++) {

            Event e1 = events.get(i);
            Set<String> names1 = sidedEvents.get(e1);

            for (int j = i + 1; j < events.size(); j++) {

                Event e2 = events.get(j);

                if (!bes.areInConflict(e1, e2)) continue;

                Set<String> names2 = sidedEvents.get(e2);

                for (String n1 : names1) {
                    for (String n2 : names2) {
                        factory.addConflict(n1, n2);
                    }
                }
            }
        }
    }

    private void liftCausalities(BundleEventStructure bes, Event source, Map<Event, Set<String>> index, String target,
                                 BundleEventStructureFactory factory) {

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