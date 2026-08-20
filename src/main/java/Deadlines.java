/**
 * Represents a task that needs to be done before a specific date/time.
 */
public class Deadlines extends Task {
    private final String by;

    public Deadlines(String description, String by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + by + ")";
    }
}
