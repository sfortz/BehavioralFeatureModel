package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;

public class FeaturedEventStructureFactory extends BundleEventStructureFactory{
    public FeaturedEventStructureFactory(FeatureModel<?> fm) {
        super(new DefaultFeaturedEventStructure<>(fm));
    }

    @Override
    public void addEvent(String event) {
        throw new UnsupportedOperationException("FES doesn't allow to add an event if not associated with a feature.");
    }

    public void addEvent(String event, Feature<?> feature) {
        this.addEvent(event, feature, FExpression.trueValue());
    }

    public void addEvent(String event, Feature<?> feature, FExpression fexpr) {
        DefaultFeaturedEventStructure<?> fes = (DefaultFeaturedEventStructure<?>) this.bes;
        Event ev = fes.addEvent(event);
        fes.addFeature(ev,feature,fexpr);
    }

    public FeaturedEventStructure<?> build() {
        return (DefaultFeaturedEventStructure<?>) super.build();
    }
}
