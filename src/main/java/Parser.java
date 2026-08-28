import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Makes sense of the user's raw command text: parsing task numbers, dates,
 * and command-specific fields (like a deadline's {@code /by} date) out of
 * it. Reports malformed input as a {@link StuartException}; does not decide
 * what to do about it.
 */
public class Parser {
    /**
     * The description and due date parsed from the text after a
     * {@code deadline} command.
     */
    public record DeadlineFields(String description, String by) {
    }

    /**
     * The description, start, and end times parsed from the text after an
     * {@code event} command.
     */
    public record EventFields(String description, String from, String to) {
    }

    /**
     * Parses the task number following a {@code mark}/{@code unmark}/
     * {@code delete} command into a 0-based index.
     *
     * @param indexText the text after the command word, expected to be a 1-based number
     * @return the 0-based index, or -1 if {@code indexText} is not a valid number
     */
    public static int parseIndex(String indexText) {
        try {
            return Integer.parseInt(indexText.trim()) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parses a date in {@code yyyy-MM-dd} format, e.g. {@code "2019-10-15"}.
     *
     * @param text the date text to parse
     * @return the parsed date
     * @throws StuartException if {@code text} is not a valid {@code yyyy-MM-dd} date
     */
    public static LocalDate parseDate(String text) throws StuartException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new StuartException(
                    "\"" + text + "\" is not a valid date. Please use yyyy-MM-dd, e.g. 2019-10-15.");
        }
    }

    /**
     * Rejects task text that contains the save-file field delimiter, since
     * it would be misread as extra fields the next time the save file is
     * loaded (e.g. a description of "milk | eggs" would split into two
     * fields instead of one).
     *
     * @param text the description or date/time text to check
     * @throws StuartException if {@code text} contains {@code " | "}
     */
    public static void checkNoSaveDelimiter(String text) throws StuartException {
        if (text.contains(" | ")) {
            throw new StuartException("Task details cannot contain \" | \".");
        }
    }

    /**
     * Splits the text after {@code deadline}/{@code deadline } into its
     * description and {@code /by} fields. Does not validate that either
     * field is non-empty; the caller decides what to do about that.
     *
     * @param rest the text after the {@code deadline} keyword
     * @return the parsed description and {@code by} fields
     * @throws StuartException if {@code rest} has no {@code /by} marker at all
     */
    public static DeadlineFields parseDeadlineFields(String rest) throws StuartException {
        int byIndex = rest.indexOf("/by");
        if (byIndex == -1) {
            throw new StuartException("A deadline needs a description and \"/by <yyyy-MM-dd>\", \n"
                    + Ui.TEXT_INDENT + "e.g. deadline return book /by 2019-10-15");
        }
        String description = rest.substring(0, byIndex).trim();
        String by = rest.substring(byIndex + "/by".length()).trim();
        return new DeadlineFields(description, by);
    }

    /**
     * Splits the text after {@code event}/{@code event } into its
     * description, {@code /from}, and {@code /to} fields. Does not validate
     * that any field is non-empty; the caller decides what to do about that.
     *
     * @param rest the text after the {@code event} keyword
     * @return the parsed description, {@code from}, and {@code to} fields
     * @throws StuartException if {@code rest} is missing {@code /from} or
     *         {@code /to}, or has them in the wrong order
     */
    public static EventFields parseEventFields(String rest) throws StuartException {
        int fromIndex = rest.indexOf("/from");
        int toIndex = rest.indexOf("/to");
        if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {
            throw new StuartException(
                    "An event needs a description, \"/from <yyyy-MM-dd>\", and \"/to <yyyy-MM-dd>\", \n"
                    + Ui.TEXT_INDENT + "e.g. event meeting /from 2019-10-15 /to 2019-10-16");
        }
        String description = rest.substring(0, fromIndex).trim();
        String from = rest.substring(fromIndex + "/from".length(), toIndex).trim();
        String to = rest.substring(toIndex + "/to".length()).trim();
        return new EventFields(description, from, to);
    }
}
