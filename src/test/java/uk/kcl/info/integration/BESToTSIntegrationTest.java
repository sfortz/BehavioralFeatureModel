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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.kcl.info.utils.TestTraceUtils.getAllTsTraces;

import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.execution.BundleEventStructureExecutor;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;
import uk.kcl.info.utils.translators.BesToTsConverter;

import java.io.File;
import java.util.*;

public class BESToTSIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String BES_IN_PATH = BASE_PATH + "bes/";

    @ParameterizedTest
    @ValueSource(strings = {"robot.bes", "robot-linear.bes"})
    public void testBESConversion(String besFileName) throws BundleEventStructureDefinitionException, TransitionSystenExecutionException {

        // Load BES
        BundleEventStructure bes = XmlLoaderUtility.loadBundleEventStructure(new File(BES_IN_PATH + besFileName));

        // Convert to TS
        BesToTsConverter converter = new BesToTsConverter(bes);
        TransitionSystem ts = converter.convert();

        // Execute both BES and TS
        Set<List<String>> besTraces = new BundleEventStructureExecutor(bes).getAllTraces();
        Set<List<String>> tsTraces = getAllTsTraces(ts);

        assertEquals(besTraces, tsTraces, "The BES and TS traces should be equivalent");
    }

}
