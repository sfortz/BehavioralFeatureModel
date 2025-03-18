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
    public static final String BFM_TAG = "bfm";

    public static final String FEATURE_TAG = "feature";
    public static final String OPTIONAL_TAG = "optional";
    public static final String MANDATORY_TAG = "mandatory";
    public static final String ALTERNATIVE_TAG = "alternative";
    public static final String OR_TAG = "or";

    public static final String FEATURE_CONSTRAINTS_TAG = "feature_constraints";
    public static final String EXCLUSIONS_TAG = "exclusions";
    public static final String EXCLUDE_TAG = "exclude";
    public static final String REQUIREMENTS_TAG = "requirements";
    public static final String REQUIRES_TAG = "requires";

    public static final String NAMESPACE_ATTR = "namespace";
    public static final String NAME_ATTR = "name";
    public static final String CONFLICT1_ATTR = "conflict1";
    public static final String CONFLICT2_ATTR = "conflict2";
    public static final String FEATURE_ATTR = "feature";
    public static final String DEPENDENCY_ATTR = "dependency";

    public static final String EVENT_CONSTRAINTS_TAG = "event_constraints";
    public static final String EVENTS_TAG = "events";
    public static final String EVENT_TAG = "event";
    public static final String CAUSALITIES_TAG = "causalities";
    public static final String CAUSALITY_TAG = "causality";
    public static final String BUNDLE_TAG = "bundle";
    public static final String CONFLICTS_TAG = "conflicts";
    public static final String CONFLICT_TAG = "conflict";

    public static final String ID_ATTR = "id";
    public static final String FEXPRESSION_ATTR = "fexpression";
    public static final String TARGET_ATTR = "target";

    private static final Logger LOG = LoggerFactory.getLogger(BehavioralFeatureModelHandler.class);

    protected BehavioralFeatureModelFactory factory;
    protected String charValue;

    // Stack to track FM depth
    protected Stack<Group> groupStack = new Stack<>();
    protected Stack<BehavioralFeature> featureStack = new Stack<>();

    // Stack to track nested bundles
    protected Stack<Set<String>> bundleStack = new Stack<>();
    protected String currentCausalityTarget = null;
    protected Stack<Set<String>> conflictStack = new Stack<>();

    public BehavioralFeatureModelHandler() {
        this.factory = new BehavioralFeatureModelFactory();
    }

    public BehavioralFeatureModel getBehavioralFeatureModel() {
        return (BehavioralFeatureModel) this.factory.build();
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
            case BFM_TAG:
                handleStartBfmTag(element);
                break;
            case FEATURE_TAG:
                handleStartFeatureTag(element);
                break;
            case EVENTS_TAG:
                handleStartEventsTag();
                break;
            case EVENT_TAG:
                handleStartEventTag(element);
                break;
            case OPTIONAL_TAG:
                handleStartOptionalTag(element);
                break;
            case MANDATORY_TAG:
                handleStartMandatoryTag(element);
                break;
            case OR_TAG:
                handleStartOrTag(element);
                break;
            case ALTERNATIVE_TAG:
                handleStartAlternativeTag(element);
                break;
            case FEATURE_CONSTRAINTS_TAG:
                handleStartFConstraintTag();
                break;
            case EXCLUSIONS_TAG:
                handleStartExclusionsTag();
                break;
            case EXCLUDE_TAG:
                handleStartExcludeTag(element);
                break;
            case REQUIREMENTS_TAG:
                handleStartRequirementsTag();
                break;
            case REQUIRES_TAG:
                handleStartRequiresTag(element);
                break;
            case EVENT_CONSTRAINTS_TAG:
                handleStartEConstraintTag();
                break;
            case CAUSALITIES_TAG:
                handleStartCausalitiesTag();
                break;
            case CAUSALITY_TAG:
                handleStartCausalityTag(element);
                break;
            case BUNDLE_TAG:
                handleStartBundleTag();
                break;
            case CONFLICTS_TAG:
                handleStartConflictsTag();
                break;
            case CONFLICT_TAG:
                handleStartConflictTag();
                break;
            default:
                LOG.debug("Unknown element: {}", tag);
        }
    }

    protected void handleStartBfmTag(StartElement element) throws XMLStreamException {
        LOG.trace("Starting BFM");
        LOG.trace("Processing namespace");
        String namespace = element.getAttributeByName(QName.valueOf(NAMESPACE_ATTR)).getValue();
        factory.setNamespace(namespace);
    }

    protected void handleStartOptionalTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing optional group");
        Group currentGroup = this.factory.addChild(this.featureStack.peek(), Group.GroupType.OPTIONAL);
        this.groupStack.push(currentGroup);
    }

    protected void handleStartMandatoryTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing mandatory group");
        Group currentGroup = this.factory.addChild(this.featureStack.peek(), Group.GroupType.MANDATORY);
        this.groupStack.push(currentGroup);
    }

    protected void handleStartOrTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing or group");
        Group currentGroup = this.factory.addChild(this.featureStack.peek(), Group.GroupType.OR);
        this.groupStack.push(currentGroup);
    }

    protected void handleStartAlternativeTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing alternative group");
        Group currentGroup = this.factory.addChild(this.featureStack.peek(), Group.GroupType.ALTERNATIVE);
        this.groupStack.push(currentGroup);
    }

    protected void handleStartFeatureTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing feature");
        String featureName = element.getAttributeByName(QName.valueOf(NAME_ATTR)).getValue();
        BehavioralFeature currentFeature;
        if (this.groupStack.isEmpty()) {
            currentFeature = this.factory.setRootFeature(featureName);
        } else {
            currentFeature = this.factory.addFeature(this.groupStack.peek(), featureName);
        }

        this.featureStack.push(currentFeature);
    }

    protected void handleStartFConstraintTag() throws XMLStreamException {
        LOG.trace("Starting Feature Constraints");
    }

    protected void handleStartExclusionsTag() throws XMLStreamException {
        LOG.trace("Starting Exclusions");
    }

    protected void handleStartRequirementsTag() throws XMLStreamException {
        LOG.trace("Starting Requirements");
    }

    protected void handleStartExcludeTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing Exclusion");
        String c1 = element.getAttributeByName(QName.valueOf(CONFLICT1_ATTR)).getValue();
        String c2 = element.getAttributeByName(QName.valueOf(CONFLICT2_ATTR)).getValue();
        this.factory.addExclusionConstraint(featureStack.peek(), c1, c2);
    }

    protected void handleStartRequiresTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing Requirements");
        String feature = element.getAttributeByName(QName.valueOf(FEATURE_ATTR)).getValue();
        String dependency = element.getAttributeByName(QName.valueOf(DEPENDENCY_ATTR)).getValue();
        this.factory.addRequirementConstraint(featureStack.peek(), feature, dependency);
    }

    protected void handleStartEConstraintTag() throws XMLStreamException {
        LOG.trace("Starting Event Constraints");
    }

    protected void handleStartConflictsTag() throws XMLStreamException {
        LOG.trace("Starting Conflicts");
    }

    protected void handleStartCausalitiesTag() throws XMLStreamException {
        LOG.trace("Starting Causalities");
    }

    protected void handleStartEventsTag() throws XMLStreamException {
        LOG.trace("Starting Events");
    }

    protected void handleStartEventTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing event");
        String id = element.getAttributeByName(QName.valueOf(ID_ATTR)).getValue();
        if (!bundleStack.isEmpty()) {
            // If inside a bundle, add to the current bundle
            bundleStack.peek().add(id);
        } else if (!conflictStack.isEmpty()) {
            // If inside a conflict, add to the current conflict
            conflictStack.peek().add(id);
        } else {
            // Otherwise, it's a standalone event declaration
            Attribute exprAtt = element.getAttributeByName(QName.valueOf(FEXPRESSION_ATTR));
            if (exprAtt != null) {
                String expr = exprAtt.getValue();
                if (expr != null) {
                    try {
                        FExpression fexpr = ParserUtil.getInstance().parse(expr);
                        factory.addEvent(featureStack.peek(), id, fexpr);
                    } catch (ParserException e) {
                        LOG.error("Exception while parsing fexpression {}!", expr, e);
                        throw new XMLStreamException("Exception while parsing fexpression " + expr, e);
                    }
                }
            } else {
                factory.addEvent(featureStack.peek(), id);
            }
        }
    }

    protected void handleStartCausalityTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing causality");
        currentCausalityTarget = element.getAttributeByName(QName.valueOf(TARGET_ATTR)).getValue();
    }

    protected void handleStartBundleTag() throws XMLStreamException {
        LOG.trace("Processing bundle");
        bundleStack.push(new HashSet<>()); // Create new bundle set
    }

    protected void handleStartConflictTag() throws XMLStreamException {
        LOG.trace("Processing conflict");
        conflictStack.push(new HashSet<>()); // Create new Conflict set
    }

    public void handleEndElement(EndElement element) throws XMLStreamException {
        String tag = element.getName().getLocalPart();
        switch (tag) {
            case FEATURE_TAG:
                LOG.trace("Ending feature");
                featureStack.pop();
                break;
            case MANDATORY_TAG, OPTIONAL_TAG, OR_TAG, ALTERNATIVE_TAG:
                LOG.trace("Ending group");
                groupStack.pop();
                break;
            case EXCLUDE_TAG:
                LOG.trace("Ending Exclusion");
                break;
            case REQUIRES_TAG:
                LOG.trace("Ending Requirement");
                break;
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
                    factory.addCausality(featureStack.peek(), bundleStack.pop(), currentCausalityTarget);
                }
                break;
            case CONFLICT_TAG:
                LOG.trace("Ending conflict");
                if (!conflictStack.isEmpty()) {
                    Set<String> conflictEvents = conflictStack.pop();
                    if (conflictEvents.size() == 2) {
                        Iterator<String> it = conflictEvents.iterator();
                        factory.addConflict(featureStack.peek(), it.next(), it.next());
                    } else {
                        LOG.warn("Invalid conflict definition: {}", conflictEvents);
                    }
                }
                break;
            case BFM_TAG:
                LOG.trace("Ending behavioral feature model");
                break;
        }
    }

    public void handleCharacters(Characters element) throws XMLStreamException {
        this.charValue = element.asCharacters().getData().trim();
    }
}