package ide.view;


import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class View extends BorderPane {
    // private Node attributes (controls)
    private MenuBar menuBar;
    private TreeView<FileNode> fileTree;
    private CodeArea codeArea;
    private VirtualizedScrollPane<CodeArea> codeAreaContainer;
    private double fontSize = 12;
    private Stage primaryStage;
    private boolean savedChanges = true;
    private TextArea terminalHistory;
    private TextField terminalConsole;
    private BorderPane terminalContainer;

    private File currentFile;

    private SyntaxHighlighting syntaxHighlighting;

    //url, width, height, preserveRatio, smooth
    private final Image imageFolder = new Image("/images/icon-folder.png", 20, 20, true, true);
    private final Image imageFile = new Image("/images/icon-file-document.png", 20, 20, true, true);
    private final Image imageArchive = new Image("/images/icon-archive.png", 20, 20, true, true);
    private final Image imageBinary = new Image("/images/icon-file-binary.png", 20, 20, true, true);
    private final Image imageHex = new Image("/images/icon-file-hex.png", 20, 20, true, true);
    private final Image imageCode = new Image("/images/icon-code.png", 20, 20, true, true);
    private final Image imageCss = new Image("/images/icon-file-css.png", 20, 20, true, true);
    private final Image imageJava = new Image("/images/icon-code-java.png", 20, 20, true, true);
    private final Image imageFolderBurn = new Image("/images/document.png", 20, 20, true, true);

    public View(Stage primaryStage) {
        this.primaryStage = primaryStage;
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

        terminalHistory = new TextArea("Welcome to moncky IDE : version 0.1.0");
        terminalConsole = new TextField();
        terminalContainer = new BorderPane();

        fileTree = new TreeView<>();
        loadFolderIntoTreeView(new File("src")); // Starting directory

        // Add resizing functionality
        fileTree.setOnMousePressed(event -> handleMousePressed(event, fileTree));
        fileTree.setOnMouseDragged(event -> handleMouseDragged(event, fileTree));

        fileTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                File file = new File(newValue.getValue().getFilePath());
                if (file.isFile()) {
                    loadFileContentIntoEditor(file);
                }
            }
        });

        menuItemFileSave.setOnAction(event -> {
            saveEditorContentIntoFile();
        });


        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.replaceText(0, 0, "");

        codeAreaContainer = new VirtualizedScrollPane<>(codeArea);

        // Create keyboard shortcuts
        KeyCombination increaseFont = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);
        KeyCombination decreaseFont = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
        KeyCombination saveFile = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);

        menuItemFileOpenFolder.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        menuItemFileOpenFolder.setOnAction(e -> openFolder(primaryStage));
        menuItemFileSaveAs.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        menuItemFileSaveAs.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            saveEditorContentAsFile(fileChooser.showSaveDialog(primaryStage));
        });

        // Add key listener
        setOnKeyPressed(event -> {
            if (increaseFont.match(event)) {
                adjustFontSize(codeArea, 1); // Increase font size by 1
            } else if (decreaseFont.match(event)) {
                adjustFontSize(codeArea, -1); // Decrease font size by 1
            } else if (saveFile.match(event)) {
                saveEditorContentIntoFile();
            }
        });

    }

    private void layoutNodes() {
        // add/set … methods
        // Insets, padding, alignment, …
        setTop(menuBar);
        setLeft(fileTree);
        setCenter(codeAreaContainer);

        terminalContainer.setTop(terminalHistory);
        terminalContainer.setBottom(terminalConsole);
        setBottom(terminalContainer);
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

    private double startX;

    private void handleMousePressed(MouseEvent event, TreeView<FileNode> treeView) {
        // When mouse is pressed on the right edge of the TreeView, start resizing
        if (event.getX() > treeView.getWidth() - 10) { // 10px tolerance from the edge
            startX = event.getScreenX();
        }
    }

    private void handleMouseDragged(MouseEvent event, TreeView<FileNode> treeView) {
        if (startX != 0) {
            double deltaX = event.getScreenX() - startX;
            double newWidth = treeView.getWidth() + deltaX;

            if (newWidth > 50) { // Minimum width of the TreeView
                treeView.setPrefWidth(newWidth);
                startX = event.getScreenX();
            }
        }
    }

    private void adjustFontSize(CodeArea codeArea, int delta) {
        fontSize = Math.max(10, fontSize + delta); // Minimum font size is 10
        codeArea.setStyle(String.format("-fx-font-size: %.1fpx !important;", fontSize));


    }

    private void openFolder(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Folder as Project");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            loadFolderIntoTreeView(selectedDirectory);
        }
    }

    private void loadFolderIntoTreeView(File folder) {
        TreeItem<FileNode> rootItem = new TreeItem<>(new FileNode(folder.getName(), folder.getAbsolutePath()));
        rootItem.setExpanded(true);
        addChildrenToTreeItem(rootItem, folder);
        fileTree.setRoot(rootItem);
    }

    private void addChildrenToTreeItem(TreeItem<FileNode> parent, File folder) {

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                ImageView icon = new ImageView(file.isDirectory() ? imageFolder : imageFile);
                icon.setFitHeight(16);
                icon.setFitWidth(16);

                TreeItem<FileNode> child = new TreeItem<>(new FileNode(file.getName(), file.getAbsolutePath()), icon);
                parent.getChildren().add(child);
                if (file.isDirectory()) {
                    addChildrenToTreeItem(child, file); // Recursive call for directories
                }
            }
        }
    }

    private void loadFileContentIntoEditor(File file) {
        try {
            currentFile = file;
            String content = Files.readString(file.toPath());
            codeArea.replaceText(content);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load file: " + file.getName());
            alert.showAndWait();
        }
    }

    private void saveEditorContentIntoFile(){
        if (currentFile == null){
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            saveEditorContentAsFile(selectedFile);
        }
        try{
            Files.writeString(currentFile.toPath(), codeArea.getText());
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + currentFile.getName());
            alert.showAndWait();
        }
    }

    private void saveEditorContentAsFile(File selectedFile) {
        if (selectedFile == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + "No file selected");
            alert.showAndWait();
            return;
        }
        try{
            Files.writeString(selectedFile.toPath(), codeArea.getText());
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + selectedFile.getName());
            alert.showAndWait();
        }
    }

}
