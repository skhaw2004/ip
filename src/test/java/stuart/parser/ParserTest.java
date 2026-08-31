package stuart.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*; //assertEquals, assertTrue, assertThrows, etc

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
}
