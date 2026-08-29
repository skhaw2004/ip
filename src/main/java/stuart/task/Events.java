package stuart.task;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Represents a task that starts at a specific date/time and ends at a
 * specific date/time.
 */
// Events class built by Claude Code, verified by myself
public class Events extends Task {
    private final LocalDate from;
    private final LocalDate to;

    public Events(String description, LocalDate from, LocalDate to) {
        super(description, TaskType.EVENT);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return super.toString() + " (from: " + from.format(DISPLAY_DATE_FORMAT)
                + " to: " + to.format(DISPLAY_DATE_FORMAT) + ")";
    }

    @Override
    public String toSaveFormat() {
        return super.toSaveFormat() + " | " + from + " | " + to;
    }

    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    @Override
    public Optional<LocalDate> getSortDate() {
        return Optional.of(from);
    }

    @Override
    public boolean isOverdue() {
        return !isDone && to.isBefore(LocalDate.now());
    }
}
