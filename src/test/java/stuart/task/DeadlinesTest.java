package stuart.task;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeadlinesTest {

    @Test
    public void toString_includesFormattedByDate() {
        Deadlines task = new Deadlines("return book", LocalDate.of(2019, 10, 15));
        assertEquals("[D][ ] return book (by: Oct 15 2019)", task.toString());
    }

    @Test
    public void toSaveFormat_includesRawByDate() {
        Deadlines task = new Deadlines("return book", LocalDate.of(2019, 10, 15));
        assertEquals("D | 0 | return book | 2019-10-15", task.toSaveFormat());
    }

    @Test
    public void occursOn_dateEqualsBy_returnsTrue() {
        Deadlines task = new Deadlines("return book", LocalDate.of(2019, 10, 15));
        assertTrue(task.occursOn(LocalDate.of(2019, 10, 15)));
    }

    @Test
    public void occursOn_dateNotEqualsBy_returnsFalse() {
        Deadlines task = new Deadlines("return book", LocalDate.of(2019, 10, 15));
        assertFalse(task.occursOn(LocalDate.of(2019, 10, 16)));
    }

    @Test
    public void getSortDate_returnsByDate() {
        LocalDate by = LocalDate.of(2019, 10, 15);
        Deadlines task = new Deadlines("return book", by);
        assertEquals(Optional.of(by), task.getSortDate());
    }

    @Test
    public void isOverdue_pastByNotDone_returnsTrue() {
        Deadlines task = new Deadlines("return book", LocalDate.of(2000, 1, 1));
        assertTrue(task.isOverdue());
    }

    @Test
    public void isOverdue_pastByButDone_returnsFalse() {
        Deadlines task = new Deadlines("return book", LocalDate.of(2000, 1, 1));
        task.markAsDone();
        assertFalse(task.isOverdue());
    }

    @Test
    public void isOverdue_futureBy_returnsFalse() {
        Deadlines task = new Deadlines("return book", LocalDate.of(2999, 1, 1));
        assertFalse(task.isOverdue());
    }
}
