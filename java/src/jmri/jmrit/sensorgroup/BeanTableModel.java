// BeanTableModel.java

package jmri.jmrit.sensorgroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Manager;
import javax.swing.table.AbstractTableModel;

/**
 * Abstract base for simple bean tables for insertion in other GUI elements
 *
 * @author Bob Jacobsen Copyright (C) 2007 
 *
 * @version     $Revision$
 */

public abstract class BeanTableModel extends AbstractTableModel {
    public Class<?> getColumnClass(int c) {
        if (c == INCLUDE_COLUMN) {
            return Boolean.class;
        }
        else {
            return String.class;
        }
    }

    public abstract Manager getManager();
    
    public int getColumnCount () {return INCLUDE_COLUMN+1;}

    public int getRowCount () {
        return getManager().getSystemNameList().size();
    }
    public boolean isCellEditable(int r,int c) {
        return ( c==INCLUDE_COLUMN );
    }
    
    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final int INCLUDE_COLUMN = 2;

    public String getColumnName(int c) {
        switch (c) {
        case SNAME_COLUMN:
            return "System Name";
        case UNAME_COLUMN:
            return "User Name";
        case INCLUDE_COLUMN:
            return "Included";
        default:
            return "";
        }
    }
    
    /**
     * User name column must be handled by subclass
     */
    public Object getValueAt (int r,int c) {
        switch (c) {
        case SNAME_COLUMN:  // slot number
            return getManager().getSystemNameList().get(r);
        default:
            log.warn("getValueAt should not have been asked about c="+c);
            return null;
        }
    }
    static final Logger log = LoggerFactory.getLogger(BeanTableModel.class.getName());
}


/* @(#)BeanTableModel.java */
