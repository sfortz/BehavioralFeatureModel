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

package uk.kcl.info.bfm.integration;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.FeaturedTransitionSystem;
import org.junit.jupiter.api.Test;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.utils.translators.FtsToBfmConverter;

import java.io.File;

public class FTSToBFMIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String FM_IN_PATH = BASE_PATH + "fm/xml/";
    private static final String FTS_IN_PATH = BASE_PATH + "fts/xml/";
    private static final String BFM_OUT_PATH = BASE_PATH + "bfm/";

    @Test
    public void testRobotFTSConversion() throws Exception {
        // Load FM
        File fmFile = new File(FM_IN_PATH + "robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);
        // Load FTS
        FeaturedTransitionSystem fts = XmlLoaderUtility.loadFeaturedTransitionSystem(new File(FTS_IN_PATH + "robot.fts"));
        // Convert to BFM
        FtsToBfmConverter converter = new FtsToBfmConverter(fm, fts);
        BehavioralFeatureModel bfm = converter.convert();
        // Save BFM
        saveBfm(bfm, "robot");
    }

    @Test
    public void testRobotLinearFTSConversion() throws Exception {
        // Load FM
        File fmFile = new File(FM_IN_PATH + "robot.xml");
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(fmFile);
        // Load FTS
        FeaturedTransitionSystem fts = XmlLoaderUtility.loadFeaturedTransitionSystem(new File(FTS_IN_PATH + "robot-linear.fts"));
        // Convert to BFM
        FtsToBfmConverter converter = new FtsToBfmConverter(fm, fts);
        BehavioralFeatureModel bfm = converter.convert();
        // Save BFM
        saveBfm(bfm, "robot-linear");
    }

    private void saveBfm(BehavioralFeatureModel bfm, String filenamePrefix) throws Exception {
        File outFile = new File(BFM_OUT_PATH + filenamePrefix + "-from-fts.bfm");
        outFile.getParentFile().mkdirs();
        XmlSaverUtility.save(bfm, outFile);
    }
}