package noro.geekbrains.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.Notifications;

import java.util.Scanner;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("File Transfer");
        primaryStage.setScene(new Scene(root, 500, 375));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

//    public static void notifier(String pTitle, String pMessage) {
//        Platform.runLater(() -> {
//                    Stage owner = new Stage(StageStyle.TRANSPARENT);
//                    StackPane root = new StackPane();
//                    root.setStyle("-fx-background-color: TRANSPARENT");
//                    Scene scene = new Scene(root, 1, 1);
//                    scene.setFill(Color.TRANSPARENT);
//                    owner.setScene(scene);
//                    owner.setWidth(1);
//                    owner.setHeight(1);
//                    owner.toBack();
//                    owner.show();
//                    Notifications.create().title(pTitle).text(pMessage).showInformation();
//                }
//        );
//    }
}


//public class Main {
//    static Scanner sc = new Scanner(System.in);
//
//    public static void main(String[] args) {
//        Client client = new Client("Norik", "11111N");
//
//    }
//
//    public static void commands(String arg) throws Exception {
//        if (arg == null) {
//            System.out.println("Provide a command");
//            arg = sc.next();
//            commands(arg);
//        }
//        if (arg.equals("--help")) {
//            help();
//        }
//    }
//    public static void help() {
//        System.out.println("-u <username>");
//        System.out.println("-p <password>");
//    }
//}
