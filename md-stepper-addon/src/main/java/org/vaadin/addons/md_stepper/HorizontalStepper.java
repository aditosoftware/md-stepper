package org.vaadin.addons.md_stepper;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.addons.md_stepper.collection.*;
import org.vaadin.addons.md_stepper.component.*;
import org.vaadin.addons.md_stepper.event.StepperCompleteListener;
import org.vaadin.addons.md_stepper.util.SerializableSupplier;

import java.util.*;

/**
 * Stepper implementation that show th steps in a horizontal style.
 */
public class HorizontalStepper extends AbstractStepper
    implements ElementAddListener<Step>, ElementRemoveListener<Step>, StepperCompleteListener
{

  public static final float DEFAULT_EXPAND_RATIO_DIVIDER = 0.75F;

  private static final String STYLE_ROOT_LAYOUT = "stepper-horizontal";
  private static final String STYLE_LABEL_BAR = "label-bar";
  private static final String STYLE_DIVIDER = "label-divider";
  private static final String STYLE_FEEDBACK_MESSAGE = "feedback-message";
  private static final String STYLE_CONTENT_CONTAINER = "content-container";
  private static final String STYLE_BUTTON_BAR = "button-bar";

  private final HorizontalLayout labelBar;
  private final HorizontalLayout buttonBar;
  private final Panel stepContent;

  private float dividerExpandRatio;



  /**
   * Create a new horizontal stepper using the given iterator and label change handler.
   *
   * @param stepIterator  The iterator that handles the iteration over the given steps
   * @param labelProvider The handler that handles changes to labels
   */
  public HorizontalStepper(StepIterator stepIterator, LabelProvider labelProvider)
  {
    super(stepIterator, labelProvider);

    addStepperCompleteListener(this);
    getStepIterator().addElementAddListener(this);
    getStepIterator().addElementRemoveListener(this);

    this.labelBar = new HorizontalLayout();
    this.labelBar.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
    this.labelBar.setWidth(100, Unit.PERCENTAGE);
    this.labelBar.addStyleName(STYLE_LABEL_BAR);

    this.buttonBar = new HorizontalLayout();
    this.buttonBar.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
    this.buttonBar.addStyleName(STYLE_BUTTON_BAR);
    this.buttonBar.setWidth(100, Unit.PERCENTAGE);
    this.buttonBar.setSpacing(true);

    this.stepContent = new Panel();
    this.stepContent.addStyleName(ValoTheme.PANEL_BORDERLESS);
    this.stepContent.addStyleName(STYLE_CONTENT_CONTAINER);
    this.stepContent.setSizeFull();

    this.dividerExpandRatio = DEFAULT_EXPAND_RATIO_DIVIDER;

    VerticalLayout rootLayout = new VerticalLayout();
    rootLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
    rootLayout.setSizeFull();
    rootLayout.setSpacing(true);
    rootLayout.addComponent(labelBar);
    rootLayout.addComponent(stepContent);
    rootLayout.addComponent(buttonBar);
    rootLayout.setExpandRatio(stepContent, 1);

    setCompositionRoot(rootLayout);
    addStyleName(STYLE_ROOT_LAYOUT);
    setSizeFull();
    refreshLabelBar();
  }

  private void refreshLabelBar()
  {
    labelBar.removeAllComponents();

    List<Step> steps = getSteps();
    for (int i = 0; i < steps.size(); i++)
    {
      addStepLabel(steps.get(i));
      if (i < steps.size() - 1)
      {
        addStepLabelDivider();
      }
    }

    labelBar.iterator().forEachRemaining(c -> c.setWidth(100, Unit.PERCENTAGE));
  }

  private void addStepLabel(Step step)
  {
    Component stepLabel = getLabelProvider().getStepLabel(step);

    labelBar.addComponent(stepLabel);
    labelBar.setExpandRatio(stepLabel, 1);
  }

  private void addStepLabelDivider()
  {
    CssLayout divider = new CssLayout();
    divider.addStyleName(STYLE_DIVIDER);

    labelBar.addComponent(divider);
    labelBar.setExpandRatio(divider, getDividerExpandRatio());
  }

  /**
   * Get the expand ratio for the divider between the labels.
   *
   * @return The expand ratio
   */
  public float getDividerExpandRatio()
  {
    return dividerExpandRatio;
  }

  /**
   * Set the expand ratio of the dividers between the labels (labels have an expand ratio of
   * <code><b>1</b></code>).
   *
   * @param dividerExpandRatio The expand ratio for the dividers
   */
  public void setDividerExpandRatio(float dividerExpandRatio)
  {
    this.dividerExpandRatio = dividerExpandRatio;
    refreshLabelBar();
  }

  @Override
  public void onStepperComplete(StepperCompleteEvent event)
  {
    buttonBar.forEach(b -> b.setVisible(false));
  }

  @Override
  public void onElementAdd(ElementAddEvent<Step> event)
  {
    refresh();
  }

  @Override
  public void refresh()
  {
    super.refresh();
    refreshLabelBar();
    setActive(getCurrent(), getCurrent(), false);
  }

  @Override
  public void showFeedbackMessage(String message)
  {
    super.showFeedbackMessage(message);

    if (message == null)
    {
      refreshLabelBar();
      setActive(getCurrent(), getCurrent(), false);
    }
    else
    {
      buttonBar.forEach(button -> button.setVisible(false));
      showTransitionLabel(message);
      showSpinner();
    }
  }

  private void showTransitionLabel(String message)
  {
    Label feedbackLabel = new Label(message);
    feedbackLabel.addStyleName(STYLE_FEEDBACK_MESSAGE);

    labelBar.removeAllComponents();
    labelBar.addComponent(feedbackLabel);
  }

  private void showSpinner()
  {
    stepContent.setContent(new CenteredLayout(new Spinner()));
  }

  @Override
  protected void setActive(Step step, Step previousStep, boolean fireEvent)
  {
    stepContent.setContent(step.getContent());
    refreshButtonBar(step);

    super.setActive(step, previousStep, fireEvent);
  }

  @Override
  protected void setHideButtons(boolean pHideButtons)
  {
    buttonBar.forEach(b -> b.setVisible(!pHideButtons));
  }

  private void refreshButtonBar(Step step)
  {
    buttonBar.removeAllComponents();
    buttonBar.setVisible(true);

    if (step == null)
    {
      return;
    }

    Button backButton = step.getBackButton();
    Button cancelButton = step.getCancelButton();
    Button skipButton = step.getSkipButton();
    Button nextButton = step.getNextButton();

    backButton.setVisible(getStepIterator().hasPrevious());
    cancelButton.setVisible(step.isCancellable());
    skipButton.setVisible(step.isOptional());
    nextButton.setVisible(!isComplete());

    buttonBar.addComponent(backButton);
    Spacer.addToLayout(buttonBar);
    buttonBar.addComponent(cancelButton);
    buttonBar.addComponent(skipButton);
    buttonBar.addComponent(nextButton);
  }

  @Override
  public void onElementRemove(ElementRemoveEvent<Step> event)
  {
    refresh();
  }

  /**
   * Styles for the horizontal stepper.
   */
  public static final class Styles
  {

    /**
     * Show the stepper in a borderless style.
     */
    public static final String STEPPER_BORDERLESS = "borderless";
    /**
     * Do not show the divider between the label bar and the content
     */
    public static final String STEPPER_NO_DIVIDER = "no-divider";

    private Styles()
    {
      // Prevent instantiation
    }
  }
}
