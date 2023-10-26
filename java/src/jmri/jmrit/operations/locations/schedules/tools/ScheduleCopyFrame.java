package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for copying a schedule for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2015
 */
public class ScheduleCopyFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);

    // text field
    JTextField scheduleNameTextField = new javax.swing.JTextField(Control.max_len_string_location_name);

    // major buttons
    JButton copyButton = new javax.swing.JButton(Bundle.getMessage("ButtonCopy"));

    // combo boxes
    JComboBox<Schedule> scheduleBox = scheduleManager.getComboBox();
    
    public ScheduleCopyFrame() {
        this(null);
    }

    public ScheduleCopyFrame(Schedule schedule) {
        super(Bundle.getMessage("MenuItemCopySchedule"));

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        // Layout the panel by rows
        // row 1
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScheduleName")));
        addItem(pName, scheduleNameTextField, 0, 0);

        // row 2
        JPanel pCopy = new JPanel();
        pCopy.setLayout(new GridBagLayout());
        pCopy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectScheduleToCopy")));
        addItem(pCopy, scheduleBox, 0, 0);

        // row 4
        JPanel pButton = new JPanel();
        pButton.setLayout(new GridBagLayout());
        addItem(pButton, copyButton, 0, 0);

        getContentPane().add(pName);
        getContentPane().add(pCopy);
        getContentPane().add(pButton);

        // get notified if combo box gets modified
        scheduleManager.addPropertyChangeListener(this);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Schedules", true); // NOI18N

        // set up buttons
        addButtonAction(copyButton);
 
        scheduleBox.setSelectedItem(schedule);
        
        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight250));
    }

    @Override
    protected void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == copyButton) {
            log.debug("copy Schedule button activated");
            if (!checkName()) {
                return;
            }

            if (scheduleBox.getSelectedItem() == null) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("SelectScheduleToCopy"),
                        Bundle.getMessage("CanNotSchedule", Bundle.getMessage("ButtonCopy")),
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }

            Schedule schedule = (Schedule) scheduleBox.getSelectedItem();
            scheduleManager.copySchedule(schedule, scheduleNameTextField.getText());
        }
    }

    protected void updateComboBoxes() {
        log.debug("update Schedule combobox");
        Object item = scheduleBox.getSelectedItem(); // remember which object was selected
        scheduleManager.updateComboBox(scheduleBox);
        scheduleBox.setSelectedItem(item);
    }

    /**
     *
     * @return true if name entered and isn't too long
     */
    protected boolean checkName() {
        if (scheduleNameTextField.getText().trim().isEmpty()) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"),
                    Bundle.getMessage("CanNotSchedule", Bundle.getMessage("ButtonCopy")),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (scheduleNameTextField.getText().length() > Control.max_len_string_location_name) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("ScheduleNameLengthMax",
                            Integer.toString(Control.max_len_string_location_name + 1)),
                    Bundle.getMessage("CanNotSchedule", Bundle.getMessage("ButtonCopy")),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        Schedule check = scheduleManager.getScheduleByName(scheduleNameTextField.getText());
        if (check != null) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("ScheduleAlreadyExists"),
                    Bundle.getMessage("CanNotSchedule", Bundle.getMessage("ButtonCopy")),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        scheduleManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("PropertyChange ({}) new: ({})", e.getPropertyName(), e.getNewValue());
        if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateComboBoxes();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduleCopyFrame.class);
}
