package Pages;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;


import java.math.BigDecimal;
import java.sql.*;

public class Manage_Materials {

    private Scene scene;

    private TextField txt_name, txt_type, txt_price, txt_quantity, txt_search;
    private TableView<Material> table;
    private ObservableList<Material> materialList = FXCollections.observableArrayList();
    private Label message;

    public Manage_Materials() {
        initializeComponents();
    }

    private void initializeComponents() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        // ---------- TOP BAR ----------
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("navbar");

        Label title = new Label("Manage Materials");
        title.getStyleClass().add("navbar-title");

        Button btnHome = new Button("← Home");
        btnHome.getStyleClass().add("button-primary");
        btnHome.setOnAction(e -> {
            SceneManager.showScene(new HomePage().getScene());
        });

        txt_search = createTextField("Search by name or type...");
        txt_search.setMaxWidth(300);
        txt_search.setMinWidth(200);
        txt_search.textProperty().addListener((observable, oldvalue, newValue) ->{
            searchMaterialsAsync(newValue);
        });


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(btnHome, title, spacer, txt_search);


        // ---------- LEFT FORM ----------
        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setPrefWidth(350);
        form.getStyleClass().add("panel");
        VBox.setVgrow(form, Priority.ALWAYS);

        txt_name = createTextField("Material Name");
        txt_type = createTextField("Type");
        txt_quantity = createTextField("Quantity");
        txt_price = createTextField("Unit Price $");

        HBox buttonBar = new HBox(10);
        Button btnAdd = new Button("Add");
        Button btnUpdate = new Button("Update");
        Button btnDelete = new Button("Delete");

        btnAdd.getStyleClass().add("button-primary");
        btnUpdate.getStyleClass().add("button-secondary");
        btnDelete.getStyleClass().add("button-danger"); // You can add .button-danger to CSS

        buttonBar.getChildren().addAll(btnAdd, btnUpdate, btnDelete);

        message = new Label();
        message.getStyleClass().add("label-value");

        form.getChildren().addAll(txt_name, txt_type, txt_quantity, txt_price, buttonBar, message);

        // ---------- RIGHT TABLE ----------
        table = new TableView<>();
        table.setPrefWidth(600);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Material, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Material, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Material, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Material, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Material, BigDecimal> priceCol = new TableColumn<>("Price $");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatAsCurrency(priceCol);

        table.getColumns().addAll(idCol, nameCol, typeCol, quantityCol, priceCol);

        loadMaterialsAsync();

