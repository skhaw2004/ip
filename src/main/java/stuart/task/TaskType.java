package stuart.task;

/**
 * The kind of a {@link Task}, and the single-letter tag used to display it,
 * e.g. {@code T} for a to-do.
 */
public enum TaskType {
    TODO("T"),
    DEADLINE("D"),
    EVENT("E");

    private final String tag;

    TaskType(String tag) {
        this.tag = tag;
    }

    /**
     * Returns the single-letter tag shown in a task's printed form,
     * e.g. {@code "T"} for {@link #TODO}.
     *
     * @return the tag
     */
    public String getTag() {
        return tag;
    }
}
