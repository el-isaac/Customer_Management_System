package Pages;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;


public class Quotation {
    private Scene scene;
    private ComboBox<Item> list_material;
    private Label lbl_selected, lbl_materialCost, message;

    private ObservableList<Item> items = FXCollections.observableArrayList();
    private ObservableList<SelectedItem> selectedItems = FXCollections.observableArrayList();



    public Quotation(){
        initializeComponents();
    }

    private void initializeComponents(){

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Quotation");
        title.getStyleClass().add("page-title");


        Button btnBack = new Button("← Home");
        btnBack.getStyleClass().add("button-primary");
        btnBack.setOnAction(e->{
          SceneManager.showScene(new HomePage().getScene());
        });

        header.getChildren().addAll(btnBack, title);

        VBox  content = new VBox();
        content.setPadding(new Insets(20));
        content.setSpacing(20);
        content.getStyleClass().add("form-box");

        list_material = new ComboBox<>(items);
        list_material.getStyleClass().add("input-field");
        list_material.setPromptText("Select Material");
        list_material.setOnAction(e -> selectMaterial());
        lbl_selected = new Label("Selected Items: None");
        lbl_materialCost = new Label("Total Material Cost: $0.00");

        content.getChildren().addAll(list_material, lbl_selected, lbl_materialCost);
        content.setAlignment(Pos.TOP_CENTER);

        root.setTop(header);
        root.setCenter(content);

        loadMaterials();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());


    }

    /*private void selectMaterial(){
        Item selected = list_material.getSelectionModel().getSelectedItem();
        if(selected == null ) return;

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setHeaderText("Enter quantity for " + selected.getName());
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(q -> {
            try{
                int quantity = Integer.parseInt(q);
                if(quantity > 0){
                    selectedItems.add(new SelectedItem(selected, quantity));
                    updateSelectedLabel();
                }
            }catch(NumberFormatException ex){
                showAlert("Invalid Quantity", "Please enter a valid number for number.");
            }
        });
        Platform.runLater(() -> list_material.getSelectionModel().clearSelection());
    }*/

    private void selectMaterial(){
        Item selected = list_material.getSelectionModel().getSelectedItem();
        if(selected == null) return;


        //int availableQty = selected.getQuantity(); //stock from DB

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Material Quantity");
        dialog.setHeaderText("Enter Quantity for " + selected.getName());

        Optional<String>  result = dialog.showAndWait();

        result.ifPresent(q ->{
            try{
                int quantity = Integer.parseInt(q);

                if(quantity <= 0){
                    showAlert("Invalid Quantity", "Quantity must be greater than zero");
                    return;
                }

                // prevent duplicate material entry
                for(SelectedItem si: selectedItems){
                    if(si.getItem().getId() == selected.getId()){
                        showAlert("Duplicate Material", "This material is already added.");
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

    private void updateSelectedLabel(){
        if(selectedItems.isEmpty()){
            lbl_selected.setText("Selected Items: None");

            lbl_materialCost.setText("Total Material Cost: $0.00");
            return;
        }

        StringBuilder sb = new StringBuilder("Selected Items: \n");
        BigDecimal total = BigDecimal.ZERO;
        for(SelectedItem sel : selectedItems){
            BigDecimal lineTotal = sel.getTotalPrice();
            total = total.add(lineTotal);
            sb.append(sel.getItem().getName()).append(" x ").append(sel.getQuantity()).append(" = $").append(lineTotal).append("\n");
        }
        lbl_selected.setText(sb.toString());
        lbl_materialCost.setText("Total Material Cost: $" + total);
    }

    private void loadMaterials(){
        Task<ObservableList<Item>> task = new Task<>() {
            protected ObservableList<Item> call() throws Exception{
                ObservableList<Item> matItems = FXCollections.observableArrayList();
                try(Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pst = conn.prepareStatement("Select id, name, price, quantity from material");
                    ResultSet rs = pst.executeQuery()){
                    while(rs.next()){
                    matItems.add(new Item(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price"), rs.getInt("quantity")));
                    }
                }
                return matItems;
            }
        };
        task.setOnSucceeded(e -> items.setAll(task.getValue()));
        new Thread(task).start();
    }

    private void showAlert(String title, String msg){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public Scene getScene(){
        return scene;
    }

}
