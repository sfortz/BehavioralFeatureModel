package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;

import java.util.Iterator;

public interface FeaturedEventStructure extends BundleEventStructure {

    void setFeatureModel(FeatureModel fm);
    Feature getFeature(Event var1);
    FExpression getFExpression(Event var1);
}
