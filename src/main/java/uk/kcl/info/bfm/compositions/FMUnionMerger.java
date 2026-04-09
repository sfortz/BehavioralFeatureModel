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

package uk.kcl.info.bfm.compositions;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.*;
import be.vibes.solver.Group.GroupType;

import java.util.*;

/**
 * Feature Model merger.
 * mode = true  → UNION
 * mode = false → INTERSECTION
 */
public class FMUnionMerger implements Composition<FeatureModel<? extends Feature<?>>> {

    @Override
    public FeatureModel<?> compose(FeatureModel<? extends Feature<?>> m1, FeatureModel<? extends Feature<?>> m2, boolean mode) {

        if (m1 == null || m2 == null) {
            throw new IllegalArgumentException("FeatureModels cannot be null");
        }

        Feature<?> root1 = m1.getRootFeature();
        Feature<?> root2 = m2.getRootFeature();

        if (root1 == null || root2 == null) {
            throw new IllegalArgumentException("FeatureModels must have a root feature");
        }

        // 1. Merge the two trees
        Feature<? extends Feature<?>> mergedRoot = merge(root1, root2, mode);

        // 2. Build the resulting FeatureModel
        FeatureModelFactory factory = new FeatureModelFactory<>();

        factory.setRootFeature(mergedRoot);

        // 3. Merge constraints
        for(FExpression constr: m1.getOwnConstraints()){
            factory.addConstraint(mergedRoot, constr);
        }
        for(FExpression constr: m2.getOwnConstraints()){
            factory.addConstraint(mergedRoot, constr);
        }

        return factory.build();
    }

    private Feature<?> merge(Feature<?> baseFeature, Feature<?> aspectFeature, boolean mode) {

        if (!baseFeature.getFeatureName().equals(aspectFeature.getFeatureName())) {
            throw new IllegalArgumentException("Only features with the same name can be merged!");
        }

        Feature mergedFeature = new Feature(baseFeature.getFeatureName());

        List<GroupPairer.Pair> pairedGroups = new GroupPairer().pairGroups(baseFeature, aspectFeature);

        for (GroupPairer.Pair pair : pairedGroups) {

            if (pair.left != null && pair.right != null) {

                Set<Feature<?>> leftOnlyFeatures = new HashSet<>(pair.left.getFeatures());
                Set<Feature<?>> rightOnlyFeatures = new HashSet<>(pair.right.getFeatures());
                GroupType op = computeOperator(pair.left.GROUPTYPE, pair.right.GROUPTYPE, mode);

                // 1. matched features
                for (Object o : pair.getMatches().values()) {
                    GroupPairer.FeatureMatch match = (GroupPairer.FeatureMatch) o;

                    Feature<?> left = (Feature<?>) match.left;
                    Feature<?> right = (Feature<?>) match.right;

                    leftOnlyFeatures.remove(left);
                    rightOnlyFeatures.remove(right);

                    Feature<?> mergedChild = merge(left, right, mode);
                    addFeatureWithCorrectGrouping(mergedFeature, (Feature) mergedChild, op);
                }

                // 2. unmatched features (UNION)
                if (mode) {
                    for (Feature<?> f : leftOnlyFeatures) {
                        GroupType type = computeOperator(pair.left.GROUPTYPE, GroupType.OPTIONAL, true); // <-- pretend "absent" = OPTIONAL
                        addFeatureWithCorrectGrouping(mergedFeature, (Feature) f.clone(), type);
                    }

                    for (Feature<?> f : rightOnlyFeatures) {
                        GroupType type = computeOperator(GroupType.OPTIONAL, pair.right.GROUPTYPE, true);
                        addFeatureWithCorrectGrouping(mergedFeature, (Feature) f.clone(), type);
                    }
                }

            } else if (mode && pair.left != null) {
                // TODO: check precondition: the intersection between the set of features of the base FM and the one of the aspect FM is empty.
                Group<?> cloned = pair.left.clone();
                mergedFeature.addChildren(cloned);

            } else if (mode && pair.right != null) {
                // TODO: check precondition: the intersection between the set of features of the base FM and the one of the aspect FM is empty.
                Group<?> cloned = pair.right.clone();
                mergedFeature.addChildren(cloned);
            }
        }

        return mergedFeature;
    }

    private static final GroupType[][][] OP_TABLE = {
            // INTERSECTION (mode=false)
            {
                    /* MANDATORY */ {GroupType.MANDATORY, GroupType.MANDATORY, GroupType.MANDATORY, GroupType.MANDATORY},
                    /* OPTIONAL  */ {GroupType.MANDATORY, GroupType.OPTIONAL,  GroupType.ALTERNATIVE, GroupType.OR},
                    /* ALTERNATIVE */ {GroupType.MANDATORY, GroupType.ALTERNATIVE, GroupType.ALTERNATIVE, GroupType.ALTERNATIVE},
                    /* OR        */ {GroupType.MANDATORY, GroupType.OR,        GroupType.ALTERNATIVE, GroupType.OR}
            },
            // UNION (mode=true)
            {
                    /* MANDATORY */ {GroupType.MANDATORY, GroupType.OPTIONAL, GroupType.OR,     GroupType.OR},
                    /* OPTIONAL  */ {GroupType.OPTIONAL,  GroupType.OPTIONAL, GroupType.OPTIONAL,GroupType.OPTIONAL},
                    /* ALTERNATIVE */ {GroupType.OR,        GroupType.OPTIONAL, GroupType.ALTERNATIVE, GroupType.OR},
                    /* OR        */ {GroupType.OR,        GroupType.OPTIONAL, GroupType.OR,     GroupType.OR}
            }
    };

    private static int idx(GroupType t) {
        return switch (t) {
            case MANDATORY -> 0;
            case OPTIONAL -> 1;
            case ALTERNATIVE -> 2;
            case OR -> 3;
            default -> throw new IllegalArgumentException("Cardinalities are not yet supported in FM!");
        };
    }

    private GroupType computeOperator(GroupType baseType, GroupType aspectType, boolean mode) {
        return OP_TABLE[mode ? 1 : 0][idx(baseType)][idx(aspectType)];
    }

    private <T extends Feature<T>> void addFeatureWithCorrectGrouping(T parent, T child, GroupType type) {

        switch (type) {
            case MANDATORY, OPTIONAL -> {
                Group<T> group = new Group<>(type);
                group.getFeatures().add(child);
                parent.addChildren(group);
            }

            case OR, ALTERNATIVE -> {
                Optional<Group<T>> existing = parent.getChildren().stream().filter(g -> g.GROUPTYPE == type).findFirst();

                if (existing.isPresent()) {
                    existing.get().getFeatures().add(child);
                } else {
                    Group<T> group = new Group<>(type);
                    group.getFeatures().add(child);
                    parent.addChildren(group);
                }
            }
        }
    }
}