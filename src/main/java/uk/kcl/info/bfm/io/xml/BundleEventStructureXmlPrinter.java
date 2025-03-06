package uk.kcl.info.bfm.io.xml;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import uk.kcl.info.bfm.BundleEventStructure;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class BundleEventStructureXmlPrinter {
    protected OutputStream output;
    private final BundleEventStructureElementPrinter besPrinter;

    public BundleEventStructureXmlPrinter(OutputStream output, BundleEventStructureElementPrinter besPrinter) {
        this.output = output;
        this.besPrinter = besPrinter;
    }

    public BundleEventStructureXmlPrinter(File outputFile, BundleEventStructureElementPrinter besPrinter) throws FileNotFoundException {
        this(new FileOutputStream(outputFile), besPrinter);
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public void print(BundleEventStructure bes) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        IndentingXMLStreamWriter xtw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(this.output));
        xtw.setIndentStep("    ");
        xtw.writeStartDocument("UTF-8","1.0");
        this.besPrinter.printElement(xtw, bes);
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }
}
