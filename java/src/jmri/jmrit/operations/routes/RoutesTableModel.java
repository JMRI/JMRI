// RoutesTableModel.java
package jmri.jmrit.operations.routes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of routes used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2015
 * @version $Revision$
 */
public class RoutesTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    RouteManager routemanager; // There is only one manager

    // Defines the columns
    public static final int ID_COLUMN = 0;
    public static final int NAME_COLUMN = ID_COLUMN + 1;
    public static final int COMMENT_COLUMN = NAME_COLUMN + 1;
    public static final int MIN_LENGTH_COLUMN = COMMENT_COLUMN +1;
    public static final int MAX_LENGTH_COLUMN = MIN_LENGTH_COLUMN +1;
    public static final int STATUS_COLUMN = MAX_LENGTH_COLUMN + 1;
    public static final int EDIT_COLUMN = STATUS_COLUMN + 1;

    private static final int HIGHESTCOLUMN = EDIT_COLUMN + 1;

    public RoutesTableModel() {
        super();
        routemanager = RouteManager.instance();
        routemanager.addPropertyChangeListener(this);
        LocationManager.instance().addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;

    private int _sort = SORTBYNAME;

    public void setSort(int sort) {
        synchronized (this) {
            _sort = sort;
        }
        updateList();
        fireTableDataChanged();
    }

    private synchronized void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeRoutes();

        if (_sort == SORTBYID) {
            sysList = routemanager.getRoutesByIdList();
        } else {
            sysList = routemanager.getRoutesByNameList();
        }
        // and add them back in
        for (Route route : sysList) {
            route.addPropertyChangeListener(this);
        }
    }

    List<Route> sysList = null;

    void initTable(RoutesTableFrame frame, JTable table) {
        // Install the button handlers
        TableColumnModel tcm = table.getColumnModel();
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);
        
        setPreferredWidths(frame, table);

        table.setRowHeight(new JComboBox<>().getPreferredSize().height);
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    
    private void setPreferredWidths(RoutesTableFrame frame, JTable table) {
        if (frame.loadTableDetails(table)) {
            return; // done
        }
        log.debug("Setting preferred widths");
        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(30);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(220);
        table.getColumnModel().getColumn(COMMENT_COLUMN).setPreferredWidth(380);
        table.getColumnModel().getColumn(STATUS_COLUMN).setPreferredWidth(70);
        table.getColumnModel().getColumn(MIN_LENGTH_COLUMN).setPreferredWidth(75);
        table.getColumnModel().getColumn(MAX_LENGTH_COLUMN).setPreferredWidth(75);
        table.getColumnModel().getColumn(EDIT_COLUMN).setPreferredWidth(80);
    }

    @Override
    public synchronized int getRowCount() {
        return sysList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHESTCOLUMN;
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
            case MIN_LENGTH_COLUMN:
                return Bundle.getMessage("MinLength");
            case MAX_LENGTH_COLUMN:
                return Bundle.getMessage("MaxLength");
            case STATUS_COLUMN:
                return Bundle.getMessage("Status");
            case EDIT_COLUMN:
                return Bundle.getMessage("Edit");
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
            case MIN_LENGTH_COLUMN:
                return String.class;
            case MAX_LENGTH_COLUMN:
                return String.class;
            case STATUS_COLUMN:
                return String.class;
            case EDIT_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case EDIT_COLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public synchronized Object getValueAt(int row, int col) {
        if (row >= sysList.size()) {
            return "ERROR unknown " + row; // NOI18N
        }
        Route route = sysList.get(row);
        if (route == null) {
            return "ERROR route unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return route.getId();
            case NAME_COLUMN:
                return route.getName();
            case COMMENT_COLUMN:
                return route.getComment();
            case MIN_LENGTH_COLUMN:
                return route.getRouteMinimumTrainLength();
            case MAX_LENGTH_COLUMN:
                return route.getRouteMaximumTrainLength();
            case STATUS_COLUMN:
                return route.getStatus();
            case EDIT_COLUMN:
                return Bundle.getMessage("Edit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case EDIT_COLUMN:
                editRoute(row);
                break;
            default:
                break;
        }
    }

    RouteEditFrame ref = null;

    private synchronized void editRoute(int row) {
        log.debug("Edit route");
        if (ref != null) {
            ref.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ref = new RouteEditFrame();
                Route route = sysList.get(row);
                ref.initComponents(route);
            }
        });
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            fireTableDataChanged();
        } else if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        } else if (e.getSource().getClass().equals(Route.class)) {
            Route route = (Route) e.getSource();
            int row = sysList.indexOf(route);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update route table row: {} id: {}", row, route.getId());
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private synchronized void removePropertyChangeRoutes() {
        if (sysList != null) {
            for (Route route : sysList) {
                route.removePropertyChangeListener(this);
            }
        }
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
        if (ref != null) {
            ref.dispose();
        }
        routemanager.removePropertyChangeListener(this);
        LocationManager.instance().removePropertyChangeListener(this);
        removePropertyChangeRoutes();
    }

    private final static Logger log = LoggerFactory.getLogger(RoutesTableModel.class.getName());
}
