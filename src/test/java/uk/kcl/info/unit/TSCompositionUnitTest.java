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

import be.vibes.ts.*;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import uk.kcl.info.bfm.compositions.TSParallelComposer;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static be.vibes.ts.io.xml.XmlLoaders.loadTransitionSystem;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.kcl.info.utils.TSTraceUtils.actionsOf;
import static uk.kcl.info.utils.TSTraceUtils.getAllTsTraces;

public class TSCompositionUnitTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String TS_PATH = BASE_PATH + "ts/";
    private static final String TS_EXT = ".ts";

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
    public void testAsyncParallelComposition(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException, TransitionSystenExecutionException {

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();
        TransitionSystem result = composer.compose(ts1, ts2, sync);

        String suffix = sync ? "_sync" : "_async";
        TransitionSystem expected = loadTransitionSystem(TS_PATH + f1 + f2 + suffix + TS_EXT);

        Set<List<String>> expectedTraces = getAllTsTraces(expected);
        Set<List<String>> resultTraces = getAllTsTraces(result);
        assertEquals(expectedTraces, resultTraces, "Parallel composition mismatch (" + f1 + ", " + f2 + ", sync=" + sync + ")");
    }

    /*
     * -------------------------
     * ALPHABET CHECK
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testAlphabetIsUnion(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException {

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();
        TransitionSystem result = composer.compose(ts1, ts2, sync);

        Set<Action> sigma1 = new HashSet<>(actionsOf(ts1));
        Set<Action> sigma2 = new HashSet<>(actionsOf(ts2));
        Set<Action> sigmaC = new HashSet<>(actionsOf(result));

        Set<Action> expectedUnion = new HashSet<>(sigma1);
        expectedUnion.addAll(sigma2);

        // ✔️ main check
        assertEquals(expectedUnion, sigmaC, "Composed TS alphabet is not equal to Σ1 ∪ Σ2");

        // ✔️ optional sanity checks
        assertTrue(sigmaC.containsAll(sigma1), "Composed TS is missing actions from TS1");
        assertTrue(sigmaC.containsAll(sigma2), "Composed TS is missing actions from TS2");
    }

    /*
     * -------------------------
     * Commutativity
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativity(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException, TransitionSystenExecutionException {

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();

        TransitionSystem res1 = composer.compose(ts1, ts2, sync);
        TransitionSystem res2 = composer.compose(ts2, ts1, sync);

        Set<List<String>> res1Traces = getAllTsTraces(res1);
        Set<List<String>> res2Traces = getAllTsTraces(res2);
        assertEquals(res1Traces, res2Traces, "Commutativity violated (" + f1 + ", " + f2 + ", sync=" + sync + ")");
    }

    /*
     * -------------------------
     * Initial state correctness
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testInitialState(String f1, String f2) throws TransitionSystemDefinitionException {

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();
        TransitionSystem result = composer.compose(ts1, ts2, true);

        String expectedInit = ts1.getInitialState().getName() + "||" + ts2.getInitialState().getName();
        assertEquals(expectedInit, result.getInitialState().getName());
    }


    /*
     * -------------------------
     * Idempotency
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(strings = {"a","b","c","robot","parallel"})
    public void testIdempotency(String fileName) throws TransitionSystemDefinitionException, TransitionSystenExecutionException {

        TransitionSystem ts = loadTransitionSystem(TS_PATH + fileName + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();
        TransitionSystem result = composer.compose(ts, ts, true);

        Set<List<String>> expectedTraces = getAllTsTraces(ts);
        Set<List<String>> resultTraces = getAllTsTraces(result);
        assertEquals(expectedTraces, resultTraces);
    }

    /*
     * -------------------------
     * Associativity
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testAssociativity(boolean sync) throws Exception {

        String f1 = "a";
        String f2 = "b";
        String f3 = "c";

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);
        TransitionSystem ts3 = loadTransitionSystem(TS_PATH + f3 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();

        // (A || B) || C
        TransitionSystem left = composer.compose(composer.compose(ts1, ts2, sync), ts3, sync);
        // A || (B || C)
        TransitionSystem right = composer.compose(ts1, composer.compose(ts2, ts3, sync), sync);

        assertEquals(left,right,"Associativity violated for sync=" + sync);
    }
}