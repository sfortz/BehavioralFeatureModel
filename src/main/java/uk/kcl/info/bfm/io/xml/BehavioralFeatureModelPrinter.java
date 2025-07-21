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

import be.vibes.fexpression.FExpression;
import be.vibes.solver.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.kcl.info.bfm.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Iterator;
import java.util.Set;

import static uk.kcl.info.bfm.io.xml.BehavioralFeatureModelHandler.*;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.CONFLICTS_TAG;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.CONFLICT_TAG;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.EVENTS_TAG;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.EVENT_TAG;
import static uk.kcl.info.bfm.io.xml.BundleEventStructureHandler.ID_ATTR;

public class BehavioralFeatureModelPrinter implements BehavioralFeatureModelElementPrinter{
    private static final Logger LOG = LoggerFactory.getLogger(BehavioralFeatureModelPrinter.class);
    private BehavioralFeatureModel bfm;

    public BehavioralFeatureModelPrinter() {
    }

    @Override
    public void printElement(XMLStreamWriter xtw, BehavioralFeatureModel bfm) throws XMLStreamException {
        LOG.trace("Printing BFM element");
        xtw.writeStartElement(BFM_TAG);
        this.bfm = bfm;
        xtw.writeAttribute(NAMESPACE_ATTR, bfm.getNamespace());
        this.printElement(xtw, bfm.getRootFeature()); //bf);
        xtw.writeEndElement();
        this.bfm = null;
    }

    @Override
    public void printElement(XMLStreamWriter xtw, BehavioralFeature bf) throws XMLStreamException {
        LOG.trace("Printing feature element");
        xtw.writeStartElement(FEATURE_TAG);
        xtw.writeAttribute(NAME_ATTR, bf.getFeatureName());

        //print events
        this.printEvents(xtw, bf.getEventMap().keySet().iterator());

        //print groups
        for(Group<BehavioralFeature> group: bf.getChildren()){
            this.printElement(xtw, group);
        }

        //print F constraints
        if(bf.getNumberOfConstraints() > 0){
            xtw.writeStartElement(FEATURE_CONSTRAINTS_TAG);
            for (FExpression fexpr : bf.getConstraints()) {
                printElement(xtw, fexpr);
            }
            xtw.writeEndElement();
        }

        //print E constraints
        if (bf.getConflictsCount() > 0 || bf.getCausalitiesCount() > 0) {
            xtw.writeStartElement(EVENT_CONSTRAINTS_TAG);

            if (bf.getCausalitiesCount() > 0) {
                this.printCausalities(xtw, bf);
            }
            if (bf.getConflictsCount() > 0) {
                this.printConflicts(xtw, bf.getRootConflictSetCopy());
            }

            xtw.writeEndElement();
        }

        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, FExpression fexpr) throws XMLStreamException {
        LOG.trace("Printing constraint element");
        xtw.writeStartElement(FEATURE_CONSTRAINT_TAG);
        xtw.writeAttribute(FEXPRESSION_ATTR, fexpr.toString());
        xtw.writeEndElement();
    }

    @Override
    public void printEvents(XMLStreamWriter xtw, Iterator<Event> iterator) throws XMLStreamException {
        LOG.trace("Starting Events");

        if(iterator.hasNext()){
            xtw.writeStartElement(EVENTS_TAG);
            while(iterator.hasNext()) {
                Event event = iterator.next();
                LOG.trace("Printing event element");
                xtw.writeStartElement(EVENT_TAG);
                xtw.writeAttribute(ID_ATTR, event.getName());
                FExpression fexpr = this.bfm.getFExpression(event); // getFExpression(event);
                if(fexpr != null && !fexpr.equals(FExpression.trueValue())){
                    xtw.writeAttribute(FEXPRESSION_ATTR, fexpr.applySimplification().toCnf().toString());
                }
                xtw.writeCharacters(" ");
                xtw.writeEndElement();
            }
            xtw.writeEndElement();
        }
        LOG.trace("End Events");
    }

    @Override
    public void printElement(XMLStreamWriter xtw, Group<BehavioralFeature> group) throws XMLStreamException {
        LOG.trace("Printing group element");
        switch (group.GROUPTYPE){
            case OR -> xtw.writeStartElement(OR_TAG);
            case ALTERNATIVE -> xtw.writeStartElement(ALTERNATIVE_TAG);
            case MANDATORY -> xtw.writeStartElement(MANDATORY_TAG);
            case OPTIONAL -> xtw.writeStartElement(OPTIONAL_TAG);
        }

        for(BehavioralFeature f: group.getFeatures()){
            printElement(xtw, f);
        }
        xtw.writeEndElement();
    }

    @Override
    public void printCausalities(XMLStreamWriter xtw, BehavioralFeature bf) throws XMLStreamException {
        LOG.trace("Starting Causalities");
        xtw.writeStartElement(CAUSALITIES_TAG);

        for(CausalityRelation causality: bf.getCausalities()){
            LOG.trace("Printing causality element");
            xtw.writeStartElement(CAUSALITY_TAG);
            xtw.writeAttribute(TARGET_ATTR, causality.getTarget().getName());
            printBundle(xtw, causality.getBundle());
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
        LOG.trace("End Causalities");
    }

    private void printBundle(XMLStreamWriter xtw, Set<Event> bundle) throws XMLStreamException {
        xtw.writeStartElement(BUNDLE_TAG);
        for (Event ev : bundle) {
            xtw.writeStartElement(EVENT_TAG);
            xtw.writeAttribute(ID_ATTR, ev.getName());
            xtw.writeCharacters(" ");
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
    }

    @Override
    public void printConflicts(XMLStreamWriter xtw, ConflictSet conflicts) throws XMLStreamException {
        LOG.trace("Starting Conflicts");
        xtw.writeStartElement(CONFLICTS_TAG);
        Set<ConflictSet.Biclique> cliques = conflicts.findMinimalBicliqueEdgeCover();

        for (ConflictSet.Biclique c: cliques){
            LOG.trace("Printing conflict element");
            xtw.writeStartElement(CONFLICT_TAG);

            // Printing Set A
            xtw.writeStartElement(EVENTS_TAG);
            for(Event e: c.getA()){
                xtw.writeStartElement(EVENT_TAG);
                xtw.writeAttribute(ID_ATTR, e.getName());
                xtw.writeCharacters(" ");
                xtw.writeEndElement();
            }
            xtw.writeEndElement();

            // Printing Set B
            xtw.writeStartElement(EVENTS_TAG);
            for(Event e: c.getB()){
                xtw.writeStartElement(EVENT_TAG);
                xtw.writeAttribute(ID_ATTR, e.getName());
                xtw.writeCharacters(" ");
                xtw.writeEndElement();
            }
            xtw.writeEndElement();

            xtw.writeEndElement();
        }

        xtw.writeEndElement();
        LOG.trace("End Conflicts");
    }
}