        // ---------- EVENTS ----------
        table.setOnMouseClicked(e -> {
            Material selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                txt_name.setText(selected.getName());
                txt_type.setText(selected.getType());
                txt_quantity.setText(String.valueOf(selected.getQuantity()));
                txt_price.setText(String.valueOf(selected.getPrice()));
            }
        });

        btnAdd.setOnAction(e -> addMaterialAsync());
        btnUpdate.setOnAction(e -> updateMaterialAsync());
        btnDelete.setOnAction(e -> deleteMaterialAsync());

        // ---------- LAYOUT ----------

        HBox content = new HBox(20, form, table);
        content.setPadding(new Insets(20));
        content.setFillHeight(true);

        HBox.setHgrow(form, Priority.ALWAYS);
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);



        root.setTop(topBar);
       root.setCenter(content);

        scene = new Scene(root);

        // Responsive bindings
        form.prefWidthProperty().bind(scene.widthProperty().multiply(0.25));
        table.prefWidthProperty().bind(scene.widthProperty().subtract(form.prefWidthProperty()).subtract(40));
        table.prefHeightProperty().bind(scene.heightProperty().subtract(topBar.heightProperty()).subtract(30));


        // Apply CSS
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.getStyleClass().add("input-field");
        tf.setPromptText(prompt);
        return tf;
    }

    private void clearForm() {
        txt_name.clear();
        txt_type.clear();
        txt_quantity.clear();
        txt_price.clear();
    }

    private void showMessage(String msg) {
        message.setText(msg);
        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3))
                .setOnFinished(e -> message.setText(""));
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ---------- Database Operations (Async) ----------
    private void loadMaterialsAsync() {
        Task<ObservableList<Material>> task = new Task<>() {
            @Override
            protected ObservableList<Material> call() {
                ObservableList<Material> list = FXCollections.observableArrayList();
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM material")) {
                    while (rs.next()) {
                        list.add(new Material(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("type"),
                                rs.getInt("quantity"),
                                rs.getBigDecimal("price")
                        ));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }
        };
        task.setOnSucceeded(e -> table.setItems(task.getValue()));
        new Thread(task).start();
    }

    private void searchMaterialsAsync(String keyword){
        Task<ObservableList<Material>> task = new Task<>(){
            @Override
          protected ObservableList<Material> call() throws Exception {
                ObservableList<Material> list = FXCollections.observableArrayList();
                String sql = "SELECT * FROM material WHERE name LIKE ? OR type LIKE ?";

                try(Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)){
                    ps.setString(1, "%" + keyword + "%");
                    ps.setString(2, "%" + keyword + "%");

                    ResultSet rs = ps.executeQuery();
                    while(rs.next()){
                        list.add(new Material(
                           rs.getInt("id"),
                           rs.getString("name"),
                           rs.getString("type"),
                           rs.getInt("quantity"),
                           rs.getBigDecimal("price")
                        ));
                    }
                }catch(SQLException e){
                    e.printStackTrace();
                }
                return list;
            }
        };
        task.setOnSucceeded(e -> table.setItems(task.getValue()));
        new Thread(task).start();
    }


    // ---------- Add Material ----------
    private void addMaterialAsync() {
        String name = txt_name.getText().trim();
        String type = txt_type.getText().trim();
        String quantityStr = txt_quantity.getText().trim();
        String priceStr = txt_price.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name and Price fields are required");
            return;
        }

        BigDecimal price;
        int quantity;
        try {
            price = new BigDecimal(priceStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price and quantity must be a number");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO material (name, type, quantity, price) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, name);
                    ps.setString(2, type);
                    ps.setInt(3,quantity);
                    ps.setBigDecimal(4, price);
                    ps.executeUpdate();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showMessage("Material added successfully!");
            clearForm();
            loadMaterialsAsync();
        });

        task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Database Error", task.getException().getMessage()));

        new Thread(task).start();
    }

    // ---------- Update Material ----------
    private void updateMaterialAsync() {
        Material selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "No material selected");
            return;
        }

        String name = txt_name.getText().trim();
        String type = txt_type.getText().trim();
        String quantityStr = txt_quantity.getText().trim();
        String priceStr = txt_price.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name and Price are required");
            return;
        }

        BigDecimal price;
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price and quantity must be a number");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE material SET name=?, type=?, quantity=?, price=? WHERE id=?")) {
                    ps.setString(1, name);
                    ps.setString(2, type);
                    ps.setInt(3, quantity);
                    ps.setBigDecimal(4, price);
                    ps.setInt(5, selected.getId());
                    ps.executeUpdate();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showMessage("Material updated successfully!");
            clearForm();
            loadMaterialsAsync();
        });

        task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Database Error", task.getException().getMessage()));

        new Thread(task).start();
    }

    // ---------- Delete Material ----------
    private void deleteMaterialAsync() {
        Material selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "No material selected");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this material?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM material WHERE id=?")) {
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showMessage("Material deleted successfully!");
            clearForm();
            loadMaterialsAsync();
        });

        task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Database Error", task.getException().getMessage()));

        new Thread(task).start();
    }

    private void formatAsCurrency(TableColumn<Material, BigDecimal> column){
        column.setCellFactory(col -> new TableCell<Material, BigDecimal>(){
            @Override
            protected void updateItem(BigDecimal value, boolean empty){
                super.updateItem(value, empty);
                if(empty || value == null){
                    setText(null);
                }else{
                    setText("$" + value.setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
        });
    }

    public Scene getScene() {
        return scene;
    }

}
