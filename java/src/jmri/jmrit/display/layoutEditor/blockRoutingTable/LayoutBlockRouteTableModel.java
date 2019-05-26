package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import java.beans.PropertyChangeListener;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Roster variable values.
 * <p>
 * Any desired ordering, etc, is handled outside this class.
 * <p>
 * The initial implementation doesn't automatically update when roster entries
 * change, doesn't allow updating of the entries, and only shows some of the
 * fields. But it's a start....
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010
 * @since 2.7.5
 */
public class LayoutBlockRouteTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    public static final int DESTCOL = 0;
    static final int NEXTHOPCOL = 1;
    static final int HOPCOUNTCOL = 2;
    static final int DIRECTIONCOL = 3;
    static final int METRICCOL = 4;
    static final int LENGTHCOL = 5;
    static final int STATECOL = 6;
    static final int VALIDCOL = 7;

    static final int NUMCOL = 7 + 1;

    boolean editable = false;

    public LayoutBlockRouteTableModel(boolean editable, LayoutBlock lBlock) {
        this.editable = editable;
        this.lBlock = lBlock;
        lBlock.addPropertyChangeListener(this);
    }

    @Override
    public int getRowCount() {
        return lBlock.getNumberOfRoutes();
    }

    @Override
    public int getColumnCount() {
        return NUMCOL;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case DESTCOL:
                return Bundle.getMessage("Destination");
            case NEXTHOPCOL:
                return Bundle.getMessage("NextHop");
            case HOPCOUNTCOL:
                return Bundle.getMessage("HopCount");
            case DIRECTIONCOL:
                return Bundle.getMessage("Direction");
            case METRICCOL:
                return Bundle.getMessage("Metric");
            case LENGTHCOL:
                return Bundle.getMessage("Length");
            case STATECOL:
                return Bundle.getMessage("State");
            case VALIDCOL:
                return Bundle.getMessage("Valid");

            default:
                return "<UNKNOWN>";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == HOPCOUNTCOL) {
            return Integer.class;
        } else if (col == METRICCOL) {
            return Integer.class;
        } else if (col == LENGTHCOL) {
            return Float.class;
        } else {
            return String.class;
        }
    }

    /**
     * Editable state must be set in ctor.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("routing")) {
            fireTableDataChanged();
        } else if (matchPropertyName(e)) {
            // a value changed.  Find it, to avoid complete redraw
            int row = (Integer) e.getNewValue();
            // since we can add columns, the entire row is marked as updated
            fireTableRowsUpdated(row, row);
        }
    }

    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return (e.getPropertyName().indexOf("state") >= 0 || e.getPropertyName().indexOf("hop") >= 0
                || e.getPropertyName().indexOf("metric") >= 0 || e.getPropertyName().indexOf("valid") >= 0
                || e.getPropertyName().indexOf("neighbourmetric") >= 0);
    }

    /**
     * Provides the empty String if attribute doesn't exist.
     */
    @Override
    public Object getValueAt(int row, int col) {
        // get roster entry for row
        if (lBlock == null) {
            log.debug("layout Block is null!");
            return "Error";
        }
        switch (col) {
            case DESTCOL:
                return lBlock.getRouteDestBlockAtIndex(row).getDisplayName();
            case NEXTHOPCOL:
                String nextBlock = lBlock.getRouteNextBlockAtIndex(row).getDisplayName();
                if (nextBlock.equals(lBlock.getDisplayName())) {
                    nextBlock = Bundle.getMessage("DirectConnect");
                }
                return nextBlock;
            case HOPCOUNTCOL:
                return Integer.valueOf(lBlock.getRouteHopCountAtIndex(row));
            case DIRECTIONCOL:
                return jmri.Path.decodeDirection(Integer.valueOf(lBlock.getRouteDirectionAtIndex(row)));
            case METRICCOL:
                return Integer.valueOf(lBlock.getRouteMetric(row));
            case LENGTHCOL:
                return Float.valueOf(lBlock.getRouteLengthAtIndex(row));
            case STATECOL:
                return lBlock.getRouteStateAsString(row);
            case VALIDCOL:
                String value = "";
                if (lBlock.getRouteValid(row)) {
                    value = "*";
                }
                return value;
            default:
                return "<UNKNOWN>";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        return;
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new javax.swing.JLabel(getColumnName(column)).getPreferredSize().width + 15);  // leave room for sorter arrow
        for (int row = 0; row < getRowCount(); row++) {
            if (getColumnClass(column).equals(String.class)) {
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            } else if (getColumnClass(column).equals(Integer.class)) {
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            }
        }
        return retval + 5;
    }

    // drop listeners
    public void dispose() {
    }

    public jmri.Manager getManager() {
        return jmri.InstanceManager.getDefault(LayoutBlockManager.class);
    }

    private LayoutBlock lBlock = null;

    private final static Logger log = LoggerFactory.getLogger(LayoutBlockRouteTableModel.class);
}
