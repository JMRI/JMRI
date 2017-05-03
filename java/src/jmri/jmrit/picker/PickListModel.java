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
import javax.swing.table.TableRowSorter;
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
import jmri.util.SystemNameComparator;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to make pick lists for NamedBeans; Table model for pick lists
 * in IconAdder
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
 */
public abstract class PickListModel extends jmri.jmrit.beantable.BeanTableDataModel implements PropertyChangeListener {

    protected ArrayList<NamedBean> _pickList;
    protected String _name;
    private JTable _table;       // table using this model
    protected TableRowSorter<PickListModel> _sorter;

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final int POSITION_COL = 2;
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    static HashMap<String, Integer> _listMap = new HashMap<String, Integer>();

    static public int getNumInstances(String type) {
        Integer num = _listMap.get(type.toLowerCase());
        log.debug("getNumInstances of {} num={}", type, num);
        if (num != null) {
            return num;
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
     * No longer needed. Now done in BeanTableDataModel.
     *
     * @deprecated since Jan 1, 2014, marked as such May 1, 2017
     */
    @Deprecated
    public void init() {
        //log.debug("manager "+getManager());
        //getManager().addPropertyChangeListener(this);   // for adds and deletes
        //makePickList();
    }

    /**
     * If table has been sorted table row no longer is the same as array index.
     *
     * @param index row of table
     * @return bean at index or null if index is out of range
     */
    public NamedBean getBeanAt(int index) {
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
    @Override
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
        TreeSet<NamedBean> ts = new TreeSet<>(new NamedBeanComparator());

        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            ts.add(getBySystemName(iter.next()));
        }
        _pickList = new ArrayList<>(systemNameList.size());

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

    @Override
    public NamedBean getBySystemName(String name) {
        return getManager().getBeanBySystemName(name);
    }

    @Override
    protected NamedBean getByUserName(String name) {
        return getManager().getBeanByUserName(name);
    }

    @Override
    abstract public Manager getManager();

    /**
     * Return bean with name given in parameter. Create if needed and possible.
     *
     * @param name the name for the bean
     * @return the bean or null if not made
     */
    abstract public NamedBean addBean(String name);

    abstract public NamedBean addBean(String sysName, String userName);

    /**
     * Check if beans can be added by this model.
     *
     * @return true if model can create beans; false otherwise
     */
    abstract public boolean canAddBean();

    // these BeanTableDataModel abstract methods not needed
    @Override
    protected String getMasterClassName() {
        return "PickListModel";
    }

    @Override
    public void clickOn(NamedBean t) {
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int c) {
        if (c == SNAME_COLUMN) {
            return Bundle.getMessage("ColumnSystemName");
        } else if (c == UNAME_COLUMN) {
            return Bundle.getMessage("ColumnUserName");
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return false;
    }

    @Override
    public int getRowCount() {
        return _pickList.size();
    }

    @Override
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

    @Override
    public void setValueAt(Object type, int r, int c) {
    }

    // these BeanTableDataModel abstract methods not needed
    @Override
    public String getValue(String systemName) {
        return systemName;
    }

    public String getName() {
        return _name;
    }

    @Override
    protected String getBeanType() {
        return _name;
    }

    /**
     * Handle additions and deletions in the table and changes to beans within
     * the table.
     *
     * @param e the change
     */
    @Override
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
     * Make pick table, DND enabled.
     * @return the table
     */
    public JTable makePickTable() {
        _sorter = new TableRowSorter<>(this);
        _table = new JTable(this) {
            /**
             * Overridden to prevent empty cells from being selected
             */
            @Override
            public void changeSelection(int row, int col, boolean toggle, boolean extend) {
                if (this.getValueAt(row, col) != null) {
                    super.changeSelection(row, col, toggle, extend);
                }
            }
        };
        _sorter.setComparator(SNAME_COLUMN, new SystemNameComparator());
        _table.setRowSorter(_sorter);

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
        _table.setAutoCreateColumnsFromModel(false);

        return _table;
    }

    public void makeSorter(JTable table) {
        _sorter = new TableRowSorter<>(this);
        _sorter.setComparator(SNAME_COLUMN, new SystemNameComparator());
        table.setRowSorter(_sorter);
    }

    public JTable getTable() {
        return _table;
    }

    @Override
    public void dispose() {
        getManager().removePropertyChangeListener(this);
    }

    public static PickListModel turnoutPickModelInstance() {
        Integer num = _listMap.get("turnout");
        if (num != null) {
            _listMap.put("turnout", num + 1);
        } else {
            _listMap.put("turnout", 1);
        }
        return new TurnoutPickModel();
    }

    public static PickListModel sensorPickModelInstance() {
        Integer num = _listMap.get("sensor");
        if (num != null) {
            _listMap.put("sensor", num + 1);
        } else {
            _listMap.put("sensor", 1);
        }
        return new SensorPickModel();
    }

    public static PickListModel multiSensorPickModelInstance() {
        Integer num = _listMap.get("multisensor");
        if (num != null) {
            _listMap.put("multisensor", num + 1);
        } else {
            _listMap.put("multisensor", 1);
        }
        return new MultiSensorPickModel();
    }

    public static PickListModel signalHeadPickModelInstance() {
        Integer num = _listMap.get("signalhead");
        if (num != null) {
            _listMap.put("signalhead", num + 1);
        } else {
            _listMap.put("signalhead", 1);
        }
        return new SignalHeadPickModel();
    }

    public static PickListModel signalMastPickModelInstance() {
        Integer num = _listMap.get("signalmast");
        if (num != null) {
            _listMap.put("signalmast", num + 1);
        } else {
            _listMap.put("signalmast", 1);
        }
        return new SignalMastPickModel();
    }

    public static PickListModel memoryPickModelInstance() {
        Integer num = _listMap.get("memory");
        if (num != null) {
            _listMap.put("memory", num + 1);
        } else {
            _listMap.put("memory", 1);
        }
        return new MemoryPickModel();
    }

    public static PickListModel blockPickModelInstance() {
        Integer num = _listMap.get("block");
        if (num != null) {
            _listMap.put("block", num + 1);
        } else {
            _listMap.put("block", 1);
        }
        return new BlockPickModel();
    }

    public static PickListModel reporterPickModelInstance() {
        Integer num = _listMap.get("reporter");
        if (num != null) {
            _listMap.put("reporter", num + 1);
        } else {
            _listMap.put("reporter", 1);
        }
        return new ReporterPickModel();
    }

    public static PickListModel lightPickModelInstance() {
        Integer num = _listMap.get("light");
        if (num != null) {
            _listMap.put("light", num + 1);
        } else {
            _listMap.put("light", 1);
        }
        return new LightPickModel();
    }

    public static PickListModel oBlockPickModelInstance() {
        Integer num = _listMap.get("oBlock");
        if (num != null) {
            _listMap.put("oBlock", num + 1);
        } else {
            _listMap.put("oBlock", 1);
        }
        return new OBlockPickModel();
    }

    public static PickListModel warrantPickModelInstance() {
        Integer num = _listMap.get("warrant");
        if (num != null) {
            _listMap.put("warrant", num + 1);
        } else {
            _listMap.put("warrant", 1);
        }
        return new WarrantPickModel();
    }

    public static PickListModel conditionalPickModelInstance() {
        Integer num = _listMap.get("conditional");
        if (num != null) {
            _listMap.put("conditional", num + 1);
        } else {
            _listMap.put("conditional", 1);
        }
        return new ConditionalPickModel();
    }

    public static PickListModel entryExitPickModelInstance() {
        Integer num = _listMap.get("entryExit");
        if (num != null) {
            _listMap.put("entryExit", num + 1);
        } else {
            _listMap.put("entryExit", 1);
        }
        return new EntryExitPickModel();
    }

    private final static Logger log = LoggerFactory.getLogger(PickListModel.class.getName());

    static class TurnoutPickModel extends PickListModel {

        TurnoutManager manager;

        TurnoutPickModel() {
            _name = rb.getString("TitleTurnoutTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.turnoutManagerInstance();
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideTurnout(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.newTurnout(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class SensorPickModel extends PickListModel {

        SensorManager manager;

        SensorPickModel() {
            _name = rb.getString("TitleSensorTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.sensorManagerInstance();
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideSensor(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.newSensor(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class MultiSensorPickModel extends SensorPickModel {

        private final HashMap<Integer, String> _position = new HashMap<>();

        MultiSensorPickModel() {
            super();
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == POSITION_COL) {
                return _position.get(r);
            }
            return super.getValueAt(r, c);
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == POSITION_COL) {
                _position.put(r, (String) type);
            }
        }
    }

    static class SignalHeadPickModel extends PickListModel {

        SignalHeadManager manager;

        SignalHeadPickModel() {
            _name = rb.getString("TitleSignalTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.getDefault(jmri.SignalHeadManager.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) {
            return manager.getSignalHead(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            SignalHead sh = manager.getSignalHead(userName);
            if (sh == null) {
                sh = manager.getSignalHead(sysName);
            }
            return sh;
        }

        @Override
        public boolean canAddBean() {
            return false;
        }
    }

    static class SignalMastPickModel extends PickListModel {

        SignalMastManager manager;

        SignalMastPickModel() {
            _name = rb.getString("TitleSignalMastTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.getDefault(jmri.SignalMastManager.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideSignalMast(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) throws IllegalArgumentException {
            SignalMast sm = manager.getSignalMast(userName);
            if (sm == null) {
                sm = manager.provideSignalMast(sysName);
            }
            return sm;
        }

        @Override
        public boolean canAddBean() {
            return false;
        }
    }

    static class MemoryPickModel extends PickListModel {

        MemoryManager manager;

        MemoryPickModel() {
            _name = rb.getString("TitleMemoryTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.memoryManagerInstance();
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideMemory(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.newMemory(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class BlockPickModel extends PickListModel {

        BlockManager manager;

        BlockPickModel() {
            _name = rb.getString("TitleBlockTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.getDefault(jmri.BlockManager.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideBlock(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewBlock(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class ReporterPickModel extends PickListModel {

        ReporterManager manager;

        ReporterPickModel() {
            _name = rb.getString("TitleReporterTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.getDefault(jmri.ReporterManager.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideReporter(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.newReporter(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class LightPickModel extends PickListModel {

        LightManager manager;

        LightPickModel() {
            _name = rb.getString("TitleLightTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.lightManagerInstance();
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideLight(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.newLight(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class OBlockPickModel extends PickListModel {

        OBlockManager manager;

        OBlockPickModel() {
            _name = rb.getString("TitleBlockTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideOBlock(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewOBlock(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class WarrantPickModel extends PickListModel {

        WarrantManager manager;

        WarrantPickModel() {
            _name = rb.getString("TitleWarrantTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.getDefault(WarrantManager.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) throws IllegalArgumentException {
            return manager.provideWarrant(name);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewWarrant(sysName, userName, false, 0);
        }

        @Override
        public boolean canAddBean() {
            return false;
        }
    }

    static class ConditionalPickModel extends PickListModel {

        ConditionalManager manager;

        ConditionalPickModel() {
            _name = rb.getString("TitleConditionalTable");
        }

        @Override
        public Manager getManager() {
            manager = InstanceManager.getDefault(jmri.ConditionalManager.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) {
            return manager.createNewConditional(name, null);
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return manager.createNewConditional(sysName, userName);
        }

        @Override
        public boolean canAddBean() {
            return false;
        }

        @Override
        public JTable makePickTable() {
            JTable table = super.makePickTable();
            TableColumn column = new TableColumn(PickListModel.POSITION_COL);
            column.setResizable(true);
            column.setMinWidth(100);
            column.setHeaderValue("Logix");
            table.addColumn(column);
            return table;
        }

        @Override
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

        EntryExitPairs manager;

        EntryExitPickModel() {
            _name = rb.getString("TitleEntryExitTable");
        }

        @Override
        public Manager getManager() {
            manager = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);
            return manager;
        }

        @Override
        public NamedBean addBean(String name) {
            return null;
        }

        @Override
        public NamedBean addBean(String sysName, String userName) {
            return null;
        }

        @Override
        public boolean canAddBean() {
            return false;
        }

        @Override
        public String getColumnName(int c) {
            if (c == SNAME_COLUMN) {
                return "Unique Id";
            } else if (c == UNAME_COLUMN) {
                return Bundle.getMessage("ColumnUserName");
            }
            return "";
        }
    }
}
