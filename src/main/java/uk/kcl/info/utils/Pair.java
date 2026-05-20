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

package uk.kcl.info.utils;

import java.util.Objects;

public class Pair<T> {
    T e;
    T f;

    public Pair(T e, T f) {
        this.e = e;
        this.f = f;
    }

    public T getLeft() {
        return e;
    }

    public T getRight() {
        return f;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pair<T> pair = (Pair<T>) o;
        return Objects.equals(e, pair.e) && Objects.equals(f, pair.f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(e, f);
    }
}

