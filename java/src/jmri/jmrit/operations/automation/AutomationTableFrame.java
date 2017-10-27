package jmri.jmrit.operations.automation;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.automation.actions.Action;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.swing.JTablePersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of a automation
 *
 * @author Dan Boudreau Copyright (C) 2016
 */
public class AutomationTableFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    AutomationTableModel _automationTableModel = new AutomationTableModel();
    JTable _automationTable = new JTable(_automationTableModel);
    JScrollPane _automationPane;

    AutomationManager automationManager;

    Automation _automation = null;
    AutomationItem _automationItem = null;

    // labels
    // major buttons
    JButton stepActionButton = new JButton(Bundle.getMessage("StepAutomation"));
    JButton runActionButton = new JButton(Bundle.getMessage("RunAutomation"));
    JButton stopActionButton = new JButton(Bundle.getMessage("StopAutomation"));
    JButton resumeActionButton = new JButton(Bundle.getMessage("ResumeAutomation"));

    JButton addActionButton = new JButton(Bundle.getMessage("AddAction"));
    JButton saveAutomationButton = new JButton(Bundle.getMessage("SaveAutomation"));
    JButton deleteAutomationButton = new JButton(Bundle.getMessage("DeleteAutomation"));
    JButton addAutomationButton = new JButton(Bundle.getMessage("AddAutomation"));

    // radio buttons
    JRadioButton addActionAtTopRadioButton = new JRadioButton(Bundle.getMessage("Top"));
    JRadioButton addActionAtMiddleRadioButton = new JRadioButton(Bundle.getMessage("Middle"));
    JRadioButton addActionAtBottomRadioButton = new JRadioButton(Bundle.getMessage("Bottom"));

    // text field
    JTextField automationNameTextField = new JTextField(20);
    JTextField commentTextField = new JTextField(35);

    // combo boxes

    public static final int MAX_NAME_LENGTH = Control.max_len_string_automation_name;
    public static final String NAME = Bundle.getMessage("Name");
    public static final String DISPOSE = "dispose"; // NOI18N

    public AutomationTableFrame(Automation automation) {
        super();

        _automation = automation;

        // load managers
        automationManager = InstanceManager.getDefault(AutomationManager.class);

        // tool tips
        stepActionButton.setToolTipText(Bundle.getMessage("TipStepAutomation"));
        runActionButton.setToolTipText(Bundle.getMessage("TipRunAutomation"));
        stopActionButton.setToolTipText(Bundle.getMessage("TipStopAutomation"));
        resumeActionButton.setToolTipText(Bundle.getMessage("TipResumeAutomation"));

        // Set up the jtable in a Scroll Pane..
        _automationPane = new JScrollPane(_automationTable);
        _automationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        _automationTableModel.initTable(this, _automationTable, automation);
        if (_automation != null) {
            automationNameTextField.setText(_automation.getName());
            commentTextField.setText(_automation.getComment());
            setTitle(Bundle.getMessage("TitleAutomationEdit"));
            enableButtons(true);
        } else {
            setTitle(Bundle.getMessage("TitleAutomationAdd"));
            enableButtons(false);
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p1Pane.setMinimumSize(new Dimension(300,
                3 * automationNameTextField.getPreferredSize().height));
        p1Pane.setMaximumSize(new Dimension(2000, 200));

        // row 1a name
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, automationNameTextField, 0, 0);

        // row 1b comment
        JPanel pComment = new JPanel();
        pComment.setLayout(new GridBagLayout());
        pComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pComment, commentTextField, 0, 0);

        p1.add(pName);
        p1.add(pComment);

        // row 10
        JPanel p3 = new JPanel();
        p3.setLayout(new GridBagLayout());
        p3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AddItem")));
        p3.setMaximumSize(new Dimension(2000, 200));

        addItem(p3, addActionButton, 1, 1);
        addItem(p3, addActionAtTopRadioButton, 2, 1);
        addItem(p3, addActionAtMiddleRadioButton, 3, 1);
        addItem(p3, addActionAtBottomRadioButton, 4, 1);

        ButtonGroup group = new ButtonGroup();
        group.add(addActionAtTopRadioButton);
        group.add(addActionAtMiddleRadioButton);
        group.add(addActionAtBottomRadioButton);
        addActionAtBottomRadioButton.setSelected(true);

        // row 9 buttons
        JPanel pControl = new JPanel();
        pControl.setLayout(new GridBagLayout());
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        pControl.setMaximumSize(new Dimension(2000, 200));

        addItem(pControl, stepActionButton, 0, 0);
        addItem(pControl, runActionButton, 1, 0);
        addItem(pControl, resumeActionButton, 2, 0);
        addItem(pControl, stopActionButton, 3, 0);

        // row 11 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        pB.setBorder(BorderFactory.createTitledBorder(""));
        pB.setMaximumSize(new Dimension(2000, 200));

        // row 13
        addItem(pB, deleteAutomationButton, 0, 0);
        addItem(pB, addAutomationButton, 1, 0);
        addItem(pB, saveAutomationButton, 3, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(_automationPane);
        getContentPane().add(p3);
        getContentPane().add(pControl);
        getContentPane().add(pB);

        // setup buttons
        addButtonAction(stepActionButton);
        addButtonAction(runActionButton);
        addButtonAction(stopActionButton);
        addButtonAction(resumeActionButton);

        addButtonAction(addActionButton);
        addButtonAction(deleteAutomationButton);
        addButtonAction(addAutomationButton);
        addButtonAction(saveAutomationButton);

        if (_automation != null) {
            _automation.addPropertyChangeListener(this);
        }

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        menuBar.add(toolMenu);
        toolMenu.add(new AutomationResetAction(this));
        toolMenu.add(new AutomationCopyAction(automation));
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Automation", true); // NOI18N

        // set frame size and automation for display
        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight400));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        stopCellEditing();
        if (_automation != null) {
            if (ae.getSource() == stepActionButton) {
                _automation.step();
            }
            if (ae.getSource() == runActionButton) {
                _automation.run();
            }
            if (ae.getSource() == stopActionButton) {
                _automation.stop();
            }
            if (ae.getSource() == resumeActionButton) {
                _automation.resume();
            }
        }
        if (ae.getSource() == addActionButton) {
            addNewAutomationItem();
        }
        if (ae.getSource() == saveAutomationButton) {
            log.debug("automation save button activated");
            Automation automation = automationManager.getAutomationByName(automationNameTextField.getText());
            if (_automation == null && automation == null) {
                saveNewAutomation();
            } else {
                if (automation != null && automation != _automation) {
                    reportAutomationExists(Bundle.getMessage("save"));
                    return;
                }
                saveAutomation();
            }
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteAutomationButton) {
            log.debug("automation delete button activated");
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
                    Bundle.getMessage("DoYouWantToDeleteAutomation"), new Object[]{automationNameTextField.getText()}),
                    Bundle.getMessage("DeleteAutomation?"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Automation automation = automationManager.getAutomationByName(automationNameTextField.getText());
            if (automation == null) {
                return;
            }

            automationManager.deregister(automation);
            _automation = null;

            enableButtons(false);
            // save automation file
            OperationsXml.save();
        }
        if (ae.getSource() == addAutomationButton) {
            Automation automation = automationManager.getAutomationByName(automationNameTextField.getText());
            if (automation != null) {
                reportAutomationExists(Bundle.getMessage("add"));
                return;
            }
            saveNewAutomation();
        }
    }

    private void addNewAutomationItem() {
        // add item to this automation
        if (addActionAtTopRadioButton.isSelected()) {
            _automation.addNewItem(0);
        } else if (addActionAtBottomRadioButton.isSelected()) {
            _automation.addItem();
        } else {
            // middle radio button selected
            if (_automationTable.getSelectedRow() >= 0) {
                int row = _automationTable.getSelectedRow();
                _automation.addNewItem(row);
                // we need to reselect the table since the content has changed
                _automationTable.getSelectionModel().setSelectionInterval(row, row);
            } else {
                _automation.addNewItem(_automation.getSize() / 2);
            }
        }
    }

    private void saveNewAutomation() {
        if (!checkName(Bundle.getMessage("add"))) {
            return;
        }
        Automation automation = automationManager.newAutomation(automationNameTextField.getText());
        _automationTableModel.initTable(this, _automationTable, automation);
        _automation = automation;
        _automation.addPropertyChangeListener(this);
        // enable checkboxes
        enableButtons(true);
        saveAutomation();
    }

    private void saveAutomation() {
        if (!checkName(Bundle.getMessage("save"))) {
            return;
        }
        _automation.setName(automationNameTextField.getText());
        _automation.setComment(commentTextField.getText());

        // save automation file
        OperationsXml.save();
    }

    private void stopCellEditing() {
        if (_automationTable.isEditing()) {
            log.debug("automation table edit true");
            _automationTable.getCellEditor().stopCellEditing();
        }
    }

    /**
     *
     * @return true if name is less than 26 characters
     */
    private boolean checkName(String s) {
        if (automationNameTextField.getText().trim().equals("")) {
            return false;
        }
        if (automationNameTextField.getText().length() > MAX_NAME_LENGTH) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    Bundle.getMessage("AutomationNameLengthMax"),
                    new Object[]{Integer.toString(MAX_NAME_LENGTH)}), MessageFormat.format(
                    Bundle.getMessage("CanNotAutomation"), new Object[]{s}),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void reportAutomationExists(String s) {
        log.info("Can not {} automation already exists", s);
        JOptionPane.showMessageDialog(this, Bundle.getMessage("ReportExists"),
                MessageFormat.format(Bundle.getMessage("CanNotAutomation"), new Object[]{s}),
                JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        enableControlButtons(enabled);

        addActionButton.setEnabled(enabled);
        addActionAtTopRadioButton.setEnabled(enabled);
        addActionAtMiddleRadioButton.setEnabled(enabled);
        addActionAtBottomRadioButton.setEnabled(enabled);
        saveAutomationButton.setEnabled(enabled);
        deleteAutomationButton.setEnabled(enabled);
        _automationTable.setEnabled(enabled);
        // the inverse!
        addAutomationButton.setEnabled(!enabled);
    }

    private void enableControlButtons(boolean enabled) {
        boolean b = enabled && _automation != null && _automation.getSize() > 0;
        stepActionButton.setEnabled(b && !_automation.isActionRunning());
        runActionButton.setEnabled(b && !_automation.isRunning());
        stopActionButton.setEnabled(b && _automation.isActionRunning());
        resumeActionButton.setEnabled(b && !_automation.isRunning());
    }

    @Override
    public void dispose() {
        if (_automation != null) {
            _automation.removePropertyChangeListener(this);
        }
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(_automationTable);
        });
        _automationTableModel.dispose();
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        //        if (Control.showProperty)
        log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                .getNewValue());
        if (e.getPropertyName().equals(Automation.LISTCHANGE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Automation.RUNNING_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Action.ACTION_RUNNING_CHANGED_PROPERTY)) {
            enableControlButtons(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AutomationTableFrame.class);
}
