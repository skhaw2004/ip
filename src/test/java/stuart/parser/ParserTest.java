package stuart.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import stuart.exception.StuartException;

public class ParserTest {

    @Test
    public void parseCommand_byeKeywrod_returnsByeType() {
        Parser.ParsedCommand result = Parser.parseCommand("bye");
        assertEquals(Parser.CommandType.BYE, result.type());
    }

    @Test
    public void parseCommand_unrecognizedKeyword_returnsUnknownType() {
        Parser.ParsedCommand result = Parser.parseCommand("zzz");
        assertEquals(Parser.CommandType.UNKNOWN, result.type());
    }

    @Test
    public void parseIndex_nonNumericText_returnsNegativeOne() {
        int result = Parser.parseIndex("a");
        assertEquals(-1, result);
    }

    // subsequent tests are AI-prompted

    @Test
    public void parseCommand_listKeyword_returnsListType() {
        Parser.ParsedCommand result = Parser.parseCommand("list");
        assertEquals(Parser.CommandType.LIST, result.type());
    }

    @Test
    public void parseCommand_sortedKeyword_returnsSortedType() {
        Parser.ParsedCommand result = Parser.parseCommand("sorted");
        assertEquals(Parser.CommandType.SORTED, result.type());
    }

    @Test
    public void parseCommand_onWithDate_returnsOnTypeWithDateArgument() {
        Parser.ParsedCommand result = Parser.parseCommand("on 2019-10-15");
        assertEquals(Parser.CommandType.ON, result.type());
        assertEquals("2019-10-15", result.arguments());
    }

    @Test
    public void parseCommand_onWithNoDate_returnsEmptyArguments() {
        Parser.ParsedCommand result = Parser.parseCommand("on");
        assertEquals(Parser.CommandType.ON, result.type());
        assertEquals("", result.arguments());
    }

    @Test
    public void parseCommand_wordStartingWithOn_notMatchedAsOnCommand() {
        Parser.ParsedCommand result = Parser.parseCommand("ontology");
        assertEquals(Parser.CommandType.UNKNOWN, result.type());
        assertEquals("ontology", result.arguments());
    }

    @Test
    public void parseCommand_markCommand_preservesRawArgumentsWithoutTrimming() {
        // Unlike todo/deadline/event, mark/unmark/delete don't trim their
        // arguments - only the single space right after the keyword is cut.
        Parser.ParsedCommand result = Parser.parseCommand("mark  2");
        assertEquals(Parser.CommandType.MARK, result.type());
        assertEquals(" 2", result.arguments());
    }

    @Test
    public void parseCommand_deleteCommand_returnsDeleteTypeWithIndexArgument() {
        Parser.ParsedCommand result = Parser.parseCommand("delete 3");
        assertEquals(Parser.CommandType.DELETE, result.type());
        assertEquals("3", result.arguments());
    }

    @Test
    public void parseCommand_todoWithDescription_trimsArguments() {
        Parser.ParsedCommand result = Parser.parseCommand("todo read book");
        assertEquals(Parser.CommandType.TODO, result.type());
        assertEquals("read book", result.arguments());
    }

    @Test
    public void parseCommand_todoWithNoDescription_returnsEmptyArguments() {
        Parser.ParsedCommand result = Parser.parseCommand("todo");
        assertEquals(Parser.CommandType.TODO, result.type());
        assertEquals("", result.arguments());
    }

    @Test
    public void parseCommand_deadlineCommand_returnsDeadlineTypeWithRawFieldsArgument() {
        Parser.ParsedCommand result = Parser.parseCommand("deadline return book /by 2019-10-15");
        assertEquals(Parser.CommandType.DEADLINE, result.type());
        assertEquals("return book /by 2019-10-15", result.arguments());
    }

    @Test
    public void parseCommand_eventCommand_returnsEventTypeWithRawFieldsArgument() {
        Parser.ParsedCommand result = Parser.parseCommand("event meeting /from 2019-10-15 /to 2019-10-16");
        assertEquals(Parser.CommandType.EVENT, result.type());
        assertEquals("meeting /from 2019-10-15 /to 2019-10-16", result.arguments());
    }

    @Test
    public void parseIndex_validNumberWithWhitespace_returnsZeroBasedIndex() {
        int result = Parser.parseIndex("  3  ");
        assertEquals(2, result);
    }

    @Test
    public void parseIndex_emptyText_returnsNegativeOne() {
        int result = Parser.parseIndex("");
        assertEquals(-1, result);
    }

    @Test
    public void parseDate_validDate_returnsParsedDate() throws StuartException {
        LocalDate result = Parser.parseDate("2019-10-15");
        assertEquals(LocalDate.of(2019, 10, 15), result);
    }

    @Test
    public void parseDate_invalidFormat_throwsStuartException() {
        assertThrows(StuartException.class, () -> Parser.parseDate("15-10-2019"));
    }

    @Test
    public void checkNoSaveDelimiter_textWithoutDelimiter_doesNotThrow() {
        assertDoesNotThrow(() -> Parser.checkNoSaveDelimiter("read book"));
    }

    @Test
    public void checkNoSaveDelimiter_textContainingDelimiter_throwsStuartException() {
        assertThrows(StuartException.class, () -> Parser.checkNoSaveDelimiter("milk | eggs"));
    }

    @Test
    public void parseDeadlineFields_validInput_splitsDescriptionAndBy() throws StuartException {
        Parser.DeadlineFields result = Parser.parseDeadlineFields("return book /by 2019-10-15");
        assertEquals("return book", result.description());
        assertEquals("2019-10-15", result.by());
    }

    @Test
    public void parseDeadlineFields_missingByMarker_throwsStuartException() {
        assertThrows(StuartException.class, () -> Parser.parseDeadlineFields("return book"));
    }

    @Test
    public void parseEventFields_validInput_splitsDescriptionFromAndTo() throws StuartException {
        Parser.EventFields result = Parser.parseEventFields("meeting /from 2019-10-15 /to 2019-10-16");
        assertEquals("meeting", result.description());
        assertEquals("2019-10-15", result.from());
        assertEquals("2019-10-16", result.to());
    }

    @Test
    public void parseEventFields_missingFromMarker_throwsStuartException() {
        assertThrows(StuartException.class, () -> Parser.parseEventFields("meeting /to 2019-10-16"));
    }

    @Test
    public void parseEventFields_toBeforeFrom_throwsStuartException() {
        assertThrows(StuartException.class, () ->
                Parser.parseEventFields("meeting /to 2019-10-16 /from 2019-10-15"));
    }
}
