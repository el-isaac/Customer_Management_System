package Pages;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.*;

public class Project_Details {

    private int projectId;
    private Scene scene;

    private Label lbl_id = new Label();
    private Label lbl_name = new Label();
    private Label lbl_cname = new Label();
    private Label lbl_phone = new Label();
    private Label lbl_type = new Label();
    private Label lbl_address = new Label();
    private Label lbl_email= new Label();
    private Label lbl_start = new Label();
    private Label lbl_end = new Label();
    private Label lbl_actual = new Label();
    private Label lbl_payment = new Label();
    private Label lbl_totalMaterialCost = new Label();

    private TableView<Project_Materials> project_material;
    private ObservableList<Project_Materials> viewProjectItem = FXCollections.observableArrayList();

    public Project_Details(int projectId) {
        this.projectId = projectId;
        initializeComponents();
    }

    private void initializeComponents() {
        DropShadow outerShadow = new DropShadow(10, 4, 4, null);
        InnerShadow innerShadow = new InnerShadow(10, -4, -4, null);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setPrefSize(595, 842);
        root.getStyleClass().add("root-pane");

        // ---------- Header ----------
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setEffect(outerShadow);
        header.getStyleClass().add("header");

        Button back = new Button("← Back");
        back.getStyleClass().add("button-primary");
        back.setOnAction(e -> {
            SceneManager.showScene(new New_Project().getScene());
        });

        Label title = new Label("Invoice");
        title.setAlignment(Pos.TOP_CENTER);
        title.getStyleClass().add("title-label");
        HBox.setHgrow(title, Priority.ALWAYS);
        header.getChildren().addAll(back, title);

        // ---------- Project Info ----------
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(5);
        infoGrid.setPadding(new Insets(2));
        infoGrid.getStyleClass().add("invoice-info");


        Label invoiceTitle = new Label("INVOICE");
        invoiceTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black");

        Label companyName = new Label("TI Electric");
        companyName.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0D1B2A ");

        Label info = new Label("Phone: 613-981-7432\nE-Mail: Tamim_2336@hotmail.com\nFacebook: Facebook.com/TI Electric");


        infoGrid.add(invoiceTitle, 0, 0);
        GridPane.setHalignment(invoiceTitle, HPos.RIGHT);
        GridPane.setColumnSpan(invoiceTitle, 2);

        infoGrid.add(companyName, 0 ,1);
        GridPane.setHalignment(companyName, HPos.LEFT);



        int rowStart = 2;
        addInfoRow(infoGrid, rowStart + 0, "Invoice #:", lbl_id);
        addInfoRow(infoGrid, rowStart + 1, "Invoice Date:", lbl_start);
        addInfoRow(infoGrid, rowStart + 2, "Due Date:", lbl_end);

        Label gap = new Label("\n");
        Label billTo = new Label("Bill To:");
        billTo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black");

        infoGrid.add(gap, 0, 5);
        infoGrid.add(billTo, 0, 6);
        addInfoRow(infoGrid, rowStart + 6, "Customer Name:", lbl_cname);
        addInfoRow(infoGrid, rowStart + 7, "Address:", lbl_address);
        addInfoRow(infoGrid, rowStart + 8, "Phone / E-Mail:", lbl_email);



        // ---------- Project Material Table ----------
        project_material = new TableView<>();
        project_material.setPrefHeight(150);
        project_material.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        project_material.getStyleClass().add("invoice-table-view");


        TableColumn<Project_Materials, String> nameCol = new TableColumn<>("Material Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("material_name"));

        TableColumn<Project_Materials, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Project_Materials, BigDecimal> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatAsCurrency(priceCol);

        TableColumn<Project_Materials, BigDecimal> totalCol = new TableColumn<>("Total Price $");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        formatAsCurrency(totalCol);

        project_material.getColumns().addAll( nameCol, quantityCol, priceCol, totalCol);

        VBox tableContainer = new VBox(10, project_material);
        tableContainer.setPadding(new Insets(10));
        tableContainer.setEffect(outerShadow);
        tableContainer.getStyleClass().add("invoice-table-container");

        GridPane infoGrid2 = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(5);
        infoGrid.setPadding(new Insets(2));
        infoGrid.getStyleClass().add("invoice-info");


        addInfoRow(infoGrid2, 0,"Actual Cost:", lbl_actual);
        addInfoRow(infoGrid2, 1,"Payment:", lbl_payment);
        addInfoRow(infoGrid2, 2, "Total Material Cost:", lbl_totalMaterialCost);
        Label thank = new Label("Thanks for choosing TI Electric!");
        thank.setStyle("-fx-text-fill: black");
        infoGrid2.add(thank, 0, 3);

        VBox invoice = new VBox(40, infoGrid, tableContainer, infoGrid2);
        invoice.setPadding(new Insets(40));
        invoice.setSpacing(20);
        invoice.setPrefWidth(1000);
        invoice.setPrefHeight(1200);
        invoice.setMaxWidth(800);
        invoice.setMaxHeight(1200);
        invoice.getStyleClass().add("invoice-content");

        HBox invoiceBox = new HBox();
        invoiceBox.setPadding(new Insets(20));
        invoiceBox.setAlignment(Pos.CENTER);
        invoiceBox.getChildren().addAll(invoice);

        // Make table container and table grow with window
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        VBox.setVgrow(project_material, Priority.ALWAYS);

        // ---------- Footer ----------
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10));

