package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.ParserUtil;
import be.vibes.fexpression.exception.ParserException;
import be.vibes.ts.Action;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.FeaturedTransitionSystemFactory;
import be.vibes.ts.io.xml.TransitionSystemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;


public class FeaturedTransitionSystemHandler extends TransitionSystemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturedTransitionSystemHandler.class);

    public static final String FEXPRESSION_ATTR = "fexpression";

    @Override
    protected void handleStartTransitionTag(StartElement element) throws XMLStreamException {
        LOG.trace("Starting transition");

        // Get target
        String to = element.getAttributeByName(QName.valueOf(TARGET_ATTR)).getValue();

        // Get action
        String act = element.getAttributeByName(QName.valueOf(ACTION_ATTR)).getValue();
        if (act == null){
            act = Action.EPSILON_ACTION;
        }

        // Get feature expression
        Attribute exprAtt = element.getAttributeByName(QName.valueOf(FEXPRESSION_ATTR));
        FExpression fexpr = FExpression.trueValue();
        if (exprAtt != null) {
            String expr = exprAtt.getValue();
            if (expr != null) {
                try {
                    fexpr = ParserUtil.getInstance().parse(expr);
                } catch (ParserException e) {
                    LOG.error("Exception while parsing fexpression {}!", expr, e);
                    throw new XMLStreamException("Exception while parsing fexpression " + expr, e);
                }
            }
        }

        // add transition
        getFactory().addTransition(this.currentState, act, fexpr, to);
    }

    @Override
    protected void handleEndStartTag(EndElement element) throws XMLStreamException {
        LOG.trace("Ending start");
        this.factory = new FeaturedTransitionSystemFactory(this.charValue);
    }

    private FeaturedTransitionSystemFactory getFactory(){
        return (FeaturedTransitionSystemFactory) this.factory;
    }

    @Override
    public FeaturedTransitionSystem geTransitionSystem() {
        return getFactory().build();
    }

}

