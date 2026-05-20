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
import be.vibes.ts.*;
import uk.kcl.info.utils.Pair;

import java.util.*;

public class FTSParallelComposer extends AbstractTSParallelComposer<FeaturedTransitionSystem> {

    @Override
    protected TransitionSystemFactory createFactory(String initName) {
        return new FeaturedTransitionSystemFactory(initName);
    }

    /*
     * Override transition handling to inject μ + pruning
     */
    @Override
    protected final void handleTransition(TransitionSystemFactory factory, Queue<Pair<State>> queue, Set<Pair<State>> visited,
                                          Map<Pair<State>, String> nameCache, Pair<State> sourcePair, Transition t1, Transition t2,
                                          String sourceName, Action action, boolean isSync) {

        FeaturedTransitionSystemFactory ftsFactory = (FeaturedTransitionSystemFactory) factory;

        FExpression fexpr = computeTargetFExpression(t1, t2, isSync);

        // Prune
        if (fexpr.isFalse()) return;

        Pair<State> targetPair = computeTargetStatePair(sourcePair, t1, t2, isSync);
        String targetName = nameCache.computeIfAbsent(targetPair, this::nameOf);
        ftsFactory.addTransition(sourceName, action.getName(), fexpr, targetName);

        if (visited.add(targetPair)) {
            queue.add(targetPair);
        }
    }

    private FExpression computeTargetFExpression(Transition t1, Transition t2, boolean isSync) {

        if (isSync) {
            return left.getFExpression(t1).and(right.getFExpression(t2)).applySimplification();
        } else if (t1 != null) {
            return left.getFExpression(t1).applySimplification();
        } else {
            return right.getFExpression(t2).applySimplification();
        }
    }

}