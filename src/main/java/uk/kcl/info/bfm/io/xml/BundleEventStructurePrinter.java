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
        xtw.writeStartElement("bes");
        this.bes = bes;
        this.printEvents(xtw, bes.events());
        this.printCausalities(xtw);
        this.printConflicts(xtw, bes.conflicts());
        xtw.writeEndElement();
        this.bes = null;
    }

    @Override
    public void printEvents(XMLStreamWriter xtw, Iterator<Event> iterator) throws XMLStreamException {
        LOG.trace("Starting Events");
        xtw.writeStartElement("events");
        while(iterator.hasNext()) {
            Event event = iterator.next();
            LOG.trace("Printing event element");
            xtw.writeStartElement("event");
            xtw.writeAttribute("id", event.getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Events");
    }

    @Override
    public void printCausalities(XMLStreamWriter xtw) throws XMLStreamException {
        LOG.trace("Starting Causalities");
        xtw.writeStartElement("causalities");
        for(Event ev: bes.getAllEvents()){
            LOG.trace("Printing causality element");
            xtw.writeStartElement("causality");
            xtw.writeAttribute("target", ev.getName());
            for (Iterator<CausalityRelation> causalities = bes.getCausalities(ev); causalities.hasNext();) {
                CausalityRelation causality = causalities.next();
                printBundle(xtw, causality.getBundle());
            }
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Causalities");
    }

    private void printBundle(XMLStreamWriter xtw, Set<Event> bundle) throws XMLStreamException {
        xtw.writeStartElement("bundle");
        for (Event ev : bundle){
            xtw.writeStartElement("event");
            xtw.writeAttribute("id", ev.getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
    }

    @Override
    public void printConflicts(XMLStreamWriter xtw, Iterator<ConflictRelation> iterator) throws XMLStreamException {
        LOG.trace("Starting Conflicts");
        xtw.writeStartElement("conflicts");
        while(iterator.hasNext()) {
            ConflictRelation conflict = iterator.next();
            LOG.trace("Printing conflict element");
            xtw.writeStartElement("conflict");
            xtw.writeStartElement("event");
            xtw.writeAttribute("id", conflict.getEvent1().getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
            xtw.writeStartElement("event");
            xtw.writeAttribute("id", conflict.getEvent2().getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Conflicts");
    }
}
