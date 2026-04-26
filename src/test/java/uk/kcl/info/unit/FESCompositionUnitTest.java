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
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import be.vibes.ts.exception.UnresolvedFExpression;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.kcl.info.bfm.Event;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.compositions.FESParallelComposer;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static uk.kcl.info.bfm.compositions.AbstractBESParallelComposer.getStarSymbol;
import static uk.kcl.info.bfm.io.xml.XmlLoaderUtility.loadFeaturedEventStructure;

import uk.kcl.info.bfm.execution.FeaturedEventStructureExecutor;

//@Disabled("Temporarily disabled while refactoring trace semantics")
public class FESCompositionUnitTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String FM_PATH = BASE_PATH + "fm/xml/";
    private static final String FES_PATH = BASE_PATH + "fes/";
    private static final String FES_EXT = ".fes";

    // --- sampling parameters ---
    private static final int MAX_CONFIG = 5;
    private static final int MAX_TRACES = 500;
    private static final long TIMEOUT_MILLIS = 100;// 1000;
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

    private static String summarize(String title, Map<Configuration, Set<List<Event>>> traces) {
        StringBuilder sb = new StringBuilder(title).append(" (").append(traces.size()).append(")\n");

        int i = 0;
        for (Map.Entry<Configuration, Set<List<Event>>> t: traces.entrySet()) {
            if (i++ >= FESCompositionUnitTest.LOG_LIMIT) break;
            sb.append("  ").append(t).append("\n");
        }
        return sb.toString();
    }

    private static void assertEquivalent(FeatureModel<?> fm, FeaturedEventStructure<?> left, FeaturedEventStructure<?> right, String message) throws TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        // Executors
        FeaturedEventStructureExecutor leftExec = new FeaturedEventStructureExecutor(left, fm);
        FeaturedEventStructureExecutor rightExec = new FeaturedEventStructureExecutor(right, fm);

        // Sample traces
        Map<Configuration, Set<List<Event>>> leftTraces = leftExec.getRandomEventTraces(MAX_CONFIG, TIMEOUT_MILLIS, MAX_TRACES);
        Map<Configuration, Set<List<Event>>> rightTraces = rightExec.getRandomEventTraces(MAX_CONFIG, TIMEOUT_MILLIS, MAX_TRACES);

        // Left ⊆ Right
        Map<Configuration, Set<List<Event>>> missingLR = notExecutable(leftTraces, rightExec);
        // Right ⊆ Left
        Map<Configuration, Set<List<Event>>> missingRL = notExecutable(rightTraces, leftExec);

        assertTrue(missingLR.isEmpty() && missingRL.isEmpty(),
                () -> message + "\n"
                        + summarize("Missing Left→Right", missingLR)
                        + summarize("Missing Right→Left", missingRL));
    }

    private static Map<Configuration, Set<List<Event>>> notExecutable(Map<Configuration, Set<List<Event>>> traces, FeaturedEventStructureExecutor exec) throws UnresolvedFExpression {

        Map<Configuration, Set<List<Event>>> nonExecutableTraces = new HashMap<>();

        for(Map.Entry<Configuration, Set<List<Event>>> setOfTrace: traces.entrySet()){
            for(List<Event> t: setOfTrace.getValue()){
                if(!exec.canExecute(setOfTrace.getKey(), t)){
                    nonExecutableTraces.put(setOfTrace.getKey(), setOfTrace.getValue());
                }
            }
        }

        return nonExecutableTraces;
    }

    /*
     * -------------------------
     * Composition (SYNC and ASYNC)
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public <F extends Feature<F>> void testParallelComposition(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException, UnresolvedFExpression, ConstraintSolvingException, TransitionSystenExecutionException {

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union" + ".xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);
        FeaturedEventStructure<F> result = composer.compose(fes1, fes2, sync);

        String suffix = sync ? "_sync" : "_async";
        FeaturedEventStructure<F> expected = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + f2 + suffix + FES_EXT, fm);

        assertEquivalent(fm, expected, result, "Parallel composition mismatch (" + f1 + ", " + f2 + ", sync=" + sync + ")");

        //assertEquals(expected, result, "Parallel composition mismatch (" + f1 + ", " + f2 + ", sync=" + sync + ")");

        /*
        Map<Configuration, Set<List<String>>> expectedTraces = new FeaturedEventStructureExecutor(expected, fm).getAllActionTraces();
        Map<Configuration, Set<List<String>>> resultTraces = new FeaturedEventStructureExecutor(result, fm).getAllActionTraces();
        assertEquals(expectedTraces, resultTraces);*/
    }

    /*
     * -------------------------
     * ALPHABET CHECK
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public <F extends Feature<F>> void testAlphabetIsUnion(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException {

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union" + ".xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);
        FeaturedEventStructure<F> result = composer.compose(fes1, fes2, sync);

        Set<String> sigma1 = new HashSet<>(fes1.getAllActions());
        Set<String> sigma2 = new HashSet<>(fes2.getAllActions());
        Set<String> sigmaC = new HashSet<>(result.getAllActions());

        Set<String> expectedUnion = new HashSet<>(sigma1);
        expectedUnion.addAll(sigma2);

        // ✔️ main check
        assertEquals(expectedUnion, sigmaC, "Composed FES alphabet is not equal to Σ1 ∪ Σ2");

        // ✔️ optional sanity checks
        assertTrue(sigmaC.containsAll(sigma1), "Composed FES is missing actions from FES1");
        assertTrue(sigmaC.containsAll(sigma2), "Composed FES is missing actions from FES2");
    }

    /*
     * -------------------------
     * Commutativity
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public <F extends Feature<F>> void testCommutativity(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException, TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union" + ".xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);

        FeaturedEventStructure<F> res1 = composer.compose(fes1, fes2, sync);
        FeaturedEventStructure<F> res2 = composer.compose(fes2, fes1, sync);

        //assertEquals(res1, res2, "Commutativity violated (" + f1 + ", " + f2 + ", sync=" + sync + ")");
        /*
        Map<Configuration, Set<List<String>>> res1Traces = new FeaturedEventStructureExecutor(res1, fm).getAllActionTraces();
        Map<Configuration, Set<List<String>>> res2Traces = new FeaturedEventStructureExecutor(res2, fm).getAllActionTraces();
        assertEquals(res1Traces, res2Traces);
        */

        assertEquivalent(fm, res1, res2, "Commutativity violated (" + f1 + ", " + f2 + ", sync=" + sync + ")");
    }

    /*
     * -------------------------
     * Idempotency
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(strings = {"coffee","soda", "soup"})
    public <F extends Feature<F>> void testIdempotency(String fileName) throws BundleEventStructureDefinitionException, TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + fileName + ".xml"));
        FeaturedEventStructure<F> fes = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + fileName + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);
        FeaturedEventStructure<F> result = composer.compose(fes, fes, true);

        /*
        Map<Configuration, Set<List<String>>> expectedTraces = new FeaturedEventStructureExecutor(fes, fm).getAllActionTraces();
        Map<Configuration, Set<List<String>>> resultTraces = new FeaturedEventStructureExecutor(result, fm).getAllActionTraces();
        assertEquals(expectedTraces, resultTraces);
        */

        assertEquivalent(fm, fes, result, "Idempotency violated for " + fileName);

        //assertEquals(fes, result, "Idempotency violated for " + fileName);
    }

    /*
     * -------------------------
     * LCA
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public <F extends Feature<F>> void testSyncFeatureIsLCA(String f1, String f2) throws BundleEventStructureDefinitionException {

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union.xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);
        FeaturedEventStructure<F> result = composer.compose(fes1, fes2, true);

        for (Event e : result.getAllEvents()) {
            if (e.getName().contains("||")) {

                String[] parts = e.getName().split("\\|\\|", 2);

                String leftName = parts[0].trim();
                String rightName = parts[1].trim();

                Event left = leftName.equals(getStarSymbol()) ? null : fes1.getEvent(leftName);
                Event right = rightName.equals(getStarSymbol()) ? null : fes2.getEvent(rightName);

                F expectedLCA;

                if(left == null){
                    expectedLCA = fes2.getFeature(right);
                }else if (right  == null) {
                    expectedLCA = fes1.getFeature(left);
                } else {
                    expectedLCA = fm.getLeastCommonAncestor(fes1.getFeature(left), fes2.getFeature(right));
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
    public <F extends Feature<F>> void testSyncFeatureExpressionConjunction(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException {

        if (!sync) return; // only meaningful for sync

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union.xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);
        FeaturedEventStructure<F> result = composer.compose(fes1, fes2, true);

        for (Event e : result.getAllEvents()) {

            // Only check sync events: e1||e2 (not * cases)
            if (!e.getName().contains("||") || e.getName().contains(getStarSymbol())) continue;

            String[] parts = e.getName().split("\\|\\|");
            Event e1 = fes1.getEvent(parts[0]);
            Event e2 = fes2.getEvent(parts[1]);

            FExpression expected = fes1.getFExpression(e1).and(fes2.getFExpression(e2)).applySimplification().toCnf();
            FExpression actual = result.getFExpression(e).applySimplification().toCnf();
            assertEquals(expected, actual, "Mismatch in feature expression for event " + e.getName());
        }
    }

    /*
     * -------------------------
     * Associativity
     * -------------------------
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public <F extends Feature<F>> void testAssociativity(boolean sync) throws BundleEventStructureDefinitionException, TransitionSystenExecutionException, UnresolvedFExpression, ConstraintSolvingException {

        String f1 = "coffee";
        String f2 = "soda";
        String f3 = "soup";

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + "svm_union.xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);
        FeaturedEventStructure<F> fes3 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f3 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);

        // (A || B) || C
        FeaturedEventStructure<F> left = composer.compose(composer.compose(fes1, fes2, sync), fes3, sync);
        // A || (B || C)
        FeaturedEventStructure<F> right = composer.compose(fes1, composer.compose(fes2, fes3, sync), sync);

        //Map<Configuration, Set<List<String>>> leftTraces = new FeaturedEventStructureExecutor(left, fm).getAllActionTraces();
        //Map<Configuration, Set<List<String>>> rightTraces = new FeaturedEventStructureExecutor(right, fm).getAllActionTraces();
        //assertEquals(leftTraces, rightTraces, "Associativity violated for sync=" + sync);
        assertEquivalent(fm,left,right,"Associativity violated for sync=\" + sync");
        //assertEquals(left, right, "Associativity violated for sync=" + sync);
    }

    /*
     * -------------------------
     * Pruning
     * -------------------------
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public <F extends Feature<F>> void testNoFalseFeatureExpressions(String f1, String f2, boolean sync) throws BundleEventStructureDefinitionException {

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union.xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);
        FeaturedEventStructure<F> result = composer.compose(fes1, fes2, sync);

        for (Event e : result.getAllEvents()) {
            FExpression expr = result.getFExpression(e);
            assert !expr.isFalse() : "Found event with FALSE feature expression: " + e.getName();
        }
    }

    /*
     * -------------------------
     * Sync ⊆ Async
     * -------------------------
     */
    /*
    @ParameterizedTest
    @MethodSource("testCases")
    public <F extends Feature<F>> void testSyncSubsetAsync(String f1, String f2, boolean ignoredSync) throws
            BundleEventStructureDefinitionException, UnresolvedFExpression, ConstraintSolvingException {

        FeatureModel<F> fm = (FeatureModel<F>) XmlLoaders.loadFeatureModel(new File(FM_PATH + f1 + f2 + "_union.xml"));
        FeaturedEventStructure<F> fes1 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f1 + FES_EXT, fm);
        FeaturedEventStructure<F> fes2 = (FeaturedEventStructure<F>) loadFeaturedEventStructure(FES_PATH + f2 + FES_EXT, fm);

        FESParallelComposer<F> composer = new FESParallelComposer<>(fm);
        FeaturedEventStructure<F> async = composer.compose(fes1, fes2, false);
        FeaturedEventStructure<F> sync  = composer.compose(fes1, fes2, true);

        Map<Configuration, Set<List<String>>> asyncTraces = new FeaturedEventStructureExecutor(async, fm).getAllActionTraces();
        Map<Configuration, Set<List<String>>> syncTraces = new FeaturedEventStructureExecutor(sync, fm).getAllActionTraces();

        // Check inclusion for each configuration
        for (Map.Entry<Configuration, Set<List<String>>> entry : syncTraces.entrySet()) {
            Configuration cfg = entry.getKey();
            Set<List<String>> syncSet = entry.getValue();
            Set<List<String>> asyncSet = asyncTraces.getOrDefault(cfg, Collections.emptySet());

            Set<List<String>> missing = new HashSet<>(syncSet);
            missing.removeAll(asyncSet);

            assertTrue(missing.isEmpty(),
                    () -> "Missing traces in async for configuration: " + cfg +
                            "\nMissing: " + missing +
                            "\nSync: " + syncSet +
                            "\nAsync: " + asyncSet);
        }
    }*/
}

