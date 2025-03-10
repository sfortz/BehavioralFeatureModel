package uk.kcl.info.bfm.io.xml;

import be.vibes.ts.io.xml.XmlSavers;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class XmlSaverUtility extends XmlSavers {

    public XmlSaverUtility() {}

    public static void save(BundleEventStructure bes, OutputStream out) throws BundleEventStructureDefinitionException {
        BundleEventStructurePrinter printer = new BundleEventStructurePrinter();
        BundleEventStructureXmlPrinter xmlOut = new BundleEventStructureXmlPrinter(out, printer);

        try {
            xmlOut.print(bes);
        } catch (XMLStreamException var5) {
            throw new BundleEventStructureDefinitionException("Exception while printing XML!", var5);
        }
    }

    public static void save(BundleEventStructure bes, File out) throws BundleEventStructureDefinitionException {
        try {
            save(bes, new FileOutputStream(out));
        } catch (FileNotFoundException var3) {
            throw new BundleEventStructureDefinitionException("Output file not found!", var3);
        }
    }

    public static void save(BundleEventStructure bes, String outputFileName) throws BundleEventStructureDefinitionException {
        save(bes, new File(outputFileName));
    }

    public static void save(FeaturedEventStructure fes, OutputStream out) throws BundleEventStructureDefinitionException {
        FeaturedEventStructurePrinter printer = new FeaturedEventStructurePrinter();
        BundleEventStructureXmlPrinter xmlOut = new BundleEventStructureXmlPrinter(out, printer);

        try {
            xmlOut.print(fes);
        } catch (XMLStreamException var5) {
            throw new BundleEventStructureDefinitionException("Exception while printing XML!", var5);
        }
    }

    public static void save(FeaturedEventStructure fes, File out) throws BundleEventStructureDefinitionException {
        try {
            save(fes, new FileOutputStream(out));
        } catch (FileNotFoundException var3) {
            throw new BundleEventStructureDefinitionException("Output file not found!", var3);
        }
    }

    public static void save(FeaturedEventStructure fes, String outputFileName) throws BundleEventStructureDefinitionException {
        save(fes, new File(outputFileName));
    }

    public static void save(BehavioralFeatureModel bfm, OutputStream out) throws BehavioralFeatureModelDefinitionException {
        BehavioralFeatureModelPrinter printer = new BehavioralFeatureModelPrinter();
        BehavioralFeatureModelXmlPrinter xmlOut = new BehavioralFeatureModelXmlPrinter(out, printer);

        try {
            xmlOut.print(bfm);
        } catch (XMLStreamException var5) {
            throw new BehavioralFeatureModelDefinitionException("Exception while printing XML!", var5);
        }
    }

    public static void save(BehavioralFeatureModel bfm, File out) throws BehavioralFeatureModelDefinitionException {
        try {
            save(bfm, new FileOutputStream(out));
        } catch (FileNotFoundException var3) {
            throw new BehavioralFeatureModelDefinitionException("Output file not found!", var3);
        }
    }

    public static void save(BehavioralFeatureModel bfm, String outputFileName) throws BehavioralFeatureModelDefinitionException {
        save(bfm, new File(outputFileName));
    }
}
