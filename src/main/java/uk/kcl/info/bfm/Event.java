package uk.kcl.info.bfm;

import com.google.common.base.Preconditions;
import java.util.Objects;

public class Event {
    public static final String EPSILON_ACTION = "epsilon";
    static final Event EPSILON = new Event("epsilon");
    private final String name;

    public Event(String name) {
        Preconditions.checkNotNull(name, "Name may not be null!");
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "Event{name=" + this.name + '}';
    }

    public int hashCode() {
        int hash = 7;
        return 37 * hash + Objects.hashCode(this.name);
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
            return Objects.equals(this.name, other.name);
        }
    }
}
