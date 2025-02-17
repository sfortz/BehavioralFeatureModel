package uk.kcl.info.bfm.io.xml;

import be.vibes.ts.TransitionSystem;
import be.vibes.ts.io.xml.TransitionSystemElementPrinter;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class TransitionSystemXmlPrinter {

    protected OutputStream output;
    private final TransitionSystemElementPrinter tsPrinter;

    public TransitionSystemXmlPrinter(OutputStream output, TransitionSystemElementPrinter tsPrinter) {
        this.output = output;
        this.tsPrinter = tsPrinter;
    }

    public TransitionSystemXmlPrinter(File outputFile, TransitionSystemElementPrinter tsPrinter) throws FileNotFoundException {
        this(new FileOutputStream(outputFile), tsPrinter);
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public void print(TransitionSystem ts) throws XMLStreamException {

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        IndentingXMLStreamWriter xtw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(this.output));
        xtw.setIndentStep("    ");
        xtw.writeStartDocument("UTF-8","1.0");
        this.tsPrinter.printElement(xtw, ts);
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }
}
