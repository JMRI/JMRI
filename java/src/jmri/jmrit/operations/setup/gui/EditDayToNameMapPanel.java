package jmri.jmrit.operations.setup.gui;

import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Hashtable;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.*;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * Panel for user edit of day to name mapping
 * 
 * @author Dan Boudreau Copyright (C) 2026
 */
public class EditDayToNameMapPanel extends OperationsPreferencesPanel {

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton daysButton = new JButton(Bundle.getMessage("DaysOfWeek"));
    JButton datesButton = new JButton(Bundle.getMessage("LoadDates"));
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
        addItem(pControl, datesButton, 2, 0);
        addItem(pControl, saveButton, 3, 0);

        add(pManifestPane);
        add(pControl);

        // set up buttons
        addButtonAction(resetButton);
        addButtonAction(daysButton);
        addButtonAction(datesButton);
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
        if (ae.getSource() == datesButton) {
            loadDates();
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
        JTextField textField;
        String[] days = InstanceManager.getDefault(TrainScheduleManager.class).getDaysOfWeek();
        int j = 0;
        // list days starting with the day the user entered
        textField = hashTableDayToName.get(0);
        if (!textField.getText().isBlank()) {
            for (String day : days) {
                if (day.equals(textField.getText()) || j == days.length - 1) {
                    break;
                }
                j++;
            }
        }
        for (int i = 0; i < Control.numberOfDays; i++) {
            textField = hashTableDayToName.get(i);
            textField.setText(days[j++]);
            if (j == days.length) {
                j = 0;
            }
        }
    }

    private void loadDates() {
        JTextField textField;
        // list dates starting with the date the user entered
        textField = hashTableDayToName.get(0);
        LocalDate ld = LocalDate.now();
        String year = Setup.getYearModeled();
        if (!year.isBlank()) {
            // default format yyyy-MM-dd
            try {
            ld = LocalDate.parse(year + "-01-01");
            } catch (DateTimeParseException e) {
                log.error("Year {} must be four digits", year);
            }
        } else {
            year = "1956";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        // three date formats accepted MMMM d, yyyy or MMMM d or MM/dd/yyyy
        if (!textField.getText().isBlank()) {
            // try month day year format MMMM d, yyyy
            try {
                ld = LocalDate.parse(textField.getText(), formatter);
            } catch (DateTimeParseException e) {
                // try month day format
                try {
                    ld = LocalDate.parse(textField.getText() + ", " + year , formatter);
                    formatter = DateTimeFormatter.ofPattern("MMMM d");
                } catch (DateTimeParseException e2) {
                    // try MM/dd/yyyy
                    try {
                        formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                        ld = LocalDate.parse(textField.getText(), formatter);
                    } catch (DateTimeParseException e3) {
                        log.error("Couldn't covert {} to date", textField.getText());
                    }
                }
            }
        }
        
        for (int i = 0; i < Control.numberOfDays; i++) {
            textField = hashTableDayToName.get(i);
            textField.setText(ld.format(formatter));
            ld = ld.plusDays(1);
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

    private final static Logger log = LoggerFactory.getLogger(EditDayToNameMapPanel.class);
}
