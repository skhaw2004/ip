import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Entry point of the Stuart chatbot.
 * Stores whatever text the user enters, lists it back on request, and lets
 * the user mark/unmark items as done, until the user types {@code bye}.
 */
public class Stuart {
    /** Path to the file tasks are saved to, relative to the project root. */
    private static final String DATA_FILE_PATH = "./data/stuart.txt";

    private final Ui ui;
    private final Storage storage;

    public Stuart(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);
    }

    /**
     * Runs the chatbot: shows the banner and greeting, then repeatedly reads
     * and executes commands until the user types {@code bye} or input runs out.
     */
    public void run() {
        ui.showBanner();
        ui.reply("Hello! I'm Stuart.", "What can I do for you?");

        ArrayList<Task> items;
        try {
            items = storage.load(ui);
        } catch (StuartException e) {
            ui.reply("Warning: " + e.getMessage() + ".");
            items = new ArrayList<>();
        }

        // Keep reading commands until the user says "bye", or the input runs out.
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            String trimmedCommand = command.trim();
            try {
                if (trimmedCommand.equals("bye")) {
                    break;
                } else if (trimmedCommand.equals("list")) {
                    // output list of tasks
                    ui.reply(listItems(items, "Here are the tasks in your list:"));
                } else if (trimmedCommand.equals("sorted")) {
                    // output list of tasks sorted by date, dateless tasks last
                    ui.reply(listItems(sortedByDate(items), "Here are your tasks sorted by date:"));
                } else if (trimmedCommand.equals("on") || trimmedCommand.startsWith("on ")) {
                    // list tasks occurring on a specific date
                    String dateText = trimmedCommand.substring("on".length()).trim();
                    if (dateText.isEmpty()) {
                        throw new StuartException("Please specify a date, e.g. \"on 2019-10-15\".");
                    }
                    LocalDate date = parseDate(dateText);
                    ui.reply(tasksOn(items, date));
                } else if (trimmedCommand.startsWith("mark ")) {
                    // mark a task
                    int index = parseIndex(trimmedCommand.substring("mark ".length()));
                    if (isValidIndex(index, items.size())) {
                        items.get(index).markAsDone();
                        storage.save(items, ui);
                        ui.reply("Nice! I've marked this task as done:",
                                "  " + withOverdueFlag(items.get(index)));
                    } else {
                        throw new StuartException("That's not a valid task number.");
                    }
                } else if (trimmedCommand.startsWith("unmark ")) {
                    // unmark a task
                    int index = parseIndex(trimmedCommand.substring("unmark ".length()));
                    if (isValidIndex(index, items.size())) {
                        items.get(index).markAsNotDone();
                        storage.save(items, ui);
                        ui.reply("OK, I've marked this task as not done yet:",
                                "  " + withOverdueFlag(items.get(index)));
                    } else {
                        throw new StuartException("That's not a valid task number.");
                    }
                } else if (trimmedCommand.startsWith("delete ")) {
                    // delete a task
                    int index = parseIndex(trimmedCommand.substring("delete ".length()));
                    if (isValidIndex(index, items.size())) {
                        Task removedTask = items.remove(index);
                        storage.save(items, ui);
                        ui.reply("Noted. I've removed this task:",
                                "  " + withOverdueFlag(removedTask),
                                "Now you have " + items.size() + " tasks in the list.");
                    } else {
                        throw new StuartException("That's not a valid task number.");
                    }
                } else if (trimmedCommand.equals("todo") || trimmedCommand.startsWith("todo ")) {
                    // add a to-do
                    String description = trimmedCommand.substring("todo".length()).trim();
                    if (description.isEmpty()) {
                        // empty description
                        throw new StuartException("The description of a todo cannot be empty.");
                    }
                    checkNoSaveDelimiter(description);
                    addTask(items, new ToDos(description));
                } else if (trimmedCommand.equals("deadline") || trimmedCommand.startsWith("deadline ")) {
                    // add a deadline
                    String rest = trimmedCommand.substring("deadline".length()).trim();
                    int byIndex = rest.indexOf("/by");
                    if (byIndex == -1) {
                        throw new StuartException("A deadline needs a description and \"/by <yyyy-MM-dd>\", \n"
                                + Ui.TEXT_INDENT + "e.g. deadline return book /by 2019-10-15");
                    }
                    String description = rest.substring(0, byIndex).trim();
                    String by = rest.substring(byIndex + "/by".length()).trim();
                    if (description.isEmpty()) {
                        // empty description
                        throw new StuartException("The description of a deadline cannot be empty.");
                    }
                    if (by.isEmpty()) {
                        throw new StuartException("The \"/by\" date of a deadline cannot be empty.");
                    }
                    checkNoSaveDelimiter(description);
                    LocalDate byDate = parseDate(by);
                    addTask(items, new Deadlines(description, byDate));
                } else if (trimmedCommand.equals("event") || trimmedCommand.startsWith("event ")) {
                    // add an event
                    String rest = trimmedCommand.substring("event".length()).trim();
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
                    if (description.isEmpty()) {
                        // empty description
                        throw new StuartException("The description of an event cannot be empty.");
                    }
                    if (from.isEmpty() || to.isEmpty()) {
                        throw new StuartException("The \"/from\" and \"/to\" times of an event cannot be empty.");
                    }
                    checkNoSaveDelimiter(description);
                    LocalDate fromDate = parseDate(from);
                    LocalDate toDate = parseDate(to);
                    addTask(items, new Events(description, fromDate, toDate));
                } else {
                    // not any of the tasks
                    throw new StuartException("To add a task, use the following format:\n" + Ui.TEXT_INDENT + "<task type> <task description>");
                }
            } catch (StuartException e) {
                ui.reply(e.getMessage());
            }
        }

        ui.reply("Bye. Hope to see you again soon!");
        ui.close();
    }

    public static void main(String[] args) {
        new Stuart(DATA_FILE_PATH).run();
    }

    /**
     * Parses the task number following a {@code mark}/{@code unmark} command
     * into a 0-based array index.
     *
     * @param indexText the text after the command word, expected to be a 1-based number
     * @return the 0-based index, or -1 if {@code indexText} is not a valid number
     */
    private static int parseIndex(String indexText) {
        try {
            return Integer.parseInt(indexText.trim()) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Checks whether {@code index} refers to an actual stored item.
     *
     * @param index the 0-based index to check
     * @param itemCount the number of items currently stored
     * @return true if {@code index} is within range
     */
    private static boolean isValidIndex(int index, int itemCount) {
        return index >= 0 && index < itemCount;
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
    private static void checkNoSaveDelimiter(String text) throws StuartException {
        if (text.contains(" | ")) {
            throw new StuartException("Task details cannot contain \" | \".");
        }
    }

    /**
     * Parses a date in {@code yyyy-MM-dd} format, e.g. {@code "2019-10-15"}.
     *
     * @param text the date text to parse
     * @return the parsed date
     * @throws StuartException if {@code text} is not a valid {@code yyyy-MM-dd} date
     */
    private static LocalDate parseDate(String text) throws StuartException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new StuartException(
                    "\"" + text + "\" is not a valid date. Please use yyyy-MM-dd, e.g. 2019-10-15.");
        }
    }

    /**
     * Appends {@code task} to {@code items} and reports it.
     *
     * @param items the list of stored tasks
     * @param task the task to add
     */
    private void addTask(ArrayList<Task> items, Task task) {
        items.add(task);
        storage.save(items, ui);
        ui.reply("Got it. I've added this task:", "  " + withOverdueFlag(task),
                "Now you have " + items.size() + " tasks in the list.");
    }

    /**
     * Builds the numbered listing lines for {@code items}, with the given header.
     *
     * @param items the tasks to list, in the order they should be numbered
     * @param header the line to print above the numbered list
     * @return one header line followed by one line per item,
     *         formatted as "{@code index.[status] item}"
     */
    private static String[] listItems(ArrayList<Task> items, String header) {
        String[] lines = new String[items.size() + 1];
        lines[0] = header;
        for (int i = 0; i < items.size(); i++) {
            lines[i + 1] = formatNumberedTask(i + 1, items.get(i));
        }
        return lines;
    }

    /**
     * Formats one numbered line for a task, appending {@code [OVERDUE]} if
     * the task is overdue.
     *
     * @param number the 1-based number to display
     * @param task the task to format
     * @return the formatted line, e.g. {@code "1.[D][ ] return book (by: Oct 20 2019) [OVERDUE]"}
     */
    private static String formatNumberedTask(int number, Task task) {
        return number + "." + withOverdueFlag(task);
    }

    /**
     * Formats a task as its normal display text, with {@code [OVERDUE]}
     * appended if it is overdue.
     *
     * @param task the task to format
     * @return the formatted text, e.g. {@code "[D][ ] return book (by: Oct 20 2019) [OVERDUE]"}
     */
    private static String withOverdueFlag(Task task) {
        String text = task.toString();
        if (task.isOverdue()) {
            text += " [OVERDUE]";
        }
        return text;
    }

    /**
     * Returns a copy of {@code items} sorted by date ({@code Deadlines} by
     * {@code by}, {@code Events} by {@code from}), with dateless tasks
     * (i.e. {@code ToDos}) placed last. Does not modify {@code items} itself.
     *
     * @param items the list of stored tasks
     * @return a new, sorted list
     */
    private static ArrayList<Task> sortedByDate(ArrayList<Task> items) {
        ArrayList<Task> sorted = new ArrayList<>(items);
        sorted.sort(Stuart::compareByDate);
        return sorted;
    }

    /**
     * Compares two tasks by their sort date, treating a missing date as
     * later than any present date.
     *
     * @param a the first task
     * @param b the second task
     * @return a negative number if {@code a} sorts before {@code b}, zero if
     *         equal, or a positive number if {@code a} sorts after {@code b}
     */
    private static int compareByDate(Task a, Task b) {
        Optional<LocalDate> dateA = a.getSortDate();
        Optional<LocalDate> dateB = b.getSortDate();
        if (dateA.isEmpty() && dateB.isEmpty()) {
            return 0;
        } else if (dateA.isEmpty()) {
            return 1;
        } else if (dateB.isEmpty()) {
            return -1;
        }
        return dateA.get().compareTo(dateB.get());
    }

    /**
     * Builds the numbered listing lines for the tasks occurring on
     * {@code date}, with a header. Item numbers match their position in the
     * full task list, so they can be used directly with {@code mark}/
     * {@code unmark}/{@code delete}.
     *
     * @param items the list of stored tasks
     * @param date the date to filter by
     * @return one header line followed by one line per matching item
     */
    private static String[] tasksOn(ArrayList<Task> items, LocalDate date) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Here are the tasks occurring on " + date.format(Task.DISPLAY_DATE_FORMAT) + ":");
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).occursOn(date)) {
                lines.add(formatNumberedTask(i + 1, items.get(i)));
            }
        }
        return lines.toArray(new String[0]);
    }
}
