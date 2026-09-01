package stuart.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * A chat bubble: a speaker's avatar next to their message text. Used for
 * both the user's messages and Stuart's replies, mirrored left/right via
 * {@link #getUserDialog} and {@link #getStuartDialog}.
 */
public class DialogBox extends HBox {
    private final Label text;
    private final ImageView displayPicture;

    private DialogBox(String s, Image i) {
        text = new Label(s);
        text.setWrapText(true);

        displayPicture = new ImageView(i);
        displayPicture.setFitWidth(100.0);
        displayPicture.setFitHeight(100.0);

        this.setAlignment(Pos.TOP_RIGHT);
        this.getChildren().addAll(text, displayPicture);
    }

    /**
     * Mirrors this dialog box so the avatar sits on the left and the text on
     * the right, to visually distinguish Stuart's replies from user input.
     */
    private void flip() {
        this.setAlignment(Pos.TOP_LEFT);
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        FXCollections.reverse(tmp);
        this.getChildren().setAll(tmp);
    }

    /**
     * Creates a dialog box for a message the user sent.
     *
     * @param s the message text
     * @param i the user's avatar
     * @return the dialog box, with the avatar on the right
     */
    public static DialogBox getUserDialog(String s, Image i) {
        return new DialogBox(s, i);
    }

    /**
     * Creates a dialog box for one of Stuart's replies.
     *
     * @param s the reply text
     * @param i Stuart's avatar
     * @return the dialog box, mirrored so the avatar is on the left
     */
    public static DialogBox getStuartDialog(String s, Image i) {
        DialogBox db = new DialogBox(s, i);
        db.flip();
        return db;
    }
}
