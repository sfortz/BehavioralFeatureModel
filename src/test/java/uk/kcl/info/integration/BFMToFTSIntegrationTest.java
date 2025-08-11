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

package uk.kcl.info.integration;

import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import be.vibes.ts.exception.UnresolvedFExpression;
import be.vibes.ts.io.dot.FeaturedTransitionSystemDotPrinter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.execution.FeaturedEventStructureExecutor;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;
import uk.kcl.info.utils.translators.BfmToFmConverter;
import uk.kcl.info.utils.translators.BfmToFtsConverter;
import uk.kcl.info.utils.translators.FesToFtsConverter;
import uk.kcl.info.utils.translators.FtsToBfmConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.kcl.info.utils.TestTraceUtils.getAllFtsTraces;

public class BFMToFTSIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String BFM_IN_PATH = BASE_PATH + "bfm/";

    @ParameterizedTest
    @ValueSource(strings = {"robot.bfm"})
    public void testBFMToFTSConversion(String bfmFileName) throws TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        // Load FES
        BehavioralFeatureModel bfm = XmlLoaderUtility.loadBehavioralFeatureModel(BFM_IN_PATH + bfmFileName);

        // Convert to FM
        BfmToFmConverter fmConverter = new BfmToFmConverter(bfm);
        FeatureModel<?> fm = fmConverter.convert();

        // Convert to FTS
        BfmToFtsConverter converter = new BfmToFtsConverter(bfm);
        FeaturedTransitionSystem fts = converter.convert();

        // Execute both BFM and FTS
        Map<Configuration, Set<List<String>>> bfmTraces = new FeaturedEventStructureExecutor(bfm).getAllTraces();
        Map<Configuration, Set<List<String>>> ftsTraces = getAllFtsTraces(fm, fts);

        assertEquals(bfmTraces, ftsTraces, "The BFM and FTS traces should be equivalent");
    }

    /*  TODO: Buggy since renaming implies moving events to their parents (e.g., liDet should be in root as it is
             associated to lidet && mapping)
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


}