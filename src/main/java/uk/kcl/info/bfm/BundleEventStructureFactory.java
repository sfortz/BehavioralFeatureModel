package uk.kcl.info.bfm;

import java.util.HashSet;
import java.util.Set;

public class BundleEventStructureFactory {
    protected final DefaultBundleEventStructure bes;

    protected BundleEventStructureFactory(DefaultBundleEventStructure bes) {
        this.bes = bes;
    }

    public BundleEventStructureFactory() {
        this(new DefaultBundleEventStructure());
    }

    public void addEvent(String name) {
        this.bes.addEvent(name);
    }

    public void addCausality(Set<String> bundle, String target) {

        Event trg = new Event(target);
        Set<Event> bndl = new HashSet<>();
        for(String name: bundle) {
            Event event = new Event(name);
            bndl.add(event);
        }

        this.bes.addCausality(bndl, trg);
    }

    public void addCausality(Set<Event> bundle, Event target) {
        this.bes.addCausality(bundle, target);
    }

    public void addConflict(String event1, String event2) {
        this.addConflict(new Event(event1), new Event(event2));
    }

    public void addConflict(Event event1, Event event2) {
        this.bes.addConflict(event1, event2);
    }

    public BundleEventStructure build() {
        return this.bes;
    }

    public void validate() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
