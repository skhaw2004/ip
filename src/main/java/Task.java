public class Task {
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
}
