package stuart.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTester {

    @Test
    public void getStatusIcon_newTask_returnsSpace() {
        ToDos task = new ToDos("eat malatang");
        assertEquals(" ", task.getStatusIcon());

    }
}
