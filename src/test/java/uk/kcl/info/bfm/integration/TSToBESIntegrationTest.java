package uk.kcl.info.bfm.integration;

import be.vibes.ts.TransitionSystem;
import org.junit.jupiter.api.Test;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.utils.translators.TsToBesConverter;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;

import java.io.File;

public class TSToBESIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String TS_IN_PATH = BASE_PATH + "ts/xml/";
    private static final String BES_OUT_PATH = BASE_PATH + "bes/";

    @Test
    public void testRobotLinearTSConversion() throws Exception {
        // Load TS
        TransitionSystem ts = XmlLoaderUtility.loadTransitionSystem(new File(TS_IN_PATH + "robot-linear.ts"));
        // Convert to BES
        TsToBesConverter converter = new TsToBesConverter(ts);
        BundleEventStructure bes = converter.convert();
        // Save BES
        saveBes(bes, "robot-linear");
    }

    @Test
    public void testRobotTSConversion() throws Exception {
        // Load TS
        TransitionSystem ts = XmlLoaderUtility.loadTransitionSystem(new File(TS_IN_PATH + "robot.ts"));
        // Convert to BES
        TsToBesConverter converter = new TsToBesConverter(ts);
        BundleEventStructure bes = converter.convert();
        // Save BES
        saveBes(bes, "robot");
    }

    @Test
    public void testParallelTSConversion() throws Exception {
        // Load TS
        TransitionSystem ts = XmlLoaderUtility.loadTransitionSystem(new File(TS_IN_PATH + "parallel.ts"));
        // Convert to BES
        TsToBesConverter converter = new TsToBesConverter(ts);
        BundleEventStructure bes = converter.convert();
        // Save BES
        saveBes(bes, "parallel");
    }

    private void saveBes(BundleEventStructure bes, String filenamePrefix) throws Exception {
        File outFile = new File(BES_OUT_PATH + filenamePrefix + "-from-ts.bes");
        outFile.getParentFile().mkdirs();
        XmlSaverUtility.save(bes, outFile);
    }
}
