package uk.kcl.info.bfm.io.xml;

import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.CausalityRelation;
import uk.kcl.info.bfm.ConflictSet;
import uk.kcl.info.bfm.Event;
import java.util.Iterator;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.*;

public class BundleEventStructurePrinter implements BundleEventStructureElementPrinter{
    private static final Logger LOG = LoggerFactory.getLogger(BundleEventStructurePrinter.class);
    protected BundleEventStructure bes;

    public BundleEventStructurePrinter() {
    }

    @Override
    public void printElement(XMLStreamWriter xtw, BundleEventStructure bes) throws XMLStreamException {
        LOG.trace("Printing BES element");
        xtw.writeStartElement(BES_TAG);
        this.bes = bes;
        this.printEvents(xtw, bes.events());
        this.printCausalities(xtw);
        this.printConflicts(xtw, bes.getConflictSetCopy());
        xtw.writeEndElement();
        this.bes = null;
    }

    @Override
    public void printEvents(XMLStreamWriter xtw, Iterator<Event> iterator) throws XMLStreamException {
        LOG.trace("Starting Events");
        xtw.writeStartElement(EVENTS_TAG);
        while(iterator.hasNext()) {
            Event event = iterator.next();
            LOG.trace("Printing event element");
            xtw.writeStartElement(EVENT_TAG);
            xtw.writeAttribute(ID_ATTR, event.getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Events");
    }

    @Override
    public void printCausalities(XMLStreamWriter xtw) throws XMLStreamException {
        LOG.trace("Starting Causalities");
        xtw.writeStartElement(CAUSALITIES_TAG);
        for(Event ev: bes.getAllEvents()){
            LOG.trace("Printing causality element");
            Iterator<CausalityRelation> causalities = bes.getAllCausalitiesOfEvent(ev);
            if(causalities.hasNext()){
                xtw.writeStartElement(CAUSALITY_TAG);
                xtw.writeAttribute(TARGET_ATTR, ev.getName());
                while (causalities.hasNext()) {
                    CausalityRelation causality = causalities.next();
                    printBundle(xtw, causality.getBundle());
                }
                xtw.writeEndElement();
            }
        }
        xtw.writeEndElement();
        LOG.trace("End Causalities");
    }

    private void printBundle(XMLStreamWriter xtw, Set<Event> bundle) throws XMLStreamException {
        xtw.writeStartElement(BUNDLE_TAG);
        for (Event ev : bundle){
            xtw.writeStartElement(EVENT_TAG);
            xtw.writeAttribute(ID_ATTR, ev.getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
    }

    @Override
    public void printConflicts(XMLStreamWriter xtw, ConflictSet conflicts) throws XMLStreamException {
        LOG.trace("Starting Conflicts");
        xtw.writeStartElement(CONFLICTS_TAG);
        Set<ConflictSet.Biclique> cliques = conflicts.findMinimalBicliqueEdgeCover();
        for (ConflictSet.Biclique c: cliques){
            LOG.trace("Printing conflict element");
            xtw.writeStartElement(CONFLICT_TAG);

            // Printing Set A
            xtw.writeStartElement(EVENTS_TAG);
            for(Event e: c.getA()){
                xtw.writeStartElement(EVENT_TAG);
                xtw.writeAttribute(ID_ATTR, e.getName());
                xtw.writeCharacters(" ");
                xtw.writeEndElement();
            }
            xtw.writeEndElement();

            // Printing Set B
            xtw.writeStartElement(EVENTS_TAG);
            for(Event e: c.getB()){
                xtw.writeStartElement(EVENT_TAG);
                xtw.writeAttribute(ID_ATTR, e.getName());
                xtw.writeCharacters(" ");
                xtw.writeEndElement();
            }
            xtw.writeEndElement();

            xtw.writeEndElement();
        }

        xtw.writeEndElement();
        LOG.trace("End Conflicts");
    }
}