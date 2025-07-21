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
import java.util.Iterator;
import java.util.Map;

public class Minepump {

    static Map<String, String> actionToFExpression = Map.<String, String>ofEntries(
            Map.entry("commandMsg_0", "Command"),
            Map.entry("palarmMsg_0", "MethaneDetect"),
            Map.entry("levelMsg_0", "WaterRegulation"),
            Map.entry("stopCmd_0", "Stop"),
            Map.entry("startCmd_0", "Start"),
            Map.entry("isNotRunning_0", "MethaneDetect"),
            Map.entry("highLevel_0", "High"),
            Map.entry("lowLevel_0", "Low"),
            Map.entry("isNotRunning_1", "Stop"),
            Map.entry("isNotRunning_2", "Start"),
            Map.entry("setMethaneStop_0", "MethaneDetect"),
            Map.entry("isStopped_0", "High"),
            Map.entry("isNotRunning_3", "Low"),
            Map.entry("setReady_0", "Start"),
            Map.entry("commandMsg_1", "Command"),
            Map.entry("palarmMsg_1", "MethaneDetect"),
            Map.entry("levelMsg_1", "WaterRegulation"),
            Map.entry("commandMsg_2", "Command"),
            Map.entry("palarmMsg_2", "MethaneDetect"),
            Map.entry("levelMsg_2", "WaterRegulation"),
            Map.entry("stopCmd_1", "Stop"),
            Map.entry("startCmd_1", "Start"),
            Map.entry("isNotRunning_4", "MethaneDetect"),
            Map.entry("highLevel_1", "High"),
            Map.entry("lowLevel_1", "Low"),
            Map.entry("stopCmd_2", "Stop"),
            Map.entry("startCmd_2", "Start"),
            Map.entry("isNotRunning_5", "MethaneDetect"),
            Map.entry("highLevel_2", "High"),
            Map.entry("lowLevel_2", "Low"),
            Map.entry("isNotRunning_6", "Stop"),
            Map.entry("isNotRunning_7", "Start"),
            Map.entry("setMethaneStop_1", "MethaneDetect"),
            Map.entry("isMethaneStop_0", "MethaneDetect"),
            Map.entry("isNotRunning_8", "Low"),
            Map.entry("isNotRunning_9", "Stop"),
            Map.entry("isNotRunning_10", "Start"),
            Map.entry("isReady_0", "Start"),
            Map.entry("setMethaneStop_2", "MethaneDetect"),
            Map.entry("isReady_1", "High"),
            Map.entry("isNotRunning_11", "Low"),
            Map.entry("setStop_0", "Stop"),
            Map.entry("setReady_1", "Start"),
            Map.entry("setStop_1", "Stop"),
            Map.entry("setReady_2", "Start"),
            Map.entry("setReady_3", "High"),
            Map.entry("setMethaneStop_3", "MethaneDetect"),
            Map.entry("isReady_2", "High"),
            Map.entry("isNotReady_0", "High"),
            Map.entry("pumpStart_0", "High"),
            Map.entry("setRunning_0", "High"),
            Map.entry("commandMsg_3", "Command"),
            Map.entry("palarmMsg_3", "MethaneDetect"),
            Map.entry("levelMsg_3", "WaterRegulation"),
            Map.entry("stopCmd_3", "Stop"),
            Map.entry("startCmd_3", "Start"),
            Map.entry("isRunning_0", "MethaneDetect"),
            Map.entry("highLevel_3", "High"),
            Map.entry("lowLevel_3", "Low"),
            Map.entry("isRunning_1", "Stop"),
            Map.entry("isRunning_2", "Start"),
            Map.entry("pumpStop_0", "MethaneDetect"),
            Map.entry("isRunning_3", "High"),
            Map.entry("isRunning_4", "Low"),
            Map.entry("pumpStop_1", "Stop"),
            Map.entry("setMethaneStop_4", "MethaneDetect"),
            Map.entry("pumpStop_2", "Low"),
            Map.entry("setStop_2", "Stop"),
            Map.entry("setLowStop_0", "Low"),
            Map.entry("commandMsg_4", "Command"),
            Map.entry("palarmMsg_4", "MethaneDetect"),
            Map.entry("levelMsg_4", "WaterRegulation"),
            Map.entry("stopCmd_4", "Stop"),
            Map.entry("startCmd_4", "Start"),
            Map.entry("isNotRunning_12", "MethaneDetect"),
            Map.entry("highLevel_4", "High"),
            Map.entry("lowLevel_4", "Low"),
            Map.entry("isNotRunning_13", "Stop"),
            Map.entry("isNotRunning_14", "Start"),
            Map.entry("isLowStop_0", "Low"),
            Map.entry("isNotRunning_15", "Low"),
            Map.entry("setReady_4", "High"),
            Map.entry("setMethaneStop_5", "MethaneDetect"),
            Map.entry("pumpStart", "High"),
            Map.entry("palarmMsg", "MethaneDetect"),
            Map.entry("commandMsg", "Command"),
            Map.entry("highLevel", "High"),
            Map.entry("lowLevel", "Low"),
            Map.entry("isLowStop", "Low"),
            Map.entry("setLowStop", "Low"),
            Map.entry("levelMsg", "WaterRegulation"),
            Map.entry("stopCmd", "Stop"),
            Map.entry("startCmd", "Start"),
            Map.entry("setStop", "Stop"),
            Map.entry("isStopped", "High"),
            Map.entry("setRunning", "High"),
            Map.entry("isNotReady", "High"),
            Map.entry("isMethaneStop", "MethaneDetect"),
            Map.entry("pumpStop_3", "MethaneDetect"),
            Map.entry("pumpStop_4", "Stop"),
            Map.entry("pumpStop_5", "Low")
    );

    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException,
            TransitionSystemDefinitionException, DimacsFormatException, BehavioralFeatureModelDefinitionException {

        String inDirPath = "src/main/resources/fts/eval/minepump/old/";
        String outDirPath = "src/main/resources/fts/eval/minepump/new/";
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
                fexpr = new FExpression(actionToFExpression.get(act));
            }
            factory.addTransition(t.getSource().getName(), act, fexpr, t.getTarget().getName());
        }
        return factory.build();
    }


}
