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

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.ts.exception.UnresolvedFExpression;

import java.util.Collection;

public interface BehavioralProduct {

    BundleEventStructure project(FeaturedEventStructure<?> var1, Collection<Feature<?>> var2, Configuration var3) throws UnresolvedFExpression;
}
