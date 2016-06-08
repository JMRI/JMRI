package jmri.jmrit.logix;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for the Warrant List
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author Pete Cressman Copyright (C) 2009, 2010
 */

class WarrantTableModel extends jmri.jmrit.beantable.BeanTableDataModel // AbstractTableModel
                                                                        // implements
                                                                        // PropertyChangeListener
{
    public static final int WARRANT_COLUMN = 0;
    public static final int ROUTE_COLUMN = 1;
    public static final int TRAIN_NAME_COLUMN = 2;
    public static final int ADDRESS_COLUMN = 3;
    public static final int ALLOCATE_COLUMN = 4;
    public static final int DEALLOC_COLUMN = 5;
    public static final int SET_COLUMN = 6;
    public static final int AUTO_RUN_COLUMN = 7;
    public static final int MANUAL_RUN_COLUMN = 8;
    public static final int CONTROL_COLUMN = 9;
    public static final int EDIT_COLUMN = 10;
    public static final int DELETE_COLUMN = 11;
    public static final int NUMCOLS = 12;

    WarrantManager _manager;
    WarrantTableFrame _frame;
    private ArrayList<Warrant> _warList;
    private ArrayList<Warrant> _warNX; // temporary warrants appended to table
    static Color myGreen = new Color(0, 100, 0);
    static Color myGold = new Color(200, 100, 0);

    public WarrantTableModel(WarrantTableFrame frame) {
        super();
        _frame = frame;
        _manager = InstanceManager
                .getDefault(jmri.jmrit.logix.WarrantManager.class);
        // _manager.addPropertyChangeListener(this); // for adds and deletes
        _warList = new ArrayList<Warrant>();
        _warNX = new ArrayList<Warrant>();
    }

    public void addHeaderListener(JTable table) {
        addMouseListenerToHeader(table);
    }

    @Override
    public Manager getManager() {
        _manager = InstanceManager.getDefault(WarrantManager.class);
        return _manager;
    }

    @Override
    public NamedBean getBySystemName(String name) {
        return _manager.getBySystemName(name);
    }

    @Override
    public String getValue(String name) {
        return _manager.getBySystemName(name).getDisplayName();
    }

    @Override
    public NamedBean getByUserName(String name) {
        return _manager.getByUserName(name);
    }

    @Override
    protected String getBeanType() {
        return "Warrant";
    }

    @Override
    public void clickOn(NamedBean t) {
    }

    @Override
    protected String getMasterClassName() {
        return WarrantTableModel.class.getName();
    }

    /**
     * Preserve current listeners so that there is no gap to miss a
     * propertyChange
     */
    public synchronized void init() {
        ArrayList<Warrant> tempList = new ArrayList<Warrant>();
        List<String> systemNameList = _manager.getSystemNameList();
        Iterator<String> iter = systemNameList.iterator();
        // copy over warrants still listed
        while (iter.hasNext()) {
            Warrant w = _manager.getBySystemName(iter.next());
            if (!_warList.contains(w)) { // new warrant
                w.addPropertyChangeListener(this);
            } else {
                _warList.remove(w);
            }
            tempList.add(w); // add old or any new warrants
        }
        // remove listeners from any deleted warrants
        for (int i = 0; i < _warList.size(); i++) {
            Warrant w = _warList.get(i);
            if (!_warNX.contains(w)) { // don't touch current running NXWarrant
                w.removePropertyChangeListener(this);
            }
        }
        // add in current temporary NX warrants
        for (int i = 0; i < _warNX.size(); i++) {
            tempList.add(_warNX.get(i));
        }
        _warList = tempList;
    }

    protected void haltAllTrains() {
        Iterator<Warrant> iter = _warList.iterator();
        while (iter.hasNext()) {
            iter.next().controlRunTrain(Warrant.HALT);
        }
        iter = _warNX.iterator();
        while (iter.hasNext()) {
            iter.next().controlRunTrain(Warrant.HALT);
        }
        fireTableDataChanged();
    }

    public void addNXWarrant(Warrant w) {
        _warList.add(w);
        _warNX.add(w);
        w.addPropertyChangeListener(this);
        fireTableDataChanged();
    }

    /**
     * Removes any warrant, not just NXWarrant
     *
     */
    public void removeNXWarrant(Warrant w) {
        w.removePropertyChangeListener(this);
        _warList.remove(w);
        _warNX.remove(w);
        _manager.deregister(w);
        w.dispose();
    }

    public Warrant getWarrantAt(int index) {
        if (index >= _warList.size()) {
            return null;
        }
        return _warList.get(index);
    }
    
    protected Warrant getWarrant(String name) {
        if (name==null || name.length()==0) {
            return null;
        }
        for (Warrant w: _warList) {
            if (name.equals(w.getUserName()) || name.equals(w.getSystemName())) {
                return w;
            }
        }
        for (Warrant w: _warNX) {
            if (name.equals(w.getUserName()) || name.equals(w.getSystemName())) {
                return w;
            }
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return _warList.size();
    }
    
    private int getRow(Warrant w) {
        int row = -1;
        Iterator<Warrant> iter = _warList.iterator();
        while (iter.hasNext()) {
            row++;
            if (iter.next().equals(w)) {
                return row;
            }
        }
        return -1;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
        case WARRANT_COLUMN:
            return Bundle.getMessage("Warrant");
        case ROUTE_COLUMN:
            return Bundle.getMessage("Route");
        case TRAIN_NAME_COLUMN:
            return Bundle.getMessage("TrainName");
        case ADDRESS_COLUMN:
            return Bundle.getMessage("DccAddress");
        case ALLOCATE_COLUMN:
            return Bundle.getMessage("Allocate");
        case DEALLOC_COLUMN:
            return Bundle.getMessage("Deallocate");
        case SET_COLUMN:
            return Bundle.getMessage("SetRoute");
        case AUTO_RUN_COLUMN:
            return Bundle.getMessage("ARun");
        case MANUAL_RUN_COLUMN:
            return Bundle.getMessage("MRun");
        case CONTROL_COLUMN:
            return Bundle.getMessage("Control");
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case WARRANT_COLUMN:
            return false;
        case TRAIN_NAME_COLUMN:
        case ADDRESS_COLUMN:
        case ROUTE_COLUMN:
        case ALLOCATE_COLUMN:
        case DEALLOC_COLUMN:
        case SET_COLUMN:
        case AUTO_RUN_COLUMN:
        case MANUAL_RUN_COLUMN:
        case CONTROL_COLUMN:
        case EDIT_COLUMN:
        case DELETE_COLUMN:
            return true;
        }
        return false;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
        case WARRANT_COLUMN:
            return String.class;
        case ROUTE_COLUMN:
            return String.class; // JComboBox.class;
        case TRAIN_NAME_COLUMN:
            return String.class;
        case ADDRESS_COLUMN:
            return String.class;
        case ALLOCATE_COLUMN:
            return JButton.class;
        case DEALLOC_COLUMN:
            return JButton.class;
        case SET_COLUMN:
            return JButton.class;
        case AUTO_RUN_COLUMN:
            return JButton.class;
        case MANUAL_RUN_COLUMN:
            return JButton.class;
        case CONTROL_COLUMN:
            return String.class; // JComboBox.class;
        case EDIT_COLUMN:
            return JButton.class;
        case DELETE_COLUMN:
            return JButton.class;
        }
        return String.class;
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
        case WARRANT_COLUMN:
        case TRAIN_NAME_COLUMN:
            return new JTextField(13).getPreferredSize().width;
        case ROUTE_COLUMN:
            return new JTextField(25).getPreferredSize().width;
        case ADDRESS_COLUMN:
            return new JTextField(7).getPreferredSize().width;
        case ALLOCATE_COLUMN:
        case DEALLOC_COLUMN:
        case SET_COLUMN:
        case AUTO_RUN_COLUMN:
        case MANUAL_RUN_COLUMN:
            return new JButton("Xxxx").getPreferredSize().width;
        case CONTROL_COLUMN:
            return new JTextField(45).getPreferredSize().width;
        case EDIT_COLUMN:
        case DELETE_COLUMN:
            return new JButton("DELETE").getPreferredSize().width;
        }
        return new JTextField(10).getPreferredSize().width;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // if (log.isDebugEnabled())
        // log.debug("getValueAt: row= "+row+", column= "+col);
        Warrant w = getWarrantAt(row);
        // some error checking
        if (w == null) {
            log.warn("getValueAt row= " + row + " Warrant is null!");
            return "";
        }
        JRadioButton allocButton = new JRadioButton();
        JRadioButton deallocButton = new JRadioButton();
        ButtonGroup group = new ButtonGroup();
        group.add(allocButton);
        group.add(deallocButton);
        switch (col) {
        case WARRANT_COLUMN:
            return w.getDisplayName();
        case ROUTE_COLUMN:
            BlockOrder bo = w.getfirstOrder();
            if (bo != null) {
                return Bundle.getMessage("Origin", bo.getBlock()
                        .getDisplayName());
            }
            break;
        case TRAIN_NAME_COLUMN:
            return w.getTrainName();
        case ADDRESS_COLUMN:
            if (w.getDccAddress() != null) {
                return w.getDccAddress().toString();
            }
            break;
        case ALLOCATE_COLUMN:
            if (w.isTotalAllocated()) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-green.gif",
                        "occupied");
            } else if (w.isAllocated()) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-occupied.gif",
                        "occupied");
            } else {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-empty.gif",
                        "off");
            }
        case DEALLOC_COLUMN:
            if (w.isAllocated()) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-empty.gif",
                        "off");
            }
            return new NamedIcon(
                    "resources/icons/smallschematics/tracksegments/circuit-occupied.gif",
                    "occupied");
        case SET_COLUMN:
            if (w.hasRouteSet() && w.isTotalAllocated()) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-green.gif",
                        "off");
            } else if (w.hasRouteSet() && w.isAllocated()) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-occupied.gif",
                        "occupied");
            } else {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-empty.gif",
                        "occupied");
            }
        case AUTO_RUN_COLUMN:
            if (w.getRunMode() == Warrant.MODE_RUN) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-error.gif",
                        "red");
            }
            return new NamedIcon(
                    "resources/icons/smallschematics/tracksegments/circuit-empty.gif",
                    "off");
        case MANUAL_RUN_COLUMN:
            if (w.getRunMode() == Warrant.MODE_MANUAL) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-error.gif",
                        "red");
            }
            return new NamedIcon(
                    "resources/icons/smallschematics/tracksegments/circuit-empty.gif",
                    "off");
        case CONTROL_COLUMN:
            String msg = w.getRunningMessage();
            return msg;
        case EDIT_COLUMN:
            return Bundle.getMessage("ButtonEdit");
        case DELETE_COLUMN:
            return Bundle.getMessage("ButtonDelete");
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (log.isDebugEnabled())
            log.debug("setValueAt: row= " + row + ", column= " + col
                    + ", value= " + value.getClass().getName());
        Warrant w = getWarrantAt(row);
        if (w == null) {
            log.warn("setValueAt row= " + row + " Warrant is null!");
            return;
        }
        String msg = null;
        switch (col) {
        case WARRANT_COLUMN:
        case ROUTE_COLUMN:
            return;
        case TRAIN_NAME_COLUMN:
            w.setTrainName((String) value);
            break;
        case ADDRESS_COLUMN:
            String addr = (String) value;
            if (!w.setDccAddress(addr)) {
                msg = Bundle.getMessage("BadDccAddress", addr);                
            }
            break;
        case ALLOCATE_COLUMN:
            msg = w.allocateRoute(null);
            if (msg == null) {
                _frame.setStatusText(
                        Bundle.getMessage("completeAllocate",
                                w.getDisplayName()), myGreen, false);
            } else {
                _frame.setStatusText(msg, myGold, false);
                msg = null;
            }
            break;
        case DEALLOC_COLUMN:
            if (w.getRunMode() == Warrant.MODE_NONE) {
                w.deAllocate();
                _frame.setStatusText("", myGreen, false);
            } else {
                _frame.setStatusText(w.getRunModeMessage(), myGold, false);
            }
            break;
        case SET_COLUMN:
            msg = w.setRoute(0, null);
            if (msg == null) {
                _frame.setStatusText(
                        Bundle.getMessage("pathsSet", w.getDisplayName()),
                        myGreen, false);
            } else {
                w.deAllocate();
                _frame.setStatusText(msg, myGold, false);
                msg = null;
            }
            break;
        case AUTO_RUN_COLUMN:
            msg = _frame.runTrain(w);
            break;
        case MANUAL_RUN_COLUMN:
            if (w.getRunMode() == Warrant.MODE_NONE) {
                if (w.getBlockOrders().size() == 0) {
                    msg = Bundle.getMessage("EmptyRoute");
                    break;
                }
                msg = w.setRoute(0, null);
                if (msg == null) {
                    msg = w.setRunMode(Warrant.MODE_MANUAL, null, null, null,
                            false);
                }
                if (msg != null) {
                    w.deAllocate();
                } else {
                    msg = w.checkStartBlock(Warrant.MODE_RUN); // notify first block occupied by
                                                // this train
                    if (msg != null) {
                        _frame.setStatusText(msg, WarrantTableModel.myGold,
                                false);
                    }
                }
                if (log.isDebugEnabled())
                    log.debug("w.runManualTrain= " + msg);
            } else {
                msg = w.getRunModeMessage();
            }
            break;
        case CONTROL_COLUMN:
            // Message is set when propertyChangeEvent (below) is received from
            // a warrant
            // change. fireTableRows then causes getValueAt() which calls
            // getRunningMessage()
            int mode = w.getRunMode();
            if (mode == Warrant.MODE_LEARN) {
                Bundle.getMessage("Learning", w.getCurrentBlockOrder()
                        .getBlock().getDisplayName());
            } else {
                String setting = (String) value;
                if (mode == Warrant.MODE_RUN || mode == Warrant.MODE_MANUAL) {
                    int s = -1;
                    if (setting.equals(WarrantTableFrame.halt)) {
                        s = Warrant.HALT;
                    } else if (setting.equals(WarrantTableFrame.resume)) {
                        s = Warrant.RESUME;
                    } else if (setting.equals(WarrantTableFrame.retry)) {
                        s = Warrant.RETRY;
                    } else if (setting.equals(WarrantTableFrame.abort)) {
                        s = Warrant.ABORT;
                    }
                    w.controlRunTrain(s);
                } else if (setting.equals(WarrantTableFrame.abort)) {
                    w.deAllocate();
                } else if (mode == Warrant.MODE_NONE) {
                    msg = Bundle.getMessage("NotRunning", w.getDisplayName());
                } else {
                    getValueAt(row, col);
                }
            }
            break;
        case EDIT_COLUMN:
            openWarrantFrame(w);
            break;
        case DELETE_COLUMN:
            if (w.getRunMode() == Warrant.MODE_NONE) {
                removeNXWarrant(w); // removes any warrant
            } else {
                w.controlRunTrain(Warrant.ABORT);
                if (_warNX.contains(w)) { // don't remove regular warrants
                    removeNXWarrant(w);
                }

            }
            break;
        default:
           log.error("Invalid Column " + col + " requested.");
           throw new java.lang.IllegalArgumentException("Invalid Column " + col + " requested.");
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            _frame.setStatusText(msg, Color.red, true);
        }
        fireTableRowsUpdated(row, row);
    }

    private void openWarrantFrame(Warrant warrant) {
        if (WarrantTableAction._openFrame != null) {
            WarrantTableAction._openFrame.dispose();
        }
        WarrantTableAction._openFrame = null;
        for (int i = 0; i < _warList.size(); i++) {
            if (warrant.equals(_warList.get(i))) {
                WarrantTableAction._openFrame = new WarrantFrame(warrant);
                break;
            }
        }
        if (WarrantTableAction._openFrame == null) {
            for (int i = 0; i < _warNX.size(); i++) {
                if (warrant.equals(_warList.get(i))) {
                    WarrantTableAction._openFrame = new WarrantFrame(warrant);
                    break;
                }
            }
            if (WarrantTableAction._openFrame != null) {
                WarrantTableAction._openFrame.setVisible(true);
                WarrantTableAction._openFrame.toFront();
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property.equals("length")) {
            // a NamedBean added or deleted
            init();
            fireTableDataChanged();
        } else if (e.getSource() instanceof Warrant) {
            // a value changed. Find it, to avoid complete redraw
            Warrant bean = (Warrant) e.getSource();
            for (int i = 0; i < _warList.size(); i++) {
                if (bean.equals(_warList.get(i))) {

                    if (_warNX.contains(bean)
                            && ((property.equals("runMode") && ((Integer)e.getNewValue()).intValue() == Warrant.MODE_NONE) 
                                    || (property.equals("controlChange") && ((Integer)e.getNewValue()).intValue() == Warrant.ABORT))) {
                        fireTableRowsDeleted(i, i);
                        removeNXWarrant(bean);
                    } else {
                        fireTableRowsUpdated(i, i);
                    }
                    break;
                }
            }
            if (e.getPropertyName().equals("blockChange")) {
                OBlock oldBlock = (OBlock) e.getOldValue();
                OBlock newBlock = (OBlock) e.getNewValue();
                if (newBlock == null) {
                    _frame.setStatusText(
                            Bundle.getMessage("ChangedRoute",
                                    bean.getDisplayName(),
                                    oldBlock.getDisplayName(),
                                    bean.getTrainName()), Color.red, true);
                } else {
                    _frame.setStatusText(
                            Bundle.getMessage("TrackerBlockEnter",
                                    bean.getTrainName(),
                                    newBlock.getDisplayName()), myGreen, true);
                }
            } else if (e.getPropertyName().equals("blockRelease")) {
                OBlock block = (OBlock) e.getNewValue();
                long et = (System.currentTimeMillis() - block._entryTime) / 1000;
                _frame.setStatusText(Bundle.getMessage("TrackerBlockLeave",
                        bean.getTrainName(), block.getDisplayName(), et / 60,
                        et % 60), myGreen, true);
            } else if (e.getPropertyName().equals("SpeedRestriction")) {
                _frame.setStatusText(Bundle.getMessage("speedChange",
                        bean.getTrainName(), bean.getCurrentBlockOrder()
                                .getBlock().getDisplayName(), e.getNewValue()),
                        myGold, true);
            } else if (e.getPropertyName().equals("SpeedChange")) {
                int row = getRow(bean);
                if (row>=0) {
                    fireTableRowsUpdated(row, row);                    
//                    _frame.setStatusText(bean.getRunningMessage(), myGreen, true);
                }
            } else if (e.getPropertyName().equals("runMode")) {
                int oldMode = ((Integer) e.getOldValue()).intValue();
                int newMode = ((Integer) e.getNewValue()).intValue();
                if (oldMode == Warrant.MODE_NONE) {
                    if (newMode != Warrant.MODE_NONE) {
                        _frame.setStatusText(Bundle.getMessage("warrantStart",
                                bean.getTrainName(), bean.getDisplayName(),
                                bean.getCurrentBlockOrder().getBlock()
                                        .getDisplayName(),
                                Bundle.getMessage(Warrant.MODES[newMode])),
                                myGreen, true);
                    }
                } else if (newMode == Warrant.MODE_NONE) {
                    OBlock block = bean.getCurrentBlockOrder().getBlock();
                    int state = block.getState();
                    if ((state & OBlock.OCCUPIED) != 0
                         || (state & OBlock.DARK) != 0) {
                       _frame.setStatusText(
                            Bundle.getMessage("warrantEnd",
                                            bean.getTrainName(),
                                            bean.getDisplayName(),
                                            block.getDisplayName()), myGreen,
                                            true);
                    } else {
                        _frame.setStatusText(
                             Bundle.getMessage("warrantAbort",
                                            bean.getTrainName(),
                                            bean.getDisplayName()), myGreen,
                                            true);
                    }
                } else {
                    _frame.setStatusText(Bundle.getMessage("modeChange",
                            bean.getTrainName(), bean.getDisplayName(),
                            Bundle.getMessage(Warrant.MODES[oldMode]),
                            Bundle.getMessage(Warrant.MODES[newMode])), myGold,
                            true);
                }
            } else if (e.getPropertyName().equals("controlChange")) {
                int runState = ((Integer) e.getOldValue()).intValue();
                int newCntrl = ((Integer) e.getNewValue()).intValue();
                String stateStr = null;
                if (runState < 0) {
                    stateStr = Bundle.getMessage(Warrant.MODES[-runState]);
                } else {
                    stateStr = Bundle.getMessage(Warrant.RUN_STATE[runState],
                            bean.getCurrentBlockOrder().getBlock()
                                    .getDisplayName());
                }
                _frame.setStatusText(Bundle.getMessage("controlChange",
                        bean.getTrainName(), stateStr,
                        Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl])),
                        myGold, true);
            } else if (e.getPropertyName().equals("throttleFail")) {
                _frame.setStatusText(Bundle.getMessage("ThrottleFail",
                        bean.getTrainName(), e.getNewValue()), Color.red, true);
            }
        }
        if (log.isDebugEnabled())
            log.debug("propertyChange of \"" + e.getPropertyName() + "\" for "
                    + e.getSource().getClass().getName());
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantTableModel.class
            .getName());
}
