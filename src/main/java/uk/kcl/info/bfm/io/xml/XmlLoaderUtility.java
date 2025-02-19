package uk.kcl.info.bfm.io.xml;

import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.xml.XmlLoaders;
import be.vibes.ts.io.xml.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XmlLoaderUtility extends XmlLoaders {

    private static final Logger LOG = LoggerFactory
            .getLogger(XmlLoaderUtility.class);

    public static FeaturedTransitionSystem loadFeaturedTransitionSystem(InputStream in) throws TransitionSystemDefinitionException {
        FeaturedTransitionSystemHandler handler = new FeaturedTransitionSystemHandler();
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading FTS!", e);
            throw new TransitionSystemDefinitionException("Error while reading FTS!", e);
        }
        return handler.geTransitionSystem();
    }

    public static FeaturedTransitionSystem loadFeaturedTransitionSystem(File xmlFile) throws TransitionSystemDefinitionException {
        try {
            return loadFeaturedTransitionSystem(new FileInputStream(xmlFile));
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading FTS input ={}!", xmlFile, e);
            throw new TransitionSystemDefinitionException("Error while loading FTS!", e);
        }
    }

    public static FeaturedTransitionSystem loadFeaturedTransitionSystem(String xmlFile) throws TransitionSystemDefinitionException {
        return loadFeaturedTransitionSystem(new File(xmlFile));
    }

    public static BundleEventStructure loadBundleEventStructure(InputStream in) throws BundleEventStructureDefinitionException {
        BundleEventStructureHandler handler = new BundleEventStructureHandler();
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading BES", e);
            throw new BundleEventStructureDefinitionException("Error while reading BES!", e);
        }
        return handler.getBundleEventStructure();
    }

    public static BundleEventStructure loadBundleEventStructure(File xmlFile) throws BundleEventStructureDefinitionException {
        try {
            return XmlLoaderUtility.loadBundleEventStructure(new FileInputStream(xmlFile));
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading BES input ={}!", xmlFile, e);
            throw new BundleEventStructureDefinitionException("Error while loading BES!", e);
        }
    }

    public static BundleEventStructure loadBundleEventStructure(String xmlFile) throws BundleEventStructureDefinitionException {
        return XmlLoaderUtility.loadBundleEventStructure(new File(xmlFile));
    }

    public static FeaturedEventStructure loadFeaturedEventStructure(InputStream in) throws BundleEventStructureDefinitionException {
        FeaturedEventStructureHandler handler = new FeaturedEventStructureHandler();
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading FES", e);
            throw new BundleEventStructureDefinitionException("Error while reading BES!", e);
        }
        return handler.getBundleEventStructure();
    }

    public static FeaturedEventStructure loadFeaturedEventStructure(File xmlFile) throws BundleEventStructureDefinitionException {
        try {
            return XmlLoaderUtility.loadFeaturedEventStructure(new FileInputStream(xmlFile));
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading FES input ={}!", xmlFile, e);
            throw new BundleEventStructureDefinitionException("Error while loading BES!", e);
        }
    }

    public static FeaturedEventStructure loadFeaturedEventStructure(String xmlFile) throws BundleEventStructureDefinitionException {
        return XmlLoaderUtility.loadFeaturedEventStructure(new File(xmlFile));
    }

}
