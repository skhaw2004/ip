/**
 * Entry point of the Stuart chatbot.
 */
public class Stuart {
    public static void main(String[] args) {
        String banner = " ____   _                       _   \n"
                + "/ ___| | |_  _   _   __ _  _ __ | |_ \n"
                + "\\___ \\ | __|| | | | / _` || '__|| __|\n"
                + " ___) || |_ | |_| || (_| || |   | |_ \n"
                + "|____/  \\__| \\__,_| \\__,_||_|    \\__|\n";
        String horizontalLine = "_".repeat(60);
        System.out.println(horizontalLine);
        System.out.println(banner);
        System.out.println("Hello! I'm Stuart. \nWhat can I do for you?");
        System.out.println(horizontalLine);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(horizontalLine);

    }
}
