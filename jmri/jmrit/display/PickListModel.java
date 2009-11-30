package jmri.jmrit.display;


import jmri.InstanceManager;
import jmri.LightManager;
import jmri.MemoryManager;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.TurnoutManager;
import jmri.NamedBean;
import jmri.Manager;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.NamedBeanComparator;

import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;

/**
 * Abstract class to make pick lists for NamedBeans.
 * <P>
 * Concrete pick list classe for many beans are include at the end of
 * this file.  This class also has instantiation methods serve as a factory
 * for those classes.
 * <P>
 * Note: Extensions of this class must call init() after instantiation.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author      Pete Cressman Copyright (C) 2009
 * @version
 */
/**
* Table model for pick lists in IconAdder
*/
public abstract class PickListModel extends AbstractTableModel implements PropertyChangeListener {

    private ArrayList <NamedBean>       _pickList;

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    /**
    * Default constructor makes a table sorted by System Name.
    */
    public PickListModel() {
        super();
    }

    /**
    * Subclasses MUST call this method at creation
    */
    public void init() {
        //log.debug("manager "+getManager());
        getManager().addPropertyChangeListener(this);   // for adds and deletes
        makePickList();
    }

    public NamedBean getBeanAt(int index) {
        return _pickList.get(index);
    }

    public int getIndexOf(NamedBean bean) {
        for (int i=0; i<_pickList.size(); i++) {
            if (_pickList.get(i).equals(bean)) {
                return i;
            }
        }
        return -1;
    }

    public List <NamedBean> getBeanList() {
        return _pickList;
    }

    private void makePickList() {
        // Don't know who is added or deleted so remove all name change listeners
        if (_pickList != null) {
            for (int i=0; i<_pickList.size(); i++) {
                _pickList.get(i).removePropertyChangeListener(this);
            }
        }
        List <String> systemNameList = getManager().getSystemNameList();
        TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());

        Iterator <String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            ts.add(getBySystemName(iter.next()));
        }
        _pickList = new ArrayList <NamedBean> (systemNameList.size());

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

    abstract public Manager getManager();
    abstract public NamedBean getBySystemName(String name);
    abstract public NamedBean addBean(String name);

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
    	// some error checking
    	if (r >= _pickList.size()){
    		log.debug("row is greater than picklist size");
    		return null;
    	}
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
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("propertyChange of \""+e.getPropertyName()+
                                            "\" for "+e.getSource().toString());
    }

    public void dispose() {
        getManager().removePropertyChangeListener(this);
    }

    public static PickListModel turnoutPickModelInstance() {
        return new TurnoutPickModel();
    }
    public static PickListModel sensorPickModelInstance() {
        return new SensorPickModel();
    }
    public static PickListModel signalPickModelInstance() {
        return new SignalPickModel();
    }
    public static PickListModel memoryPickModelInstance() {
        return new MemoryPickModel();
    }
    public static PickListModel reporterPickModelInstance() {
        return new ReporterPickModel();
    }
    public static PickListModel lightPickModelInstance() {
        return new LightPickModel();
    }
    public static PickListModel oBlockPickModelInstance() {
        return new OBlockPickModel();
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PickListModel.class.getName());
}

    class TurnoutPickModel extends PickListModel {
        TurnoutManager manager;
        TurnoutPickModel () {
            manager = InstanceManager.turnoutManagerInstance();
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideTurnout(name);
        }
    }

    class SensorPickModel extends PickListModel {
        SensorManager manager;
        SensorPickModel () {
            manager = InstanceManager.sensorManagerInstance();
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideSensor(name);
        }
    }

    class SignalPickModel extends PickListModel {
        SignalHeadManager manager;
        SignalPickModel () {
            manager = InstanceManager.signalHeadManagerInstance();
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.getSignalHead(name);
        }
    }


    class MemoryPickModel extends PickListModel {
        MemoryManager manager;
        MemoryPickModel () {
            manager = InstanceManager.memoryManagerInstance();
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideMemory(name);
        }
    }

    class ReporterPickModel extends PickListModel {
        ReporterManager manager;
        ReporterPickModel () {
            manager = InstanceManager.reporterManagerInstance();
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideReporter(name);
        }
    }

    class LightPickModel extends PickListModel {
        LightManager manager;
        LightPickModel () {
            manager = InstanceManager.lightManagerInstance();
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideLight(name);
        }
    }

    class OBlockPickModel extends PickListModel {
        OBlockManager manager;
        OBlockPickModel () {
            manager = InstanceManager.oBlockManagerInstance();
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideOBlock(name);
        }
    }

