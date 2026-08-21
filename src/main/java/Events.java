/**
 * Represents a task that starts at a specific date/time and ends at a
 * specific date/time.
 */
// Events class built by Claude Code, verified by myself
public class Events extends Task {
    private final String from;
    private final String to;

    public Events(String description, String from, String to) {
        super(description, TaskType.EVENT);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
