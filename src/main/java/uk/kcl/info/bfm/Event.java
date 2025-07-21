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

import com.google.common.base.Preconditions;
import java.util.Objects;

public class Event {
    public static final String EPSILON_ACTION = "epsilon";
    static final Event EPSILON = new Event("epsilon");
    private final String name;

    public Event(String name) {
        Preconditions.checkNotNull(name, "Name may not be null!");
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "Event{name=" + this.name + '}';
    }

    public int hashCode() {
        int hash = 7;
        return 37 * hash + Objects.hashCode(this.name);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Event other = (Event)obj;
            return Objects.equals(this.name, other.name);
        }
    }
}
