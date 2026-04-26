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

package uk.kcl.info.bfm.compositions;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import uk.kcl.info.bfm.*;
import uk.kcl.info.utils.Pair;

import java.util.*;

public class FESParallelComposer<F extends Feature<F>> extends AbstractBESParallelComposer<FeaturedEventStructure<F>> {

    private final FeatureModel<F> fm;

    public FESParallelComposer(FeatureModel<F> fm) {
        this.fm = fm;
    }

    @Override
    protected BundleEventStructureFactory createFactory() {
        return new FeaturedEventStructureFactory(fm);
    }

    private F computeLCA(F f1, F f2) {
        return fm.getLeastCommonAncestor(f1, f2);
    }

    @Override
    protected void createEvents(FeaturedEventStructure<F> bes1, FeaturedEventStructure<F> bes2, boolean sync,
                                BundleEventStructureFactory factory, List<Event> events1, List<Event> events2,
                                Set<String> actions1, Set<String> actions2, Map<String, List<Event>> index2,
                                Map<Pair<Event>, String> eventMap) {

        FeaturedEventStructureFactory f = (FeaturedEventStructureFactory) factory;

        // 1. left-only
        for (Event e : events1) {
            if (!sync || !actions2.contains(e.getAction())) {
                String name = e.getName() + "||" + STAR;
                f.addEvent(name, e.getAction(), bes1.getFeature(e), bes1.getFExpression(e));
                eventMap.put(new Pair<>(e, null), name);
            }
        }

        // 2. right-only
        for (Event f2 : events2) {
            if (!sync || !actions1.contains(f2.getAction())) {
                String name = STAR + "||" + f2.getName();
                f.addEvent(name, f2.getAction(), bes2.getFeature(f2), bes2.getFExpression(f2));
                eventMap.put(new Pair<>(null, f2), name);
            }
        }

        // 3. sync
        if (sync) {
            for (Event e : events1) {
                List<Event> matches = index2.get(e.getAction());
                if (matches == null) continue;

                for (Event f2 : matches) {
                    String name = e.getName() + "||" + f2.getName();
                    Feature<?> lca = computeLCA(bes1.getFeature(e), bes2.getFeature(f2));
                    FExpression expr = bes1.getFExpression(e).and(bes2.getFExpression(f2)).applySimplification();
                    f.addEvent(name, e.getAction(), lca, expr);
                    eventMap.put(new Pair<>(e, f2), name);
                }
            }
        }
    }
}