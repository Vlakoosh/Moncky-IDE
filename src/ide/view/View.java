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
import javafx.scene.input.KeyCombination;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class View extends BorderPane {
    // private Node attributes (controls)

    // parent stage/window
    private final Stage primaryStage;

    // main menu bar
    private MenuBar menuBar;

    // menus
    private Menu menuFile;
    private Menu menuView;
    private Menu menuEdit;
    private Menu menuRun;

    // menu items
    private MenuItem menuItemFileNew;
    private MenuItem menuItemFileNewFolder;
    private MenuItem menuItemFileSave;
    private MenuItem menuItemFileSaveAs;
    private MenuItem menuItemFileOpenFolder;

    private MenuItem menuViewFontSizeIncrease;
    private MenuItem menuViewFontSizeDecrease;

    private MenuItem menuItemEditUndo;

    private MenuItem menuItemRunCompile;
    private MenuItem menuItemRunInterpreter;

    // file tree
    private TreeView<FileNode> fileTree;
    // code editor
    private CodeArea codeArea;
    private VirtualizedScrollPane<CodeArea> codeAreaContainer;

    // terminal
    private TextArea terminalHistory;
    private TextField terminalConsole;
    private BorderPane terminalContainer;

    // values:

    private boolean savedChanges = true;
    private File currentFile;
    private SyntaxHighlighting syntaxHighlighting;

    // images
    // url, width, height, preserveRatio, smooth
    private final Image imageFolder = new Image("/images/icon-folder.png", 45, 45, true, true);
    private final Image imageFile = new Image("/images/icon-file-document.png", 45, 45, true, true);
    private final Image imageArchive = new Image("/images/icon-archive.png", 45, 45, true, true);
    private final Image imageBinary = new Image("/images/icon-file-binary.png", 45, 45, true, true);
    private final Image imageHex = new Image("/images/icon-file-hex.png", 45, 45, true, true);
    private final Image imageCode = new Image("/images/icon-code.png", 45, 45, true, true);
    private final Image imageCss = new Image("/images/icon-file-css.png", 45, 45, true, true);
    private final Image imageJava = new Image("/images/icon-code-java.png", 45, 45, true, true);
    private final Image imageFolderBurn = new Image("/images/icon-folder-burn.png", 45, 45, true, true);

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
        menuFile = new Menu("File");
        menuView = new Menu("View");
        menuEdit = new Menu("Edit");
        menuRun = new Menu("Run");

        // create menu items
        // file menu
        menuItemFileNew = new MenuItem("New File");
        menuItemFileNewFolder = new MenuItem("New Project");
        menuItemFileSave = new MenuItem("Save");
        menuItemFileSaveAs = new MenuItem("Save As");
        menuItemFileOpenFolder = new MenuItem("Open Folder");

        // view menu
        menuViewFontSizeIncrease = new MenuItem("Increase Font Size");
        menuViewFontSizeDecrease = new MenuItem("Decrease Font Size");

        // edit menu
        menuItemEditUndo = new MenuItem("Undo");

        // run menu
        menuItemRunCompile = new MenuItem("Compile");
        menuItemRunInterpreter = new MenuItem("Interpreter");

        // add menu items to menu
        menuFile.getItems().add(menuItemFileSave);
        menuFile.getItems().add(menuItemFileSaveAs);
        menuFile.getItems().add(menuItemFileOpenFolder);

        menuView.getItems().add(menuViewFontSizeIncrease);
        menuView.getItems().add(menuViewFontSizeDecrease);

        menuEdit.getItems().add(menuItemEditUndo);

        menuRun.getItems().add(menuItemRunCompile);
        menuRun.getItems().add(menuItemRunInterpreter);

        // create a menubar
        menuBar = new MenuBar();

        // add menu to menubar
        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuView);
        menuBar.getMenus().add(menuEdit);
        menuBar.getMenus().add(menuRun);

        terminalHistory = new TextArea("Welcome to moncky IDE : version 0.1.0");
        terminalHistory.setEditable(false);
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
//        KeyCombination increaseFont = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);
//        KeyCombination decreaseFont = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
//
//        KeyCombination saveFile = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);




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



    public void openFolder(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Folder as Project");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            loadFolderIntoTreeView(selectedDirectory);
        }
    }

    public void loadFolderIntoTreeView(File folder) {
        TreeItem<FileNode> rootItem = new TreeItem<>(new FileNode(folder.getName(), folder.getAbsolutePath()));
        rootItem.setExpanded(true);
        addChildrenToTreeItem(rootItem, folder);
        fileTree.setRoot(rootItem);
        terminalHistory.setText(terminalHistory.getText() + "\nOpened folder: " + folder.getName() + " from directory: " + folder.getAbsolutePath());
    }

    private void addChildrenToTreeItem(TreeItem<FileNode> parent, File folder) {

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                Image image;
                if (file.isDirectory()){
                    if (file.getName().equalsIgnoreCase("moncky2out")) image = imageFolderBurn;
                    else image = imageFolder;
                } else {
                    if (file.getName().endsWith(".hex")) image = imageHex;
                    else if (file.getName().endsWith(".bin")) image = imageBinary;
                    else if (file.getName().endsWith(".java")) image = imageJava;
                    else if (file.getName().endsWith(".css")) image = imageCss;
                    else if (file.getName().endsWith(".mck2")) image = imageCode;
                    else image = imageFile;
                }

                ImageView icon = new ImageView(image);
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
            terminalHistory.setText(terminalHistory.getText() + "\nLoaded file: " + file.getName() + " in directory: " + file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load file: " + file.getName());
            alert.showAndWait();
        }
    }

    public void saveEditorContentIntoFile(){
        if (currentFile == null){
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            saveEditorContentAsFile(selectedFile);
        }
        try{
            Files.writeString(currentFile.toPath(), codeArea.getText());
            terminalHistory.setText(terminalHistory.getText() + "\nSaved file: " + currentFile.getName() + " in directory: " + currentFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + currentFile.getName());
            alert.showAndWait();
        }
    }

    public void saveEditorContentAsFile(File selectedFile) {
        if (selectedFile == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + "No file selected");
            alert.showAndWait();
            return;
        }
        try{
            Files.writeString(selectedFile.toPath(), codeArea.getText());
            terminalHistory.setText(terminalHistory.getText() + "\nSaved file: " + selectedFile.getName() + " in directory: " + selectedFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save file: " + selectedFile.getName());
            alert.showAndWait();
        }
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getMenuFile() {
        return menuFile;
    }

    public Menu getMenuView() {
        return menuView;
    }

    public Menu getMenuEdit() {
        return menuEdit;
    }

    public Menu getMenuRun() {
        return menuRun;
    }

    public MenuItem getMenuItemFileNew() {
        return menuItemFileNew;
    }

    public MenuItem getMenuItemFileNewFolder() {
        return menuItemFileNewFolder;
    }

    public MenuItem getMenuItemFileSave() {
        return menuItemFileSave;
    }

    public MenuItem getMenuItemFileSaveAs() {
        return menuItemFileSaveAs;
    }

    public MenuItem getMenuItemFileOpenFolder() {
        return menuItemFileOpenFolder;
    }

    public MenuItem getMenuViewFontSizeIncrease() {
        return menuViewFontSizeIncrease;
    }

    public MenuItem getMenuViewFontSizeDecrease() {
        return menuViewFontSizeDecrease;
    }

    public MenuItem getMenuItemEditUndo() {
        return menuItemEditUndo;
    }

    public MenuItem getMenuItemRunCompile() {
        return menuItemRunCompile;
    }

    public MenuItem getMenuItemRunInterpreter() {
        return menuItemRunInterpreter;
    }

    public TreeView<FileNode> getFileTree() {
        return fileTree;
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public VirtualizedScrollPane<CodeArea> getCodeAreaContainer() {
        return codeAreaContainer;
    }

    public TextArea getTerminalHistory() {
        return terminalHistory;
    }

    public TextField getTerminalConsole() {
        return terminalConsole;
    }

    public BorderPane getTerminalContainer() {
        return terminalContainer;
    }
}
