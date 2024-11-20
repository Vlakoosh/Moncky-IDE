package ide;


import ide.model.Model;
import ide.view.Presenter;
import ide.view.View;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Model model = new Model();
        View view = new View();
        primaryStage.setScene(new Scene(view));
        primaryStage.setTitle("IDE");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        primaryStage.setResizable(true);
        new Presenter(model, view);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
