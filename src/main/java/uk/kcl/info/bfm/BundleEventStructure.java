package uk.kcl.info.bfm;

import java.util.*;

public interface BundleEventStructure {

    Iterator<Event> events();

    List<Event> getAllEvents();

    Event getEvent(String var1);

    Iterator<CausalityRelation> causalities();

    ConflictSet getConflictSetCopy();

    Iterator<CausalityRelation> getAllCausalitiesOfEvent(Event event);

    CausalityRelation getCausality(Set<Event> bundle, Event event);

    Iterator<CausalityRelation> getOutgoingCausalities(Event var1);

    int getOutgoingCausalityCount(Event var1);

    Iterator<CausalityRelation> getIncomingCausalities(Event var1);

    int getIncomingCausalityCount(Event var1);

    Set<Event> getAllConflictsOfEvent(Event event);

    Set<Event> getInitialEvents();

    TreeMap<Integer, Set<Set<Event>>> getAllConfigurations();

    int getEventsCount();

    int getCausalitiesCount();

    int getConflictsCount();

    int getMaxConflictSize();

    boolean areInConflict(Event var1, Event var2);

}
