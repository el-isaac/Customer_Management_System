package Pages;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;


public class Statistics {
    private Scene scene;
    private Label lbl_custNo, lbl_totalMat, lbl_projNo, lbl_totalEarning, lbl_ProjMatNo, lbl_totalProjMatCost, lbl_totalInventory;
    private static final int LOW_STOCK_THRESHOLD = 20;

    public Statistics(){
        initializeComponents();
    }

    private void initializeComponents(){
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Statistics");
        title.getStyleClass().add("page-title");

        Button btnBack = new Button("← Home");
        btnBack.getStyleClass().add("button-primary");
        btnBack.setOnAction(e ->{
            SceneManager.showScene(new HomePage().getScene());
        });

        VBox content = new VBox();
        content.setPadding(new Insets(20));
        content.getStyleClass().add("form-box");
        content.setAlignment(Pos.TOP_CENTER);


        header.getChildren().addAll(btnBack, title);

        HBox cardsContainer = new HBox(30);
        cardsContainer.setPadding(new Insets(40));
        cardsContainer.setAlignment(Pos.CENTER);

        VBox cardProjects = createCard("\uD83D\uDC77 Total Projects");
        lbl_projNo = createCardNumber();
        cardProjects.getChildren().add(lbl_projNo);

        VBox cardCustomers = createCard("\uD83D\uDC65 Total Customers");
        lbl_custNo = createCardNumber();
        cardCustomers.getChildren().add(lbl_custNo);

        VBox cardMaterial = createCard("\uD83D\uDCA1 Total Materials");
        lbl_totalMat = createCardNumber();
        cardMaterial.getChildren().add(lbl_totalMat);

        VBox cardInventory = createCard("\uD83D\uDCE6 Inventory");
        lbl_totalInventory = createCardNumber();
        cardInventory.getChildren().add(lbl_totalInventory);

        VBox cardProjMatNo = createCard("\uD83D\uDD0C Total Proj. Materials");
        lbl_ProjMatNo = createCardNumber();
        cardProjMatNo.getChildren().add(lbl_ProjMatNo);

        VBox cardTotalProjMatCost = createCard("\uD83E\uDDFE Total Proj. Material Cost");
        lbl_totalProjMatCost = createCardNumber();
        cardTotalProjMatCost.getChildren().add(lbl_totalProjMatCost);

        VBox cardTotalEarning = createCard("\uD83D\uDCB0 Total Gross Income");
        lbl_totalEarning = createCardNumber();
        cardTotalEarning.getChildren().add(lbl_totalEarning);

        // allow card to grow
        HBox.setHgrow(cardProjects, Priority.ALWAYS);
        HBox.setHgrow(cardCustomers, Priority.ALWAYS);
        HBox.setHgrow(cardMaterial, Priority.ALWAYS);
        HBox.setHgrow(cardInventory, Priority.ALWAYS);
        HBox.setHgrow(cardProjMatNo, Priority.ALWAYS);
        HBox.setHgrow(cardTotalProjMatCost, Priority.ALWAYS);
        HBox.setHgrow(cardTotalEarning, Priority.ALWAYS);

        VBox barContainer = new VBox();
        barContainer.setPadding(new Insets(40));
        barContainer.getStyleClass().add("form-box");

        barContainer.getChildren().add(createBarChart());

        cardsContainer.getChildren().addAll(cardProjects, cardCustomers, cardMaterial, cardInventory, cardProjMatNo, cardTotalProjMatCost, cardTotalEarning);

        content.getChildren().addAll(cardsContainer);

        root.setTop(header);
        root.setCenter(content);
        root.setBottom(barContainer);


        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        setDataToCards();
    }

    private VBox createCard(String title){
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(150,150);
        card.getStyleClass().add("dashboard-card");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("card-title");
        card.getChildren().add(lblTitle);
        return card;
    }

    private Label createCardNumber(){
        Label lbl = new Label("0");
        lbl.getStyleClass().add("card-number");
        return lbl;
    }

