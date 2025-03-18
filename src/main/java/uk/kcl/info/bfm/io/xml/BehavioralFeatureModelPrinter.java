package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import be.vibes.solver.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static be.vibes.solver.io.xml.FeatureModelHandler.ALTERNATIVE_TAG;
import static be.vibes.solver.io.xml.FeatureModelHandler.EXCLUSIONS_TAG;
import static be.vibes.solver.io.xml.FeatureModelHandler.MANDATORY_TAG;
import static be.vibes.solver.io.xml.FeatureModelHandler.OPTIONAL_TAG;
import static be.vibes.solver.io.xml.FeatureModelHandler.OR_TAG;
import static be.vibes.solver.io.xml.FeatureModelHandler.REQUIREMENTS_TAG;
import static uk.kcl.info.bfm.io.xml.BehavioralFeatureModelHandler.*;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.EVENTS_TAG;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.EVENT_TAG;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.ID_ATTR;

public class BehavioralFeatureModelPrinter implements BehavioralFeatureModelElementPrinter{
    private static final Logger LOG = LoggerFactory.getLogger(BehavioralFeatureModelPrinter.class);
    private BehavioralFeatureModel bfm;

    public BehavioralFeatureModelPrinter() {
    }

    @Override
    public void printElement(XMLStreamWriter xtw, BehavioralFeatureModel bfm) throws XMLStreamException {
        LOG.trace("Printing BFM element");
        xtw.writeStartElement(BFM_TAG);
        this.bfm = bfm;
        //this.bf = bfm.getRootFeature();
        xtw.writeAttribute(NAMESPACE_ATTR, bfm.getNamespace());
        this.printElement(xtw, bfm.getRootFeature()); //bf);
        xtw.writeEndElement();
        //this.bf = null;
        this.bfm = null;
    }

    @Override
    public void printElement(XMLStreamWriter xtw, BehavioralFeature bf) throws XMLStreamException {
        LOG.trace("Printing feature element");
        xtw.writeStartElement(FEATURE_TAG);
        xtw.writeAttribute(NAME_ATTR, bf.getFeatureName());

        //print events
        this.printEvents(xtw, bf.events());

        //print groups
        for(Group group: bf.getChildren()){
            this.printElement(xtw, group);
        }

        //print F constraints
        if(bf.getTotalNumberOfConstraints() > 0){
            xtw.writeStartElement(FEATURE_CONSTRAINTS_TAG);
            if(bf.getNumberOfExclusionConstraints() > 0){
                this.printExclusions(xtw, bf.getExclusions());
            }
            if(bf.getNumberOfRequirementConstraints() > 0){
                this.printRequirements(xtw, bf.getRequirements());
            }
            xtw.writeEndElement();
        }

        //print E constraints
        if (bf.getConflictsCount() > 0 || bf.getCausalitiesCount() > 0) {
            xtw.writeStartElement(EVENT_CONSTRAINTS_TAG);

            if (bf.getCausalitiesCount() > 0) {
                this.printCausalities(xtw, bf);
            }
            if (bf.getConflictsCount() > 0) {
                this.printConflicts(xtw, bf.conflicts());
            }

            xtw.writeEndElement();
        }

        xtw.writeEndElement();
    }

    @Override
    public void printEvents(XMLStreamWriter xtw, Iterator<Event> iterator) throws XMLStreamException {
        LOG.trace("Starting Events");

        if(iterator.hasNext()){
            xtw.writeStartElement(EVENTS_TAG);
            while(iterator.hasNext()) {
                Event event = iterator.next();
                LOG.trace("Printing event element");
                xtw.writeStartElement(EVENT_TAG);
                xtw.writeAttribute(ID_ATTR, event.getName());
                FExpression fexpr = this.bfm.getFExpression(event); // getFExpression(event);
                if(fexpr != null && !fexpr.equals(FExpression.trueValue())){
                    xtw.writeAttribute(FEXPRESSION_ATTR, fexpr.applySimplification().toCnf().toString());
                }
                xtw.writeCharacters(" ");
                xtw.writeEndElement();
            }
            xtw.writeEndElement();
        }
        LOG.trace("End Events");
    }

