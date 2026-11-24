package pe.edu.upeu.systurismo.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import pe.edu.upeu.systurismo.dto.PersonaDto;
import pe.edu.upeu.systurismo.repository.CrudRepository;
import pe.edu.upeu.systurismo.service.SistemaService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class MainController {
    private final SistemaService service = new SistemaService(new CrudRepository());
    private final String usuario;
    private final String rol;
    private final Map<String, Def> definiciones = new LinkedHashMap<>();
    private final Map<TableView<Map<String, Object>>, Paginador> paginadores = new HashMap<>();
    private static final int FILAS_POR_PAGINA = 10;

    private boolean esAdmin() { return "Administrador".equals(rol); }

    /** Perfil Vendedor: Destinos y Paquetes en solo lectura. */
    private boolean soloLectura(Def d) {
        return !esAdmin() && (d.tabla.equals("destino") || d.tabla.equals("paquete"));
    }

    public MainController(String usuario, String rol) {
        this.usuario = usuario;
        this.rol = rol;
        definiciones.put("Clientes", new Def("cliente", "id_cliente",
                new String[]{"dni", "nombres", "correo", "telefono"}));
        definiciones.put("Destinos", new Def("destino", "id_destino",
                new String[]{"nombre", "ciudad", "descripcion"}));
        definiciones.put("Paquetes", new Def("paquete", "id_paquete",
                new String[]{"nombre", "id_destino", "dias", "precio", "cupos"}));
        definiciones.put("Reservas", new Def("reserva", "id_reserva",
                new String[]{"id_cliente", "id_paquete", "fecha_reserva", "pasajeros", "total", "estado"}));
        definiciones.put("Pagos", new Def("pago", "id_pago",
                new String[]{"id_reserva", "fecha_pago", "monto", "metodo", "estado"}));
        definiciones.put("Boletas", new Def("boleta", "id_boleta",
                new String[]{"id_reserva", "fecha_emision", "numero", "total"}));
        if (esAdmin()) {
            definiciones.put("Usuarios", new Def("usuario", "id_usuario",
                    new String[]{"usuario", "clave", "rol"}));
        }
    }

    public void mostrar(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));

        Label titulo = new Label("SysTurismo | Agencia de Turismo");
        titulo.setStyle("-fx-font-size:21px;-fx-font-weight:bold;-fx-text-fill:#164b78;");
        Label sesion = new Label("Usuario: " + usuario + "  |  Rol: " + rol);
        Button salir = new Button("Cerrar sesión");
        salir.setOnAction(e -> {
            try {
                new pe.edu.upeu.systurismo.SysTurismo().mostrarLogin(new Stage());
                stage.close();
            } catch (Exception ex) {
                mensaje("No se pudo volver al inicio: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        VBox datosSesion = new VBox(4, titulo, sesion);
        HBox top = new HBox(15, datosSesion, new Region(), salir);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        root.setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 12, 0));

        TabPane tabs = new TabPane();
        Tab inicio = new Tab("Inicio", pantallaInicio());
        inicio.setClosable(false);
        tabs.getTabs().add(inicio);
        tabs.getTabs().add(crearTabPrecios());

        for (String nombre : definiciones.keySet()) {
            tabs.getTabs().add(crearTab(nombre, definiciones.get(nombre)));
        }
        if (esAdmin()) tabs.getTabs().add(crearTabReportes());
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1150, 720);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("SysTurismo - Menú principal");
        stage.setScene(scene);
        stage.setMinWidth(950);
        stage.setMinHeight(620);
        stage.setMaximized(true);
        stage.show();
    }

    private VBox pantallaInicio() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(22));
        Label bienvenida = new Label("Bienvenido a SysTurismo");
        bienvenida.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:#164b78;");
        box.getChildren().addAll(bienvenida,
                new Label("Sistema de gestión para una agencia de turismo."),
                new Label("En la pestaña \"Precios\" puede consultar el precio de cada paquete y calcular el total según los pasajeros."),
                new Label("Desde las pestañas puede administrar clientes, destinos, paquetes, reservas, pagos y boletas."),
                new Label("Para registrar una reserva primero debe registrar al cliente y el paquete turístico."),
                new Label("Para registrar un pago primero debe existir una reserva."),
                new Label(esAdmin()
                        ? "Perfil Administrador: acceso completo, gestión de usuarios y reportes."
                        : "Perfil Vendedor: puede registrar clientes, reservas, pagos y boletas; Destinos y Paquetes son de solo lectura."));
        return box;
    }

    private Tab crearTab(String nombre, Def d) {
        TableView<Map<String, Object>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (String columna : columnasDe(d)) {
            if (columna.equals("clave")) continue; // nunca se muestra el hash de la contraseña
            TableColumn<Map<String, Object>, String> c = new TableColumn<>(etiqueta(columna));
            c.setCellValueFactory(x -> new javafx.beans.property.SimpleStringProperty(
                    formatear(columna, x.getValue().get(columna))));
            tabla.getColumns().add(c);
        }

        Button nuevo = new Button("Nuevo");
        Button editar = new Button("Editar");
        Button eliminar = new Button("Eliminar");
        Button actualizar = new Button("Actualizar");
        Button accionExtra = new Button(d.tabla.equals("reserva") ? "Emitir boleta" : "Generar comprobante");
        accionExtra.setVisible(d.tabla.equals("reserva") || d.tabla.equals("boleta"));
        accionExtra.setManaged(accionExtra.isVisible());

        nuevo.setOnAction(e -> formulario(d, null, tabla));
        editar.setOnAction(e -> {
            Map<String, Object> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila == null) mensaje("Seleccione un registro para editar.", Alert.AlertType.WARNING);
            else formulario(d, fila, tabla);
        });
        eliminar.setOnAction(e -> {
            Map<String, Object> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila == null) {
                mensaje("Seleccione un registro para eliminar.", Alert.AlertType.WARNING);
                return;
            }
            if (d.tabla.equals("usuario") && usuario.equals(String.valueOf(fila.get("usuario")))) {
                mensaje("No puede eliminar el usuario con el que inició sesión.", Alert.AlertType.WARNING);
                return;
            }
            if (confirmar("¿Está seguro de eliminar el registro seleccionado?")) {
                try {
                    service.eliminar(d.tabla, d.pk, fila.get(d.pk));
                    cargar(tabla, d);
                    mensaje("Registro eliminado correctamente.", Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    mensaje("No se pudo eliminar. Verifique si el registro está relacionado con otros datos.\n" +
                            ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
        actualizar.setOnAction(e -> cargar(tabla, d));
        accionExtra.setOnAction(e -> {
            Map<String, Object> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila == null) {
                mensaje("Seleccione un registro.", Alert.AlertType.WARNING);
                return;
            }
            if (d.tabla.equals("reserva")) emitirBoleta(fila, tabla);
            else generarComprobante(fila);
        });

        if (soloLectura(d)) {
            for (Button b : List.of(nuevo, editar, eliminar)) { b.setVisible(false); b.setManaged(false); }
        }
        // Las boletas se emiten desde Reservas (el total siempre coincide con la reserva): no se crean ni editan a mano.
        if (d.tabla.equals("boleta")) {
            editar.setVisible(false); editar.setManaged(false);
        }
        if (!esAdmin()) { eliminar.setVisible(false); eliminar.setManaged(false); }

        Paginador pag = new Paginador();
        paginadores.put(tabla, pag);
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar...");
        buscar.setPrefWidth(220);
        buscar.textProperty().addListener((obs, a, b) -> { pag.filtro = b.trim().toLowerCase(); pag.pagina = 0; refrescar(tabla); });
        pag.anterior.setOnAction(e -> { pag.pagina--; refrescar(tabla); });
        pag.siguiente.setOnAction(e -> { pag.pagina++; refrescar(tabla); });

        HBox botones = new HBox(8, nuevo, editar, eliminar, actualizar, accionExtra);
        HBox pie = new HBox(10, pag.anterior, pag.info, pag.siguiente);
        pie.setAlignment(javafx.geometry.Pos.CENTER);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(8, botones, espacio, new Label("Buscar:"), buscar);
        barra.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox contenido = new VBox(10, barra, tabla, pie);
        contenido.setPadding(new Insets(10));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        Tab tab = new Tab(nombre, contenido);
        tab.setClosable(false);
        cargar(tabla, d);
        return tab;
    }

    private List<String> columnasDe(Def d) {
        List<String> columnas = new ArrayList<>();
        columnas.add(d.pk);
        columnas.addAll(Arrays.asList(d.campos));
        if (d.tabla.equals("boleta")) columnas.addAll(List.of("cliente", "destino", "dias", "pasajeros"));
        return columnas;
    }

    private void cargar(TableView<Map<String, Object>> tabla, Def d) {
        try {
            Paginador pag = paginadores.get(tabla);
            pag.datos = d.tabla.equals("boleta") ? service.listarBoletas() : service.listar(d.tabla);
            refrescar(tabla);
        } catch (Exception ex) {
            mensaje("No se pudo cargar la información: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refrescar(TableView<Map<String, Object>> tabla) {
        Paginador pag = paginadores.get(tabla);
        List<Map<String, Object>> filtrados = new ArrayList<>();
        for (Map<String, Object> fila : pag.datos) {
            if (pag.filtro.isEmpty() || fila.entrySet().stream().anyMatch(en ->
                    !en.getKey().equals("clave") && String.valueOf(en.getValue()).toLowerCase().contains(pag.filtro)))
                filtrados.add(fila);
        }
        int paginas = Math.max(1, (int) Math.ceil(filtrados.size() / (double) FILAS_POR_PAGINA));
        pag.pagina = Math.max(0, Math.min(pag.pagina, paginas - 1));
        int desde = pag.pagina * FILAS_POR_PAGINA;
        tabla.getItems().setAll(filtrados.subList(desde, Math.min(filtrados.size(), desde + FILAS_POR_PAGINA)));
        pag.info.setText("Página " + (pag.pagina + 1) + " de " + paginas + "  (" + filtrados.size() + " registros)");
        pag.anterior.setDisable(pag.pagina == 0);
        pag.siguiente.setDisable(pag.pagina >= paginas - 1);
    }

    private void formulario(Def d, Map<String, Object> actual, TableView<Map<String, Object>> tabla) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(actual == null ? "Registrar " + d.tabla : "Editar " + d.tabla);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(14));

        Map<String, Control> controles = new LinkedHashMap<>();
        Label[] buscarDniAccion = {null};
        int fila = 0;
        for (String campo : d.campos) {
            Label etiqueta = new Label(etiqueta(campo) + ":");
            try {
                Control control = crearControl(campo, actual, d.tabla);
                controles.put(campo, control);
                grid.add(etiqueta, 0, fila);
                grid.add(control, 1, fila);
                if (d.tabla.equals("cliente") && campo.equals("dni")) {
                    Label estadoDni = new Label();
                    estadoDni.setStyle("-fx-font-size:11px;-fx-text-fill:#666;");
                    grid.add(estadoDni, 2, fila);
                    buscarDniAccion[0] = estadoDni;
                }
                fila++;
            } catch (Exception e) {
                e.printStackTrace();

                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Error");
                alerta.setHeaderText("No se pudo crear el formulario");
                alerta.setContentText(e.getMessage());
                alerta.showAndWait();

                return;
            }
        }
        // En reservas, mostrar el total inmediatamente al cambiar paquete o pasajeros.
        if (d.tabla.equals("reserva")) {
            @SuppressWarnings("unchecked")
            ComboBox<Choice> comboPaquete = (ComboBox<Choice>) controles.get("id_paquete");
            TextField txtPasajeros = (TextField) controles.get("pasajeros");
            TextField txtTotal = (TextField) controles.get("total");

            Label lblDetalle = new Label();
            lblDetalle.setStyle("-fx-font-weight:bold;-fx-text-fill:#1b5e20;");
            grid.add(lblDetalle, 1, fila++);

            Runnable actualizarTotal = () -> {
                try {
                    Choice paquete = comboPaquete.getValue();
                    String cantidadTexto = txtPasajeros.getText().trim();
                    if (paquete == null || cantidadTexto.isEmpty()) {
                        txtTotal.clear();
                        lblDetalle.setText("Elija un paquete e indique los pasajeros.");
                        return;
                    }
                    int cantidad = Integer.parseInt(cantidadTexto);
                    if (cantidad <= 0) {
                        txtTotal.clear();
                        lblDetalle.setText("");
                        return;
                    }
                    long idPaquete = Long.parseLong(paquete.id.toString());
                    double total = service.calcularTotalReserva(idPaquete, cantidad);
                    txtTotal.setText(String.format(java.util.Locale.US, "%.2f", total));
                    lblDetalle.setText(SistemaService.dinero(service.precioPaquete(idPaquete)) + " x " + cantidad
                            + (cantidad == 1 ? " pasajero" : " pasajeros") + " = " + SistemaService.dinero(total));
                } catch (Exception ex) {
                    txtTotal.clear();
                    lblDetalle.setText("");
                }
            };

            comboPaquete.valueProperty().addListener((obs, anterior, nuevo) -> actualizarTotal.run());
            txtPasajeros.textProperty().addListener((obs, anterior, nuevo) -> actualizarTotal.run());
            actualizarTotal.run();
        }

        // Boletas: el total sale de la reserva. Pagos: el monto sugerido es el saldo pendiente.
        if (d.tabla.equals("boleta") || d.tabla.equals("pago")) {
            @SuppressWarnings("unchecked")
            ComboBox<Choice> comboReserva = (ComboBox<Choice>) controles.get("id_reserva");
            Runnable autocompletar = () -> {
                try {
                    Choice r = comboReserva.getValue();
                    if (r == null) return;
                    long idR = Long.parseLong(r.id.toString());
                    if (d.tabla.equals("boleta")) {
                        ((TextField) controles.get("total")).setText(String.format(Locale.US, "%.2f", service.totalReserva(idR)));
                        if (actual == null) ((TextField) controles.get("numero")).setText(
                                "BOL-" + LocalDate.now().toString().replace("-", "") + "-" + idR);
                    } else if (actual == null) {
                        ((TextField) controles.get("monto")).setText(String.format(Locale.US, "%.2f", service.saldoReserva(idR)));
                    }
                } catch (Exception ignorado) { }
            };
            comboReserva.valueProperty().addListener((o, a, b) -> autocompletar.run());
            autocompletar.run();
        }

        if (buscarDniAccion[0] != null) {
            TextField txtDni = (TextField) controles.get("dni");
            TextField txtNombres = (TextField) controles.get("nombres");
            Label estadoDni = buscarDniAccion[0];
            txtDni.setPromptText("8 dígitos: se completa el nombre solo");
            // Al completar 8 dígitos se consulta automáticamente.
            txtDni.textProperty().addListener((obs, anterior, nuevo) -> {
                String dni = nuevo.trim();
                if (dni.matches("\\d{8}")) buscarDni(dni, txtDni, txtNombres, estadoDni);
                else estadoDni.setText("");
            });
        }
        // Mensaje de error dentro del propio formulario: si hay un dato inválido el diálogo
        // NO se cierra y el usuario no pierde lo que escribió.
        Label lblError = new Label();
        lblError.setWrapText(true);
        lblError.setMaxWidth(420);
        lblError.setStyle("-fx-text-fill:#c62828;-fx-font-weight:bold;");
        lblError.setVisible(false);
        lblError.setManaged(false);
        grid.add(lblError, 0, fila, 3, 1);

        dialogo.getDialogPane().setContent(grid);
        Button botonOk = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        botonOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            try {
                guardarFormulario(d, actual, controles);
                lblError.setVisible(false);
                lblError.setManaged(false);
            } catch (Exception ex) {
                lblError.setText("⚠ " + ex.getMessage());
                lblError.setVisible(true);
                lblError.setManaged(true);
                dialogo.getDialogPane().getScene().getWindow().sizeToScene();
                ev.consume(); // evita que se cierre el diálogo
            }
        });

        dialogo.showAndWait().ifPresent(respuesta -> {
            if (respuesta != ButtonType.OK) return;
            cargar(tabla, d);
            mensaje("La información se guardó correctamente.", Alert.AlertType.INFORMATION);
        });
    }

    /** Lee los controles del formulario, convierte los tipos y delega la validación al Service. */
    private void guardarFormulario(Def d, Map<String, Object> actual, Map<String, Control> controles) throws Exception {
        LinkedHashMap<String, Object> datos = new LinkedHashMap<>();
        for (String campo : d.campos) {
            Control control = controles.get(campo);
            Object valor;
            if ((d.tabla.equals("reserva") || d.tabla.equals("boleta")) && campo.equals("total")) {
                continue; // el total se calcula en SistemaService, no se ingresa manualmente
            } else if (control instanceof ComboBox<?> combo) {
                Object seleccionado = combo.getValue();
                if (seleccionado == null) throw new IllegalArgumentException("Seleccione " + etiqueta(campo) + ".");
                valor = seleccionado instanceof Choice choice ? choice.id : seleccionado.toString();
            } else {
                String texto = ((TextField) control).getText().trim();
                try {
                    if (campo.startsWith("id_") || campo.equals("dias") ||
                            campo.equals("cupos") || campo.equals("pasajeros")) {
                        valor = Long.parseLong(texto);
                    } else if (campo.equals("precio") || campo.equals("monto") || campo.equals("total")) {
                        valor = Double.parseDouble(texto);
                    } else if (campo.startsWith("fecha_")) {
                        valor = Date.valueOf(texto);
                    } else {
                        valor = texto;
                    }
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("El campo " + etiqueta(campo) + " debe ser un número válido.");
                } catch (IllegalArgumentException iae) {
                    if (campo.startsWith("fecha_"))
                        throw new IllegalArgumentException("La fecha debe tener el formato AAAA-MM-DD.");
                    throw iae;
                }
            }
            datos.put(campo, valor);
        }
        service.guardar(d.tabla, d.pk, actual == null ? null : actual.get(d.pk), datos);
    }

    private Control crearControl(String campo, Map<String, Object> actual, String tablaActual) throws Exception {
        if (campo.equals("id_cliente")) return comboRelacion("cliente", "id_cliente", "nombres", actual, campo);
        if (campo.equals("id_destino")) return comboRelacion("destino", "id_destino", "nombre", actual, campo);
        if (campo.equals("id_paquete")) return comboRelacion("paquete", "id_paquete", "nombre", actual, campo);
        if (campo.equals("id_reserva")) return comboReservas(actual, campo);

        if (campo.equals("estado")) {
            ComboBox<String> combo = new ComboBox<>();
            if (tablaActual.equals("pago")) combo.getItems().addAll("Pendiente", "Pagado", "Anulado");
            else combo.getItems().addAll("Pendiente", "Confirmada", "Cancelada");
            combo.setValue(actual == null ? combo.getItems().get(0) : String.valueOf(actual.get(campo)));
            combo.setPrefWidth(280);
            return combo;
        }
        if (campo.equals("rol")) {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll("Administrador", "Vendedor");
            combo.setValue(actual == null ? "Vendedor" : String.valueOf(actual.get(campo)));
            combo.setPrefWidth(280);
            return combo;
        }
        if (campo.equals("clave")) {
            PasswordField clave = new PasswordField();
            clave.setPrefWidth(280);
            clave.setPromptText(actual == null ? "Mínimo 6 caracteres" : "Vacío = no cambiar");
            return clave;
        }
        if (campo.equals("metodo")) {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll("Efectivo", "Tarjeta", "Transferencia", "Yape/Plin");
            combo.setValue(actual == null ? "Efectivo" : String.valueOf(actual.get(campo)));
            return combo;
        }

        TextField texto = new TextField();
        texto.setPrefWidth(280);
        texto.setPromptText(etiqueta(campo));
        if (actual != null && actual.get(campo) != null) texto.setText(String.valueOf(actual.get(campo)));
        if (campo.startsWith("fecha_") && actual == null) texto.setText(LocalDate.now().toString());
        if (campo.equals("total") && actual == null) {
            texto.setEditable(false);
            texto.setPromptText("Se calcula automáticamente");
        }
        if (campo.equals("total") && actual != null && actual.containsKey(campo)) {
            texto.setEditable(false);
        }
        if (campo.equals("estado") || campo.equals("metodo")) return texto;
        return texto;
    }

    /** Consulta el DNI en segundo plano (sin congelar la ventana) y rellena el nombre. */
    private void buscarDni(String dni, TextField txtDni, TextField txtNombres, Label estado) {
        estado.setStyle("-fx-font-size:11px;-fx-text-fill:#666;");
        estado.setText("Buscando...");
        Task<PersonaDto> tarea = new Task<>() {
            @Override protected PersonaDto call() throws Exception { return service.buscarPorDni(dni); }
        };
        tarea.setOnSucceeded(e -> {
            // Ignora la respuesta si el usuario ya cambió el DNI mientras se consultaba.
            if (!txtDni.getText().trim().equals(dni)) return;
            txtNombres.setText(tarea.getValue().nombreCompleto());
            estado.setStyle("-fx-font-size:11px;-fx-text-fill:#2e7d32;");
            estado.setText("Datos encontrados");
        });
        tarea.setOnFailed(e -> {
            if (!txtDni.getText().trim().equals(dni)) return;
            estado.setStyle("-fx-font-size:11px;-fx-text-fill:#c62828;");
            estado.setText(tarea.getException().getMessage() + " Escriba el nombre manualmente.");
        });
        Thread hilo = new Thread(tarea, "consulta-dni");
        hilo.setDaemon(true);
        hilo.start();
    }

    private ComboBox<Choice> comboRelacion(String tabla, String pk, String descripcion,
                                            Map<String, Object> actual, String campo) throws Exception {
        ComboBox<Choice> combo = new ComboBox<>();
        for (Map<String, Object> registro : service.listar(tabla)) {
            Object id = registro.get(pk);
            Object nombre = registro.get(descripcion);
            String texto = nombre == null ? String.valueOf(id) : nombre.toString();
            if (tabla.equals("paquete") && registro.get("precio") instanceof Number precio)
                texto += "  (" + SistemaService.dinero(precio.doubleValue()) + " por persona)";
            combo.getItems().add(new Choice(id, texto));
        }
        if (actual != null) {
            Object idActual = actual.get(campo);
            for (Choice opcion : combo.getItems()) {
                if (String.valueOf(opcion.id).equals(String.valueOf(idActual))) {
                    combo.setValue(opcion);
                    break;
                }
            }
        }
        if (combo.getValue() == null && !combo.getItems().isEmpty()) combo.getSelectionModel().selectFirst();
        combo.setPrefWidth(380);
        return combo;
    }

    /** Reservas con texto legible: "3 - Juan Pérez - Machu Picchu Clásico". */
    private ComboBox<Choice> comboReservas(Map<String, Object> actual, String campo) throws Exception {
        ComboBox<Choice> combo = new ComboBox<>();
        for (Map.Entry<Long, String> e : service.etiquetasReserva().entrySet())
            combo.getItems().add(new Choice(e.getKey(), e.getValue()));
        if (actual != null) {
            for (Choice opcion : combo.getItems())
                if (String.valueOf(opcion.id).equals(String.valueOf(actual.get(campo)))) { combo.setValue(opcion); break; }
        }
        if (combo.getValue() == null && !combo.getItems().isEmpty()) combo.getSelectionModel().selectFirst();
        combo.setPrefWidth(280);
        return combo;
    }

    private String formatear(String columna, Object valor) {
        if (valor == null) return "";
        if (columna.equals("dias") && valor instanceof Number n)
            return n.longValue() + (n.longValue() == 1 ? " día" : " días");
        if ((columna.equals("precio") || columna.equals("monto") || columna.equals("total")) && valor instanceof Number n)
            return SistemaService.dinero(n.doubleValue());
        return String.valueOf(valor);
    }

    /** Catálogo de precios para todos los perfiles: buscar, ordenar por columna y calcular el total. */
    private Tab crearTabPrecios() {
        ObservableList<Map<String, Object>> base = FXCollections.observableArrayList();
        FilteredList<Map<String, Object>> filtrada = new FilteredList<>(base, f -> true);
        SortedList<Map<String, Object>> ordenada = new SortedList<>(filtrada);
        TableView<Map<String, Object>> tabla = new TableView<>(ordenada);
        ordenada.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        columnaPrecios(tabla, "PAQUETE", "paquete", false);
        columnaPrecios(tabla, "DESTINO", "destino", false);
        columnaPrecios(tabla, "CIUDAD", "ciudad", false);
        columnaPrecios(tabla, "DÍAS", "dias", false);
        columnaPrecios(tabla, "PRECIO POR PERSONA", "precio", true);
        columnaPrecios(tabla, "CUPOS DISPONIBLES", "disponibles", false);

        Runnable cargarDatos = () -> {
            try {
                base.setAll(service.catalogoPrecios());
            } catch (Exception ex) {
                mensaje("No se pudieron cargar los precios: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        };

        TextField buscar = new TextField();
        buscar.setPromptText("Buscar paquete, destino o ciudad...");
        buscar.setPrefWidth(280);
        buscar.textProperty().addListener((obs, a, b) -> {
            String f = b.trim().toLowerCase();
            filtrada.setPredicate(fila -> f.isEmpty() || List.of("paquete", "destino", "ciudad").stream()
                    .anyMatch(k -> String.valueOf(fila.get(k)).toLowerCase().contains(f)));
        });
        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> cargarDatos.run());

        // Calculadora: precio por persona x pasajeros del paquete seleccionado.
        TextField txtPasajeros = new TextField("1");
        txtPasajeros.setPrefWidth(70);
        Label resultado = new Label("Seleccione un paquete para calcular el total.");
        resultado.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1b5e20;");
        Runnable calcular = () -> {
            Map<String, Object> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila == null) { resultado.setText("Seleccione un paquete para calcular el total."); return; }
            try {
                int n = Integer.parseInt(txtPasajeros.getText().trim());
                if (n <= 0) throw new NumberFormatException();
                double precio = ((Number) fila.get("precio")).doubleValue();
                resultado.setText(fila.get("paquete") + ":  " + SistemaService.dinero(precio) + " x " + n
                        + (n == 1 ? " pasajero" : " pasajeros") + " = " + SistemaService.dinero(precio * n));
            } catch (NumberFormatException ex) {
                resultado.setText("Ingrese una cantidad de pasajeros válida.");
            }
        };
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> calcular.run());
        txtPasajeros.textProperty().addListener((obs, a, b) -> calcular.run());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(8, actualizar, espacio, new Label("Buscar:"), buscar);
        barra.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox calculadora = new HBox(10, new Label("Pasajeros:"), txtPasajeros, resultado);
        calculadora.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        calculadora.setPadding(new Insets(10));
        calculadora.setStyle("-fx-background-color:#eef5ee;-fx-background-radius:6;");

        VBox contenido = new VBox(10, barra, tabla, calculadora);
        contenido.setPadding(new Insets(10));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        cargarDatos.run();
        Tab tab = new Tab("Precios", contenido);
        tab.setClosable(false);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) cargarDatos.run(); });
        return tab;
    }

    private void columnaPrecios(TableView<Map<String, Object>> tabla, String titulo, String clave, boolean dinero) {
        TableColumn<Map<String, Object>, Object> c = new TableColumn<>(titulo);
        c.setCellValueFactory(x -> new SimpleObjectProperty<>(x.getValue().get(clave)));
        c.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else if (dinero && item instanceof Number n) {
                    setText(SistemaService.dinero(n.doubleValue()));
                    setStyle("-fx-alignment:CENTER-RIGHT;-fx-font-weight:bold;-fx-text-fill:#1b5e20;");
                } else setText(item.toString());
            }
        });
        tabla.getColumns().add(c);
    }

    private Tab crearTabReportes() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefRowCount(20);
        Button generar = new Button("Generar reporte");
        generar.setOnAction(e -> {
            try {
                area.setText(service.resumen());
            } catch (Exception ex) {
                mensaje(ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        VBox box = new VBox(10, generar, area);
        box.setPadding(new Insets(12));
        Tab tab = new Tab("Reportes", box);
        tab.setClosable(false);
        return tab;
    }

    private void emitirBoleta(Map<String, Object> reserva, TableView<Map<String, Object>> tablaReservas) {
        try {
            long idReserva = Long.parseLong(reserva.get("id_reserva").toString());
            for (Map<String, Object> boleta : service.listar("boleta")) {
                if (String.valueOf(boleta.get("id_reserva")).equals(String.valueOf(idReserva))) {
                    mensaje("Esta reserva ya tiene una boleta: " + boleta.get("numero"), Alert.AlertType.INFORMATION);
                    return;
                }
            }
            LinkedHashMap<String, Object> datos = new LinkedHashMap<>();
            datos.put("id_reserva", idReserva);
            datos.put("fecha_emision", Date.valueOf(LocalDate.now()));
            datos.put("numero", "BOL-" + LocalDate.now().toString().replace("-", "") + "-" + idReserva);
            datos.put("total", reserva.get("total"));
            service.guardar("boleta", "id_boleta", null, datos);
            mensaje("Boleta emitida correctamente.", Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            mensaje("No se pudo emitir la boleta: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void generarComprobante(Map<String, Object> boletaFila) {
        try {
            Map<String, Object> boleta = service.detalleBoleta(boletaFila.get("id_boleta"));
            if (boleta.isEmpty()) boleta = boletaFila;
            String dias = boleta.get("dias") == null ? "-" : boleta.get("dias") + (String.valueOf(boleta.get("dias")).equals("1") ? " día" : " días");
            String lugar = boleta.get("destino") == null ? "-"
                    : boleta.get("destino") + (boleta.get("ciudad") == null ? "" : " (" + boleta.get("ciudad") + ")");
            String contenido = "BOLETA DE VENTA - SysTurismo\n" +
                    "==================================\n" +
                    "Número:      " + boleta.get("numero") + "\n" +
                    "Fecha:       " + boleta.get("fecha_emision") + "\n" +
                    "Reserva N°:  " + boleta.get("id_reserva") + "\n" +
                    "----------------------------------\n" +
                    "DATOS DEL PASAJERO\n" +
                    "Cliente:     " + valor(boleta.get("cliente")) + "\n" +
                    "DNI:         " + valor(boleta.get("dni")) + "\n" +
                    "----------------------------------\n" +
                    "DETALLE DEL VIAJE\n" +
                    "Paquete:     " + valor(boleta.get("paquete")) + "\n" +
                    "Lugar:       " + lugar + "\n" +
                    "Duración:    " + dias + "\n" +
                    "Pasajeros:   " + valor(boleta.get("pasajeros")) + "\n" +
                    "Fecha reserva: " + valor(boleta.get("fecha_reserva")) + "\n" +
                    "----------------------------------\n" +
                    "TOTAL:       " + SistemaService.dinero(((Number) boleta.get("total")).doubleValue()) + "\n" +
                    "==================================\nGracias por elegirnos.";
            java.nio.file.Path carpeta = pe.edu.upeu.systurismo.config.DatabaseConfig.carpetaDatos().resolveSibling("comprobantes");
            java.nio.file.Files.createDirectories(carpeta);
            java.nio.file.Path archivo = carpeta.resolve("Boleta_" + boleta.get("numero") + ".txt");
            java.nio.file.Files.writeString(archivo, contenido);
            mensaje("Comprobante generado en:\n" + archivo.toAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            mensaje("No se pudo generar el comprobante: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String valor(Object o) {
        return o == null ? "-" : String.valueOf(o);
    }

    private String etiqueta(String texto) {
        if (texto.equals("dias")) return "DÍAS";
        return texto.replace("_", " ").toUpperCase();
    }

    private boolean confirmar(String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void mensaje(String texto, Alert.AlertType tipo) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }

    private static class Paginador {
        List<Map<String, Object>> datos = new ArrayList<>();
        String filtro = "";
        int pagina = 0;
        final Button anterior = new Button("◀ Anterior");
        final Button siguiente = new Button("Siguiente ▶");
        final Label info = new Label();
    }

    private record Def(String tabla, String pk, String[] campos) {}
    private record Choice(Object id, String nombre) {
        @Override public String toString() { return id + " - " + nombre; }
    }
}
