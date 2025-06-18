package uk.kcl.info.bfm.integration;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import org.junit.jupiter.api.Test;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.utils.translators.FesToFtsConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FESToFTSIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String FM_IN_PATH = BASE_PATH + "fm/xml/";
    private static final String FES_IN_PATH = BASE_PATH + "fes/";
    private static final String XML_OUT_PATH = BASE_PATH + "fts/xml/";
    private static final String DOT_OUT_PATH = BASE_PATH + "fts/dot/";

    @Test
    public void testRobotFESToFTSConversion() throws Exception {
        // Load FM
        File fmFile = new File(FM_IN_PATH + "robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);
        // Load FES
        File fesFile = new File(FES_IN_PATH + "robot.fes");
        FeaturedEventStructure<?> fes = XmlLoaderUtility.loadFeaturedEventStructure(fesFile, fm);
        // Convert to FTS
        FesToFtsConverter converter = new FesToFtsConverter(fes);
        FeaturedTransitionSystem fts = converter.convert();

        saveFts(fts, "robot");
    }

    @Test
    public void testRobotLinearFESToFTSConversion() throws Exception {
        // Load FM
        File fmFile = new File(FM_IN_PATH + "robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);
        // Load FES
        File fesFile = new File(FES_IN_PATH + "robot-linear.fes");
        FeaturedEventStructure<?> fes = XmlLoaderUtility.loadFeaturedEventStructure(fesFile, fm);
        // Convert to FTS
        FesToFtsConverter converter = new FesToFtsConverter(fes);
        FeaturedTransitionSystem fts = converter.convert();

        saveFts(fts, "robot-linear");
    }

    private void saveFts(FeaturedTransitionSystem fts, String filenamePrefix) throws Exception {

        // Save DOT
        File dotFile = new File(DOT_OUT_PATH + filenamePrefix + "-from-fes.dot");
        dotFile.getParentFile().mkdirs();
        try (PrintStream out = new PrintStream(new FileOutputStream(dotFile))) {
            FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(fts, out);
            printer.printDot();
            printer.flush();
        }

        // Save XML
        XmlSaverUtility.save(fts, XML_OUT_PATH + filenamePrefix + "-from-fes.fts");
    }
}
