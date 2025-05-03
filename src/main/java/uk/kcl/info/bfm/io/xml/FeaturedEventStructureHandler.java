package uk.kcl.info.bfm.io.xml;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.ParserUtil;
import be.vibes.fexpression.exception.ParserException;
import be.vibes.solver.FeatureModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.FeaturedEventStructureFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class FeaturedEventStructureHandler extends BundleEventStructureHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturedEventStructureHandler.class);

    public static final String BES_TAG = "fes";
    public static final String FEATURE_ATTR = "feature";
    public static final String FEXPRESSION_ATTR = "fexpression";

    public FeaturedEventStructureHandler(FeatureModel<?> fm) {
        this.factory = new FeaturedEventStructureFactory(fm);
    }

    @Override
    protected void handleStartBesTag() {
        LOG.trace("Starting FES");
    }

    @Override
    protected void handleStartEventTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing event");
        String id = element.getAttributeByName(QName.valueOf(ID_ATTR)).getValue();
        if (bundleStack != null) {
            // If inside a bundle, add to the current bundle
            bundleStack.peek().add(id);
        } else if (conflictStack != null) {
            // If inside a conflict, add to the current conflict
            conflictStack.peek().add(id);
        } else {
            // Otherwise, it's a standalone event declaration

            Attribute featAtt = element.getAttributeByName(QName.valueOf(FEATURE_ATTR));
            String f;
            if (featAtt != null) {
                f = featAtt.getValue();
            } else {
                LOG.error("Exception while parsing event: no feature specified!");
                throw new XMLStreamException("Exception while parsing event: no feature specified!");
            }

            Attribute exprAtt = element.getAttributeByName(QName.valueOf(FEXPRESSION_ATTR));
            if (exprAtt != null) {
                String expr = exprAtt.getValue();
                if (expr != null) {
                    try {
                        FExpression fexpr = ParserUtil.getInstance().parse(expr);
                        getFactory().addEvent(id,new Feature<>(f),fexpr);
                    } catch (ParserException e) {
                        LOG.error("Exception while parsing fexpression {}!", expr, e);
                        throw new XMLStreamException("Exception while parsing fexpression " + expr, e);
                    }
                }
            } else {
                getFactory().addEvent(id,new Feature<>(f));
            }
        }
    }

    private FeaturedEventStructureFactory getFactory(){
        return (FeaturedEventStructureFactory) this.factory;
    }

    @Override
    public FeaturedEventStructure<?> getBundleEventStructure() {
        return getFactory().build();
    }

}
