package stuart.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*; //assertEquals, assertTrue, assertThrows, etc

public class ParserTest {

    @Test
    public void parseCommand_byeKeywrod_returnsByeType() {
        Parser.ParsedCommand result = Parser.parseCommand("bye");
        assertEquals(Parser.CommandType.BYE, result.type());
    }
}
