package stuart.task;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ToDosTest {

    @Test
    public void occursOn_anyDate_returnsFalse() {
        ToDos task = new ToDos("eat malatang");
        assertFalse(task.occursOn(LocalDate.of(2000, 1, 1)));
        assertFalse(task.occursOn(LocalDate.of(2999, 1, 1)));
    }

    @Test
    public void getSortDate_always_returnsEmpty() {
        ToDos task = new ToDos("eat malatang");
        assertEquals(Optional.empty(), task.getSortDate());
    }

    @Test
    public void isOverdue_always_returnsFalse() {
        ToDos task = new ToDos("eat malatang");
        assertFalse(task.isOverdue());
        task.markAsDone();
        assertFalse(task.isOverdue());
    }
}
