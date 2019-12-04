package jmri.jmrit.logix;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
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
 * <br>
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
 * @author Pete Cressman Copyright (C) 2009, 2010
 */

class WarrantTableModel extends jmri.jmrit.beantable.BeanTableDataModel {
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
    private final ArrayList<Warrant> _warNX; // temporary warrants appended to table
    static Color myGreen = new Color(0, 100, 0);
    static Color myGold = new Color(200, 100, 0);

    public WarrantTableModel(WarrantTableFrame frame) {
        super();
        _frame = frame;
        _manager = InstanceManager
                .getDefault(jmri.jmrit.logix.WarrantManager.class);
        // _manager.addPropertyChangeListener(this); // for adds and deletes
        _warList = new ArrayList<>();
        _warNX = new ArrayList<>();
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
        ArrayList<Warrant> tempList = new ArrayList<>();
        // copy over warrants still listed
        for (Warrant w : _manager.getNamedBeanSet()) {
            if (!_warList.contains(w)) { // new warrant
                w.addPropertyChangeListener(this);
            } else {
                _warList.remove(w);
            }
            cleanBlockOrderList(w); // removes bad BlockOrders
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

    private void cleanBlockOrderList(Warrant warrant) {
        ArrayList<BlockOrder> valid = new ArrayList<>();
        Iterator<BlockOrder> iter = warrant.getBlockOrders().iterator();
        while (iter.hasNext()) {
            BlockOrder bo = iter.next();
            if (WarrantRoute.pathIsValid(bo.getBlock(), bo.getPathName()) == null) {
                valid.add(bo);
            }
        }
        warrant.setBlockOrders(valid);
    }

    protected void haltAllTrains() {
        ArrayList<Warrant> abortList = new ArrayList<>();
        Iterator<Warrant> iter = _warList.iterator();
        while (iter.hasNext()) {
            Warrant w = iter.next();
            if (w.getState() >= 0) {
                abortList.add(w);
            }
        }
/*        iter = _warNX.iterator();
        while (iter.hasNext()) {
            Warrant w = iter.next();
            if (w.getState() >= 0) {
                abortList.add(w);
            }
        }*/
        iter = abortList.iterator();
        while (iter.hasNext()) {
            iter.next().controlRunTrain(Warrant.STOP);
        }
        fireTableDataChanged();
    }

    protected void addNXWarrant(Warrant w) {
        _warList.add(w);
        _warNX.add(w);
        w.addPropertyChangeListener(this);
        fireTableDataChanged();
    }

    /**
     * Removes any warrant, not just NXWarrant
     * @param w Warrant
     * @param deregister deregister warrant
     */
    public void removeWarrant(Warrant w, boolean deregister) {
        log.debug("removeWarrant {}", w.getDisplayName());
        _warList.remove(w);
        _warNX.remove(w);
        if (deregister) {
            _manager.deregister(w);
        }
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
        return null;
    }
     
    protected String checkAddressInUse(Warrant warrant) {
        String address = warrant.getSpeedUtil().getAddress();

        if (address ==null) {
            return Bundle.getMessage("NoLoco");
        }
        for (Warrant w :_warList) {
            if (!warrant.equals(w) && w._runMode != Warrant.MODE_NONE) {
                if (address.equals(w.getSpeedUtil().getAddress())) {
                    return Bundle.getMessage("AddressInUse", address, w.getDisplayName(), w.getTrainName());
                }
            }
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return _warList.size();
    }
    
    protected int getRow(Warrant w) {
        int row = -1;
        Iterator<Warrant> iter = _warList.iterator();
        while (iter.hasNext()) {
            row++;
            Warrant war = iter.next();
            if (war.equals(w)) {
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
        default:
            // fall out
            break;
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
        default:
            // fall out
            break;
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
        default:
            // fall out
            break;
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
            return new JButton("Delete").getPreferredSize().width;
        default:
            // fall out
            break;
        }
        return new JTextField(10).getPreferredSize().width;
    }

    @Override
    public Object getValueAt(int row, int col) {
//        if (log.isTraceEnabled()) {
//            log.debug("getValueAt: row= "+row+", column= "+col);
//        }
        Warrant w = getWarrantAt(row);
        // some error checking
        if (w == null) {
            log.warn("getValueAt row= {}, Warrant is null!", row);
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
            BlockOrder bo0 = w.getfirstOrder();
            BlockOrder bo1 = w.getLastOrder();
            return Bundle.getMessage("WarrantRoute", 
                        (bo0==null?"?":bo0.getBlock().getDisplayName()),
                        (bo1==null?"?":bo1.getBlock().getDisplayName()));
        case TRAIN_NAME_COLUMN:
            return w.getTrainName();
        case ADDRESS_COLUMN:
            return w.getSpeedUtil().getAddress();
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
            if (w.hasRouteSet()) {
                if (w.isTotalAllocated()) {
                    return new NamedIcon(
                            "resources/icons/smallschematics/tracksegments/circuit-green.gif",
                            "off");
                } else if (w.isAllocated()) {
                    return new NamedIcon(
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "occupied");
                }
            } else if (w.isAllocated()) {
                return new NamedIcon(
                        "resources/icons/smallschematics/tracksegments/circuit-occupied.gif",
                        "occupied");
            }
            return new NamedIcon(
                    "resources/icons/smallschematics/tracksegments/circuit-empty.gif",
                    "occupied");
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
//            log.debug("getValueAt: warrant= {}, getRunningMessage= \"{}\"", w.getDisplayName(), msg);
            return msg;
        case EDIT_COLUMN:
            return Bundle.getMessage("ButtonEdit");
        case DELETE_COLUMN:
            return Bundle.getMessage("ButtonDelete");
        default:
            // fall out
            break;
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
//        if (log.isTraceEnabled())
//            log.debug("setValueAt: row= " + row + ", column= " + col
//                    + ", value= " + (value==null ? value : (value.toString()==null ? value.getClass().getName() :value.toString())));
        Warrant w = getWarrantAt(row);
        if (w == null) {
            log.warn("setValueAt row= {}, Warrant is null!", row);
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
            if (w.getRunMode() == Warrant.MODE_NONE) {
                String addr = (String) value;
                if (!w.getSpeedUtil().setDccAddress(addr)) {
                    msg = Bundle.getMessage("BadDccAddress", addr);                
                }
            } else {
                msg = w.getRunModeMessage();
                msg = Bundle.getMessage("CannotChangeAddress", w.getDisplayName(), msg);
            }
            break;
        case ALLOCATE_COLUMN:
            if (w.getRunMode() == Warrant.MODE_NONE) {
                msg = w.allocateRoute(true, null);
                if (msg == null) {
                    _frame.setStatusText(
                            Bundle.getMessage("completeAllocate",
                                    w.getDisplayName()), myGreen, false);
                } else {
//                    w.deAllocate();
                    _frame.setStatusText(Bundle.getMessage("UnableToAllocate",
                            w.getDisplayName()) + msg, myGold, false);
                    msg = null;
                }
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
            if (w.getRunMode() == Warrant.MODE_NONE) {
                msg = w.setRoute(true, null);
                if (msg == null) {
                    _frame.setStatusText(
                            Bundle.getMessage("pathsSet",
                                    w.getDisplayName()), myGreen, false);
                } else {
//                    w.deAllocate();
                    _frame.setStatusText(Bundle.getMessage("UnableToAllocate",
                            w.getDisplayName()) + msg, myGold, false);
                    msg = null;
                }
            }
            break;
        case AUTO_RUN_COLUMN:
            msg = checkAddressInUse(w);
            if (msg == null) {
            	msg = _frame.runTrain(w, Warrant.MODE_RUN);
            }
            break;
        case MANUAL_RUN_COLUMN:
            msg = checkAddressInUse(w);
            if (msg == null) {
            	msg = _frame.runTrain(w, Warrant.MODE_MANUAL);
            }
            break;
        case CONTROL_COLUMN:
            // Message is set when propertyChangeEvent (below) is received from
            // a warrant change. fireTableRows then causes getValueAt() which
            // calls getRunningMessage()
            int mode = w.getRunMode();
            if (log.isTraceEnabled()) {
                log.debug("setValueAt({}) for warrant {}", value, w.getDisplayName());
            }
            if (mode == Warrant.MODE_LEARN) {
                msg = Bundle.getMessage("Learning", w.getCurrentBlockName());
            } else if (value!=null) {
                String setting = (String) value;
                if (mode == Warrant.MODE_RUN || mode == Warrant.MODE_MANUAL) {
                    int s = -1;
                    if (setting.equals(WarrantTableFrame.halt)) {
                        s = Warrant.HALT;
                    } else if (setting.equals(WarrantTableFrame.resume)) {
                        s = Warrant.RESUME;
                    } else if (setting.equals(WarrantTableFrame.retry)) {
                        s = Warrant.RETRY;
                    } else if (setting.equals(WarrantTableFrame.stop)) {
                        s = Warrant.ESTOP;
                    } else if (setting.equals(WarrantTableFrame.abort)) {
                        s = Warrant.ABORT;
                    } else if (setting.equals(WarrantTableFrame.ramp)) {
                        s = Warrant.RAMP_HALT;
                    } else /*if (setting.equals("Debug"))*/ {
                        s = Warrant.DEBUG;
                    }
                    w.controlRunTrain(s);
                }
            }
            break;
        case EDIT_COLUMN:
            openWarrantFrame(w);                
            break;
        case DELETE_COLUMN:
            if (w.getRunMode() == Warrant.MODE_NONE) {
                fireTableRowsDeleted(row, row);
                removeWarrant(w, true); // removes any warrant
            } else {
                w.controlRunTrain(Warrant.ABORT);
                if (_warNX.contains(w)) { // don't remove regular warrants
                    fireTableRowsDeleted(row, row);
                    removeWarrant(w, false);
                }
            }
            break;
        default:
           log.error("Invalid Column " + col + " requested.");
           throw new java.lang.IllegalArgumentException("Invalid Column " + col + " requested.");
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(_frame, msg,
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
//            _frame.setStatusText(msg, Color.red, true);
        }
    }

    private void openWarrantFrame(Warrant warrant) {
        WarrantFrame frame = null;
        for (int i = 0; i < _warList.size(); i++) {
            if (warrant.equals(_warList.get(i))) {
                frame = new WarrantFrame(warrant);
                break;
            }
        }
        if (frame != null) {
            WarrantFrame f = WarrantTableAction.getWarrantFrame();
            if (f != null) {
                WarrantTableAction.closeWarrantFrame(f);
            }
            frame.setVisible(true);
            frame.toFront();
            WarrantTableAction.setWarrantFrame(frame);
        }
    }

    private String _lastProperty;
    private long _propertyTime;
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
//        log.debug("propertyChange "+property);
        long time = _propertyTime;
        _propertyTime = System.currentTimeMillis();
        if ((_propertyTime-time)<20 && property.equals(_lastProperty) && !property.equals("length")) {
            return;
        }
        _lastProperty = property;
        
        if (property.equals("length")) {
            // a NamedBean added or deleted
            init();
            fireTableDataChanged();
        } else if (e.getSource() instanceof Warrant) {
            // a value changed. Find it, to avoid complete redraw
            Warrant bean = (Warrant) e.getSource();
            log.debug("source is warrant "+bean.getDisplayName());
            for (int i = 0; i < _warList.size(); i++) {
                if (bean.equals(_warList.get(i))) {

                    if (_warNX.contains(bean)
                            && ((property.equals("runMode") && ((Integer)e.getNewValue()).intValue() == Warrant.MODE_NONE) 
                                    || (property.equals("controlChange") && ((Integer)e.getNewValue()).intValue() == Warrant.ABORT))) {
                        fireTableRowsDeleted(i, i);
                        removeWarrant(bean, false);
                    } else {
                        fireTableRowsUpdated(i, i);
                    }
                    break;
                }
            }
            int row = getRow(bean);
            if (row < 0) {	// warrant deleted
            	return;
            }
            if (property.equals("blockChange")) {
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
           /* } else if (property.equals("blockRelease")) {
                OBlock block = (OBlock) e.getNewValue();
                long et = (System.currentTimeMillis() - block._entryTime) / 1000;
                _frame.setStatusText(Bundle.getMessage("TrackerBlockLeave",
                        bean.getTrainName(), block.getDisplayName(), et / 60,
                        et % 60), myGreen, true);*/
            } else if (property.equals("SpeedRestriction")) {
                String name = (String) e.getOldValue();
                String speed = (String) e.getNewValue();
                _frame.setStatusText(Bundle.getMessage("SpeedRestriction",
                        bean.getTrainName(), speed, name),
                        Color.red, true);
            } else if (property.equals("SpeedChange")) {
            	fireTableCellUpdated(row, CONTROL_COLUMN);
            } else if (property.equals("WaitForSync")) {
            	fireTableCellUpdated(row, CONTROL_COLUMN);
            } else if (property.equals("runMode")) {
                int oldMode = ((Integer) e.getOldValue()).intValue();
                int newMode = ((Integer) e.getNewValue()).intValue();
                if (newMode == Warrant.MODE_ABORT) {
                    if (oldMode != Warrant.MODE_NONE) {
                        _frame.setStatusText(
                                Bundle.getMessage("warrantAbort",
                                               bean.getTrainName(),
                                               bean.getDisplayName()), myGreen,
                                               true);                        
                    } else {
                        _frame.setStatusText(
                                Bundle.getMessage("warrantAnnull",
                                               bean.getTrainName(),
                                               bean.getDisplayName()), myGreen,
                                               true);
                    }
                } else if (oldMode == Warrant.MODE_NONE && newMode != Warrant.MODE_NONE) {
                    // OK to run warrant, but look for problems ahead that may restrict extent of warrant

                    // From here on messages are status information, not abort info
                    String msg1 = bean.checkStartBlock();  // notify first block occupied by this train
                    if (msg1 == null) {
                        msg1 = "";
                    } else if (msg1.equals("BlockDark")) {
                        msg1 = Bundle.getMessage("startDark");
                    } else if (msg1.equals("warnStart")) {
                        msg1 = Bundle.getMessage("startUnoccupied");
                    }
                    String msg2 = bean.checkRoute();   // notify about occupation ahead
                    if (msg2 == null) {
                        msg2 = "";
                    }
                    _frame.setStatusText(Bundle.getMessage("warrantStart",
                            bean.getTrainName(), bean.getDisplayName(),
                            bean.getCurrentBlockName(),
                            Bundle.getMessage("startAppendage", msg1, msg2, Bundle.getMessage(Warrant.MODES[newMode]))),
                            myGreen, true); 
                } else if (oldMode != Warrant.MODE_NONE && newMode == Warrant.MODE_NONE) {
                    OBlock curBlock = bean.getCurrentBlockOrder().getBlock();
                    OBlock lastBlock = bean.getLastOrder().getBlock();
                    if (lastBlock.equals(curBlock)) {
                        _frame.setStatusText(Bundle.getMessage("warrantComplete",
                                bean.getTrainName(), bean.getDisplayName(), 
                                lastBlock.getDisplayName()), myGold,
                                true);
                        
                    } else {
                        _frame.setStatusText(Bundle.getMessage("warrantEnd",
                                bean.getTrainName(), bean.getDisplayName(), 
                                lastBlock.getDisplayName()), myGold,
                                true);                        
                    }
                }
            } else if (property.equals("RampDone")) {
                boolean halt = ((Boolean) e.getOldValue()).booleanValue();
                String speed = (String) e.getNewValue();
                if (speed.equals(Warrant.Stop) || speed.equals(Warrant.EStop))  {
                    if (halt) {
                        _frame.setStatusText(Bundle.getMessage("RampHalt",
                                bean.getTrainName(), bean.getCurrentBlockName()), myGreen, true);
                    } else {
                        String s;
                        if (bean.isWaitingForSignal()) {
                            s = Bundle.getMessage("Signal");
                        } else if (bean.isWaitingForWarrant()) {
                            Warrant w = bean.getBlockingWarrant();
                            String name = (w != null ? w.getDisplayName() : "???");
                            s = Bundle.getMessage("WarrantWait", name);
                        } else if (bean.isWaitingForClear()) {
                            s = Bundle.getMessage("Occupancy");
                        } else {
                            s = Bundle.getMessage("Halt");
                        }
                        _frame.setStatusText(Bundle.getMessage("RampWaitForClear", 
                                bean.getTrainName(), bean.getCurrentBlockName(), s), myGreen, true);
                    }
                } else {
                    _frame.setStatusText(Bundle.getMessage("RampSpeed",
                            bean.getTrainName(), speed, bean.getCurrentBlockName()), myGreen, true);
                }
            } else if (property.equals("ReadyToRun")) {
                _frame.setStatusText(Bundle.getMessage("TrainReady",
                        bean.getTrainName(), bean.getCurrentBlockName()), myGreen, true);
            } else if (property.equals("controlChange")) {
                String blkName = bean.getCurrentBlockName();
                String stateStr;
                Color color;
                if (e.getOldValue()==null) {
                    stateStr = Bundle.getMessage("engineerGone", blkName); 
                    color = Color.red;
                } else {
                    int runState = ((Integer) e.getOldValue()).intValue();
                    stateStr = Bundle.getMessage(Warrant.RUN_STATE[runState], blkName);
                    color = myGold;
                }
                int newCntrl = ((Integer) e.getNewValue()).intValue();
                _frame.setStatusText(Bundle.getMessage("controlChange",
                        bean.getTrainName(), stateStr,
                        Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl])),
                        color, true);
                fireTableCellUpdated(row, CONTROL_COLUMN);
            } else if (property.equals("controlFailed")) {
                String blkName = bean.getCurrentBlockName();
                String stateStr;
                if (e.getOldValue()==null) {
                    stateStr = Bundle.getMessage("engineerGone", blkName); 
                } else {
                    int runState = ((Integer) e.getOldValue()).intValue();
                    stateStr = Bundle.getMessage(Warrant.RUN_STATE[runState], blkName);
                }
                int newCntrl = ((Integer) e.getNewValue()).intValue();
                _frame.setStatusText(Bundle.getMessage("controlFailed",
                        bean.getTrainName(), stateStr,
                        Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl])),
                        Color.red, true);
                fireTableCellUpdated(row, CONTROL_COLUMN);
            } else if (property.equals("SensorSetCommand")) {
                String action = (String) e.getOldValue();
                String sensorName = (String) e.getNewValue();
                _frame.setStatusText(Bundle.getMessage("setSensor",
                            bean.getTrainName(), sensorName, action), myGreen, true);
            } else if (property.equals("SensorWaitCommand")) {
                String action = (String) e.getOldValue();
                String sensorName = (String) e.getNewValue();
                if (action != null) {
                    _frame.setStatusText(Bundle.getMessage("waitSensor",
                            bean.getTrainName(), sensorName, action), myGreen, true);
                } else {
                    _frame.setStatusText(Bundle.getMessage("waitSensorChange",
                            bean.getTrainName(), sensorName), myGreen, true);
                }
                fireTableCellUpdated(row, CONTROL_COLUMN);                    
            } else if (property.equals("throttleFail")) {
                _frame.setStatusText(Bundle.getMessage("ThrottleFail",
                        bean.getTrainName(), e.getNewValue()), Color.red, true);
            }
            if (log.isDebugEnabled())
                log.debug("propertyChange of \"{}\" done for warrant \"{}\"",
                        property, bean.getDisplayName());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantTableModel.class
            .getName());
}