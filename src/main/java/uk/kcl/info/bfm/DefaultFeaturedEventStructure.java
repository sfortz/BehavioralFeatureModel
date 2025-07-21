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
import be.vibes.fexpression.FExpressionVisitorWithReturn;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.ConfigurationSet;
import be.vibes.fexpression.exception.FExpressionException;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import com.google.common.base.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultFeaturedEventStructure<F extends Feature<F>>  extends DefaultBundleEventStructure implements FeaturedEventStructure<F>{

    private final Map<Event, F> features = new HashMap<>();

    private final Map<Event, FExpression> eventFexpressions = new HashMap<>();

    private final FeatureModel<F> fm;

    private Map<Set<Event>, FExpression> configFexpressions;

    public DefaultFeaturedEventStructure(FeatureModel<F> fm) {
        super();
        this.fm = fm;
    }

    public FeatureModel<F> getFm() {
        return fm;
    }

    @Override
    public F getFeature(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        return this.features.get(event);
    }

    @Override
    public FExpression getFExpression(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        FExpression fexpr = this.eventFexpressions.get(event);
        if (fexpr == null) {
            fexpr = FExpression.trueValue();
        }

        return fexpr;
    }

    @Override
    public FExpression getFExpression(Set<Event> config) {
        return this.configFexpressions.get(config);
    }

    private F getFeatureFromFM(F feature){
        return this.fm.getFeature(feature.getFeatureName());
    }

    private class FexpFromFMBuilder implements FExpressionVisitorWithReturn<FExpression> {

        private FExpression fexp;

        public FexpFromFMBuilder() {}

        @Override
        public FExpression constant(boolean val) {
            if (val) {
                return FExpression.trueValue();
            } else {
                return FExpression.falseValue();
            }
        }

        @Override
        public FExpression feature(Feature<?> feature) {
            return new FExpression(getFeatureFromFM((F) feature));
        }

        @Override
        public FExpression not(FExpression expr) {
            try {
                FExpression operand = expr.accept(this);
                return operand.not();
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

        @Override
        public FExpression and(List<FExpression> operands) {
            try {
                FExpression conj = FExpression.trueValue();
                for (FExpression e : operands) {
                    conj = conj.and(e.accept(this));
                }
                return conj;
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

        @Override
        public FExpression or(List<FExpression> operands) {
            try {
                FExpression disj = FExpression.falseValue();
                for (FExpression e : operands) {
                    disj = disj.or(e.accept(this));
                }
                return disj;
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

    }

    private FExpression getFexpFromFM(FExpression fexp) {
        try {
            return fexp.accept(new FexpFromFMBuilder());
        } catch (FExpressionException e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    protected void addFeature(Event event, Feature<?> feature, FExpression fexpr) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        Preconditions.checkNotNull(feature, "Feature may not be null!");
        this.features.put(event, getFeatureFromFM((F) feature));
        this.eventFexpressions.put(event, getFexpFromFM(fexpr));
    }

    @Override
    public TreeMap<Integer, Set<Set<Event>>> getAllConfigurations() {
        TreeMap<Integer, Set<Set<Event>>> configurationsBySize = new TreeMap<>();
        this.configFexpressions = new HashMap<>();
        this.configFexpressions.put(new HashSet<>(), FExpression.trueValue());
        try {
            buildProductConfigurations(new LinkedHashSet<>(), new ArrayList<>(this.getAllEvents()), configurationsBySize);
            this.fm.resetSolver();
        } catch (ConstraintSolvingException e) {
            throw new IllegalStateException("Error solving constraints: " + e.getMessage(), e.getCause());
        }
        return configurationsBySize;
    }

    private void buildProductConfigurations(Set<Event> currentConfig, List<Event> remainingEvents, TreeMap<Integer, Set<Set<Event>>> configurationsBySize) throws ConstraintSolvingException {

        // Store a copy of the current configuration
        Set<Event> configSet = new HashSet<>(currentConfig);
        // Add to TreeMap based on its size
        configurationsBySize.computeIfAbsent(configSet.size(), k -> new HashSet<>()).add(configSet);

        // Create a copy of remaining events to avoid concurrent modification
        List<Event> remainingEventsList = new ArrayList<>(remainingEvents);
        for (Event e : remainingEventsList) {
            if (isConflictFree(e, currentConfig)) {
                List<FExpression> products = getValidProducts(e, currentConfig); // The products containing e parent feature,  satisfying the fexpr associated to e AND the fexpr associated to currentConfig
                for (FExpression productFExp : products) {
                    if (respectsCausality(e, currentConfig, productFExp)) {
                        // If both conditions are satisfied, add the event to the current configuration.
                        currentConfig.add(e);
                        // Remove event 'e' from remaining events to prevent re-selection in this configuration.
                        remainingEvents.remove(e);
                        // Concatenate FExpression
                        this.configFexpressions.merge(new HashSet<>(currentConfig), productFExp, (oldValue, newValue) -> oldValue.or(newValue).applySimplification().toCnf());
                        // Recursively build configurations with the updated current configuration and remaining events.
                        buildProductConfigurations(currentConfig, remainingEvents, configurationsBySize);
                        // Backtrack: Remove event 'e' from the current configuration to explore other possible configurations.
                        currentConfig.remove(e);
                        // Add event 'e' back to the remaining events for further exploration.
                        remainingEvents.add(e);
                    }
                }
            }
        }
    }

    protected boolean respectsCausality(Event e, Set<Event> config, FExpression productFexpr) {
        Set<Set<Event>> causes = this.getAllBundles(e); // All X such as X ↦ e

        for (Set<Event> bundle : causes) {

            // Restrict bundle to only events whose features are in the product
            Set<Event> restrictedBundle = bundle.stream()
                    .filter(event -> {
                        FExpression fexpr = this.getFExpression(event).and(productFexpr);
                        return !fexpr.applySimplification().isFalse();
                    })
                    .collect(Collectors.toSet());

            // If the intersection is empty, causality is not respected
            if (!restrictedBundle.isEmpty() & Collections.disjoint(config, restrictedBundle)) { // X inter {𝑒1, . . . , 𝑒𝑖−1} = ∅
                return false;
            }
        }
        return true;
    }

    private List<FExpression> getValidProducts(Event event, Set<Event> config) {

        Collection<F> allFeatures = this.fm.getFeatures();
        FExpression configFexpr = this.configFexpressions.get(config);

        FExpression constraint = this.getFExpression(event).and(configFexpr);
        ConfigurationSet allProducts = new ConfigurationSet(this.fm, constraint);

        List<FExpression> allFExps = allProducts.stream().map(product -> {
            // Construct the product feature expression
            FExpression productFExp = FExpression.trueValue();

            List<F> featuresTMP = new ArrayList<>();
            F f1 = null;
            for (Feature<?> f : product) { //TODO: To remove once Feature.hashcode() is debugged
                for(F f2: this.fm.getFeatures()){
                    if (f.getFeatureName().equals(f2.getFeatureName())){
                       f1 = f2;
                    }
                }
                assert (f1 != null);
                featuresTMP.add(f1);
            }

            for (F f : allFeatures) {
                FExpression fFexpr= new FExpression(f);
                if(featuresTMP.contains(f)){
                //if(product.isSelected(f)){
                    productFExp.andWith(fFexpr);
                } else {
                    productFExp.andWith(fFexpr.not());
                }
            }
            return productFExp.applySimplification();
        }).toList();

        return allFExps;
    }
}
