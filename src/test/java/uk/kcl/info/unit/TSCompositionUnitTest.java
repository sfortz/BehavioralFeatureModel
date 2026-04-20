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
                Arguments.of("a", "b"),
                Arguments.of("a", "c"),
                Arguments.of("b", "c")
        );
    }

    /*
     * -------------------------
     * UNION (async composition)
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testAsyncParallelComposition(String f1, String f2) throws TransitionSystemDefinitionException, TransitionSystenExecutionException {

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();
        TransitionSystem result = composer.compose(ts1, ts2, false);

        TransitionSystem expected = loadTransitionSystem(TS_PATH + f1 + f2 + "_async" + TS_EXT);

        Set<List<String>> expectedTraces = getAllTsTraces(expected);
        Set<List<String>> resultTraces = getAllTsTraces(result);
        assertEquals(expectedTraces, resultTraces);
    }

    /*
     * -------------------------
     * SYNCHRONOUS composition
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testSyncParallelComposition(String f1, String f2) throws TransitionSystemDefinitionException, TransitionSystenExecutionException {

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();
        TransitionSystem result = composer.compose(ts1, ts2, true);

        TransitionSystem expected = loadTransitionSystem(TS_PATH + f1 + f2 + "_sync" + TS_EXT);

        Set<List<String>> expectedTraces = getAllTsTraces(expected);
        Set<List<String>> resultTraces = getAllTsTraces(result);
        assertEquals(expectedTraces, resultTraces);
    }

    /*
     * -------------------------
     * Commutativity
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativity(String f1, String f2) throws TransitionSystemDefinitionException, TransitionSystenExecutionException {

        TransitionSystem ts1 = loadTransitionSystem(TS_PATH + f1 + TS_EXT);
        TransitionSystem ts2 = loadTransitionSystem(TS_PATH + f2 + TS_EXT);

        TSParallelComposer composer = new TSParallelComposer();

        TransitionSystem res1 = composer.compose(ts1, ts2, true);
        TransitionSystem res2 = composer.compose(ts2, ts1, true);

        Set<List<String>> res1Traces = getAllTsTraces(res1);
        Set<List<String>> res2Traces = getAllTsTraces(res2);
        assertEquals(res1Traces, res2Traces);
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
}