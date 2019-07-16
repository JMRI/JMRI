package jmri.jmrit.picker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.*;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.entryexit.*;
import jmri.jmrit.logix.*;
import jmri.util.*;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to make pick lists for NamedBeans; Table model for pick lists
 * in IconAdder
 * <p>
 * Concrete pick list class for many beans are include at the end of this file.
 * This class also has instantiation methods serve as a factory for those
 * classes.
 * <p>
 * Note: Extensions of this class must call init() after instantiation.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @param <E> the supported type of NamedBean
 * @author Pete Cressman Copyright (C) 2009, 2010
 */
public abstract class PickListModel<E extends NamedBean> extends BeanTableDataModel<E> {

    protected ArrayList<E> _pickList;
    protected String _name;
    private JTable _table;       // table using this model
    protected TableRowSorter<PickListModel<E>> _sorter;

    public static final int SNAME_COLUMN = 0;
    public static final int UNAME_COLUMN = 1;
    public static final int POSITION_COL = 2;
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    static HashMap<String, Integer> _listMap = new HashMap<String, Integer>();

    static public int getNumInstances(@Nonnull String type) {
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
    }

    /**
     * If table has been sorted table row no longer is the same as array index.
     *
     * @param index row of table
     * @return bean at index or null if index is out of range
     */
    @CheckForNull
    public E getBeanAt(int index) {
        if (index >= _pickList.size()) {
            return null;
        }
        return _pickList.get(index);
    }

    public int getIndexOf(@Nonnull E bean) {
        for (int i = 0; i < _pickList.size(); i++) {
            if (_pickList.get(i).equals(bean)) {
                return i;
            }
        }
        return -1;
    }

    @Nonnull
    public List<E> getBeanList() {
        return _pickList;
    }

    /**
     * override BeanTableDataModel only lists SystemName
     */
    @Override
    protected synchronized void updateNameList() {
        makePickList();
    }

    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
    private void makePickList() {
        // Don't know who is added or deleted so remove all name change listeners
        if (_pickList != null) {
            for (int i = 0; i < _pickList.size(); i++) {
                _pickList.get(i).removePropertyChangeListener(this);
            }
        }
        List<String> systemNameList = getManager().getSystemNameList();
        TreeSet<E> ts = new TreeSet<>(new NamedBeanComparator<>());

        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            ts.add(getBySystemName(iter.next()));
        }
        _pickList = new ArrayList<>(systemNameList.size());

