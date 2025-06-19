package uk.kcl.info;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotHandler;
import uk.kcl.info.bfm.*;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.utils.translators.FtsToBfmConverter;

public class Main {

    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException, TransitionSystemDefinitionException, DimacsFormatException, BehavioralFeatureModelDefinitionException {

        /* for(Map.Entry<String,String> entry: getSystems().entrySet()){
            testfts2bfm(entry.getValue(), entry.getKey());
        }
       */

        System.out.println("============================================");
        for(String key: getSystems().keySet()){
            testbfmMetrics(key);
        }

        //testftsMetrics();
    }

    public static void testfts2bfm(String fmName, String ftsName) throws IOException, TransitionSystemDefinitionException {
        System.out.println("********************************** " + ftsName+ " ******************************");

        String dirPath = "src/main/resources/";

        File fmFile = new File(dirPath + "fm/xml/" + fmName + ".xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);

        FeaturedTransitionSystem fts;

        String basePath = dirPath + "fts/eval/" + ftsName;
        File ftsFile = new File(basePath + ".fts");
        File dotFile = new File(basePath + ".dot");

        if (ftsFile.exists()) {
            fts = XmlLoaderUtility.loadFeaturedTransitionSystem(ftsFile);
        } else if (dotFile.exists()) {
            fts = FeaturedTransitionSystemDotHandler.parseDotFile(dotFile.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Neither .fts nor .dot file found for " + ftsName);
        }

        long startTime = System.nanoTime();
        FtsToBfmConverter<?> converter = new FtsToBfmConverter<>(fm, fts);
        BehavioralFeatureModel bfm = converter.convert();
        long endTime = System.nanoTime();
        long durationInNanoseconds = endTime - startTime;
        double durationInMilliseconds = durationInNanoseconds / 1_000_000.0;
        System.out.println("Execution time: " + durationInMilliseconds + " ms");


        XmlSaverUtility.save(bfm, dirPath + "bfm/eval-results/" + ftsName + ".bfm");
    }

    public static Map<String, String> getSystems() {
        Map<String, String> systems = new HashMap<>();

        systems.put("cpterminal","cpterminal");
        systems.put("robot-linear","robot");
        systems.put("coffee","coffee");
        systems.put("soup","soup");
        systems.put("soda","soda");
        systems.put("/vm/new/coffeesoda_synchro","coffeesoda");
        systems.put("/vm/new/coffeesoup_synchro","coffeesoup");
        systems.put("/vm/new/sodasoup_synchro","sodasoup");
        systems.put("/vm/new/svm_synchro","svm");
        systems.put("/vm/new/coffeesoup","coffeesoup");
        systems.put("/vm/new/sodasoup","sodasoup");
        systems.put("/vm/new/coffeesoda","coffeesoda");

        systems.put("/vm/new/svm","svm");

        String minepumpPath = "minepump/new/";
        //File minepumpDir = new File("src/main/resources/fts/eval/" + minepumpPath + "done/");
        File minepumpDir = new File("src/main/resources/fts/eval/" + minepumpPath);

        File[] ftsFiles = minepumpDir.listFiles((d, name) -> name.endsWith(".dot"));
        //File[] ftsFiles = minepumpDir.listFiles((d, name) -> name.endsWith("synchro.dot"));

        if (ftsFiles == null) {
            System.err.println("Directory not found or IO error: " + minepumpDir);
            return null;
        }

        for (File file : ftsFiles) {
            String filename = file.getName().substring(0, file.getName().length() - 4);
            systems.put(minepumpPath + filename,"minepump");
        }

        return systems;
    }

    public static void testbfmMetrics(String system) throws TransitionSystemDefinitionException, IOException {

        //System.out.println("********************************** " + system + " ******************************");

        String dirPath = "src/main/resources/";
        String bfmBasePath = dirPath + "bfm/eval-results/" + system;
        File fileBFM = new File(bfmBasePath + ".bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(fileBFM);

        FeaturedTransitionSystem fts;
        String ftsBasePath = dirPath + "fts/eval/" + system;
        File ftsFile = new File(ftsBasePath + ".fts");
        File dotFile = new File(ftsBasePath + ".dot");

        if (ftsFile.exists()) {
            fts = XmlLoaderUtility.loadFeaturedTransitionSystem(ftsFile);
        } else if (dotFile.exists()) {
            fts = FeaturedTransitionSystemDotHandler.parseDotFile(dotFile.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Neither .fts nor .dot file found for " + system);
        }

        int ftsTotal = fts.getActionsCount() + fts.getStatesCount() + fts.getTransitionsCount();
        //int bfmTotal = bfm.getEventsCount() + bfm.getCausalitiesCount() + bfm.getTotalNumberOfConflictingEvents();
        //System.out.println(system + " & " + fts.getActionsCount() + " & " + fts.getStatesCount() + " & \\cellcolor{lightred} " + fts.getTransitionsCount() + " & " + ftsTotal + " & " + bfm.getEventsCount() + " & " + bfm.getConflictsCount() + "\\(\\mid\\) " + bfm.getMaxConflictSize() + " & \\cellcolor{lightgreen} " + bfm.getCausalitiesCount() + " & " + bfmTotal + " & TIME \\\\");
        //System.out.println(system +  " & " + ftsTotal +  " & " + bfmTotal + " = " + (bfmTotal-ftsTotal));
        int bfmTotal = bfm.getEventsCount() + bfm.getCausalitiesCount() + bfm.getConflictsCount();
        System.out.println(system +  " & " + bfmTotal);
    }


    public static void testftsMetrics() throws TransitionSystemDefinitionException, IOException {

        //System.out.println("********************************** " + system + " ******************************");

        File dirPath = new File("src/main/resources/fts/components/");

        File[] ftsFiles = dirPath.listFiles((d, name) -> name.endsWith(".dot"));

        if (ftsFiles == null) {
            System.err.println("Directory not found or IO error: " + dirPath);
        } else {
            for (File file: ftsFiles) {
                String system = file.getName().substring(0, file.getName().length() - 4);
                FeaturedTransitionSystem fts;
                File dotFile = new File(dirPath + "/" + system + ".dot");
                fts = FeaturedTransitionSystemDotHandler.parseDotFile(dotFile.getAbsolutePath());
                int ftsTotal = fts.getActionsCount() + fts.getStatesCount() + fts.getTransitionsCount();
                System.out.println(system + " & " + fts.getActionsCount() + " & " + fts.getStatesCount() + " & " + fts.getTransitionsCount() + " & " + ftsTotal);
            }
        }
    }
}

