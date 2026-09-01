package stuart.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import stuart.Stuart;

/**
 * Controller for the main GUI, defined in {@code /view/MainWindow.fxml}.
 */
public class MainWindow extends AnchorPane {
    private final Image userImage = new Image(this.getClass().getResourceAsStream("/images/messi_ugly.jpeg"));
    private final Image stuartImage = new Image(this.getClass().getResourceAsStream("/images/stuart_chinese.png"));

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Stuart stuart;

    @FXML
    private void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Injects the Stuart instance this window talks to, loads its previously
     * saved tasks, and shows its greeting as the first message.
     *
     * @param s the chatbot instance to use
     */
    public void setStuart(Stuart s) {
        stuart = s;
        String warning = stuart.initialize();
        String greeting = "Hello! I'm Stuart. What can I do for you?";
        dialogContainer.getChildren().add(
                DialogBox.getStuartDialog(warning.isEmpty() ? greeting : warning + "\n" + greeting, stuartImage));
    }

    /**
     * Creates dialog boxes for the user's input and Stuart's reply to it,
     * appends both to the conversation, and clears the input field.
     */
    @FXML
    private void handleUserInput() {
        String userText = userInput.getText();
        if (userText.isBlank()) {
            return;
        }
        String stuartText = stuart.getResponse(userText);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText, userImage),
                DialogBox.getStuartDialog(stuartText, stuartImage)
        );
        userInput.clear();
    }
}
