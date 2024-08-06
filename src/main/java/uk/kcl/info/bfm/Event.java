package uk.kcl.info.bfm;

import be.vibes.fexpression.FExpression;
import com.google.common.base.Preconditions;

import java.util.Objects;

public class Event {
    public static final String EPSILON_ACTION = "epsilon";
    static final Event EPSILON = new Event("epsilon");
    private final FExpression fexpr;
    private final String name;

    public Event(String name, FExpression fexpr) {
        Preconditions.checkNotNull(name, "Name may not be null!");
        this.name = name;
        this.fexpr = fexpr;
    }

    public Event(String name) {
        Preconditions.checkNotNull(name, "Name may not be null!");
        this.name = name;
        this.fexpr = FExpression.trueValue();
    }

    public String getName() {
        return this.name;
    }

    public FExpression getFexpr() {
        return fexpr;
    }

    public String toString() {
        return "Event{name=" + this.name + ", fexpr=" + this.fexpr + '}';
    }

    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.fexpr);
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
            Event other = (Event)obj;
            return Objects.equals(this.name, other.name) && Objects.equals(this.fexpr, other.fexpr);
        }
    }
}
