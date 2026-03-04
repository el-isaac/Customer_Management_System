// java
package Pages;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomePage {
    private Scene scene;
    private Label lbl_custNo, lbl_totalMat, lbl_projNo;

    public HomePage() {
        initializeComponents();
    }

    private void initializeComponents() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        // ---------- TOP NAVBAR ----------
        HBox navbar = new HBox();
        navbar.getStyleClass().add("navbar");
        navbar.setPadding(new Insets(15));
        navbar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⚡ TI Electric");
        title.getStyleClass().add("navbar-title");
        navbar.getChildren().add(title);
        root.setTop(navbar);

        // ---------- SIDEBAR ----------
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setMaxWidth(280);

        Button btnNewCustomer = createSidebarButton("\uD83D\uDC64 Register Customer");
        btnNewCustomer.setOnAction(e -> {
            SceneManager.showScene(new Customer_Registration().getScene());
        });

        Button btnNewProject = createSidebarButton("\uD83D\uDC77 Start Project");
        btnNewProject.setOnAction(e -> {
           SceneManager.showScene(new New_Project().getScene());
        });

        Button btnManageMaterials = createSidebarButton("\uD83D\uDCA1 Manage Materials");
        btnManageMaterials.setOnAction(e -> {
            SceneManager.showScene(new Manage_Materials().getScene());
        });

        Button btnViewQuotation = createSidebarButton("\uD83D\uDCAC Quotation");
        btnViewQuotation.setOnAction(e ->{
            SceneManager.showScene(new Quotation().getScene());
        });

        Button btnViewStatistics = createSidebarButton("\uD83D\uDCCA Statistics");
        btnViewStatistics.setOnAction(e->{
            SceneManager.showScene(new Statistics().getScene());
        });

        Button btnViewInventory = createSidebarButton("\uD83D\uDCE6 Inventory");
        btnViewInventory.setOnAction(e ->{
            SceneManager.showScene(new Inventory().getScene());
        });

        Button btnViewSetting = createSidebarButton("\uD83D\uDEE0 Setting");
        btnViewSetting.setOnAction(e ->{
            SceneManager.showScene(new Setting().getScene());
        });

        sidebar.getChildren().addAll(btnNewCustomer, btnNewProject, btnManageMaterials, btnViewQuotation, btnViewInventory, btnViewStatistics, btnViewSetting);
        root.setLeft(sidebar);

        // ---------- MAIN DASHBOARD CARDS ----------
        HBox cardsContainer = new HBox(30);
        cardsContainer.setPadding(new Insets(30));
        cardsContainer.setAlignment(Pos.CENTER);

        VBox cardCustomers = createCard("\uD83D\uDC65 Total Customers");
        lbl_custNo = createCardNumber();
        cardCustomers.getChildren().add(lbl_custNo);

        VBox cardProjects = createCard("\uD83D\uDDC3 Total Projects");
        lbl_projNo = createCardNumber();
        cardProjects.getChildren().add(lbl_projNo);

        VBox cardMaterials = createCard("\uD83D\uDD0B Total Materials");
        lbl_totalMat = createCardNumber();
        cardMaterials.getChildren().add(lbl_totalMat);

        // make cards clickable and open related pages
        cardCustomers.setOnMouseClicked(e -> {
            SceneManager.showScene(new Customer_Registration().getScene());
        });
        cardProjects.setOnMouseClicked(e -> {
            SceneManager.showScene(new New_Project().getScene());
        });
        cardMaterials.setOnMouseClicked(e -> {
            SceneManager.showScene(new Manage_Materials().getScene());
        });



        Label lbl_company = new Label("TI⚡Electric");
        lbl_company.getStyleClass().add("logo");
        HBox companyTitleContainer = new HBox(30);
        companyTitleContainer.setPadding(new Insets(30));
        companyTitleContainer.setAlignment(Pos.CENTER);



        // cursor feedback
        cardCustomers.setCursor(Cursor.HAND);
        cardProjects.setCursor(Cursor.HAND);
        cardMaterials.setCursor(Cursor.HAND);

        // allow cards to grow
        HBox.setHgrow(cardCustomers, Priority.ALWAYS);
        HBox.setHgrow(cardProjects, Priority.ALWAYS);
        HBox.setHgrow(cardMaterials, Priority.ALWAYS);

        cardCustomers.setMaxWidth(Double.MAX_VALUE);
        cardProjects.setMaxWidth(Double.MAX_VALUE);
        cardMaterials.setMaxWidth(Double.MAX_VALUE);

        cardsContainer.getChildren().addAll(cardCustomers, cardProjects, cardMaterials);
        companyTitleContainer.getChildren().add(lbl_company);

        VBox title_cardsContainer = new VBox(cardsContainer, companyTitleContainer);

        HBox content = new HBox(20,sidebar, title_cardsContainer);
        content.setPadding(new Insets(20));
        content.setFillHeight(true);

        root.setCenter(content);

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());


        // Sidebar width proportional to window width
        sidebar.prefWidthProperty().bind(scene.widthProperty().multiply(0.18));

        // Cards width/height responsive to scene size minus sidebar
        cardCustomers.prefWidthProperty().bind(scene.widthProperty()
                .subtract(sidebar.prefWidthProperty())
                .subtract(120) // margins / spacing buffer
                .divide(3));
        cardProjects.prefWidthProperty().bind(cardCustomers.prefWidthProperty());
        cardMaterials.prefWidthProperty().bind(cardCustomers.prefWidthProperty());

        cardCustomers.prefHeightProperty().bind(scene.heightProperty().multiply(0.35));
        cardProjects.prefHeightProperty().bind(cardCustomers.prefHeightProperty());
        cardMaterials.prefHeightProperty().bind(cardCustomers.prefHeightProperty());

        setDataToCards(); // async database load
    }


    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        return btn;
    }

    private VBox createCard(String title) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(100, 100);
        card.getStyleClass().add("dashboard-card");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("card-title");
        card.getChildren().add(lblTitle);
        return card;
    }

    private Label createCardNumber() {
        Label lbl = new Label("0");
        lbl.getStyleClass().add("card-number");
        return lbl;
    }

    private void setDataToCards() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM customer_details");
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        int custCount = rs.getInt(1);
                        Platform.runLater(() -> lbl_custNo.setText(String.valueOf(custCount)));
                    }

                    pst = conn.prepareStatement("SELECT COUNT(*) FROM material");
                    rs = pst.executeQuery();
                    if (rs.next()) {
                        int matCount = rs.getInt(1);
                        Platform.runLater(() -> lbl_totalMat.setText(String.valueOf(matCount)));
                    }

                    pst = conn.prepareStatement("SELECT COUNT(*) FROM project");
                    rs = pst.executeQuery();
                    if (rs.next()) {
                        int projCount = rs.getInt(1);
                        Platform.runLater(() -> lbl_projNo.setText(String.valueOf(projCount)));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public Scene getScene() {
        return scene;
    }
}
