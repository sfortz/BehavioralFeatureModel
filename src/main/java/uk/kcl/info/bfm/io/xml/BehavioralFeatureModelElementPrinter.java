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

package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.solver.Group;
import uk.kcl.info.bfm.BehavioralFeature;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.ConflictSet;
import uk.kcl.info.bfm.Event;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;

public interface BehavioralFeatureModelElementPrinter {
    void printElement(XMLStreamWriter writer, BehavioralFeatureModel bfm) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, BehavioralFeature feature) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, Group<BehavioralFeature> group) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, FExpression fexpr) throws XMLStreamException;

    void printEvents(XMLStreamWriter writer, Iterator<Event>  events) throws XMLStreamException;

    void printCausalities(XMLStreamWriter xtw, BehavioralFeature bf) throws XMLStreamException;

    void printConflicts(XMLStreamWriter writer, ConflictSet conflicts) throws XMLStreamException;
}
