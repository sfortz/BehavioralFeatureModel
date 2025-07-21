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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CausalityRelation {
    private final Set<Event> bundle;
    private final Event target;

    public CausalityRelation(Set<Event> bundle, Event target) {
        this.bundle = bundle;
        this.target = target;
    }

    public Set<Event> getBundle() {
        return this.bundle;
    }

    public Event getTarget() {
        return this.target;
    }

    @Override
    public String toString() {
        String bundleStr = "{" + this.bundle.stream()
                .map(Event::getName)
                .collect(Collectors.joining(", ")) + "}";
        return "Causality{bundle=" + bundleStr + ", target=" + this.target + '}';
    }

    public int hashCode() {
        return Objects.hashCode(this.bundle) + Objects.hashCode(this.target);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            CausalityRelation other = (CausalityRelation)obj;
            return Objects.equals(this.bundle, other.bundle) && Objects.equals(this.target, other.target);
        }
    }
}

