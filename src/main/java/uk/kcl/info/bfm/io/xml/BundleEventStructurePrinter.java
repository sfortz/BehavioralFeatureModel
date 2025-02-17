package uk.kcl.info.bfm.io.xml;

import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.CausalityRelation;
import uk.kcl.info.bfm.ConflictRelation;
import uk.kcl.info.bfm.Event;
import java.util.Iterator;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleEventStructurePrinter implements BundleEventStructureElementPrinter{
    private static final Logger LOG = LoggerFactory.getLogger(BundleEventStructurePrinter.class);
    protected BundleEventStructure bes;

    public BundleEventStructurePrinter() {
    }

    @Override
    public void printElement(XMLStreamWriter xtw, BundleEventStructure bes) throws XMLStreamException {
        LOG.trace("Printing BES element");
        xtw.writeStartElement("bundleEventStructure");
        this.bes = bes;
        xtw.writeStartElement("events");
        this.printElement(xtw, bes.events());
        xtw.writeEndElement();
        xtw.writeStartElement("causalities");
        this.printElement(xtw, bes.causalities());
        xtw.writeEndElement();
        xtw.writeStartElement("conflicts");
        this.printElement(xtw, bes.conflicts());
        xtw.writeEndElement();
        xtw.writeEndElement();
        this.bes = null;
    }

    @Override
    public void printElement(XMLStreamWriter xtw, Iterator<?> iterator) throws XMLStreamException {
        while(iterator.hasNext()) {
            Object object = iterator.next();

            switch (object) {
                case Event event:
                    this.printElement(xtw, event);
                    break;
                case CausalityRelation causality:
                    this.printElement(xtw, causality);
                    break;
                case ConflictRelation conflict:
                    this.printElement(xtw, conflict);
                    break;
                default:
                    // Handle other cases or throw an exception if needed
                    throw new XMLStreamException("Unknown object type: " + object.getClass().getName());
            }
        }
    }


    @Override
    public void printElement(XMLStreamWriter xtw, Event event) throws XMLStreamException {
        LOG.trace("Printing event element");
        xtw.writeStartElement("event");
        xtw.writeAttribute("id", event.getName());
        xtw.writeCharacters(" ");
        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, CausalityRelation causality) throws XMLStreamException {
        LOG.trace("Printing causality element");
        xtw.writeStartElement("causality");
        xtw.writeAttribute("target", causality.getTarget().getName());

        xtw.writeStartElement("bundle");
        for (Event ev : causality.getBundle()){
            xtw.writeStartElement("event");
            xtw.writeAttribute("id", ev.getName());
            xtw.writeCharacters(" ");
        }
        xtw.writeEndElement();
        xtw.writeEndElement();
    }


    @Override
    public void printElement(XMLStreamWriter xtw, ConflictRelation conflict) throws XMLStreamException {
        LOG.trace("Printing conflict element");
        xtw.writeStartElement("conflict");
        xtw.writeStartElement("event");
        xtw.writeAttribute("id", conflict.getEvent1().getName());
        xtw.writeCharacters(" ");
        xtw.writeStartElement("event");
        xtw.writeAttribute("id", conflict.getEvent2().getName());
        xtw.writeCharacters(" ");
        xtw.writeEndElement();
    }
}
