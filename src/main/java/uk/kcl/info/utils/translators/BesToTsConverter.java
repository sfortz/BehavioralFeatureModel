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
import com.google.common.collect.BiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.Event;

import java.util.*;

import static uk.kcl.info.utils.translators.TranslationUtils.*;

public class BesToTsConverter implements ModelConverter<BundleEventStructure, TransitionSystem> {

    private static final Logger LOG = LoggerFactory.getLogger(BesToTsConverter.class);

    private final BundleEventStructure bes;
    private final TreeMap<Integer, Set<Set<Event>>> configurations;
    private final BiMap<Set<Event>, String> configToStateMap;

    public BesToTsConverter(BundleEventStructure bes) {
        this.bes = Objects.requireNonNull(bes);;
        this.configurations = bes.getAllConfigurations();
        this.configToStateMap = indexConfigurationsAsStates(configurations.values());
    }

    public TransitionSystem convert() {
        TransitionSystemFactory factory = new TransitionSystemFactory(INITIAL_STATE);
        addActions(factory);
        addStates(factory);
        addTransitions(factory);
        return factory.build();
    }

    private void addActions(TransitionSystemFactory factory) {
        int i = 0;
        for (Event event : bes.getAllEvents()) {
            factory.addAction(event.getName());
            i++;
            LOG.trace("Events to actions: {}/{}", i, bes.getEventsCount());
        }
    }

    private void addStates(TransitionSystemFactory factory) {
        int i = 0;
        for (String state : configToStateMap.values()) {
            factory.addState(state);
            i++;
            LOG.trace("Configurations to states: {}/{}", i, configToStateMap.size());
        }
    }

    private void addTransitions(TransitionSystemFactory factory) {
        int i = 0;
        for (int size : configurations.keySet()) {
            Set<Set<Event>> currentLevel = configurations.get(size);
            Set<Set<Event>> nextLevel = configurations.get(size + 1);
            if (nextLevel == null) continue;

            for (Set<Event> sourceConfig : currentLevel) {
                for (Set<Event> targetConfig : nextLevel) {
                    if (isSingleStepSuccessor(sourceConfig, targetConfig)) {
                        Event addedEvent = getSingleDifference(sourceConfig, targetConfig);
                        if (addedEvent != null) {
                            String sourceState = configToStateMap.get(sourceConfig);
                            String targetState = configToStateMap.get(targetConfig);
                            factory.addTransition(sourceState, addedEvent.getName(), targetState);
                        }
                    }
                }
            }
            i++;
            LOG.trace("Configurations to transitions: {}/{}", i, configurations.size());
        }
    }

    public BiMap<Set<Event>, String> getConfigurationStateMap() {
        return configToStateMap;
    }
}
