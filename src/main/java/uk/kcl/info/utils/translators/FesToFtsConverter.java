package uk.kcl.info.utils.translators;

import be.vibes.fexpression.FExpression;
import be.vibes.ts.*;
import uk.kcl.info.bfm.Event;
import uk.kcl.info.bfm.FeaturedEventStructure;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class FesToFtsConverter {

    private final FeaturedEventStructure<?> fes;
    private final BesToTsConverter besToTsConverter;
    private TransitionSystem ts;
    private FeaturedTransitionSystemFactory factory;

    public FesToFtsConverter(FeaturedEventStructure<?> fes) {
        this.fes = Objects.requireNonNull(fes);
        this.besToTsConverter = new BesToTsConverter(fes);
    }

    public FeaturedTransitionSystem convert() {
        this.ts = besToTsConverter.convert();
        this.factory = new FeaturedTransitionSystemFactory(ts.getInitialState().getName());

        addFeaturedTransitions();

        return factory.build();
    }

    private void addFeaturedTransitions() {
        for (Iterator<Transition> it = ts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            String source = t.getSource().getName();
            String action = t.getAction().getName();
            String target = t.getTarget().getName();

            Set<Event> sourceConfig = getConfiguration(source);
            Set<Event> targetConfig = getConfiguration(target);

            FExpression expr = fes.getFExpression(sourceConfig)
                    .and(fes.getFExpression(targetConfig))
                    .applySimplification()
                    .toCnf();

            if (!expr.isFalse()) {
                factory.addTransition(source, action, expr, target);
            }
        }
    }

    private Set<Event> getConfiguration(String state) {
        return besToTsConverter.getConfigurationStateMap().inverse().get(state);
    }
}
