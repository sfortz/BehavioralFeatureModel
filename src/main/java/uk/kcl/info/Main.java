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

package uk.kcl.info;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.solver.io.xml.XmlSavers;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;
import uk.kcl.info.bfm.compositions.BFMParallelComposer;
import uk.kcl.info.bfm.compositions.FTSParallelComposer;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.bfm.translators.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String RESOURCE_DIR = "src/main/resources/";

    private static final String FTS_DIR = RESOURCE_DIR + "fts/";
    private static final String TS_DIR = RESOURCE_DIR + "ts/";
    private static final String BFM_DIR = RESOURCE_DIR + "bfm/";
    private static final String FES_DIR = RESOURCE_DIR + "fes/";
    private static final String BES_DIR = RESOURCE_DIR + "bes/";
    private static final String FM_DIR = RESOURCE_DIR + "fm/xml/";

    private static final String FM_OUTPUT_DIR  = FM_DIR  + "output/";
    private static final String FTS_OUTPUT_DIR = FTS_DIR + "output/";
    private static final String TS_OUTPUT_DIR  = TS_DIR  + "output/";
    private static final String BFM_OUTPUT_DIR = BFM_DIR + "output/";
    private static final String FES_OUTPUT_DIR = FES_DIR + "output/";
    private static final String BES_OUTPUT_DIR = BES_DIR + "output/";

    public static void main(String[] args) throws Exception {

        LOG.info("convertBesToTs");
        convertBesToTs("robot");

        LOG.info("convertFesToFts");
        convertFesToFts("robot", "robot");

        LOG.info("convertBfmToFm");
        convertBfmToFm("robot");
        LOG.info("convertBfmToFts");
        convertBfmToFts("robot");

        LOG.info("convertTsToBes");
        convertTsToBes("robot");
        convertTsToBes("parallel");

        LOG.info("convertFtsToFes");
        convertFtsToFes("robot", "robot");

        LOG.info("convertFtsToBfm");
        for (Map.Entry<String, String> entry : getSystems().entrySet()) {
            convertFtsToBfm(entry.getValue(), entry.getKey());
        }

        List<String> svmSystems = List.of("coffee","soup","soda");
        List<String> minePumpSystems = List.of("controller_state", "controller", "methane", "pump", "water");

        generateCombinations("/vm/", svmSystems,0, new ArrayList<>());
        generateCombinations("/minepump/", minePumpSystems,0, new ArrayList<>());
    }

    private static final Set<Set<String>> FORBIDDEN_COMBINATIONS = Set.of(
            Set.of("controller", "controller_state")
    );

    private static boolean isValidCombination(List<String> combo) {
        Set<String> comboSet = new HashSet<>(combo);

        for (Set<String> forbidden : FORBIDDEN_COMBINATIONS) {
            if (comboSet.containsAll(forbidden)) {
                return false;
            }
        }
        return true;
    }

    private static void generateCombinations(String sub_dir, List<String> systems, int start, List<String> current) throws Exception {

        if (current.size() >= 2 && isValidCombination(current)) {
            runEvaluation(sub_dir, new ArrayList<>(current));
        }

        for (int i = start; i < systems.size(); i++) {
            current.add(systems.get(i));
            generateCombinations(sub_dir, systems, i + 1, current);
            current.removeLast();
        }
    }

    public static void runEvaluation(String sub_dir, List<String> systems) throws IOException, TransitionSystemDefinitionException {

        List<FeaturedTransitionSystem> ftsList = new ArrayList<>();
        List<BehavioralFeatureModel> bfmList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for(String system: systems){
            sb.append(system).append("_");
            ftsList.add(loadFts(sub_dir + system));
            bfmList.add(XmlLoaderUtility.loadBehavioralFeatureModel(new File(BFM_OUTPUT_DIR + sub_dir +  system + ".bfm")));
        }

        String systemName = sb.toString();
        LOG.info("************ Processing system: {} ************", systemName);

        // Compose
        evaluateComposition(systemName, sub_dir, ftsList, bfmList, true);
        evaluateComposition(systemName, sub_dir, ftsList, bfmList, false);
    }

    private static void evaluateComposition(String sub_dir, String systemName, List<FeaturedTransitionSystem> ftsList, List<BehavioralFeatureModel> bfmList, boolean sync) throws TransitionSystemDefinitionException {

        Iterator<FeaturedTransitionSystem> ftsQueue = ftsList.iterator();
        Iterator<BehavioralFeatureModel> bfmQueue = bfmList.iterator();
        FTSParallelComposer ftsComposer = new FTSParallelComposer();
        FeaturedTransitionSystem ftsResult = ftsQueue.next();

        while (ftsQueue.hasNext()) {
            ftsResult = ftsComposer.compose(ftsResult, ftsQueue.next(), sync);
        }

        BFMParallelComposer bfmComposer = new BFMParallelComposer();
        BehavioralFeatureModel bfmResult = bfmQueue.next();

        while (ftsQueue.hasNext()) {
            bfmResult = bfmComposer.compose(bfmResult, bfmQueue.next(), sync);
        }

        logSummary(ftsResult, bfmResult);

        // Save output
        String ftsOutputPath = FTS_OUTPUT_DIR + sub_dir + systemName + sync + ".fts";
        ensureParentDirExists(ftsOutputPath);
        XmlSaverUtility.save(ftsResult, ftsOutputPath);
        String bfmOutputPath = BFM_OUTPUT_DIR + sub_dir + systemName + sync + ".bfm";
        ensureParentDirExists(bfmOutputPath);
        XmlSaverUtility.save(bfmResult, bfmOutputPath);
    }

    public static void convertBfmToFm(String system) {
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(new File(BFM_DIR + system + ".bfm"));
        String outputPath = FM_OUTPUT_DIR + system + ".xml";

        convertAndSave(
                bfm, new BfmToFmConverter(bfm),
                XmlSavers::save,
                outputPath, system
        );
    }

    public static void convertBesToTs(String system) throws BundleEventStructureDefinitionException {
        BundleEventStructure bes = XmlLoaderUtility.loadBundleEventStructure(new File(BES_DIR + system + ".bes"));
        String outputPath = TS_OUTPUT_DIR + system + "_from_bes.ts";

        convertAndSave(
                bes,
                new BesToTsConverter(bes),
                (output, path) -> {
                    try {
                        XmlSaverUtility.save(output, path);
                    } catch (TransitionSystemDefinitionException e) {
                        throw new RuntimeException("Failed to save TS", e);                    }
                },
                outputPath,
                system
        );
    }

    public static void convertFesToFts(String fmName, String system) throws BundleEventStructureDefinitionException {
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(new File(FM_DIR + fmName + ".xml"));
        FeaturedEventStructure<?> fes = XmlLoaderUtility.loadFeaturedEventStructure(new File(FES_DIR + system + ".fes"), fm);
        String outputPath = FTS_OUTPUT_DIR + system + "_from_fes.fts";

        convertAndSave(
                fes,
                new FesToFtsConverter(fes),
                (output, path) -> {
                    try {
                        XmlSaverUtility.save(output, path);
                    } catch (TransitionSystemDefinitionException e) {
                        throw new RuntimeException("Failed to save FTS", e);
                    }
                },
                outputPath,
                system
        );
    }

    public static void convertBfmToFts(String system) {
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(new File(BFM_DIR + system + ".bfm"));
        String outputPath = FTS_OUTPUT_DIR + system + "_from_bfm.fts";

        convertAndSave(
                bfm, new BfmToFtsConverter(bfm),
                (output, path) -> {
                    try {
                        XmlSaverUtility.save(output, path);
                    } catch (TransitionSystemDefinitionException e) {
                        throw new RuntimeException("Failed to save FTS", e);
                    }
                },
                outputPath, system
        );
    }

    public static void convertTsToBes(String system) throws TransitionSystemDefinitionException {
        TransitionSystem ts = XmlLoaderUtility.loadTransitionSystem(TS_DIR + system + ".ts");
        String outputPath = BES_OUTPUT_DIR + system + ".bes";

        convertAndSave(
                ts,
                new TsToBesConverter(ts),
                (output, path) -> {
                    try {
                        XmlSaverUtility.save(output, path);
                    } catch (BundleEventStructureDefinitionException e) {
                        throw new RuntimeException("Failed to save BES", e);
                    }
                },
                outputPath,
                system
        );
    }

    public static void convertFtsToFes(String fmName, String system) throws IOException, TransitionSystemDefinitionException {
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(new File(FM_DIR + fmName + ".xml"));
        FeaturedTransitionSystem fts = loadFts(system);
        String outputPath = FES_OUTPUT_DIR + system + ".fes";

        convertAndSave(
                fts, new FtsToFesConverter(fm, fts),
                (output, path) -> {
                    try {
                        XmlSaverUtility.save(output, path);
                    } catch (BundleEventStructureDefinitionException e) {
                        throw new RuntimeException("Failed to save FES", e);
                    }
                }, outputPath, system
        );
    }

    public static void convertFtsToBfm(String fmName, String system) throws IOException, TransitionSystemDefinitionException {
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(new File(FM_DIR + fmName + ".xml"));
        FeaturedTransitionSystem fts = loadFts(system);
        String outputPath = BFM_OUTPUT_DIR + system + ".bfm";

        convertAndSave(fts, new FtsToBfmConverter<>(fm, fts), XmlSaverUtility::save, outputPath, system);
    }

    public static <In, Out> void convertAndSave(In input, ModelConverter<In, Out> converter, BiConsumer<Out, String> saver, String outputPath, String systemName) {
        LOG.info("************ Processing system: {} ************", systemName);

        // Convert
        long startTime = System.nanoTime();
        Out output = converter.convert();
        double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;

        // Log summary
        logSummary(input, output, durationMs);

        // Save output
        ensureParentDirExists(outputPath);
        saver.accept(output, outputPath);
    }

    private static FeaturedTransitionSystem loadFts(String ftsName) throws IOException, TransitionSystemDefinitionException {
        File ftsFile = new File(FTS_DIR + ftsName + ".fts");
        File dotFile = new File(FTS_DIR + ftsName + ".dot");

        if (ftsFile.exists()) {
            return XmlLoaderUtility.loadFeaturedTransitionSystem(ftsFile);
        } else if (dotFile.exists()) {
            return FeaturedTransitionSystemDotHandler.parseDotFile(dotFile.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Neither .fts nor .dot file found for " + ftsName);
        }
    }

    private static <In, Out> void logSummary(In input, Out output) {
        logModelSize(input);
        logModelSize(output);
        LOG.info("\n");
    }
    
    private static <In, Out> void logSummary(In input, Out output, double executionTime) {
        logModelSize(input);
        logModelSize(output);
        LOG.info("Conversion Time: {} ms\n", executionTime);
    }

    private static <ModelType> void logModelSize(ModelType model) {
        switch (model) {
            case BehavioralFeatureModel bfm ->
                    logBesStructure("BFM", bfm.getEventsCount(), bfm.getConflictsCount(), bfm.getMaxConflictSize(), bfm.getCausalitiesCount());
            case FeaturedEventStructure<?> fes ->
                    logBesStructure("FES", fes.getEventsCount(), fes.getConflictsCount(), fes.getMaxConflictSize(), fes.getCausalitiesCount());
            case BundleEventStructure bes ->
                    logBesStructure("BES", bes.getEventsCount(), bes.getConflictsCount(), bes.getMaxConflictSize(), bes.getCausalitiesCount());
            case FeaturedTransitionSystem fts ->
                    logTsStructure("FTS", fts.getStatesCount(), fts.getTransitionsCount()); //fts.getActionsCount(),
            case TransitionSystem ts ->
                    logTsStructure("TS", ts.getStatesCount(), ts.getTransitionsCount()); // ts.getActionsCount(),
            case FeatureModel<?> fm -> {
                LOG.info("[{}] - Features: {}, Constraints: {}",
                        "FM", fm.getFeatures().size(), fm.getConstraints().size());
            }
            case null -> throw new IllegalArgumentException("Model is null.");
            default -> throw new IllegalArgumentException("Unsupported model type: " + model.getClass().getName());
        }
    }

    private static void logTsStructure(String label, int states, int transitions) {
        int total = states + transitions;
        LOG.info("[{}] - States: {}, Transitions: {}, Total: {}",
                label, states, transitions, total);
    }

    private static void logBesStructure(String label, int events, int conflicts, int maxConflictSize, int causalities) {
        int total = events + conflicts + causalities;
        LOG.info("[{}] - Events: {}, Conflicts: {}, Max Conflict Size: {}, Causalities: {}, Total: {}",
                label, events, conflicts, maxConflictSize, causalities, total);
    }

    public static Map<String, String> getSystems() {
        Map<String, String> systems = new LinkedHashMap<>();

        systems.put("cpterminal", "cpterminal");
        systems.put("robot", "robot");
        systems.put("/vm/coffee", "coffee");
        systems.put("/vm/soup", "soup");
        systems.put("/vm/soda", "soda");

        String minepumpPath = "/minepump/";

        File minepumpDir = new File(FTS_DIR + minepumpPath);
        File[] ftsFiles = minepumpDir.listFiles((d, name) -> name.endsWith(".fts"));

        if (ftsFiles == null) {
            String msg = "Directory not found or IO error: " + minepumpDir;
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }

        for (File file : ftsFiles) {
            String filename = file.getName().replaceFirst("[.][^.]+$", ""); // remove extension
            systems.put(minepumpPath + filename, "minepump");
        }

        return systems;
    }

    private static void ensureDirExists(String path) {
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + path);
        }
    }

    private static void ensureParentDirExists(String filePath) {
        File parentDir = new File(filePath).getParentFile();
        ensureDirExists(parentDir.getPath());
    }
}
