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
import java.util.HashMap;
import java.util.Map;
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
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.utils.translators.*;

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

    public static void main(String[] args) throws IOException, TransitionSystemDefinitionException, BehavioralFeatureModelDefinitionException, BundleEventStructureDefinitionException {

        LOG.info("convertBesToTs");
        convertBesToTs("robot");
        //convertBesToTs("robot-linear");

        LOG.info("convertFesToFts");
        convertFesToFts("robot", "robot");
        //convertFesToFts("robot", "robot-linear");

        LOG.info("convertBfmToFm");
        convertBfmToFm("robot");
        LOG.info("convertBfmToFts");
        convertBfmToFts("robot");
        //convertBfmToFts("robot-linear");

        LOG.info("convertTsToBes");
        convertTsToBes("robot-linear");
        convertTsToBes("parallel");

        LOG.info("convertFtsToFes");
        convertFtsToFes("robot", "robot-linear");

        LOG.info("convertFtsToBfm");
        for (Map.Entry<String, String> entry : getSystems().entrySet()) {
            convertFtsToBfm(entry.getValue(), entry.getKey());
        }
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
                    logTsStructure("FTS", fts.getActionsCount(), fts.getStatesCount(), fts.getTransitionsCount());
            case TransitionSystem ts ->
                    logTsStructure("TS", ts.getActionsCount(), ts.getStatesCount(), ts.getTransitionsCount());
            case FeatureModel<?> fm -> {
                LOG.info("[{}] - Features: {}, Constraints: {}",
                        "FM", fm.getFeatures().size(), fm.getConstraints().size());
            }
            case null -> throw new IllegalArgumentException("Model is null.");
            default -> throw new IllegalArgumentException("Unsupported model type: " + model.getClass().getName());
        }
    }

    private static void logTsStructure(String label, int actions, int states, int transitions) {
        int total = actions + states + transitions;
        LOG.info("[{}] - Actions: {}, States: {}, Transitions: {}, Total: {}",
                label, actions, states, transitions, total);
    }

    private static void logBesStructure(String label, int events, int conflicts, int maxConflictSize, int causalities) {
        int total = events + conflicts + causalities;
        LOG.info("[{}] - Events: {}, Conflicts: {}, Max Conflict Size: {}, Causalities: {}, Total: {}",
                label, events, conflicts, maxConflictSize, causalities, total);
    }

    public static Map<String, String> getSystems() {
        Map<String, String> systems = new HashMap<>();

        systems.put("cpterminal", "cpterminal");
        systems.put("robot-linear", "robot");
        systems.put("/vm/coffee", "coffee");
        systems.put("/vm/soup", "soup");
        systems.put("/vm/soda", "soda");
        systems.put("/vm/coffeesoda_synchro", "coffeesoda");
        systems.put("/vm/coffeesoup_synchro", "coffeesoup");
        systems.put("/vm/sodasoup_synchro", "sodasoup");
        systems.put("/vm/coffeesoup", "coffeesoup");
        systems.put("/vm/sodasoup", "sodasoup");
        systems.put("/vm/coffeesoda", "coffeesoda");
        systems.put("/vm/svm_synchro", "svm");
        systems.put("/vm/svm", "svm");

        String minepumpPath = "minepump/";
        File minepumpDir = new File(FTS_DIR + minepumpPath);
        File[] ftsFiles = minepumpDir.listFiles((d, name) -> name.endsWith(".dot"));

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
