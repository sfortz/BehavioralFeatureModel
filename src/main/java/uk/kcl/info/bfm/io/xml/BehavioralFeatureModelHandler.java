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
import be.vibes.fexpression.ParserUtil;
import be.vibes.fexpression.exception.ParserException;
import be.vibes.solver.Group;
import be.vibes.ts.io.xml.XmlEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.BehavioralFeature;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.BehavioralFeatureModelFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.util.*;

public class BehavioralFeatureModelHandler implements XmlEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BehavioralFeatureModelHandler.class);

    // ===== TAGS =====
    public static final String BFM_TAG = "bfm";
    public static final String FEATURE_TAG = "feature";
    public static final String OPTIONAL_TAG = "optional";
    public static final String MANDATORY_TAG = "mandatory";
    public static final String ALTERNATIVE_TAG = "alternative";
    public static final String OR_TAG = "or";

    public static final String FEATURE_CONSTRAINTS_TAG = "feature_constraints";
    public static final String FEATURE_CONSTRAINT_TAG = "feature_constraint";

    public static final String EVENT_CONSTRAINTS_TAG = "event_constraints";
    public static final String EVENTS_TAG = "events";
    public static final String EVENT_TAG = "event";
    public static final String CAUSALITIES_TAG = "causalities";
    public static final String CAUSALITY_TAG = "causality";
    public static final String BUNDLE_TAG = "bundle";
    public static final String CONFLICTS_TAG = "conflicts";
    public static final String CONFLICT_TAG = "conflict";

    // ===== ATTRIBUTES =====
    public static final String NAMESPACE_ATTR = "namespace";
    public static final String NAME_ATTR = "name";
    public static final String ID_ATTR = "id";
    public static final String ACTION_ATTR = "action";
    public static final String FEXPRESSION_ATTR = "fexpression";
    public static final String TARGET_ATTR = "target";

    // ===== CONTEXT =====
    private enum Context {
        EVENTS_DECLARATION,
        BUNDLE,
        CONFLICT
    }

    private final Stack<Context> contextStack = new Stack<>();

    // ===== MODEL =====
    protected BehavioralFeatureModelFactory factory = new BehavioralFeatureModelFactory();

    // ===== FEATURE STRUCTURE =====
    protected Stack<Group<BehavioralFeature>> groupStack = new Stack<>();
    protected Stack<BehavioralFeature> featureStack = new Stack<>();

    // ===== CONSTRAINT STRUCTURES =====
    protected Stack<Set<String>> bundleStack = new Stack<>();
    protected Stack<Set<String>> conflictStack = new Stack<>();
    protected String currentCausalityTarget = null;

    protected String charValue;

    // ===== API =====
    public BehavioralFeatureModel getBehavioralFeatureModel() {
        return factory.build();
    }

    // ===== DOCUMENT =====
    public void handleStartDocument() {
        LOG.trace("Start document");
    }

    public void handleEndDocument() {
        LOG.trace("End document");
    }

    // ===== START ELEMENT =====
    public void handleStartElement(StartElement element) throws XMLStreamException {

        String tag = element.getName().getLocalPart();

        switch (tag) {
            case BFM_TAG -> {
                String namespace = element.getAttributeByName(QName.valueOf(NAMESPACE_ATTR)).getValue();
                factory.setNamespace(namespace);
            }

            case FEATURE_TAG -> {
                String name = element.getAttributeByName(QName.valueOf(NAME_ATTR)).getValue();
                BehavioralFeature f;

                if (groupStack.isEmpty()) {
                    f = factory.setRootFeature(name);
                } else {
                    f = factory.addFeature(groupStack.peek(), name);
                }

                featureStack.push(f);
            }

            case OPTIONAL_TAG -> groupStack.push(factory.addChild(featureStack.peek(), Group.GroupType.OPTIONAL));
            case MANDATORY_TAG -> groupStack.push(factory.addChild(featureStack.peek(), Group.GroupType.MANDATORY));
            case OR_TAG -> groupStack.push(factory.addChild(featureStack.peek(), Group.GroupType.OR));
            case ALTERNATIVE_TAG -> groupStack.push(factory.addChild(featureStack.peek(), Group.GroupType.ALTERNATIVE));

            case EVENTS_TAG -> {
                if (!contextStack.isEmpty() && contextStack.peek() == Context.CONFLICT) {
                    conflictStack.push(new HashSet<>());
                } else {
                    contextStack.push(Context.EVENTS_DECLARATION);
                }
            }

            case EVENT_TAG -> handleEvent(element);

            case FEATURE_CONSTRAINT_TAG -> {
                String expr = element.getAttributeByName(QName.valueOf(FEXPRESSION_ATTR)).getValue();
                try {
                    FExpression fexpr = ParserUtil.getInstance().parse(expr);
                    factory.addConstraint(featureStack.peek(), fexpr);
                } catch (ParserException e) {
                    throw new XMLStreamException("Error parsing fexpression: " + expr, e);
                }
            }

            case CAUSALITY_TAG -> {
                currentCausalityTarget = element.getAttributeByName(QName.valueOf(TARGET_ATTR)).getValue();
            }

            case BUNDLE_TAG -> {
                contextStack.push(Context.BUNDLE);
                bundleStack.push(new HashSet<>());
            }

            case CONFLICT_TAG -> {
                contextStack.push(Context.CONFLICT);
                conflictStack = new Stack<>();
            }
        }
    }

    // ===== EVENT HANDLING =====
    private void handleEvent(StartElement element) throws XMLStreamException {
        String id = element.getAttributeByName(QName.valueOf(ID_ATTR)).getValue();

        if (!contextStack.isEmpty() && contextStack.peek() == Context.BUNDLE) {
            bundleStack.peek().add(id);
            return;
        }

        if (!contextStack.isEmpty() && contextStack.peek() == Context.CONFLICT) {
            conflictStack.peek().add(id);
            return;
        }

        // Declaration
        Attribute actionAttr = element.getAttributeByName(QName.valueOf(ACTION_ATTR));
        if (actionAttr == null) {
            throw new XMLStreamException("Invalid event " + id + " declaration: null Action");
        }

        String action = actionAttr.getValue();
        Attribute exprAttr = element.getAttributeByName(QName.valueOf(FEXPRESSION_ATTR));

        if (exprAttr != null) {
            factory.addEvent(featureStack.peek(), id, action, exprAttr.getValue());
        } else {
            factory.addEvent(featureStack.peek(), id, action);
        }
    }

    // ===== END ELEMENT =====
    public void handleEndElement(EndElement element) throws XMLStreamException {
        String tag = element.getName().getLocalPart();

        switch (tag) {

            case FEATURE_TAG -> featureStack.pop();

            case OPTIONAL_TAG, MANDATORY_TAG, OR_TAG, ALTERNATIVE_TAG -> groupStack.pop();

            case EVENTS_TAG -> {
                if (!contextStack.isEmpty() && contextStack.peek() == Context.EVENTS_DECLARATION) {
                    contextStack.pop();
                }
            }

            case CAUSALITY_TAG -> currentCausalityTarget = null;

            case BUNDLE_TAG -> {
                Set<String> bundle = bundleStack.pop();
                contextStack.pop();
                if (currentCausalityTarget != null) {
                    factory.addCausality(featureStack.peek(), bundle, currentCausalityTarget);
                }
            }

            case CONFLICT_TAG -> {
                contextStack.pop();
                if (conflictStack.size() == 2) {
                    Set<String> s1 = conflictStack.pop();
                    Set<String> s2 = conflictStack.pop();
                    factory.addConflicts(featureStack.peek(), s1, s2);
                } else {
                    LOG.warn("Invalid conflict definition");
                }
            }

            case BFM_TAG -> factory.updateAllEventFexpr();
        }
    }

    // ===== CHARACTERS =====
    public void handleCharacters(Characters element) throws XMLStreamException {
        this.charValue = element.asCharacters().getData().trim();
    }
}