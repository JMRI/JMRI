package jmri.jmrit.operations.setup.gui;

import java.awt.GridBagLayout;
import java.util.Hashtable;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.*;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * Panel for user edit of day to name mapping
 * @author Dan Boudreau Copyright (C) 2026
 */
public class EditDayToNameMapPanel extends OperationsPreferencesPanel {

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton daysButton = new JButton(Bundle.getMessage("DaysOfWeek"));
    JButton resetButton = new JButton(Bundle.getMessage("Reset"));

    protected Hashtable<Integer, JTextField> hashTableDayToName = new Hashtable<>();
 

    public EditDayToNameMapPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel pDays = new JPanel();
        JScrollPane pManifestPane = new JScrollPane(pDays);
        pManifestPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutDays")));
        pDays.setLayout(new BoxLayout(pDays, BoxLayout.Y_AXIS));
        
        for (int i = 0; i < Control.numberOfDays; i++) {            
            JPanel pDay = new JPanel();
            pDay.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutDay", i)));
            JTextField textField = new JTextField(25);
            pDay.add(textField);
            pDays.add(pDay);
            String name = Setup.getDayToName(Integer.toString(i));
            textField.setText(name);
            hashTableDayToName.put(i, textField);
        }
        
        // row 11
        JPanel pControl = new JPanel();
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, resetButton, 0, 0);
        addItem(pControl, daysButton, 1, 0);
        addItem(pControl, saveButton, 2, 0);

        add(pManifestPane);
        add(pControl);

        // set up buttons
        addButtonAction(resetButton);
        addButtonAction(daysButton);
        addButtonAction(saveButton);
   
    }

    // Save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == resetButton) {
            hashTableDayToName.forEach((key, textField) -> {
                textField.setText("");
            });
        }
        if (ae.getSource() == daysButton) {
            loadDays();
        }
        if (ae.getSource() == saveButton) {
            savePreferences();
            var topLevelAncestor = getTopLevelAncestor();
            if (Setup.isCloseWindowOnSaveEnabled() && topLevelAncestor instanceof EditDayToNameMapFrame) {
                ((EditDayToNameMapFrame) topLevelAncestor).dispose();
            }
        }
    }
    
    private void loadDays() {
        String[] days = InstanceManager.getDefault(TrainScheduleManager.class).getDaysOfWeek();
        int j = 0;
        for (int i = 0; i < Control.numberOfDays; i++) {
            JTextField textField = hashTableDayToName.get(i);
            textField.setText(days[j++]);
            if (j == days.length) {
                j = 0;
            }
        }
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TitleDayToNameMap");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        for (int i = 0; i < Control.numberOfDays; i++) {
            JTextField textField = hashTableDayToName.get(i);
            Setup.setDayToName(Integer.toString(i), textField.getText());
        } 
        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();
        InstanceManager.getDefault(TrainManager.class).setTrainsModified();
    }

    @Override
    public boolean isDirty() {
        return false;
    }
}
