package uk.kcl.info.bfm;

import java.util.Objects;

public class CausalityRelation {
    private final Event source;
    private final Event target;

    CausalityRelation(Event source, Event target) {
        this.source = source;
        this.target = target;
    }

    public Event getSource() {
        return this.source;
    }

    public Event getTarget() {
        return this.target;
    }

    public String toString() {
        return "Transition{source=" + this.source + ", target=" + this.target + '}';
    }

    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.source);
        hash = 23 * hash + Objects.hashCode(this.target);
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
            CausalityRelation other = (CausalityRelation)obj;
            return Objects.equals(this.source, other.source) && Objects.equals(this.target, other.target);
        }
    }
}

