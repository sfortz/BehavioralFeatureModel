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