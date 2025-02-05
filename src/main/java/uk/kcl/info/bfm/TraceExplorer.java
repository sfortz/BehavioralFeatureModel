package uk.kcl.info.bfm;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import be.vibes.solver.FeatureModel;
import be.vibes.ts.*;
import be.vibes.ts.exception.TransitionSystenExecutionException;

public class TraceExplorer {
    private final FeaturedTransitionSystem fts;
    private final FeatureModel fm;
    private final List<Execution> allTraces;

    public TraceExplorer(FeatureModel fm, FeaturedTransitionSystem fts) {
        this.fm = fm;
        this.fts = fts;
        this.allTraces = new ArrayList<>();
    }

    public List<Execution> exploreAllTraces() throws TransitionSystenExecutionException {
        explore(new Stack<>(), null); // Start from the initial state
        return allTraces;
    }

    private void explore(Stack<Execution> currentTrace, Execution currentExecution) throws TransitionSystenExecutionException {

        State currentState = (currentExecution == null) ? fts.getInitialState() : currentExecution.getLast().getTarget();

        if (currentExecution != null && currentExecution.getLast().getTarget().equals(fts.getInitialState())) { //TODO: if action == end, you should probably ignore it.

            Execution trace = currentTrace.pop();
            allTraces.add(trace);
            return;
        }

        for (Iterator<Transition> it = fts.getOutgoing(currentState); it.hasNext(); ) {
            Transition next = it.next();
            Execution newExecution = (currentExecution == null) ? new Execution() : currentExecution.copy();
            newExecution.enqueue(next);
            currentTrace.push(newExecution);
            explore(currentTrace, newExecution);
            currentTrace.pop();
        }

    }
}