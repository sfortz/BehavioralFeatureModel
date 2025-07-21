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

package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.ParserUtil;
import be.vibes.fexpression.exception.ParserException;
import be.vibes.solver.*;
import be.vibes.solver.XMLModelFactory;
import com.google.common.base.Preconditions;
import uk.kcl.info.bfm.exceptions.BehavioralFeatureModelDefinitionException;
import java.util.*;

public class BehavioralFeatureModelFactory extends XMLModelFactory<BehavioralFeature, FeatureModel<BehavioralFeature>> {

    private final Map<Event, BehavioralFeature> featureMap = new HashMap<>();
    private final Map<Event, String> eventFexprMap = new HashMap<>();

    public BehavioralFeatureModelFactory() {
        super(BehavioralFeatureModel::new);
    }

    public BehavioralFeatureModelFactory(SolverType type) {
        super(BehavioralFeatureModel::new, type);
    }

    public BehavioralFeatureModelFactory(FeatureModel<?> fm) {
        super(() -> new BehavioralFeatureModel(fm), fm.getSolver().getType());
    }

    public BehavioralFeature setRootFeature(String name){
        BehavioralFeature feature = new BehavioralFeature(name);
        return setRootFeature(feature, name);
    }

    public BehavioralFeature addFeature(Group<BehavioralFeature> group, String name){
        BehavioralFeature feature = new BehavioralFeature(name);
        return addFeature(feature, group, name);
    }

    public void addEvent(BehavioralFeature feat, String event) {
        addEvent(feat.getFeatureName(), event, FExpression.trueValue());
    }

    public void addEvent(BehavioralFeature feat, String event, FExpression fexpr) {
        addEvent(feat.getFeatureName(), event, fexpr);
    }

    public void addEvent(BehavioralFeature feat, String event, String fexprStr) {
        addEvent(feat.getFeatureName(), event, fexprStr);
    }

    public void addEvent(String featName, String event) {
        addEvent(featName, event, FExpression.trueValue());
    }

    public void addEvent(String featName, String event, String fexprStr) {

        BehavioralFeature feature = getFeature(featName);
        if (feature == null) {
            throw new BehavioralFeatureModelDefinitionException("Events should always be associated to one feature of the BFM.");
        }

        Event ev = feature.addEvent(event, FExpression.trueValue());
        featureMap.put(ev, feature);
        eventFexprMap.put(ev, fexprStr);
    }

    public void addEvent(String featName, String event, FExpression fexpr) {
        BehavioralFeature feature = getFeature(featName);
        if (feature == null) {
            throw new BehavioralFeatureModelDefinitionException("Events should always be associated to one feature of the BFM.");
        }

        Event ev = feature.addEvent(event, fexpr);
        featureMap.put(ev, feature);
    }

    public void updateAllEventFexpr() {
        for (Map.Entry<Event, String> entry : eventFexprMap.entrySet()) {
            Event ev = entry.getKey();
            String fexprStr = entry.getValue();

            if (fexprStr != null) {
                FExpression fexpr;
                try {
                    fexpr = ParserUtil.getInstance().parse(fexprStr);
                } catch (ParserException e) {
                    throw new BehavioralFeatureModelDefinitionException("Exception while parsing fexpression " + fexprStr, e);
                }

                BehavioralFeature feature = featureMap.get(ev);
                if (feature == null) {
                    throw new BehavioralFeatureModelDefinitionException("No feature found for event: " + ev.getName());
                }

                feature.updateEventFexpr(ev.getName(), fexpr);
            }
        }

        eventFexprMap.clear();
    }

    public void addCausality(String featName, Set<String> bundle, String target) {

        Event trg = new Event(target);
        Set<Event> bndl = new HashSet<>();
        for(String name: bundle) {
            Event event = new Event(name);
            bndl.add(event);
        }

        this.addCausality(featName,bndl,trg);
    }

