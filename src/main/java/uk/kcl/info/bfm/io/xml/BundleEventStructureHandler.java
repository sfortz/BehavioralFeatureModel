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

import be.vibes.ts.io.xml.XmlEventHandler;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.BundleEventStructureFactory;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BundleEventStructureHandler implements XmlEventHandler {
    public static final String BES_TAG = "bes";
    public static final String EVENTS_TAG = "events";
    public static final String EVENT_TAG = "event";
    public static final String CAUSALITIES_TAG = "causalities";
    public static final String CAUSALITY_TAG = "causality";
    public static final String BUNDLE_TAG = "bundle";
    public static final String CONFLICTS_TAG = "conflicts";
    public static final String CONFLICT_TAG = "conflict";

    public static final String ID_ATTR = "id";
    public static final String TARGET_ATTR = "target";

    private static final Logger LOG = LoggerFactory.getLogger(BundleEventStructureHandler.class);

    protected BundleEventStructureFactory factory;
    protected String charValue;

    // Stack to track nested bundles
    protected Stack<Set<String>> bundleStack = null;
    protected String currentCausalityTarget = null;
    protected Stack<Set<String>> conflictStack = null;

    public BundleEventStructureHandler() {
        this.factory = new BundleEventStructureFactory();
    }

    public BundleEventStructure getBundleEventStructure() {
        return this.factory.build();
    }

    public void handleStartDocument() {
        LOG.trace("Starting document");
    }

    public void handleEndDocument() {
        LOG.trace("Ending document");
    }

    public void handleStartElement(StartElement element) throws XMLStreamException {
        String tag = element.getName().getLocalPart();
        switch (tag) {
            case BES_TAG:
                handleStartBesTag();
                break;
            case EVENTS_TAG:
                handleStartEventsTag();
                break;
            case CAUSALITIES_TAG:
                handleStartCausalitiesTag();
                break;
            case CONFLICTS_TAG:
                handleStartConflictsTag();
                break;
            case EVENT_TAG:
                handleStartEventTag(element);
                break;
            case CAUSALITY_TAG:
                handleStartCausalityTag(element);
                break;
            case BUNDLE_TAG:
                handleStartBundleTag();
                break;
            case CONFLICT_TAG:
                handleStartConflictTag();
                break;
            default:
                LOG.debug("Unknown element: {}", tag);
        }
    }

    protected void handleStartConflictsTag() throws XMLStreamException {
        LOG.trace("Starting Conflicts");
    }

    protected void handleStartCausalitiesTag() throws XMLStreamException {
        LOG.trace("Starting Causalities");
    }

    protected void handleStartEventsTag() throws XMLStreamException {
        LOG.trace("Starting Events");
        if (conflictStack != null) {
            conflictStack.push(new HashSet<>()); // Create new Conflict set
        }
    }

    protected void handleStartBesTag() throws XMLStreamException {
        LOG.trace("Starting BES");
    }

    protected void handleStartEventTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing event");
        String id = element.getAttributeByName(QName.valueOf(ID_ATTR)).getValue();
        if (bundleStack != null) {
            // If inside a bundle, add to the current bundle
            bundleStack.peek().add(id);
        } else if (conflictStack != null) {
            // If inside a conflict, add to the current conflict
            conflictStack.peek().add(id);
        } else {
            // Otherwise, it's a standalone event declaration
            factory.addEvent(id);
        }
    }

    protected void handleStartCausalityTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing causality");
        currentCausalityTarget = element.getAttributeByName(QName.valueOf(TARGET_ATTR)).getValue();
    }

    protected void handleStartBundleTag() throws XMLStreamException {
        LOG.trace("Processing bundle");
        bundleStack = new Stack<>();
        bundleStack.push(new HashSet<>()); // Create new bundle set
    }

    protected void handleStartConflictTag() throws XMLStreamException {
        LOG.trace("Processing conflict");
        conflictStack = new Stack<>();
    }

    public void handleEndElement(EndElement element) throws XMLStreamException {
        String tag = element.getName().getLocalPart();
        switch (tag) {
            case EVENT_TAG:
                LOG.trace("Ending event");
                break;
            case CAUSALITY_TAG:
                LOG.trace("Ending causality");
                currentCausalityTarget = null;
                break;
            case BUNDLE_TAG:
                LOG.trace("Ending bundle");
                if (!bundleStack.isEmpty() && currentCausalityTarget != null) {
                    factory.addCausality(bundleStack.pop(), currentCausalityTarget);
                }
                break;
            case CONFLICT_TAG:
                LOG.trace("Ending conflict");
                if (conflictStack.size() == 2) {
                    Set<String> conflictSet1 = conflictStack.pop();
                    Set<String> conflictSet2 = conflictStack.pop();
                    factory.addConflicts(conflictSet1, conflictSet2);
                } else {
                    LOG.warn("Invalid conflict definition!");
                }
                conflictStack = null;
                break;
            case CAUSALITIES_TAG:
                bundleStack = null;
                break;
            case BES_TAG:
                LOG.trace("Ending bundle event structure");
                break;
        }
    }

    public void handleCharacters(Characters element) throws XMLStreamException {
        this.charValue = element.asCharacters().getData().trim();
    }
}
