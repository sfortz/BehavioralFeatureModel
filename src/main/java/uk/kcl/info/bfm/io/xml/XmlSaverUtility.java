package uk.kcl.info.bfm.io.xml;

import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.xml.TransitionSystemPrinter;
import be.vibes.ts.io.xml.XmlSavers;
import uk.kcl.info.bfm.BundleEventStructure;
import uk.kcl.info.bfm.FeaturedEventStructure;
import uk.kcl.info.bfm.exceptions.BundleEventStructureDefinitionException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class XmlSaverUtility extends XmlSavers {

    public XmlSaverUtility() {}

    public static void save(TransitionSystem ts, OutputStream out) throws TransitionSystemDefinitionException {
        TransitionSystemPrinter printer = new TransitionSystemPrinter();
        TransitionSystemXmlPrinter xmlOut = new TransitionSystemXmlPrinter(out, printer);

        try {
            xmlOut.print(ts);
        } catch (XMLStreamException e) {
            throw new TransitionSystemDefinitionException("Exception while printing XML!", e);
        }
    }

    public static void save(TransitionSystem ts, File out) throws TransitionSystemDefinitionException {
        try {
            save(ts, new FileOutputStream(out));
        } catch (FileNotFoundException e) {
            throw new TransitionSystemDefinitionException("Output file not found!", e);
        }
    }

    public static void save(TransitionSystem ts, String outputFileName) throws TransitionSystemDefinitionException {
        save(ts, new File(outputFileName));
    }

    public static void save(FeaturedTransitionSystem fts, OutputStream out) throws TransitionSystemDefinitionException {
        FeaturedTransitionSystemPrinter printer = new FeaturedTransitionSystemPrinter();
        TransitionSystemXmlPrinter xmlOut = new TransitionSystemXmlPrinter(out, printer);

        try {
            xmlOut.print(fts);
        } catch (XMLStreamException var5) {
            throw new TransitionSystemDefinitionException("Exception while printing XML!", var5);
        }
    }

    public static void save(FeaturedTransitionSystem fts, File out) throws TransitionSystemDefinitionException {
        try {
            save(fts, new FileOutputStream(out));
        } catch (FileNotFoundException var3) {
            throw new TransitionSystemDefinitionException("Output file not found!", var3);
        }
    }

    public static void save(FeaturedTransitionSystem fts, String outputFileName) throws TransitionSystemDefinitionException {
        save(fts, new File(outputFileName));
    }

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
}
