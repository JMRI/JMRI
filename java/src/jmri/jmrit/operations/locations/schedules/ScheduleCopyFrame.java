package jmri.jmrit.operations.locations.schedules;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                JOptionPane.showMessageDialog(this, Bundle.getMessage("SelectScheduleToCopy"), MessageFormat.format(Bundle
                        .getMessage("CanNotSchedule"), new Object[]{Bundle.getMessage("ButtonCopy")}), JOptionPane.ERROR_MESSAGE);
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
        if (scheduleNameTextField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"), MessageFormat.format(Bundle
                    .getMessage("CanNotSchedule"), new Object[]{Bundle.getMessage("ButtonCopy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (scheduleNameTextField.getText().length() > Control.max_len_string_location_name) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("ScheduleNameLengthMax"),
                    new Object[]{Integer.toString(Control.max_len_string_location_name + 1)}), MessageFormat.format(Bundle
                            .getMessage("CanNotSchedule"), new Object[]{Bundle.getMessage("ButtonCopy")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Schedule check = scheduleManager.getScheduleByName(scheduleNameTextField.getText());
        if (check != null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ScheduleAlreadyExists"), MessageFormat.format(Bundle
                    .getMessage("CanNotSchedule"), new Object[]{Bundle.getMessage("ButtonCopy")}), JOptionPane.ERROR_MESSAGE);
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

    private final static Logger log = LoggerFactory.getLogger(ScheduleCopyFrame.class);
}
