package uk.kcl.info;

import java.io.*;

import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.solver.io.xml.XmlSavers;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
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
    }

    public static void testfts2fes() throws TransitionSystemDefinitionException, BundleEventStructureDefinitionException {
        String dirPath = "src/main/resources/";

        File fmFile = new File(dirPath + "bfm/fm2xml/robot.xml");
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

        File fmFile = new File(dirPath + "bfm/fm2xml/robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);

        File file0 = new File(dirPath + "fes/robot.fes");
        FeaturedEventStructure<?> fes0 = XmlLoaderUtility.loadFeaturedEventStructure(file0, fm);

        FeaturedTransitionSystem fts0 = Translator.fes2fts(fes0);
        System.out.println("Action Count: " + fts0.getActionsCount());
        System.out.println("State Count: " + fts0.getStatesCount());
        System.out.println("Transition Count: " + fts0.getTransitionsCount());

        PrintStream output0 = new PrintStream(new FileOutputStream(dirPath + "fts/dot/new.dot"));
        FeaturedTransitionSystemDotPrinter printer0 = new FeaturedTransitionSystemDotPrinter(fts0, output0);
        printer0.printDot();
        printer0.flush();
        XmlSaverUtility.save(fts0, dirPath + "fts/new.fts");

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

        File fmFile = new File(dirPath + "bfm/fm2xml/robot.xml");
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
        File file0 = new File(dirPath + "bfm/fm2xml/new.xml");
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

        /*  LINEAR  */

        File fileBFM = new File(dirPath + "bfm/robot.bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(fileBFM);

        FeatureModel<?> fm =Translator.bfm2fm(bfm);
        File file = new File(dirPath + "bfm/fm2xml/new-linear.xml");
        XmlSavers.save(fm, file);

        FeaturedTransitionSystem fts = Translator.bfm2fts(bfm);
        System.out.println("Action Count: " + fts.getActionsCount());
        System.out.println("State Count: " + fts.getStatesCount());
        System.out.println("Transition Count: " + fts.getTransitionsCount());

        PrintStream output = new PrintStream(new FileOutputStream(dirPath + "fts/dot/new-fromBFM-linear.dot"));
        FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(fts, output);
        printer.printDot();
        printer.flush();
        XmlSaverUtility.save(fts, dirPath + "fts/new-fromBFM-linear.fts");

    }

    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException, TransitionSystemDefinitionException, DimacsFormatException, BehavioralFeatureModelDefinitionException {
        testts2bes();
        testbes2ts();
        testfts2fes();
        testfes2fts();
        testfts2bfm();
        testbfm2fts();
    }
}

