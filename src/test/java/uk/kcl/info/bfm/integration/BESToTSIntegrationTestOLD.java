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

import be.vibes.ts.TransitionSystem;
import org.junit.jupiter.api.Test;
import uk.kcl.info.bfm.BundleEventStructure;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BESToTSIntegrationTestOLD {

    private static final String BASE_PATH = "src/test/resources/testcases/bes/";

    @Test
    public void testRobotBesToTsTraceEquivalence() throws Exception {
        testTraceEquivalence("robot.bes", "robot.ts");
    }

    @Test
    public void testRobotLinearBesToTsTraceEquivalence() throws Exception {
        testTraceEquivalence("robot-linear.bes", "robot-linear.ts");
    }

    private void testTraceEquivalence(String besFileName, String expectedTsFileName) throws Exception {
        // Load BES
        File besFile = new File(BASE_PATH + besFileName);

        /*
        BundleEventStructure bes = BESParser.parse(besFile);

        // Convert to TS using refactored converter
        BesToTsConverter converter = new BesToTsConverter(bes);
        TransitionSystem generatedTs = converter.convert();

        // Load expected TS
        File tsFile = new File(BASE_PATH + expectedTsFileName);
        TransitionSystem expectedTs = TraceParser.parseTS(tsFile);

        // Extract traces
        Traces expectedTraces = new Traces(expectedTs);
        Traces generatedTraces = new Traces(generatedTs);

        // Compare trace equivalence
        boolean isEquivalent = expectedTraces.hasSameTracesAs(generatedTraces);
        assertTrue(isEquivalent, "Generated TS is not trace-equivalent to expected TS");*/
    }
}
