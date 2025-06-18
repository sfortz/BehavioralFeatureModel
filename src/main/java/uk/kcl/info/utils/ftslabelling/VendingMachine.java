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
        actionToFExpression.put("insert_Euro", FExpression.featureExpr("F").not().and(FExpression.featureExpr("E")));
        actionToFExpression.put("insert_Dollar", FExpression.featureExpr("F").not().and(FExpression.featureExpr("D")));
        actionToFExpression.put("take", FExpression.featureExpr("F").not().and(FExpression.featureExpr("M")));

        actionToFExpression.put("insertBev_Euro", FExpression.featureExpr("E"));
        actionToFExpression.put("insertBev_Dollar", FExpression.featureExpr("D"));
        actionToFExpression.put("cancelBev", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelBev_0", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelBev_1", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelBev_2", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelBev_3", FExpression.featureExpr("X"));
        actionToFExpression.put("sugar", FExpression.featureExpr("B"));
        actionToFExpression.put("no_sugar", FExpression.featureExpr("B"));
        actionToFExpression.put("coffee_0", FExpression.featureExpr("C"));
        actionToFExpression.put("cappuccino_0", FExpression.featureExpr("P"));
        actionToFExpression.put("cappuccino_1", FExpression.featureExpr("P"));
        actionToFExpression.put("tea_0", FExpression.featureExpr("T"));
        actionToFExpression.put("tea_1", FExpression.featureExpr("T"));
        actionToFExpression.put("teaBev_0", FExpression.featureExpr("T"));
        actionToFExpression.put("teaBev_1", FExpression.featureExpr("T"));
        actionToFExpression.put("coffee_1", FExpression.featureExpr("C"));
        actionToFExpression.put("pour_sugar_0", FExpression.featureExpr("B"));
        actionToFExpression.put("pour_sugar_1", FExpression.featureExpr("B"));
        actionToFExpression.put("pour_sugar_2", FExpression.featureExpr("B"));
        actionToFExpression.put("pour_milk_0", FExpression.featureExpr("P"));
        actionToFExpression.put("pour_coffee_0", FExpression.featureExpr("P"));
        actionToFExpression.put("pour_tea", FExpression.featureExpr("T"));
        actionToFExpression.put("pourBev_tea", FExpression.featureExpr("T"));
        actionToFExpression.put("pour_coffee_1", FExpression.featureExpr("C"));
        actionToFExpression.put("pour_coffee_2", FExpression.featureExpr("P"));
        actionToFExpression.put("pour_milk_1", FExpression.featureExpr("P"));
        actionToFExpression.put("take_cup", FExpression.featureExpr("M"));

        actionToFExpression.put("insertSoup_Euro", FExpression.featureExpr("SC").and(FExpression.featureExpr("E")));
        actionToFExpression.put("insertSoup_Dollar", FExpression.featureExpr("SC").and(FExpression.featureExpr("D")));
        actionToFExpression.put("cancelSoup_0", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoup_1", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoup_2", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoup_3", FExpression.featureExpr("X"));
        actionToFExpression.put("tomato", FExpression.featureExpr("TS"));
        actionToFExpression.put("chicken", FExpression.featureExpr("CS"));
        actionToFExpression.put("pea", FExpression.featureExpr("PS"));
        actionToFExpression.put("no_cup_0", FExpression.featureExpr("U"));
        actionToFExpression.put("cup_present_0", FExpression.featureExpr("U"));
        actionToFExpression.put("no_cup_1", FExpression.featureExpr("U"));
        actionToFExpression.put("cup_present_1", FExpression.featureExpr("U"));
        actionToFExpression.put("no_cup_2", FExpression.featureExpr("U"));
        actionToFExpression.put("cup_present_2", FExpression.featureExpr("U"));
        actionToFExpression.put("pour_tomato", FExpression.featureExpr("TS"));
        actionToFExpression.put("pour_chicken", FExpression.featureExpr("CS"));
        actionToFExpression.put("pour_pea", FExpression.featureExpr("PS"));
        actionToFExpression.put("take_soup", FExpression.featureExpr("M"));
        actionToFExpression.put("bad_luck", FExpression.featureExpr("U").not());

        actionToFExpression.put("ring", FExpression.featureExpr("R"));
        actionToFExpression.put("ringBev", FExpression.featureExpr("R"));
        actionToFExpression.put("ringSoup", FExpression.featureExpr("R"));
        actionToFExpression.put("cancel_0", FExpression.featureExpr("X"));
        actionToFExpression.put("cancel_1", FExpression.featureExpr("X"));
        actionToFExpression.put("cancel_2", FExpression.featureExpr("X"));
        actionToFExpression.put("cancel_3", FExpression.featureExpr("X"));
        actionToFExpression.put("skip", FExpression.featureExpr("U").not().and(FExpression.featureExpr("R").not()));
        actionToFExpression.put("skip_0", FExpression.featureExpr("U").not().and(FExpression.featureExpr("R").not()));
        actionToFExpression.put("skip_1", FExpression.featureExpr("U").not().and(FExpression.featureExpr("R").not()));
        actionToFExpression.put("skip_2", FExpression.featureExpr("U").not().and(FExpression.featureExpr("R").not()));
        actionToFExpression.put("skip_3", FExpression.featureExpr("U").not().and(FExpression.featureExpr("R").not()));

        actionToFExpression.put("change", FExpression.featureExpr("F").not());
        actionToFExpression.put("pay", FExpression.featureExpr("F").not());
        actionToFExpression.put("pay_Euro", FExpression.featureExpr("F").not().and(FExpression.featureExpr("E")));
        actionToFExpression.put("pay_Dollar", FExpression.featureExpr("F").not().and(FExpression.featureExpr("D")));
        actionToFExpression.put("open", FExpression.featureExpr("F").not());
        actionToFExpression.put("take_1", FExpression.featureExpr("F").not());
        actionToFExpression.put("close", FExpression.featureExpr("F").not());
        actionToFExpression.put("free", FExpression.featureExpr("F"));
        actionToFExpression.put("take_0", FExpression.featureExpr("F"));
        actionToFExpression.put("tea", FExpression.featureExpr("T"));
        actionToFExpression.put("teaSoda", FExpression.featureExpr("T"));
        actionToFExpression.put("teaSoda_0", FExpression.featureExpr("T"));
        actionToFExpression.put("teaSoda_1", FExpression.featureExpr("T"));
        actionToFExpression.put("soda", FExpression.featureExpr("SD"));
        actionToFExpression.put("serveSoda", FExpression.featureExpr("SD"));
        actionToFExpression.put("serveTea", FExpression.featureExpr("T"));
        actionToFExpression.put("return", FExpression.featureExpr("X"));
        actionToFExpression.put("cancel", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoda", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoda_0", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoda_1", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoda_2", FExpression.featureExpr("X"));
        actionToFExpression.put("cancelSoda_3", FExpression.featureExpr("X"));

        return actionToFExpression;
    }

    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException,
            TransitionSystemDefinitionException, FExpressionException, BehavioralFeatureModelDefinitionException {

        String inDirPath = "src/main/resources/fts/eval/vm/old/";
        String outDirPath = "src/main/resources/fts/eval/vm/new/";
        File dir = new File(inDirPath);

        File[] ftsFiles = dir.listFiles((d, name) -> name.endsWith(".dot"));

        if (ftsFiles == null) {
            System.err.println("Directory not found or IO error: " + inDirPath);
            return;
        }


        Map<String, String> systems = new HashMap<>();
        systems.put("coffeesoda_synchro.dot","coffeesoda");
        systems.put("coffeesoup.dot","coffeesoup");
        systems.put("sodasoup.dot","sodasoup");
        systems.put("coffeesoda.dot","coffeesoda");
        systems.put("svm.dot","svm");
        systems.put("coffeesoup_synchro.dot","coffeesoup");
        systems.put("sodasoup_synchro.dot","sodasoup");
        systems.put("svm_synchro.dot","svm");
        
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
