package uk.kcl.info.utils.translators;

import be.vibes.ts.*;
import com.google.common.collect.BiMap;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.Event;

import java.util.*;

import static uk.kcl.info.utils.translators.TranslationUtils.*;

public class BesToTsConverter {

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
        for (Event event : bes.getAllEvents()) {
            factory.addAction(event.getName());
        }
    }

    private void addStates(TransitionSystemFactory factory) {
        for (String state : configToStateMap.values()) {
            factory.addState(state);
        }
    }

    private void addTransitions(TransitionSystemFactory factory) {
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
        }
    }

    public BiMap<Set<Event>, String> getConfigurationStateMap() {
        return configToStateMap;
    }
}
