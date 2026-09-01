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
        readLoop:
        while (ui.hasNextCommand()) {
            Parser.ParsedCommand parsed = Parser.parseCommand(ui.readCommand().trim());
            try {
                switch (parsed.type()) {
                    case BYE:
                        break readLoop;
                    case LIST:
                        // output list of tasks
                        ui.reply(listItems(tasks.getAll(), "Here are the tasks in your list:"));
                        break;
                    case SORTED:
                        // output list of tasks sorted by date, dateless tasks last
                        ui.reply(listItems(sortedByDate(tasks.getAll()), "Here are your tasks sorted by date:"));
                        break;
                    case ON:
                        // list tasks occurring on a specific date
                        if (parsed.arguments().isEmpty()) {
                            throw new StuartException("Please specify a date, e.g. \"on 2019-10-15\".");
                        }
                        LocalDate date = Parser.parseDate(parsed.arguments());
                        ui.reply(tasksOn(tasks.getAll(), date));
                        break;
                    case FIND:
                        // list tasks that contains keyword in its description
                        if (parsed.arguments().isEmpty()) {
                            throw new StuartException(
                                    "Please specify the keyword you want to find tasks with, e.g find shopping");
                        }
                        String keyword = parsed.arguments();
                        ui.reply(tasksFind(tasks.getAll(), keyword));
                        break;
                    case MARK: {
                        // mark a task
                        int index = Parser.parseIndex(parsed.arguments());
                        Task task = tasks.get(index);
                        task.markAsDone();
                        storage.save(tasks.getAll(), ui);
                        ui.reply("Nice! I've marked this task as done:", "  " + withOverdueFlag(task));
                        break;
                    }
                    case UNMARK: {
                        // unmark a task
                        int index = Parser.parseIndex(parsed.arguments());
                        Task task = tasks.get(index);
                        task.markAsNotDone();
                        storage.save(tasks.getAll(), ui);
                        ui.reply("OK, I've marked this task as not done yet:", "  " + withOverdueFlag(task));
                        break;
                    }
                    case DELETE: {
                        // delete a task
                        int index = Parser.parseIndex(parsed.arguments());
                        Task removedTask = tasks.delete(index);
                        storage.save(tasks.getAll(), ui);
                        ui.reply("Noted. I've removed this task:",
                                "  " + withOverdueFlag(removedTask),
                                "Now you have " + tasks.size() + " tasks in the list.");
                        break;
                    }
                    case TODO:
                        // add a to-do
                        if (parsed.arguments().isEmpty()) {
                            // empty description
                            throw new StuartException("The description of a todo cannot be empty.");
                        }
                        Parser.checkNoSaveDelimiter(parsed.arguments());
                        addTask(new ToDos(parsed.arguments()));
                        break;
                    case DEADLINE: {
                        // add a deadline
                        Parser.DeadlineFields fields = Parser.parseDeadlineFields(parsed.arguments());
                        if (fields.description().isEmpty()) {
                            // empty description
                            throw new StuartException("The description of a deadline cannot be empty.");
                        }
                        if (fields.by().isEmpty()) {
                            throw new StuartException("The \"/by\" date of a deadline cannot be empty.");
                        }
                        Parser.checkNoSaveDelimiter(fields.description());
                        LocalDate byDate = Parser.parseDate(fields.by());
                        addTask(new Deadlines(fields.description(), byDate));
                        break;
                    }
                    case EVENT: {
                        // add an event
                        Parser.EventFields fields = Parser.parseEventFields(parsed.arguments());
                        if (fields.description().isEmpty()) {
                            // empty description
                            throw new StuartException("The description of an event cannot be empty.");
                        }
                        if (fields.from().isEmpty() || fields.to().isEmpty()) {
                            throw new StuartException("The \"/from\" and \"/to\" times of an event cannot be empty.");
                        }
                        Parser.checkNoSaveDelimiter(fields.description());
                        LocalDate fromDate = Parser.parseDate(fields.from());
                        LocalDate toDate = Parser.parseDate(fields.to());
                        addTask(new Events(fields.description(), fromDate, toDate));
                        break;
                    }
                    default:
                        // not any recognized command
                        throw new StuartException("To add a task, use the following format:\n"
                                + Ui.TEXT_INDENT + "<task type> <task description>");
                }
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
     * Appends {@code task} to {@link #tasks} and reports it.
     *
     * @param task the task to add
     */
    private void addTask(Task task) {
        tasks.add(task);
        storage.save(tasks.getAll(), ui);
        ui.reply("Got it. I've added this task:", "  " + withOverdueFlag(task),
                "Now you have " + tasks.size() + " tasks in the list.");
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
