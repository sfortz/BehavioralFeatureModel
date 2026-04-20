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
import be.vibes.solver.FeatureModel;
import be.vibes.solver.Group;
import be.vibes.solver.io.xml.XmlLoaders;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.kcl.info.bfm.compositions.FMMerger;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FMCompositionUnitTest {

    private static final String BASE_PATH = "src/test/resources/testcases/";
    private static final String FM_IN_PATH = BASE_PATH + "fm/xml/";
    private static final String XML_EXT = ".xml";

    static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of("coffee", "soda"),
                Arguments.of("soda", "soup"),
                Arguments.of("coffee", "soup")
        );
    }

    private static String groupSignature(Group<?> g) {
        List<String> names = g.getFeatures().stream().map(Feature::getFeatureName).sorted().toList();
        return String.join(",", names);
    }

    private static String canonical(FeatureModel<?> fm) {
        return canonical(fm.getRootFeature());
    }

    private static String canonical(Feature<?> f) {
        StringBuilder sb = new StringBuilder();

        sb.append(f.getFeatureName());

        List<Group<?>> groups = new ArrayList<>(f.getChildren());

        // Sort groups deterministically
        groups.sort(Comparator.comparing((Group<?> g) -> g.GROUPTYPE.name()).thenComparing(FMCompositionUnitTest::groupSignature));

        for (Group<?> g : groups) {
            sb.append("{").append(g.GROUPTYPE).append(":[");

            List<Feature<?>> children = new ArrayList<>(g.getFeatures());

            // Sort children recursively by canonical form
            children.sort(Comparator.comparing(FMCompositionUnitTest::canonical));

            for (Feature<?> child : children) {
                sb.append(canonical(child)).append(",");
            }

            sb.append("]}");
        }

        return sb.toString();
    }

    private static List<FExpression> normalizeConstraints(FeatureModel<? extends Feature<?>> fm) {
        return fm.getOwnConstraints().stream().map(FExpression::applySimplification).sorted(Comparator.comparing(Object::toString)).toList();
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativeUnionStructure(String f1FileName, String f2FileName) {

        FeatureModel<? extends Feature<?>> fm1 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f1FileName + XML_EXT);
        FeatureModel<? extends Feature<?>> fm2 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f2FileName + XML_EXT);

        FMMerger merger = new FMMerger();
        FeatureModel<? extends Feature<?>> fm1fm2 = merger.compose(fm1, fm2, true);
        FeatureModel<? extends Feature<?>> fm2fm1 = merger.compose(fm2, fm1, true);

        assertEquals(canonical(fm1fm2), canonical(fm2fm1));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testCommutativeUnionConstraints(String f1FileName, String f2FileName) {

        FeatureModel<? extends Feature<?>> fm1 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f1FileName + XML_EXT);
        FeatureModel<? extends Feature<?>> fm2 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f2FileName + XML_EXT);

        FMMerger merger = new FMMerger();
        FeatureModel<? extends Feature<?>> fm1fm2 = merger.compose(fm1, fm2, true);
        FeatureModel<? extends Feature<?>> fm2fm1 = merger.compose(fm2, fm1, true);

        List<FExpression> fm1fm2Constraints = normalizeConstraints(fm1fm2);
        List<FExpression> fm2fm1Constraints = normalizeConstraints(fm2fm1);

        assertEquals(fm1fm2Constraints, fm2fm1Constraints);
    }

    @ParameterizedTest
    @ValueSource(strings = {"coffee","soda","soup"})
    public void testUnionIdempotentStructure(String fileName) {

        FeatureModel<? extends Feature<?>> fm = XmlLoaders.loadFeatureModel(FM_IN_PATH + fileName + XML_EXT);

        FMMerger merger = new FMMerger();
        FeatureModel<? extends Feature<?>> result = merger.compose(fm, fm, true);

        assertEquals(canonical(fm), canonical(result));
    }

    @ParameterizedTest
    @ValueSource(strings = {"coffee","soda","soup"})
    public void testUnionIdempotentConstraints(String fileName) {

        FeatureModel<? extends Feature<?>> fm = XmlLoaders.loadFeatureModel(FM_IN_PATH + fileName + XML_EXT);

        FMMerger merger = new FMMerger();
        FeatureModel<? extends Feature<?>> result = merger.compose(fm, fm, true);

        List<FExpression> resultConstraints = normalizeConstraints(result);
        List<FExpression> expectedConstraints = normalizeConstraints(fm);

        assertEquals(expectedConstraints, resultConstraints);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testFMUnionStructure(String f1FileName, String f2FileName) {

        FeatureModel<? extends Feature<?>> fm1 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f1FileName + XML_EXT);
        FeatureModel<? extends Feature<?>> fm2 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f2FileName + XML_EXT);

        FMMerger merger = new FMMerger();
        FeatureModel<? extends Feature<?>> result = merger.compose(fm1, fm2, true);

        String expectedFileName = f1FileName + f2FileName + "_union";
        FeatureModel<? extends Feature<?>> expected = XmlLoaders.loadFeatureModel(FM_IN_PATH + expectedFileName + XML_EXT);

        assertEquals(canonical(expected), canonical(result));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testFMUnionConstraints(String f1FileName, String f2FileName) {

        FeatureModel<? extends Feature<?>> fm1 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f1FileName + XML_EXT);
        FeatureModel<? extends Feature<?>> fm2 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f2FileName + XML_EXT);

        FMMerger merger = new FMMerger();
        FeatureModel<? extends Feature<?>> result = merger.compose(fm1, fm2, true);

        String expectedFileName = f1FileName + f2FileName + "_union";
        FeatureModel<? extends Feature<?>> expected = XmlLoaders.loadFeatureModel(FM_IN_PATH + expectedFileName + XML_EXT);

        List<FExpression> resultConstraints = normalizeConstraints(result);
        List<FExpression> expectedConstraints = normalizeConstraints(expected);

        assertEquals(expectedConstraints, resultConstraints);
    }

    /*  TODO: once we add all possible merging mode: union, strict union, intersection or diff (see Mathieu Acher's PhD)
    @ParameterizedTest
    @MethodSource("testCases")
    public void testFMIntersectionMerger(String f1FileName, String f2FileName) {

        FeatureModel<? extends Feature<?>> fm1 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f1FileName + XML_EXT);
        FeatureModel<? extends Feature<?>> fm2 = XmlLoaders.loadFeatureModel(FM_IN_PATH + f2FileName + XML_EXT);

        FMMerger merger = new FMMerger();
        FeatureModel<? extends Feature<?>> result = merger.compose(fm1, fm2, false);

        String expectedFileName = f1FileName + f2FileName + "_intersection";
        FeatureModel<? extends Feature<?>> expected = XmlLoaders.loadFeatureModel(FM_IN_PATH + expectedFileName + XML_EXT);

        assertEquals(canonical(expected), canonical(result));
        }
    }
    */
}
