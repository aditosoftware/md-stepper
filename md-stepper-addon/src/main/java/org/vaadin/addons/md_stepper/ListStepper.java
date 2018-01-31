package org.vaadin.addons.md_stepper;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import org.vaadin.addons.md_stepper.collection.ElementAddListener;
import org.vaadin.addons.md_stepper.collection.ElementRemoveListener;
import org.vaadin.addons.md_stepper.component.Spacer;
import org.vaadin.addons.md_stepper.component.Spinner;
import org.vaadin.addons.md_stepper.event.StepperCompleteListener;
import org.vaadin.addons.md_stepper.util.SerializableSupplier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stepper implementation that shows the steps in a vertical style.
 */
public class ListStepper extends AbstractStepper
        implements ElementAddListener<Step>, ElementRemoveListener<Step>, StepperCompleteListener {

    private static final String STYLE_ROOT_LAYOUT = "stepper-custom-vertical";
    private static final String STYLE_STEPSBAR = "stepper-custom-vertical-stepsbar";

    private final HorizontalLayout rootLayout;
    private final VerticalLayout stepsListLayout;
    private final Panel stepContentLayout;

    private final Map<Step, StepEntry> stepEntryMap;


    private Spacer spacer;

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

        addStepperCompleteListener(this);
        getStepIterator().addElementAddListener(this);
        getStepIterator().addElementRemoveListener(this);

        this.stepEntryMap = new HashMap<>();

        this.rootLayout = new HorizontalLayout();
        this.rootLayout.setSizeFull();
        this.rootLayout.setMargin(false);
        this.rootLayout.setSpacing(false);

        // (left) Steps list
        this.stepsListLayout = new VerticalLayout();
        this.stepsListLayout.setMargin(false);
        this.stepsListLayout.setSpacing(false);
        this.stepsListLayout.addStyleName(STYLE_STEPSBAR);
        this.stepsListLayout.setWidth(20, Unit.REM);
        // Ad it to the root layout
        this.rootLayout.addComponent(this.stepsListLayout);

        // (right) Steps content
        this.stepContentLayout = new Panel();
        this.stepContentLayout.setSizeFull();
        this.stepContentLayout.addStyleName(ValoTheme.PANEL_BORDERLESS);
        this.stepContentLayout.setHeight(100, Unit.PERCENTAGE);
        // Add it to the root layout
        this.rootLayout.addComponent(this.stepContentLayout);
        this.rootLayout.setExpandRatio(this.stepContentLayout, 1);



        setCompositionRoot(rootLayout);
        addStyleName(STYLE_ROOT_LAYOUT);
        setSizeFull();
        refreshLayout();
    }

    private void refreshLayout() {
        this.stepsListLayout.removeAllComponents();
        this.stepEntryMap.clear();

        List<Step> steps = getSteps();
        steps.forEach(step -> {
            // Create a new StepEntry for each step
            StepEntry entry = new StepEntry(step, this.stepContentLayout);
            stepEntryMap.put(step, entry);

            // Add it to the steps list
            this.stepsListLayout.addComponent(entry);
        });

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
    public ListStepper(List<Step> steps, boolean linear,
                       SerializableSupplier<StepLabel> labelFactory) {
        this(new StepIterator(steps, linear), labelFactory);
    }

    @Override
    public void onStepperComplete(StepperCompleteEvent event) {
        //rowMap.values().forEach(r -> r.onStepperComplete(event));
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

        this.stepEntryMap.get(getCurrent()).showTransitionMessage(message);
    }

    @Override
    protected void setActive(Step step, Step previousStep, boolean fireEvent) {
        this.stepEntryMap.entrySet().forEach(entry -> {
           if (Objects.equals(entry.getKey(), step)) {
               entry.getValue().setActive(true);
           } else {
               entry.getValue().setActive(false);
           }
        });

        super.setActive(step, previousStep, fireEvent);
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

    private class StepEntry extends CustomComponent {
        private final HorizontalLayout listeEntryLayout;
        private final Panel stepContainerLayout;
        private final Step step;
        private final String STYLE_BUTTON_CONTAINER = "step-button-container";

        private boolean entryActive = false;

        public StepEntry (Step pStep, Panel stepContainer) {
            this.step = pStep;
            this.listeEntryLayout = new HorizontalLayout();
            this.stepContainerLayout = stepContainer;

            listeEntryLayout.addComponent(getLabelProvider().getStepLabel(pStep));
            listeEntryLayout.setWidth(100, Unit.PERCENTAGE);

            this.setActive(false);
            setCompositionRoot(listeEntryLayout);
        }

        private VerticalLayout getCombinedStepContent () {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSizeFull();
            verticalLayout.setSpacing(false);
            verticalLayout.setMargin(false);

            Panel contentPanel = new Panel();
            step.getContent().setHeightUndefined();
            contentPanel.setContent(step.getContent());
            contentPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
            verticalLayout.addComponent(contentPanel);

            Spacer.addToLayout(verticalLayout);

            HorizontalLayout buttonContainer = new HorizontalLayout();
            buttonContainer.setWidth(100, Unit.PERCENTAGE);
            buttonContainer.addStyleName(STYLE_BUTTON_CONTAINER);
            buttonContainer.setMargin(true);
            verticalLayout.addComponent(buttonContainer);
            buttonContainer.addComponent(step.getNextButton());
            if (step.isOptional()) {
                buttonContainer.addComponent(step.getSkipButton());
            }
            Spacer.addToLayout(buttonContainer);
            if (!isFirstStep(this.step)) buttonContainer.addComponent(step.getBackButton());


            return verticalLayout;
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

        private boolean isLastStep(Step step) {
            return getSteps().indexOf(step) == getSteps().size() - 1;
        }

        private boolean isFirstStep (Step pStep) {
            return getSteps().indexOf(pStep) == 0;
        }

        public void setActive (boolean isActive) {
            System.out.println("setActive " + isActive);
            this.entryActive = isActive;

            if (isActive) {
                this.stepContainerLayout.setContent(this.getCombinedStepContent());
            }
        }

        public void showTransitionMessage (String message) {
            System.out.println(message);
            if (message == null)
                this.setActive(true);
            else
                this.stepContainerLayout.setContent(this.getLoadingContainer(message));
        }
    }
}
