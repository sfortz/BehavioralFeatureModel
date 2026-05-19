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

package uk.kcl.info.bfm.translators;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.ts.Action;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.State;
import be.vibes.ts.Transition;
import org.slf4j.Logger;
import uk.kcl.info.bfm.CausalityRelation;
import uk.kcl.info.bfm.Event;

import java.util.*;

import static uk.kcl.info.bfm.translators.TranslationUtils.forwardSymbolicReachable;

public abstract class AbstractFtsConverter<F extends Feature<F>, R> implements ModelConverter<FeaturedTransitionSystem, R> {

    protected final Logger log;

    protected final FeaturedTransitionSystem fts;
    protected final FeatureModel<F> fm;

    protected final Map<Transition, Event> transitionEventMap = new HashMap<>();

    // ===== PRECOMPUTED STRUCTURES =====
    protected final Map<Transition, FExpression> transitionExpr = new HashMap<>();
    protected final Map<State, List<Transition>> outgoing = new HashMap<>();
    // State-based symbolic reachability
    protected final Map<State, Map<State, Set<FExpression>>> stateReachability = new HashMap<>();

    protected AbstractFtsConverter(Logger log, FeatureModel<F> fm, FeaturedTransitionSystem fts) {
        this.log = Objects.requireNonNull(log);
        this.fm = Objects.requireNonNull(fm);
        this.fts = Objects.requireNonNull(fts);
    }

    @Override
    public final R convert() {

        initializeFactory();
        preprocess();
        Map<State, FExpression> pathConditions = computePathConditions();
        addEvents(pathConditions);
        addConflicts();
        addCausalities();

        return buildResult();
    }

    // ============================================================
    // ABSTRACT HOOKS
    // ============================================================

    protected abstract void initializeFactory();

    protected abstract void addEventToFactory(Event event, FExpression fexpr);

    protected abstract void addConflictToFactory(Event e1, Event e2);

    protected abstract void addCausalityToFactory(CausalityRelation relation);

    protected abstract R buildResult();

    // ============================================================
    // PREPROCESS
    // ============================================================

    private void preprocess() {

        for (Iterator<State> it = fts.states(); it.hasNext(); ) {
            State s = it.next();
            stateReachability.put(s, forwardSymbolicReachable(fts, s));
        }

        for (Iterator<Transition> it = fts.transitions(); it.hasNext(); ) {
            Transition t = it.next();
            transitionExpr.put(t, fts.getFExpression(t).applySimplification());
            outgoing.computeIfAbsent(t.getSource(), k -> new ArrayList<>()).add(t);
        }
    }

    // ============================================================
    // PATH CONDITIONS (FIXPOINT)
    // ============================================================

    private Map<State, FExpression> computePathConditions() {

        Map<State, FExpression> pathCond = new HashMap<>();
        Queue<State> worklist = new ArrayDeque<>();

        State init = fts.getInitialState();
        pathCond.put(init, FExpression.trueValue());
        worklist.add(init);

        while (!worklist.isEmpty()) {

            State current = worklist.poll();
            FExpression currentCond = pathCond.get(current);

            for (Transition t : outgoing.getOrDefault(current, Collections.emptyList())) {

                State target = t.getTarget();

                FExpression newCond = currentCond.and(transitionExpr.get(t));
                FExpression oldCond = pathCond.get(target);

                if (oldCond == null) {
                    pathCond.put(target, newCond.applySimplification());
                    worklist.add(target);
                } else {
                    FExpression merged = oldCond.or(newCond).applySimplification().toCnf();

                    if (!merged.equals(oldCond)) {
                        pathCond.put(target, merged);
                        worklist.add(target);
                    }
                }
            }
        }

        return pathCond;
    }

    // ============================================================
    // EVENT CREATION
    // ============================================================
    private void addEvents(Map<State, FExpression> statePathConditions) {

        Map<Action, Integer> actionCounter = new HashMap<>();

        int i = 0;

        for (Transition t : transitionExpr.keySet()) {

            Action a = t.getAction();

            int j = actionCounter.getOrDefault(a, 0);
            String name = a.getName();
            String eventName = name + "_" + j;

            Event e = new Event(eventName, name);
            transitionEventMap.put(t, e);

            FExpression fexpr = transitionExpr.get(t)
                    .and(statePathConditions.getOrDefault(t.getSource(), FExpression.trueValue()))
                    .applySimplification().toCnf();

            addEventToFactory(e, fexpr);

            actionCounter.put(a, j + 1);
            i++;
            log.trace("Transitions to events: {}/{}", i, transitionExpr.size());
        }
    }

    // ============================================================
    // CONFLICTS
    // ============================================================
    private void addConflicts() {

        // Liste indexée des transitions
        List<Map.Entry<Transition, Event>> entries = new ArrayList<>(transitionEventMap.entrySet());
        int n = entries.size();

        // Cache global des forward reachability
        Map<Transition, Map<State, Set<FExpression>>> reachCache = new HashMap<>();

        for (int i = 0; i < n; i++) {

            Transition t1 = entries.get(i).getKey();
            Event e1 = entries.get(i).getValue();
            Map<State, Set<FExpression>> r1 = reachCache.computeIfAbsent(t1, t -> forwardSymbolicReachable(fts, t));

            for (int j = i + 1; j < n; j++) {

                Transition t2 = entries.get(j).getKey();
                Event e2 = entries.get(j).getValue();

                boolean t1ToT2 = r1.getOrDefault(t2.getSource(), Set.of()).stream().anyMatch(f -> !f.isFalse());
                if (t1ToT2) {continue;} // ← EARLY EXIT

                Map<State, Set<FExpression>> r2 = reachCache.computeIfAbsent(t2, t -> forwardSymbolicReachable(fts, t));

                boolean t2ToT1 = r2.getOrDefault(t1.getSource(), Set.of()).stream().anyMatch(f -> !f.isFalse());

                if (!t2ToT1) {
                    addConflictToFactory(e1, e2);
                }
            }

            log.trace("Conflict matrix row: {}/{}", i, n);
        }
    }

    // ============================================================
    // CAUSALITIES
    // ============================================================

    private void addCausalities() {

        Map<State, Set<Event>> incomingEvents = new HashMap<>();

        for (Map.Entry<Transition, Event> entry : transitionEventMap.entrySet()) {
            incomingEvents.computeIfAbsent(entry.getKey().getTarget(), k -> new HashSet<>()).add(entry.getValue());
        }

        int i = 0;

        for (Map.Entry<Transition, Event> entry : transitionEventMap.entrySet()) {

            Transition t = entry.getKey();
            Event e = entry.getValue();

            Set<Event> bundle = incomingEvents.getOrDefault(t.getSource(), Set.of());

            if (!bundle.isEmpty()) {
                addCausalityToFactory(new CausalityRelation(bundle, e));
            }

            i++;

            log.trace("Transitions to causalities: {}/{}", i, transitionEventMap.size());
        }
    }
}