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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.execution.FeaturedEventStructureExecutor;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.utils.translators.FesToFtsConverter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.kcl.info.utils.TestTraceUtils.getAllFtsTraces;

public class FESToFTSIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String FM_IN_PATH = BASE_PATH + "fm/xml/";
    private static final String FES_IN_PATH = BASE_PATH + "fes/";


    @ParameterizedTest
    @ValueSource(strings = {"robot.fes", "robot-linear.fes"})
    public void testFESToFTSConversion(String fesFileName) throws BundleEventStructureDefinitionException, TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        // Load FM
        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(FM_IN_PATH + "robot.xml");

        // Load FES
        FeaturedEventStructure<?> fes = XmlLoaderUtility.loadFeaturedEventStructure(FES_IN_PATH + fesFileName, fm);

        // Convert to FTS
        FesToFtsConverter converter = new FesToFtsConverter(fes);
        FeaturedTransitionSystem fts = converter.convert();

        // Execute both FES and FTS
        Map<Configuration, Set<List<String>>> fesTraces = new FeaturedEventStructureExecutor(fes, fm).getAllTraces();
        Map<Configuration, Set<List<String>>> ftsTraces = getAllFtsTraces(fm, fts);

        assertEquals(fesTraces, ftsTraces, "The FES and FTS traces should be equivalent");
    }

}
