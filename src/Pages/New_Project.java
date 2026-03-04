package Pages;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.animation.PauseTransition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.sql.Date;
import java.util.Optional;

public class New_Project {

    private Scene scene;

    private TextField txt_location, txt_grossPay, txt_payment, txt_projectName, txt_search;
    private DatePicker dt_startDate, dt_endDate;
    private ComboBox<Item> list_material;
    private ComboBox<Register> cmb_customer;
    private ComboBox<String> cmb_projectType;               // replaced txt_cid
    private Label lbl_materialCost, message;
    private TableView<Project> tbl_project;
    private ListView<SelectedItem> selectedListView;
    private Project selectedProjectForUpdate = null;

    private Button btnUpdate;
    private ObservableList<Project> projectList = FXCollections.observableArrayList();
    private ObservableList<Item> items = FXCollections.observableArrayList();
    private ObservableList<SelectedItem> selectedItems = FXCollections.observableArrayList();
    private ObservableList<Register> customers = FXCollections.observableArrayList(); // customer list
    private BigDecimal grossPay = BigDecimal.ZERO;
    private boolean loadingData = false;

    public New_Project() {
        initializeComponents();
        clearForm();
    }

    private void initializeComponents() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-pane");

        // Header
        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        Button btn_home = new Button("← Home");
        btn_home.getStyleClass().add("button-primary");
        btn_home.setOnAction(e -> {
            SceneManager.showScene(new HomePage().getScene());
        });
        txt_search = createTextField("Search Projects by name, c.name, type...");
        txt_search.setMaxWidth(400);
        txt_search.setMinWidth(300);
        txt_search.textProperty().addListener((obs, oldValue, newValue) ->{
            searchProjectsAsync(newValue);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label title = new Label("Record New Project");
        title.getStyleClass().add("page-title");
        header.getChildren().addAll(btn_home, title, spacer, txt_search);
        header.setAlignment(Pos.CENTER_LEFT);
        root.setTop(header);





        // Left form
        VBox form = new VBox(15);
        form.getStyleClass().add("panel");
        form.setPadding(new Insets(30));
        form.setMaxWidth(350);
        VBox.setVgrow(form, Priority.ALWAYS);

        txt_projectName = createTextField("Enter Project Name");
        // replace Customer ID text field with ComboBox of customers
        cmb_customer = new ComboBox<>(customers);
        cmb_customer.setPromptText("Select Customer");
        cmb_customer.getStyleClass().add("input-field");
        cmb_customer.setMaxWidth(Double.MAX_VALUE);

        cmb_customer.setConverter(new StringConverter<Register>(){
            @Override
            public String toString(Register customer){
                if(customer == null) return null;
                return customer.getName() + " " + customer.getLastName();
            }

            @Override
            public Register fromString(String string){
                return null;
            }
        });

        cmb_projectType = new ComboBox<>();
        cmb_projectType.getItems().addAll(
                "Residential",
                "Commercial",
                "Industrial"
        );
        cmb_projectType.setPromptText("Select Project Type");
        cmb_projectType.getStyleClass().add("input-field");
        cmb_projectType.setMaxWidth(Double.MAX_VALUE);

        txt_location = createTextField("Project Location");
        dt_startDate = new DatePicker();
        dt_endDate = new DatePicker();
        dt_startDate.getStyleClass().add("input-field");
        dt_endDate.getStyleClass().add("input-field");
        dt_startDate.setMaxWidth(Double.MAX_VALUE);
        dt_endDate.setMaxWidth(Double.MAX_VALUE);
        txt_grossPay= createTextField("Gross Pay $");
        txt_grossPay.setEditable(false);
        txt_grossPay.setFocusTraversable(false);

        selectedItems.addListener((ListChangeListener<SelectedItem>) c ->{
            if(!loadingData){
                updateGrossPay();
            }
        });

        txt_payment = createTextField("Payment $");
        txt_payment.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!loadingData){
                updateGrossPay();
            }
        });

        list_material = new ComboBox<>(items);
        list_material.getStyleClass().add("input-field");
        list_material.setPromptText("Select Material");
        list_material.setOnAction(e -> selectMaterial());
        selectedListView = new ListView<>(selectedItems);
        selectedListView.getStyleClass().add("selected-items-list-view");
        selectedListView.setPrefHeight(150);
        selectedListView.setMinHeight(100);

        selectedListView.setCellFactory(list -> new ListCell<>(){
            protected void updateItem(SelectedItem item, boolean empty){
                super.updateItem(item, empty);

                if(empty || item == null){
                    setGraphic(null);
                }else{
                    int number = getIndex() + 1;

                    Label info = new Label(
                            number + ". " + item.getItem().getName() +
                                    " x" + item.getQuantity() +
                                    " = $" + item.getTotalPrice()
                    );
                    Button removeBtn = new Button("❌");
                    removeBtn.getStyleClass().add("selected-remove-btn");
                    removeBtn.setOnAction(e ->{
                        selectedItems.remove(item);
                        updateSelectedLabel();
                    });

                    HBox row = new HBox(10, removeBtn, info);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(row);
                }
            }

        });


        lbl_materialCost = new Label("Total Material Cost: $0.00");


        message = new Label();
        message.getStyleClass().add("success-message");

        Button btn_add = new Button("Confirm");
        btn_add.getStyleClass().add("button-success");
        btn_add.setOnAction(e -> addProjectAction());

        btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().add("button-primary");
        btnUpdate.setDisable(true);
        btnUpdate.setOnAction(e -> handleUpdate());



        form.getChildren().addAll(
                txt_projectName, cmb_customer, cmb_projectType, txt_location,
                new Label("Start Date:"), dt_startDate,
                new Label("End Date:"), dt_endDate,
                new Label("Payement"),txt_payment,
                new Label("Gross Pay"),txt_grossPay,
                list_material, selectedListView, lbl_materialCost, btn_add, btnUpdate, message
        );


        form.setMaxWidth(Double.MAX_VALUE);

        // wrap form in a ScrollPane so it remains usable on small heights
        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setFitToHeight(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setMaxWidth(Double.MAX_VALUE);
        formScroll.getStyleClass().add("panel");

        root.setLeft(formScroll);

        // Right table
        tbl_project = new TableView<>();
        tbl_project.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupProjectTable();
        loadProjectTableAsync();

        //Main content

        HBox content = new HBox(20, formScroll, tbl_project);
        content.setPadding(new Insets(20));
        content.setFillHeight(true);

        HBox.setHgrow(formScroll, Priority.ALWAYS);
        HBox.setHgrow(tbl_project, Priority.ALWAYS);
        VBox.setVgrow(tbl_project, Priority.ALWAYS);

        root.setCenter(content);

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());



        // responsive bindings so the form and table scale with the scene
        formScroll.prefWidthProperty().bind(scene.widthProperty().multiply(0.35));
        tbl_project.prefWidthProperty().bind(scene.widthProperty().multiply(0.63));
        tbl_project.prefHeightProperty().bind(scene.heightProperty().subtract(140)); // reserve space for header/padding

        tbl_project.setItems(projectList);
        loadMaterialToListViewAsync();
        loadCustomersAsync(); // loads customers into the combo box
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("input-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tf, Priority.ALWAYS);
        return tf;
    }

    private void selectMaterial(){
        Item selected = list_material.getSelectionModel().getSelectedItem();
        if(selected == null) return;


        int availableQty = selected.getQuantity(); //stock from DB

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Material Quantity");
        dialog.setHeaderText("Enter Quantity for " + selected.getName() + "\nAvailable stock: " + availableQty);

        Optional<String>  result = dialog.showAndWait();

        result.ifPresent(q ->{
            try{
                int quantity = Integer.parseInt(q);

                if(quantity <= 0){
                    showAlert("Invalid Quantity", "Quantity must be greater than zero");
                    return;
                }

                if(quantity > availableQty){
                    showAlert("Stock Limit Exceeded", "only " + availableQty + " units available for " + selected.getName());
                    return;
                }

                // prevent duplicate material entry
                for(SelectedItem si: selectedItems){
                    if(si.getItem().getId() == selected.getId()){
                        showAlert("Duplicate Material", "This material is already added to the project.");
                        return;
                    }
                }

                selectedItems.add(new SelectedItem(selected, quantity));
                updateSelectedLabel();

            }catch(NumberFormatException ex){
                showAlert("Invalid Input", "Please enter a valid number");
            }
        });

        Platform.runLater(()->
                list_material.getSelectionModel().clearSelection()
                );
    }


    private void updateGrossPay(){

        BigDecimal totalMaterial =  BigDecimal.ZERO;

        for(SelectedItem sel : selectedItems){
            totalMaterial = totalMaterial.add(sel.getTotalPrice());
        }

        BigDecimal payment = BigDecimal.ZERO;

        try{
            if(!txt_payment.getText().isEmpty()){
                payment = new BigDecimal(txt_payment.getText());
            }
        }catch(NumberFormatException e){
            txt_grossPay.setText("0.00");
            return;
        }
        grossPay = payment.subtract(totalMaterial);
       // BigDecimal grossPay = payment.subtract(totalMaterial);
        txt_grossPay.setText(grossPay.setScale(2, RoundingMode.HALF_UP).toString());
    }

    private void updateSelectedLabel(){

        BigDecimal total = BigDecimal.ZERO;

        for(SelectedItem sel : selectedItems){
            total = total.add(sel.getTotalPrice());
        }
        lbl_materialCost.setText("Total Material Cost: $" + total.setScale(2, BigDecimal.ROUND_HALF_UP));
        updateGrossPay();
    }

    private void addProjectAction() {
        if (txt_projectName.getText().isEmpty() || cmb_customer.getSelectionModel().getSelectedItem() == null ) {
            showAlert("Error", "Please fill project name and select a customer.");
            return;
        }

        LocalDate startDate = dt_startDate.getValue();
        LocalDate endDate = dt_endDate.getValue();
        if(startDate == null || endDate == null){
            showAlert("Error", "Start and End Dates cannot be empty");
            return;
        }

        BigDecimal actualCost = BigDecimal.ZERO;
        BigDecimal payment = BigDecimal.ZERO;

        try{
            if(!txt_payment.getText().isEmpty()){
                payment = new BigDecimal(txt_payment.getText());
            }

        }catch(NumberFormatException e){
            showAlert("Invalid Input", "Actual cost and Payment must be a valid numbers.");
            return;
        }

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                int projectId = addProject();
                if (projectId > 0) addProjectMaterial(projectId, selectedItems);
                return projectId;
            }
        };

        task.setOnSucceeded(e -> {
            int projectId = task.getValue();
            if (projectId > 0) {
                loadProjectTableAsync();
                clearForm();
                message.setText("Project Added Successfully!");
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(ev -> message.setText(""));
                pause.play();
            } else showAlert("Error", "Failed to add project.");
        });

        new Thread(task).start();
    }


    private void clearForm() {
        txt_projectName.clear();
        cmb_customer.getSelectionModel().clearSelection();
        cmb_projectType.getSelectionModel().clearSelection();
        txt_location.clear();
        txt_grossPay.setText("0");
        txt_payment.setText("0");
        selectedItems.clear();
        updateSelectedLabel();
        dt_startDate.setValue(null);
        dt_endDate.setValue(null);
        selectedProjectForUpdate = null;
        btnUpdate.setDisable(true);
    }

    private void setupProjectTable() {
        tbl_project.getColumns().clear();

        TableColumn<Project, Integer> pidCol = new TableColumn<>("PID");
        pidCol.setCellValueFactory(new PropertyValueFactory<>("pid"));
        pidCol.setPrefWidth(20);

        TableColumn<Project, String> pnameCol = new TableColumn<>("Project Name");
        pnameCol.setCellValueFactory(new PropertyValueFactory<>("pname"));
        pnameCol.setPrefWidth(100);

        TableColumn<Project, Integer> cidCol = new TableColumn<>("CID");
        cidCol.setCellValueFactory(new PropertyValueFactory<>("cid"));
        cidCol.setPrefWidth(20);

        TableColumn<Project, String> cNameCol = new TableColumn<>("Customer Name");
        cNameCol.setCellValueFactory(new PropertyValueFactory<>("cname"));
        cNameCol.setPrefWidth(120);

        TableColumn<Project, String> ptypeCol = new TableColumn<>("Project Type");
        ptypeCol.setCellValueFactory(new PropertyValueFactory<>("ptype"));
        ptypeCol.setPrefWidth(80);

        TableColumn<Project, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(100);

        TableColumn<Project, Date> sDateCol = new TableColumn<>("Start Date");
        sDateCol.setCellValueFactory(new PropertyValueFactory<>("start_date"));
        sDateCol.setPrefWidth(100);

        TableColumn<Project, Date> eDateCol = new TableColumn<>("End Date");
        eDateCol.setCellValueFactory(new PropertyValueFactory<>("end_date"));
        eDateCol.setPrefWidth(100);

        TableColumn<Project, BigDecimal> paymentCol = new TableColumn<>("Payment $");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("payment"));
        formatAsCurrency(paymentCol);
        paymentCol.setPrefWidth(100);

        TableColumn<Project, BigDecimal> grossPayCol = new TableColumn<>("Gross Pay $");
        grossPayCol.setCellValueFactory(new PropertyValueFactory<>("grossPay"));
        formatAsCurrency(grossPayCol);
        grossPayCol.setPrefWidth(100);



        tbl_project.getColumns().addAll(pidCol, pnameCol, cidCol, cNameCol, ptypeCol, locationCol,
                sDateCol, eDateCol, paymentCol, grossPayCol);

        tbl_project.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();
            MenuItem delete = new MenuItem("Delete");
            delete.setOnAction(e -> {
                Project selected = row.getItem();
                if(selected == null) return;

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Delete");
                alert.setHeaderText("Delete Project " + selected.getPname());
                alert.setContentText("Are you sure you want to delete this project?");

                Optional<ButtonType> result = alert.showAndWait();

                if(result.isPresent() && result.get() == ButtonType.OK){
                    deleteProjectAsync(selected.getPid());
                }
            });
            menu.getItems().add(delete);

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {

                    if(event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY){
                        selectedProjectForUpdate = row.getItem();
                        loadProjectToForm(selectedProjectForUpdate);
                        btnUpdate.setDisable(false);
                    }

                    if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                        Project_Details details = new Project_Details(row.getItem().getPid());
                        SceneManager.showScene(details.getScene());
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        menu.show(row, event.getScreenX(), event.getScreenY());
                    }
                }
            });
            return row;
        });
    }

    private void loadMaterialToListViewAsync() {
        Task<ObservableList<Item>> task = new Task<>() {
            @Override
            protected ObservableList<Item> call() throws Exception {
                ObservableList<Item> matItems = FXCollections.observableArrayList();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pst = conn.prepareStatement("SELECT id, name, price, quantity FROM material");
                     ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        matItems.add(new Item(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price"), rs.getInt("quantity")));
                    }
                }
                return matItems;
            }
        };

        task.setOnSucceeded(e -> items.setAll(task.getValue()));
        new Thread(task).start();
    }

    // new: load customers into the combo box asynchronously
    private void loadCustomersAsync() {
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

        task.setOnSucceeded(e ->{
            customers.setAll(task.getValue());
        });
        new Thread(task).start();
    }

    private void loadProjectTableAsync() {
        Task<ObservableList<Project>> task = new Task<>() {
            @Override
            protected ObservableList<Project> call() throws Exception {
                ObservableList<Project> projects = FXCollections.observableArrayList();
                String query = """
                        SELECT p.project_id,
                        p.project_name,
                        p.customer_id,
                        COALESCE(c.name, 'Deleted Customer') AS customer_name,
                        p.project_type,
                        p.project_location,
                        p.start_date,
                        p.end_date,
                        p.payment,
                        p.actual_cost
                        FROM project p
                        LEFT JOIN customer_details c ON p.customer_id = c.id
                        """;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pst = conn.prepareStatement(query);
                     ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        projects.add(new Project(
                                rs.getInt("project_id"),
                                rs.getString("project_name"),
                                rs.getInt("customer_id"),
                                rs.getString("customer_name"),
                                rs.getString("project_type"),
                                rs.getString("project_location"),
                                rs.getDate("start_date"),
                                rs.getDate("end_date"),
                                rs.getBigDecimal("payment"),
                                rs.getBigDecimal("actual_cost")
                        ));
                    }
                   // System.out.println("Loaded projects: " + projects.size());
                }
                return projects;
            }
        };

        task.setOnSucceeded(e ->{
                projectList.setAll(task.getValue());
                tbl_project.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    //search projects by project name, customer name, or project type
    private void searchProjectsAsync(String keyword){
        Task<ObservableList<Project>> task = new Task<>(){
            @Override
            protected ObservableList<Project> call() throws Exception{
                ObservableList<Project> list = FXCollections.observableArrayList();
                String sql = """
                        SELECT p.project_id,
                        p.project_name,
                        p.customer_id,
                        COALESCE(c.name, 'Deleted Customer') AS customer_name,
                        p.project_type,
                        p.project_location,
                        p.start_date,
                        p.end_date,
                        p.payment,
                        p.actual_cost
                        FROM project p
                        LEFT JOIN customer_details c ON p.customer_id = c.id
                        WHERE p.project_name LIKE ? OR c.name LIKE ? OR p.project_type LIKE ?
                        """;
                try(Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)){
                    pst.setString(1, "%" + keyword + "%");
                    pst.setString(2, "%" + keyword + "%");
                    pst.setString(3, "%" + keyword + "%");

                    ResultSet rs = pst.executeQuery();
                    while(rs.next()){
                        list.add(new Project(
                                rs.getInt("project_id"),
                                rs.getString("project_name"),
                                rs.getInt("customer_id"),
                                rs.getString("customer_name"),
                                rs.getString("project_type"),
                                rs.getString("project_location"),
                                rs.getDate("start_date"),
                                rs.getDate("end_date"),
                                rs.getBigDecimal("payment"),
                                rs.getBigDecimal("actual_cost")
                        ));
                    }
                }catch(SQLException e){
                    e.printStackTrace();
                }
                return list;
            }
        };
        task.setOnSucceeded(e-> tbl_project.setItems(task.getValue()));
        new Thread(task).start();
    }

    // search customers by name or ID for the combo box (Register class contains customer details)
    private void searchCustomersAsync(String keyword){
        Task<ObservableList<Register>> task = new Task<>(){
            @Override
            protected ObservableList<Register> call() throws Exception{
                ObservableList<Register> list = FXCollections.observableArrayList();
                String sql = "SELECT id, name, last_name FROM customer_details WHERE name LIKE ? or last_name LIKE ? or CAST(id AS CHAR) LIKE ?";

                try(Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)){
                    pst.setString(1, "%" + keyword + "%");
                    pst.setString(2, "%" + keyword + "%");
                    pst.setString(3, "%" + keyword + "%");

                    ResultSet rs = pst.executeQuery();
                    while(rs.next()){
                        list.add(new Register(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("last_name"),
                                null, null, null, null,null, null
                        ));
                    }
                }catch(SQLException e){
                    e.printStackTrace();
                }
                return list;
            }
        };
        task.setOnSucceeded(e-> cmb_customer.setItems(task.getValue()));
        new Thread(task).start();
    }

    private void deleteProjectAsync(int id) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return deleteProject(id);
            }
        };
        task.setOnSucceeded(e -> loadProjectTableAsync());
        new Thread(task).start();
    }

    private int addProject() {
        int projectId = 0;
        Register selectedCustomer = cmb_customer.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) return 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO project (project_name, customer_id, project_type, project_location, start_date, end_date, actual_cost, payment) VALUES (?,?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, txt_projectName.getText());
            pst.setInt(2, selectedCustomer.getId()); // use selected customer's id
            pst.setString(3, cmb_projectType.getValue());
            pst.setString(4, txt_location.getText());
            pst.setDate(5, Date.valueOf(dt_startDate.getValue()));
            pst.setDate(6, Date.valueOf(dt_endDate.getValue()));
            pst.setBigDecimal(7, new BigDecimal(txt_grossPay.getText()));
            pst.setBigDecimal(8, new BigDecimal(txt_payment.getText()));
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) projectId = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return projectId;
    }

    // load project names

    private void addProjectMaterial(int projectId, List<SelectedItem> selectedItems){

        if(selectedItems.isEmpty()) return;

        String checkStockSql = "SELECT quantity FROM material WHERE id = ?";
        String updateStockSql = "UPDATE material SET quantity = quantity - ? WHERE id = ?";
        String insertPMsql = """
                INSERT INTO project_material 
                (project_id, material_id, quantity, unit_price, total_price)
                VALUES (?,?,?,?,?)
                """;
        Connection conn = null;

        try{
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try(PreparedStatement checkStock = conn.prepareStatement(checkStockSql);
                PreparedStatement updateStock = conn.prepareStatement(updateStockSql);
                PreparedStatement insertPM = conn.prepareStatement(insertPMsql)) {

                for (SelectedItem sel : selectedItems) {
                    int materialId = sel.getItem().getId();
                    int usedQty = sel.getQuantity();

                    checkStock.setInt(1, materialId);
                    ResultSet rs = checkStock.executeQuery();

                    if(!rs.next() || rs.getInt("quantity") < usedQty){
                        throw new SQLException("Not enough stock for material: " + sel.getItem().getName());
                    }

                    // subtract from material table
                    updateStock.setInt(1, usedQty);
                    updateStock.setInt(2, materialId);
                    updateStock.executeUpdate();

                    // insert into project_material
                    insertPM.setInt(1, projectId);
                    insertPM.setInt(2, materialId);
                    insertPM.setInt(3, usedQty);
                    insertPM.setBigDecimal(4, sel.getItem().getPrice());
                    insertPM.setBigDecimal(5, sel.getTotalPrice());
                    insertPM.executeUpdate();
                }

                conn.commit(); //success

            }catch(Exception ex){
                    conn.rollback(); //undo everything
                Platform.runLater(()->
                        showAlert("Stock Error", ex.getMessage()));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }finally {
            try{
                if(conn !=null) conn.setAutoCommit(true);
            }catch(SQLException ignored){}
        }
    }

    private void loadProjectToForm(Project project){

        loadingData = true;

        txt_projectName.setText(project.getPname());
        cmb_projectType.setValue(project.getPtype());
        txt_location.setText(project.getLocation());

        dt_startDate.setValue(project.getStart_date().toLocalDate());
        dt_endDate.setValue(project.getEnd_date().toLocalDate());

        // load payment only
        txt_payment.setText(
                project.getPayment().setScale(2, RoundingMode.HALF_UP).toString()
        );

        for(Register r : customers){
            if(r.getId() == project.getCid()){
                cmb_customer.getSelectionModel().select(r);
                break;
            }
        }
        loadProjectMaterials(project.getPid());

        loadingData = false;

        updateGrossPay();
    }

    private void updatePayment(){
        BigDecimal totalMaterial = BigDecimal.ZERO;
        for(SelectedItem sel : selectedItems){
            totalMaterial =  totalMaterial.add(sel.getTotalPrice());
        }
        BigDecimal payment = grossPay.add(totalMaterial);
        txt_payment.setText(payment.setScale(2, RoundingMode.HALF_UP).toString());
    }

    private void loadProjectMaterials(int projectId){

        selectedItems.clear();
        String sql = """
                SELECT pm.material_id, pm.quantity, pm.unit_price,
                     m.name 
                FROM project_material pm 
                JOIN material m ON pm.material_id = m.id
                WHERE pm.project_id = ?
                """;

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)){

            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();

            while(rs.next()){
                int materialId = rs.getInt("material_id");
                String name = rs.getString("name");
                BigDecimal price = rs.getBigDecimal("unit_price");
                int quantity = rs.getInt("quantity");

                Item item = new Item(materialId, name, price, 0);
                //stock not needed here
                selectedItems.add(new SelectedItem(item, quantity));
            }
            updateSelectedLabel();

        }catch(SQLException e){
            e.printStackTrace();
        }


    }

    private void handleUpdate(){
        if(selectedProjectForUpdate == null){
            showAlert("No Selection", " Please select a project to update");
            return;
        }

        if(txt_projectName.getText().isEmpty() || cmb_customer.getSelectionModel().getSelectedItem() == null){
            showAlert("Error", "Please fill project name and select a customer.");
            return;
        }

        Task<Boolean> task = new Task<>(){
            protected Boolean call(){

                boolean projectUpdated = updateProject(selectedProjectForUpdate.getPid());

                if(projectUpdated){
                    //updates material table
                    updateProjectMaterials(selectedProjectForUpdate.getPid(), selectedItems);
                    return true;
                }else return false;
            }
        };

        task.setOnSucceeded(e ->{
            if(task.getValue()){
                loadProjectTableAsync();
                clearForm();
                message.setText("Project Updated Successfully!");
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(ev -> message.setText(""));
                pause.play();
            }else{
                showAlert("Error", "Failed to update project.");
            }
        });
        new Thread(task).start();
    }

    private boolean updateProject(int projectId){

        Register selectedCustomer = cmb_customer.getSelectionModel().getSelectedItem();
        if(selectedCustomer == null)return false;

        String sql = """
                UPDATE project
                SET project_name = ?,
                customer_id = ?,
                project_type = ?,
                project_location = ?,
                start_date = ?,
                end_date = ?,
                actual_cost = ?,
                payment = ?
                WHERE project_id = ?
                """;

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement(sql)){

            pst.setString(1, txt_projectName.getText());
            pst.setInt(2, selectedCustomer.getId());
            pst.setString(3, cmb_projectType.getValue());
            pst.setString(4, txt_location.getText());
            pst.setDate(5, Date.valueOf(dt_startDate.getValue()));
            pst.setDate(6, Date.valueOf(dt_endDate.getValue()));
            pst.setBigDecimal(7, new BigDecimal(txt_grossPay.getText()));
            pst.setBigDecimal(8, new BigDecimal(txt_payment.getText()));
            pst.setInt(9, projectId);

            return pst.executeUpdate() > 0;

        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    private void updateProjectMaterials(int projectId, List<SelectedItem> newSelectedItems){


        String checkStockSql = "SELECT quantity FROM material WHERE id = ?";
        String getOldSql = "SELECT material_id, quantity FROM project_material WHERE project_id = ?";
        String restoreStockSql = "UPDATE material SET quantity = quantity + ? WHERE id = ?";
        String reduceStockSql = "UPDATE material SET quantity = quantity - ? WHERE id = ?";
        String deleteSql = "DELETE FROM project_material WHERE project_id = ?";
        String insertSql = """
                INSERT INTO project_material (project_id, material_id, quantity, unit_price, total_price) VALUES (?,?,?,?,?)
                """;
        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);          // start transaction
            try(
                PreparedStatement getOld = conn.prepareStatement(getOldSql);
                PreparedStatement restoreStock = conn.prepareStatement(restoreStockSql);
                PreparedStatement checkStock = conn.prepareStatement(checkStockSql);
                PreparedStatement reduceStock = conn.prepareStatement(reduceStockSql);
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                PreparedStatement insertStmt = conn.prepareStatement(insertSql)
            ){

                // 1. get old materials to restore stock
                getOld.setInt(1, projectId);
                ResultSet rs = getOld.executeQuery();
                while(rs.next()){
                    int materialId = rs.getInt("material_id");
                    int oldQty = rs.getInt("quantity");

                    // restores stock
                    restoreStock.setInt(1, oldQty);
                    restoreStock.setInt(2, materialId);
                    restoreStock.executeUpdate();
                }

                // 2. deletes all old materials
                deleteStmt.setInt(1, projectId);
                deleteStmt.executeUpdate();

                // 3.Inserts currently selected material and update stock
                for(SelectedItem sel : newSelectedItems){

                    int materialId = sel.getItem().getId();
                    int newQty = sel.getQuantity();

                    // checks stock before reducing
                    checkStock.setInt(1, materialId);
                    ResultSet stockRs = checkStock.executeQuery();
                    if(!stockRs.next()){
                        throw new SQLException("Material not found (ID: " + materialId + ")" );
                    }

                    int availableStock = stockRs.getInt("quantity");

                    if(availableStock < newQty){
                        throw new SQLException("Not enough stock for material ID: " + materialId +
                        ". Available: " + availableStock +
                        ", Requested: " + newQty
                        );
                    }

                    //reduces stock
                    reduceStock.setInt(1, newQty);
                    reduceStock.setInt(2, materialId);
                    reduceStock.executeUpdate();

                    //inserts new record
                    insertStmt.setInt(1, projectId);
                    insertStmt.setInt(2, materialId);
                    insertStmt.setInt(3,newQty);
                    insertStmt.setBigDecimal(4, sel.getItem().getPrice());
                    insertStmt.setBigDecimal(5, sel.getTotalPrice());
                    insertStmt.addBatch();

                    insertStmt.executeUpdate();
                }
                //insertStmt.executeBatch();
                conn.commit();

            }catch(SQLException e){
                conn.rollback();
                Platform.runLater(() -> showAlert("Datebase Error", " Failed to update materials: " + e.getMessage()));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

    }


    private boolean deleteProject(int projectId){
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("DELETE FROM project WHERE project_id = ?")){

            pst.setInt(1, projectId);
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;

        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }


    private void formatAsCurrency(TableColumn<Project, BigDecimal> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("$" + value.setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
        });
    }


    private void showAlert(String title, String msg) {
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
