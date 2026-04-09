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

    /* =========================================
       INTERNAL STATE (for strict union)
       ========================================= */
    private final List<FExpression> strictUnionConstraints = new ArrayList<>();

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

        // 1. Merge trees
        Feature<?> mergedRoot = merge(root1, root2, mode);

        // 2. Build FM
        FeatureModelFactory factory = new FeatureModelFactory<>();
        factory.setRootFeature(mergedRoot);

        // 3. Merge original constraints
        for (FExpression constr : m1.getOwnConstraints()) {
            factory.addConstraint(mergedRoot, constr);
        }
        for (FExpression constr : m2.getOwnConstraints()) {
            factory.addConstraint(mergedRoot, constr);
        }

        // 4. Add strict union constraints (collected during merge)
        for (FExpression constr : strictUnionConstraints) {
            factory.addConstraint(mergedRoot, constr);
        }

        return factory.build();
    }

    /* =========================================
       MERGE
       ========================================= */
    private Feature<?> merge(Feature<?> baseFeature, Feature<?> aspectFeature, boolean mode) {

        if (!baseFeature.getFeatureName().equals(aspectFeature.getFeatureName())) {
            throw new IllegalArgumentException("Only features with the same name can be merged!");
        }

        Feature mergedFeature = new Feature<>(baseFeature.getFeatureName());

        List<GroupPairer.Pair> pairedGroups = new GroupPairer().pairGroups(baseFeature, aspectFeature);

        for (GroupPairer.Pair pair : pairedGroups) {

            if (pair.left != null && pair.right != null) {

                List<Feature<?>> leftOnly = new ArrayList<Feature<?>>(pair.left.getFeatures());
                List<Feature<?>> rightOnly = new ArrayList<Feature<?>>(pair.right.getFeatures());
                Collection<GroupPairer.FeatureMatch> matches = pair.getMatches().values();

                // 1. matched features
                for (GroupPairer.FeatureMatch match : matches) {

                    Feature<?> left = (Feature<?>) match.left;
                    Feature<?> right = (Feature<?>) match.right;

                    leftOnly.remove(left);
                    rightOnly.remove(right);

                    Feature<?> mergedChild = merge(left, right, mode);

                    GroupType op = computeOperator(pair.left.GROUPTYPE, pair.right.GROUPTYPE, mode);
                    addFeatureWithCorrectGrouping(mergedFeature, (Feature) mergedChild, op);
                }

                // 2. unmatched features (UNION only)
                if (mode) {

                    // LEFT
                    if (!leftOnly.isEmpty()) {

                        GroupType lifted = liftAbsent(pair.left.GROUPTYPE);
                        List<Feature<?>> liftedFeatures = new ArrayList<>();

                        for (Feature<?> f : leftOnly) {
                            Feature<?> clone = f.clone();
                            addFeatureWithCorrectGrouping(mergedFeature, (Feature) clone, lifted);
                            liftedFeatures.add(clone);
                        }

                        if (pair.left.GROUPTYPE == GroupType.ALTERNATIVE) {
                            addMutualExclusionConstraints(liftedFeatures);
                        }
                    }

                    // RIGHT
                    if (!rightOnly.isEmpty()) {

                        GroupType lifted = liftAbsent(pair.right.GROUPTYPE);
                        List<Feature<?>> liftedFeatures = new ArrayList<>();

                        for (Feature<?> f : rightOnly) {
                            Feature<?> clone = f.clone();
                            addFeatureWithCorrectGrouping(mergedFeature, (Feature) clone, lifted);
                            liftedFeatures.add(clone);
                        }

                        if (pair.right.GROUPTYPE == GroupType.ALTERNATIVE) {
                            addMutualExclusionConstraints(liftedFeatures);
                        }
                    }
                }

            } else if (mode && pair.left != null) {
                // TODO: check precondition: the intersection between the set of features of the base FM and the one of the aspect FM is empty.
                mergedFeature.addChildren(pair.left.clone());

            } else if (mode && pair.right != null) {
                // TODO: check precondition: the intersection between the set of features of the base FM and the one of the aspect FM is empty.
                mergedFeature.addChildren(pair.right.clone());
            }
        }

        // 3. normalize OR and ALTERNATIVE singleton groups
        normalizeGroups(mergedFeature);

        return mergedFeature;
    }

    /* =========================================
       NORMALISATION
       ========================================= */
    private <T extends Feature<T>> void normalizeGroups(T feature) {

        List<Group<T>> newGroups = new ArrayList<>();

        for (Group<T> group : feature.getChildren()) {

            if (group.getFeatures().size() == 1) {

                GroupType newType = switch (group.GROUPTYPE) {
                    case OR, ALTERNATIVE -> GroupType.MANDATORY;
                    default -> group.GROUPTYPE;
                };

                if (newType != group.GROUPTYPE) {
                    Group<T> newGroup = new Group<>(newType);
                    newGroup.getFeatures().addAll(group.getFeatures());
                    newGroups.add(newGroup);
                    continue;
                }
            }

            newGroups.add(group);
        }

        feature.getChildren().clear();
        feature.getChildren().addAll(newGroups);
    }

    /* =========================================
       STRICT UNION (ALTERNATIVE)
       ========================================= */
    private void addMutualExclusionConstraints(List<Feature<?>> features) {

        for (int i = 0; i < features.size(); i++) {
            for (int j = i + 1; j < features.size(); j++) {

                Feature<?> f1 = features.get(i);
                Feature<?> f2 = features.get(j);

                // ¬(f1 ∧ f2)
                FExpression c =
                        FExpression.featureExpr(f1.getFeatureName())
                                .not()
                                .or(FExpression.featureExpr(f2.getFeatureName()).not());

                strictUnionConstraints.add(c);
            }
        }
    }

    /* =========================================
       GROUP HANDLING
       ========================================= */
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

    /* =========================================
       OPERATOR TABLE
       ========================================= */
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
            default -> throw new IllegalArgumentException();
        };
    }

    private GroupType computeOperator(GroupType baseType, GroupType aspectType, boolean mode) {
        return OP_TABLE[mode ? 1 : 0][idx(baseType)][idx(aspectType)];
    }

    /* =========================================
       LIFT ABSENT
       ========================================= */
    private GroupType liftAbsent(GroupType existingType) {
        return switch (existingType) {
            case MANDATORY -> GroupType.OPTIONAL;
            case OPTIONAL -> GroupType.OPTIONAL;
            case OR -> GroupType.OPTIONAL;
            case ALTERNATIVE -> GroupType.OPTIONAL;
            default -> throw new IllegalArgumentException("Cardinalities are not yet supported in FM!");
        };
    }
}