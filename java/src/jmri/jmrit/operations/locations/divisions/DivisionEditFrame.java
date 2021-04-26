package jmri.jmrit.operations.locations.divisions;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for user edit of a division
 *
 * @author Dan Boudreau Copyright (C) 2021
 */
public class DivisionEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {
    
    protected Division _division;
    DivisionManager divisionManager;

    // major buttons
    JButton saveDivisionButton = new JButton(Bundle.getMessage("SaveDivision"));
    JButton deleteDivisionButton = new JButton(Bundle.getMessage("DeleteDivision"));
    JButton addDivisionButton = new JButton(Bundle.getMessage("AddDivision"));

    // text field
    JTextField divisionNameTextField = new JTextField(20);
    JTextField commentTextField = new JTextField(35);

    public static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;
    public static final String NAME = Bundle.getMessage("Name");
    public static final String DISPOSE = "dispose"; // NOI18N

    public DivisionEditFrame(Division division) {
        super(Bundle.getMessage("AddDivision"));

        _division = division;
        // load manager
        divisionManager = InstanceManager.getDefault(DivisionManager.class);
 
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p1Pane.setMinimumSize(new Dimension(300,
                3 * divisionNameTextField.getPreferredSize().height));
        p1Pane.setMaximumSize(new Dimension(2000, 200));

        // row 1a name
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, divisionNameTextField, 0, 0);

        // row 1b comment
        JPanel pC = new JPanel();
        pC.setLayout(new GridBagLayout());
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pC, commentTextField, 0, 0);

        p1.add(pName);
        p1.add(pC);

        // row 11 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        pB.setBorder(BorderFactory.createTitledBorder(""));

        // row 13
        addItem(pB, deleteDivisionButton, 0, 0);
        addItem(pB, addDivisionButton, 1, 0);
        addItem(pB, saveDivisionButton, 3, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(pB);

        // set up buttons
        addButtonAction(deleteDivisionButton);
        addButtonAction(addDivisionButton);
        addButtonAction(saveDivisionButton);
        
        enableButtons(division != null);
        
        if (division != null) {
            setTitle(Bundle.getMessage("EditDivision"));
            divisionNameTextField.setText(division.getName());
            commentTextField.setText(division.getComment());
        }

        // build menu
        setJMenuBar(new JMenuBar());
        addHelpMenu("package.jmri.jmrit.operations.Operations_Divisions", true); // NOI18N

        // set frame size and division for display
        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight200));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveDivisionButton) {
            log.debug("division save button activated");
            Division division = divisionManager.getDivisionByName(divisionNameTextField.getText());
            if (_division == null && division == null) {
                saveNewDivision();
            } else {
                if (division != null && division != _division) {
                    reportDivisionExists(Bundle.getMessage("save"));
                    return;
                }
                saveDivision();
            }
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteDivisionButton) {
            log.debug("division delete button activated");
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
                    Bundle.getMessage("DoYouWantToDeleteDivision"),
                    new Object[]{divisionNameTextField.getText()}), Bundle
                            .getMessage("DeleteDivision"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Division division = divisionManager.getDivisionByName(divisionNameTextField.getText());
            if (division == null) {
                return;
            }

            divisionManager.deregister(division);
            _division = null;

            enableButtons(false);
            OperationsXml.save();
        }
        if (ae.getSource() == addDivisionButton) {
            Division division = divisionManager.getDivisionByName(divisionNameTextField.getText());
            if (division != null) {
                reportDivisionExists(Bundle.getMessage("add"));
                return;
            }
            saveNewDivision();
        }
    }

    private void saveNewDivision() {
        if (!checkName(Bundle.getMessage("add"))) {
            return;
        }
        _division = divisionManager.newDivision(divisionNameTextField.getText());
        enableButtons(true);
        saveDivision();
    }

    private void saveDivision() {
        if (!checkName(Bundle.getMessage("save"))) {
            return;
        }
        _division.setName(divisionNameTextField.getText());
        _division.setComment(commentTextField.getText());
        OperationsXml.save();
    }

    /**
     *
     * @return true if name is less than 26 characters
     */
    private boolean checkName(String s) {
        if (divisionNameTextField.getText().trim().isEmpty()) {
            return false;
        }
        if (divisionNameTextField.getText().length() > MAX_NAME_LENGTH) {
            log.error("Division name must be less than 26 charaters");
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    Bundle.getMessage("DivisionNameLengthMax"),
                    new Object[]{Integer.toString(MAX_NAME_LENGTH + 1)}), MessageFormat.format(
                            Bundle.getMessage("CanNotDivision"), new Object[]{s}),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void reportDivisionExists(String s) {
        log.info("Can not {}, division already exists", s);
        JOptionPane.showMessageDialog(this, Bundle.getMessage("ReportDivisionExists"),
                MessageFormat.format(Bundle.getMessage("CanNotDivision"), new Object[]{s}),
                JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        saveDivisionButton.setEnabled(enabled);
        deleteDivisionButton.setEnabled(enabled);
        // the inverse!
        addDivisionButton.setEnabled(!enabled);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DivisionEditFrame.class.getName());
}
