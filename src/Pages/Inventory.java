package Pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;

import java.math.BigDecimal;
import java.sql.*;

public class Inventory {
    private Scene scene;


    private TableView<Material> table;
    private ObservableList<Material> materialList = FXCollections.observableArrayList();

    private TextField txt_search;

    public Inventory(){
        initializeComponents();
    }

    private void initializeComponents(){
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Inventory");
        title.getStyleClass().add("page-title");

        Button btnBack = new Button("← Home");
        btnBack.getStyleClass().add("button-primary");
        btnBack.setOnAction(e ->{
            SceneManager.showScene(new HomePage().getScene());
        });

        txt_search = new TextField();
        txt_search.setPromptText("Search by name or type...");
        txt_search.getStyleClass().add("input-field");
        txt_search.setMaxWidth(300);
        txt_search.setMinWidth(200);
        txt_search.textProperty().addListener((observable, oldValue, newValue) ->{
            searchMaterialsAsync(newValue);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(btnBack, title, spacer, txt_search);

        VBox content = new VBox();
        content.setPadding(new Insets(20));
        content.getStyleClass().add("form-box");

        table = new TableView<>();
        table.setPrefHeight(1350);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Material, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Material, String> nameCol = new TableColumn<>("name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Material, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Material, Integer> quantityCol = new TableColumn<>("quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Material, BigDecimal> priceCol = new TableColumn<>("Price $");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatAsCurrency(priceCol);

        table.getColumns().addAll(idCol, nameCol, typeCol, quantityCol, priceCol);

        loadMaterialsAsync();

        content.getChildren().addAll(table);
        //content.setAlignment(Pos.CENTER);



        root.setTop(header);
        root.setCenter(content);
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
    }

    private void loadMaterialsAsync() {
        Task<ObservableList<Material>> task = new Task<>() {
            @Override
            protected ObservableList<Material> call() {
                ObservableList<Material> list = FXCollections.observableArrayList();
                try (
                        Connection conn = DatabaseConnection.getConnection();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * FROM material WHERE quantity > 0")) {
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

    // Search functionality can be implemented here, e.g. by adding a listener to the searchBar and filtering the materialList based on the input
    private void searchMaterialsAsync(String keyword){
        Task<ObservableList<Material>> task = new Task<>(){
          @Override
            protected ObservableList<Material> call() throws Exception{
              ObservableList<Material> list = FXCollections.observableArrayList();
              String sql = "SELECT * FROM material WHERE quantity > 0 AND name LIKE ? OR type LIKE ?";

              try(Connection conn = DatabaseConnection.getConnection();
                  PreparedStatement pst = conn.prepareStatement(sql)){

                  pst.setString(1, "%" + keyword + "%");
                  pst.setString(2, "%" + keyword + "%");

                  ResultSet rs = pst.executeQuery();
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
        task.setOnSucceeded(e-> table.setItems(task.getValue()));
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

    public Scene getScene(){
        return scene;
    }

}