    private void setDataToCards(){
        Task<Void> task = new Task<>(){

            @Override
            protected Void call() throws Exception{
                try(Connection conn = DatabaseConnection.getConnection()){

                    PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM project");
                    ResultSet rs = pst.executeQuery();
                    if(rs.next()) {
                        int projCount = rs.getInt(1);
                        Platform.runLater(()-> lbl_projNo.setText(String.valueOf(projCount)));
                    }

                    pst = conn.prepareStatement("SELECT COUNT(*) FROM material");
                    rs = pst.executeQuery();
                    if(rs.next()){
                        int matCount = rs.getInt(1);
                        Platform.runLater(()-> lbl_totalMat.setText(String.valueOf(matCount)));
                    }

                    pst = conn.prepareStatement("SELECT SUM(quantity) FROM material WHERE quantity > 0");
                    rs = pst.executeQuery();
                    if(rs.next()){
                        int inventoryCount = rs.getInt(1);
                        Platform.runLater(()-> lbl_totalInventory.setText(String.valueOf(inventoryCount)));
                    }


                    pst = conn.prepareStatement("SELECT COUNT(*) FROM customer_details");
                    rs = pst.executeQuery();
                    if(rs.next()){
                        int custCount = rs.getInt(1);
                        Platform.runLater(() -> lbl_custNo.setText(String.valueOf(custCount)));
                    }

                    pst = conn.prepareStatement("SELECT SUM(quantity) FROM project_material");
                    rs = pst.executeQuery();
                    if(rs.next()){
                        int projMatCount = rs.getInt(1);
                        Platform.runLater(()-> lbl_ProjMatNo.setText(String.valueOf(projMatCount)));
                    }

                    pst = conn.prepareStatement("SELECT SUM(total_price) FROM project_material");
                    rs = pst.executeQuery();
                    if(rs.next()){
                        BigDecimal totalProjMatCost = rs.getBigDecimal(1);
                        if(totalProjMatCost == null) totalProjMatCost = BigDecimal.ZERO;

                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);
                        String formatted = currencyFormat.format(totalProjMatCost);
                        Platform.runLater(()-> lbl_totalProjMatCost.setText(String.valueOf(formatted)));
                    }

                    pst = conn.prepareStatement("SELECT SUM(actual_cost) FROM project");
                    rs = pst.executeQuery();
                    if(rs.next()){
                        BigDecimal totalEarning = rs.getBigDecimal(1);
                        if(totalEarning == null) totalEarning = BigDecimal.ZERO;

                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);
                        String formatted = currencyFormat.format(totalEarning);
                        Platform.runLater(()-> lbl_totalEarning.setText(String.valueOf(formatted)));
                    }

                }catch(SQLException ex){
                    ex.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public static BarChart<String, Number> createBarChart(){
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Monthly Revenue");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Income");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.getStyleClass().add("bar-chart");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        String sql = """
                SELECT
                MONTH(start_date) AS month_no,
                FORMATDATETIME(start_date, 'MMM') AS month_name,
                SUM(payment) AS total_payment
                FROM project WHERE start_date IS NOT NULL 
                GROUP BY MONTH(start_date), FORMATDATETIME(start_date, 'MMM')
                ORDER BY month_no
                """;

        try{
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            String[] colors = {
                    "#00ffff", // cyan
                    "#7fffd4", // aquamarine
                    "#ff6ec7", // pink neon
                    "#ffcc00", // bright yellow
                    "#ff4500", // orange-red
                    "#00ff7f", // spring green
                    "#1e90ff",  // dodger blue
                    "#ff00ff", // magenta
                    "#32cd32", // lime green
                    "#ff1493", // deep pink
                    "#00ced1", // dark turquoise
                    "#ffd700"  // gold
            };
            int colorIndex = 0;

            while(rs.next()){
                String month = rs.getString("month_name");

                BigDecimal totalPayment = rs.getBigDecimal("total_payment");

                XYChart.Data<String, Number> data = new XYChart.Data<>(month, totalPayment);
                series.getData().add(data);

                //pick color for this bar
                final String color = colors[colorIndex % colors.length];
                colorIndex++;

                //color bars after node creation
                data.nodeProperty().addListener((obs, oldNode, newNode)->{
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: " + color + ";" +
                                        "-fx-effect: dropshadow(one-pass-box, rgba(224,247,255,0.35), 8, 0, 0, 2);");

                        Tooltip.install(newNode, new Tooltip(month + " : " + totalPayment));
                    }

                });
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        barChart.getData().add(series);

        // Rotate X-axis label for long name
        xAxis.setTickLabelRotation(45);

        return barChart;
    }


    public Scene getScene(){
        return scene;
    }
}
