//java
package Pages;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.*;
import java.util.Optional;

public class Customer_Registration {

    private TextField txt_name, txt_lastName, txt_street, txt_hNo, txt_city, txt_zip, txt_contact, txt_email, txt_search;
    private Label message;
    private TableView<Register> tbl_customer;
    private ObservableList<Register> customerList = FXCollections.observableArrayList();
    private Scene scene;

    public Customer_Registration() {
        initComponents();
    }

    private void initComponents() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        // ---------- TOP BAR ----------
        HBox topBar = new HBox(20);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15));

        Button backBtn = new Button("← Home");
        backBtn.getStyleClass().add("button-primary"); // uses existing CSS
        backBtn.setOnAction(e -> {
            SceneManager.showScene(new HomePage().getScene());
        });

        Label title = new Label("Customer Management");
        title.getStyleClass().add("page-title");

        txt_search = createTextField("Search by name or last name...");
        txt_search.setMaxWidth(300);
        txt_search.setMinWidth(200);
        txt_search.textProperty().addListener((obs, oldValue, newValue) ->{
            searchCustomersAsync(newValue);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, title, spacer, txt_search);
        root.setTop(topBar);

        // ---------- LEFT FORM ----------
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("panel");
        formBox.setPadding(new Insets(30));
        formBox.setPrefWidth(350);
        //formBox.setMaxWidth(Double.MAX_VALUE);

        txt_name = createTextField("First Name...");
        txt_lastName = createTextField("Last Name...");
        txt_street = createTextField("Street...");
        txt_hNo = createTextField("House No...");
        txt_city = createTextField("City...");
        txt_zip = createTextField("ZIP Code...");
        txt_contact = createTextField("Contact...");
        txt_email = createTextField("Email...");

        // ZIP Code formatting
        txt_zip.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("\\s+", "");
            if (digits.length() > 9) digits = digits.substring(0, 9);
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && i % 3 == 0) formatted.append(" ");
                formatted.append(digits.charAt(i));
            }
            if (!formatted.toString().equals(newVal)) {
                int caretPos = txt_zip.getCaretPosition();
                txt_zip.setText(formatted.toString());
                txt_zip.positionCaret(Math.min(caretPos, formatted.length()));
            }
        });

        // Contact formatting
        txt_contact.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("\\D", "");
            if (digits.length() > 10) digits = digits.substring(0, 10);
            StringBuilder formatted = new StringBuilder();
            if (digits.length() > 0) formatted.append("(");
            for (int i = 0; i < digits.length(); i++) {
                if (i == 3) formatted.append(")");
                formatted.append(digits.charAt(i));
            }
            if (!formatted.toString().equals(newVal)) {
                int caretPos = txt_contact.getCaretPosition();
                txt_contact.setText(formatted.toString());
                txt_contact.positionCaret(Math.min(caretPos, formatted.length()));
            }
        });

        message = new Label("");
        message.getStyleClass().add("message-label");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Use existing CSS classes: primary, secondary, danger
        Button btn_add = createActionButton("Register", "primary");
        btn_add.setOnAction(e -> handleRegister());

        Button btn_update = createActionButton("Update", "secondary");
        btn_update.setOnAction(e -> handleUpdate());

        Button btn_delete = createActionButton("Delete", "danger");
        btn_delete.setOnAction(e -> handleDelete());

        buttonBox.getChildren().addAll(btn_add, btn_update, btn_delete);

        formBox.getChildren().addAll(
                new Label("First Name"), txt_name,
                new Label("Last Name"), txt_lastName,
                new Label("Street"), txt_street,
                new Label("House No"), txt_hNo,
                new Label("City"), txt_city,
                new Label("ZIP"), txt_zip,
                new Label("Contact"), txt_contact,
                new Label("Email"), txt_email,
                buttonBox, message
        );

        // wrap form in ScrollPane to keep usable at small heights
        ScrollPane formScroll = new ScrollPane(formBox);
        formScroll.setFitToWidth(true);
        formScroll.getStyleClass().add("panel");
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setMaxWidth(Double.MAX_VALUE);

        // right table
        tbl_customer = new TableView<>();
        tbl_customer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Register, Integer> idCol = new TableColumn<>("CID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(40);

        TableColumn<Register, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(100);

        TableColumn<Register, String> lastCol = new TableColumn<>("Last Name");
        lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastCol.setMinWidth(100);

        TableColumn<Register, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAddress()));
        addressCol.setMinWidth(200);

        TableColumn<Register, String> emailCol = new TableColumn<>("E-Mail");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setMinWidth(170);

        TableColumn<Register, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setMinWidth(60);

        tbl_customer.getColumns().addAll(idCol, nameCol, lastCol, addressCol, emailCol, contactCol);

        tbl_customer.setOnMouseClicked(e -> {
            Register selected = tbl_customer.getSelectionModel().getSelectedItem();
            if (selected != null) {
                txt_name.setText(selected.getName());
                txt_lastName.setText(selected.getLastName());
                txt_street.setText(selected.getStreet());
                txt_hNo.setText(selected.getHouseNumber());
                txt_city.setText(selected.getCity());
                txt_zip.setText(selected.getZip());
                txt_contact.setText(selected.getContact());
                txt_email.setText(selected.getEmail());
            }
        });

        // Main content
        HBox content = new HBox(20, formScroll, tbl_customer);
        content.setPadding(new Insets(20));
        content.setFillHeight(true);

        // allow both sides to grow
        HBox.setHgrow(formScroll, Priority.ALWAYS);
        HBox.setHgrow(tbl_customer, Priority.ALWAYS);
        VBox.setVgrow(tbl_customer, Priority.ALWAYS);

        root.setCenter(content);

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());




        // Responsive bindings: form and table scale with scene
        formScroll.prefWidthProperty().bind(scene.widthProperty().multiply(0.28));
        tbl_customer.prefWidthProperty().bind(scene.widthProperty().multiply(0.68));
        tbl_customer.prefHeightProperty().bind(scene.heightProperty().subtract(160)); // reserve space for top/padding

        loadCustomerDetails();
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("input-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tf, Priority.ALWAYS);
        return tf;
    }

    private Button createActionButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add("button-" + styleClass); // matches styles.css
        return btn;
    }

    private void handleRegister() {
        if (txt_name.getText().isEmpty() || txt_lastName.getText().isEmpty()) {
            showError("Invalid Entry", "Please fill all required fields.");
        } else {
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() { return registerCustomer(); }
            };
            task.setOnSucceeded(e -> {
                if (task.getValue()) {
                    refreshTable();
                    message.setText("Customer Registered!");
                    clearFields();
                    autoClearMessage();
                } else showError("Error", "Registration failed.");
            });
            new Thread(task).start();
        }
    }

    private void handleUpdate() {
        Register selected = tbl_customer.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Update this record?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() { return update(); }
                };
                task.setOnSucceeded(e -> {
                    if (task.getValue()) {
                        refreshTable();
                        message.setText("Customer Updated!");
                        clearFields();
                        autoClearMessage();
                    } else showError("Error", "Update failed.");
                });
                new Thread(task).start();
            }
        }
    }

    private void handleDelete() {
        Register selected = tbl_customer.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this customer?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() { return delete(); }
                };
                task.setOnSucceeded(e -> {
                    if (task.getValue()) {
                        refreshTable();
                        clearFields();
                        message.setText("Customer Deleted!");
                        autoClearMessage();
                    }
                });
                new Thread(task).start();
            }
        }
    }

    private void autoClearMessage() {
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> message.setText(""));
        pause.play();
    }

    private void clearFields() {
        txt_name.clear();
        txt_lastName.clear();
        txt_street.clear();
        txt_hNo.clear();
        txt_city.clear();
        txt_zip.clear();
        txt_contact.clear();
        txt_email.clear();
    }

    private void refreshTable() {
        loadCustomerDetails();
    }

    public void loadCustomerDetails() {
        Task<ObservableList<Register>> task = new Task<>() {
            @Override
            protected ObservableList<Register> call() {
                ObservableList<Register> list = FXCollections.observableArrayList();
                String query = "SELECT * FROM customer_details";
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    while (rs.next()) {
                        list.add(new Register(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("last_name"),
                                rs.getString("street"),
                                rs.getString("house_number"),
                                rs.getString("city"),
                                rs.getString("zip"),
                                rs.getString("contact"),
                                rs.getString("email")
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return list;
            }
        };

        task.setOnSucceeded(e -> tbl_customer.setItems(task.getValue()));
        new Thread(task).start();
    }

    public boolean registerCustomer() {
        boolean isAdded = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO customer_details (name, last_name, street, house_number, city, zip, contact, email) VALUES (?,?,?,?,?,?,?,?)")) {

            pst.setString(1, txt_name.getText());
            pst.setString(2, txt_lastName.getText());
            pst.setString(3, txt_street.getText());
            pst.setString(4, txt_hNo.getText());
            pst.setString(5, txt_city.getText());
            pst.setString(6, txt_zip.getText());
            pst.setString(7, txt_contact.getText());
            pst.setString(8, txt_email.getText());

            isAdded = pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAdded;
    }

    public boolean update() {
        Register selected = tbl_customer.getSelectionModel().getSelectedItem();
        if (selected == null) return false;

        boolean isUpdated = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "UPDATE customer_details SET name=?, last_name=?, street=?, house_number=?, city=?, zip=?, contact=?, email=? WHERE id=?")) {

            pst.setString(1, txt_name.getText());
            pst.setString(2, txt_lastName.getText());
            pst.setString(3, txt_street.getText());
            pst.setString(4, txt_hNo.getText());
            pst.setString(5, txt_city.getText());
            pst.setString(6, txt_zip.getText());
            pst.setString(7, txt_contact.getText());
            pst.setString(8, txt_email.getText());
            pst.setInt(9, selected.getId());

            isUpdated = pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isUpdated;
    }

    public boolean delete() {
        Register selected = tbl_customer.getSelectionModel().getSelectedItem();
        if (selected == null) return false;

        boolean isDeleted = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("DELETE FROM customer_details WHERE id=?")) {
            pst.setInt(1, selected.getId());
            isDeleted = pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isDeleted;
    }

    // search functionality can be implemented here, e.g. by adding a listener to a searchBar and filtering the customerList based on the input
    private void searchCustomersAsync(String keyword){
        Task<ObservableList<Register>> task = new Task<>(){
            @Override
            protected ObservableList<Register> call() throws Exception{
                ObservableList<Register> list = FXCollections.observableArrayList();
                String sql = "SELECT * FROM customer_details WHERE name LIKE ? OR last_name LIKE ?";

                try(Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)){

                    pst.setString(1,"%" + keyword + "%");
                    pst.setString(2,"%" + keyword + "%");

                    ResultSet rs = pst.executeQuery();
                    while(rs.next()){
                        list.add(new Register(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("last_name"),
                                rs.getString("street"),
                                rs.getString("house_number"),
                                rs.getString("city"),
                                rs.getString("zip"),
                                rs.getString("contact"),
                                rs.getString("email")
                        ));
                    }

                }catch(SQLException e){
                    e.printStackTrace();
                }
                return list;
            }
        };
        task.setOnSucceeded((e -> tbl_customer.setItems(task.getValue())));
        new Thread(task).start();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }


    public Scene getScene() {
        return scene;
    }
}
