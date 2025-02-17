package uk.kcl.info.bfm.io.xml;

import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.CausalityRelation;
import uk.kcl.info.bfm.ConflictRelation;
import uk.kcl.info.bfm.Event;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface BundleEventStructureElementPrinter {
    void printElement(XMLStreamWriter writer, BundleEventStructure bes) throws XMLStreamException;

    void printElement(XMLStreamWriter writer, Iterator<?> iterator) throws XMLStreamException;

    void printElement(XMLStreamWriter writer, Event event) throws XMLStreamException;

    void printElement(XMLStreamWriter writer, CausalityRelation causality) throws XMLStreamException;

    void printElement(XMLStreamWriter writer, ConflictRelation conflict) throws XMLStreamException;

}
