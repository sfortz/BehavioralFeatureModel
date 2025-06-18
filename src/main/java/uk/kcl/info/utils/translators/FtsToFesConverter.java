package uk.kcl.info.utils.translators;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.ts.Action;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.Transition;
import uk.kcl.info.bfm.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FtsToFesConverter {

    private final FeaturedTransitionSystem fts;
    private final FeatureModel<?> fm;
    private final TsToBesConverter converter;
    private BundleEventStructure bes;
    private FeaturedEventStructureFactory factory;

    public FtsToFesConverter(FeatureModel<?> fm, FeaturedTransitionSystem fts) {
        this.fts = Objects.requireNonNull(fts);
        this.fm = Objects.requireNonNull(fm);
        this.converter = new TsToBesConverter(fts);
    }

    public FeaturedEventStructure<?> convert() {
        this.bes = converter.convert();
        this.factory = new FeaturedEventStructureFactory(fm);

        addEvents();
        addCausalities();
        addConflicts();

        return factory.build();
    }

    private void addEvents() {
        for (Iterator<Event> it = bes.events(); it.hasNext(); ) {
            Event event = it.next();
            Action action = fts.getAction(event.getName());

            List<FExpression> fexprList = new ArrayList<>();
            FExpression combinedExpr = FExpression.falseValue();

            for (Iterator<Transition> transIt = fts.getTransitions(action); transIt.hasNext(); ) {
                FExpression fexpr = fts.getFExpression(transIt.next());
                fexprList.add(fexpr);
                combinedExpr.orWith(fexpr);
            }

            FExpression simplifiedExpr = combinedExpr.applySimplification();
            Feature<?> feature = fm.getLeastCommonAncestor(fexprList);
            factory.addEvent(event.getName(), feature, simplifiedExpr);
        }
    }

    private void addCausalities() {
        for (Iterator<CausalityRelation> it = bes.causalities(); it.hasNext(); ) {
            CausalityRelation causality = it.next();
            factory.addCausality(causality.getBundle(), causality.getTarget());
        }
    }

    private void addConflicts() {
        factory.addConflicts(bes.getConflictSetCopy());
    }
}
