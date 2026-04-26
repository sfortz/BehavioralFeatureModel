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

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.ts.exception.UnresolvedFExpression;
import uk.kcl.info.bfm.*;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;

import java.io.File;
import java.util.*;

public class BundleEventStructureExecutor {

    private final BundleEventStructure bes;

    public BundleEventStructureExecutor(BundleEventStructure bes) {
        this.bes = bes;
    }


    public boolean canExecute(List<Event> trace) {

        Set<Event> executed = new HashSet<>();

        for (Event event : trace) {

            // 1. Event must belong to the BES (sanity check)
            if (!bes.getAllEvents().contains(event)) {
                return false;
            }

            // 2. No repetition (configurations are sets in BES semantics)
            if (executed.contains(event)) {
                return false;
            }

            // 3. Conflict check
            if (isInConflictWithExecuted(event, executed)) {
                return false;
            }

            // 4. Causality (bundle satisfaction)
            if (!areAllCausalPredecessorsExecuted(event, executed)) {
                return false;
            }

            // 5. Execute event
            executed.add(event);
        }

        return true;
    }

    /**
     * Return all traces as sequences of action names.
     * This method should explore the BES configurations respecting causality and conflict.
     */
    public Set<List<String>> getAllActionTraces() {
        Set<List<Event>> eventTraces = new HashSet<>();
        Set<List<String>> actionTraces = new HashSet<>();
        exploreConfigurations(new ArrayList<>(), new HashSet<>(), eventTraces);

        for (List<Event> eventTrace : eventTraces) {
            List<String> actionTrace = eventTrace.stream().map(Event::getAction).toList();
            actionTraces.add(actionTrace);
        }

        return actionTraces;
    }

    /**
     * Return all traces as sequences of event names.
     * This method should explore the BES configurations respecting causality and conflict.
     */
    public Set<List<Event>> getAllEventTraces() {
        Set<List<Event>> traces = new HashSet<>();
        exploreConfigurations(new ArrayList<>(), new HashSet<>(), traces);
        return traces;
    }

    private void exploreConfigurations(List<Event> currentTrace, Set<Event> executed, Set<List<Event>> traces) {
        // Add current trace to traces
        traces.add(new ArrayList<>(currentTrace));

        // Find all next possible events that can be executed next:
        for (Event event : bes.getAllEvents()) {
            if (executed.contains(event)) continue;

            // Check conflicts: event not in conflict with executed events
            if (isInConflictWithExecuted(event, executed)) continue;

            // Check causality: all causal predecessors executed
            if (!areAllCausalPredecessorsExecuted(event, executed)) continue;

            // Execute this event next
            List<Event> newTrace = new ArrayList<>(currentTrace);
            newTrace.add(event);
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

    public Set<List<String>> getRandomActionTraces(int numTraces) {
        Set<List<Event>> eventTraces = getRandomEventTraces(numTraces);
        Set<List<String>> actionTraces = new HashSet<>();

        for (List<Event> trace : eventTraces) {
            List<String> actions = trace.stream()
                    .map(Event::getAction)
                    .toList();
            actionTraces.add(actions);
        }

        return actionTraces;
    }

    public Set<List<Event>> getRandomEventTraces(int numTraces) {
        Set<List<Event>> traces = new HashSet<>();
        Random random = new Random();

        int attempts = 0;
        int maxAttempts = numTraces * 10; // avoid infinite loops if duplicates

        while (traces.size() < numTraces && attempts < maxAttempts) {
            traces.add(generateRandomEventTrace(random));
            attempts++;
        }

        return traces;
    }

    private List<Event> generateRandomEventTrace(Random random) {
        List<Event> trace = new ArrayList<>();
        Set<Event> executed = new HashSet<>();

        while (true) {
            List<Event> enabled = getEnabledEvents(executed);

            if (enabled.isEmpty()) break;

            Event chosen = enabled.get(random.nextInt(enabled.size()));

            trace.add(chosen);
            executed.add(chosen);
        }

        return trace;
    }

    private List<Event> getEnabledEvents(Set<Event> executed) {
        List<Event> enabled = new ArrayList<>();

        for (Event event : bes.getAllEvents()) {
            if (executed.contains(event)) continue;
            if (isInConflictWithExecuted(event, executed)) continue;
            if (!areAllCausalPredecessorsExecuted(event, executed)) continue;

            enabled.add(event);
        }

        return enabled;
    }
}
