package stuart.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A chat bubble containing a speaker's message text, defined in
 * {@code /view/DialogBox.fxml}. Used for both the user's messages and
 * Stuart's replies, styled and aligned to opposite sides via
 * {@link #getUserDialog} and {@link #getStuartDialog}.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;

    private DialogBox(String text) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load /view/DialogBox.fxml", e);
        }

        dialog.setText(text);
    }

    /**
     * Restyles this dialog box as a reply: aligned left, with the "other
     * speaker" bubble color, instead of the default right-aligned "mine".
     */
    private void flip() {
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("bubble-other");
    }

    /**
     * Creates a dialog box for a message the user sent.
     *
     * @param text the message text
     * @return the dialog box, right-aligned in the "mine" bubble color
     */
    public static DialogBox getUserDialog(String text) {
        DialogBox db = new DialogBox(text);
        db.dialog.getStyleClass().add("bubble-mine");
        return db;
    }

    /**
     * Creates a dialog box for one of Stuart's replies.
     *
     * @param text the reply text
     * @return the dialog box, left-aligned in the "other speaker" bubble color
     */
    public static DialogBox getStuartDialog(String text) {
        DialogBox db = new DialogBox(text);
        db.flip();
        return db;
    }
}
