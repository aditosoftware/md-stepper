package org.vaadin.addons.md_stepper;

/**
 * @author A.Loreth, 04.05.2018
 */
public enum ELabelIconStrategy
{
  DEFAULT (true, true, true),
  NUMBERS_ONLY (false, false, false)
  ;

  private boolean allowNexted;
  private boolean allowSkipped;
  private boolean allowEditable;

  ELabelIconStrategy (boolean pAllowNexted, boolean pAllowSkipped, boolean pAllowEditable) {
    allowNexted = pAllowNexted;
    allowSkipped = pAllowSkipped;
    allowEditable = pAllowEditable;
  }

  public boolean isAllowNexted()
  {
    return allowNexted;
  }

  public boolean isAllowSkipped()
  {
    return allowSkipped;
  }

  public boolean isAllowEditable()
  {
    return allowEditable;
  }
}
