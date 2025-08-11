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

package uk.kcl.info.bfm.execution;

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.ts.exception.UnresolvedFExpression;
import uk.kcl.info.bfm.*;
import uk.kcl.info.bfm.io.xml.XmlLoaderUtility;

import java.util.*;

public class FeaturedEventStructureExecutor {

    private final FeaturedEventStructure<?> fes;
    private final FeatureModel<?> fm;

    public FeaturedEventStructureExecutor(FeaturedEventStructure<?> fes, FeatureModel<?> fm) {
        this.fes = fes;
        this.fm = fm;
    }

    public FeaturedEventStructureExecutor(BehavioralFeatureModel bfm) {
        this.fes = bfm;
        this.fm = bfm;
    }


    public Map<Configuration, Set<List<String>>> getAllTraces() throws ConstraintSolvingException, UnresolvedFExpression {

        BehavioralProduct proj = SimpleBehavioralProduct.getInstance();
        Map<Configuration, Set<List<String>>> tracesMap = new HashMap<>();
        Iterator<Configuration> it = fm.getSolutions();

        while(it.hasNext()){
            Configuration product = it.next();
            BundleEventStructure bes = proj.project(fes, (Collection<Feature<?>>) fm.getFeatures(), product);
            BundleEventStructureExecutor exec = new BundleEventStructureExecutor(bes);
            tracesMap.put(product, exec.getAllTraces());
        }

        return tracesMap;
    }

}
