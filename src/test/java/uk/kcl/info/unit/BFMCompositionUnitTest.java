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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import uk.kcl.info.bfm.*;
import uk.kcl.info.bfm.compositions.BFMParallelComposer;
import uk.kcl.info.bfm.execution.FeaturedEventStructureExecutor;
import uk.kcl.info.bfm.io.xml.XmlSaverUtility;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.kcl.info.bfm.compositions.AbstractBESParallelComposer.getStarSymbol;
import static uk.kcl.info.bfm.io.xml.XmlLoaderUtility.loadBehavioralFeatureModel;

public class BFMCompositionUnitTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String BFM_PATH = BASE_PATH + "bfm/";
    private static final String BFM_EXT = ".bfm";

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
                Arguments.of("coffee", "soup", true)/*,
                Arguments.of("coffee", "soda", false),
                Arguments.of("soda", "soup", false),
                Arguments.of("coffee", "soup", false)*/
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

    private static void assertEquivalent(BehavioralFeatureModel left, BehavioralFeatureModel right, String message) {

        // Executors
        FeaturedEventStructureExecutor leftExec = new FeaturedEventStructureExecutor(left);
        FeaturedEventStructureExecutor rightExec = new FeaturedEventStructureExecutor(right);

        // Sample traces
        Map<FExpression, Set<List<String>>> leftTraces = leftExec.getRandomActionTraces(MAX_TRACES, MAX_ATTEMPTS);
        Map<FExpression, Set<List<String>>> rightTraces = rightExec.getRandomActionTraces(MAX_TRACES, MAX_ATTEMPTS);

        // Left ⊆ Right
        Map<FExpression, Set<List<String>>> missingLR = rightExec.notExecutableActions(leftTraces); //notExecutable(leftTraces, rightExec);
        // Right ⊆ Left
        Map<FExpression, Set<List<String>>> missingRL = leftExec.notExecutableActions(rightTraces); //notExecutable(rightTraces, leftExec);

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
    public void testParallelComposition(String f1, String f2, boolean sync) {

        System.out.println("Testing parallel composition...");
        BehavioralFeatureModel bfm1 = loadBehavioralFeatureModel(BFM_PATH + f1 + BFM_EXT);
        BehavioralFeatureModel bfm2 = loadBehavioralFeatureModel(BFM_PATH + f2 + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();
        BehavioralFeatureModel result = composer.compose(bfm1, bfm2, sync);

        String suffix = sync ? "_sync" : "_async";
        //XmlSaverUtility.save(result, BFM_PATH + f1 + f2 + suffix + BFM_EXT);
        BehavioralFeatureModel expected = loadBehavioralFeatureModel(BFM_PATH + f1 + f2 + suffix + BFM_EXT);

        assertEquivalent(expected, result, "Parallel composition mismatch (" + f1 + ", " + f2 + ", sync=" + sync + ")");
    }

    /*
     * -------------------------
     * ALPHABET CHECK
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testAlphabetIsUnion(String f1, String f2, boolean sync) {

        System.out.println("Testing alphabet isUnion...");
        BehavioralFeatureModel bfm1 = loadBehavioralFeatureModel(BFM_PATH + f1 + BFM_EXT);
        BehavioralFeatureModel bfm2 = loadBehavioralFeatureModel(BFM_PATH + f2 + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();
        BehavioralFeatureModel result = composer.compose(bfm1, bfm2, sync);

        Set<String> sigma1 = new HashSet<>(bfm1.getAllActions());
        Set<String> sigma2 = new HashSet<>(bfm2.getAllActions());
        Set<String> sigmaC = new HashSet<>(result.getAllActions());

        Set<String> expectedUnion = new HashSet<>(sigma1);
        expectedUnion.addAll(sigma2);

        // ✔️ main check
        assertEquals(expectedUnion, sigmaC, "Composed BFM alphabet is not equal to Σ1 ∪ Σ2");

        // ✔️ optional sanity checks
        assertTrue(sigmaC.containsAll(sigma1), "Composed BFM is missing actions from BFM1");
        assertTrue(sigmaC.containsAll(sigma2), "Composed BFM is missing actions from BFM2");
    }

    /*
     * -------------------------
     * Commutativity
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativity(String f1, String f2, boolean sync) {

        System.out.println("Testing commutativity...");

        BehavioralFeatureModel bfm1 = loadBehavioralFeatureModel(BFM_PATH + f1 + BFM_EXT);
        BehavioralFeatureModel bfm2 = loadBehavioralFeatureModel(BFM_PATH + f2 + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();

        BehavioralFeatureModel res1 = composer.compose(bfm1, bfm2, sync);
        BehavioralFeatureModel res2 = composer.compose(bfm2, bfm1, sync);

        assertEquivalent(res1, res2, "Commutativity violated (" + f1 + ", " + f2 + ", sync=" + sync + ")");
    }

    /*
     * -------------------------
     * Associativity
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(booleans = {true}) //, false // Associativity only works for sync events
    public void testAssociativity(boolean sync) {

        System.out.println("Testing associativity...");
        String f1 = "coffee";
        String f2 = "soda";
        String f3 = "soup";

        BehavioralFeatureModel bfm1 = loadBehavioralFeatureModel(BFM_PATH + f1 + BFM_EXT);
        BehavioralFeatureModel bfm2 = loadBehavioralFeatureModel(BFM_PATH + f2 + BFM_EXT);
        BehavioralFeatureModel bfm3 = loadBehavioralFeatureModel(BFM_PATH + f3 + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();

        // (A || B) || C
        BehavioralFeatureModel left = composer.compose(composer.compose(bfm1, bfm2, sync), bfm3, sync);
        // A || (B || C)
        BehavioralFeatureModel right = composer.compose(bfm1, composer.compose(bfm2, bfm3, sync), sync);

        assertEquivalent(left, right,"Associativity violated for sync=" + sync);
    }

    /*
     * -------------------------
     * Idempotency
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(strings = {"coffee","soda", "soup"})
    public void testIdempotency(String fileName) {

        System.out.println("Testing idempotency...");
        BehavioralFeatureModel bfm = loadBehavioralFeatureModel(BFM_PATH + fileName + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();
        BehavioralFeatureModel result = composer.compose(bfm, bfm, true);

        assertEquivalent(bfm, result, "Idempotency violated for " + fileName);
    }

    /*
     * -------------------------
     * LCA
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testSyncFeatureIsLCA(String f1, String f2) {

        BehavioralFeatureModel bfm1 = loadBehavioralFeatureModel(BFM_PATH + f1 + BFM_EXT);
        BehavioralFeatureModel bfm2 = loadBehavioralFeatureModel(BFM_PATH + f2 + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();
        BehavioralFeatureModel result = composer.compose(bfm1, bfm2, true);

        for (Event e : result.getAllEvents()) {
            if (e.getName().contains("||")) {

                String[] parts = e.getName().split("\\|\\|", 2);

                String leftName = parts[0].trim();
                String rightName = parts[1].trim();

                Event left = leftName.equals(getStarSymbol()) ? null : bfm1.getEvent(leftName);
                Event right = rightName.equals(getStarSymbol()) ? null : bfm2.getEvent(rightName);

                BehavioralFeature expectedLCA;

                if(left == null){
                    expectedLCA = bfm2.getFeature(right);
                }else if (right  == null) {
                    expectedLCA = bfm1.getFeature(left);
                } else {
                    expectedLCA = bfm1.getLeastCommonAncestor(bfm1.getFeature(left), bfm2.getFeature(right));
                }

                assertEquals(expectedLCA, result.getFeature(e));
            }
        }
    }

    /*
     * -------------------------
     * Feature expression conjunction if sync
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testSyncFeatureExpressionConjunction(String f1, String f2, boolean sync) {

        System.out.println("Testing syncFeatureExpressionConjunction...");
        if (!sync) return; // only meaningful for sync

        BehavioralFeatureModel bfm1 = loadBehavioralFeatureModel(BFM_PATH + f1 + BFM_EXT);
        BehavioralFeatureModel bfm2 = loadBehavioralFeatureModel(BFM_PATH + f2 + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();
        BehavioralFeatureModel result = composer.compose(bfm1, bfm2, true);

        for (Event e : result.getAllEvents()) {

            // Only check sync events: e1||e2 (not * cases)
            if (!e.getName().contains("||") || e.getName().contains(getStarSymbol())) continue;

            String[] parts = e.getName().split("\\|\\|");
            Event e1 = bfm1.getEvent(parts[0]);
            Event e2 = bfm2.getEvent(parts[1]);

            FExpression expected = bfm1.getFExpression(e1).and(bfm2.getFExpression(e2)).applySimplification().toCnf();
            FExpression actual = result.getFExpression(e).applySimplification().toCnf();
            assertEquals(expected, actual, "Mismatch in feature expression for event " + e.getName());
        }
    }

    /*
     * -------------------------
     * Pruning
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testNoFalseFeatureExpressions(String f1, String f2, boolean sync) {

        System.out.println("Testing noFalseFeatureExpressions...");
        BehavioralFeatureModel bfm1 = loadBehavioralFeatureModel(BFM_PATH + f1 + BFM_EXT);
        BehavioralFeatureModel bfm2 = loadBehavioralFeatureModel(BFM_PATH + f2 + BFM_EXT);

        BFMParallelComposer composer = new BFMParallelComposer();
        BehavioralFeatureModel result = composer.compose(bfm1, bfm2, sync);

        for (Event e : result.getAllEvents()) {
            FExpression expr = result.getFExpression(e);
            assert !expr.isFalse() : "Found event with FALSE feature expression: " + e.getName();
        }
    }
}

