package uk.kcl.info;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import be.vibes.fexpression.configuration.Configuration;
import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.FeatureModelFactory;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;


public class Main {

    public static void testts2bes() throws TransitionSystemDefinitionException, BundleEventStructureDefinitionException {
        String dirPath = "src/main/resources/";
        File file = new File(dirPath + "ts/robot.ts");
        TransitionSystem ts = XmlLoaderUtility.loadTransitionSystem(file);
        BundleEventStructure bes = Translator.ts2bes(ts);
        System.out.println("Event Count: " + bes.getEventsCount());
        System.out.println("Causality Count: " + bes.getCausalitiesCount());
        System.out.println("Conflict Count: " + bes.getConflictsCount());
        XmlSaverUtility.save(bes, dirPath + "bes/new.bes");
    }

    public static void testbes2ts() throws BundleEventStructureDefinitionException, TransitionSystemDefinitionException {
        String dirPath = "src/main/resources/";
        File file = new File(dirPath + "bes/robot.bes");
        BundleEventStructure bes = XmlLoaderUtility.loadBundleEventStructure(file);
        TransitionSystem ts = Translator.bes2ts(bes);
        System.out.println("Action Count: " + ts.getActionsCount());
        System.out.println("State Count: " + ts.getStatesCount());
        System.out.println("Transition Count: " + ts.getTransitionsCount());
        XmlSaverUtility.save(ts, dirPath + "ts/new.ts");
    }

    public static void testfts2fes() throws TransitionSystemDefinitionException, BundleEventStructureDefinitionException, IOException {
        String dirPath = "src/main/resources/";

        Path filePath = Paths.get(dirPath + "fm/uvl/robot.uvl");
        String content = new String(Files.readAllBytes(filePath));
        FeatureModelFactory uvlModelFactory = new FeatureModelFactory();
        FeatureModel fm = (FeatureModel) uvlModelFactory.parse(content);

        File file = new File(dirPath + "fts/robot.fts");
        FeaturedTransitionSystem fts = XmlLoaderUtility.loadFeaturedTransitionSystem(file);

        FeaturedEventStructure fes = Translator.fts2fes(fm, fts);
        System.out.println("Event Count: " + fes.getEventsCount());
        System.out.println("Causality Count: " + fes.getCausalitiesCount());
        System.out.println("Conflict Count: " + fes.getConflictsCount());
        XmlSaverUtility.save(fes, dirPath + "fes/new.fes");
    }

    public static void testfes2fts() throws BundleEventStructureDefinitionException, TransitionSystemDefinitionException, IOException {
        String dirPath = "src/main/resources/";

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

        System.out.println("-- END --");

        File file = new File(dirPath + "fes/robot.fes");
        FeaturedEventStructure fes = XmlLoaderUtility.loadFeaturedEventStructure(file, fm);

        FeaturedTransitionSystem fts = Translator.fes2fts(fes);
        System.out.println("Action Count: " + fts.getActionsCount());
        System.out.println("State Count: " + fts.getStatesCount());
        System.out.println("Transition Count: " + fts.getTransitionsCount());

        PrintStream output = new PrintStream(new FileOutputStream(dirPath + "fts/dot/new.dot"));
        FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(fts, output);
        printer.printDot();
        printer.flush();
        XmlSaverUtility.save(fts, dirPath + "fts/new.fts");
    }


    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException, TransitionSystemDefinitionException, DimacsFormatException {

        //testts2bes();
        //testbes2ts();
        //testfts2fes();
        testfes2fts();
    }

    public static void oldmain(FeatureModel featureModel) throws IOException {

        String dirPath = "src/main/resources/fm/uvl/";
        // Write
        String uvlModel = featureModel.toString();
        Path outFilePath = Paths.get(dirPath + featureModel.getNamespace() + "_out.uvl");
        Files.write(outFilePath, uvlModel.getBytes());
    }
}

