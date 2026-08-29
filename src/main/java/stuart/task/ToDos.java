package stuart.task;

/**
 * Represents a task with no date/time attached to it.
 */
// ToDos class built by Claude Code, verified by myself
public class ToDos extends Task {

    public ToDos(String description) {
        super(description, TaskType.TODO);
    }
}
