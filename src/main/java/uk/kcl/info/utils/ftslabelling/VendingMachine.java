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

package uk.kcl.info.utils.ftslabelling;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.FExpressionVisitorWithReturn;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.exception.FExpressionException;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.FeaturedTransitionSystemFactory;
import be.vibes.ts.Transition;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotHandler;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class VendingMachine {

    public static Map<String, FExpression> getMapping(){

        Map<String, FExpression> actionToFExpression = new HashMap<>();

        actionToFExpression.put("insert_euro_bev", FExpression.featureExpr("Euro"));
        actionToFExpression.put("insert_dollar_bev", FExpression.featureExpr("Dollar"));
        actionToFExpression.put("insert_euro_soup",  FExpression.featureExpr("Euro"));
        actionToFExpression.put("insert_dollar_soup", FExpression.featureExpr("Dollar"));
        actionToFExpression.put("insert_euro",  FExpression.featureExpr("Euro"));
        actionToFExpression.put("insert_dollar", FExpression.featureExpr("Dollar"));

        actionToFExpression.put("bad_luck", FExpression.featureExpr("Cup").not());
        actionToFExpression.put("no_cup", FExpression.featureExpr("Cup"));
        actionToFExpression.put("cup_present", FExpression.featureExpr("Cup"));

        actionToFExpression.put("pour_milk", FExpression.featureExpr("Cappuccino"));
        actionToFExpression.put("pour_coffee", FExpression.featureExpr("Coffee").or(FExpression.featureExpr("Cappuccino")));
        actionToFExpression.put("cappuccino", FExpression.featureExpr("Cappuccino"));
        actionToFExpression.put("coffee", FExpression.featureExpr("Coffee"));

        actionToFExpression.put("no_sugar", FExpression.featureExpr("Beverages"));
        actionToFExpression.put("pour_sugar", FExpression.featureExpr("Beverages"));
        actionToFExpression.put("sugar", FExpression.featureExpr("Beverages"));

        actionToFExpression.put("tea_bev", FExpression.featureExpr("Tea"));
        actionToFExpression.put("pour_tea_bev", FExpression.featureExpr("Tea"));
        actionToFExpression.put("pour_tea", FExpression.featureExpr("Tea"));
        actionToFExpression.put("tea", FExpression.featureExpr("Tea"));
        actionToFExpression.put("pour_tea_soda", FExpression.featureExpr("Tea"));
        actionToFExpression.put("tea_soda", FExpression.featureExpr("Tea"));

        actionToFExpression.put("soda", FExpression.featureExpr("Soda"));
        actionToFExpression.put("pour_soda", FExpression.featureExpr("Soda"));

        actionToFExpression.put("tomato", FExpression.featureExpr("TomatoSoup"));
        actionToFExpression.put("chicken", FExpression.featureExpr("ChickenSoup"));
        actionToFExpression.put("pea", FExpression.featureExpr("PeaSoup"));
        actionToFExpression.put("pour_tomato", FExpression.featureExpr("TomatoSoup"));
        actionToFExpression.put("pour_chicken", FExpression.featureExpr("ChickenSoup"));
        actionToFExpression.put("pour_pea", FExpression.featureExpr("PeaSoup"));

        actionToFExpression.put("ring_bev", FExpression.featureExpr("Ringtone"));
        actionToFExpression.put("ring_soup", FExpression.featureExpr("Ringtone"));
        actionToFExpression.put("ring", FExpression.featureExpr("Ringtone"));

        actionToFExpression.put("take_soda", FExpression.featureExpr("VendingMachine"));
        actionToFExpression.put("take_soup", FExpression.featureExpr("VendingMachine"));
        actionToFExpression.put("take_bev", FExpression.featureExpr("VendingMachine"));
        actionToFExpression.put("take", FExpression.featureExpr("VendingMachine"));

        actionToFExpression.put("cancel_soup", FExpression.featureExpr("CancelPurchase"));
        actionToFExpression.put("cancel_soda", FExpression.featureExpr("CancelPurchase"));
        actionToFExpression.put("cancel_bev", FExpression.featureExpr("CancelPurchase"));
        actionToFExpression.put("cancel", FExpression.featureExpr("CancelPurchase"));
        actionToFExpression.put("return", FExpression.featureExpr("CancelPurchase"));

        actionToFExpression.put("pay", FExpression.featureExpr("FreeDrinks").not());
        actionToFExpression.put("free", FExpression.featureExpr("FreeDrinks"));
        actionToFExpression.put("change", FExpression.featureExpr("FreeDrinks").not());
        actionToFExpression.put("close", FExpression.featureExpr("FreeDrinks").not());
        actionToFExpression.put("open", FExpression.featureExpr("FreeDrinks").not());

        actionToFExpression.put("skip_soup", FExpression.featureExpr("Cup").not().or(FExpression.featureExpr("Ringtone").not()));
        actionToFExpression.put("skip_bev", FExpression.featureExpr("Ringtone").not());
        actionToFExpression.put("skip", FExpression.featureExpr("Ringtone").not());

        return actionToFExpression;
    }

    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException,
            TransitionSystemDefinitionException, FExpressionException, BehavioralFeatureModelDefinitionException {

        String inDirPath = "src/main/resources/fts/vm/old/";
        String outDirPath = "src/main/resources/fts/vm/new/";
        File dir = new File(inDirPath);

        File[] ftsFiles = dir.listFiles((d, name) -> name.endsWith(".dot"));

        if (ftsFiles == null) {
            System.err.println("Directory not found or IO error: " + inDirPath);
            return;
        }


        Map<String, String> systems = new HashMap<>();
        systems.put("coffeesoda_synchro.dot","coffeesoda");
        systems.put("coffeesoup_synchro.dot","coffeesoup");
        systems.put("sodasoup_synchro.dot","sodasoup");
        systems.put("svm_synchro.dot","svm");

        systems.put("coffeesoup.dot","coffeesoup");
        systems.put("sodasoup.dot","sodasoup");
        systems.put("coffeesoda.dot","coffeesoda");
        systems.put("svm.dot","svm");

        systems.put("soda.dot","soda");
        systems.put("soup.dot","soup");
        systems.put("coffee.dot","coffee");
        
        for (File file : ftsFiles) {
            String system = file.getName();
            System.out.println("Processing: " + system);
            String fmName = systems.get(system);
            FeaturedTransitionSystem fts = FeaturedTransitionSystemDotHandler.parseDotFile(inDirPath + system);
            File fmFile = new File("src/main/resources/fm/xml/" + fmName + ".xml");
            FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);
            FeaturedTransitionSystem newFts = getFts((Collection<Feature<?>>) fm.getFeatures(), fts);

            File outFile = new File(outDirPath + system);
            try (PrintStream output = new PrintStream(new FileOutputStream(outFile))) {
                FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(newFts, output);
                printer.printDot();
                printer.flush();
            }
        }
    }

    public static FeaturedTransitionSystem getFts(Collection<Feature<?>> features, FeaturedTransitionSystem fts) throws BehavioralFeatureModelDefinitionException, FExpressionException {
        FeaturedTransitionSystemFactory factory = new FeaturedTransitionSystemFactory(fts.getInitialState().getName());
        Map<String, FExpression> actionToFExpression = getMapping();

        for (Iterator<Transition> it = fts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            String act = t.getAction().getName();
            if(actionToFExpression.containsKey(act)){
                RestrictedFexp visitor = new RestrictedFexp(features);
                FExpression fexp = actionToFExpression.get(act).accept(visitor);
                factory.addTransition(t.getSource().getName(), act, fexp, t.getTarget().getName());
            } else {
                factory.addTransition(t.getSource().getName(), act, FExpression.trueValue(), t.getTarget().getName());
            }
        }
        return factory.build();
    }

    private static class RestrictedFexp implements FExpressionVisitorWithReturn<FExpression> {

        private final Set<String> allowedFeatureNames;
        private boolean inNegation = false;

        public RestrictedFexp(Collection<Feature<?>> features) {
            this.allowedFeatureNames = new HashSet<>();
            for (Feature<?> f : features) {
                allowedFeatureNames.add(f.getFeatureName());
            }
        }

        @Override
        public FExpression constant(boolean val) {
            return val ? FExpression.trueValue() : FExpression.falseValue();
        }

        @Override
        public FExpression feature(Feature<?> feature) {
            if (allowedFeatureNames.contains(feature.getFeatureName())) {
                return FExpression.featureExpr(feature.getFeatureName());
            } else if(inNegation) {
                return FExpression.falseValue();
            } else {
                return FExpression.trueValue();
            }
        }

        @Override
        public FExpression not(FExpression expr) {
            try {
                inNegation = !inNegation; // flip negation flag
                FExpression operand = expr.accept(this);
                inNegation = !inNegation; // restore state
                return operand.not();
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

        @Override
        public FExpression and(List<FExpression> operands) {
            try {
                FExpression result = FExpression.trueValue();
                for (FExpression e : operands) {
                    FExpression simplified = e.accept(this);
                    if (simplified.isTrue()) {
                        continue; // ignore true
                    }
                    if (simplified.isFalse()) {
                        return FExpression.falseValue(); // short-circuit: false dominates AND
                    }
                    result = result.and(simplified);
                }
                return result;
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }

        @Override
        public FExpression or(List<FExpression> operands) {
            try {
                FExpression result = FExpression.falseValue();
                for (FExpression e : operands) {
                    FExpression simplified = e.accept(this);
                    if (simplified.isFalse()) {
                        continue; // ignore false
                    }
                    if (simplified.isTrue()) {
                        return FExpression.trueValue(); // short-circuit: true dominates OR
                    }
                    result = result.or(simplified);
                }
                return result;
            } catch (FExpressionException ex) {
                throw new IllegalStateException("No exception should happen while using this visitor!", ex);
            }
        }
    }
}
