package noro.geekbrains.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("File Transfer");
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);

        primaryStage.minWidthProperty().bind(scene.heightProperty().multiply(1.5));
        primaryStage.minHeightProperty().bind(scene.widthProperty().divide(1.5));

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}


