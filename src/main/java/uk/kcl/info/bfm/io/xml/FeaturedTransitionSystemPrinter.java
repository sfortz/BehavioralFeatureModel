package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.Transition;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.io.xml.TransitionSystemPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class FeaturedTransitionSystemPrinter extends TransitionSystemPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturedTransitionSystemPrinter.class);

    public FeaturedTransitionSystemPrinter() {}

    @Override
    public void printElement(XMLStreamWriter xtw, TransitionSystem ts) throws XMLStreamException {
        LOG.trace("Printing FTS element");
        xtw.writeStartElement("fts");
        this.ts = ts;
        xtw.writeStartElement("start");
        xtw.writeCharacters(ts.getInitialState().getName());
        xtw.writeEndElement();
        xtw.writeStartElement("states");
        this.printElement(xtw, ts.states());
        xtw.writeEndElement();
        xtw.writeEndElement();
        this.ts = null;
    }

    @Override
    public void printElement(XMLStreamWriter xtw, Transition transition) throws XMLStreamException {
        LOG.trace("Printing transition element");
        xtw.writeStartElement("transition");
        xtw.writeAttribute("action", transition.getAction().getName());
        xtw.writeAttribute("target", transition.getTarget().getName());
        FExpression fexpr = this.getFTS().getFExpression(transition);
        xtw.writeAttribute("fexpression", fexpr.toString());
        xtw.writeEndElement();
    }

    private FeaturedTransitionSystem getFTS() {
        return (FeaturedTransitionSystem)this.ts;
    }
}
