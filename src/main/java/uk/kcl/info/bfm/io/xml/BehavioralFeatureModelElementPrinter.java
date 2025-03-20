package uk.kcl.info.bfm.io.xml;

import be.vibes.solver.Group;
import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import uk.kcl.info.bfm.BehavioralFeature;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.ConflictRelation;
import uk.kcl.info.bfm.Event;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;
import java.util.List;

public interface BehavioralFeatureModelElementPrinter {
    void printElement(XMLStreamWriter writer, BehavioralFeatureModel bfm) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, BehavioralFeature feature) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, Group<BehavioralFeature> group) throws XMLStreamException;

    void printExclusions(XMLStreamWriter xtw, List<ExclusionConstraint> exclusions) throws XMLStreamException;

    void printRequirements(XMLStreamWriter xtw, List<RequirementConstraint> requirements) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, ExclusionConstraint constraint) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, RequirementConstraint constraint) throws XMLStreamException;

    void printEvents(XMLStreamWriter writer, Iterator<Event>  events) throws XMLStreamException;

    void printCausalities(XMLStreamWriter xtw, BehavioralFeature bf) throws XMLStreamException;

    void printConflicts(XMLStreamWriter writer, Iterator<ConflictRelation> conflicts) throws XMLStreamException;
}
