package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;

import java.util.List;

public interface FeaturedEventStructure<F extends Feature<F>> extends BundleEventStructure {

    F getFeature(Event var1);
    FExpression getFExpression(Event var1);
    FExpression getFexpression(List<Event> config);
}
