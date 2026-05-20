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

package uk.kcl.info.bfm.translators;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.FeatureModelFactory;
import uk.kcl.info.bfm.BehavioralFeature;
import uk.kcl.info.bfm.BehavioralFeatureModel;

public class BfmToFmConverter implements ModelConverter<BehavioralFeatureModel, FeatureModel<BehavioralFeature>> {

    private final BehavioralFeatureModel bfm;

    public BfmToFmConverter(BehavioralFeatureModel bfm) {
        this.bfm = bfm;
    }

    @Override
    public FeatureModel<BehavioralFeature> convert() {
        return new FeatureModelFactory<>(bfm).build();
    }
}
