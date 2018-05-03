package org.vaadin.addons.md_stepper.demo.steps;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import org.vaadin.addons.md_stepper.Step;

public class Step2 extends Step {

  public Step2() {
    super(true); // Use default actions

    VerticalLayout content = new VerticalLayout();
    content.setWidth(100, Sizeable.Unit.PERCENTAGE);
    content.setSpacing(true);
    content.setMargin(true);

    Label stepAttributesTitle = new Label("Step Attributes");
    stepAttributesTitle.addStyleName(ValoTheme.LABEL_H2);
    Label stepAttributesLabel = new Label("You can change various attributes on a single step:" +
                                          "<ul>" +
                                          "<li>caption</li>" +
                                          "<li>description</li>" +
                                          "<li>icon</li>" +
                                          "<li>optional (to be able to skip it)</li>" +
                                          "<li>editable (come back if skipped or next, show edit " +
                                          "icon)" +
                                          "</li>" +
                                          "</ul>", ContentMode.HTML);

    TextField textField = new TextField("Enter any value");

    content.addComponent(stepAttributesTitle);
    content.addComponent(stepAttributesLabel);
    content.addComponent(textField);
    content.iterator().forEachRemaining(c -> c.setWidth(100, Unit.PERCENTAGE));

    /*addStepCompleteListener(event -> {
      event.getSource().getSteps().get(2).setDisabled(textField.getValue().equals(""));
      event.getSource().refresh();
    });*/



    setCaption("Step 2");
    setDescription("Step Attributes");
    setContent(content);
    setEditable(true);
  }
}
