package uk.kcl.info;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;
import uk.kcl.info.bfm.BundleEventStructure;
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


    public static void main(String[] args) throws IOException, BundleEventStructureDefinitionException, TransitionSystemDefinitionException {
        testts2bes();
        testbes2ts();
    }

    public static void oldmain(String[] args) throws IOException {
        // Read
        String dirPath = "src/main/resources/fm/uvl/";
        Path filePath = Paths.get(dirPath + "robot.uvl");
        String content = new String(Files.readAllBytes(filePath));
        UVLModelFactory uvlModelFactory = new UVLModelFactory();
        FeatureModel featureModel = uvlModelFactory.parse(content);

        System.out.println("This is fm: " + featureModel.getRootFeature().getFeatureType());
        System.out.println("This is fm: " + featureModel.getRootFeature().getChildren());

        System.out.println("This is fm: " + featureModel.getRootFeature().getParentFeature());

        System.out.println("This is fm: " + featureModel.getRootFeature().getParentGroup());


        // Write
        String uvlModel = featureModel.toString();
        Path outFilePath = Paths.get(dirPath + featureModel.getNamespace() + "_out.uvl");
        Files.write(outFilePath, uvlModel.getBytes());
    }
}

