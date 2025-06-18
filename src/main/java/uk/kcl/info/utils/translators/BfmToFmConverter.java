package uk.kcl.info.utils.translators;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.FeatureModelFactory;
import uk.kcl.info.bfm.BehavioralFeature;
import uk.kcl.info.bfm.BehavioralFeatureModel;

public class BfmToFmConverter {

    private final BehavioralFeatureModel bfm;

    public BfmToFmConverter(BehavioralFeatureModel bfm) {
        this.bfm = bfm;
    }

    public FeatureModel<BehavioralFeature> convert() {
        return new FeatureModelFactory<>(bfm).build();
    }
}
