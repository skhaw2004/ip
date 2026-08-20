import java.util.Scanner;

/**
 * Entry point of the Stuart chatbot.
 * Stores whatever text the user enters, lists it back on request, and lets
 * the user mark/unmark items as done, until the user types {@code bye}.
 */
public class Stuart {
    /** Divider printed above and below every reply (without indentation). */
    private static final String HORIZONTAL_LINE = "_".repeat(60);

    /** Indentation applied to each divider line. */
    private static final String DIVIDER_INDENT = "    ";

    /** Indentation applied to every line of text Stuart prints. */
    private static final String TEXT_INDENT = "     ";

    /** Maximum number of items Stuart can remember. */
    private static final int MAX_ITEMS = 100;

    public static void main(String[] args) {
        String banner = " ____   _                       _   \n"
                + "/ ___| | |_  _   _   __ _  _ __ | |_ \n"
                + "\\___ \\ | __|| | | | / _` || '__|| __|\n"
                + " ___) || |_ | |_| || (_| || |   | |_ \n"
                + "|____/  \\__| \\__,_| \\__,_||_|    \\__|\n";
        System.out.print(banner);

        reply("Hello! I'm Stuart.", "What can I do for you?");

        Task[] items = new Task[MAX_ITEMS];
        int itemCount = 0;

        try (Scanner scanner = new Scanner(System.in)) {
            // Keep reading commands until the user says "bye", or the input runs out.
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                String trimmedCommand = command.trim();
                try {
                    if (trimmedCommand.equals("bye")) {
                        break;
                    } else if (trimmedCommand.equals("list")) { // output list of tasks
                        reply(listItems(items, itemCount));
                    } else if (trimmedCommand.startsWith("mark ")) { // mark a task
                        int index = parseIndex(trimmedCommand.substring("mark ".length()));
                        if (isValidIndex(index, itemCount)) {
                            items[index].markAsDone();
                            reply("Nice! I've marked this task as done:",
                                    "  " + items[index]);
                        } else {
                            throw new StuartException("That's not a valid task number.");
                        }
                    } else if (trimmedCommand.startsWith("unmark ")) { // unmark a task
                        int index = parseIndex(trimmedCommand.substring("unmark ".length()));
                        if (isValidIndex(index, itemCount)) {
                            items[index].markAsNotDone();
                            reply("OK, I've marked this task as not done yet:",
                                    "  " + items[index]);
                        } else {
                            throw new StuartException("That's not a valid task number.");
                        }
                    } else if (trimmedCommand.equals("todo") || trimmedCommand.startsWith("todo ")) {
                        // add a to-do
                        String description = trimmedCommand.substring("todo".length()).trim();
                        if (description.isEmpty()) {
                            throw new StuartException("The description of a todo cannot be empty.");
                        }
                        itemCount = addTask(items, itemCount, new ToDos(description));
                    } else if (trimmedCommand.equals("deadline") || trimmedCommand.startsWith("deadline ")) {
                        // add a deadline
                        String rest = trimmedCommand.substring("deadline".length()).trim();
                        int byIndex = rest.indexOf("/by");
                        if (byIndex == -1) {
                            throw new StuartException("A deadline needs a description and \"/by <when>\", \n"
                                    + TEXT_INDENT + "e.g. deadline return book /by Sunday");
                        }
                        String description = rest.substring(0, byIndex).trim();
                        String by = rest.substring(byIndex + "/by".length()).trim();
                        if (description.isEmpty()) {
                            throw new StuartException("The description of a deadline cannot be empty.");
                        }
                        itemCount = addTask(items, itemCount, new Deadlines(description, by));
                    } else if (trimmedCommand.equals("event") || trimmedCommand.startsWith("event ")) {
                        // add an event
                        String rest = trimmedCommand.substring("event".length()).trim();
                        int fromIndex = rest.indexOf("/from");
                        int toIndex = rest.indexOf("/to");
                        if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {
                            throw new StuartException(
                                    "An event needs a description, \"/from <start>\", and \"/to <end>\", \n"
                                    + TEXT_INDENT + "e.g. event meeting /from Mon 2pm /to Mon 4pm");
                        }
                        String description = rest.substring(0, fromIndex).trim();
                        String from = rest.substring(fromIndex + "/from".length(), toIndex).trim();
                        String to = rest.substring(toIndex + "/to".length()).trim();
                        if (description.isEmpty()) {
                            throw new StuartException("The description of an event cannot be empty.");
                        }
                        itemCount = addTask(items, itemCount, new Events(description, from, to));
                    } else {
                        throw new StuartException("To add a task, use the following format:\n" + TEXT_INDENT + "<task type> <task description>");
                    }
                } catch (StuartException e) {
                    reply(e.getMessage());
                }
            }
        }

        reply("Bye. Hope to see you again soon!");
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
     * Stores {@code task} at the next free slot and reports it.
     *
     * @param items the backing array of stored tasks
     * @param itemCount the number of tasks currently stored
     * @param task the task to add
     * @return the new item count, {@code itemCount + 1}
     * @throws StuartException if {@code items} is already full
     */
    private static int addTask(Task[] items, int itemCount, Task task) throws StuartException {
        if (itemCount >= MAX_ITEMS) {
            throw new StuartException("OOPS!!! Sorry, I can't store more than " + MAX_ITEMS + " items.");
        }
        items[itemCount] = task;
        reply("Got it. I've added this task:\n " + TEXT_INDENT + " " + task + "\n"
                + TEXT_INDENT + "Now you have " + (itemCount + 1) + " tasks in the list.");
        return itemCount + 1;
    }

    /**
     * Builds the numbered listing lines for the stored items, with a header.
     *
     * @param items the backing array of stored descriptions
     * @param itemCount the number of items currently stored
     * @return one header line followed by one line per item,
     *         formatted as "{@code index.[status] item}"
     */
    private static String[] listItems(Task[] items, int itemCount) {
        String[] lines = new String[itemCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < itemCount; i++) {
            lines[i + 1] = (i + 1) + "." + items[i];
        }
        return lines;
    }

    /**
     * Prints one reply from Stuart: the given lines, indented and wrapped
     * between horizontal dividers, followed by a blank line.
     *
     * @param lines the lines of text to display
     */
    private static void reply(String... lines) {
        System.out.println(DIVIDER_INDENT + HORIZONTAL_LINE);
        for (String line : lines) {
            System.out.println(TEXT_INDENT + line);
        }
        System.out.println(DIVIDER_INDENT + HORIZONTAL_LINE);
        System.out.println();
    }
}
