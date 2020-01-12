package jmri.jmrit.operations.trains.schedules;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to edit train schedules.
 *
 * @author Daniel Boudreau Copyright (C)
 * 
 *
 */
public class TrainsScheduleEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // text box
    JTextField addTextBox = new JTextField(Control.max_len_string_attibute);

    // combo box
    private JComboBox<TrainSchedule> comboBox = null;

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton replaceButton = new JButton(Bundle.getMessage("Replace"));

    JButton restoreButton = new JButton(Bundle.getMessage("Restore"));

    TrainScheduleManager trainScheduleManager = null;

    public TrainsScheduleEditFrame() {
        super();

        trainScheduleManager = InstanceManager.getDefault(TrainScheduleManager.class);

        // the following code sets the frame's initial state
        getContentPane().setLayout(new GridBagLayout());

        trainScheduleManager.addPropertyChangeListener(this);

        initComponents();
    }

    @Override
    public void initComponents() {
        try {
           comboBox = trainScheduleManager.getComboBox();
        } catch(IllegalArgumentException iae) {
           comboBox = new JComboBox<>();
           trainScheduleManager.updateComboBox(comboBox);
        }

        // row 1
        addItem(addTextBox, 2, 2);
        addItem(addButton, 3, 2);

        // row 3
        addItem(comboBox, 2, 3);
        addItem(deleteButton, 3, 3);

        // row 4 
        addItem(replaceButton, 3, 4);

        // row 5
        addItem(restoreButton, 2, 5);

        addButtonAction(addButton);
        addButtonAction(deleteButton);
        addButtonAction(replaceButton);
        addButtonAction(restoreButton);

        setTitle(Bundle.getMessage("MenuItemEditSchedule"));
        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight200));

    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == deleteButton && comboBox.getSelectedItem() != null) {
            trainScheduleManager.deregister((TrainSchedule) comboBox.getSelectedItem());
        }
        if (ae.getSource() == restoreButton) {
            trainScheduleManager.createDefaultSchedules();
        }
        // check for valid name
        String s = addTextBox.getText();
        s = s.trim();
        if (s.equals("")) {
            return; // done
        }
        if (ae.getSource() == addButton) {
            trainScheduleManager.newSchedule(s);
        }
        if (ae.getSource() == replaceButton && comboBox.getSelectedItem() != null) {
            TrainSchedule ts = ((TrainSchedule) comboBox.getSelectedItem());
            ts.setName(s);
        }
    }

    @Override
    public void dispose() {
        trainScheduleManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        trainScheduleManager.updateComboBox(comboBox);
    }

    private final static Logger log = LoggerFactory.getLogger(TrainsScheduleEditFrame.class);
}
