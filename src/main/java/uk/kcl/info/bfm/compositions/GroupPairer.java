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
import be.vibes.solver.Group;

import java.util.*;

public class GroupPairer<T extends Feature<T>> {

    public static class Pair<T extends Feature<T>> {
        public final Group<T> left;
        public final Group<T>  right;

        // Map: feature name -> (feature from left, feature from right)
        private final Map<String, FeatureMatch<T>> matches;

        public Pair(Group<T>  left, Group<T>  right, Map<String, FeatureMatch<T>> matches) {
            this.left = left;
            this.right = right;
            this.matches = matches;
        }

        public Map<String, FeatureMatch<T>> getMatches() {
            return matches;
        }

        public boolean isSingleton() {
            return left == null || right == null;
        }

    }

    public static class FeatureMatch<T> {
        public final T left;
        public final T right;

        public FeatureMatch(T left, T right) {
            this.left = left;
            this.right = right;
        }
    }

    public List<Pair<T>> pairGroups(Feature<T> f1, Feature<T> f2) {
        List<Group<T>> groups1 = f1.getChildren();
        List<Group<T>> groups2 = f2.getChildren();

        List<Pair<T>> result = new ArrayList<>();

        // ---- 1. Precompute feature-name sets ----
        Map<Group<T>, Set<String>> namesCache = new HashMap<>();

        for (Group<T> g : groups1) {
            namesCache.put(g, extractNames(g));
        }
        for (Group<T> g : groups2) {
            namesCache.put(g, extractNames(g));
        }

        // ---- 2. Build index for F2: feature name -> groups ----
        Map<String, Set<Group<T>>> indexF2 = new HashMap<>();

        for (Group<T> g2 : groups2) {
            for (String name : namesCache.get(g2)) {
                indexF2.computeIfAbsent(name, k -> new HashSet<>()).add(g2);
            }
        }

        // ---- 3. Matching with bijection constraints ----
        Map<Group<T>, Group<T>> matchG1toG2 = new HashMap<>();
        Map<Group<T>, Group<T>> matchG2toG1 = new HashMap<>();

        Map<Group<T>, Map<String, FeatureMatch<T>>> matchData = new HashMap<>();

        for (Group<T> g1 : groups1) {

            Group<T> match = null;
            Map<String, FeatureMatch<T>> matches = null;

            Set<Group<T>> visited = new HashSet<>();

            for (String name : namesCache.get(g1)) {
                Set<Group<T>> candidates = indexF2.get(name);
                if (candidates == null) continue;

                for (Group<T> g2 : candidates) {

                    if (!visited.add(g2)) continue;

                    Map<String, FeatureMatch<T>> currentMatches = computeMatches(g1, g2);

                    if (currentMatches.isEmpty()) continue;

                    if (match == null) {
                        match = g2;
                        matches = currentMatches;
                    } else if (match != g2) {
                        throw new IllegalStateException("Group " + g1 + " matches multiple groups in F2");
                    }
                }
            }

            if (match != null) {
                Group<T> previous = matchG2toG1.putIfAbsent(match, g1);
                if (previous != null) {
                    throw new IllegalStateException("Group " + match + " from F2 matches multiple groups in F1.");
                }

                matchG1toG2.put(g1, match);
                matchData.put(g1, matches);
            }
        }

        // ---- 4. Build result (F1 side) ----
        for (Group<T> g1 : groups1) {
            Group<T> g2 = matchG1toG2.get(g1);
            if (g2 != null) {
                result.add(new Pair<>(g1, g2, matchData.get(g1)));
            } else {
                result.add(new Pair<>(g1, null, Collections.emptyMap()));
            }
        }

        // ---- 5. Add unmatched F2 groups ----
        for (Group<T> g2 : groups2) {
            if (!matchG2toG1.containsKey(g2)) {
                result.add(new Pair<>(null, g2, Collections.emptyMap()));
            }
        }

        return result;
    }

    private Map<String, FeatureMatch<T>> computeMatches(Group<T> g1, Group<T> g2) {
        Map<String, T> map1 = new HashMap<>();

        for (T f : g1.getFeatures()) {
            map1.put(f.getFeatureName(), f);
        }

        Map<String, FeatureMatch<T>> matches = new HashMap<>();

        for (T f2 : g2.getFeatures()) {
            T f1 = map1.get(f2.getFeatureName());
            if (f1 != null) {
                matches.put(f2.getFeatureName(), new FeatureMatch<>(f1, f2));
            }
        }

        return matches;
    }

    private Set<String> extractNames(Group<T> group) {
        Set<String> names = new HashSet<>();
        for (T f : group.getFeatures()) {
            names.add(f.getFeatureName());
        }
        return names;
    }
}