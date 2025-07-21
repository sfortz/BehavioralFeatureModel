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

import java.util.*;

public interface BundleEventStructure {

    Iterator<Event> events();

    List<Event> getAllEvents();

    Event getEvent(String var1);

    Iterator<CausalityRelation> causalities();

    ConflictSet getConflictSetCopy();

    Iterator<CausalityRelation> getAllCausalitiesOfEvent(Event event);

    CausalityRelation getCausality(Set<Event> bundle, Event event);

    Iterator<CausalityRelation> getOutgoingCausalities(Event var1);

    int getOutgoingCausalityCount(Event var1);

    Iterator<CausalityRelation> getIncomingCausalities(Event var1);

    int getIncomingCausalityCount(Event var1);

    Set<Event> getAllConflictsOfEvent(Event event);

    Set<Event> getInitialEvents();

    TreeMap<Integer, Set<Set<Event>>> getAllConfigurations();

    int getEventsCount();

    int getCausalitiesCount();

    int getConflictsCount();

    int getMaxConflictSize();

    int getTotalNumberOfConflictingEvents();

    boolean areInConflict(Event var1, Event var2);

}
