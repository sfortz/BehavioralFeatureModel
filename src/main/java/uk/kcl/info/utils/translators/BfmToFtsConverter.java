package uk.kcl.info.utils.translators;

import be.vibes.fexpression.FExpression;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.FeaturedTransitionSystemFactory;
import com.google.common.collect.BiMap;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.Event;

import java.util.*;

import static uk.kcl.info.utils.translators.TranslationUtils.*;

public class BfmToFtsConverter {

    private final BehavioralFeatureModel bfm;
    private final TreeMap<Integer, Set<Set<Event>>> configurations;
    private final BiMap<Set<Event>, String> configToStateMap;

    public BfmToFtsConverter(BehavioralFeatureModel bfm) {
        this.bfm = Objects.requireNonNull(bfm);
        this.configurations = bfm.getAllConfigurations();
        this.configToStateMap = indexConfigurationsAsStates(configurations.values());
    }

    public FeaturedTransitionSystem convert() {
        FeaturedTransitionSystemFactory factory = new FeaturedTransitionSystemFactory(INITIAL_STATE);
        addActions(factory);
        addStates(factory);
        addTransitions(factory);
        return factory.build();
    }

    private void addActions(FeaturedTransitionSystemFactory factory) {
        for (Event ev : bfm.getAllEvents()) {
            factory.addAction(ev.getName());
        }
    }

    private void addStates(FeaturedTransitionSystemFactory factory) {
        factory.addStates(configToStateMap.values().toArray(new String[0]));
    }

    private void addTransitions(FeaturedTransitionSystemFactory factory) {
        for (Map.Entry<Integer, Set<Set<Event>>> entry : configurations.entrySet()) {
            int size = entry.getKey();
            Set<Set<Event>> currentConfigs = entry.getValue();
            Set<Set<Event>> nextConfigs = configurations.get(size + 1);

            if (nextConfigs == null) continue;

            for (Set<Event> c1 : currentConfigs) {
                String s1 = configToStateMap.get(c1);
                for (Set<Event> c2 : nextConfigs) {
                    if (isSingleStepSuccessor(c1, c2)) {
                        addTransitionIfValid(factory, c1, c2, s1);
                    }
                }
            }
        }
    }

    private void addTransitionIfValid(FeaturedTransitionSystemFactory factory, Set<Event> sourceConfig, Set<Event> targetConfig, String source) {
        Event e = getSingleDifference(sourceConfig, targetConfig);
        if (e == null) return;

        String target = configToStateMap.get(targetConfig);
        FExpression fexpr = bfm.getFExpression(sourceConfig)
                .and(bfm.getFExpression(targetConfig))
                .applySimplification()
                .toCnf();

        if (!fexpr.isFalse()) {
            factory.addTransition(source, e.getName(), fexpr, target);
        }
    }
}
