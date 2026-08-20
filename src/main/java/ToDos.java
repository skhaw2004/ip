/**
 * Represents a task with no date/time attached to it.
 */
public class ToDos extends Task {

    public ToDos(String description) {
        super(description, TaskType.TODO);
    }
}
