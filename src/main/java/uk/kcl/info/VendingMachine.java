package uk.kcl.info;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.exception.DimacsFormatException;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VendingMachine {

    public static Map<String, FExpression> getMapping(){

        Map<String, FExpression> actionToFExpression = new HashMap<>();
        actionToFExpression.put("insert_Euro", FExpression.("E"));
        actionToFExpression.put("insert_Dollar", new FExpression("D"));
        actionToFExpression.put("cancelBev", new FExpression("X"));
        actionToFExpression.put("sugar", new FExpression("B"));
        actionToFExpression.put("no_sugar", new FExpression("B"));
        actionToFExpression.put("coffee_0", new FExpression("C"));
        actionToFExpression.put("tea_0", new FExpression("T"));
        actionToFExpression.put("cappuccino_0", new FExpression("P"));
        actionToFExpression.put("cappuccino_1", new FExpression("P"));
        actionToFExpression.put("tea_1", new FExpression("T"));
        actionToFExpression.put("coffee_1", new FExpression("C"));
        actionToFExpression.put("pour_sugar_0", new FExpression("B"));
        actionToFExpression.put("pour_sugar_1", new FExpression("B"));
        actionToFExpression.put("pour_sugar_2", new FExpression("B"));
        actionToFExpression.put("pour_milk_0", new FExpression("P"));
        actionToFExpression.put("pour_coffee_0", new FExpression("P"));
        actionToFExpression.put("pour_tea", new FExpression("T"));
        actionToFExpression.put("pour_coffee_1", new FExpression("C"));
        actionToFExpression.put("pour_coffee_2", new FExpression("P"));
        actionToFExpression.put("pour_milk_1", new FExpression("P"));
        actionToFExpression.put("ring", new FExpression("R"));
        actionToFExpression.put("skip", new FExpression("not R"));
        actionToFExpression.put("take_cup", new FExpression("M"));


        Map<String, FExpression> actionToFExpression = new HashMap<>();

        actionToFExpression.put("insert_Euro", new FExpression("E"));
        actionToFExpression.put("insert_Dollar", new FExpression("D"));
        actionToFExpression.put("cancelBev", new FExpression("X"));
        actionToFExpression.put("sugar", new FExpression("B"));
        actionToFExpression.put("no_sugar", new FExpression("B"));
        actionToFExpression.put("coffee_0", new FExpression("C"));
        actionToFExpression.put("tea_0", new FExpression("T"));
        actionToFExpression.put("cappuccino_0", new FExpression("P"));
        actionToFExpression.put("cappuccino_1", new FExpression("P"));
        actionToFExpression.put("tea_1", new FExpression("T"));
        actionToFExpression.put("coffee_1", new FExpression("C"));
        actionToFExpression.put("pour_sugar_0", new FExpression("B"));
        actionToFExpression.put("pour_sugar_1", new FExpression("B"));
        actionToFExpression.put("pour_sugar_2", new FExpression("B"));
        actionToFExpression.put("pour_milk_0", new FExpression("P"));
        actionToFExpression.put("pour_coffee_0", new FExpression("P"));
        actionToFExpression.put("pour_tea", new FExpression("T"));
        actionToFExpression.put("pour_coffee_1", new FExpression("C"));
        actionToFExpression.put("pour_coffee_2", new FExpression("P"));
        actionToFExpression.put("pour_milk_1", new FExpression("P"));
        actionToFExpression.put("ring", new FExpression("R"));
        actionToFExpression.put("skip", new FExpression("not R"));
        actionToFExpression.put("take_cup", new FExpression("M"));




        return actionToFExpression;


        Map<String, FExpression> actionToFExpression = new HashMap<>();

        actionToFExpression.put("change", new FExpression("!FreeDrinks"));
        actionToFExpression.put("pay", new FExpression("!FreeDrinks"));
        actionToFExpression.put("free", new FExpression("FreeDrinks"));
        actionToFExpression.put("cancel", new FExpression("CancelPurchase"));
        actionToFExpression.put("tea", new FExpression("Tea"));
        actionToFExpression.put("soda", new FExpression("Soda"));
        actionToFExpression.put("return", new FExpression("CancelPurchase"));
        actionToFExpression.put("serveSoda", new FExpression("Soda"));
        actionToFExpression.put("serveTea", new FExpression("Tea"));
        actionToFExpression.put("open", new FExpression("!FreeDrinks"));
        actionToFExpression.put("take_0", new FExpression("FreeDrinks"));
        actionToFExpression.put("take_1", new FExpression("!FreeDrinks"));
        actionToFExpression.put("close", new FExpression("!FreeDrinks"));

    }




  0 -> 1 [ label = "insert_Euro | SC and E" ];
  0 -> 1 [ label = "insert_Dollar | SC and D" ];
  1 -> 13 [ label = "cancelSoup_0 | X" ];
  1 -> 2 [ label = "tomato | TS" ];
  1 -> 4 [ label = "chicken | CS" ];
  1 -> 6 [ label = "pea | PS" ];
  2 -> 3 [ label = "no_cup_0 | U" ];
  2 -> 8 [ label = "cup_present_0 | U" ];
  2 -> 8 [ label = "skip_0 | not U" ];
  3 -> 13 [ label = "cancelSoup_1 | X" ];
  4 -> 5 [ label = "no_cup_1 | U" ];
  4 -> 9 [ label = "cup_present_1 | U" ];
  4 -> 9 [ label = "skip_1 | not U" ];
  5 -> 13 [ label = "cancelSoup_2 | X" ];
  6 -> 7 [ label = "no_cup_2 | U" ];
  6 -> 10 [ label = "cup_present_2 | U" ];
  6 -> 10 [ label = "skip_2 | not U" ];
  7 -> 13 [ label = "cancelSoup_3 | X" ];
  8 -> 11 [ label = "pour_tomato | TS" ];
  9 -> 11 [ label = "pour_chicken | CS" ];
  10 -> 11 [ label = "pour_pea | PS" ];
  11 -> 12 [ label = "skip_3 | not R" ];
  11 -> 12 [ label = "ring | R" ];
  12 -> 13 [ label = "take_soup | M" ];
  12 -> 13 [ label = "bad_luck | not U" ];


    );

    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException,
            TransitionSystemDefinitionException, DimacsFormatException, BehavioralFeatureModelDefinitionException {

        String inDirPath = "src/main/resources/fts/eval/vm/old/";
        String outDirPath = "src/main/resources/fts/eval/vm/new/";
        File dir = new File(inDirPath);

        File[] ftsFiles = dir.listFiles((d, name) -> name.endsWith(".dot"));

        if (ftsFiles == null) {
            System.err.println("Directory not found or IO error: " + inDirPath);
            return;
        }

        for (File file : ftsFiles) {
            String system = file.getName();
            System.out.println("Processing: " + system);

            FeaturedTransitionSystem fts = FeaturedTransitionSystemDotHandler.parseDotFile(inDirPath + system);
            //FeaturedTransitionSystem fts = XmlLoaderUtility.loadFeaturedTransitionSystem(file);
            FeaturedTransitionSystem newFts = getFts(fts);

            File outFile = new File(outDirPath + system);
            try (PrintStream output = new PrintStream(new FileOutputStream(outFile))) {
                FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(newFts, output);
                printer.printDot();
                printer.flush();
            }
        }
    }

    public static FeaturedTransitionSystem getFts(FeaturedTransitionSystem fts) throws BehavioralFeatureModelDefinitionException {
        FeaturedTransitionSystemFactory factory = new FeaturedTransitionSystemFactory(fts.getInitialState().getName());

        for (Iterator<Transition> it = fts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            String act = t.getAction().getName();
            if(actionToFExpression.containsKey(act)){
                factory.addTransition(t.getSource().getName(), act, actionToFExpression.get(act), t.getTarget().getName());
            } else {
                factory.addTransition(t.getSource().getName(), act, actionToFExpression.get(act), t.getTarget().getName());
            }
        }
        return factory.build();
    }
}
