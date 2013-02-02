package jmri.jmrit.picker;


import org.apache.log4j.Logger;
import jmri.*;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.signalling.EntryExitPairs;

import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jmri.util.NamedBeanComparator;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmri.util.com.sun.TableSorter;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

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
 * @author      Pete Cressman Copyright (C) 2009, 2010
 * @version
 */
/**
* Table model for pick lists in IconAdder
*/
public abstract class PickListModel extends AbstractTableModel implements PropertyChangeListener {

    protected ArrayList <NamedBean> _pickList;
    protected String _name;
    private JTable  _table;       // table using this model
    protected TableSorter _sorter;

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final int POSITION_COL = 2;
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    static HashMap<String,Integer> _listMap = new HashMap<String,Integer>();

    static public int getNumInstances(String type) {
        Integer num = _listMap.get(type.toLowerCase());
        if (log.isDebugEnabled()) log.debug("getNumInstances of "+type+" num= "+num);
        if (num!=null) {
            return num.intValue();
        }
        return 0;
    }

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

    /**
     * If table has been sorted table row no longer is the same as array index
     * @param index = row of table
     */
    public NamedBean getBeanAt(int index) {
    	index = _sorter.modelIndex(index);
    	if (index >=_pickList.size()) {
    		return null;
    	}
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

    /**
    * Return bean with name given in parameter.  Create if needed and possible
    */
    abstract public NamedBean addBean(String name);
    abstract public NamedBean addBean(String sysName, String userName);
    /**
    * Return true if model can create beans
    */
    abstract public boolean canAddBean();

    public Class<?> getColumnClass(int c) {
            return String.class;
    }

    public int getColumnCount () {
        return 2;
    }

    public String getColumnName(int c) {
        if (c == SNAME_COLUMN) {
            return rb.getString("ColumnSystemName");
        } else if (c == UNAME_COLUMN) {
            return rb.getString("ColumnUserName");
        }
        return "";
    }

    public boolean isCellEditable(int r,int c) {
        return false;
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
    
    public String getName() {
        return _name;
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a NamedBean added or deleted
            makePickList();
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("DisplayListName")){
            //This is a call from the manager, which can be ignored
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

    /**
    * Make pick table, DND enabled
    */
    public JTable makePickTable() {
        this.init();
        try {   // following might fail due to a missing method on Mac Classic
        	_sorter = new TableSorter(this);
            _table = jmri.util.JTableUtil.sortableDataModel(_sorter);
            _sorter.setTableHeader(_table.getTableHeader());
            _sorter.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            _table.setModel(_sorter);
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            log.error("makePickTable: Unexpected error: "+e);
            _table = new JTable(this);
        }

        _table.setRowSelectionAllowed(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setPreferredScrollableViewportSize(new java.awt.Dimension(250,_table.getRowHeight()*7));
        _table.setDragEnabled(true);
        _table.setTransferHandler(new jmri.util.DnDTableExportHandler());
        TableColumnModel columnModel = _table.getColumnModel();

        TableColumn sNameColumnT = columnModel.getColumn(SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(50);
        //sNameColumnT.setMaxWidth(200);

        TableColumn uNameColumnT = columnModel.getColumn(UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(100);
        //uNameColumnT.setMaxWidth(300);

        return _table;
    }
    
    public void makeSorter(JTable table) {
        try {   // following might fail due to a missing method on Mac Classic
        	_sorter = new TableSorter(this);
            _sorter.setTableHeader(table.getTableHeader());
            _sorter.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            table.setModel(_sorter);
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            log.error("makeSorter: Unexpected error: "+e);
        }
    }

    public JTable getTable() {
        return _table;
    }


    public void dispose() {
        getManager().removePropertyChangeListener(this);
    }

    public static PickListModel turnoutPickModelInstance() {
        Integer num = _listMap.get("turnout");
        if (num!=null) {
            _listMap.put("turnout", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("turnout", Integer.valueOf(1));
        }
        return new TurnoutPickModel();
    }
    public static PickListModel sensorPickModelInstance() {
        Integer num = _listMap.get("sensor");
        if (num!=null) {
            _listMap.put("sensor", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("sensor", Integer.valueOf(1));
        }
        return new SensorPickModel();
    }
    public static PickListModel multiSensorPickModelInstance() {
        Integer num = _listMap.get("multisensor");
        if (num!=null) {
            _listMap.put("multisensor", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("multisensor", Integer.valueOf(1));
        }
        return new MultiSensorPickModel();
    }
    public static PickListModel signalHeadPickModelInstance() {
        Integer num = _listMap.get("sensor");
        if (num!=null) {
            _listMap.put("signalhead", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("signalhead", Integer.valueOf(1));
        }
        return new SignalHeadPickModel();
    }
    public static PickListModel signalMastPickModelInstance() {
        Integer num = _listMap.get("signalmast");
        if (num!=null) {
            _listMap.put("signalmast", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("signalmast", Integer.valueOf(1));
        }
        return new SignalMastPickModel();
    }
    public static PickListModel memoryPickModelInstance() {
        Integer num = _listMap.get("memory");
        if (num!=null) {
            _listMap.put("memory", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("memory", Integer.valueOf(1));
        }
        return new MemoryPickModel();
    }
    public static PickListModel reporterPickModelInstance() {
        Integer num = _listMap.get("reporter");
        if (num!=null) {
            _listMap.put("reporter", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("reporter", Integer.valueOf(1));
        }
        return new ReporterPickModel();
    }
    public static PickListModel lightPickModelInstance() {
        Integer num = _listMap.get("light");
        if (num!=null) {
            _listMap.put("light", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("light", Integer.valueOf(1));
        }
        return new LightPickModel();
    }
    public static PickListModel oBlockPickModelInstance() {
        Integer num = _listMap.get("oBlock");
        if (num!=null) {
            _listMap.put("oBlock", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("oBlock", Integer.valueOf(1));
        }
        return new OBlockPickModel();
    }
    public static PickListModel warrantPickModelInstance() {
        Integer num = _listMap.get("warrant");
        if (num!=null) {
            _listMap.put("warrant", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("warrant", Integer.valueOf(1));
        }
        return new WarrantPickModel();
    }
    public static PickListModel conditionalPickModelInstance() {
        Integer num = _listMap.get("conditional");
        if (num!=null) {
            _listMap.put("conditional", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("conditional", Integer.valueOf(1));
        }
        return new ConditionalPickModel();
    }
    
    public static PickListModel entryExitPickModelInstance() {
        Integer num = _listMap.get("entryExit");
        if (num!=null) {
            _listMap.put("entryExit", Integer.valueOf(num.intValue()+1));
        } else {
            _listMap.put("entryExit", Integer.valueOf(1));
        }
        return new EntryExitPickModel();
    }

    static final Logger log = Logger.getLogger(PickListModel.class.getName());
}

    class TurnoutPickModel extends PickListModel {
        TurnoutManager manager;
        TurnoutPickModel () {
            manager = InstanceManager.turnoutManagerInstance();
            _name = rb.getString("TitleTurnoutTable");
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
        public NamedBean addBean(String sysName, String userName) {
            return manager.newTurnout(sysName, userName);
        }
        public boolean canAddBean() {
            return true;
        }
    }

    class SensorPickModel extends PickListModel {
        SensorManager manager;
        SensorPickModel () {
            manager = InstanceManager.sensorManagerInstance();
            _name = rb.getString("TitleSensorTable");
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
        public NamedBean addBean(String sysName, String userName) {
            return manager.newSensor(sysName, userName);
        }
        public boolean canAddBean() {
            return true;
        }
    }

    class MultiSensorPickModel extends SensorPickModel {
        private HashMap <Integer, String> _position = new HashMap <Integer, String> ();
        MultiSensorPickModel () {
            super();
        }
        public Object getValueAt (int r, int c) {
            if (c==POSITION_COL) {
                return _position.get(Integer.valueOf(r));
            }
            return super.getValueAt(r, c);
        }
        public void setValueAt(Object type,int r,int c) {
            if (c==POSITION_COL) {
                _position.put( Integer.valueOf(r), (String)type);
            }
        }
    }

    class SignalHeadPickModel extends PickListModel {
        SignalHeadManager manager;
        SignalHeadPickModel () {
            manager = InstanceManager.signalHeadManagerInstance();
            _name = rb.getString("TitleSignalTable");
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
        public NamedBean addBean(String sysName, String userName) {
            SignalHead sh = manager.getSignalHead(userName);
            if (sh==null) {
                sh = manager.getSignalHead(sysName);
            }
            return sh;
        }
        public boolean canAddBean() {
            return false;
        }
    }

    class SignalMastPickModel extends PickListModel {
        SignalMastManager manager;
        SignalMastPickModel () {
            manager = InstanceManager.signalMastManagerInstance();
            _name = rb.getString("TitleSignalMastTable");
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideSignalMast(name);
        }
        public NamedBean addBean(String sysName, String userName) {
            SignalMast sm = manager.getSignalMast(userName);
            if (sm==null) {
                sm = manager.provideSignalMast(sysName);
            }
            return sm;
        }
        public boolean canAddBean() {
            return false;
        }
    }

    class MemoryPickModel extends PickListModel {
        MemoryManager manager;
        MemoryPickModel () {
            manager = InstanceManager.memoryManagerInstance();
            _name = rb.getString("TitleMemoryTable");
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
        public NamedBean addBean(String sysName, String userName) {
            return manager.newMemory(sysName, userName);
        }
        public boolean canAddBean() {
            return true;
        }
    }

    class ReporterPickModel extends PickListModel {
        ReporterManager manager;
        ReporterPickModel () {
            manager = InstanceManager.reporterManagerInstance();
            _name = rb.getString("TitleReporterTable");
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
        public NamedBean addBean(String sysName, String userName) {
            return manager.newReporter(sysName, userName);
        }
        public boolean canAddBean() {
            return true;
        }
    }

    class LightPickModel extends PickListModel {
        LightManager manager;
        LightPickModel () {
            manager = InstanceManager.lightManagerInstance();
            _name = rb.getString("TitleLightTable");
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
        public NamedBean addBean(String sysName, String userName) {
            return manager.newLight(sysName, userName);
        }
        public boolean canAddBean() {
            return true;
        }
    }

    class OBlockPickModel extends PickListModel {
        OBlockManager manager;
        OBlockPickModel () {
            manager = InstanceManager.oBlockManagerInstance();
            _name = rb.getString("TitleBlockTable");
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
        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewOBlock(sysName, userName);
        }
        public boolean canAddBean() {
            return true;
        }
    }

    class WarrantPickModel extends PickListModel {
        WarrantManager manager;
        WarrantPickModel () {
            manager = InstanceManager.warrantManagerInstance();
            _name = rb.getString("TitleWarrantTable");
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.provideWarrant(name);
        }
        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewWarrant(sysName, userName);
        }
        public boolean canAddBean() {
            return false;
        }
    }
    class ConditionalPickModel extends PickListModel {
        ConditionalManager manager;
        ConditionalPickModel () {
            manager = InstanceManager.conditionalManagerInstance();
            _name = rb.getString("TitleConditionalTable");
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        public NamedBean addBean(String name) {
            return manager.createNewConditional(name, null);
        }
        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewConditional(sysName, userName);
        }
        public boolean canAddBean() {
            return false;
        }

        public JTable makePickTable() {
            JTable table = super.makePickTable();
            TableColumn column = new TableColumn(PickListModel.POSITION_COL);
            column.setResizable(true);
            column.setMinWidth(100);
            column.setHeaderValue("Logix");
            table.addColumn(column);
            return table;
        }
        
        public Object getValueAt (int r, int c) {
            if (c==POSITION_COL) {
                jmri.Logix l = manager.getParentLogix(_pickList.get(r).getSystemName());
                if (l!=null) {
                    return l.getDisplayName();
                }
            }
            return super.getValueAt(r, c);
        }
    }
    
    class EntryExitPickModel extends PickListModel {
        
        EntryExitPairs manager;
        EntryExitPickModel () {
            manager = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);
            _name = rb.getString("TitleEntryExitTable");
        }
        public Manager getManager() {
            return manager;
        }
        public NamedBean getBySystemName(String name) {
            return manager.getBySystemName(name);
        }
        
        public NamedBean addBean(String name) {
            return null;
        }
        public NamedBean addBean(String sysName, String userName) {
            return null;
        }
        public boolean canAddBean() {
            return false;
        }
        
        public String getColumnName(int c) {
            if (c == SNAME_COLUMN) {
                return "Unique Id";
            } else if (c == UNAME_COLUMN) {
                return rb.getString("ColumnUserName");
            }
            return "";
        }
    }

