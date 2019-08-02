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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.automation.actions.Action;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of a automation used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2016
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
    private static final int HIAF_COLUMN = STATUS_COLUMN + 1;
    private static final int MESSAGE_COLUMN = HIAF_COLUMN + 1;
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

    private void updateList() {
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

    List<AutomationItem> _list = new ArrayList<>();

    protected void initTable(AutomationTableFrame frame, JTable table, Automation automation) {
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

        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(35);
        table.getColumnModel().getColumn(CURRENT_COLUMN).setPreferredWidth(60);
        table.getColumnModel().getColumn(ACTION_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(TRAIN_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(ROUTE_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(AUTOMATION_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(STATUS_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(HIAF_COLUMN).setPreferredWidth(50);
        table.getColumnModel().getColumn(MESSAGE_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(UP_COLUMN).setPreferredWidth(60);
        table.getColumnModel().getColumn(DOWN_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(70);

        _frame.loadTableDetails(table);
        // does not use a table sorter
        table.setRowSorter(null);

        updateList();
    }

    @Override
    public int getRowCount() {
        return _list.size();
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
            case HIAF_COLUMN:
                return Bundle.getMessage("HaltIfActionFails");
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
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
            case HIAF_COLUMN:
                return Boolean.class;
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

    @Override
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
            case HIAF_COLUMN: {
                AutomationItem item = _list.get(row);
                return item.getAction().isMessageFailEnabled();
            }
            case MESSAGE_COLUMN: {
                AutomationItem item = _list.get(row);
                JComboBox<Action> acb = getActionComboBox(item);
                return ((Action) acb.getSelectedItem()).isMessageOkEnabled();
            }
            default:
                return false;
        }
    }

    // TODO adding synchronized to the following causes thread lock.
    // See line in propertyChange below:
    // _table.scrollRectToVisible(_table.getCellRect(row, 0, true));
    // Stack trace:
    //    owns: Component$AWTTreeLock  (id=127)   
    //    waiting for: AutomationTableModel  (id=128) 
    //    AutomationTableModel.getRowCount() line: 131    
    //    JTable.getRowCount() line: 2662 
    //    BasicTableUI.paint(Graphics, JComponent) line: 1766 
    //    BasicTableUI(ComponentUI).update(Graphics, JComponent) line: 161    
    //    JTable(JComponent).paintComponent(Graphics) line: 777   
    //    JTable(JComponent).paint(Graphics) line: 1053   
    //    JViewport(JComponent).paintChildren(Graphics) line: 886 
    //    JViewport(JComponent).paint(Graphics) line: 1062    
    //    JViewport.paint(Graphics) line: 692 
    //    JViewport(JComponent).paintToOffscreen(Graphics, int, int, int, int, int, int) line: 5223   
    //    RepaintManager$PaintManager.paintDoubleBuffered(JComponent, Image, Graphics, int, int, int, int) line: 1572 
    //    RepaintManager$PaintManager.paint(JComponent, JComponent, Graphics, int, int, int, int) line: 1495  
    //    RepaintManager.paint(JComponent, JComponent, Graphics, int, int, int, int) line: 1265   
    //    JViewport(JComponent).paintForceDoubleBuffered(Graphics) line: 1089 
    //    JViewport.paintView(Graphics) line: 1635    
    //    JViewport.flushViewDirtyRegion(Graphics, Rectangle) line: 1508  
    //    JViewport.setViewPosition(Point) line: 1093 
    //    JViewport.scrollRectToVisible(Rectangle) line: 436  
    //    JTable(JComponent).scrollRectToVisible(Rectangle) line: 3108    
    //    AutomationTableModel.propertyChange(PropertyChangeEvent) line: 498  
    //    PropertyChangeSupport.fire(PropertyChangeListener[], PropertyChangeEvent) line: 335 
    //    PropertyChangeSupport.firePropertyChange(PropertyChangeEvent) line: 327 
    //    PropertyChangeSupport.firePropertyChange(String, Object, Object) line: 263  
    //    Automation.setDirtyAndFirePropertyChange(String, Object, Object) line: 666  
    //    Automation.setCurrentAutomationItem(AutomationItem) line: 279   
    //    Automation.setNextAutomationItem() line: 243    
    //    Automation.CheckForActionPropertyChange(PropertyChangeEvent) line: 607  
    //    Automation.propertyChange(PropertyChangeEvent) line: 646    
    //    PropertyChangeSupport.fire(PropertyChangeListener[], PropertyChangeEvent) line: 335 
    //    PropertyChangeSupport.firePropertyChange(PropertyChangeEvent) line: 327 
    //    PropertyChangeSupport.firePropertyChange(String, Object, Object) line: 263  
    //    ResetTrainAction(Action).firePropertyChange(String, Object, Object) line: 244   
    //    ResetTrainAction(Action).finishAction(boolean, Object[]) line: 158  
    //    ResetTrainAction(Action).finishAction(boolean) line: 128    
    //    ResetTrainAction.doAction() line: 27    
    //    Automation$1.run() line: 172    
    //    Thread.run() line: 745  

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount()) {
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
                return getCurrentPointer(row, item);
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
            case HIAF_COLUMN:
                return item.isHaltFailureEnabled() & item.getAction().isMessageFailEnabled();
            case MESSAGE_COLUMN:
                if (item.getMessage().equals(AutomationItem.NONE) && item.getMessageFail().equals(AutomationItem.NONE))
                    return Bundle.getMessage("Add");
                else
                    return Bundle.getMessage("ButtonEdit");
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
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
            case HIAF_COLUMN:
                item.setHaltFailureEnabled(((Boolean) value).booleanValue());
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

    private String getCurrentPointer(int row, AutomationItem item) {
        if (_automation.getCurrentAutomationItem() == item) {
            return "    -->"; // NOI18N
        } else {
            return "";
        }
    }

    private JComboBox<Action> getActionComboBox(AutomationItem item) {
        JComboBox<Action> cb = AutomationItem.getActionComboBox();
        //      cb.setSelectedItem(item.getAction()); TODO understand why this didn't work, class?
        for (int index = 0; index < cb.getItemCount(); index++) {
            // select the action based on its action code
            if (item.getAction() != null && (cb.getItemAt(index)).getCode() == item.getAction().getCode()) {
                cb.setSelectedIndex(index);
                break;
            }
        }
        return cb;
    }

    private JComboBox<Train> getTrainComboBox(AutomationItem item) {
        JComboBox<Train> cb = InstanceManager.getDefault(TrainManager.class).getTrainComboBox();
        cb.setSelectedItem(item.getTrain());
        // determine if train combo box is enabled
        cb.setEnabled(item.getAction() != null && item.getAction().isTrainMenuEnabled());
        return cb;
    }

    private JComboBox<RouteLocation> getRouteLocationComboBox(AutomationItem item) {
        JComboBox<RouteLocation> cb = new JComboBox<>();
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
            buttonPane.add(new JLabel("      ")); // some padding
        }

        JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                item.setMessage(messageTextArea.getText());
                item.setMessageFail(messageFailTextArea.getText());
                item.setHaltFailureEnabled(haltCheckBox.isSelected());
                dialog.dispose();
                return;
            }
        });
        buttonPane.add(okayButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dialog.dispose();
                return;
            }
        });
        buttonPane.add(cancelButton);

        JButton defaultMessagesButton = new JButton(Bundle.getMessage("DefaultMessages"));
        defaultMessagesButton.setToolTipText(Bundle.getMessage("TipDefaultButton"));
        defaultMessagesButton.addActionListener(new ActionListener() {
            @Override
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

    // this table listens for changes to a automation and its car types
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY)
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());

        if (e.getPropertyName().equals(Automation.LISTCHANGE_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }
        if (e.getPropertyName().equals(Automation.CURRENT_ITEM_CHANGED_PROPERTY)) {
            SwingUtilities.invokeLater(() -> {
                int row = _list.indexOf(_automation.getCurrentAutomationItem());
                int viewRow = _table.convertRowIndexToView(row);
                // the following line can be responsible for a thread lock
                _table.scrollRectToVisible(_table.getCellRect(viewRow, 0, true));
                fireTableDataChanged();
            });
        }
        // update automation item?
        if (e.getSource().getClass().equals(AutomationItem.class)) {
            AutomationItem item = (AutomationItem) e.getSource();
            int row = _list.indexOf(item);
            if (Control.SHOW_PROPERTY)
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
        if (_automation != null) {
            removePropertyChangeAutomationItems();
            _automation.removePropertyChangeListener(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AutomationTableModel.class);
}
