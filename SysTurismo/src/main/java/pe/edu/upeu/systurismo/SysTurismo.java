package pe.edu.upeu.systurismo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pe.edu.upeu.systurismo.config.DatabaseConfig;

public class SysTurismo extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConfig.iniciar();
        mostrarLogin(stage);
    }

    public void mostrarLogin(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        stage.setTitle("SysTurismo - Inicio de sesión");
        stage.setScene(new Scene(loader.load(), 440, 350));
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConfig.cerrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
