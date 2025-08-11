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

package uk.kcl.info.bfm.execution;

import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.CausalityRelation;
import uk.kcl.info.bfm.Event;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;

import java.io.File;
import java.util.*;

public class BundleEventStructureExecutor {

    private final BundleEventStructure bes;

    public BundleEventStructureExecutor(BundleEventStructure bes) {
        this.bes = bes;
    }

    /**
     * Return all traces as sequences of event names.
     * This method should explore the BES configurations respecting causality and conflict.
     */
    public Set<List<String>> getAllTraces() {
        Set<List<String>> traces = new HashSet<>();
        exploreConfigurations(new ArrayList<>(), new HashSet<>(), traces);
        return traces;
    }

    private void exploreConfigurations(List<String> currentTrace, Set<Event> executed, Set<List<String>> traces) {
        // Add current trace to traces
        traces.add(new ArrayList<>(currentTrace));

        // Find all next possible events that can be executed next:
        for (Event event : bes.getAllEvents()) {
            if (executed.contains(event)) continue;

            // Check causality: all causal predecessors executed
            if (!areAllCausalPredecessorsExecuted(event, executed)) continue;

            // Check conflicts: event not in conflict with executed events
            if (isInConflictWithExecuted(event, executed)) continue;

            // Execute this event next
            List<String> newTrace = new ArrayList<>(currentTrace);
            newTrace.add(event.getName());
            Set<Event> newExecuted = new HashSet<>(executed);
            newExecuted.add(event);
            exploreConfigurations(newTrace, newExecuted, traces);
        }
    }

    private boolean areAllCausalPredecessorsExecuted(Event event, Set<Event> executed) {
        Iterator<CausalityRelation> causals = bes.getAllCausalitiesOfEvent(event);
        while (causals.hasNext()) {
            CausalityRelation cr = causals.next();
            // For this bundle to be satisfied, at least one event in the bundle must be executed
            boolean bundleSatisfied = cr.getBundle().stream().anyMatch(executed::contains);
            if (!bundleSatisfied) return false; // If any bundle is unsatisfied, event cannot execute
        }
        return true; // All bundles satisfied
    }

    private boolean isInConflictWithExecuted(Event event, Set<Event> executed) {
        for (Event executedEvent : executed) {
            if (bes.areInConflict(event, executedEvent)) return true;
        }
        return false;
    }

}