    @Override
    public void printElement(XMLStreamWriter xtw, Group group) throws XMLStreamException {
        LOG.trace("Printing group element");
        switch (group.GROUPTYPE){
            case OR -> xtw.writeStartElement(OR_TAG);
            case ALTERNATIVE -> xtw.writeStartElement(ALTERNATIVE_TAG);
            case MANDATORY -> xtw.writeStartElement(MANDATORY_TAG);
            case OPTIONAL -> xtw.writeStartElement(OPTIONAL_TAG);
        }

        for(Feature feature: group.getFeatures()){
            BehavioralFeature f = bfm.getFeature(feature.getFeatureName());
            printElement(xtw, f);
        }
        xtw.writeEndElement();
    }

    @Override
    public void printExclusions(XMLStreamWriter xtw, List<ExclusionConstraint> exclusions) throws XMLStreamException {
        xtw.writeStartElement(EXCLUSIONS_TAG);
        for (ExclusionConstraint constraint : exclusions) {
            printElement(xtw, constraint);
        }
        xtw.writeEndElement();
    }

    @Override
    public void printRequirements(XMLStreamWriter xtw, List<RequirementConstraint> requirements) throws XMLStreamException {
        xtw.writeStartElement(REQUIREMENTS_TAG);
        for (RequirementConstraint constraint : requirements) {
            printElement(xtw, constraint);
        }
        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, ExclusionConstraint constraint) throws XMLStreamException {
        LOG.trace("Printing exclusion element");
        xtw.writeStartElement(EXCLUDE_TAG);
        xtw.writeAttribute(CONFLICT1_ATTR, constraint.getLeft().getLiteral());
        xtw.writeAttribute(CONFLICT2_ATTR, constraint.getRight().getLiteral());
        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, RequirementConstraint constraint) throws XMLStreamException {
        LOG.trace("Printing requirement element");
        xtw.writeStartElement(REQUIRES_TAG);
        xtw.writeAttribute(FEATURE_ATTR, constraint.getRight().getLiteral());
        xtw.writeAttribute(DEPENDENCY_ATTR, constraint.getLeft().getLiteral());
        xtw.writeEndElement();
    }

    @Override
    public void printCausalities(XMLStreamWriter xtw, BehavioralFeature bf) throws XMLStreamException {
        LOG.trace("Starting Causalities");
        xtw.writeStartElement(CAUSALITIES_TAG);
        for(Event ev: bf.getAllRecursiveEvents()){
            Iterator<CausalityRelation> causalities = bf.getCausalities(ev);
            if(causalities.hasNext()){
                LOG.trace("Printing causality element");
                xtw.writeStartElement(CAUSALITY_TAG);
                xtw.writeAttribute(TARGET_ATTR, ev.getName());
                while(causalities.hasNext()) {
                    CausalityRelation causality = causalities.next();
                    if (!causality.getBundle().isEmpty()) {
                        printBundle(xtw, causality.getBundle());
                    }
                }
                xtw.writeEndElement();
            }
        }
        xtw.writeEndElement();
        LOG.trace("End Causalities");
    }

    private void printBundle(XMLStreamWriter xtw, Set<Event> bundle) throws XMLStreamException {
        xtw.writeStartElement(BUNDLE_TAG);
        for (Event ev : bundle) {
            xtw.writeStartElement(EVENT_TAG);
            xtw.writeAttribute(ID_ATTR, ev.getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
    }


    @Override
    public void printConflicts(XMLStreamWriter xtw, Iterator<ConflictRelation> iterator) throws XMLStreamException {
        LOG.trace("Starting Conflicts");
        xtw.writeStartElement(CONFLICTS_TAG);
        while(iterator.hasNext()) {
            ConflictRelation conflict = iterator.next();
            LOG.trace("Printing conflict element");
            xtw.writeStartElement(CONFLICT_TAG);
            xtw.writeStartElement(EVENT_TAG);
            xtw.writeAttribute(ID_ATTR, conflict.getEvent1().getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
            xtw.writeStartElement(EVENT_TAG);
            xtw.writeAttribute(ID_ATTR, conflict.getEvent2().getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Conflicts");
    }
}
