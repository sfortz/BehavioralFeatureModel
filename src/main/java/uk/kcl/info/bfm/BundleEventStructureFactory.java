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

package uk.kcl.info.bfm;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BundleEventStructureFactory {
    protected final DefaultBundleEventStructure bes;

    protected BundleEventStructureFactory(DefaultBundleEventStructure bes) {
        this.bes = bes;
    }

    public BundleEventStructureFactory() {
        this(new DefaultBundleEventStructure());
    }

    public void addEvent(String name) {
        this.bes.addEvent(name);
    }

    public void addCausality(Set<String> bundle, String target) {

        Event trg = new Event(target);
        Set<Event> bndl = new HashSet<>();
        for(String name: bundle) {
            Event event = new Event(name);
            bndl.add(event);
        }

        this.bes.addCausality(bndl, trg);
    }

    public void addCausality(Set<Event> bundle, Event target) {
        this.bes.addCausality(bundle, target);
    }

    public void addCausality(CausalityRelation causalityRelation) {
        this.bes.addCausality(causalityRelation);
    }

    public void addConflict(String event1, String event2) {
        this.addConflict(new Event(event1), new Event(event2));
    }

    public void addConflict(Event event1, Event event2) {
        Set<Event> allEvents = new HashSet<>(this.bes.getAllEvents());
        Preconditions.checkArgument(allEvents.contains(event1), event1 + " does not belong to this bundle event structure!");
        Preconditions.checkArgument(allEvents.contains(event2), event2 + " does not belong to this bundle event structure!");
        this.bes.getConflictSet().addConflict(event1, event2);
    }

    public void addConflicts(Event event1, Collection<Event> group) {
        Set<Event> allEvents = new HashSet<>(this.bes.getAllEvents());
        Preconditions.checkArgument(allEvents.contains(event1), event1 + " does not belong to this bundle event structure!");
        Preconditions.checkArgument(allEvents.containsAll(group), "All events of a conflict should belong to the bundle event structure!");
        this.bes.getConflictSet().addConflicts(event1, group);
    }

    public void addConflicts(Collection<?> group1, Collection<?> group2) {
        Set<Event> allEvents = new HashSet<>(this.bes.getAllEvents());

        Set<Event> events1 = toEventSet(group1, allEvents);
        Set<Event> events2 = toEventSet(group2, allEvents);

        this.bes.getConflictSet().addConflicts(events1, events2);
    }

    private Set<Event> toEventSet(Collection<?> group, Set<Event> allEvents) {
        Set<Event> events = new HashSet<>();
        for (Object o : group) {
            Event e;
            if (o instanceof Event) {
                e = (Event) o;
            } else if (o instanceof String) {
                e = new Event((String) o);
            } else {
                throw new IllegalArgumentException(
                        "Conflict collections must contain only Event or String elements.");
            }
            Preconditions.checkArgument(allEvents.contains(e),
                    "All events of a conflict should belong to the bundle event structure!");
            events.add(e);
        }
        return events;
    }

    public void addConflicts(ConflictSet set) {
        Set<Event> allEvents = new HashSet<>(this.bes.getAllEvents());
        Preconditions.checkArgument(allEvents.containsAll(set.getAllEvents()),
                "All events of a conflict should belong to the bundle event structure!");

        this.bes.getConflictSet().addConflicts(set);
    }

    public BundleEventStructure build() {
        return this.bes;
    }

    public void validate() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
