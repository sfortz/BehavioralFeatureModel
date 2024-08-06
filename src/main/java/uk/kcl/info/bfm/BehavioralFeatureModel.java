package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import be.vibes.solver.FeatureModel;

import java.util.Iterator;

public interface BehavioralFeatureModel extends FeatureModel {

    Event getEvent(String var1);

    Event getInitialEvent();

    Iterator<Event> events();
    Iterator<CausalityRelation> causalities();
    Iterator<ConflictRelation> conflicts();

    CausalityRelation getCausality(Event var1, Event var2);
    ConflictRelation getConflict(Event var1, Event var2);
    boolean hasCausality(Event var1, Event var2);
    boolean hasConflict(Event var1, Event var2);
    Iterator<CausalityRelation> getCausalities(Event var1);

    Iterator<ConflictRelation> getConflicts(Event var1);

    Iterator<Event> getOutgoing(Event var1);

    Iterator<Event> getIncoming(Event var1);

    int getOutgoingCount(Event var1);

    int getIncomingCount(Event var1);

    int getCausalitiesCount();
    int getConflictsCount();

    int getEventsCount();

    FExpression getFExpression(Event event);

}
