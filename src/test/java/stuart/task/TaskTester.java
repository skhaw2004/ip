package stuart.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTester {

    @Test
    public void getStatusIcon_newTask_returnsSpace() {
        ToDos task = new ToDos("eat malatang");
        assertEquals(" ", task.getStatusIcon());

    }

    @Test
    public void getStatusIcon_afterMarkAsDone_returnsX() {
        ToDos task = new ToDos("eat malatang");
        task.markAsDone();
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    public void getStatusIcon_markAsDoneThenMarkAsNotDone_returnsSpace() {
        ToDos task = new ToDos("eat malatang");
        task.markAsDone();
        task.markAsNotDone();
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    public void toString_notDone_returnsFormattedLineWithSpaceIcon() {
        ToDos task = new ToDos("eat malatang");
        assertEquals("[T][ ] eat malatang", task.toString());
    }

    @Test
    public void toString_done_returnsFormattedLineWithXIcon() {
        ToDos task = new ToDos("eat malatang");
        task.markAsDone();
        assertEquals("[T][X] eat malatang", task.toString());
    }

    @Test
    public void toSaveFormat_notDone_returnsPipeDelimitedLineWithZero() {
        ToDos task = new ToDos("eat malatang");
        assertEquals("T | 0 | eat malatang", task.toSaveFormat());
    }

    @Test
    public void toSaveFormat_done_returnsPipeDelimitedLineWithOne() {
        ToDos task = new ToDos("eat malatang");
        task.markAsDone();
        assertEquals("T | 1 | eat malatang", task.toSaveFormat());
    }
}
