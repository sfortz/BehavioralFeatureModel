package uk.kcl.info.bfm.integration;

import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import org.junit.jupiter.api.Test;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.utils.translators.BfmToFtsConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class BFMToFTSIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String BFM_IN_PATH = BASE_PATH + "bfm/";
    private static final String XML_OUT_PATH = BASE_PATH + "fts/xml/";
    private static final String DOT_OUT_PATH = BASE_PATH + "fts/dot/";

    @Test
    public void testRobotBFMToFTSConversion() throws Exception {
        // Load BFM
        File bfmFile = new File(BFM_IN_PATH + "robot.bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(bfmFile);
        // Convert to FTS
        BfmToFtsConverter converter = new BfmToFtsConverter(bfm);
        FeaturedTransitionSystem fts = converter.convert();

        saveFts(fts, "robot");
    }

    /*
    @Test
    public void testRobotLinearBFMToFTSConversion() throws Exception {
        // Load BFM
        File bfmFile = new File(BFM_IN_PATH + "robot-linear.bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(bfmFile);
        // Convert to FTS
        BfmToFtsConverter converter = new BfmToFtsConverter(bfm);
        FeaturedTransitionSystem fts = converter.convert();

        saveFts(fts, "robot-linear");
    }*/

    private void saveFts(FeaturedTransitionSystem fts, String filenamePrefix) throws Exception {

        // Save DOT
        File dotFile = new File(DOT_OUT_PATH + filenamePrefix + "-from-bfm.dot");
        dotFile.getParentFile().mkdirs();
        try (PrintStream out = new PrintStream(new FileOutputStream(dotFile))) {
            FeaturedTransitionSystemDotPrinter printer = new FeaturedTransitionSystemDotPrinter(fts, out);
            printer.printDot();
            printer.flush();
        }

        // Save XML
        XmlSaverUtility.save(fts, XML_OUT_PATH + filenamePrefix + "-from-bfm.fts");
    }
}