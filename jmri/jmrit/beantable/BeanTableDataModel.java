// BeanTableDataModel.java

package jmri.jmrit.beantable;

import javax.swing.*;
import jmri.*;
import javax.swing.table.*;
import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collections;
import java.beans.*;

/**
 * Table data model for display of NamedBean manager contents
 * @author		Bob Jacobsen   Copyright (C) 2003
 * @version		$Revision: 1.1 $
 */
abstract public class BeanTableDataModel extends javax.swing.table.AbstractTableModel
            implements PropertyChangeListener  {

    static public final int SYSNAMECOL  = 0;
    static public final int USERNAMECOL = 1;
    static public final int VALUECOL = 2;


    static public final int NUMCOLUMN = 3;

    public BeanTableDataModel() {
        super();
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    synchronized void updateNameList() {
        // first, remove listeners from the individual objects
        if (sysNameList != null) {
            for (int i = 0; i< sysNameList.size(); i++)
                getBySystemName((String)sysNameList.get(i)).removePropertyChangeListener(this);
        }
        sysNameList = getManager().getSystemNameList();
        // and add them back in
        for (int i = 0; i< sysNameList.size(); i++)
            getBySystemName((String)sysNameList.get(i)).addPropertyChangeListener(this);
    }

    List sysNameList = null;

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            updateNameList();
            log.debug("Table changed length to "+sysNameList.size());
            fireTableDataChanged();
        } else if (e.getPropertyName().indexOf("State")>0) {
            // a value changed.  Find it, to avoid complete redraw
            String name = ((NamedBean)e.getSource()).getSystemName();
            if (log.isDebugEnabled()) log.debug("Update cell "+sysNameList.indexOf(name)+","
                                                +VALUECOL+" for "+name);
            fireTableCellUpdated(sysNameList.indexOf(name), VALUECOL);
        }
    }

    public int getRowCount() {
        return sysNameList.size();
    }


    public int getColumnCount( ){ return NUMCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case SYSNAMECOL: return "System Name";
        case USERNAMECOL: return "User Name";
        case VALUECOL: return "State";

        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case SYSNAMECOL:
        case USERNAMECOL:
        case VALUECOL:
            return String.class;
        default:
            return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case USERNAMECOL:
        case VALUECOL:
            return true;
        default:
            return false;
        }
    }

    public Object getValueAt(int row, int col) {
        switch (col) {
        case SYSNAMECOL:  // slot number
            return sysNameList.get(row);
        case USERNAMECOL:  //
            return getBySystemName((String)sysNameList.get(row)).getUserName();
        case VALUECOL:  //
            return getValue((String)sysNameList.get(row));;
        default:
            log.error("internal state inconsistent with table requst for "+row+" "+col);
            return null;
        }
    };

    public int getPreferredWidth(int col) {
        switch (col) {
        case SYSNAMECOL:
            return new JLabel(" 123 ").getPreferredSize().width;
        case USERNAMECOL:
            return new JLabel(" 12345678 ").getPreferredSize().width;
        case VALUECOL:
            return new JLabel(" CLOSED ").getPreferredSize().width;
        default:
            return new JLabel(" <unknown> ").getPreferredSize().width;
        }
    }

    abstract public String getValue(String systemName);

    abstract Manager getManager();

    abstract NamedBean getBySystemName(String name);

    public void setValueAt(Object value, int row, int col) {
        if (col==USERNAMECOL) {
            getBySystemName((String)sysNameList.get(row))
                        .setUserName((String)value);
            fireTableRowsUpdated(row,row);
        }
    }

    /**
     * Configure a table to have our standard rows and columns.
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param s
     */
    public void configureTable(JTable table) {
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    synchronized public void dispose() {
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i< sysNameList.size(); i++)
                getBySystemName((String)sysNameList.get(i)).removePropertyChangeListener(this);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BeanTableDataModel.class.getName());

}
