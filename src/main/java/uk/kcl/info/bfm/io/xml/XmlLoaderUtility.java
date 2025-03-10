package uk.kcl.info.bfm.io.xml;

import be.vibes.solver.FeatureModel;
import be.vibes.ts.io.xml.XmlLoaders;
import be.vibes.ts.io.xml.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XmlLoaderUtility extends XmlLoaders {

    private static final Logger LOG = LoggerFactory
            .getLogger(XmlLoaderUtility.class);

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

    public static FeaturedEventStructure loadFeaturedEventStructure(InputStream in, FeatureModel fm) throws BundleEventStructureDefinitionException {
        FeaturedEventStructureHandler handler = new FeaturedEventStructureHandler(fm);
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading FES", e);
            throw new BundleEventStructureDefinitionException("Error while reading FES!", e);
        }
        return handler.getBundleEventStructure();
    }

    public static FeaturedEventStructure loadFeaturedEventStructure(File xmlFile, FeatureModel fm) throws BundleEventStructureDefinitionException {
        try {
            return XmlLoaderUtility.loadFeaturedEventStructure(new FileInputStream(xmlFile), fm);
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading FES input ={}!", xmlFile, e);
            throw new BundleEventStructureDefinitionException("Error while loading FES!", e);
        }
    }

    public static FeaturedEventStructure loadFeaturedEventStructure(String xmlFile, FeatureModel fm) throws BundleEventStructureDefinitionException {
        return XmlLoaderUtility.loadFeaturedEventStructure(new File(xmlFile), fm);
    }

    public static BehavioralFeatureModel loadBehavioralFeatureModel(InputStream in) throws BehavioralFeatureModelDefinitionException {
        BehavioralFeatureModelHandler handler = new BehavioralFeatureModelHandler();
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading BFM", e);
            throw new BehavioralFeatureModelDefinitionException("Error while reading BFM!", e);
        }
        return handler.getBehavioralFeatureModel();
    }

    public static BehavioralFeatureModel loadBehavioralFeatureModel(File xmlFile) throws BehavioralFeatureModelDefinitionException {
        try {
            return XmlLoaderUtility.loadBehavioralFeatureModel(new FileInputStream(xmlFile));
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading BFM input ={}!", xmlFile, e);
            throw new BehavioralFeatureModelDefinitionException("Error while loading BFM!", e);
        }
    }

    public static BehavioralFeatureModel loadBehavioralFeatureModel(String xmlFile) throws BehavioralFeatureModelDefinitionException {
        return XmlLoaderUtility.loadBehavioralFeatureModel(new File(xmlFile));
    }
}
