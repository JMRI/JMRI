package jmri.jmrix;

import org.netbeans.jemmy.operators.*;

import javax.swing.*;

/*
 *  Helper class for operating AbstractMonPane its descendants.
 *
 *  @author Paul Bender Copyright (C) 2016
 */
public class AbstractMonPaneScaffold extends ContainerOperator {

    public AbstractMonPaneScaffold(AbstractMonPane pane) {
        super(pane);
    }

    public void checkTimeStampCheckBox() {
        new JCheckBoxOperator(this, Bundle.getMessage("ButtonShowTimestamps")).clickMouse();
    }

    public boolean getTimeStampCheckBoxValue() {
        return (new JCheckBoxOperator(this, Bundle.getMessage("ButtonShowTimestamps")).isSelected());
    }

    public void checkRawCheckBox() {
        new JCheckBoxOperator(this, Bundle.getMessage("ButtonShowRaw")).clickMouse();
    }

    public boolean getRawCheckBoxValue() {
        return (new JCheckBoxOperator(this, Bundle.getMessage("ButtonShowRaw")).isSelected());
    }

    public void checkOnTopCheckBox() {
        new JCheckBoxOperator(this, Bundle.getMessage("ButtonWindowOnTop")).clickMouse();
    }

    public boolean getOnTopCheckBoxValue() {
        return (new JCheckBoxOperator(this, Bundle.getMessage("ButtonWindowOnTop")).isSelected());
    }

    public void checkAutoScrollCheckBox() {
        new JCheckBoxOperator(this, Bundle.getMessage("ButtonAutoScroll")).clickMouse();
    }

    public boolean getAutoScrollCheckBoxValue() {
        return (new JCheckBoxOperator(this, Bundle.getMessage("ButtonAutoScroll")).isSelected());
    }

    public void clickFreezeButton() {
        new JToggleButtonOperator(this, Bundle.getMessage("ButtonFreezeScreen")).clickMouse();
    }

    public boolean getFreezeButtonState() {
        return (new JToggleButtonOperator(this, Bundle.getMessage("ButtonFreezeScreen")).isSelected());
    }

    public void clickEnterButton() {
        new JButtonOperator(this, Bundle.getMessage("ButtonAddMessage")).clickMouse();
    }

    public void enterTextInEntryField(String text) {
        // there is no label on the entry field, so we use the index of the field.
        // the index may need to change if more text fields are added to the pane.
        new JTextFieldOperator(this, 1).typeText(text);
    }

    public String getEntryFieldText() {
        // there is no label on the entry field, so we use the index of the field.
        // the index may need to change if more text fields are added to the pane.
        return new JTextFieldOperator(this, 1).getText();
    }

    public void enterTextInFilterField(String text) {
        JLabelOperator jlo = new JLabelOperator(this, Bundle.getMessage("LabelFilterBytes"));
        new JTextFieldOperator(((JTextField) jlo.getLabelFor())).typeText(text);
    }

    public String getFilterFieldText() {
        JLabelOperator jlo = new JLabelOperator(this, Bundle.getMessage("LabelFilterBytes"));
        return new JTextFieldOperator(((JTextField) jlo.getLabelFor())).getText();
    }

}
