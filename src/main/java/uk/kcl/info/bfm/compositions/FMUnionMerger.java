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

import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.FeatureModelFactory;
import be.vibes.solver.Group;
import be.vibes.solver.Group.GroupType;

import java.util.*;

/**
 * Feature Model merger.
 * mode = true  → UNION
 * mode = false → INTERSECTION
 */
public class FMUnionMerger<T extends Feature<T>> implements Composition<FeatureModel<T>> {

    @Override
    public FeatureModel<T> compose(FeatureModel<T> m1, FeatureModel<T> m2, boolean mode) {

        if (m1 == null || m2 == null) {
            throw new IllegalArgumentException("FeatureModels cannot be null");
        }

        T root1 = m1.getRootFeature();
        T root2 = m2.getRootFeature();

        if (root1 == null || root2 == null) {
            throw new IllegalArgumentException("FeatureModels must have a root feature");
        }

        // 1. Merge the two trees
        T rootFeature = merge(root1, root2, mode);

        // 2. Build the resulting FeatureModel
        FeatureModelFactory<T> factory = new FeatureModelFactory<>();
        factory.setRootFeature(rootFeature);
        FeatureModel<T> result = factory.build();

        // 3. Merge constraints
        // TODO: only works for Union mode (mode == true)
        result.getOwnConstraints().addAll(m1.getOwnConstraints());
        result.getOwnConstraints().addAll(m2.getOwnConstraints());

        return result;
    }

    private T merge(T baseFeature, T aspectFeature, boolean mode){

        if (!baseFeature.getFeatureName().equals(aspectFeature.getFeatureName())) {
            throw new IllegalArgumentException("Only features with the same name can be merged!");
        }

        T mergedFeature = (T) new Feature<T>(baseFeature.getFeatureName());

        List<GroupPairer.Pair<T>> pairedGroups = new GroupPairer<T>().pairGroups(baseFeature, aspectFeature);

        for(GroupPairer.Pair<T> pair: pairedGroups){

            if (pair.left != null && pair.right != null) {

                Set<T> leftOnlyFeatures = new HashSet<>(pair.left.getFeatures());
                Set<T> rightOnlyFeatures = new HashSet<>(pair.right.getFeatures());
                GroupType op = computeOperator(pair.left.GROUPTYPE, pair.right.GROUPTYPE, mode);
                Group<T> group = new Group<>(op);

                for(GroupPairer.FeatureMatch<T> match: pair.getMatches().values()){
                    leftOnlyFeatures.remove(match.left);
                    rightOnlyFeatures.remove(match.right);
                    T mergedChild = merge(match.left, match.right, mode);
                    group.getFeatures().add(mergedChild);
                }
                if(mode) {
                    for (T feature : leftOnlyFeatures) {
                        group.getFeatures().add((T) feature.clone());
                    }
                    for (T feature : rightOnlyFeatures) {
                        group.getFeatures().add((T) feature.clone());
                    }
                }
                if (!group.getFeatures().isEmpty()) {
                    mergedFeature.addChildren(group);
                    //group.setParentFeature(mergedFeature);
                }
            } else if (mode && pair.left != null) {
                // TODO: check precondition: the intersection between the set of features of the base FM and the one of the aspect FM is empty.
                Group<T> cloned = pair.left.clone();
                //cloned.setParentFeature(mergedFeature);
                mergedFeature.addChildren(cloned);
            } else if (mode && pair.right != null) {
                // TODO: check precondition: the intersection between the set of features of the base FM and the one of the aspect FM is empty.
                Group<T> cloned = pair.right.clone();
                //cloned.setParentFeature(mergedFeature);
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
}