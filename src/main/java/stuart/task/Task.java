package stuart.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Task {
    /** Formatter used to display deadline/event dates, e.g. "Oct 15 2019". */
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");

    protected String description;
    protected boolean isDone;
    protected TaskType type;

    public Task(String description, TaskType type) {
        this.description = description;
        this.isDone = false;
        this.type = type;
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        this.isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns "X" if this task is done, or " " otherwise.
     *
     * @return the status icon
     */
    public String getStatusIcon() {
        return (this.isDone? "X" : " "); // mark done task with X
    }


    /**
     * Returns this task formatted as "[T][X] description" or "[T][ ] description",
     * with the leading tag depending on {@link #type}.
     *
     * @return the formatted task line
     */
    @Override
    public String toString() {
        return "[" + type.getTag() + "][" + this.getStatusIcon() + "] " + this.description;
    }

    /**
     * Returns this task formatted for the save file, e.g. {@code "T | 1 | read book"}.
     *
     * @return the save-file line for this task, without a trailing newline
     */
    public String toSaveFormat() {
        return type.getTag() + " | " + (isDone ? "1" : "0") + " | " + description;
    }

    /**
     * Checks whether this task occurs on the given date. A plain to-do never
     * occurs on any date, since it has none.
     *
     * @param date the date to check
     * @return true if this task occurs on {@code date}
     */
    public boolean occursOn(LocalDate date) {
        return false;
    }

    /**
     * Returns the date this task should be sorted by, if it has one.
     * A plain to-do has no date, so it sorts after every dated task.
     *
     * @return the sort date, or {@link Optional#empty()} if this task has none
     */
    public Optional<LocalDate> getSortDate() {
        return Optional.empty();
    }

    /**
     * Checks whether this task is overdue: its date has passed and it is
     * not yet done. A plain to-do is never overdue, since it has no date,
     * and a done task is never overdue regardless of its date.
     *
     * @return true if this task is overdue
     */
    public boolean isOverdue() {
        return false;
    }
}
