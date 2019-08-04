package jmri.jmrit.operations.automation;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table model allowing the edit and status of an automation used by operations.
 *
 * @author Daniel Boudreau Copyright (C) 2016
 */
public class AutomationsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    AutomationManager automationManager; // There is only one manager

    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int NAME_COLUMN = ID_COLUMN + 1;
    private static final int COMMENT_COLUMN = NAME_COLUMN + 1;
    private static final int ACTION_COLUMN = COMMENT_COLUMN + 1;
    private static final int STATUS_COLUMN = ACTION_COLUMN + 1;
    private static final int RUN_COLUMN = STATUS_COLUMN + 1;
    private static final int EDIT_COLUMN = RUN_COLUMN + 1;
    private static final int DELETE_COLUMN = EDIT_COLUMN + 1;

    private static final int HIGHEST_COLUMN = DELETE_COLUMN + 1;

    public AutomationsTableModel() {
        super();
        automationManager = InstanceManager.getDefault(AutomationManager.class);
        automationManager.addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;

    private int _sort = SORTBYNAME;

    public void setSort(int sort) {
        _sort = sort;
        updateList();
        fireTableDataChanged();
    }

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeAutomations();

        if (_sort == SORTBYID) {
            _sysList = automationManager.getAutomationsByIdList();
        } else {
            _sysList = automationManager.getAutomationsByNameList();
        }
        // and add them back in
        for (Automation automation : _sysList) {
            automation.addPropertyChangeListener(this);
        }
    }

    List<Automation> _sysList = null;
    JTable _table;

    protected void initTable(AutomationsTableFrame frame, JTable table) {
        _table = table;
        // Install the button handlers
        TableColumnModel tcm = table.getColumnModel();
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(RUN_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(RUN_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DELETE_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DELETE_COLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(40);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(COMMENT_COLUMN).setPreferredWidth(350);
        table.getColumnModel().getColumn(ACTION_COLUMN).setPreferredWidth(250);
        table.getColumnModel().getColumn(STATUS_COLUMN).setPreferredWidth(90);
        table.getColumnModel().getColumn(RUN_COLUMN).setPreferredWidth(90);
        table.getColumnModel().getColumn(EDIT_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(90);

        frame.loadTableDetails(table);
    }

    @Override
    public int getRowCount() {
        return _sysList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHEST_COLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case ID_COLUMN:
                return Bundle.getMessage("Id");
            case NAME_COLUMN:
                return Bundle.getMessage("Name");
            case COMMENT_COLUMN:
                return Bundle.getMessage("Comment");
            case ACTION_COLUMN:
                return Bundle.getMessage("Action");
            case STATUS_COLUMN:
                return Bundle.getMessage("Status");
            case RUN_COLUMN:
                return Bundle.getMessage("Run");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ID_COLUMN:
                return String.class;
            case NAME_COLUMN:
                return String.class;
            case COMMENT_COLUMN:
                return String.class;
            case STATUS_COLUMN:
                return String.class;
            case ACTION_COLUMN:
                return String.class;
            case RUN_COLUMN:
                return JButton.class;
            case EDIT_COLUMN:
                return JButton.class;
            case DELETE_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case RUN_COLUMN:
            case EDIT_COLUMN:
            case DELETE_COLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount()) {
            return "ERROR row " + row; // NOI18N
        }
        Automation automation = _sysList.get(row);
        if (automation == null) {
            return "ERROR automation unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return automation.getId();
            case NAME_COLUMN:
                return automation.getName();
            case COMMENT_COLUMN:
                return automation.getComment();
            case ACTION_COLUMN:
                return automation.getCurrentActionString();
            case STATUS_COLUMN:
                return automation.getActionStatus();
            case RUN_COLUMN:
                if (automation.isActionRunning())
                    return Bundle.getMessage("Stop");
                else if (automation.isReadyToRun())
                    return Bundle.getMessage("Run");
                else
                    return Bundle.getMessage("Resume");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case RUN_COLUMN:
                runAutomation(row);
                break;
            case EDIT_COLUMN:
                editAutomation(row);
                break;
            case DELETE_COLUMN:
                deleteAutomation(row);
                break;
            default:
                break;
        }
    }

    private void runAutomation(int row) {
        Automation automation = _sysList.get(row);
        SwingUtilities.invokeLater(() -> {
            if (automation.isActionRunning())
                automation.stop();
            else
                automation.resume();
        });
    }

    Hashtable<String, AutomationTableFrame> automationEditFrames = new Hashtable<String, AutomationTableFrame>();

    private void editAutomation(int row) {
        log.debug("Edit automation");
        Automation automation = _sysList.get(row);

        // is the edit window already open?
        if (automationEditFrames.containsKey(automation.getId())) {
            AutomationTableFrame frame = automationEditFrames.get(automation.getId());
            if (frame.isVisible()) {
                frame.toFront();
                frame.setExtendedState(Frame.NORMAL);
                return; // done
            }
        }

        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            AutomationTableFrame frame = new AutomationTableFrame(automation);
            automationEditFrames.put(automation.getId(), frame);
        });
    }

    private void deleteAutomation(int row) {
        log.debug("Delete automation");
        Automation automation = _sysList.get(row);
        if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("DoYouWantToDeleteAutomation"),
                new Object[]{automation.getName()}), Bundle.getMessage("DeleteAutomation?"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            automationManager.deregister(automation);
            OperationsXml.save();
        }
    }

    protected void comboBoxActionPerformed(ActionEvent ae) {
        log.debug("combobox action");
        if (_table.isEditing()) {
            _table.getCellEditor().stopCellEditing(); // Allows the table contents to update
        }
    }

    private void removePropertyChangeAutomations() {
        if (_sysList != null) {
            for (Automation automation : _sysList) {
                automation.removePropertyChangeListener(this);
            }
        }
    }

    public void dispose() {
        Enumeration<String> en = automationEditFrames.keys();
        while (en.hasMoreElements()) {
            AutomationTableFrame frame = automationEditFrames.get(en.nextElement());
            frame.dispose();
        }

        automationManager.removePropertyChangeListener(this);
        removePropertyChangeAutomations();
    }

    // check for change in number of automations, or a change in a automation
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(AutomationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        } else if (e.getSource().getClass().equals(Automation.class)) {
            Automation automation = (Automation) e.getSource();
            int row = _sysList.indexOf(automation);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update automation table row: {} name: {}", row, automation.getName());
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AutomationsTableModel.class);
}
