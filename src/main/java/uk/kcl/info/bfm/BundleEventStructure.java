package uk.kcl.info.bfm;

import java.util.*;

public interface BundleEventStructure {
    Iterator<Event> events();

    List<Event> getAllEvents();

    Event getEvent(String var1);

    Iterator<CausalityRelation> causalities();

    Iterator<ConflictRelation> conflicts();

    Iterator<CausalityRelation> getAllCausalitiesOfEvent(Event event);

    CausalityRelation getCausality(Set<Event> bundle, Event event);

    ConflictRelation getConflict(Event var1, Event var2);

    Set<ConflictRelation> getAllConflictsOfEvent(Event var1);

    Iterator<CausalityRelation> getOutgoingCausalities(Event var1);

    int getOutgoingCausalityCount(Event var1);

    Iterator<CausalityRelation> getIncomingCausalities(Event var1);

    int getIncomingCausalityCount(Event var1);

    Set<Event> getInitialEvents();

    TreeMap<Integer, Set<Set<Event>>> getAllConfigurations();

    int getEventsCount();

    int getCausalitiesCount();

    int getConflictsCount();

    boolean isInConflict(Event var1, Event var2);

}