        Iterator<E> it = ts.iterator();
        while (it.hasNext()) {
            E elt = it.next();
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

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public E getBySystemName(@Nonnull String name) {
        return getManager().getBeanBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    protected E getByUserName(@Nonnull String name) {
        return getManager().getBeanByUserName(name);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    abstract public Manager<E> getManager();

    /**
     * Return bean with name given in parameter. Create if needed and possible.
     *
     * @param name the name for the bean
     * @return the bean or null if not made
     */
    @CheckForNull
    abstract public E addBean(@Nonnull String name);

    @CheckForNull
    abstract public E addBean(@Nonnull String sysName, String userName);

    /**
     * Check if beans can be added by this model.
     *
     * @return true if model can create beans; false otherwise
     */
    abstract public boolean canAddBean();

    // these BeanTableDataModel abstract methods not needed
    /** {@inheritDoc} */
    @Override
    protected String getMasterClassName() {
        return "PickListModel";
    }

    /** {@inheritDoc} */
    @Override
    public void clickOn(E t) {
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int c) {
        if (c == SNAME_COLUMN) {
            return Bundle.getMessage("ColumnSystemName");
        } else if (c == UNAME_COLUMN) {
            return Bundle.getMessage("ColumnUserName");
        }
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(int r, int c) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _pickList.size();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void setValueAt(Object type, int r, int c) {
    }

    // these BeanTableDataModel abstract methods not needed
    /** {@inheritDoc} */
    @Override
    public String getValue(String systemName) {
        return systemName;
    }

    public String getName() {
        return _name;
    }

    /** {@inheritDoc} */
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
                if (super.getValueAt(row, col) != null) {
                    super.changeSelection(row, col, toggle, extend);
                }
            }
        };
        _table.setRowSorter(_sorter);

        _table.setRowSelectionAllowed(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.setPreferredScrollableViewportSize(new java.awt.Dimension(250, _table.getRowHeight() * 7));
        _table.setDragEnabled(true);
        _table.setTransferHandler(new DnDTableExportHandler());

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

    public void makeSorter(@Nonnull JTable table) {
        _sorter = new TableRowSorter<>(this);
        table.setRowSorter(_sorter);
    }

    @Nonnull
    public JTable getTable() {
        return _table;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        getManager().removePropertyChangeListener(this);
    }

    @Nonnull
    public static PickListModel<Turnout> turnoutPickModelInstance() {
        Integer num = _listMap.get("turnout");
        if (num != null) {
            _listMap.put("turnout", num + 1);
        } else {
            _listMap.put("turnout", 1);
        }
        return new TurnoutPickModel();
    }

    @Nonnull
    public static PickListModel<Sensor> sensorPickModelInstance() {
        Integer num = _listMap.get("sensor");
        if (num != null) {
            _listMap.put("sensor", num + 1);
        } else {
            _listMap.put("sensor", 1);
        }
        return new SensorPickModel();
    }

    @Nonnull
    public static PickListModel<Sensor> multiSensorPickModelInstance() {
        Integer num = _listMap.get("multisensor");
        if (num != null) {
            _listMap.put("multisensor", num + 1);
        } else {
            _listMap.put("multisensor", 1);
        }
        return new MultiSensorPickModel();
    }

    @Nonnull
    public static PickListModel<SignalHead> signalHeadPickModelInstance() {
        Integer num = _listMap.get("signalhead");
        if (num != null) {
            _listMap.put("signalhead", num + 1);
        } else {
            _listMap.put("signalhead", 1);
        }
        return new SignalHeadPickModel();
    }

    @Nonnull
    public static PickListModel<SignalMast> signalMastPickModelInstance() {
        Integer num = _listMap.get("signalmast");
        if (num != null) {
            _listMap.put("signalmast", num + 1);
        } else {
            _listMap.put("signalmast", 1);
        }
        return new SignalMastPickModel();
    }

    @Nonnull
    public static PickListModel<Memory> memoryPickModelInstance() {
        Integer num = _listMap.get("memory");
        if (num != null) {
            _listMap.put("memory", num + 1);
        } else {
            _listMap.put("memory", 1);
        }
        return new MemoryPickModel();
    }

    @Nonnull
    public static PickListModel<Block> blockPickModelInstance() {
        Integer num = _listMap.get("block");
        if (num != null) {
            _listMap.put("block", num + 1);
        } else {
            _listMap.put("block", 1);
        }
        return new BlockPickModel();
    }

    @Nonnull
    public static PickListModel<Reporter> reporterPickModelInstance() {
        Integer num = _listMap.get("reporter");
        if (num != null) {
            _listMap.put("reporter", num + 1);
        } else {
            _listMap.put("reporter", 1);
        }
        return new ReporterPickModel();
    }

    @Nonnull
    public static PickListModel<Light> lightPickModelInstance() {
        Integer num = _listMap.get("light");
        if (num != null) {
            _listMap.put("light", num + 1);
        } else {
            _listMap.put("light", 1);
        }
        return new LightPickModel();
    }

    @Nonnull
    public static PickListModel<OBlock> oBlockPickModelInstance() {
        Integer num = _listMap.get("oBlock");
        if (num != null) {
            _listMap.put("oBlock", num + 1);
        } else {
            _listMap.put("oBlock", 1);
        }
        return new OBlockPickModel();
    }

    @Nonnull
    public static PickListModel<Warrant> warrantPickModelInstance() {
        Integer num = _listMap.get("warrant");
        if (num != null) {
            _listMap.put("warrant", num + 1);
        } else {
            _listMap.put("warrant", 1);
        }
        return new WarrantPickModel();
    }

    @Nonnull
    public static PickListModel<DestinationPoints> entryExitPickModelInstance() {
        Integer num = _listMap.get("entryExit");
        if (num != null) {
            _listMap.put("entryExit", num + 1);
        } else {
            _listMap.put("entryExit", 1);
        }
        return new EntryExitPickModel();
    }

    public static PickListModel<Logix> logixPickModelInstance() {
        Integer num = _listMap.get("logix");
        if (num != null) {
            _listMap.put("logix", num + 1);
        } else {
            _listMap.put("logix", 1);
        }
        return new LogixPickModel();
    }

    private final static Logger log = LoggerFactory.getLogger(PickListModel.class);

    static class TurnoutPickModel extends PickListModel<Turnout> {

        TurnoutManager manager = InstanceManager.turnoutManagerInstance();

        TurnoutPickModel() {
            _name = rb.getString("TitleTurnoutTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Turnout> getManager() {
            manager = InstanceManager.turnoutManagerInstance();
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Turnout addBean(String name) throws IllegalArgumentException {
            return manager.provideTurnout(name);
        }

        /** {@inheritDoc} */
        @Override
        public Turnout addBean(String sysName, String userName) {
            return manager.newTurnout(sysName, userName);
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class SensorPickModel extends PickListModel<Sensor> {

        SensorManager manager = InstanceManager.sensorManagerInstance();

        SensorPickModel() {
            _name = rb.getString("TitleSensorTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Sensor> getManager() {
            manager = InstanceManager.sensorManagerInstance();
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Sensor addBean(String name) throws IllegalArgumentException {
            return manager.provideSensor(name);
        }

        /** {@inheritDoc} */
        @Override
        public Sensor addBean(String sysName, String userName) {
            return manager.newSensor(sysName, userName);
        }

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
        @Override
        public Object getValueAt(int r, int c) {
            if (c == POSITION_COL) {
                return _position.get(r);
            }
            return super.getValueAt(r, c);
        }

        /** {@inheritDoc} */
        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == POSITION_COL) {
                _position.put(r, (String) type);
            }
        }
    }

    static class SignalHeadPickModel extends PickListModel<SignalHead> {

        SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);

        SignalHeadPickModel() {
            _name = rb.getString("TitleSignalTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<SignalHead> getManager() {
            manager = InstanceManager.getDefault(SignalHeadManager.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public SignalHead addBean(String name) {
            return manager.getSignalHead(name);
        }

        /** {@inheritDoc} */
        @Override
        public SignalHead addBean(String sysName, String userName) {
            SignalHead sh = manager.getSignalHead(userName);
            if (sh == null) {
                sh = manager.getSignalHead(sysName);
            }
            return sh;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return false;
        }
    }

    static class SignalMastPickModel extends PickListModel<SignalMast> {

        SignalMastManager manager = InstanceManager.getDefault(SignalMastManager.class);

        SignalMastPickModel() {
            _name = rb.getString("TitleSignalMastTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<SignalMast> getManager() {
            manager = InstanceManager.getDefault(SignalMastManager.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public SignalMast addBean(String name) throws IllegalArgumentException {
            return manager.provideSignalMast(name);
        }

        /** {@inheritDoc} */
        @Override
        public SignalMast addBean(String sysName, String userName) throws IllegalArgumentException {
            SignalMast sm = manager.getSignalMast(userName);
            if (sm == null) {
                sm = manager.provideSignalMast(sysName);
            }
            return sm;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return false;
        }
    }

    static class MemoryPickModel extends PickListModel<Memory> {

        MemoryManager manager = InstanceManager.memoryManagerInstance();

        MemoryPickModel() {
            _name = rb.getString("TitleMemoryTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Memory> getManager() {
            manager = InstanceManager.memoryManagerInstance();
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Memory addBean(String name) throws IllegalArgumentException {
            return manager.provideMemory(name);
        }

        /** {@inheritDoc} */
        @Override
        public Memory addBean(String sysName, String userName) {
            return manager.newMemory(sysName, userName);
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class BlockPickModel extends PickListModel<Block> {

        BlockManager manager = InstanceManager.getDefault(BlockManager.class);

        BlockPickModel() {
            _name = rb.getString("TitleBlockTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Block> getManager() {
            manager = InstanceManager.getDefault(BlockManager.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Block addBean(String name) throws IllegalArgumentException {
            return manager.provideBlock(name);
        }

        /** {@inheritDoc} */
        @Override
        public Block addBean(String sysName, String userName) {
            return manager.createNewBlock(sysName, userName);
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class ReporterPickModel extends PickListModel<Reporter> {

        ReporterManager manager = InstanceManager.getDefault(ReporterManager.class);

        ReporterPickModel() {
            _name = rb.getString("TitleReporterTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Reporter> getManager() {
            manager = InstanceManager.getDefault(ReporterManager.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Reporter addBean(String name) throws IllegalArgumentException {
            return manager.provideReporter(name);
        }

        /** {@inheritDoc} */
        @Override
        public Reporter addBean(String sysName, String userName) {
            return manager.newReporter(sysName, userName);
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class LightPickModel extends PickListModel<Light> {

        LightManager manager = InstanceManager.lightManagerInstance();

        LightPickModel() {
            _name = rb.getString("TitleLightTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Light> getManager() {
            manager = InstanceManager.lightManagerInstance();
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Light addBean(String name) throws IllegalArgumentException {
            return manager.provideLight(name);
        }

        /** {@inheritDoc} */
        @Override
        public Light addBean(String sysName, String userName) {
            return manager.newLight(sysName, userName);
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class OBlockPickModel extends PickListModel<OBlock> {

        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);

        OBlockPickModel() {
            _name = rb.getString("TitleBlockTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<OBlock> getManager() {
            manager = InstanceManager.getDefault(OBlockManager.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public OBlock addBean(String name) throws IllegalArgumentException {
            return manager.provideOBlock(name);
        }

        /** {@inheritDoc} */
        @Override
        public OBlock addBean(String sysName, String userName) {
            return manager.createNewOBlock(sysName, userName);
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return true;
        }
    }

    static class WarrantPickModel extends PickListModel<Warrant> {

        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);

        WarrantPickModel() {
            _name = rb.getString("TitleWarrantTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Warrant> getManager() {
            manager = InstanceManager.getDefault(WarrantManager.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Warrant addBean(String name) throws IllegalArgumentException {
            return manager.provideWarrant(name);
        }

        /** {@inheritDoc} */
        @Override
        public Warrant addBean(String sysName, String userName) {
            return manager.createNewWarrant(sysName, userName, false, 0);
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return false;
        }
    }

    static class EntryExitPickModel extends PickListModel<DestinationPoints> {

        EntryExitPairs manager = InstanceManager.getDefault(EntryExitPairs.class);

        EntryExitPickModel() {
            _name = rb.getString("TitleEntryExitTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<DestinationPoints> getManager() {
            manager = InstanceManager.getDefault(EntryExitPairs.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public DestinationPoints addBean(String name) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public DestinationPoints addBean(String sysName, String userName) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return false;
        }

        /** {@inheritDoc} */
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

    static class LogixPickModel extends PickListModel<Logix> {

        LogixManager manager;

        LogixPickModel() {
           _name = rb.getString("TitleLogixTable");
        }

        /** {@inheritDoc} */
        @Override
        public Manager<Logix> getManager() {
            manager = InstanceManager.getDefault(LogixManager.class);
            return manager;
        }

        /** {@inheritDoc} */
        @Override
        public Logix addBean(String name) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Logix addBean(String sysName, String userName) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canAddBean() {
            return false;
        }
    }
}
