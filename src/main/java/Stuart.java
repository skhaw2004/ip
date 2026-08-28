import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Optional;
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

    /** ANSI code that makes following text bold and cyan. */
    private static final String BANNER_COLOR = "[1;36m";

    /** ANSI code that resets text formatting back to the terminal default. */
    private static final String ANSI_RESET = "[0m";

    /** Filled block-letter "STUART" banner, one row per element. */
    private static final String[] BANNER_LINES = {
        "█████ █████ █   █  ███  ████  █████",
        "█       █   █   █ █   █ █   █   █  ",
        "█████   █   █   █ █████ ████    █  ",
        "    █   █   █   █ █   █ █  █    █  ",
        "█████   █   █████ █   █ █   █   █  ",
    };

    /** How many colour steps the banner animation cycles through. */
    private static final int BANNER_ANIMATION_FRAMES = 60;

    /** Milliseconds between each colour step of the banner animation. */
    private static final int BANNER_FRAME_DELAY_MS = 40;

    /** Path to the file tasks are saved to, relative to the project root. */
    private static final String DATA_FILE_PATH = "./data/stuart.txt";

    // Creating of STUART banner was assisted by Claude Code
    public static void main(String[] args) {
        printBanner();
        reply("Hello! I'm Stuart.", "What can I do for you?");

        ArrayList<Task> items = loadTasks();

        try (Scanner scanner = new Scanner(System.in)) {
            // Keep reading commands until the user says "bye", or the input runs out.
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                String trimmedCommand = command.trim();
                try {
                    if (trimmedCommand.equals("bye")) {
                        break;
                    } else if (trimmedCommand.equals("list")) {
                        // output list of tasks
                        reply(listItems(items, "Here are the tasks in your list:"));
                    } else if (trimmedCommand.equals("sorted")) {
                        // output list of tasks sorted by date, dateless tasks last
                        reply(listItems(sortedByDate(items), "Here are your tasks sorted by date:"));
                    } else if (trimmedCommand.equals("on") || trimmedCommand.startsWith("on ")) {
                        // list tasks occurring on a specific date
                        String dateText = trimmedCommand.substring("on".length()).trim();
                        if (dateText.isEmpty()) {
                            throw new StuartException("Please specify a date, e.g. \"on 2019-10-15\".");
                        }
                        LocalDate date = parseDate(dateText);
                        reply(tasksOn(items, date));
                    } else if (trimmedCommand.startsWith("mark ")) {
                        // mark a task
                        int index = parseIndex(trimmedCommand.substring("mark ".length()));
                        if (isValidIndex(index, items.size())) {
                            items.get(index).markAsDone();
                            saveTasks(items);
                            reply("Nice! I've marked this task as done:",
                                    "  " + items.get(index));
                        } else {
                            throw new StuartException("That's not a valid task number.");
                        }
                    } else if (trimmedCommand.startsWith("unmark ")) {
                        // unmark a task
                        int index = parseIndex(trimmedCommand.substring("unmark ".length()));
                        if (isValidIndex(index, items.size())) {
                            items.get(index).markAsNotDone();
                            saveTasks(items);
                            reply("OK, I've marked this task as not done yet:",
                                    "  " + items.get(index));
                        } else {
                            throw new StuartException("That's not a valid task number.");
                        }
                    } else if (trimmedCommand.startsWith("delete ")) {
                        // delete a task
                        int index = parseIndex(trimmedCommand.substring("delete ".length()));
                        if (isValidIndex(index, items.size())) {
                            Task removedTask = items.remove(index);
                            saveTasks(items);
                            reply("Noted. I've removed this task:",
                                    "  " + removedTask,
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
                                    + TEXT_INDENT + "e.g. deadline return book /by 2019-10-15");
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
                                    + TEXT_INDENT + "e.g. event meeting /from 2019-10-15 /to 2019-10-16");
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
     * Prints the STUART banner. Animates it through the RGB spectrum when
     * connected to a real interactive terminal; otherwise (e.g. piped input,
     * many IDE run consoles) prints one static coloured frame, so that
     * automated tests get deterministic output.
     */
    private static void printBanner() {
        if (System.console() != null) {
            animateBanner();
        } else {
            printBannerFrame(BANNER_COLOR);
        }
    }

    /**
     * Prints the banner once, cycling its colour through the RGB spectrum
     * over {@link #BANNER_ANIMATION_FRAMES} steps, redrawing in place.
     */
    private static void animateBanner() {
        for (int frame = 0; frame < BANNER_ANIMATION_FRAMES; frame++) {
            double angle = 2 * Math.PI * frame / BANNER_ANIMATION_FRAMES;
            int red = (int) (Math.sin(angle) * 127 + 128);
            int green = (int) (Math.sin(angle + 2 * Math.PI / 3) * 127 + 128);
            int blue = (int) (Math.sin(angle + 4 * Math.PI / 3) * 127 + 128);
            printBannerFrame("[1;38;2;" + red + ";" + green + ";" + blue + "m");
            try {
                Thread.sleep(BANNER_FRAME_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (frame < BANNER_ANIMATION_FRAMES - 1) {
                // Move the cursor back to the top-left of the banner to redraw over it.
                System.out.print("[" + BANNER_LINES.length + "F");
            }
        }
    }

    /**
     * Prints one frame of the banner in the given ANSI colour code.
     *
     * @param ansiColor the ANSI escape code to colour the banner with
     */
    private static void printBannerFrame(String ansiColor) {
        for (String line : BANNER_LINES) {
            System.out.println(ansiColor + line + ANSI_RESET);
        }
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
    private static void addTask(ArrayList<Task> items, Task task) {
        items.add(task);
        saveTasks(items);
        reply("Got it. I've added this task:", "  " + task,
                "Now you have " + items.size() + " tasks in the list.");
    }

    /**
     * Loads the task list from the save file at {@link #DATA_FILE_PATH}.
     * Returns an empty list if the file does not exist yet (e.g. first run).
     * A line that cannot be parsed is skipped with a warning rather than
     * aborting the whole load, so one corrupted line doesn't cost every
     * other saved task.
     *
     * @return the loaded tasks, in the order they appear in the file
     */
    private static ArrayList<Task> loadTasks() {
        ArrayList<Task> items = new ArrayList<>();
        File dataFile = new File(DATA_FILE_PATH);
        if (!dataFile.exists()) {
            return items;
        }
        try (Scanner fileScanner = new Scanner(dataFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    items.add(parseSavedTask(line));
                } catch (StuartException e) {
                    reply("Warning: skipping a corrupted saved task (" + e.getMessage() + ").");
                }
            }
        } catch (IOException e) {
            reply("Warning: could not load saved tasks (" + e.getMessage() + ").");
        }
        return items;
    }

    /**
     * Parses one line of the save file into a {@link Task}.
     *
     * @param line one line from the save file, e.g. {@code "T | 1 | read book"}
     * @return the task the line describes
     * @throws StuartException if {@code line} is not validly formatted
     */
    private static Task parseSavedTask(String line) throws StuartException {
        String[] parts = line.split(" \\| ");
        if (parts.length < 3) {
            throw new StuartException("expected at least 3 fields: \"" + line + "\"");
        }
        String type = parts[0].trim();
        boolean isDone = parts[1].trim().equals("1");
        String description = parts[2].trim();

        Task task;
        if (type.equals("T")) {
            task = new ToDos(description);
        } else if (type.equals("D")) {
            if (parts.length < 4) {
                throw new StuartException("deadline is missing its \"by\" field: \"" + line + "\"");
            }
            task = new Deadlines(description, parseDate(parts[3].trim()));
        } else if (type.equals("E")) {
            if (parts.length < 5) {
                throw new StuartException("event is missing its \"from\"/\"to\" fields: \"" + line + "\"");
            }
            task = new Events(description, parseDate(parts[3].trim()), parseDate(parts[4].trim()));
        } else {
            throw new StuartException("unknown task type \"" + type + "\": \"" + line + "\"");
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Overwrites the save file at {@link #DATA_FILE_PATH} with the current
     * task list, creating the containing directory if needed. If the write
     * fails, reports it but leaves the in-memory task list untouched.
     *
     * @param items the list of stored tasks
     */
    private static void saveTasks(ArrayList<Task> items) {
        File dataFile = new File(DATA_FILE_PATH);
        File parentDir = dataFile.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(dataFile)) {
            for (Task task : items) {
                writer.write(task.toSaveFormat() + System.lineSeparator());
            }
        } catch (IOException e) {
            reply("Warning: could not save tasks to disk (" + e.getMessage() + ").");
        }
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
            lines[i + 1] = (i + 1) + "." + items.get(i);
        }
        return lines;
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
                lines.add((i + 1) + "." + items.get(i));
            }
        }
        return lines.toArray(new String[0]);
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
