package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;
import java.util.Objects;

public class FeaturedEventStructurePrinter extends BundleEventStructurePrinter {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturedEventStructurePrinter.class);

    public FeaturedEventStructurePrinter() {}

    @Override
    public void printElement(XMLStreamWriter xtw, BundleEventStructure bes) throws XMLStreamException {
        LOG.trace("Printing FES element");
        xtw.writeStartElement("fes");
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
            Feature feature = this.getFES().getFeature(event);
            LOG.trace(this.getFES().toString());
            LOG.trace(feature.toString());
            xtw.writeAttribute("feature", feature.getFeatureName());
            FExpression fexpr = this.getFES().getFExpression(event);
            if(!fexpr.equals(FExpression.trueValue())){
                xtw.writeAttribute("fexpression", fexpr.applySimplification().toCnf().toString());
            }
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Events");
    }

    private FeaturedEventStructure getFES() {
        return (FeaturedEventStructure)this.bes;
    }

}
