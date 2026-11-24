package pe.edu.upeu.systurismo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pe.edu.upeu.systurismo.repository.CrudRepository;
import pe.edu.upeu.systurismo.service.SistemaService;

public class LoginController {
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;

    private final SistemaService service = new SistemaService(new CrudRepository());

    @FXML
    private void ingresar() {
        String usuario = txtUsuario.getText().trim();
        String clave = txtClave.getText();

        if (usuario.isBlank() || clave.isBlank()) {
            alert("Ingrese usuario y contraseña.");
            return;
        }

        try {
            if (service.login(usuario, clave)) {
                String rol = service.obtenerRol(usuario);
                new MainController(usuario, rol).mostrar((Stage) txtUsuario.getScene().getWindow());
            } else {
                alert("Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
            alert("No se pudo iniciar sesión: " + e.getMessage());
        }
    }

    @FXML
    private void salir() {
        ((Stage) txtUsuario.getScene().getWindow()).close();
    }

    private void alert(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK).showAndWait();
    }
}
