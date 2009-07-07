package jmri.jmrit.display;


import jmri.InstanceManager;
import jmri.MemoryManager;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.TurnoutManager;
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

    private ArrayList <NamedBean>       _pickList;

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

    public NamedBean getBeanAt(int index) {
        return _pickList.get(index);
    }

    public int getIndexOf(NamedBean bean) {
        for (int i=0; i<_pickList.size(); i++) {
            if (bean.getSystemName().equals(_pickList.get(i).getSystemName())) {
                return i;
            }
        }
        return -1;
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

    static PickListModel turnoutPickModelInstance() {
        return new TurnoutPickModel();
    }
    static PickListModel sensorPickModelInstance() {
        return new SensorPickModel();
    }
    static PickListModel signalPickModelInstance() {
        return new SignalPickModel();
    }
    static PickListModel memoryPickModelInstance() {
        return new MemoryPickModel();
    }
    static PickListModel reporterPickModelInstance() {
        return new ReporterPickModel();
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PickListModel.class.getName());
}

    class TurnoutPickModel extends PickListModel {
        TurnoutManager manager;
        TurnoutPickModel () {
            manager = InstanceManager.turnoutManagerInstance();
        }
        Manager getManager() {
            return manager;
        }
        NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        NamedBean addBean(String name) {
            return manager.provideTurnout(name);
        }
    }

    class SensorPickModel extends PickListModel {
        SensorManager manager;
        SensorPickModel () {
            manager = InstanceManager.sensorManagerInstance();
        }
        Manager getManager() {
            return manager;
        }
        NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        NamedBean addBean(String name) {
            return manager.provideSensor(name);
        }
    }

    class SignalPickModel extends PickListModel {
        SignalHeadManager manager;
        SignalPickModel () {
            manager = InstanceManager.signalHeadManagerInstance();
        }
        Manager getManager() {
            return manager;
        }
        NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        NamedBean addBean(String name) {
            return manager.getSignalHead(name);
        }
    }


    class MemoryPickModel extends PickListModel {
        MemoryManager manager;
        MemoryPickModel () {
            manager = InstanceManager.memoryManagerInstance();
        }
        Manager getManager() {
            return manager;
        }
        NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        NamedBean addBean(String name) {
            return manager.provideMemory(name);
        }
    }

    class ReporterPickModel extends PickListModel {
        ReporterManager manager;
        ReporterPickModel () {
            manager = InstanceManager.reporterManagerInstance();
        }
        Manager getManager() {
            return manager;
        }
        NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        NamedBean addBean(String name) {
            return manager.provideReporter(name);
        }
    }
