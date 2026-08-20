public class Task {
    protected String description;
    protected boolean isDone;

    public Task(String description) {
        this.description = description;
        this.isDone = false;
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
     * Returns this task formatted as "[X] description" or "[ ] description".
     *
     * @return the formatted task line
     */
    @Override
    public String toString() {
        // TODO
        return null;
    }
}
