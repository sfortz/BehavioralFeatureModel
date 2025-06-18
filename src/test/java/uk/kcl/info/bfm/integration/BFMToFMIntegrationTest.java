package uk.kcl.info.bfm.integration;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlSavers;
import org.junit.jupiter.api.Test;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.utils.translators.BfmToFmConverter;

import java.io.File;

public class BFMToFMIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String BFM_IN_PATH = BASE_PATH + "bfm/";
    private static final String FM_OUT_PATH = BASE_PATH + "fm/xml/";


    @Test
    public void testRobotBFMToFTSConversion() throws Exception {
        // Load BFM
        File bfmFile = new File(BFM_IN_PATH + "robot.bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(bfmFile);
        // Convert to FM
        BfmToFmConverter converter = new BfmToFmConverter(bfm);
        FeatureModel<?> fm = converter.convert();

        saveFm(fm, "robot");
    }

    @Test
    public void testRobotLinearBFMToFTSConversion() throws Exception {
        // Load BFM
        File bfmFile = new File(BFM_IN_PATH + "robot-linear.bfm");
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(bfmFile);
        // Convert to FM
        BfmToFmConverter converter = new BfmToFmConverter(bfm);
        FeatureModel<?> fm = converter.convert();

        saveFm(fm, "robot-linear");
    }

    private void saveFm(FeatureModel<?> fm, String filenamePrefix) {
        XmlSavers.save(fm, FM_OUT_PATH + filenamePrefix + "-from-bfm.xml");
    }
}