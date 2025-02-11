package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;

import java.util.Iterator;

public interface FeaturedEventStructure extends BundleEventStructure {
    Feature getFeature(Event var1);
    FExpression getFExpression(Event var1);
}
