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
import uk.kcl.info.utils.Pair;

import java.util.*;

public class BESParallelComposer extends AbstractBESParallelComposer<BundleEventStructure> {

    @Override
    protected BundleEventStructureFactory createFactory() {
        return new BundleEventStructureFactory();
    }

    @Override
    protected void createEvents(BundleEventStructure bes1, BundleEventStructure bes2, boolean sync, BundleEventStructureFactory factory,
                                List<Event> events1, List<Event> events2, Set<String> actions1, Set<String> actions2,
                                Map<String, List<Event>> index2, Map<Pair<Event>, String> map) {

        /*
         * 1. Independent BES1 events
         */
        for (Event e : events1) {
            if (!sync || !actions2.contains(e.getAction())) {
                String name = e.getName() + "||" + STAR;
                factory.addEvent(name, e.getAction());
                map.put(new Pair<>(e, null), name);
            }
        }

        /*
         * 2. Independent BES2 events
         */
        for (Event f : events2) {
            if (!sync || !actions1.contains(f.getAction())) {
                String name = STAR + "||" + f.getName();
                factory.addEvent(name, f.getAction());
                map.put(new Pair<>(null, f), name);
            }
        }

        /*
         * 3. Synchronized events
         */
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
    }
}