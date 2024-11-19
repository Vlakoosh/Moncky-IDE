package ide.view;


import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.Objects;

public class View extends BorderPane
        /* layout type */ {
    // private Node attributes (controls)
    private MenuBar menuBar;
    private TreeView<String> fileTree;

    //url, width, height, preserveRatio, smooth
    private final Image imageFolder = new Image("/images/folder.png", 20, 20, true, true );
    private final Image imageFile = new Image("/images/document.png", 20, 20, true, true );

    public View() {
        initialiseNodes();
        layoutNodes();
        styleNodes();
    }

    private void initialiseNodes() {
        // create and configure controls
        // button = new Button("...")
        // label = new Label("...")

        //create menus
        Menu menuFile = new Menu("File");
        Menu menuEdit = new Menu("Edit");
        Menu menuRun = new Menu("Run");

        // create menuitems
        MenuItem menuItemFileSave = new MenuItem("Save");
        MenuItem menuItemFileSaveAs = new MenuItem("Save As");
        MenuItem menuItemFileOpenFolder = new MenuItem("Open Folder");

        MenuItem menuItemEditUndo = new MenuItem("Undo");

        MenuItem menuItemRunCompile = new MenuItem("Compile");
        MenuItem menuItemRunInterpreter = new MenuItem("Interpreter");

        // add menu items to menu
        menuFile.getItems().add(menuItemFileSave);
        menuFile.getItems().add(menuItemFileSaveAs);
        menuFile.getItems().add(menuItemFileOpenFolder);

        menuEdit.getItems().add(menuItemEditUndo);

        menuRun.getItems().add(menuItemRunCompile);
        menuRun.getItems().add(menuItemRunInterpreter);

        // create a menubar
        menuBar = new MenuBar();

        // add menu to menubar
        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuEdit);
        menuBar.getMenus().add(menuRun);

        fileTree = createFileTreeView("src"); // Starting directory



        ImageView imageViewFolder = new ImageView(imageFolder);
        ImageView imageViewFile = new ImageView(imageFile);


    }

    private void layoutNodes() {
        // add/set … methods
        // Insets, padding, alignment, …
        setTop(menuBar);
        setLeft(fileTree);
    }

    private void styleNodes() {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

    }


    // package-private Getters
    // for controls used by Presenter

    private TreeView<String> createFileTreeView(String rootDir) {
        File rootFile = new File(rootDir);
        TreeItem<String> rootItem = createNode(rootFile);
        TreeView<String> treeView = new TreeView<>(rootItem);
        return treeView;
    }

    private TreeItem<String> createNode(File file) {
        ImageView icon = new ImageView(file.isDirectory() ? imageFolder : imageFile);
        icon.setFitHeight(16);
        icon.setFitWidth(16);

        TreeItem<String> treeItem = new TreeItem<>(file.getName(), icon);
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                treeItem.getChildren().add(createNode(child));
            }
        }
        return treeItem;
    }
}
