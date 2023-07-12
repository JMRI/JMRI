package jmri.jmrit.operations.automation;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for user selection of a startup automation
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dan Boudreau Copyright (C) 2022
 */
public class AutomationStartupFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    AutomationManager automationManager = InstanceManager.getDefault(AutomationManager.class);

    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton testButton = new JButton(Bundle.getMessage("ButtonTest"));
    
    JComboBox<Automation> comboBox = automationManager.getComboBox();

    public AutomationStartupFrame() {
        super(Bundle.getMessage("MenuStartupAutomation"));
        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // setup by rows
        JPanel pAutomation = new JPanel();
        pAutomation.setLayout(new GridBagLayout());
        pAutomation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MenuStartupAutomation")));
        addItem(pAutomation, comboBox, 0, 0);

        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        pB.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pB, testButton, 0, 0);
        addItem(pB, saveButton, 1, 0);

        getContentPane().add(pAutomation);
        getContentPane().add(pB);

        addButtonAction(testButton);
        addButtonAction(saveButton);
        comboBox.setSelectedItem(automationManager.getStartupAutomation());
        
        testButton.setToolTipText(Bundle.getMessage("ButtonTestTip"));

        automationManager.addPropertyChangeListener(this);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight200));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == testButton) {
            automationManager.runStartupAutomation();
        }
        if (ae.getSource() == saveButton) {
            automationManager.setStartupAutomation((Automation) comboBox.getSelectedItem());
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    public void dispose() {
        automationManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(AutomationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            automationManager.updateComboBox(comboBox);
            comboBox.setSelectedItem(automationManager.getStartupAutomation());
        }
    }
}
