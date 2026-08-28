import java.time.LocalDate;

/**
 * Represents a task that needs to be done before a specific date/time.
 */
// // Deadlines class built by Claude Code, verified by myself
public class Deadlines extends Task {
    private final LocalDate by;

    public Deadlines(String description, LocalDate by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + by.format(DISPLAY_DATE_FORMAT) + ")";
    }

    @Override
    public String toSaveFormat() {
        return super.toSaveFormat() + " | " + by;
    }

    @Override
    public boolean occursOn(LocalDate date) {
        return by.equals(date);
    }
}
