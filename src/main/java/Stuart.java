import java.util.Scanner;

/**
 * Entry point of the Stuart chatbot.
 * Echoes back each command the user types, until the user types {@code bye}.
 */
public class Stuart {
    /** Divider printed above and below every reply. */
    private static final String HORIZONTAL_LINE = "    " + "_".repeat(60);

    /** Indentation applied to every line of text Stuart prints. */
    private static final String INDENT = "     ";

    public static void main(String[] args) {
        String banner = " ____   _                       _   \n"
                + "/ ___| | |_  _   _   __ _  _ __ | |_ \n"
                + "\\___ \\ | __|| | | | / _` || '__|| __|\n"
                + " ___) || |_ | |_| || (_| || |   | |_ \n"
                + "|____/  \\__| \\__,_| \\__,_||_|    \\__|\n";
        System.out.println(banner);

        reply("Hello! I'm Stuart.", "What can I do for you?");

        Scanner scanner = new Scanner(System.in);
        // Keep reading commands until the user says "bye", or the input runs out.
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (command.equals("bye")) {
                break;
            }
            reply(command);
        }

        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Prints one reply from Stuart: the given lines, indented and wrapped
     * between horizontal dividers, followed by a blank line.
     *
     * @param lines the lines of text to display
     */
    private static void reply(String... lines) {
        System.out.println(HORIZONTAL_LINE);
        for (String line : lines) {
            System.out.println(INDENT + line);
        }
        System.out.println(HORIZONTAL_LINE);
        System.out.println();
    }
}
