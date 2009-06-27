package jmri.jmrit.display;


import jmri.NamedBean;
import jmri.Manager;
import jmri.util.NamedBeanComparator;

import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;

/**
* Table model for pick lists in IconAdder
*/
public abstract class PickListModel extends AbstractTableModel implements PropertyChangeListener {

    ArrayList <NamedBean>       _pickList;

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public PickListModel() {
        super();
    }

    public void init() {
        getManager().addPropertyChangeListener(this);   // for adds and deletes
        makePickList();
    }

    public NamedBean getIndexOf(int index) {
        return _pickList.get(index);
    }

    private void makePickList() {
        // Don't know who is added or deleted so remove all name change listeners
        if (_pickList != null) {
            for (int i=0; i<_pickList.size(); i++) {
                _pickList.get(i).removePropertyChangeListener(this);
            }
        }
        TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());

        List <String> systemNameList = getManager().getSystemNameList();
        Iterator <String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            ts.add(getBySystemName(iter.next()));
        }
        _pickList = new ArrayList <NamedBean> (ts.size());

        Iterator <NamedBean> it = ts.iterator();
        while(it.hasNext()) {
            NamedBean elt = it.next();
            _pickList.add(elt);
        }
        // add name change listeners
        for (int i=0; i<_pickList.size(); i++) {
            _pickList.get(i).addPropertyChangeListener(this);
        }
        if (log.isDebugEnabled()) log.debug("_pickList has "+_pickList.size()+" beans");
    }

    abstract Manager getManager();
    abstract NamedBean getBySystemName(String name);
    abstract NamedBean addBean(String name);

    public Class<?> getColumnClass(int c) {
            return String.class;
    }

    public int getColumnCount () {
        return 2;
    }

    public String getColumnName(int c) {
        if (c == SNAME_COLUMN) {
            return rb.getString("SystemName");
        } else if (c == UNAME_COLUMN) {
            return rb.getString("UserName");
        }
        return "";
    }

    public boolean isCellEditable(int r,int c) {
        return ( false );
    }

    public int getRowCount () {
        return _pickList.size();
    }
    public Object getValueAt (int r,int c) {
        if (c == SNAME_COLUMN) {
            return _pickList.get(r).getSystemName();
        } else if (c == UNAME_COLUMN) {
            return _pickList.get(r).getUserName();
        }
        return null;
    }
    public void setValueAt(Object type,int r,int c) {
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a NamedBean added or deleted
            makePickList();
            fireTableDataChanged();
        } else {
            // a value changed.  Find it, to avoid complete redraw
            NamedBean bean = (NamedBean)e.getSource();
            for (int i=0; i<_pickList.size(); i++) {
                if (bean.equals(_pickList.get(i)))  {
                    fireTableRowsUpdated(i, i);
                    return;
                }
            }
        }
    }

    public void dispose() {
        getManager().removePropertyChangeListener(this);
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PickListModel.class.getName());
}