        Button print = new Button("Print");
        print.getStyleClass().add("button-primary");
        print.setOnAction(e -> printDetails());
        footer.getChildren().add(print);
        root.setTop(header);
        root.setCenter(invoiceBox);
        root.setBottom(footer);

        scene = new Scene(root, 900, 800);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Responsive bindings: scale grid and table to scene size
        infoGrid.maxWidthProperty().bind(scene.widthProperty().subtract(40));
        tableContainer.prefWidthProperty().bind(scene.widthProperty().subtract(40));
        project_material.prefWidthProperty().bind(scene.widthProperty().subtract(60));

        // Lets table height take a proportional area of the window
        project_material.prefHeightProperty().bind(scene.heightProperty().multiply(0.42));

        // Optional: when shown, ensure controls recalculate sizes
        scene.windowProperty().addListener((obs, oldwindow, newWindow) ->{
            if(newWindow != null){
                newWindow.setOnShown(e -> project_material.refresh());
            }
        });

        loadProjectDetails();
        loadProjectMaterial();
    }

    private void addInfoRow(GridPane grid, int row, String labelText, Label valueLabel) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("label-bold");
        valueLabel.getStyleClass().add("label-value");
        grid.add(lbl, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private void loadProjectDetails() {
        String query = "SELECT p.project_id, p.project_name, c.name AS customer_name, p.project_type, p.project_location, " +
                "p.start_date, p.end_date, p.actual_cost, p.payment " +
                "FROM project p JOIN customer_details c ON p.customer_id = c.id WHERE p.project_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                lbl_id.setText(String.valueOf(rs.getInt("project_id")));
                lbl_name.setText(rs.getString("project_name"));
                lbl_cname.setText(rs.getString("customer_name"));
                lbl_type.setText(rs.getString("project_type"));
                lbl_address.setText(rs.getString("project_location"));
                lbl_start.setText(String.valueOf(rs.getDate("start_date")));
                lbl_end.setText(String.valueOf(rs.getDate("end_date")));
                lbl_actual.setText("$" + rs.getBigDecimal("actual_cost"));
                lbl_payment.setText("$" + rs.getBigDecimal("payment"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProjectMaterial() {
        //String query = "SELECT pm.id AS material_id, m.name AS material_name, pm.quantity, pm.unit_price, pm.total_price, " +
          //      "(SELECT SUM(pm2.total_price) FROM project_material pm2 WHERE pm2.project_id = pm.project_id) AS grand_total_cost " +
            //    "FROM project_material pm JOIN material m ON pm.material_id = m.id WHERE pm.project_id = ?";

        String query = """
                SELECT pm.id AS material_id,
                COALESCE(m.name, 'Deleted Material') AS material_name,
                pm.quantity,
                pm.unit_price,
                pm.total_price,
                (SELECT SUM(pm2.total_price) FROM project_material pm2 WHERE pm2.project_id = pm.project_id) AS grand_total_cost
                FROM project_material pm
                LEFT JOIN material m ON pm.material_id = m.id WHERE pm.project_id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, projectId);
            ResultSet rs = pst.executeQuery();
            viewProjectItem.clear();
            BigDecimal grandTotal = BigDecimal.ZERO;
            while (rs.next()) {
                int id = rs.getInt("material_id");
                String name = rs.getString("material_name");
                int quantity = rs.getInt("quantity");
                BigDecimal price = rs.getBigDecimal("unit_price");
                BigDecimal total = rs.getBigDecimal("total_price");
                grandTotal = rs.getBigDecimal("grand_total_cost");
                viewProjectItem.add(new Project_Materials(projectId, name, quantity, price, total));
            }
            lbl_totalMaterialCost.setText("$" + grandTotal);
            project_material.setItems(viewProjectItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void formatAsCurrency(TableColumn<Project_Materials, BigDecimal> column){
        column.setCellFactory(col -> new TableCell<Project_Materials, BigDecimal>(){
            @Override
            protected void updateItem(BigDecimal value, boolean empty){
                super.updateItem(value, empty);
                if(empty || value == null){
                    setText(null);
                }else{
                    setText("$" + value.toPlainString());
                }
            }
        });
    }

    private void printDetails() {
        Stage currentStage = (Stage) scene.getWindow();
        if (currentStage == null) {
            // Scene not yet attached, try later
            Platform.runLater(this::printDetails);
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean proceed = job.showPageSetupDialog(currentStage);
            if (proceed) {
                PageLayout layout = job.getPrinter().createPageLayout(Paper.A4,
                        PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);
                boolean success = job.printPage(layout, scene.getRoot());
                if (success) job.endJob();
            }
        }
    }



    public Scene getScene() {
        return scene;
    }
}
