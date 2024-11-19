package ide.view;


import ide.model.Model;

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
    }

    private void updateView() {
        // fills the view with model data
    }
}