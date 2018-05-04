package org.vaadin.addons.md_stepper;

import org.vaadin.addons.md_stepper.collection.*;
import org.vaadin.addons.md_stepper.collection.ElementAddListener.ElementAddEvent;
import org.vaadin.addons.md_stepper.collection.ElementRemoveListener.ElementRemoveEvent;
import org.vaadin.addons.md_stepper.event.*;
import org.vaadin.addons.md_stepper.iterator.*;
import org.vaadin.addons.md_stepper.state.StateTracker;
import org.vaadin.addons.md_stepper.state.StateTracker.State;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Iterator that is used to iterate over steps allowing iterations based upon the steps attributes.
 */
public class StepIterator extends AbstractObservableIterator<Step> implements CollectionChangeNotifier<Step>, StepCompleteListener, StepResetListener
{

  private final Collection<ElementAddListener<Step>> elementAddListeners;
  private final Collection<ElementRemoveListener<Step>> elementRemoveListeners;

  private final List<Step> steps;
  private final StateTracker<Step> stateTracker;

  private boolean linear;
  private Step current;
  private Step startAt;
  private boolean readOnly = false;
  private boolean locked = false;

  /**
   * Create a new, non-linear iterator with an empty step list.
   */
  public StepIterator()
  {
    this(false);
  }

  /**
   * Create a new iterator for the given linearity with an empty step list.
   *
   * @param linear <code>true</code> if the iterator should be linear, <code>false</code> else
   */
  public StepIterator(boolean linear)
  {
    this(new ArrayList<>(), linear, null);
  }

  /**
   * Create a new iterator for the given steps and linearity.
   *
   * @param steps  The steps to add to the iterator
   * @param linear <code>true</code> if the iterator should be linear, <code>false</code> else
   */
  public StepIterator(List<Step> steps, boolean linear, Step startAt)
  {
    this.elementAddListeners = new HashSet<>();
    this.elementRemoveListeners = new HashSet<>();

    this.steps = new ArrayList<>();
    this.stateTracker = new StateTracker<>();

    this.linear = linear;

    this.startAt = startAt;

    steps.forEach(this::add);
  }

  /**
   * Create a new, non-linear iterator for the given steps.
   *
   * @param steps The steps to add to the iterator
   */
  public StepIterator(List<Step> steps)
  {
    this(steps, false, null);
  }

  /**
   * Get the linearity of the iterator.
   *
   * @return <code>true</code> if the iterator is linear, <code>false</code> else
   */
  public boolean isLinear()
  {
    return linear;
  }

  /**
   * Set the iterators linearity.
   *
   * @param linear <code>true</code> if the iterator should be linear, <code>false</code> else
   */
  public void setLinear(boolean linear)
  {
    this.linear = linear;
  }

  /**
   * Get the steps of this step iterator.
   *
   * @return The steps
   */
  public List<Step> getSteps()
  {
    return Collections.unmodifiableList(steps);
  }

  /**
   * Get the current step.
   *
   * @return The current step or <code>null</code> if no current step exists
   */
  public Step getCurrent()
  {
    return current;
  }

  protected boolean isTransitionAllowed(Step to)
  {
    return isTransitionAllowed(to, false);
  }

  protected boolean isTransitionAllowed(Step to, boolean currentShouldBeComplete)
  {
    if (to == null)
    {
      return true;
    }

    if (to.isDisabled())
    {
      return false;
    }

    if (!steps.contains(to) || Objects.equals(current, to) || isComplete())
    {
      return false;
    }

    if (stateTracker.getState(to) == State.VISITED && to.isEditable())
    {
      return true;
    }

    List<Step> openSteps = steps.stream().filter(s -> currentShouldBeComplete || !Objects.equals(s, current)).filter(s -> stateTracker.getState(s) == State.UNVISITED).filter(s -> !s.isDisabled()).collect(Collectors.toList());

    if (linear)
    {
      return openSteps.indexOf(to) == 0;
    }
    else
    {
      return openSteps.contains(to);
    }
  }

  /**
   * Check if the iterator is complete and all steps have been visited.
   *
   * @return <code>true</code> if the iterator is complete, <code>false</code> else
   */
  public boolean isComplete()
  {
    return steps.stream().map(stateTracker::getState).noneMatch(state -> state == State.UNVISITED);
  }

  /**
   * Check if the given step is complete.
   *
   * @param step The step to check
   * @return <code>true</code> if the step is complete, <code>false</code> else
   */
  public boolean isStepComplete(Step step)
  {
    return stateTracker.getState(step) == State.VISITED;
  }

  @Override
  public void onStepComplete(StepCompleteEvent event)
  {
    stateTracker.setState(event.getStep(), State.VISITED);
  }

  @Override
  public void onStepReset(StepResetEvent event)
  {
    stateTracker.setState(event.getStep(), State.UNVISITED);
  }

  @Override
  public boolean addElementAddListener(ElementAddListener<Step> listener)
  {
    return elementAddListeners.add(listener);
  }