    public void addCausality(String featName, Set<Event> bundle, Event target) {

        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            feature.addCausality(bundle, target);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(String featName, CausalityRelation causalityRelation) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            feature.addCausality(causalityRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(BehavioralFeature feat, Set<String> bundle, String target) {

        Event trg = new Event(target);
        Set<Event> bndl = new HashSet<>();
        for(String name: bundle) {
            Event event = new Event(name);
            bndl.add(event);
        }

        this.addCausality(feat,bndl,trg);
    }

    public void addCausality(BehavioralFeature feat, Set<Event> bundle, Event target) {

        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.addCausality(bundle, target);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addCausality(BehavioralFeature feat, CausalityRelation causalityRelation) {
        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.addCausality(causalityRelation);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Causalities should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(String featName, String event1, String event2) {
        this.addConflict(featName, new Event(event1), new Event(event2));
    }

    public void addConflict(BehavioralFeature feat, String event1, String event2) {
        this.addConflict(feat, new Event(event1), new Event(event2));
    }

    public void addConflict(String featName, Event event1, Event event2) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            this.addConflict(feature, event1, event2);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflict(BehavioralFeature feat, Event event1, Event event2) {

        Set<Event> ev = feat.getAllRecursiveEvents();
        Preconditions.checkArgument(ev.contains(event1), event1 + " does not belong to this behavioral feature model or any of its subtree!");
        Preconditions.checkArgument(ev.contains(event2), event2 + " does not belong to this behavioral feature model or any of its subtree!");

        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.getConflictSet().addConflict(event1, event2);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflicts(String featName, Event event1, Collection<?> group) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            this.addConflicts(feature, event1, group);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflicts(String featName, Collection<?> group1, Collection<?> group2) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            this.addConflicts(feature, group1, group2);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflicts(String featName, ConflictSet set) {
        BehavioralFeature feature = this.getFeature(featName);
        if(feature != null){
            this.addConflicts(feature, set);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflicts(BehavioralFeature feat, Event event1, Collection<?> group) {
        Set<Event> allEvents = feat.getAllRecursiveEvents();
        Preconditions.checkArgument(allEvents.contains(event1), event1 + " does not belong to this BFM!");
        Set<Event> events = toEventSet(group, allEvents);

        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.getConflictSet().addConflicts(event1, events);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflicts(BehavioralFeature feat, Collection<?> group1, Collection<?> group2) {
        Set<Event> allEvents = feat.getAllRecursiveEvents();
        Set<Event> events1 = toEventSet(group1, allEvents);
        Set<Event> events2 = toEventSet(group2, allEvents);

        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.getConflictSet().addConflicts(events1, events2);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    public void addConflicts(BehavioralFeature feat, ConflictSet set) {
        Set<Event> allEvents = feat.getAllRecursiveEvents();
        Preconditions.checkArgument(allEvents.containsAll(set.getAllEvents()), "All events of a conflict should belong to the BFM!");

        BehavioralFeature feature = this.getFeature(feat.getFeatureName());
        if(feature != null){
            feat.getConflictSet().addConflicts(set);
        } else {
            throw new BehavioralFeatureModelDefinitionException("Conflicts should always be associated to one feature of the BFM.");
        }
    }

    private static Set<Event> toEventSet(Collection<?> group, Set<Event> allEvents) {
        Set<Event> events = new HashSet<>();
        for (Object o : group) {
            Event e;
            if (o instanceof Event) {
                e = (Event) o;
            } else if (o instanceof String) {
                e = new Event((String) o);
            } else {
                throw new IllegalArgumentException(
                        "Conflict collections must contain only Event or String elements.");
            }
            Preconditions.checkArgument(allEvents.contains(e),
                    "All events of a conflict should belong to the BFM!");
            events.add(e);
        }
        return events;
    }

    public Map<Event, BehavioralFeature> getFeatureMap() {
        return featureMap;
    }

    @Override
    public BehavioralFeatureModel build() {
        Preconditions.checkArgument(eventFexprMap.isEmpty(), "Some FExpressions are not yet associated with their Event. Please call updateAllEventFexpr().");
        BehavioralFeatureModel bfm = (BehavioralFeatureModel) super.build();
        bfm.setCausalityTable();
        return bfm;
    }

}