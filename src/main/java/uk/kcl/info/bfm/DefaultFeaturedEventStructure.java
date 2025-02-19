package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DefaultFeaturedEventStructure extends DefaultBundleEventStructure implements FeaturedEventStructure{

    private final Map<Event, Feature> feature = new HashMap();

    private final Map<Event, FExpression> fexpression = new HashMap();

    public DefaultFeaturedEventStructure() {
        super();
    }

    //TODO: Check that all event have a related feature (or root?).

    @Override
    public Feature getFeature(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        Feature feature = this.feature.get(event);
        return feature;
    }

    @Override
    public FExpression getFExpression(Event event) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        FExpression fexpr = this.fexpression.get(event);
        if (fexpr == null) {
            fexpr = FExpression.trueValue();
        }

        return fexpr;
    }

    void addFeature(Event event, Feature feature, FExpression fexpr) {
        Preconditions.checkNotNull(event, "Event may not be null!");
        Preconditions.checkNotNull(feature, "Feature may not be null!");
        this.feature.put(event, feature);
        this.fexpression.put(event,fexpr);
    }
}
