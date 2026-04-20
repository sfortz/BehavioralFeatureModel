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

package uk.kcl.info.unit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.compositions.BESParallelComposer;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;
import uk.kcl.info.bfm.execution.BundleEventStructureExecutor;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.kcl.info.bfm.io.xml.XmlLoaderUtility.loadBundleEventStructure;

public class BESCompositionUnitTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String BES_PATH = BASE_PATH + "bes/";
    private static final String BES_EXT = ".bes";

    /*
     * -------------------------
     * Test cases
     * -------------------------
     */
    static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of("a", "b"),
                Arguments.of("a", "c"),
                Arguments.of("b", "c")
        );
    }

    /*
     * -------------------------
     * ASYNC composition
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testAsyncParallelComposition(String f1, String f2) throws BundleEventStructureDefinitionException {

        BundleEventStructure bes1 = loadBundleEventStructure(BES_PATH + f1 + BES_EXT);
        BundleEventStructure bes2 = loadBundleEventStructure(BES_PATH + f2 + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();
        BundleEventStructure result = composer.compose(bes1, bes2, false);

        BundleEventStructure expected = loadBundleEventStructure(BES_PATH + f1 + f2 + "_async" + BES_EXT);

        Set<List<String>> expectedTraces = new BundleEventStructureExecutor(expected).getAllActionTraces();
        Set<List<String>> resultTraces = new BundleEventStructureExecutor(result).getAllActionTraces();
        assertEquals(expectedTraces, resultTraces);
    }

    /*
     * -------------------------
     * SYNC composition
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testSyncParallelComposition(String f1, String f2) throws BundleEventStructureDefinitionException {

        BundleEventStructure bes1 = loadBundleEventStructure(BES_PATH + f1 + BES_EXT);
        BundleEventStructure bes2 = loadBundleEventStructure(BES_PATH + f2 + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();
        BundleEventStructure result = composer.compose(bes1, bes2, true);

        BundleEventStructure expected = loadBundleEventStructure(BES_PATH + f1 + f2 + "_sync" + BES_EXT);

        Set<List<String>> expectedTraces = new BundleEventStructureExecutor(expected).getAllActionTraces();
        Set<List<String>> resultTraces = new BundleEventStructureExecutor(result).getAllActionTraces();
        assertEquals(expectedTraces, resultTraces);
    }

    /*
     * -------------------------
     * Commutativity
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativity(String f1, String f2) throws BundleEventStructureDefinitionException {

        BundleEventStructure bes1 = loadBundleEventStructure(BES_PATH + f1 + BES_EXT);
        BundleEventStructure bes2 = loadBundleEventStructure(BES_PATH + f2 + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();

        BundleEventStructure res1 = composer.compose(bes1, bes2, true);
        BundleEventStructure res2 = composer.compose(bes2, bes1, true);

        Set<List<String>> res1Traces = new BundleEventStructureExecutor(res1).getAllActionTraces();
        Set<List<String>> res2Traces = new BundleEventStructureExecutor(res2).getAllActionTraces();
        assertEquals(res1Traces, res2Traces);
    }

    /*
     * -------------------------
     * Idempotency
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(strings = {"a","b","c","robot"})
    public void testIdempotency(String fileName) throws BundleEventStructureDefinitionException {

        BundleEventStructure bes = loadBundleEventStructure(BES_PATH + fileName + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();
        BundleEventStructure result = composer.compose(bes, bes, true);

        Set<List<String>> expectedTraces = new BundleEventStructureExecutor(bes).getAllActionTraces();
        Set<List<String>> resultTraces = new BundleEventStructureExecutor(result).getAllActionTraces();
        assertEquals(expectedTraces, resultTraces);

    }
}
