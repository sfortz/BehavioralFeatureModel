package uk.kcl.info;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.solver.io.xml.XmlSavers;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotHandler;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import be.vibes.ts.io.dot.TransitionSystemDotPrinter;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;

public class Main {

    public static void testts2bes() throws TransitionSystemDefinitionException, BundleEventStructureDefinitionException {
        String dirPath = "src/main/resources/";

        File file1 = new File(dirPath + "ts/robot.ts");
        TransitionSystem ts1 = XmlLoaderUtility.loadTransitionSystem(file1);
        BundleEventStructure bes1 = Translator.ts2bes(ts1);
        XmlSaverUtility.save(bes1, dirPath + "bes/new.bes");

        File file = new File(dirPath + "ts/robot-linear.ts");
        TransitionSystem ts = XmlLoaderUtility.loadTransitionSystem(file);
        BundleEventStructure bes = Translator.ts2bes(ts);
        XmlSaverUtility.save(bes, dirPath + "bes/new-linear.bes");
    }

    public static void testbes2ts() throws BundleEventStructureDefinitionException, TransitionSystemDefinitionException, FileNotFoundException {
        String dirPath = "src/main/resources/";
        File file = new File(dirPath + "bes/robot-linear.bes");
        BundleEventStructure bes = XmlLoaderUtility.loadBundleEventStructure(file);
        TransitionSystem ts = Translator.bes2ts(bes);
        XmlSaverUtility.save(ts, dirPath + "ts/new-linear.ts");

        PrintStream output = new PrintStream(new FileOutputStream(dirPath + "ts/dot/new-linear.dot"));
        TransitionSystemDotPrinter printer = new TransitionSystemDotPrinter(ts, output);
        printer.printDot();
        printer.flush();

        /*  LINEAR  */

        File file2 = new File(dirPath + "bes/robot.bes");
        BundleEventStructure bes2 = XmlLoaderUtility.loadBundleEventStructure(file2);
        TransitionSystem ts2 = Translator.bes2ts(bes2);
        XmlSaverUtility.save(ts2, dirPath + "ts/new.ts");

        PrintStream output2 = new PrintStream(new FileOutputStream(dirPath + "ts/dot/new.dot"));
        TransitionSystemDotPrinter printer2 = new TransitionSystemDotPrinter(ts2, output2);
        printer2.printDot();
        printer2.flush();
    }

