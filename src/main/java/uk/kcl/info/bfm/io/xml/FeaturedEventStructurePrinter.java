package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;
import static uk.kcl.info.bfm.io.xml.FeaturedEventStructureHandler.*;

public class FeaturedEventStructurePrinter extends BundleEventStructurePrinter {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturedEventStructurePrinter.class);

    public FeaturedEventStructurePrinter() {}

    @Override
    public void printElement(XMLStreamWriter xtw, BundleEventStructure bes) throws XMLStreamException {
        LOG.trace("Printing FES element");
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
            Feature<?> feature = this.getFES().getFeature(event);
            LOG.trace(this.getFES().toString());
            LOG.trace(feature.toString());
            xtw.writeAttribute(FEATURE_ATTR, feature.getFeatureName());
            FExpression fexpr = this.getFES().getFExpression(event);
            if(!fexpr.equals(FExpression.trueValue())){
                xtw.writeAttribute(FEXPRESSION_ATTR, fexpr.applySimplification().toCnf().toString());
            }
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Events");
    }

    private FeaturedEventStructure<?> getFES() {
        return (FeaturedEventStructure<?>)this.bes;
    }

}
