package org.vaadin.addons.md_stepper.component;

import com.vaadin.ui.*;

public class CenteredLayout extends CssLayout
{

  public CenteredLayout(Component... components)
  {
    super(components);
    setSizeFull();
    addStyleName("centered-layout");
  }
}
