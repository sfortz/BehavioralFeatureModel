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

public class BFMParallelComposer implements Composition<BehavioralFeatureModel> {

    @Override
    public BehavioralFeatureModel compose(BehavioralFeatureModel bfm1, BehavioralFeatureModel bfm2, boolean sync) {

        /*
         * --------------------------------
         * 1. MERGE FEATURE MODELS
         * --------------------------------
         */
        FMMerger fmMerger = new FMMerger();
        FeatureModel<BehavioralFeature> mergedFM = (FeatureModel<BehavioralFeature>) fmMerger.compose(bfm1, bfm2, true);

        /*
         * --------------------------------
         * 2. COMPOSE FES
         * --------------------------------
         */
        FESParallelComposer<BehavioralFeature> fesComposer = new FESParallelComposer<>(mergedFM);
        FeaturedEventStructure<BehavioralFeature> fes = fesComposer.compose(bfm1, bfm2, sync);

        /*
         * --------------------------------
         * 3. BUILD RESULTING BFM
         * --------------------------------
         */
        BehavioralFeatureModelFactory factory = new BehavioralFeatureModelFactory(mergedFM);

        /*
         * --------------------------------
         * 4. Helper: map feature safely
         * --------------------------------
         */
        final java.util.function.Function<Feature<?>, BehavioralFeature> mapFeature =
                raw -> {
                    BehavioralFeature f = factory.getFeature(raw.getFeatureName());
                    if (f == null) {
                        throw new IllegalStateException(
                                "Unknown feature in merged FM: " + raw.getFeatureName()
                        );
                    }
                    return f;
                };

        /*
         * --------------------------------
         * 5. COPY EVENTS
         * --------------------------------
         */
        for (Event e : fes.getAllEvents()) {
            String name = e.getName();
            String action = e.getAction();
            BehavioralFeature targetFeature = mapFeature.apply(fes.getFeature(e));
            FExpression fexpr = fes.getFExpression(e);
            factory.addEvent(targetFeature, name, action, fexpr);
        }

        /*
         * --------------------------------
         * 6. COPY CONFLICTS
         * --------------------------------
         */
        for (Pair<Event> conflict : fes.getConflictSetCopy().conflictPairs()) {
            Event e1 = conflict.getLeft();
            Event e2 = conflict.getRight();
            BehavioralFeature targetFeature = mapFeature.apply(fes.getFeature(e1));
            factory.addConflict(targetFeature, e1.getName(), e2.getName());
        }

        /*
         * --------------------------------
         * 7. COPY CAUSALITIES
         * --------------------------------
         */
        for (Event target : fes.getAllEvents()) {

            BehavioralFeature targetFeature = mapFeature.apply(fes.getFeature(target));
            Iterator<CausalityRelation> it = fes.getIncomingCausalities(target);

            while (it.hasNext()) {
                CausalityRelation rel = it.next();

                Set<String> bundle = new HashSet<>();
                for (Event cause : rel.getBundle()) {
                    bundle.add(cause.getName());
                }

                factory.addCausality(targetFeature, bundle, target.getName());
            }
        }

        /*
         * --------------------------------
         * 8. FINALIZE
         * --------------------------------
         */
        factory.updateAllEventFexpr();

        return factory.build();
    }

}