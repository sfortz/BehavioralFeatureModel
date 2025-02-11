package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;

import java.util.Set;

public class FeaturedEventStructureFactory extends BundleEventStructureFactory{
    public FeaturedEventStructureFactory() {
        super(new DefaultFeaturedEventStructure());
    }

    public void addEvent(String event, Feature feature) {
        this.addEvent(event, feature, FExpression.trueValue());
    }

    public void addEvent(String event, Feature feature, FExpression fexpr) {
        DefaultFeaturedEventStructure fes = (DefaultFeaturedEventStructure)this.bes;
        Event ev = fes.addEvent(event);
        fes.setFeature(ev,feature);
        fes.setFExpression(ev,fexpr);
    }

    public FeaturedEventStructure build() {
        return (DefaultFeaturedEventStructure)super.build();
    }
}
