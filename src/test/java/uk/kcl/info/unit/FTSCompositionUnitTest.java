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

import be.vibes.fexpression.FExpression;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.*;
import be.vibes.ts.exception.*;
import be.vibes.ts.execution.FeaturedTransitionSystemExecutor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import uk.kcl.info.bfm.compositions.FTSParallelComposer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Stream;

import static be.vibes.ts.io.xml.XmlLoaders.loadFeaturedTransitionSystem;
import static org.junit.jupiter.api.Assertions.*;
import static uk.kcl.info.utils.TSTraceUtils.actionsOf;
import static uk.kcl.info.utils.FTSTraceUtils.*;

public class FTSCompositionUnitTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String FM_PATH = BASE_PATH + "fm/xml/";
    private static final String FTS_PATH = BASE_PATH + "fts/";
    private static final String FTS_EXT = ".fts";

    // --- sampling parameters ---
    private static final int MAX_TRACES = 500;
    private static final int MAX_ATTEMPTS = 10;
    private static final int LOG_LIMIT = 5;

    /*
     * -------------------------
     * Test cases
     * -------------------------
     */
    static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of("coffee", "soda", true),
                Arguments.of("soda", "soup", true),
                Arguments.of("coffee", "soup", true),
                Arguments.of("coffee", "soda", false),
                Arguments.of("soda", "soup", false),
                Arguments.of("coffee", "soup", false)
        );
    }

    private static String summarize(String title, Map<FExpression, Set<List<String>>> traces) {
        StringBuilder sb = new StringBuilder(title).append(" (").append(traces.size()).append(")\n");

        int i = 0;
        for (Map.Entry<FExpression, Set<List<String>>> t: traces.entrySet()) {
            if (i++ >= LOG_LIMIT) break;
            sb.append("  ").append(t).append("\n");
        }
        return sb.toString();
    }

    private static void assertEquivalent(FeatureModel<?> fm, FeaturedTransitionSystem left, FeaturedTransitionSystem right, String message) throws TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException, FileNotFoundException {

        // Executors
        FeaturedTransitionSystemExecutor leftExec = new FeaturedTransitionSystemExecutor(left, fm);
        FeaturedTransitionSystemExecutor rightExec = new FeaturedTransitionSystemExecutor(right, fm);

        // Sample traces
        Map<FExpression, Set<List<String>>> leftTraces = getRandomTraces(fm, left, MAX_TRACES, MAX_ATTEMPTS);
        Map<FExpression, Set<List<String>>> rightTraces = getRandomTraces(fm, right, MAX_TRACES, MAX_ATTEMPTS);

        // Left ⊆ Right
        Map<FExpression, Set<List<String>>> missingLR = notExecutable(leftTraces, rightExec);
        // Right ⊆ Left
        Map<FExpression, Set<List<String>>> missingRL = notExecutable(rightTraces, leftExec);

        assertTrue(missingLR.isEmpty() && missingRL.isEmpty(),
                () -> message + "\n"
                        + summarize("Missing Left→Right", missingLR)
                        + summarize("Missing Right→Left", missingRL));
    }

    /*
     * -------------------------
     * Composition (SYNC and ASYNC)
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testParallelComposition(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException, FileNotFoundException, TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union" + ".xml"));
        FeaturedTransitionSystem fts1 = loadFeaturedTransitionSystem(FTS_PATH + f1 + FTS_EXT);
        FeaturedTransitionSystem fts2 = loadFeaturedTransitionSystem(FTS_PATH + f2 + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();
        FeaturedTransitionSystem result = composer.compose(fts1, fts2, sync);

        String suffix = sync ? "_sync" : "_async";
        FeaturedTransitionSystem expected = loadFeaturedTransitionSystem(FTS_PATH + f1 + f2 + suffix + FTS_EXT);

        assertEquivalent(fm, expected, result, "Parallel composition mismatch (" + f1 + ", " + f2 + ", sync=" + sync + ")");
    }

    /*
     * -------------------------
     * ALPHABET CHECK
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testAlphabetIsUnion(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException {

        FeaturedTransitionSystem fts1 = loadFeaturedTransitionSystem(FTS_PATH + f1 + FTS_EXT);
        FeaturedTransitionSystem fts2 = loadFeaturedTransitionSystem(FTS_PATH + f2 + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();
        FeaturedTransitionSystem result = composer.compose(fts1, fts2, sync);

        Set<Action> sigma1 = new HashSet<>(actionsOf(fts1));
        Set<Action> sigma2 = new HashSet<>(actionsOf(fts2));
        Set<Action> sigmaC = new HashSet<>(actionsOf(result));

        Set<Action> expectedUnion = new HashSet<>(sigma1);
        expectedUnion.addAll(sigma2);

        // ✔️ main check
        assertEquals(expectedUnion, sigmaC, "Composed FTS alphabet is not equal to Σ1 ∪ Σ2");

        // ✔️ optional sanity checks
        assertTrue(sigmaC.containsAll(sigma1), "Composed FTS is missing actions from FTS1");
        assertTrue(sigmaC.containsAll(sigma2), "Composed FTS is missing actions from FTS2");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativity(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException, FileNotFoundException, TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union" + ".xml"));
        FeaturedTransitionSystem fts1 = loadFeaturedTransitionSystem(FTS_PATH + f1 + FTS_EXT);
        FeaturedTransitionSystem fts2 = loadFeaturedTransitionSystem(FTS_PATH + f2 + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();

        FeaturedTransitionSystem r1 = composer.compose(fts1, fts2, sync);
        FeaturedTransitionSystem r2 = composer.compose(fts2, fts1, sync);

        assertEquivalent(fm, r1, r2, "Commutativity violated (" + f1 + ", " + f2 + ", sync=" + sync + ")");
    }

    /*
     * -------------------------
     * Associativity
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testAssociativity(boolean sync) throws Exception {

        String f1 = "coffee";
        String f2 = "soda";
        String f3 = "soup";

        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(new File(FM_PATH + "svm_union.xml"));
        FeaturedTransitionSystem fts1 = loadFeaturedTransitionSystem(FTS_PATH + f1 + FTS_EXT);
        FeaturedTransitionSystem fts2 = loadFeaturedTransitionSystem(FTS_PATH + f2 + FTS_EXT);
        FeaturedTransitionSystem fts3 = loadFeaturedTransitionSystem(FTS_PATH + f3 + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();

        // (A || B) || C
        FeaturedTransitionSystem left = composer.compose(composer.compose(fts1, fts2, sync), fts3, sync);
        // A || (B || C)
        FeaturedTransitionSystem right = composer.compose(fts1, composer.compose(fts2, fts3, sync), sync);

        assertEquivalent(fm, left, right,"Associativity violated for sync=" + sync);
    }

    /*
     * -------------------------
     * IDEMPOTENCY
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(strings = {"coffee","soda", "soup"})
    public void testIdempotency(String fileName) throws TransitionSystemDefinitionException, TransitionSystenExecutionException, FileNotFoundException, UnresolvedFExpression, ConstraintSolvingException {

        FeatureModel<?> fm = XmlLoaders.loadFeatureModel(new File(FM_PATH + fileName + ".xml"));
        FeaturedTransitionSystem fts = loadFeaturedTransitionSystem(FTS_PATH + fileName + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();
        FeaturedTransitionSystem result = composer.compose(fts, fts, true);

        assertEquivalent(fm, fts, result, "Idempotency violated for " + fileName);
    }

    /*
     * -------------------------
     * INITIAL STATE
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testInitialState(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException {

        FeaturedTransitionSystem ts1 = loadFeaturedTransitionSystem(FTS_PATH + f1 + FTS_EXT);
        FeaturedTransitionSystem ts2 = loadFeaturedTransitionSystem(FTS_PATH + f2 + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();
        FeaturedTransitionSystem result = composer.compose(ts1, ts2, sync);

        String expectedInit = ts1.getInitialState().getName() + "||" + ts2.getInitialState().getName();

        assertEquals(expectedInit, result.getInitialState().getName());
    }

    /*
     * -------------------------
     * Feature expression conjunction if sync
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testSyncFeatureConjunctionStrong(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException {

        if (!sync) return;

        FeaturedTransitionSystem ts1 = loadFeaturedTransitionSystem(FTS_PATH + f1 + FTS_EXT);
        FeaturedTransitionSystem ts2 = loadFeaturedTransitionSystem(FTS_PATH + f2 + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();
        FeaturedTransitionSystem result = composer.compose(ts1, ts2, true);

        for (Iterator<Transition> it = result.transitions(); it.hasNext(); ) {
            Transition t = it.next();

            State src = t.getSource();
            State tgt = t.getTarget();
            String action = t.getAction().getName();

            // Only consider sync transitions: both sides must move
            if (!src.getName().contains("||") || !tgt.getName().contains("||")) continue;

            String[] srcParts = src.getName().split("\\|\\|");
            String[] tgtParts = tgt.getName().split("\\|\\|");

            String s1 = srcParts[0];
            String s2 = srcParts[1];
            String t1Name = tgtParts[0];
            String t2Name = tgtParts[1];

            // Skip async transitions (one side stays the same)
            boolean leftMoves  = !s1.equals(t1Name);
            boolean rightMoves = !s2.equals(t2Name);

            if (!(leftMoves && rightMoves)) continue;

            // Find matching transitions in original TS
            Transition leftTransition  = getUniqueTransition(ts1, s1, action, t1Name);
            Transition rightTransition = getUniqueTransition(ts2, s2, action, t2Name);

            FExpression expected = ts1.getFExpression(leftTransition).and(ts2.getFExpression(rightTransition)).applySimplification();
            FExpression actual = result.getFExpression(t);

            assertEquals(expected, actual, "Mismatch in feature expression for transition: " + t);
        }
    }

    /*
     * -------------------------
     * FEATURE PRUNING
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testNoFalseFeatureTransitionsAreCreated(String f1, String f2, boolean sync) throws TransitionSystemDefinitionException {

        FeaturedTransitionSystem ts1 = loadFeaturedTransitionSystem(FTS_PATH + f1 + FTS_EXT);
        FeaturedTransitionSystem ts2 = loadFeaturedTransitionSystem(FTS_PATH + f2 + FTS_EXT);

        FTSParallelComposer composer = new FTSParallelComposer();
        FeaturedTransitionSystem result = composer.compose(ts1, ts2, sync);

        for (Iterator<Transition> it = result.transitions(); it.hasNext(); ) {
            FExpression expr = result.getFExpression(it.next());
            assert !expr.isFalse() : "Found pruned (false) transition in result!";
        }
    }

    private Transition getUniqueTransition(FeaturedTransitionSystem ts, String source, String action, String target) {
        Iterator<Transition> it = ts.getTransitions(source, action, target);
        assertTrue(it.hasNext(), "No matching transition for (" + source + ", " + action + ", " + target + ")");
        Transition t = it.next();
        assertFalse(it.hasNext(), "Multiple matching transitions for (" + source + ", " + action + ", " + target + ")");
        return t;
    }
}