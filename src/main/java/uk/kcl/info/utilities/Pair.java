package uk.kcl.info.utilities;

public class Pair<T, U> {
    private final T first;
    private final U second;

    // Constructor to initialize the pair
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    // Getter for the first element
    public T getFirst() {
        return first;
    }

    // Getter for the second element
    public U getSecond() {
        return second;
    }

    // Override equals method to compare pairs
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) obj;
        return first.equals(pair.first) && second.equals(pair.second);
    }

    // Override hashCode method to hash pairs
    @Override
    public int hashCode() {
        return 31 * first.hashCode() + second.hashCode();
    }

    // Override toString method for better printing
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
