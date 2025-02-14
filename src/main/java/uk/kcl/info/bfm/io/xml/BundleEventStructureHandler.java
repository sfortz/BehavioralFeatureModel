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
import uk.kcl.info.bfm.Event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

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
    private Stack<Set<String>> bundleStack = new Stack<>();
    private String currentCausalityTarget = null;
    private Stack<Set<String>> conflictStack = new Stack<>();

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

    private void handleStartConflictsTag() {
        LOG.trace("Starting Conflicts");
    }

    private void handleStartCausalitiesTag() {
        LOG.trace("Starting Causalities");
    }

    private void handleStartEventsTag() {
        LOG.trace("Starting Events");
    }

    private void handleStartBesTag() {
        LOG.trace("Starting BES");
    }

    private void handleStartEventTag(StartElement element) {
        LOG.trace("Processing event");
        String id = element.getAttributeByName(QName.valueOf(ID_ATTR)).getValue();
        if (!bundleStack.isEmpty()) {
            LOG.trace("BUNDLE");
            // If inside a bundle, add to the current bundle
            bundleStack.peek().add(id);
        } else if (!conflictStack.isEmpty()) {
            // If inside a conflict, add to the current conflict
            conflictStack.peek().add(id);
        } else {
            // Otherwise, it's a standalone event declaration
            LOG.trace("HELP");
            factory.addEvent(id);
        }
    }

    private void handleStartCausalityTag(StartElement element) {
        LOG.trace("Processing causality");
        currentCausalityTarget = element.getAttributeByName(QName.valueOf(TARGET_ATTR)).getValue();
    }

    private void handleStartBundleTag() {
        LOG.trace("Processing bundle");
        bundleStack.push(new HashSet<>()); // Create new bundle set
    }

    private void handleStartConflictTag() {
        LOG.trace("Processing conflict");
        conflictStack.push(new HashSet<>()); // Create new Conflict set
    }

    public void handleEndElement(EndElement element) {
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
                if (!conflictStack.isEmpty()) {
                    Set<String> conflictEvents = conflictStack.pop();
                    if (conflictEvents.size() == 2) {
                        Iterator<String> it = conflictEvents.iterator();
                        factory.addConflict(new Event(it.next()), new Event(it.next()));
                    } else {
                        LOG.warn("Invalid conflict definition: {}", conflictEvents);
                    }
                }
                break;
            case BES_TAG:
                LOG.trace("Ending bundle event structure");
                break;
        }
    }

    public void handleCharacters(Characters element) {
        this.charValue = element.asCharacters().getData().trim();
    }
}
