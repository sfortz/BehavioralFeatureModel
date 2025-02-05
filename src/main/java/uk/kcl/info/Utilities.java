package uk.kcl.info;

import be.vibes.fexpression.DimacsModel;
import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.Sat4JSolverFacade;
import be.vibes.solver.exception.SolverInitializationException;
import be.vibes.ts.*;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import uk.kcl.info.bfm.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utilities {

    public static FeaturedTransitionSystem BFMtoFTS (BehavioralFeatureModel bfm){
        FeaturedTransitionSystemFactory factory = null;//new FeaturedTransitionSystemFactory(bfm.getInitialEvent().getName());
        /* To Do */
        return factory.build();
    }

    public static BehavioralFeatureModel FTStoBFM (DimacsModel dimacs, FeaturedTransitionSystem fts) throws SolverInitializationException {

        FExpression fm = dimacs.getFd();
        List<String> features = dimacs.getFeatures();
        String initialState = fts.getInitialState().getName();
        Map<Event, Feature> mapping = new HashMap<>();

        List<Transition> transitions = new LinkedList<>();

        for (Iterator<Transition> it = fts.transitions(); it.hasNext(); ) {
                transitions.add(it.next());
        }

        List<Event> events = new LinkedList<>();
        List<CausalityRelation> causalities = new LinkedList<>();
        List<ConflictRelation> conflicts = new LinkedList<>();

        Map<FExpression, Feature> featureMap = new HashMap<>();
        for (String f : features) {
            featureMap.put(new FExpression(f), new Feature(f));
        }

        for (Iterator<Action> it = fts.actions(); it.hasNext(); ) {
            Action action = it.next();
            Stream<Transition> filtered = transitions.stream().filter(trans -> trans.getAction().equals(action));
            FExpression fexpr = filtered.map(fts::getFExpression).reduce(FExpression.trueValue(),(fe1, fe2) -> fe1.and(fe2).applySimplification());
            Event event = new Event(action.getName(), fexpr.applySimplification());
            events.add(event);

            if (featureMap.containsKey(fexpr)) {
                Feature corresponding = featureMap.get(fexpr);
                mapping.put(event, corresponding);
            }else{
                String f = fexpr.toString(); // Creating a new feature TODO: Change for a better feature name
                features.add(f);
                Feature feature =  new Feature(f);
                mapping.put(event,feature);
                featureMap.put(fexpr, feature);
                fm.and(fexpr.or(fexpr.not())); // Adding the new feature as an optional feature
                //TODO: Add requirements in the FM
            }
        }


        FeaturedTransitionSystemExecutor executor = new FeaturedTransitionSystemExecutor(fts, new Sat4JSolverFacade(dimacs));
        //xecutor.get


        BehavioralFeatureModelFactory factory = new BehavioralFeatureModelFactory(dimacs);

        for (Iterator<State> it = fts.states(); it.hasNext(); ) {
            State s = it.next();
            
            //factory.addEvent(, s.getName());
        }

        /* To Do */
        return factory.build();
    }


    public static void main(String[] args) throws TransitionSystenExecutionException {
        // Assuming `executor` is already instantiated and properly configured

        /*TraceExplorer explorer = new TraceExplorer(fts, fm);
        List<List<Execution>> allTraces = explorer.exploreAllTraces();

        // Process allTraces as needed
        for (List<Execution> trace : allTraces) {
            System.out.println("Trace:");
            for (Execution exec : trace) {
                System.out.println(exec);
            }
        }*/
    }

}
