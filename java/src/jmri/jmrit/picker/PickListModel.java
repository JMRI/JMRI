package jmri.jmrit.picker;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.BlockManager;
import jmri.ConditionalManager;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.Manager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.TurnoutManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.signalling.EntryExitPairs;
import jmri.util.NamedBeanComparator;
import jmri.util.com.sun.TableSorter;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to make pick lists for NamedBeans.
 * <P>
 * Concrete pick list class for many beans are include at the end of this file.
 * This class also has instantiation methods serve as a factory for those
 * classes.
 * <P>
 * Note: Extensions of this class must call init() after instantiation.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Pete Cressman Copyright (C) 2009, 2010
 * @version
 */
/**
 * Table model for pick lists in IconAdder
 */
public abstract class PickListModel extends jmri.jmrit.beantable.BeanTableDataModel implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -4174669657476555432L;
    protected ArrayList<NamedBean> _pickList;
    protected String _name;
    private JTable _table;       // table using this model
    protected TableSorter _sorter;

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final int POSITION_COL = 2;
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    static HashMap<String, Integer> _listMap = new HashMap<String, Integer>();

    static public int getNumInstances(String type) {
        Integer num = _listMap.get(type.toLowerCase());
        if (log.isDebugEnabled()) {
            log.debug("getNumInstances of " + type + " num= " + num);
        }
        if (num != null) {
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
     * No longer needed. Now done in BeanTableDataModel
     */
    public void init() {
        //log.debug("manager "+getManager());
        //getManager().addPropertyChangeListener(this);   // for adds and deletes
        //makePickList();
    }

    /**
     * If table has been sorted table row no longer is the same as array index
     *
     * @param index = row of table
     */
    public NamedBean getBeanAt(int index) {
        index = _sorter.modelIndex(index);
        if (index >= _pickList.size()) {
            return null;
        }
        return _pickList.get(index);
    }

    public int getIndexOf(NamedBean bean) {
        for (int i = 0; i < _pickList.size(); i++) {
            if (_pickList.get(i).equals(bean)) {
                return i;
            }
        }
        return -1;
    }

    public List<NamedBean> getBeanList() {
        return _pickList;
    }

    /**
     * override BeanTableDataModel only lists SystemName
     */
    protected synchronized void updateNameList() {
        makePickList();
    }

    private void makePickList() {
        // Don't know who is added or deleted so remove all name change listeners
        if (_pickList != null) {
            for (int i = 0; i < _pickList.size(); i++) {
                _pickList.get(i).removePropertyChangeListener(this);
            }
        }
        List<String> systemNameList = getManager().getSystemNameList();
        TreeSet<NamedBean> ts = new TreeSet<NamedBean>(new NamedBeanComparator());

        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            ts.add(getBySystemName(iter.next()));
        }
        _pickList = new ArrayList<NamedBean>(systemNameList.size());

        Iterator<NamedBean> it = ts.iterator();
        while (it.hasNext()) {
            NamedBean elt = it.next();
            _pickList.add(elt);
        }
        // add name change listeners
        for (int i = 0; i < _pickList.size(); i++) {
            _pickList.get(i).addPropertyChangeListener(this);
        }
        if (log.isDebugEnabled()) {
            log.debug("_pickList has " + _pickList.size() + " beans");
        }
    }

    public NamedBean getBySystemName(String name) {
        return getManager().getBeanBySystemName(name);
    }

    protected NamedBean getByUserName(String name) {
        return getManager().getBeanByUserName(name);
    }

    abstract public Manager getManager();

    /**
     * Return bean with name given in parameter. Create if needed and possible
     */
    abstract public NamedBean addBean(String name);

    abstract public NamedBean addBean(String sysName, String userName);

    /**
     * Return true if model can create beans
     */
    abstract public boolean canAddBean();

    // these BeanTableDataModel abstract methods not needed
    protected String getMasterClassName() {
        return "PickListModel";
    }

    public void clickOn(NamedBean t) {
    }

    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    public int getColumnCount() {
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

    public boolean isCellEditable(int r, int c) {
        return false;
    }

    public int getRowCount() {
        return _pickList.size();
    }

    public Object getValueAt(int r, int c) {
        // some error checking
        if (r >= _pickList.size()) {
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

    public void setValueAt(Object type, int r, int c) {
    }

    // these BeanTableDataModel abstract methods not needed
    public String getValue(String systemName) {
        return systemName;
    }

    public String getName() {
        return _name;
    }

    protected String getBeanType() {
        return _name;
    }

    /**
     * override. only interested in additions and deletions
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a NamedBean added or deleted
            makePickList();
            fireTableDataChanged();
        }
        if (e.getSource() instanceof NamedBean) {
            NamedBean bean = (NamedBean) e.getSource();
            for (int i = 0; i < _pickList.size(); i++) {
                if (bean.equals(_pickList.get(i))) {
                    fireTableRowsUpdated(i, i);
                    break;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("propertyChange of \"" + e.getPropertyName()
                    + "\" for " + e.getSource().toString());
        }
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
            log.error("makePickTable: Unexpected error: " + e);
            _table = new JTable(this);
        }

        _table.setRowSelectionAllowed(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setPreferredScrollableViewportSize(new java.awt.Dimension(250, _table.getRowHeight() * 7));
        _table.setDragEnabled(true);
        _table.setTransferHandler(new jmri.util.DnDTableExportHandler());

        _table.getTableHeader().setReorderingAllowed(true);
        _table.setColumnModel(new XTableColumnModel());
        _table.createDefaultColumnsFromModel();
        TableColumnModel columnModel = _table.getColumnModel();

        TableColumn sNameColumnT = columnModel.getColumn(SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(50);
        //sNameColumnT.setMaxWidth(200);

        TableColumn uNameColumnT = columnModel.getColumn(UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(100);
        //uNameColumnT.setMaxWidth(300);

        addMouseListenerToHeader(_table);

        return _table;
    }

    public void makeSorter(JTable table) {
        try {   // following might fail due to a missing method on Mac Classic
            _sorter = new TableSorter(this);
            _sorter.setTableHeader(table.getTableHeader());
            _sorter.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            table.setModel(_sorter);
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            log.error("makeSorter: Unexpected error: " + e);
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
        if (num != null) {
            _listMap.put("turnout", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("turnout", Integer.valueOf(1));
        }
        return new TurnoutPickModel();
    }

    public static PickListModel sensorPickModelInstance() {
        Integer num = _listMap.get("sensor");
        if (num != null) {
            _listMap.put("sensor", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("sensor", Integer.valueOf(1));
        }
        return new SensorPickModel();
    }

    public static PickListModel multiSensorPickModelInstance() {
        Integer num = _listMap.get("multisensor");
        if (num != null) {
            _listMap.put("multisensor", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("multisensor", Integer.valueOf(1));
        }
        return new MultiSensorPickModel();
    }

    public static PickListModel signalHeadPickModelInstance() {
        Integer num = _listMap.get("sensor");
        if (num != null) {
            _listMap.put("signalhead", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("signalhead", Integer.valueOf(1));
        }
        return new SignalHeadPickModel();
    }

    public static PickListModel signalMastPickModelInstance() {
        Integer num = _listMap.get("signalmast");
        if (num != null) {
            _listMap.put("signalmast", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("signalmast", Integer.valueOf(1));
        }
        return new SignalMastPickModel();
    }

    public static PickListModel memoryPickModelInstance() {
        Integer num = _listMap.get("memory");
        if (num != null) {
            _listMap.put("memory", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("memory", Integer.valueOf(1));
        }
        return new MemoryPickModel();
    }

    public static PickListModel blockPickModelInstance() {
        Integer num = _listMap.get("block");
        if (num != null) {
            _listMap.put("block", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("block", Integer.valueOf(1));
        }
        return new BlockPickModel();
    }

    public static PickListModel reporterPickModelInstance() {
        Integer num = _listMap.get("reporter");
        if (num != null) {
            _listMap.put("reporter", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("reporter", Integer.valueOf(1));
        }
        return new ReporterPickModel();
    }

    public static PickListModel lightPickModelInstance() {
        Integer num = _listMap.get("light");
        if (num != null) {
            _listMap.put("light", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("light", Integer.valueOf(1));
        }
        return new LightPickModel();
    }

    public static PickListModel oBlockPickModelInstance() {
        Integer num = _listMap.get("oBlock");
        if (num != null) {
            _listMap.put("oBlock", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("oBlock", Integer.valueOf(1));
        }
        return new OBlockPickModel();
    }

    public static PickListModel warrantPickModelInstance() {
        Integer num = _listMap.get("warrant");
        if (num != null) {
            _listMap.put("warrant", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("warrant", Integer.valueOf(1));
        }
        return new WarrantPickModel();
    }

    public static PickListModel conditionalPickModelInstance() {
        Integer num = _listMap.get("conditional");
        if (num != null) {
            _listMap.put("conditional", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("conditional", Integer.valueOf(1));
        }
        return new ConditionalPickModel();
    }

    public static PickListModel entryExitPickModelInstance() {
        Integer num = _listMap.get("entryExit");
        if (num != null) {
            _listMap.put("entryExit", Integer.valueOf(num.intValue() + 1));
        } else {
            _listMap.put("entryExit", Integer.valueOf(1));
        }
        return new EntryExitPickModel();
    }

    static final Logger log = LoggerFactory.getLogger(PickListModel.class.getName());

    static class TurnoutPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = 7013117956249797371L;
        TurnoutManager manager;

        TurnoutPickModel() {
            _name = rb.getString("TitleTurnoutTable");
        }

        public Manager getManager() {
            manager = InstanceManager.turnoutManagerInstance();
            return manager;
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

    static class SensorPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = -5449473524170410469L;
        SensorManager manager;

        SensorPickModel() {
            _name = rb.getString("TitleSensorTable");
        }

        public Manager getManager() {
            manager = InstanceManager.sensorManagerInstance();
            return manager;
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

    static class MultiSensorPickModel extends SensorPickModel {

        /**
         *
         */
        private static final long serialVersionUID = 5378755836882039735L;
        private HashMap<Integer, String> _position = new HashMap<Integer, String>();

        MultiSensorPickModel() {
            super();
        }

        public Object getValueAt(int r, int c) {
            if (c == POSITION_COL) {
                return _position.get(Integer.valueOf(r));
            }
            return super.getValueAt(r, c);
        }

        public void setValueAt(Object type, int r, int c) {
            if (c == POSITION_COL) {
                _position.put(Integer.valueOf(r), (String) type);
            }
        }
    }

    static class SignalHeadPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = -2036689134503776495L;
        SignalHeadManager manager;

        SignalHeadPickModel() {
            _name = rb.getString("TitleSignalTable");
        }

        public Manager getManager() {
            manager = InstanceManager.signalHeadManagerInstance();
            return manager;
        }

        public NamedBean addBean(String name) {
            return manager.getSignalHead(name);
        }

        public NamedBean addBean(String sysName, String userName) {
            SignalHead sh = manager.getSignalHead(userName);
            if (sh == null) {
                sh = manager.getSignalHead(sysName);
            }
            return sh;
        }

        public boolean canAddBean() {
            return false;
        }
    }

    static class SignalMastPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = -2376422980165819407L;
        SignalMastManager manager;

        SignalMastPickModel() {
            _name = rb.getString("TitleSignalMastTable");
        }

        public Manager getManager() {
            manager = InstanceManager.signalMastManagerInstance();
            return manager;
        }

        public NamedBean addBean(String name) {
            return manager.provideSignalMast(name);
        }

        public NamedBean addBean(String sysName, String userName) {
            SignalMast sm = manager.getSignalMast(userName);
            if (sm == null) {
                sm = manager.provideSignalMast(sysName);
            }
            return sm;
        }

        public boolean canAddBean() {
            return false;
        }
    }

    static class MemoryPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = 554967330577788658L;
        MemoryManager manager;

        MemoryPickModel() {
            _name = rb.getString("TitleMemoryTable");
        }

        public Manager getManager() {
            manager = InstanceManager.memoryManagerInstance();
            return manager;
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

    static class BlockPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = 6772550115260370075L;
        BlockManager manager;

        BlockPickModel() {
            _name = rb.getString("TitleBlockTable");
        }

        public Manager getManager() {
            manager = InstanceManager.blockManagerInstance();
            return manager;
        }

        public NamedBean addBean(String name) {
            return manager.provideBlock(name);
        }

        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewBlock(sysName, userName);
        }

        public boolean canAddBean() {
            return true;
        }
    }

    static class ReporterPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = -8225533577316449385L;
        ReporterManager manager;

        ReporterPickModel() {
            _name = rb.getString("TitleReporterTable");
        }

        public Manager getManager() {
            manager = InstanceManager.reporterManagerInstance();
            return manager;
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

    static class LightPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = 2563996274392877385L;
        LightManager manager;

        LightPickModel() {
            _name = rb.getString("TitleLightTable");
        }

        public Manager getManager() {
            manager = InstanceManager.lightManagerInstance();
            return manager;
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

    static class OBlockPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = -8891253867640053650L;
        OBlockManager manager;

        OBlockPickModel() {
            _name = rb.getString("TitleBlockTable");
        }

        public Manager getManager() {
            manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
            return manager;
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

    static class WarrantPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = 233304766160346957L;
        WarrantManager manager;

        WarrantPickModel() {
            _name = rb.getString("TitleWarrantTable");
        }

        public Manager getManager() {
            manager = InstanceManager.getDefault(WarrantManager.class);
            return manager;
        }

        public NamedBean addBean(String name) {
            return manager.provideWarrant(name);
        }

        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewWarrant(sysName, userName, false, 0);
        }

        public boolean canAddBean() {
            return false;
        }
    }

    static class ConditionalPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = 1850772979922233034L;
        ConditionalManager manager;

        ConditionalPickModel() {
            _name = rb.getString("TitleConditionalTable");
        }

        public Manager getManager() {
            manager = InstanceManager.conditionalManagerInstance();
            return manager;
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

        public Object getValueAt(int r, int c) {
            if (c == POSITION_COL) {
                jmri.Logix l = manager.getParentLogix(_pickList.get(r).getSystemName());
                if (l != null) {
                    return l.getDisplayName();
                }
            }
            return super.getValueAt(r, c);
        }
    }

    static class EntryExitPickModel extends PickListModel {

        /**
         *
         */
        private static final long serialVersionUID = -1274360959113717578L;
        EntryExitPairs manager;

        EntryExitPickModel() {
            _name = rb.getString("TitleEntryExitTable");
        }

        public Manager getManager() {
            manager = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);
            return manager;
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
}
