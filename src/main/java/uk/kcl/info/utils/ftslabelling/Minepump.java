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
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.FeaturedTransitionSystemFactory;
import be.vibes.ts.Transition;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotHandler;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

public class Minepump {

    static Map<String, FExpression> actionToFExpression = Map.<String, FExpression>ofEntries(
            Map.entry("commandMsg", FExpression.featureExpr("Command")),
            Map.entry("highLevel", FExpression.featureExpr("High")),
            Map.entry("setMethaneStop", FExpression.featureExpr("MethaneDetect")),
            Map.entry("palarmMsg", FExpression.featureExpr("MethaneDetect")),
            Map.entry("levelMsg", FExpression.featureExpr("WaterRegulation")),
            Map.entry("stopCmd", FExpression.featureExpr("Stop")),
            Map.entry("setStop", FExpression.featureExpr("Stop")),
            Map.entry("startCmd", FExpression.featureExpr("Start")),
            Map.entry("lowLevel", FExpression.featureExpr("Low")),
            Map.entry("setLowStop", FExpression.featureExpr("Low")),
            Map.entry("isLowStop", FExpression.featureExpr("Low")),
            Map.entry("isStopped", FExpression.featureExpr("High")),
            Map.entry("pumpStart", FExpression.featureExpr("High")),
            Map.entry("setRunning", FExpression.featureExpr("High")),
            Map.entry("isNotReady", FExpression.featureExpr("High")),
            Map.entry("isMethaneStop", FExpression.featureExpr("MethaneDetect")),
            Map.entry("isReady", FExpression.featureExpr("Start").or(FExpression.featureExpr("High"))),
            Map.entry("setReady", FExpression.featureExpr("Start").or(FExpression.featureExpr("High")))   ,
            Map.entry("isRunning", FExpression.featureExpr("Stop")
                    .or(FExpression.featureExpr("Start"))
                    .or(FExpression.featureExpr("High"))
                    .or(FExpression.featureExpr("Low"))
                    .or(FExpression.featureExpr("MethaneDetect"))),
            Map.entry("isNotRunning", FExpression.featureExpr("Stop")
                    .or(FExpression.featureExpr("Start"))
                    .or(FExpression.featureExpr("Low"))
                    .or(FExpression.featureExpr("MethaneDetect"))),
            Map.entry("pumpStop", FExpression.featureExpr("Stop")
                    .or(FExpression.featureExpr("Low"))
                    .or(FExpression.featureExpr("MethaneDetect")))
    );

    public static void main(String[] args) throws IOException, BehavioralFeatureModelDefinitionException {

        String inDirPath = "src/main/resources/fts/minepump/olds/";
        String outDirPath = "src/main/resources/fts/minepump/news/";
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
            FExpression fexpr = FExpression.trueValue();
            if(actionToFExpression.containsKey(act)){
                fexpr = actionToFExpression.get(act);
            }
            factory.addTransition(t.getSource().getName(), act, fexpr, t.getTarget().getName());
        }
        return factory.build();
    }


}
