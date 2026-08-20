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
                if (trimmedCommand.equals("bye")) {
                    break;
                } else if (trimmedCommand.equals("list")) {
                    reply(listItems(items, itemCount));
                } else if (trimmedCommand.startsWith("mark ")) {
                    int index = parseIndex(trimmedCommand.substring("mark ".length()));
                    if (isValidIndex(index, itemCount)) {
                        items[index].markAsDone();
                        reply("Nice! I've marked this task as done:",
                                "  " + items[index]);
                    } else {
                        reply("That's not a valid task number.");
                    }
                } else if (trimmedCommand.startsWith("unmark ")) {
                    int index = parseIndex(trimmedCommand.substring("unmark ".length()));
                    if (isValidIndex(index, itemCount)) {
                        items[index].markAsNotDone();
                        reply("OK, I've marked this task as not done yet:",
                                "  " + items[index]);
                    } else {
                        reply("That's not a valid task number.");
                    }
                } else if (itemCount >= MAX_ITEMS) {
                    reply("Sorry, I can't store more than " + MAX_ITEMS + " items.");
                } else {
                    items[itemCount] = new Task(command);
                    itemCount++;
                    reply("added: " + command);
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
     * Formats a single task as its done-status icon plus its description,
     * e.g. {@code "[X] read book"}.
     *
     * @param items the backing array of stored descriptions
     * @param done the backing array of done statuses, parallel to {@code items}
     * @param index the 0-based index of the task to format
     * @return the formatted task line
     */
    private static String formatTask(String[] items, boolean[] done, int index) {
        String statusIcon = done[index] ? "X" : " ";
        return "[" + statusIcon + "] " + items[index];
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
