package uk.kcl.info.bfm;

import java.util.Iterator;
import java.util.Set;

public interface BundleEventStructure {
    Iterator<Event> events();

    Event getEvent(String var1);

    Iterator<CausalityRelation> causalities();

    CausalityRelation getCausality(Set<Event> bundle, Event event);

    ConflictRelation getConflict(Event var1, Event var2);

    Set<ConflictRelation> getConflicts(Event var1);

    Iterator<CausalityRelation> getOutgoingCausalities(Event var1);

    int getOutgoingCausalityCount(Event var1);

    Iterator<CausalityRelation> getIncomingCausalities(Event var1);

    int getIncomingCausalityCount(Event var1);

    Set<Event> getInitialEvents();

    int getEventsCount();

    int getCausalitiesCount();

}
