package ide.view;


import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.util.Objects;

public class View extends BorderPane
        /* layout type */ {
    // private Node attributes (controls)
    private MenuBar menuBar;
    private TreeView<String> fileTree;
    private CodeArea codeArea;
    private ScrollPane codeAreaContainer;

    private SyntaxHighlighting syntaxHighlighting;

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

        // Add resizing functionality
        fileTree.setOnMousePressed(event -> handleMousePressed(event, fileTree));
        fileTree.setOnMouseDragged(event -> handleMouseDragged(event, fileTree));

        ImageView imageViewFolder = new ImageView(imageFolder);
        ImageView imageViewFile = new ImageView(imageFile);

        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.replaceText(0, 0,
                """
                ;save a
                li r0, 10
                li r1, 0x0A
                li r8, 8
                li r2, 1
                shl r1, r8
                add r1, r2
                st r1, (r0)
                                
                                
                ;save b
                li r0, 11
                li r1, 10
                st r1, (r0)
                                
                ;if statement code block
                li r10, :endif
                li r11, :else
                li r0, 10
                ld r1, (r0)
                li r0, 11
                ld r2, (r0)
                sub r1, r2
                jps r11
                li r0, 10
                ld r1, (r0)
                li r0, 11
                ld r2, (r0)
                sub r1, r2
                li r0, 10
                st r1, (r0)
                jp r10
                                
                                
                                
                                
                                
                :else
                ;else code block
                li r0, 10
                ld r1, (r0)
                li r0, 11
                ld r2, (r0)
                add r1, r2
                li r0, 10
                st r1, (r0)
                                
                :endif
                halt""");

        codeAreaContainer = new ScrollPane(codeArea);
        codeAreaContainer.setFitToHeight(true);
        codeAreaContainer.setFitToWidth(true);
    }

    private void layoutNodes() {
        // add/set … methods
        // Insets, padding, alignment, …
        setTop(menuBar);
        setLeft(fileTree);
        setCenter(codeAreaContainer);
    }

    private void styleNodes() {
        // Load CSS file
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        // Add CSS class to the CodeArea
        codeArea.getStyleClass().add("code-area");

        syntaxHighlighting = new SyntaxHighlighting();
        syntaxHighlighting.setupSyntaxHighlighting(codeArea);
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

    private double startX;

    private void handleMousePressed(MouseEvent event, TreeView<String> treeView) {
        // When mouse is pressed on the right edge of the TreeView, start resizing
        if (event.getX() > treeView.getWidth() - 10) { // 10px tolerance from the edge
            startX = event.getScreenX();
        }
    }

    private void handleMouseDragged(MouseEvent event, TreeView<String> treeView) {
        if (startX != 0) {
            double deltaX = event.getScreenX() - startX;
            double newWidth = treeView.getWidth() + deltaX;

            if (newWidth > 50) { // Minimum width of the TreeView
                treeView.setPrefWidth(newWidth);
                startX = event.getScreenX();
            }
        }
    }


}