  @Override
  public boolean removeElementAddListener(ElementAddListener<Step> listener)
  {
    return elementAddListeners.remove(listener);
  }

  @Override
  public boolean addElementRemoveListener(ElementRemoveListener<Step> listener)
  {
    return elementRemoveListeners.add(listener);
  }

  @Override
  public boolean removeElementRemoveListener(ElementRemoveListener<Step> listener)
  {
    return elementRemoveListeners.remove(listener);
  }

  @Override
  public void moveTo(Step element)
  {

    if (!hasMoveTo(element))
    {
      throw new NoSuchElementException();
    }

    if (readOnly && element != null)
      return;

    Step tmp = current;
    current = element;
    notifyMoveTo(tmp, current);
  }

  @Override
  public boolean hasMoveTo(Step element)
  {
    return isTransitionAllowed(element, linear);
  }

  @Override
  public Step previous()
  {
    if (!hasPrevious())
    {
      throw new NoSuchElementException();
    }

    if (readOnly)
      return current;

    Step tmp = current;
    current = steps.get(previousIndex());
    notifyPrevious(tmp, current);

    return current;
  }

  @Override
  public boolean hasPrevious()
  {
    return previousIndex() >= 0;
  }

  @Override
  public int previousIndex()
  {
    return steps.stream().limit(current != null ? steps.indexOf(current) : 0).filter(this::isTransitionAllowed).reduce((first, second) -> second).map(steps::indexOf).orElse(-1);
  }

  /**
   * Removes the current step and moves on to the next step.
   */
  @Override
  public void remove()
  {
    stateTracker.remove(current);
    steps.remove(current);

    ElementRemoveEvent<Step> event = new ElementRemoveEvent<>(steps, current);
    elementRemoveListeners.forEach(l -> l.onElementRemove(event));

    moveTo(steps.get(nextIndex()));
  }

  @Override
  public void set(Step step)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(Step step)
  {
    Integer insertIndex = Optional.ofNullable(current).map(s -> steps.indexOf(current) + 1).orElse(steps.size());
    steps.add(insertIndex, step);
    step.addStepCompleteListener(this);
    step.addStepResetListener(this);

    ElementAddEvent<Step> event = new ElementAddEvent<>(steps, step);
    elementAddListeners.forEach(l -> l.onElementAdd(event));
  }

  @Override
  public Step next()
  {
    if (!hasNext())
    {
      throw new NoSuchElementException();
    }
    if (readOnly)
      return current;

    Step tmp = current;
    if (steps.size() != 0)
    {
      current = steps.get(nextIndex());
    }

    if (tmp == null)
    {
      notifyStart(new NextListener.NextEvent<>(this, null, current));
    }

    notifyNext(tmp, current);

    if (!hasSkip() && !hasNext())
    {
      notifyEnd(new NextListener.NextEvent<>(this, tmp, current));
    }

    return current;
  }

  @Override
  public boolean hasNext()
  {
    return nextIndex() >= 0;
  }

  @Override
  public int nextIndex()
  {
    if (current == null)
    {
      return 0;
    }

    Step possibleNextStep = steps
        .stream()
        .filter(s -> isTransitionAllowed(s) || Objects.equals(s, current))
        .filter(s -> stateTracker.getState(s) == State.UNVISITED)
        .findFirst().orElse(null);

    // There are no unfinished steps.
    if (possibleNextStep == null)
      return -1;

    // There are undone steps


    // Return simply the next step
    return steps.indexOf(possibleNextStep);
  }

  @Override
  public boolean hasSkip()
  {
    return current != null && current.isOptional() && hasNext();
  }

  @Override
  public Step skip()
  {
    if (!hasSkip())
    {
      throw new NoSuchElementException();
    }

    if (readOnly)
      return current;

    Step tmp = current;
    current = steps.get(nextIndex());

    if (tmp == null)
    {
      notifyStart(new SkipListener.SkipEvent<>(this, null, current));
    }

    notifySkip(tmp, current);

    if (!hasSkip() && !hasNext())
    {
      notifyEnd(new SkipListener.SkipEvent<>(this, tmp, current));
    }

    return current;
  }

  public void resetStep(Step pStep)
  {
    stateTracker.setState(pStep, State.UNVISITED);
  }

  public void setStepVisited(Step pStep)
  {
    stateTracker.setState(pStep, State.VISITED);
  }

  public Step getLastVisitableStep()
  {
    List<Step> stepsList = steps.stream().filter(e -> !e.isDisabled()).collect(Collectors.toList());
    return stepsList.get(stepsList.size() - 1);
  }

  public Step getStartAt()
  {
    return startAt;
  }

  public void setReadOnly(boolean pReadOnly)
  {
    readOnly = pReadOnly;
  }

  public void setLocked (boolean pLocked) {
    locked = pLocked;
  }

  public boolean isLocked () {
    return locked;
  }
}
