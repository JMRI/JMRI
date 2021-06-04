package jmri.jmrit.operations.routes.tools;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of route locations used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2013
 */
public class RouteBlockingOrderEditTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int NAME_COLUMN = ID_COLUMN + 1;
    private static final int TRAIN_DIRECTION_COLUMN = NAME_COLUMN + 1;
    private static final int UP_COLUMN = TRAIN_DIRECTION_COLUMN + 1;
    private static final int DOWN_COLUMN = UP_COLUMN + 1;

    private static final int HIGHEST_COLUMN = DOWN_COLUMN + 1;

    private JTable _table;
    private Route _route;
    private RouteBlockingOrderEditFrame _frame;
    List<RouteLocation> _blockingOrderList = new ArrayList<>();

    public RouteBlockingOrderEditTableModel() {
        super();
    }

    private void updateList() {
        if (_route == null) {
            return;
        }
        // first, remove listeners from the individual objects
        removePropertyChangeRouteLocations();
        _blockingOrderList = _route.getBlockingOrder();
        // and add them back in
        for (RouteLocation rl : _blockingOrderList) {
            rl.addPropertyChangeListener(this);
        }
    }

    protected void initTable(RouteBlockingOrderEditFrame frame, JTable table, Route route) {
        _frame = frame;
        _table = table;
        _route = route;
        if (_route != null) {
            _route.addPropertyChangeListener(this);
        }
        initTable(table);
    }

    private void initTable(JTable table) {
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _table.setColumnModel(tcm);
        _table.createDefaultColumnsFromModel();
        // Install the button handlers
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(UP_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(UP_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DOWN_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DOWN_COLUMN).setCellEditor(buttonEditor);
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(40);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(150);
        table.getColumnModel().getColumn(TRAIN_DIRECTION_COLUMN).setPreferredWidth(95);
        table.getColumnModel().getColumn(UP_COLUMN).setPreferredWidth(60);
        table.getColumnModel().getColumn(DOWN_COLUMN).setPreferredWidth(70);

        _frame.loadTableDetails(table);
        // does not use a table sorter
        table.setRowSorter(null);

        updateList();
    }

    @Override
    public int getRowCount() {
        return _blockingOrderList.size();
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
                return Bundle.getMessage("Location");
            case TRAIN_DIRECTION_COLUMN:
                return Bundle.getMessage("TrainDirection");
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ID_COLUMN:
            case NAME_COLUMN:
            case TRAIN_DIRECTION_COLUMN:
                return String.class; 
            case UP_COLUMN:
            case DOWN_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case UP_COLUMN:
            case DOWN_COLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount()) {
            return "ERROR unknown " + row; // NOI18N
        }
        RouteLocation rl = _blockingOrderList.get(row);
        if (rl == null) {
            return "ERROR unknown route location " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return rl.getId();
            case NAME_COLUMN:
                return rl.getName();
            case TRAIN_DIRECTION_COLUMN: {
                return rl.getTrainDirectionString();
            }
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (value == null) {
            log.debug("Warning route table row {} still in edit", row);
            return;
        }
        RouteLocation rl = _blockingOrderList.get(row);
        if (rl == null) {
            log.error("ERROR unknown route location for row: {}", row); // NOI18N
        }
        switch (col) {
            case UP_COLUMN:
                moveUpRouteLocation(rl);
                break;
            case DOWN_COLUMN:
                moveDownRouteLocation(rl);
                break;
            default:
                break;
        }
    }

    private void moveUpRouteLocation(RouteLocation rl) {
        log.debug("move location up");
        _route.setBlockingOrderUp(rl);
    }

    private void moveDownRouteLocation(RouteLocation rl) {
        log.debug("move location down");
        _route.setBlockingOrderDown(rl);
    }

    // this table listens for changes to a route and it's locations
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Route.ROUTE_BLOCKING_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }

        if (e.getSource().getClass().equals(RouteLocation.class)) {
            RouteLocation rl = (RouteLocation) e.getSource();
            int row = _blockingOrderList.indexOf(rl);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update route table row: {} id: {}", row, rl.getId());
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private void removePropertyChangeRouteLocations() {
        for (RouteLocation rl : _blockingOrderList) {
            rl.removePropertyChangeListener(this);
        }
    }

    public void dispose() {
        removePropertyChangeRouteLocations();
        if (_route != null) {
            _route.removePropertyChangeListener(this);
        }
        _blockingOrderList.clear();
        fireTableDataChanged();
    }

    private final static Logger log = LoggerFactory.getLogger(RouteBlockingOrderEditTableModel.class);
}
