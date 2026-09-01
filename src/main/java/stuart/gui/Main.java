package stuart.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import stuart.Stuart;

/**
 * A GUI for Stuart using FXML.
 */
public class Main extends Application {
    private static final String DATA_FILE_PATH = "./data/stuart.txt";

    private final Stuart stuart = new Stuart(DATA_FILE_PATH);

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();
            Scene scene = new Scene(ap);
            scene.getStylesheets().add(Main.class.getResource("/view/telegram.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Stuart");
            stage.setResizable(false);
            fxmlLoader.<MainWindow>getController().setStuart(stuart);
            stage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load /view/MainWindow.fxml", e);
        }
    }
}
