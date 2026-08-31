package stuart.task;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventsTest {

    @Test
    public void toString_includesFormattedFromAndToDates() {
        Events task = new Events("meeting", LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 16));
        assertEquals("[E][ ] meeting (from: Oct 15 2019 to: Oct 16 2019)", task.toString());
    }

    @Test
    public void toSaveFormat_includesRawFromAndToDates() {
        Events task = new Events("meeting", LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 16));
        assertEquals("E | 0 | meeting | 2019-10-15 | 2019-10-16", task.toSaveFormat());
    }

    @Test
    public void occursOn_dateBetweenFromAndTo_returnsTrue() {
        Events task = new Events("meeting", LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 17));
        assertTrue(task.occursOn(LocalDate.of(2019, 10, 16)));
    }

    @Test
    public void occursOn_dateEqualsFrom_returnsTrue() {
        Events task = new Events("meeting", LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 16));
        assertTrue(task.occursOn(LocalDate.of(2019, 10, 15)));
    }

    @Test
    public void occursOn_dateEqualsTo_returnsTrue() {
        Events task = new Events("meeting", LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 16));
        assertTrue(task.occursOn(LocalDate.of(2019, 10, 16)));
    }

    @Test
    public void occursOn_dateBeforeFrom_returnsFalse() {
        Events task = new Events("meeting", LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 16));
        assertFalse(task.occursOn(LocalDate.of(2019, 10, 14)));
    }

    @Test
    public void occursOn_dateAfterTo_returnsFalse() {
        Events task = new Events("meeting", LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 16));
        assertFalse(task.occursOn(LocalDate.of(2019, 10, 17)));
    }

    @Test
    public void getSortDate_returnsFromDate() {
        LocalDate from = LocalDate.of(2019, 10, 15);
        Events task = new Events("meeting", from, LocalDate.of(2019, 10, 16));
        assertEquals(Optional.of(from), task.getSortDate());
    }

    @Test
    public void isOverdue_pastToNotDone_returnsTrue() {
        Events task = new Events("meeting", LocalDate.of(1999, 12, 31), LocalDate.of(2000, 1, 1));
        assertTrue(task.isOverdue());
    }

    @Test
    public void isOverdue_pastToButDone_returnsFalse() {
        Events task = new Events("meeting", LocalDate.of(1999, 12, 31), LocalDate.of(2000, 1, 1));
        task.markAsDone();
        assertFalse(task.isOverdue());
    }

    @Test
    public void isOverdue_futureTo_returnsFalse() {
        Events task = new Events("meeting", LocalDate.of(2998, 12, 31), LocalDate.of(2999, 1, 1));
        assertFalse(task.isOverdue());
    }
}
