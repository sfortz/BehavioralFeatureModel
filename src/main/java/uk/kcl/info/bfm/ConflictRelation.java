package uk.kcl.info.bfm;

import java.util.Objects;

public class ConflictRelation {
    private final Event event1;
    private final Event event2;

    ConflictRelation(Event event1, Event event2) {
        this.event1 = event1;
        this.event2 = event2;
    }

    public Event getEvent1() {
        return this.event1;
    }

    public Event getEvent2() {
        return this.event2;
    }

    public String toString() {
        return "Conflict{" + this.event1 + ", " + this.event2 + '}';
    }

    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.event1);
        hash = 23 * hash + Objects.hashCode(this.event2);
        return hash;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            ConflictRelation other = (ConflictRelation)obj;
            return (Objects.equals(this.event1, other.event1) && Objects.equals(this.event2, other.event2))
                    || (Objects.equals(this.event1, other.event2) && Objects.equals(this.event2, other.event1));
        }
    }
}

