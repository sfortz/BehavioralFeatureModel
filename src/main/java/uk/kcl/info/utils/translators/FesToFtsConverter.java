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

import be.vibes.fexpression.FExpression;
import be.vibes.ts.*;
import uk.kcl.info.bfm.Event;
import uk.kcl.info.bfm.FeaturedEventStructure;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class FesToFtsConverter implements ModelConverter<FeaturedEventStructure<?>, FeaturedTransitionSystem> {

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
