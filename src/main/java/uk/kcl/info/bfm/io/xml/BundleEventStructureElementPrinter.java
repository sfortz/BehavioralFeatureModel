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

import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.ConflictSet;
import uk.kcl.info.bfm.Event;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface BundleEventStructureElementPrinter {
    void printElement(XMLStreamWriter writer, BundleEventStructure bes) throws XMLStreamException;

    void printEvents(XMLStreamWriter writer, Iterator<Event>  events) throws XMLStreamException;

    void printCausalities(XMLStreamWriter writer) throws XMLStreamException;

    void printConflicts(XMLStreamWriter writer, ConflictSet conflicts) throws XMLStreamException;

}
