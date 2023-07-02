/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication7;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class javafxapplication7 extends Application {
    
    private static final String CSV_DELIMITER = ",";
    
    private Map<String, Location> locationsMap = new HashMap<>();
    private AVLTree<Martyr> martyrsAvl1 = new AVLTree<>();
    private AVLTree<Martyr> martyrsAvl2 = new AVLTree<>();

    private TableView<Location> locationsTable = new TableView<>();
    private TableView<Martyr> martyrsTable = new TableView<>();
    private Label summaryLabel = new Label();
    private TableView<Martyr> avl1Table = new TableView<>();
    private TableView<Martyr> avl2Table = new TableView<>();
    private TextField searchField = new TextField();
    private Label searchResultLabel = new Label();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Martyrs App");

        BorderPane root = new BorderPane();

        root.setTop(createTopPane());
        root.setCenter(createMainScreen());
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createTopPane() {
        Button loadButton = new Button("Load File");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open CSV File");
                File file = fileChooser.showOpenDialog(null);
                if (file != null) {
                    loadMartyrsDataFromFile(file);
                    locationsTable.setItems(getLocationsList());
                }
            }
        });

        HBox hbox = new HBox(loadButton);
        hbox.setPadding(new Insets(10));

        return new VBox(hbox);
    }

    private TabPane createMainScreen() {
        TabPane tabPane = new TabPane();

        Tab locationsTab = new Tab("Location Screen");
        locationsTab.setContent(createLocationsScreen());

        Tab martyrsTab = new Tab("Martyrs Screen");
        martyrsTab.setContent(createMartyrsScreen());

        Tab statisticsTab = new Tab("Statistics Screen");
        statisticsTab.setContent(createStatisticsScreen());

        Tab saveTab = new Tab("Save Screen");
        saveTab.setContent(createSaveScreen());

        tabPane.getTabs().addAll(locationsTab, martyrsTab, statisticsTab, saveTab);

        return tabPane;
    }

    private VBox createLocationsScreen() {
        TableColumn<Location, String> nameCol = new TableColumn<>("Location Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Location, Integer> numMartyrsCol = new TableColumn<>("Number of Martyrs");
        numMartyrsCol.setCellValueFactory(new PropertyValueFactory<>("numMartyrs"));

        TableColumn<Location, LocalDate> lastUpdatedCol = new TableColumn<>("Last Updated");
        lastUpdatedCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));

        locationsTable.getColumns().addAll(nameCol, numMartyrsCol, lastUpdatedCol);

        Button addLocationButton = new Button("Add Location");
        addLocationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showAddLocationDialog();
            }
        });

        Button editLocationButton = new Button("Edit Location");
        editLocationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Location location = locationsTable.getSelectionModel().getSelectedItem();
                if (location != null) {
                    showEditLocationDialog(location);
                }
            }
        });

        Button deleteLocationButton = new Button("Delete Location");
        deleteLocationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Location location = locationsTable.getSelectionModel().getSelectedItem();
                if (location != null) {
                    deleteLocation(location);
                }
            }
        });

        VBox vbox = new VBox(locationsTable, addLocationButton, editLocationButton,deleteLocationButton);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        return vbox;
    }

    private VBox createMartyrsScreen() {
        TableColumn<Martyr, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Martyr, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("locationName"));

        TableColumn<Martyr, LocalDate> dateCol = new TableColumn<>("Date of Death");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateOfDeath"));

        martyrsTable.getColumns().addAll(nameCol, locationCol, dateCol);

        searchField.setPromptText("Search by Name");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Martyr> result = searchMartyrsByName(newValue);
            martyrsTable.setItems(result);
            searchResultLabel.setText("Found " + result.size() + " results.");
        });

        HBox searchBox = new HBox(searchField, searchResultLabel);
        searchBox.setSpacing(10);

        VBox vbox = new VBox(martyrsTable, searchBox);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        return vbox;
    }

    private VBox createStatisticsScreen() {
        TableColumn<Martyr, String> nameCol1 = new TableColumn<>("Name");
        nameCol1.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Martyr, String> locationCol1 = new TableColumn<>("Location");
        locationCol1.setCellValueFactory(new PropertyValueFactory<>("locationName"));

        TableColumn<Martyr, LocalDate> dateCol1 = new TableColumn<>("Date of Death");
        dateCol1.setCellValueFactory(new PropertyValueFactory<>("dateOfDeath"));

        avl1Table.getColumns().addAll(nameCol1, locationCol1, dateCol1);

        TableColumn<Martyr, String> nameCol2 = new TableColumn<>("Name");
        nameCol2.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Martyr, String> locationCol2 = new TableColumn<>("Location");
        locationCol2.setCellValueFactory(new PropertyValueFactory<>("locationName"));

        TableColumn<Martyr, LocalDate> dateCol2 = new TableColumn<>("Date of Death");
        dateCol2.setCellValueFactory(new PropertyValueFactory<>("dateOfDeath"));

        avl2Table.getColumns().addAll(nameCol2, locationCol2, dateCol2);

        Button populateButton = new Button("Populate Trees");
        populateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                populateTrees();
            }
        });

        summaryLabel.setWrapText(true);

        VBox vbox = new VBox(avl1Table, avl2Table, populateButton, summaryLabel);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        return vbox;
    }

    private VBox createSaveScreen() {
        Button saveButton = new Button("Save to File");
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveDataToFile();
            }
        });

        VBox vbox = new VBox(saveButton);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        return vbox;
    }

    private ObservableList<Location> getLocationsList() {
        List<Location> locations = new ArrayList<>(locationsMap.values());
        return FXCollections.observableArrayList(locations);
    }

    private void showAddLocationDialog() {
        LocationDialog dialog = new LocationDialog();
        Location location = dialog.showAndWait();
        if (location != null) {
            locationsMap.put(location.getName(), location);
            locationsTable.setItems(getLocationsList());
        }
    }

    private void showEditLocationDialog(Location location) {
        LocationDialog dialog = new LocationDialog(location);
        Location updatedLocation = dialog.showAndWait();
        if (updatedLocation != null) {
            locationsMap.put(updatedLocation.getName(), updatedLocation);
            locationsTable.setItems(getLocationsList());
        }
    }

    private void deleteLocation(Location location) {
        locationsMap.remove(location.getName());
        locationsTable.setItems(getLocationsList());
    }

    private void loadMartyrsDataFromFile(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] fields = line.split(CSV_DELIMITER);
                String name = fields[0];
                String locationName = fields[1];
                LocalDate dateOfDeath = LocalDate.parse(fields[2]);
                Location location = locationsMap.get(locationName);
                if (location == null) {
                    location = new Location(locationName);
                    locationsMap.put(locationName, location);
                }
                Martyr martyr = new Martyr(name, location, dateOfDeath);
                location.addMartyr(martyr);
                martyrsAvl1.insert(martyr);
                martyrsAvl2.insert(martyr);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Martyr> searchMartyrsByName(String name) {
        List<Martyr> martyrs = new ArrayList<>();
        martyrs.addAll(martyrsAvl1.search(m -> m.getName().toLowerCase().contains(name.toLowerCase())));
        martyrs.addAll(martyrsAvl2.search(m -> m.getName().toLowerCase().contains(name.toLowerCase())));
        return FXCollections.observableArrayList(martyrs);
    }

    private void populateTrees() {
        martyrsAvl1 = new AVLTree<>();
        martyrsAvl2 = new AVLTree<>();
        for (Location location : locationsMap.values()) {
            for (Martyr martyr : location.getMartyrs()) {
                martyrsAvl1.insert(martyr);
            }
        }
        for (Martyr martyr : martyrsAvl1.inOrder()) {
            martyrsAvl2.insert(martyr);
        }
        summaryLabel.setText("AVL Tree 1 size: " + martyrsAvl1.size() + "\nAVL Tree 2 size: " + martyrsAvl2.size());
        avl1Table.setItems(getMartyrsList(martyrsAvl1.inOrder()));
        avl2Table.setItems(getMartyrsList(martyrsAvl2.inOrder()));
    }

    private void saveDataToFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save CSV File");
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                StringBuilder sb = new StringBuilder();
                for (Location location : locationsMap.values()) {
                    for (Martyr martyr : location.getMartyrs()) {
                        sb.append(martyr.getName()).append(CSV_DELIMITER);
                        sb.append(location.getName()).append(CSV_DELIMITER);
                        sb.append(martyr.getDateOfDeath()).append("\n");
                    }
                }
                Files.write(file.toPath(), sb.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Martyr> getMartyrsList(List<Martyr> martyrs) {
        return FXCollections.observableArrayList(martyrs);
    }

    private static class LocationDialog extends Dialog<Location> {

        private TextField nameField = new TextField();
        private Label errorLabel = new Label();
        private Location location;

        public LocationDialog() {
            setTitle("Add Location");
            setHeaderText("Please enter the location details.");

            ButtonType addButton = new ButtonType("Add", ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

            VBox vbox = new VBox();
            vbox.setSpacing(10);
            vbox.setPadding(new Insets(10));

            nameField.setPromptText("Location Name");

            vbox.getChildren().addAll(nameField, errorLabel);

            getDialogPane().setContent(vbox);

            setResultConverter(dialogButton -> {
                if (dialogButton == addButton) {
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        errorLabel.setText("Location name cannot be empty.");
                        return null;
                    }
                    location = new Location(name);
                    return location;
                }
                return null;
            });
        }

        public LocationDialog(Location location) {
            this();
            setTitle("Edit Location");
            setHeaderText("Please update the location details.");

            this.location = location;
            nameField.setText(location.getName());

            setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        errorLabel.setText("Location name cannot be empty.");
                        return null;
                    }
                    location.setName(name);
                    return location;
                }
                return null;
            });
        }
    }

    private static class Martyr {

        private final SimpleStringProperty name;
        private final SimpleObjectProperty<Location> location;
        private final SimpleObjectProperty<LocalDate> dateOfDeath;

        public Martyr(String name, Location location, LocalDate dateOfDeath) {
            this.name = new SimpleStringProperty(name);
            this.location = new SimpleObjectProperty<>(location);
            this.dateOfDeath = new SimpleObjectProperty<>(dateOfDeath);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public Location getLocation() {
            return location.get();
        }

        public String getLocationName() {
            return location.get().getName();
        }

        public void setLocation(Location location) {
            this.location.set(location);
        }

        public LocalDate getDateOfDeath() {
            return dateOfDeath.get();
        }

        public void setDateOfDeath(LocalDate dateOfDeath) {
            this.dateOfDeath.set(dateOfDeath);
        }
    }

    private static class Location {

        private final SimpleStringProperty name;
        private final List<Martyr> martyrs;

        public Location(String name) {
            this.name = new SimpleStringProperty(name);
            this.martyrs = new ArrayList<>();
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public List<Martyr> getMartyrs() {
            return martyrs;
        }

        public void addMartyr(Martyr martyr) {
            martyrs.add(martyr);
        }

        public void removeMartyr(Martyr martyr) {
            martyrs.remove(martyr);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}


public  class AVLTree<T extends Comparable<T>> {

    private AVLNode<T> root;

    public void insert(T data) {
        root = insert(root, data);
    }

    private AVLNode<T> insert(AVLNode<T> node, T data) {
        if (node == null) {
            return new AVLNode<>(data);
        }

        if (data.compareTo(node.data) < 0) {
            node.left = insert(node.left, data);
        } else if (data.compareTo(node.data) > 0) {
            node.right = insert(node.right, data);
        } else {
            // data already exists in the tree
            return node;
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));

        int balance = getBalance(node);

        // Left Left Case
        if (balance > 1 && data.compareTo(node.left.data) < 0) {
            return rightRotate(node);
        }

        // Right Right Case
        if (balance < -1 && data.compareTo(node.right.data) > 0) {
            return leftRotate(node);
        }

        // Left Right Case
        if (balance > 1 && data.compareTo(node.left.data) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Left Case
        if (balance < -1 && data.compareTo(node.right.data) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    public void delete(T data) {
        root = delete(root, data);
    }

    private AVLNode<T> delete(AVLNode<T> node, T data) {
        if (node == null) {
            return node;
        }

        if (data.compareTo(node.data) < 0) {
            node.left = delete(node.left, data);
        } else if (data.compareTo(node.data) > 0) {
            node.right = delete(node.right, data);
        } else {
            if (node.left == null || node.right == null) {
                AVLNode<T> temp = null;
                if (temp == node.left) {
                    temp = node.right;
                } else {
                    temp = node.left;
                }

                if (temp == null) {
                    node = null;
                } else {
                    node = temp;
                }
            } else {
                AVLNode<T> temp = minValueNode(node.right);
                node.data = temp.data;
                node.right = delete(node.right, temp.data);
            }
        }

        if (node == null) {
            return node;
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));

        int balance = getBalance(node);

        // Left Left Case
        if (balance > 1 && getBalance(node.left) >= 0) {
            return rightRotate(node);
        }

        // Left Right Case
        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Right Case
        if (balance < -1 && getBalance(node.right) <= 0) {
            return leftRotate(node);
        }

        // Right Left Case
        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    private AVLNode<T> minValueNode(AVLNode<T> node) {
        AVLNode<T> current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    private int height(AVLNode<T> node) {
        if (node == null) {
            return 0;
        }
        return node.height;
    }

    private int getBalance(AVLNode<T> node) {
        if (node == null) {
            return 0;
        }
        return height(node.left) - height(node.right);
    }

    private AVLNode<T> rightRotate(AVLNode<T> y) {
        AVLNode<T> x = y.left;
        AVLNode<T> t2 = x.right;

        x.right = y;
        y.left = t2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private AVLNode<T> leftRotate(AVLNode<T> x) {
        AVLNode<T> y = x.right;
        AVLNode<T> t2 = y.left;

        y.left = x;
        x.right = t2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    private static class AVLNode<T> {
        private T data;
        private AVLNode<T> left;
        private AVLNode<T> right;
        private int height;

        AVLNode(T data) {
            this.data = data;
            this.height = 1;
        }
    }
}