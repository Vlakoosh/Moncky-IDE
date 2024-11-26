package ide.view;


import ide.model.Model;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.CodeArea;

public class Presenter {
    private Model model;
    private View view;

    public Presenter(
            Model model, View view) {
        this.model = model;
        this.view = view;
        addEventHandlers();
        updateView();
    }

    private void addEventHandlers() {
        // Add event handlers (inner classes or
        // lambdas) to view controls.
        // In the event handlers: call model methods
        // and updateView().


        // increase font with key combination
        view.getMenuViewFontSizeIncrease().setAccelerator(KeyCombination.keyCombination("Ctrl+."));
        view.getMenuViewFontSizeIncrease().setOnAction(e -> adjustFontSize(view.getCodeArea(), 1));

        // decrease font with key combination
        view.getMenuViewFontSizeDecrease().setAccelerator(KeyCombination.keyCombination("Ctrl+,"));
        view.getMenuViewFontSizeDecrease().setOnAction(e -> adjustFontSize(view.getCodeArea(), -1));

        // save file
        view.getMenuItemFileSave().setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        view.getMenuItemFileSave().setOnAction(e -> view.saveEditorContentIntoFile());

        // open a folder as a project
        view.getMenuItemFileOpenFolder().setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        view.getMenuItemFileOpenFolder().setOnAction(e -> view.openFolder(view.getPrimaryStage()));


        view.getMenuItemFileSaveAs().setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        view.getMenuItemFileSaveAs().setOnAction(e -> {
            FileChooser fileChooser = new FileChooser(); //TODO move this FileChooser
            view.saveEditorContentAsFile(fileChooser.showSaveDialog(view.getPrimaryStage()));
        });

    }

    private void updateView() {
        // fills the view with model data
    }

    public void adjustFontSize(CodeArea codeArea, int delta) {
        model.setFontSize(Math.max(10, model.getFontSize() + delta)); // Minimum font size is 10
        codeArea.setStyle(String.format("-fx-font-size: %.1fpx !important;", model.getFontSize()));
    }


}