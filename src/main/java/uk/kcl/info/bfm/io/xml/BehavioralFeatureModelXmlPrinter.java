/*
 *
 *  * Copyright 2025 Sophie Fortz
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package uk.kcl.info.bfm.io.xml;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import uk.kcl.info.bfm.BehavioralFeatureModel;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class BehavioralFeatureModelXmlPrinter {
    protected OutputStream output;
    private final BehavioralFeatureModelElementPrinter bfmPrinter;

    public BehavioralFeatureModelXmlPrinter(OutputStream output, BehavioralFeatureModelElementPrinter bfmPrinter) {
        this.output = output;
        this.bfmPrinter = bfmPrinter;
    }

    public BehavioralFeatureModelXmlPrinter(File outputFile, BehavioralFeatureModelElementPrinter bfmPrinter) throws FileNotFoundException {
        this(new FileOutputStream(outputFile), bfmPrinter);
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public void print(BehavioralFeatureModel bfm) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        IndentingXMLStreamWriter xtw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(this.output));
        xtw.setIndentStep("    ");
        xtw.writeStartDocument("UTF-8","1.0");
        this.bfmPrinter.printElement(xtw, bfm);
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }
}