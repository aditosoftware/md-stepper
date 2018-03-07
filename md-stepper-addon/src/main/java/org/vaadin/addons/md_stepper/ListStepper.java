package org.vaadin.addons.md_stepper;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import org.vaadin.addons.md_stepper.collection.ElementAddListener;
import org.vaadin.addons.md_stepper.collection.ElementRemoveListener;
import org.vaadin.addons.md_stepper.component.Spacer;
import org.vaadin.addons.md_stepper.component.Spinner;
import org.vaadin.addons.md_stepper.event.StepperCompleteListener;
import org.vaadin.addons.md_stepper.util.SerializableSupplier;

import java.util.List;

/**
 * Stepper implementation that shows the steps in a vertical style.
 */
public class ListStepper extends AbstractStepper
        implements ElementAddListener<Step>, ElementRemoveListener<Step>, StepperCompleteListener {

    private static final String STYLE_ROOT_LAYOUT = "stepper-list";
    private static final String STYLE_STEPSBAR = "stepper-list-stepsbar";

    private HorizontalLayout rootStepperLayout;
    private VerticalLayout stepsListLayout;
    private Panel stepsContentPanel;

    /**
     * Create a new linear, vertical stepper for the given steps using a {@link StepIterator}.
     *
     * @param steps
     *     The steps to show
     */
    public ListStepper(List<Step> steps) {
        this(steps, true);
    }

    /**
     * Create a new vertical stepper for the given steps using a {@link StepIterator}.
     *
     * @param steps
     *     The steps to show
     * @param linear
     *     <code>true</code> if the state rule should be linear, <code>false</code> else
     */
    public ListStepper(List<Step> steps, boolean linear) {
        this(new StepIterator(steps, linear), StepLabel::new);
    }

    /**
     * Create a new linear, vertical stepper for the given steps using a {@link StepIterator}.
     *
     * @param steps
     *     The steps to show
     * @param labelFactory
     *     The factory used to create new labels for the steps
     */
    public ListStepper(List<Step> steps, SerializableSupplier<StepLabel> labelFactory) {
        this(steps, true, labelFactory);
    }

    /**
     * Create a new vertical stepper for the given steps using a {@link StepIterator}.
     *
     * @param steps
     *     The steps to show
     * @param linear
     *     <code>true</code> if the state rule should be linear, <code>false</code> else
     * @param labelFactory
     *     The factory used to create new labels for the steps
     */
    public ListStepper(List<Step> steps, boolean linear, SerializableSupplier<StepLabel> labelFactory) {
        this(new StepIterator(steps, linear), labelFactory);
    }

    /**
     * Create a new vertical stepper fusing the given iterator.
     *
     * @param stepIterator
     *     The iterator that handles the iteration over the given steps
     * @param labelFactory
     *     The label factory to build step labels
     */
    private ListStepper(StepIterator stepIterator, SerializableSupplier<StepLabel> labelFactory) {
        this(stepIterator, new LabelProvider(stepIterator, labelFactory));
    }

    /**
     * Create a new vertical stepper using the given iterator and label change handler.
     *
     * @param stepIterator
     *     The iterator that handles the iteration over the given steps
     * @param labelProvider
     *     The handler that handles changes to labels
     */
    private ListStepper(StepIterator stepIterator, LabelProvider labelProvider) {
        super(stepIterator, labelProvider);

        this.addStepperCompleteListener(this);
        this.getStepIterator().addElementAddListener(this);
        this.getStepIterator().addElementRemoveListener(this);

        // Build the Layout
        this._buildRootLayout();

        this.setCompositionRoot(this.rootStepperLayout);
        this.addStyleName(STYLE_ROOT_LAYOUT);
        this.setSizeFull();
        this.refreshLayout();
    }

    private void _buildRootLayout () {
        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setSizeFull();
        rootLayout.setMargin(false);
        rootLayout.setSpacing(false);

        // Left steps list
        VerticalLayout stepsListLayout = new VerticalLayout();
        stepsListLayout.setMargin(false);
        stepsListLayout.setSpacing(false);
        stepsListLayout.addStyleName(STYLE_STEPSBAR);
        stepsListLayout.setWidth(20, Unit.REM);

        rootLayout.addComponent(stepsListLayout);

        Panel stepContentPanel = new Panel();
        stepContentPanel.setSizeFull();
        stepContentPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
        stepContentPanel.setHeight(100, Unit.PERCENTAGE);

        rootLayout.addComponentsAndExpand(stepContentPanel);
        rootLayout.setExpandRatio(stepContentPanel, 1);

        this.rootStepperLayout = rootLayout;
        this.stepsListLayout = stepsListLayout;
        this.stepsContentPanel = stepContentPanel;
    }

    private void refreshLayout() {
        this.stepsListLayout.removeAllComponents();

        List<Step> steps = getSteps();
        steps.forEach(step -> {
            // Add it to the steps list
            this.stepsListLayout.addComponent(getLabelProvider().getStepLabel(step));
        });

    }

    @Override
    public void onStepperComplete(StepperCompleteEvent event) {
        ((StepContent) this.stepsContentPanel.getContent()).setHideButtons(true);
    }

    @Override
    public void onElementAdd(ElementAddEvent<Step> event) {
        refresh();
    }

    @Override
    public void refresh() {
        super.refresh();
        refreshLayout();

        setActive(getCurrent(), getCurrent(), false);
    }

    @Override
    public void showFeedbackMessage(String message) {
        super.showFeedbackMessage(message);

        ((StepContent)this.stepsContentPanel.getContent()).setLoadingIndicator(message);
    }

    @Override
    protected void setActive(Step step, Step previousStep, boolean fireEvent) {
        super.setActive(step, previousStep, fireEvent);

        this.stepsContentPanel.setContent(new StepContent(step));
    }

    @Override
    public void onElementRemove(ElementRemoveEvent<Step> event) {
        refresh();
    }

    /**
     * Styles for the vertical stepper.
     */
    public static final class Styles {

        /**
         * Show the stepper in a borderless style.
         */
        public static final String STEPPER_BORDERLESS = "borderless";

        private Styles() {
            // Prevent instantiation
        }
    }

    public class StepContent extends VerticalLayout {
        private final Step step;

        private String loadingIndicator = null;
        private boolean hideButtons = false;

        private final String STYLE_BUTTON_CONTAINER = "step-button-container";

        public StepContent (Step pStep) {
            this.step = pStep;

            this.setSizeFull();
            this.setSpacing(false);
            this.setMargin(false);

            this.buildLayout();
        }

        public void setHideButtons (boolean pHideButtons) {
            this.hideButtons = pHideButtons;
            this.buildLayout();
        }

        public void setLoadingIndicator (String message) {
            this.loadingIndicator = message;
            this.buildLayout();
        }

        private void buildLayout () {
            this.removeAllComponents();
            if (this.loadingIndicator == null) {
                this.addComponentsAndExpand(this.getMainPanel());
                if (!this.hideButtons) this.addComponent(this.getButtonPanel());
            } else {
                this.addComponent(this.getLoadingContainer(this.loadingIndicator));
            }
        }

        private boolean isFirstStep (Step pStep) {
            return getSteps().indexOf(pStep) == 0;
        }

        private Panel getMainPanel () {
            Panel contentPanel = new Panel();
            this.step.getContent().setHeightUndefined();
            contentPanel.setContent(this.step.getContent());
            contentPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
            return contentPanel;
        }

        private HorizontalLayout getButtonPanel () {
            HorizontalLayout buttonPanel = new HorizontalLayout();
            buttonPanel.addStyleName(STYLE_BUTTON_CONTAINER);
            buttonPanel.setMargin(true);
            buttonPanel.addComponent(this.step.getBackButton());
            this.step.getBackButton().setEnabled(!isFirstStep(this.step) && getStepIterator().hasPrevious());
            if (step.isOptional())
                buttonPanel.addComponent(this.step.getSkipButton());

            buttonPanel.addComponent(this.step.getNextButton());

            return buttonPanel;
        }

        private AbstractLayout getLoadingContainer (String loadingMessage) {
            VerticalLayout loadingContainer = new VerticalLayout();
            VerticalLayout subLayout = new VerticalLayout();

            loadingContainer.setSizeFull();
            subLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
            subLayout.setSpacing(false);
            subLayout.setMargin(false);

            Spinner loadingSpinner = new Spinner();
            Label feedbackMessage = new Label(loadingMessage);

            subLayout.addComponent(loadingSpinner);
            subLayout.addComponent(feedbackMessage);

            loadingContainer.addComponent(subLayout);
            loadingContainer.setComponentAlignment(subLayout, Alignment.MIDDLE_CENTER);

            return loadingContainer;
        }
    }
}