    public static void testfts2fes() throws TransitionSystemDefinitionException, BundleEventStructureDefinitionException {
        String dirPath = "src/main/resources/";

        File fmFile = new File(dirPath + "fm/xml/robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);

        File file0 = new File(dirPath + "fts/robot.fts");
        FeaturedTransitionSystem fts0 = XmlLoaderUtility.loadFeaturedTransitionSystem(file0);

        FeaturedEventStructure<?> fes0 = Translator.fts2fes(fm, fts0);
        System.out.println("Event Count: " + fes0.getEventsCount());
        System.out.println("Causality Count: " + fes0.getCausalitiesCount());
        System.out.println("Conflict Count: " + fes0.getConflictsCount());
        XmlSaverUtility.save(fes0, dirPath + "fes/new.fes");

        /*  LINEAR  */

        File file = new File(dirPath + "fts/robot-linear.fts");
        FeaturedTransitionSystem fts = XmlLoaderUtility.loadFeaturedTransitionSystem(file);

        FeaturedEventStructure<?> fes = Translator.fts2fes(fm, fts);
        System.out.println("Event Count: " + fes.getEventsCount());
        System.out.println("Causality Count: " + fes.getCausalitiesCount());
        System.out.println("Conflict Count: " + fes.getConflictsCount());
        XmlSaverUtility.save(fes, dirPath + "fes/new-linear.fes");

    }

    public static void testfes2fts() throws BundleEventStructureDefinitionException, TransitionSystemDefinitionException, IOException {
        String dirPath = "src/main/resources/";

        /*
        Path filePath = Paths.get(dirPath + "fm/uvl/robot.uvl");
        String content = new String(Files.readAllBytes(filePath));
        FeatureModelFactory uvlModelFactory = new FeatureModelFactory(FeatureModelFactory.SolverType.BDD);
        FeatureModel fm = (FeatureModel) uvlModelFactory.parse(content);

        FeatureModelFactory sat4jFactory = new FeatureModelFactory(FeatureModelFactory.SolverType.SAT4J);
        FeatureModel sat4jfm = (FeatureModel) sat4jFactory.parse(content);

        System.out.println("BDD:"); //TODO: Debug SAT4J Solver initialisation
        try {
            Iterator<Configuration> solutions = fm.getSolutions();
            for (; solutions.hasNext(); ) {
                System.out.println(solutions.next());
            }
        } catch (ConstraintSolvingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("SAT4J:");
        try {
            Iterator<Configuration> solutions = sat4jfm.getSolutions();
            for (; solutions.hasNext(); ) {
                System.out.println(solutions.next());
            }
        } catch (ConstraintSolvingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("-- END --"); //TODO: Debug SAT4J Solver initialisation */

        File fmFile = new File(dirPath + "fm/xml/robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);

        File file0 = new File(dirPath + "fes/robot.fes");
        FeaturedEventStructure<?> fes0 = XmlLoaderUtility.loadFeaturedEventStructure(file0, fm);

        FeaturedTransitionSystem fts0 = Translator.fes2fts(fes0);
        System.out.println("Action Count: " + fts0.getActionsCount());
        System.out.println("State Count: " + fts0.getStatesCount());
        System.out.println("Transition Count: " + fts0.getTransitionsCount());

        PrintStream output0 = new PrintStream(new FileOutputStream(dirPath + "fts/dot/new-fromFES.dot"));
        FeaturedTransitionSystemDotPrinter printer0 = new FeaturedTransitionSystemDotPrinter(fts0, output0);
        printer0.printDot();
        printer0.flush();
        XmlSaverUtility.save(fts0, dirPath + "fts/new-fromFES.fts");

        /*  LINEAR  */

        File file = new File(dirPath + "fes/robot-linear.fes");
        FeaturedEventStructure<?> fes = XmlLoaderUtility.loadFeaturedEventStructure(file, fm);

        FeaturedTransitionSystem fts = Translator.fes2fts(fes);
        System.out.println("Action Count: " + fts.getActionsCount());
        System.out.println("State Count: " + fts.getStatesCount());
        System.out.println("Transition Count: " + fts.getTransitionsCount());

        PrintStream output = new PrintStream(new FileOutputStream(dirPath + "fts/dot/new-fromFES-linear.dot"));
        FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(fts, output);
        printer.printDot();
        printer.flush();
        XmlSaverUtility.save(fts, dirPath + "fts/new-fromFES-linear.fts");


    }

    public static void testfts2bfm() throws TransitionSystemDefinitionException {
        String dirPath = "src/main/resources/";

        File fmFile = new File(dirPath + "fm/xml/robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);

        File file0 = new File(dirPath + "fts/robot.fts");
        FeaturedTransitionSystem fts0 = XmlLoaderUtility.loadFeaturedTransitionSystem(file0);

        BehavioralFeatureModel bfm0 = Translator.fts2bfm(fm, fts0);
        XmlSaverUtility.save(bfm0, dirPath + "bfm/new.bfm");

        /*  LINEAR  */

        File file = new File(dirPath + "fts/robot-linear.fts");
        FeaturedTransitionSystem fts = XmlLoaderUtility.loadFeaturedTransitionSystem(file);

        BehavioralFeatureModel bfm = Translator.fts2bfm(fm, fts);
        XmlSaverUtility.save(bfm, dirPath + "bfm/new-linear.bfm");
    }

    public static void testbfm2fts() throws TransitionSystemDefinitionException, IOException {
        String dirPath = "src/main/resources/";

        File fileBFM0 = new File(dirPath + "bfm/robot.bfm");
        BehavioralFeatureModel bfm0 = XmlLoaderUtility.loadBehavioralFeatureModel(fileBFM0);

        FeatureModel<?> fm0 =Translator.bfm2fm(bfm0);
        File file0 = new File(dirPath + "fm/xml/new.xml");
        XmlSavers.save(fm0, file0);

        FeaturedTransitionSystem fts0 = Translator.bfm2fts(bfm0);
        System.out.println("Action Count: " + fts0.getActionsCount());
        System.out.println("State Count: " + fts0.getStatesCount());
        System.out.println("Transition Count: " + fts0.getTransitionsCount());

        PrintStream output0 = new PrintStream(new FileOutputStream(dirPath + "fts/dot/new-fromBFM.dot"));
        FeaturedTransitionSystemDotPrinter printer0 = new FeaturedTransitionSystemDotPrinter(fts0, output0);
        printer0.printDot();
        printer0.flush();
        XmlSaverUtility.save(fts0, dirPath + "fts/new-fromBFM.fts");

        /*  LINEAR */
        /*
            TODO: Buggy since renaming implies moving events to their parents (e.g., liDet should be in root as it is
             associated to lidet && mapping) */
/*
        File fileBFM = new File(dirPath + "bfm/robot-linear.bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(fileBFM);

        FeatureModel<?> fm =Translator.bfm2fm(bfm);
        File file = new File(dirPath + "fm/xml/new-linear.xml");
        XmlSavers.save(fm, file);

        FeaturedTransitionSystem fts = Translator.bfm2fts(bfm);
        System.out.println("Action Count: " + fts.getActionsCount());
        System.out.println("State Count: " + fts.getStatesCount());
        System.out.println("Transition Count: " + fts.getTransitionsCount());

        PrintStream output = new PrintStream(new FileOutputStream(dirPath + "fts/dot/new-fromBFM-linear.dot"));
        FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(fts, output);
        printer.printDot();
        printer.flush();
        XmlSaverUtility.save(fts, dirPath + "fts/new-fromBFM-linear.fts");*/

    }

    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException, TransitionSystemDefinitionException, DimacsFormatException, BehavioralFeatureModelDefinitionException {
        /*testts2bes();
        testbes2ts();
        testfts2fes();
        testfes2fts();
        testfts2bfm();
        testbfm2fts();
        testparallel();*/


        for(Map.Entry<String,String> entry: getSystems().entrySet()){
            testfts2bfm(entry.getValue(), entry.getKey());
        }

        System.out.println("============================================");
        for(String key: getSystems().keySet()){
            testbfmMetrics(key);
        }

    }

    public static void testparallel() throws TransitionSystemDefinitionException, BundleEventStructureDefinitionException {
        String dirPath = "src/main/resources/";
        File file = new File(dirPath + "ts/parallel.ts");
        TransitionSystem ts = XmlLoaderUtility.loadTransitionSystem(file);
        BundleEventStructure bes = Translator.ts2bes(ts);
        XmlSaverUtility.save(bes, dirPath + "bes/new-parallel.bes");
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
        BehavioralFeatureModel bfm = Translator.fts2bfm(fm, fts);
        long endTime = System.nanoTime();
        long durationInNanoseconds = endTime - startTime;
        double durationInMilliseconds = durationInNanoseconds / 1_000_000.0;
        System.out.println("Execution time: " + durationInMilliseconds + " ms");


        XmlSaverUtility.save(bfm, dirPath + "bfm/eval-results/" + ftsName + ".bfm");
    }

    public static Map<String, String> getSystems() {
        Map<String, String> systems = new HashMap<>();

        /*
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
        systems.put("/vm/new/coffeesoda","coffeesoda");*/

        //systems.put("/vm/new/svm","svm");

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

    public static void testbfmMetrics(String bfmName) {

        System.out.println("********************************** " + bfmName+ " ******************************");

        String dirPath = "src/main/resources/";
        String basePath = dirPath + "bfm/eval-results/" + bfmName;
        File fileBFM = new File(basePath + ".bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(fileBFM);

        System.out.println("BFM Event count: " + bfm.getEventsCount());
        System.out.println("BFM Conflict count: " + bfm.getConflictsCount());
        System.out.println("BFM Causality count: " + bfm.getCausalitiesCount());
        System.out.println("BFM Max Conflict size: " + bfm.getMaxConflictSize());
    }
}

