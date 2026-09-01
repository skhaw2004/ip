package stuart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import stuart.exception.StuartException;
import stuart.parser.Parser;
import stuart.storage.Storage;
import stuart.task.Deadlines;
import stuart.task.Events;
import stuart.task.Task;
import stuart.task.TaskList;
import stuart.task.ToDos;
import stuart.ui.Ui;

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
    private TaskList tasks;

    /**
     * Creates a new Stuart chatbot that saves/loads its tasks at {@code filePath}.
     *
     * @param filePath the path to the save file, e.g. {@code "./data/stuart.txt"}
     */
    public Stuart(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);
        this.tasks = new TaskList();
    }

    /**
     * Runs the chatbot: shows the banner and greeting, loads any previously
     * saved tasks, then repeatedly reads and executes commands until the
     * user types {@code bye} or input runs out.
     */
    public void run() {
        ui.showBanner();
        ui.reply("Hello! I'm Stuart.", "What can I do for you?");

        try {
            tasks = new TaskList(storage.load(ui));
        } catch (StuartException e) {
            ui.reply("Warning: " + e.getMessage() + ".");
        }

        // Keep reading commands until the user says "bye", or the input runs out.
        while (ui.hasNextCommand()) {
            Parser.ParsedCommand parsed = Parser.parseCommand(ui.readCommand().trim());
            if (parsed.type() == Parser.CommandType.BYE) {
                break;
            }
            try {
                ui.reply(handleCommand(parsed));
            } catch (StuartException e) {
                ui.reply(e.getMessage());
            }
        }

        ui.reply("Bye. Hope to see you again soon!");
        ui.close();
    }

    /**
     * Launches the Stuart chatbot, saving/loading tasks at {@link #DATA_FILE_PATH}.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        new Stuart(DATA_FILE_PATH).run();
    }

    /**
     * Loads previously saved tasks from disk. {@link #run()} already does
     * this itself (after showing its banner) for the console interface;
     * a GUI entry point has no equivalent startup sequence, so it should
     * call this once before the first {@link #getResponse(String)}.
     *
     * @return a warning message if loading failed, or an empty string otherwise
     */
    public String initialize() {
        try {
            tasks = new TaskList(storage.load(ui));
            return "";
        } catch (StuartException e) {
            return "Warning: " + e.getMessage() + ".";
        }
    }

    /**
     * Generates a response for a single line of user input, for use by a GUI
     * that sends one command at a time rather than looping over a
     * {@code Scanner} the way {@link #run()} does.
     *
     * @param input one line of user input, as typed
     * @return the chatbot's reply text
     */
    public String getResponse(String input) {
        Parser.ParsedCommand parsed = Parser.parseCommand(input.trim());
        if (parsed.type() == Parser.CommandType.BYE) {
            return "Bye. Hope to see you again soon!";
        }
        try {
            return String.join("\n", handleCommand(parsed));
        } catch (StuartException e) {
            return e.getMessage();
        }
    }

    /**
     * Executes a parsed command (every type except {@code BYE}, which the
     * caller handles itself) and returns its reply as one line per element.
     *
     * @param parsed the command to execute
     * @return the reply lines to show the user
     * @throws StuartException if {@code parsed} is malformed or invalid
     */
    private String[] handleCommand(Parser.ParsedCommand parsed) throws StuartException {
        switch (parsed.type()) {
            case LIST:
                return listItems(tasks.getAll(), "Here are the tasks in your list:");
            case SORTED:
                return listItems(sortedByDate(tasks.getAll()), "Here are your tasks sorted by date:");
            case ON:
                if (parsed.arguments().isEmpty()) {
                    throw new StuartException("Please specify a date, e.g. \"on 2019-10-15\".");
                }
                LocalDate date = Parser.parseDate(parsed.arguments());
                return tasksOn(tasks.getAll(), date);
            case FIND:
                if (parsed.arguments().isEmpty()) {
                    throw new StuartException(
                            "Please specify the keyword you want to find tasks with, e.g find shopping");
                }
                String keyword = parsed.arguments();
                return tasksFind(tasks.getAll(), keyword);
            case MARK: {
                int index = Parser.parseIndex(parsed.arguments());
                Task task = tasks.get(index);
                task.markAsDone();
                storage.save(tasks.getAll(), ui);
                return new String[] {"Nice! I've marked this task as done:", "  " + withOverdueFlag(task)};
            }
            case UNMARK: {
                int index = Parser.parseIndex(parsed.arguments());
                Task task = tasks.get(index);
                task.markAsNotDone();
                storage.save(tasks.getAll(), ui);
                return new String[] {"OK, I've marked this task as not done yet:", "  " + withOverdueFlag(task)};
            }
            case DELETE: {
                int index = Parser.parseIndex(parsed.arguments());
                Task removedTask = tasks.delete(index);
                storage.save(tasks.getAll(), ui);
                return new String[] {
                    "Noted. I've removed this task:",
                    "  " + withOverdueFlag(removedTask),
                    "Now you have " + tasks.size() + " tasks in the list."
                };
            }
            case TODO:
                if (parsed.arguments().isEmpty()) {
                    throw new StuartException("The description of a todo cannot be empty.");
                }
                Parser.checkNoSaveDelimiter(parsed.arguments());
                return addTask(new ToDos(parsed.arguments()));
            case DEADLINE: {
                Parser.DeadlineFields fields = Parser.parseDeadlineFields(parsed.arguments());
                if (fields.description().isEmpty()) {
                    throw new StuartException("The description of a deadline cannot be empty.");
                }
                if (fields.by().isEmpty()) {
                    throw new StuartException("The \"/by\" date of a deadline cannot be empty.");
                }
                Parser.checkNoSaveDelimiter(fields.description());
                LocalDate byDate = Parser.parseDate(fields.by());
                return addTask(new Deadlines(fields.description(), byDate));
            }
            case EVENT: {
                Parser.EventFields fields = Parser.parseEventFields(parsed.arguments());
                if (fields.description().isEmpty()) {
                    throw new StuartException("The description of an event cannot be empty.");
                }
                if (fields.from().isEmpty() || fields.to().isEmpty()) {
                    throw new StuartException("The \"/from\" and \"/to\" times of an event cannot be empty.");
                }
                Parser.checkNoSaveDelimiter(fields.description());
                LocalDate fromDate = Parser.parseDate(fields.from());
                LocalDate toDate = Parser.parseDate(fields.to());
                return addTask(new Events(fields.description(), fromDate, toDate));
            }
            default:
                throw new StuartException("To add a task, use the following format:\n"
                        + Ui.TEXT_INDENT + "<task type> <task description>");
        }
    }

    /**
     * Appends {@code task} to {@link #tasks} and returns a reply describing it.
     *
     * @param task the task to add
     * @return the reply lines to show the user
     */
    private String[] addTask(Task task) {
        tasks.add(task);
        storage.save(tasks.getAll(), ui);
        return new String[] {
            "Got it. I've added this task:",
            "  " + withOverdueFlag(task),
            "Now you have " + tasks.size() + " tasks in the list."
        };
    }

    /**
     * Builds the numbered listing lines for {@code items}, with the given header.
     *
     * @param items the tasks to list, in the order they should be numbered
     * @param header the line to print above the numbered list
     * @return one header line followed by one line per item,
     *         formatted as "{@code index.[status] item}"
     */
    private static String[] listItems(List<Task> items, String header) {
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
    private static List<Task> sortedByDate(List<Task> items) {
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
    private static String[] tasksOn(List<Task> items, LocalDate date) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Here are the tasks occurring on " + date.format(Task.DISPLAY_DATE_FORMAT) + ":");
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).occursOn(date)) {
                lines.add(formatNumberedTask(i + 1, items.get(i)));
            }
        }
        return lines.toArray(new String[0]);
    }

    /**
     * Builds the numbered listing lines for the tasks containing
     * keyword in its description
     *
     * @param items the list of stored tasks
     * @param keyword the string to filter by
     * @return one header line followed by one line per matching item
     */
    private static String[] tasksFind(List<Task> items, String keyword) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Here are the tasks with keyword: " + keyword + ":");
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).containsKeyword(keyword)) {
                lines.add(formatNumberedTask(i + 1, items.get(i)));
            }
        }
        return lines.toArray(new String[0]);
    }
}
