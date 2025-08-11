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

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.Configuration;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleBehavioralProduct implements BehavioralProduct {

    private static SimpleBehavioralProduct instance = null;

    public static SimpleBehavioralProduct getInstance() {
        return instance == null ? (instance = new SimpleBehavioralProduct()) : instance;
    }

    protected SimpleBehavioralProduct() {
    }

    @Override
    public BundleEventStructure project(FeaturedEventStructure<?> fes, Collection<Feature<?>> features, Configuration product) {
        BundleEventStructureFactory factory = new BundleEventStructureFactory();

        FExpression productFexpr = FExpression.trueValue();

        for(Feature<?> feature : features) {
            FExpression featureFexpr = new FExpression(feature);
            if(!product.isSelected(feature)) {
                featureFexpr.notWith();
            }
            productFexpr.andWith(featureFexpr);
        }

        List<Event> events = new ArrayList<>();
        for(Event e: fes.getAllEvents()){
            if(product.isSelected(fes.getFeature(e))){ // 𝜆(𝑒) ∈ pr
                FExpression fexpr1 = fes.getFExpression(e);
                FExpression fexpr2 = productFexpr;
                FExpression fexpr3 = fexpr1.and(fexpr2);
                fexpr3.applySimplification();
                if(!fes.getFExpression(e).and(productFexpr).applySimplification().isFalse()){ // pr |= 𝜈 (𝑒)
                    //Adding event
                    events.add(e);
                    factory.addEvent(e.getName());
                }
            }
        }

        for(Event e: events){
            //Adding conflicts involving e
            List<Event> conflictingEvents = fes.getAllConflictsOfEvent(e).stream().filter(events::contains).toList();
            if(!conflictingEvents.isEmpty()){
                factory.addConflicts(e,conflictingEvents);
            }

            //Adding causalities targeting e
            List<CausalityRelation> causalityList = new ArrayList<>();

            Iterator<CausalityRelation> it = fes.getAllCausalitiesOfEvent(e); // X → e, e ∈ E'
            while(it.hasNext()) {
                CausalityRelation causality = it.next();
                Set<Event> newBundle = causality.getBundle().stream().filter(events::contains).collect(Collectors.toSet()); // X ∩ E'
                if(!newBundle.isEmpty()){ // X ∩ E' ≠ ∅

                    // Check if any existing causality is a superset of the new bundle
                    boolean supersetPresent = causalityList.stream()
                            .anyMatch(existing -> existing.getBundle().containsAll(newBundle));

                    if (!supersetPresent) {
                        // Remove any existing causalities whose bundle is a subset of the new bundle
                        causalityList.removeIf(existing -> newBundle.containsAll(existing.getBundle())
                                && !existing.getBundle().equals(newBundle));

                        // Add the new causality
                        causalityList.add(new CausalityRelation(newBundle, e));
                    }
                }

            }
            causalityList.forEach(factory::addCausality);
        }

        return factory.build();
    }
}
