import java.util.Scanner;

/**
 * Entry point of the Stuart chatbot.
 * Stores whatever text the user enters, and lists it back on request,
 * until the user types {@code bye}.
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

        String[] items = new String[MAX_ITEMS];
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
                } else {
                    items[itemCount] = command;
                    itemCount++;
                    reply("added: " + command);
                }
            }
        }

        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Builds the numbered listing lines for the stored items.
     *
     * @param items the backing array of stored items
     * @param itemCount the number of items currently stored
     * @return one line per item, formatted as "{@code index. item}"
     */
    private static String[] listItems(String[] items, int itemCount) {
        String[] lines = new String[itemCount];
        for (int i = 0; i < itemCount; i++) {
            lines[i] = (i + 1) + ". " + items[i];
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
