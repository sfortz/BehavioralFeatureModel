package uk.kcl.info.bfm.io.xml;

import be.vibes.ts.io.xml.XmlSavers;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class XmlSaverUtility extends XmlSavers {

    public XmlSaverUtility() {}

    /*
    public static void save(FeaturedTransitionSystem fts, OutputStream out) throws BundleEventStructureDefinitionException {
        FeaturedTransitionSystemPrinter printer = new FeaturedTransitionSystemPrinter();
        TransitionSystemXmlPrinter xmlOut = new TransitionSystemXmlPrinter(out, printer);

        try {
            xmlOut.print(fts);
        } catch (XMLStreamException var5) {
            throw new BundleEventStructureDefinitionException("Exception while printing XML!", var5);
        }
    }

    public static void save(FeaturedTransitionSystem fts, File out) throws BundleEventStructureDefinitionException {
        try {
            save(fts, new FileOutputStream(out));
        } catch (FileNotFoundException var3) {
            throw new BundleEventStructureDefinitionException("Output file not found!", var3);
        }
    }

    public static void save(FeaturedTransitionSystem fts, String outputFileName) throws BundleEventStructureDefinitionException {
        save(fts, new File(outputFileName));
    }*/


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

}
