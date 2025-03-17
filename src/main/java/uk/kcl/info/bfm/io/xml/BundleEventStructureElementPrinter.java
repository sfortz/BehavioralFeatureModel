package uk.kcl.info.bfm.io.xml;

import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.ConflictRelation;
import uk.kcl.info.bfm.Event;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface BundleEventStructureElementPrinter {
    void printElement(XMLStreamWriter writer, BundleEventStructure bes) throws XMLStreamException;

    void printEvents(XMLStreamWriter writer, Iterator<Event>  events) throws XMLStreamException;

    void printCausalities(XMLStreamWriter writer) throws XMLStreamException;

    void printConflicts(XMLStreamWriter writer, Iterator<ConflictRelation> conflicts) throws XMLStreamException;

}
