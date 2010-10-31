package jmri.jmrit.picker;


import jmri.*;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;

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

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.StringSelection;

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

    protected ArrayList <NamedBean>       _pickList;
    protected String  _name;

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final int POSITION_COL = 2;
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

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
        JTable table = new JTable(this);

        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(250,table.getRowHeight()*7));
        table.setDragEnabled(true);
        table.setTransferHandler(new DnDExportHandler());
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn sNameColumnT = columnModel.getColumn(SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(50);
        //sNameColumnT.setMaxWidth(200);

        TableColumn uNameColumnT = columnModel.getColumn(UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(100);
        //uNameColumnT.setMaxWidth(300);

        return table;
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
    public static PickListModel multiSensorPickModelInstance() {
        return new MultiSensorPickModel();
    }
    public static PickListModel signalHeadPickModelInstance() {
        return new SignalHeadPickModel();
    }
    public static PickListModel signalMastPickModelInstance() {
        return new SignalMastPickModel();
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
    public static PickListModel warrantPickModelInstance() {
        return new WarrantPickModel();
    }
    public static PickListModel conditionalPickModelInstance() {
        return new ConditionalPickModel();
    }

    static class DnDExportHandler extends TransferHandler{

        DnDExportHandler() {
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            JTable table = (JTable)c;
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (col<0 || row<0) {
                return null;
            }
            if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from ("
                                                +row+", "+col+") for \""
                                                +table.getModel().getValueAt(row, col)+"\"");
            return new StringSelection((String)table.getModel().getValueAt(row, col));
        }

        public void exportDone(JComponent c, Transferable t, int action) {
            if (log.isDebugEnabled()) log.debug("TransferHandler.exportDone ");
        }
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PickListModel.class.getName());
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
        public boolean isCellEditable(int r,int c) {
            if (c==POSITION_COL) {
                return true;
            }
            return super.isCellEditable(r, c);
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
            return true;
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
            return true;
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
                return l.getDisplayName();
            }
            return super.getValueAt(r, c);
        }
    }

