/*
 *
 *  * Copyright 2025 Sophie Fortz
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

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
