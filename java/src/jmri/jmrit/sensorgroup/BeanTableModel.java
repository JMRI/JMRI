package jmri.jmrit.sensorgroup;

import javax.swing.table.AbstractTableModel;
import jmri.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for simple bean tables for insertion in other GUI elements
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public abstract class BeanTableModel extends AbstractTableModel {

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == INCLUDE_COLUMN) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    public abstract Manager getManager();

    @Override
    public int getColumnCount() {
        return INCLUDE_COLUMN + 1;
    }

    @Override
    public int getRowCount() {
        return getManager().getNamedBeanSet().size();
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return (c == INCLUDE_COLUMN);
    }

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final int INCLUDE_COLUMN = 2;

    @Override
    public String getColumnName(int c) {
        switch (c) {
            case SNAME_COLUMN:
                return Bundle.getMessage("ColumnSystemName");
            case UNAME_COLUMN:
                return Bundle.getMessage("ColumnUserName");
            case INCLUDE_COLUMN:
                return Bundle.getMessage("ColumnIncluded");
            default:
                return "";
        }
    }

    /**
     * User name column must be handled by subclass
     */
    @Override
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations & generics
    public Object getValueAt(int r, int c) {
        switch (c) {
            case SNAME_COLUMN:  // slot number
                return getManager().getSystemNameList().get(r);
            default:
                log.warn("getValueAt should not have been asked about c=" + c);
                return null;
        }
    }
    private final static Logger log = LoggerFactory.getLogger(BeanTableModel.class);
}
