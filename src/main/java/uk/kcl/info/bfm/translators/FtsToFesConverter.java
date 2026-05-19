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

package uk.kcl.info.bfm.translators;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.ts.FeaturedTransitionSystem;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

public class FtsToFesConverter<F extends Feature<F>> extends AbstractFtsConverter<F, FeaturedEventStructure<?>> {

    private FeaturedEventStructureFactory factory;

    public FtsToFesConverter(FeatureModel<F> fm, FeaturedTransitionSystem fts) {
        super(LoggerFactory.getLogger(FtsToFesConverter.class), fm, fts);
    }

    @Override
    protected void initializeFactory() {
        this.factory = new FeaturedEventStructureFactory(fm);
    }

    @Override
    protected void addEventToFactory(Event event, FExpression fexpr) {
        factory.addEvent(event.getName(), event.getAction(), fm.getRootFeature(), fexpr);
    }

    @Override
    protected void addConflictToFactory(Event e1, Event e2) {
        factory.addConflict(e1, e2);
    }

    @Override
    protected void addCausalityToFactory(CausalityRelation relation) {
        factory.addCausality(relation);
    }

    @Override
    protected FeaturedEventStructure<?> buildResult() {
        return factory.build();
    }
}
