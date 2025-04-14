package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.solver.Group;
import uk.kcl.info.bfm.BehavioralFeature;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.ConflictRelation;
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

    void printConflicts(XMLStreamWriter writer, Iterator<ConflictRelation> conflicts) throws XMLStreamException;
}
