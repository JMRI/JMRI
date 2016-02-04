// AutomationTableModel.java
package jmri.jmrit.operations.automation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.jmrit.operations.automation.actions.Action;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of a automation used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2016
 * @version $Revision$
 */
public class AutomationTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int CURRENT_COLUMN = ID_COLUMN + 1;
    private static final int ACTION_COLUMN = CURRENT_COLUMN + 1;
    private static final int TRAIN_COLUMN = ACTION_COLUMN + 1;
    private static final int ROUTE_COLUMN = TRAIN_COLUMN + 1;
    private static final int AUTOMATION_COLUMN = ROUTE_COLUMN + 1;
    private static final int STATUS_COLUMN = AUTOMATION_COLUMN + 1;
    private static final int MESSAGE_COLUMN = STATUS_COLUMN + 1;
    private static final int UP_COLUMN = MESSAGE_COLUMN + 1;
    private static final int DOWN_COLUMN = UP_COLUMN + 1;
    private static final int DELETE_COLUMN = DOWN_COLUMN + 1;

    private static final int HIGHEST_COLUMN = DELETE_COLUMN + 1;

    public AutomationTableModel() {
        super();
    }

    Automation _automation;
    JTable _table;
    AutomationTableFrame _frame;
    boolean _matchMode = false;

    synchronized void updateList() {
        if (_automation == null) {
            return;
        }
        // first, remove listeners from the individual objects
        removePropertyChangeAutomationItems();
        _list = _automation.getItemsBySequenceList();
        // and add them back in
        for (AutomationItem item : _list) {
            item.addPropertyChangeListener(this);
        }
    }

    List<AutomationItem> _list = new ArrayList<AutomationItem>();

    void initTable(AutomationTableFrame frame, JTable table, Automation automation) {
        _automation = automation;
        _table = table;
        _frame = frame;

        // add property listeners
        if (_automation != null) {
            _automation.addPropertyChangeListener(this);
        }
        initTable(table);
    }

    private void initTable(JTable table) {
        // Install the button handlers
        TableColumnModel tcm = table.getColumnModel();
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(MESSAGE_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(MESSAGE_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(UP_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(UP_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DOWN_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DOWN_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DELETE_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DELETE_COLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        setPreferredWidths(table);

        // set row height
        table.setRowHeight(new JComboBox<Object>().getPreferredSize().height);
        updateList();
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // only allow one row at a time to be selected
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void setPreferredWidths(JTable table) {
        if (_frame.loadTableDetails(table)) {
            return; // done
        }
        log.debug("Setting preferred widths");
        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(35);
        table.getColumnModel().getColumn(CURRENT_COLUMN).setPreferredWidth(60);
        table.getColumnModel().getColumn(ACTION_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(TRAIN_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(ROUTE_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(AUTOMATION_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(STATUS_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(MESSAGE_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(UP_COLUMN).setPreferredWidth(60);
        table.getColumnModel().getColumn(DOWN_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(70);
    }

    public int getRowCount() {
        return _list.size();
    }

    public int getColumnCount() {
        return HIGHEST_COLUMN;
    }

    public String getColumnName(int col) {
        switch (col) {
            case ID_COLUMN:
                return Bundle.getMessage("Id");
            case CURRENT_COLUMN:
                return Bundle.getMessage("Current");
            case ACTION_COLUMN:
                return Bundle.getMessage("Action");
            case TRAIN_COLUMN:
                return Bundle.getMessage("Train");
            case ROUTE_COLUMN:
                return Bundle.getMessage("RouteLocation");
            case AUTOMATION_COLUMN:
                return Bundle.getMessage("AutomationOther");
            case STATUS_COLUMN:
                return Bundle.getMessage("Status");
            case MESSAGE_COLUMN:
                return Bundle.getMessage("Message");
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            case DELETE_COLUMN:
                return Bundle.getMessage("Delete");
            default:
                return "unknown"; // NOI18N
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ID_COLUMN:
                return String.class;
            case CURRENT_COLUMN:
                return String.class;
            case ACTION_COLUMN:
                return JComboBox.class;
            case TRAIN_COLUMN:
                return JComboBox.class;
            case ROUTE_COLUMN:
                return JComboBox.class;
            case AUTOMATION_COLUMN:
                return JComboBox.class;
            case STATUS_COLUMN:
                return String.class;
            case MESSAGE_COLUMN:
                return JButton.class;
            case UP_COLUMN:
                return JButton.class;
            case DOWN_COLUMN:
                return JButton.class;
            case DELETE_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case ACTION_COLUMN:
            case TRAIN_COLUMN:
            case ROUTE_COLUMN:
            case AUTOMATION_COLUMN:
            case UP_COLUMN:
            case DOWN_COLUMN:
            case DELETE_COLUMN:
                return true;
            case MESSAGE_COLUMN: {
                // determine if messages are enabled
                AutomationItem item = _list.get(row);
                JComboBox<Action> acb = getActionComboBox(item);
                return ((Action) acb.getSelectedItem()).isMessageOkEnabled();
            }
            default:
                return false;
        }
    }

    public Object getValueAt(int row, int col) {
        if (row >= _list.size()) {
            return "ERROR row " + row; // NOI18N
        }
        AutomationItem item = _list.get(row);
        if (item == null) {
            return "ERROR automation item unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return item.getId();
            case CURRENT_COLUMN:
                return getCurrentPointer(item);
            case ACTION_COLUMN:
                return getActionComboBox(item);
            case TRAIN_COLUMN:
                return getTrainComboBox(item);
            case ROUTE_COLUMN:
                return getRouteLocationComboBox(item);
            case AUTOMATION_COLUMN:
                return getAutomationComboBox(item);
            case STATUS_COLUMN:
                return getStatus(item);
            case MESSAGE_COLUMN:
                if (item.getMessage().equals(AutomationItem.NONE)
                        && item.getMessageFail().equals(AutomationItem.NONE))
                    return Bundle.getMessage("Add");
                else
                    return Bundle.getMessage("Edit");
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            case DELETE_COLUMN:
                return Bundle.getMessage("Delete");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (value == null) {
            log.debug("Warning automation table row {} still in edit", row);
            return;
        }
        AutomationItem item = _list.get(row);
        switch (col) {
            case ACTION_COLUMN:
                setAction(value, item);
                break;
            case TRAIN_COLUMN:
                setTrain(value, item);
                break;
            case ROUTE_COLUMN:
                setRouteLocation(value, item);
                break;
            case AUTOMATION_COLUMN:
                setAutomationColumn(value, item);
                break;
            case MESSAGE_COLUMN:
                setMessage(value, item);
                break;
            case UP_COLUMN:
                moveUpAutomationItem(item);
                break;
            case DOWN_COLUMN:
                moveDownAutomationItem(item);
                break;
            case DELETE_COLUMN:
                deleteAutomationItem(item);
                break;
            default:
                break;
        }
    }

    private String getCurrentPointer(AutomationItem item) {
        if (_automation.getCurrentAutomationItem() == item)
            return "    -->"; // NOI18N
        else
            return "";
    }

    private JComboBox<Action> getActionComboBox(AutomationItem item) {
        JComboBox<Action> cb = item.getActionComboBox();
        //      cb.setSelectedItem(item.getAction()); TODO understand why this didn't work, class?
        for (int index = 0; index < cb.getItemCount(); index++) {
            // select the action based on it's action code
            if (item.getAction() != null && (cb.getItemAt(index)).getCode() == item.getAction().getCode()) {
                cb.setSelectedIndex(index);
                break;
            }
        }
        return cb;
    }

    private JComboBox<Train> getTrainComboBox(AutomationItem item) {
        JComboBox<Train> cb = TrainManager.instance().getTrainComboBox();
        cb.setSelectedItem(item.getTrain());
        // determine if train combo box is enabled
        cb.setEnabled(item.getAction() != null && item.getAction().isTrainMenuEnabled());
        return cb;
    }

    private JComboBox<RouteLocation> getRouteLocationComboBox(AutomationItem item) {
        JComboBox<RouteLocation> cb = new JComboBox<RouteLocation>();
        if (item.getTrain() != null && item.getTrain().getRoute() != null) {
            cb = item.getTrain().getRoute().getComboBox();
            cb.setSelectedItem(item.getRouteLocation());
        }
        // determine if route combo box is enabled
        cb.setEnabled(item.getAction() != null && item.getAction().isRouteMenuEnabled());
        return cb;
    }

    /**
     * Returns either a comboBox loaded with Automations, or a goto list of
     * AutomationItems, or TrainSchedules.
     * 
     * @param item
     * @return comboBox loaded with automations or a goto automationIem list
     */
    private JComboBox<?> getAutomationComboBox(AutomationItem item) {
        if (item.getAction() != null) {
            return item.getAction().getComboBox();
        }
        return null;
    }

    private String getStatus(AutomationItem item) {
        return item.getStatus();
    }

    private void setAction(Object value, AutomationItem item) {
        @SuppressWarnings("unchecked")
        JComboBox<Action> cb = (JComboBox<Action>) value;
        item.setAction((Action) cb.getSelectedItem());
    }

    private void setTrain(Object value, AutomationItem item) {
        @SuppressWarnings("unchecked")
        JComboBox<Train> cb = (JComboBox<Train>) value;
        item.setTrain((Train) cb.getSelectedItem());
    }

    private void setRouteLocation(Object value, AutomationItem item) {
        @SuppressWarnings("unchecked")
        JComboBox<RouteLocation> cb = (JComboBox<RouteLocation>) value;
        item.setRouteLocation((RouteLocation) cb.getSelectedItem());
    }

    private void setAutomationColumn(Object value, AutomationItem item) {
        item.setOther(((JComboBox<?>) value).getSelectedItem());
    }

    private void setMessage(Object value, AutomationItem item) {
        // Create comment panel
        final JDialog dialog = new JDialog();
        dialog.setLayout(new BorderLayout());
        dialog.setTitle(Bundle.getMessage("Message"));

        final JTextArea messageTextArea = new JTextArea(6, 100);
        JScrollPane messageScroller = new JScrollPane(messageTextArea);
        messageScroller.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MessageOk")));
        dialog.add(messageScroller, BorderLayout.NORTH);
        messageTextArea.setText(item.getMessage());
        messageTextArea.setToolTipText(Bundle.getMessage("TipMessage"));

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        dialog.add(buttonPane, BorderLayout.SOUTH);

        JCheckBox haltCheckBox = new JCheckBox(Bundle.getMessage("HaltIfFail"));
        haltCheckBox.setSelected(item.isHaltFailureEnabled());

        final JTextArea messageFailTextArea = new JTextArea(6, 100);
        if (item.getAction() != null && item.getAction().isMessageFailEnabled()) {
            JScrollPane messageFailScroller = new JScrollPane(messageFailTextArea);
            messageFailScroller.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MessageFail")));
            dialog.add(messageFailScroller, BorderLayout.CENTER);
            messageFailTextArea.setText(item.getMessageFail());
            messageFailTextArea.setToolTipText(Bundle.getMessage("TipMessage"));

            buttonPane.add(haltCheckBox);
        }

        JButton okayButton = new JButton(Bundle.getMessage("Okay"));
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                item.setMessage(messageTextArea.getText());
                item.setMessageFail(messageFailTextArea.getText());
                item.setHaltFailureEnabled(haltCheckBox.isSelected());
                dialog.dispose();
                return;
            }
        });
        buttonPane.add(okayButton);

        JButton cancelButton = new JButton(Bundle.getMessage("Cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dialog.dispose();
                return;
            }
        });
        buttonPane.add(cancelButton);

        JButton defaultMessagesButton = new JButton(Bundle.getMessage("DefaultMessages"));
        defaultMessagesButton.setToolTipText(Bundle.getMessage("TipDefaultButton"));
        defaultMessagesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (messageTextArea.getText().equals(AutomationItem.NONE)) {
                    messageTextArea.setText(Bundle.getMessage("DefaultMessageOk"));
                }
                if (messageFailTextArea.getText().equals(AutomationItem.NONE)) {
                    messageFailTextArea.setText(Bundle.getMessage("DefaultMessageFail"));
                }
                return;
            }
        });
        buttonPane.add(defaultMessagesButton);

        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void moveUpAutomationItem(AutomationItem item) {
        log.debug("move automation item up");
        _automation.moveItemUp(item);
    }

    private void moveDownAutomationItem(AutomationItem item) {
        log.debug("move automation item down");
        _automation.moveItemDown(item);
    }

    private void deleteAutomationItem(AutomationItem item) {
        log.debug("Delete automation item");
        _automation.deleteItem(item);
    }

    // this table listens for changes to a automation and it's car types
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.showProperty)
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());

        if (e.getPropertyName().equals(Automation.LISTCHANGE_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }
        if (e.getPropertyName().equals(Automation.CURRENT_ITEM_CHANGED_PROPERTY)) {
            fireTableDataChanged();
        }
        // update automation item?
        if (e.getSource().getClass().equals(AutomationItem.class)) {
            AutomationItem item = (AutomationItem) e.getSource();
            int row = _list.indexOf(item);
            if (Control.showProperty)
                log.debug("Update automation item table row: {}", row);
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private void removePropertyChangeAutomationItems() {
        for (AutomationItem item : _list) {
            item.removePropertyChangeListener(this);
        }
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
        if (_automation != null) {
            removePropertyChangeAutomationItems();
            _automation.removePropertyChangeListener(this);
        }
    }

    static Logger log = LoggerFactory.getLogger(AutomationTableModel.class.getName());
}
