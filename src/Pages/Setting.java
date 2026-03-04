package Pages;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

public class Setting {

    private Scene scene;
    private ObservableList<String> projectNames;

    private ComboBox<String> cmb_projectNames;
    private TextField txt_newProject;
    private Button btnAdd, btnDelete;

    public Setting(){
        initializeComponents();
    }

    private void initializeComponents(){
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Setting");
        title.getStyleClass().add("page-title");


        Button btnBack = new Button("← Home");
        btnBack.getStyleClass().add("button-primary");
        btnBack.setOnAction(e ->{
            SceneManager.showScene(new HomePage().getScene());
        });

        header.getChildren().addAll(btnBack, title);

        VBox content = new VBox();
        content.setPadding(new Insets(20));
        content.setSpacing(20);
        content.getStyleClass().add("form-box");

        // ComboBox to show project names
        cmb_projectNames = new ComboBox<>();
        cmb_projectNames.setPromptText("Project Names");
        cmb_projectNames.getStyleClass().add("input-field");

        txt_newProject = new TextField();
        txt_newProject.setPromptText("New Project Name");
        txt_newProject.getStyleClass().add("input-field");

        btnAdd = new Button("Add");
        btnAdd.getStyleClass().add("button-success");
        btnAdd.setOnAction(e -> addProjectNames());

        //btn

        HBox row = new HBox(10, cmb_projectNames, txt_newProject, btnAdd);
        row.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(row);

        root.setTop(header);
        root.setCenter(content);

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

    }

    private void addProjectNames(){
        if(projectNames == null) return;

        String newName = txt_newProject.getText().trim();

        if(newName.isEmpty()){
            showAlert("Error", "Project name cannot be empty.");
            return;
        }

        if(projectNames.contains(newName)){
            showAlert("Error", "Project name already exists");
            return;
        }

        projectNames.add(newName);
        txt_newProject.clear();
        cmb_projectNames.getSelectionModel().select(newName);
    }

    private void showAlert(String title, String msg){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }



    // setter to pass ObservableList from new _project
    public void setProjectNames(ObservableList<String> projectNames){
        this.projectNames = projectNames;

        // update ListView to show current names
        cmb_projectNames.setItems(this.projectNames);
    }


    public Scene getScene(){
        return scene;
    }

}
