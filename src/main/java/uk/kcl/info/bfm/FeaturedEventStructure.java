package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;

import java.util.Iterator;
import java.util.List;

public interface FeaturedEventStructure extends BundleEventStructure {

    Feature getFeature(Event var1);
    FExpression getFExpression(Event var1);
    FExpression getFexpression(List<Event> config);
}
