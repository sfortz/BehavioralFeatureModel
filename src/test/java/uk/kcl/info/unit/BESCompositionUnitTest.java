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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                Arguments.of("a", "b", true),
                Arguments.of("a", "c", true),
                Arguments.of("b", "c", true),
                Arguments.of("a", "b", false),
                Arguments.of("a", "c", false),
                Arguments.of("b", "c", false)
        );
    }

    /*
     * -------------------------
     * Composition (SYNC and ASYNC)
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testAsyncParallelComposition(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException {

        BundleEventStructure bes1 = loadBundleEventStructure(BES_PATH + f1 + BES_EXT);
        BundleEventStructure bes2 = loadBundleEventStructure(BES_PATH + f2 + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();
        BundleEventStructure result = composer.compose(bes1, bes2, sync);

        String suffix = sync ? "_sync" : "_async";
        BundleEventStructure expected = loadBundleEventStructure(BES_PATH + f1 + f2 + suffix + BES_EXT);

        Set<List<String>> expectedTraces = new BundleEventStructureExecutor(expected).getAllActionTraces();
        Set<List<String>> resultTraces = new BundleEventStructureExecutor(result).getAllActionTraces();
        assertEquals(expectedTraces, resultTraces);
    }

    /*
     * -------------------------
     * ALPHABET CHECK
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testAlphabetIsUnion(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException {

        BundleEventStructure bes1 = loadBundleEventStructure(BES_PATH + f1 + BES_EXT);
        BundleEventStructure bes2 = loadBundleEventStructure(BES_PATH + f2 + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();
        BundleEventStructure result = composer.compose(bes1, bes2, sync);

        Set<String> sigma1 = new HashSet<>(bes1.getAllActions());
        Set<String> sigma2 = new HashSet<>(bes2.getAllActions());
        Set<String> sigmaC = new HashSet<>(result.getAllActions());

        Set<String> expectedUnion = new HashSet<>(sigma1);
        expectedUnion.addAll(sigma2);

        // ✔️ main check
        assertEquals(expectedUnion, sigmaC, "Composed BES alphabet is not equal to Σ1 ∪ Σ2");

        // ✔️ optional sanity checks
        assertTrue(sigmaC.containsAll(sigma1), "Composed BES is missing actions from BES1");
        assertTrue(sigmaC.containsAll(sigma2), "Composed BES is missing actions from BES2");
    }

    /*
     * -------------------------
     * Commutativity
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativity(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException {

        BundleEventStructure bes1 = loadBundleEventStructure(BES_PATH + f1 + BES_EXT);
        BundleEventStructure bes2 = loadBundleEventStructure(BES_PATH + f2 + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();

        BundleEventStructure res1 = composer.compose(bes1, bes2, sync);
        BundleEventStructure res2 = composer.compose(bes2, bes1, sync);

        Set<List<String>> res1Traces = new BundleEventStructureExecutor(res1).getAllActionTraces();
        Set<List<String>> res2Traces = new BundleEventStructureExecutor(res2).getAllActionTraces();
        assertEquals(res1Traces, res2Traces, "Commutativity violated (" + f1 + ", " + f2 + ", sync=" + sync + ")");

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

    /*
     * -------------------------
     * Associativity
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testAssociativity(boolean sync) throws BundleEventStructureDefinitionException {

        String f1 = "a";
        String f2 = "b";
        String f3 = "c";

        BundleEventStructure bes1 = loadBundleEventStructure(BES_PATH + f1 + BES_EXT);
        BundleEventStructure bes2 = loadBundleEventStructure(BES_PATH + f2 + BES_EXT);
        BundleEventStructure bes3 = loadBundleEventStructure(BES_PATH + f3 + BES_EXT);

        BESParallelComposer composer = new BESParallelComposer();

        // (A || B) || C
        BundleEventStructure left = composer.compose(composer.compose(bes1, bes2, sync), bes3, sync);
        // A || (B || C)
        BundleEventStructure right = composer.compose(bes1, composer.compose(bes2, bes3, sync), sync);

        Set<List<String>> leftTraces = new BundleEventStructureExecutor(left).getAllActionTraces();
        Set<List<String>> rightTraces = new BundleEventStructureExecutor(right).getAllActionTraces();
        assertEquals(leftTraces, rightTraces, "Associativity violated for sync=" + sync);
    }
}
