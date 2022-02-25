package jmri.jmrit.logix;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.ThreadingUtil;

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

class WarrantTableModel extends jmri.jmrit.beantable.BeanTableDataModel<Warrant> {
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
        _manager.addPropertyChangeListener(this); // for adds and deletes
        _warList = new ArrayList<>();
        _warNX = new ArrayList<>();
    }

    public void addHeaderListener(JTable table) {
        addMouseListenerToHeader(table);
    }

    @Override
    public Manager<Warrant> getManager() {
        _manager = InstanceManager.getDefault(WarrantManager.class);
        return _manager;
    }

    @Override
    public Warrant getBySystemName(@Nonnull String name) {
        return _manager.getBySystemName(name);
    }

    @Override
    public String getValue(String name) {
        Warrant w = _manager.getBySystemName(name);
        if (w == null) {
            return null;
        }
        return w.getDisplayName();
    }

    @Override
    public Warrant getByUserName(@Nonnull String name) {
        return _manager.getByUserName(name);
    }

    @Override
    protected String getBeanType() {
        return "Warrant";
    }

    @Override
    public void clickOn(Warrant t) {
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
        iter = abortList.iterator();
        while (iter.hasNext()) {
            iter.next().controlRunTrain(Warrant.STOP);
        }
        fireTableUpdate();
    }

    protected void addNXWarrant(Warrant w) {
        _warList.add(w);
        _warNX.add(w);
        w.addPropertyChangeListener(this);
        fireTableUpdate();
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
        jmri.DccLocoAddress address = warrant.getSpeedUtil().getDccAddress();

        if (address ==null) {
            return Bundle.getMessage("NoLoco");
        }
        for (Warrant w :_warList) {
            if (w._runMode != Warrant.MODE_NONE) {
                if (address.equals(w.getSpeedUtil().getDccAddress())) {
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
        case ADDRESS_COLUMN:
            return new JTextField(13).getPreferredSize().width;
        case ROUTE_COLUMN:
            return new JTextField(25).getPreferredSize().width;
        case ALLOCATE_COLUMN:
        case DEALLOC_COLUMN:
        case SET_COLUMN:
        case AUTO_RUN_COLUMN:
        case MANUAL_RUN_COLUMN:
            return new JButton("Xxxx").getPreferredSize().width;
        case CONTROL_COLUMN:
            return new JTextField(60).getPreferredSize().width;
        case EDIT_COLUMN:
            return new JButton(Bundle.getMessage("ButtonEdit")).getPreferredSize().width;
        case DELETE_COLUMN:
            return new JButton(Bundle.getMessage("ButtonDelete")).getPreferredSize().width;
        default:
            // fall out
            break;
        }
        return new JTextField(10).getPreferredSize().width;
    }

    static String GREEN_LED = "resources/icons/smallschematics/tracksegments/circuit-green.gif";
    static String YELLOW_LED = "resources/icons/smallschematics/tracksegments/circuit-occupied.gif";
    static String OFF_LED = "resources/icons/smallschematics/tracksegments/circuit-empty.gif";
    static String RED_LED = "resources/icons/smallschematics/tracksegments/circuit-error.gif";

    @Override
    public Object getValueAt(int row, int col) {
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
            return w.getSpeedUtil().getRosterId();
        case ALLOCATE_COLUMN:
            NamedIcon icon;
            if (w.isTotalAllocated()) {
                icon = new NamedIcon(GREEN_LED, "green");
            } else if (w.isAllocated()) {
                icon = new NamedIcon(YELLOW_LED, "yellow");
            } else {
                icon = new NamedIcon(OFF_LED, "off");
            }
            return icon;
        case DEALLOC_COLUMN:
            if (w.isAllocated()) {
                icon = new NamedIcon(OFF_LED, "off");
            } else {
                icon = new NamedIcon(YELLOW_LED, "occupied");
            }
            return icon;
        case SET_COLUMN:
            if (w.hasRouteSet()) {
                if (w.isTotalAllocated()) {
                    icon = new NamedIcon(GREEN_LED, "green");
                } else if (w.isAllocated()) {
                    icon = new NamedIcon(YELLOW_LED, "yellow");
                } else {
                    icon = new NamedIcon(RED_LED, "error");
                }
            } else {
                icon = new NamedIcon(OFF_LED, "off");
            }
            return icon;
        case AUTO_RUN_COLUMN:
            if (w.getRunMode() == Warrant.MODE_RUN) {
                icon = new NamedIcon(RED_LED, "red");
            } else {
                icon = new NamedIcon(OFF_LED, "off");
            }
            return icon;
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
            if (w.isNXWarrant()) {
                return Bundle.getMessage("ButtonSave");
            } else {
                return Bundle.getMessage("ButtonEdit");
            }
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
                if (!w.getSpeedUtil().setAddress(addr)) {
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
                    setFrameStatusText(
                            Bundle.getMessage("completeAllocate",
                                    w.getDisplayName()), myGreen, false);
                } else {
                    setFrameStatusText(Bundle.getMessage("UnableToAllocate",
                            w.getDisplayName()) + msg, myGold, false);
                    msg = null;
                }
                this.fireTableRowsUpdated(row, row);
            }
            break;
        case DEALLOC_COLUMN:
            if (w.getRunMode() == Warrant.MODE_NONE) {
                w.deAllocate();
                setFrameStatusText("", myGreen, false);
                this.fireTableRowsUpdated(row, row);
            } else {
                setFrameStatusText(w.getRunModeMessage(), myGold, false);
            }
            break;
        case SET_COLUMN:
            if (w.getRunMode() == Warrant.MODE_NONE) {
                msg = w.setRoute(false, null);
                if (msg == null) {
                    setFrameStatusText(
                            Bundle.getMessage("pathsSet",
                                    w.getDisplayName()), myGreen, false);
                } else {
                    setFrameStatusText(Bundle.getMessage("UnableToAllocate",
                            w.getDisplayName()) + msg, myGold, false);
                    msg = null;
                }
                this.fireTableRowsUpdated(row, row);
            }
            break;
        case AUTO_RUN_COLUMN:
            msg = frameRunTrain(w, Warrant.MODE_RUN);
            this.fireTableRowsUpdated(row, row);
            break;
        case MANUAL_RUN_COLUMN:
            msg = frameRunTrain(w, Warrant.MODE_MANUAL);
            this.fireTableRowsUpdated(row, row);
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
                    } else if (setting.equals(WarrantTableFrame.ramp)) {
                        s = Warrant.RAMP_HALT;
                    } else if (setting.equals(WarrantTableFrame.resume)) {
                        s = Warrant.RESUME;
                    } else if (setting.equals(WarrantTableFrame.speedup)) {
                        s = Warrant.SPEED_UP;
                    } else if (setting.equals(WarrantTableFrame.retry)) {
                        s = Warrant.RETRY;
                    } else if (setting.equals(WarrantTableFrame.stop)) {
                        s = Warrant.ESTOP;
                    } else if (setting.equals(WarrantTableFrame.abort)) {
                        s = Warrant.ABORT;
                    } else if (setting.isEmpty()) {
                        s = Warrant.DEBUG;
                    }
                    if (s != -1) {
                        w.controlRunTrain(s);
                    }
                }
            }
            break;
        case EDIT_COLUMN:
            if (w.isNXWarrant()) {
                saveNXWarrant(w);                
            } else {
                openWarrantFrame(w);                
            }
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
            log.error("Invalid Column {} requested.", col);
           throw new java.lang.IllegalArgumentException("Invalid Column " + col + " requested.");
        }
        if (msg != null) {
            showMessageDialog(msg);
        }
    }

    private void showMessageDialog(String msg) {
        ThreadingUtil.runOnGUIEventually(() -> {
            JOptionPane.showMessageDialog(_frame, msg,
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
        });
    }

    private void openWarrantFrame(Warrant warrant) {
        for (Warrant w : _warList) {
            if (warrant.equals(w)) {
                WarrantTableAction.getDefault().editWarrantFrame(warrant);
                break;
            }
        }
    }

    private void saveNXWarrant(Warrant warrant) {
        for (Warrant w : _warNX) {
            if (warrant.equals(w)) {
                Warrant war = cloneWarrant(warrant);
                WarrantTableAction.getDefault().makeWarrantFrame(war, null);
                break;
            }
        }
    }

    private Warrant cloneWarrant(Warrant warrant) {
        Warrant w = new Warrant(InstanceManager.getDefault(WarrantManager.class).getAutoSystemName(), null);
        w.setTrainName(warrant.getTrainName());
        w.setRunBlind(warrant.getRunBlind());
        w.setShareRoute(warrant.getShareRoute());
        w.setAddTracker(warrant.getAddTracker());
        w.setNoRamp(warrant.getNoRamp());

        for (BlockOrder bo : warrant.getBlockOrders()) {
            w.addBlockOrder(new BlockOrder(bo));
        }
        w.setViaOrder(warrant.getViaOrder());
        w.setAvoidOrder(warrant.getAvoidOrder());
        for (ThrottleSetting ts : warrant.getThrottleCommands()) {
            w.addThrottleCommand(ts);
        }
        SpeedUtil copySU = w.getSpeedUtil();
        SpeedUtil su = warrant.getSpeedUtil();
        copySU.setDccAddress(su.getDccAddress());
        copySU.setRosterId(su.getRosterId());
        return w;
    }

    private String frameRunTrain(Warrant w, int mode) {
        return jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
            String m = _frame.runTrain(w, mode);
            return m;
        });
    }

    private void setFrameStatusText(String m, Color c, boolean save) {
        ThreadingUtil.runOnGUIEventually(()-> _frame.setStatusText(m, c, true));
    }

    private void fireCellUpdate(int row, int col) {
        if (row < getRowCount()) {
            ThreadingUtil.runOnGUIEventually(()-> fireTableCellUpdated(row, col));
        }
    }

    private void fireTableUpdate() {
        ThreadingUtil.runOnGUIEventually(()-> fireTableDataChanged());
    }

    private String _lastProperty;
    private long _propertyTime;
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
        long time = _propertyTime;
        _propertyTime = System.currentTimeMillis();
        if ((_propertyTime-time)<20 && property.equals(_lastProperty) && !property.equals("length")) {
            return;
        }
        _lastProperty = property;
        
        if (property.equals("length")) {
            // a NamedBean added or deleted
            init();
            fireTableUpdate();
        } else if (e.getSource() instanceof Warrant) {
            // a value changed. Find it, to avoid complete redraw
            Warrant bean = (Warrant) e.getSource();
            log.debug("source is warrant {}", bean.getDisplayName());
            for (int i = 0; i < _warList.size(); i++) {
                if (bean.equals(_warList.get(i))) {
                    if (_warNX.contains(bean)) {
                        if ((property.equals("runMode") && ((Integer)e.getNewValue()).intValue() == Warrant.MODE_NONE) ||
                                (property.equals("controlChange") && ((Integer)e.getNewValue()).intValue() == Warrant.ABORT)) {
                            removeWarrant(bean, false);
                            fireTableRowsDeleted(i, i);
                        }
                    } else {
                        fireTableRowsUpdated(i, i);
                    }
                    break;
                }
            }
            int row = getRow(bean);
            if (row < 0) { // warrant deleted
                return;
            }

            if (property.equals("blockChange")) {
                OBlock oldBlock = (OBlock) e.getOldValue();
                OBlock newBlock = (OBlock) e.getNewValue();
                if (newBlock == null) {
                    setFrameStatusText(Bundle.getMessage("ChangedRoute",
                            bean.getTrainName(), oldBlock.getDisplayName(),
                            bean.getDisplayName()), Color.red, true);
                } else {
                    setFrameStatusText(Bundle.getMessage("TrackerBlockEnter",
                            bean.getTrainName(), 
                            newBlock.getDisplayName()), myGreen, true);
                }
            } else if (property.equals("SpeedChange")) {
                fireCellUpdate(row, CONTROL_COLUMN);
            } else if (property.equals("WaitForSync")) {
                fireCellUpdate(row, CONTROL_COLUMN);
            } else if (property.equals("cannotRun")) {
                fireCellUpdate(row, CONTROL_COLUMN);
                setFrameStatusText(Bundle.getMessage("trainWaiting", bean.getTrainName(),
                        e.getNewValue(), e.getOldValue()), Color.red, true);
            } else if (property.equals("SignalOverrun")) {
                String name = (String) e.getOldValue();
                String speed = (String) e.getNewValue();
                setFrameStatusText(Bundle.getMessage("SignalOverrun",
                        bean.getTrainName(), speed, name), Color.red, true);
            } else if (property.equals("OccupyOverrun")) {
                String blkName = (String) e.getOldValue();
                String occuppier = (String) e.getNewValue();
                setFrameStatusText(Bundle.getMessage("OccupyOverrun",
                        bean.getTrainName(), blkName, occuppier), Color.red, true);
            } else if (property.equals("WarrantOverrun")) {
                String blkName = (String) e.getOldValue();
                String warName = (String) e.getNewValue();
                setFrameStatusText(Bundle.getMessage("WarrantOverrun",
                        bean.getTrainName(), blkName, warName), Color.red, true);
            } else if (property.equals("runMode")) {
                int oldMode = ((Integer) e.getOldValue()).intValue();
                int newMode = ((Integer) e.getNewValue()).intValue();
                if (newMode == Warrant.MODE_ABORT) {
                    if (oldMode != Warrant.MODE_NONE) {
                        setFrameStatusText(Bundle.getMessage("warrantAbort",
                                bean.getTrainName(), bean.getDisplayName()),
                                myGreen, true);                        
                    } else {
                        setFrameStatusText(Bundle.getMessage("warrantAnnull",
                                bean.getTrainName(), bean.getDisplayName()),
                                myGreen, true);
                    }
                } else if (oldMode != Warrant.MODE_NONE && newMode == Warrant.MODE_NONE) {
                    OBlock curBlock = bean.getCurrentBlockOrder().getBlock();
                    OBlock lastBlock = bean.getLastOrder().getBlock();
                    if (lastBlock.equals(curBlock)) {
                        setFrameStatusText(Bundle.getMessage("warrantComplete",
                                bean.getTrainName(), bean.getDisplayName(), 
                                lastBlock.getDisplayName()), myGold, true);
                        
                    } else {
                        setFrameStatusText(Bundle.getMessage("warrantEnd",
                                bean.getTrainName(), bean.getDisplayName(), 
                                lastBlock.getDisplayName()), myGold, true);                        
                    }
                } else if (newMode == Warrant.MODE_RUN) {
                    setFrameStatusText(Bundle.getMessage("warrantStart",
                            bean.getTrainName(), bean.getDisplayName(),
                            bean.getCurrentBlockName()), myGreen, true);
                }
            } else if (property.equals("RampDone")) {
                boolean halt = ((Boolean) e.getOldValue()).booleanValue();
                String speed = (String) e.getNewValue();
                if (halt || speed.equals(Warrant.EStop))  {
                    setFrameStatusText(Bundle.getMessage("RampHalt",
                            bean.getTrainName(), bean.getCurrentBlockName()), myGreen, true);
                } else {
                    setFrameStatusText(Bundle.getMessage("RampSpeed", bean.getTrainName(), 
                            speed, bean.getCurrentBlockName()), myGreen, true);
               }
                fireCellUpdate(row, CONTROL_COLUMN);
            } else if (property.equals("RampBegin")) {
//                String ms = (String) e.getOldValue();
                String speedType = (String) e.getNewValue();
                setFrameStatusText(Bundle.getMessage("RampStart", bean.getTrainName(),
                        speedType, bean.getCurrentBlockName()), myGreen, true);
            } else if (property.equals("ReadyToRun")) {
                setFrameStatusText(Bundle.getMessage("TrainReady", bean.getTrainName(), bean.getCurrentBlockName()), myGreen, true);
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
                setFrameStatusText(Bundle.getMessage("controlChange",
                        bean.getTrainName(), stateStr,
                        Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl])),
                        color, true);
                fireCellUpdate(row, CONTROL_COLUMN);
            } else if (property.equals("controlFailed")) {
                String blkName = bean.getCurrentBlockName();
                String stateStr;
                if (e.getOldValue()==null) {
                    stateStr = Bundle.getMessage("engineerGone", blkName); 
                } else {
                    stateStr = ((String) e.getOldValue());
                }
                int newCntrl = ((Integer) e.getNewValue()).intValue();
                setFrameStatusText(Bundle.getMessage("controlFailed",
                        bean.getTrainName(), stateStr,
                        Bundle.getMessage(Warrant.CNTRL_CMDS[newCntrl])),
                        Color.red, true);
                fireCellUpdate(row, CONTROL_COLUMN);
            } else if (property.equals("SensorSetCommand")) {
                String action = (String) e.getOldValue();
                String sensorName = (String) e.getNewValue();
                setFrameStatusText(Bundle.getMessage("setSensor",
                            bean.getTrainName(), sensorName, action), myGreen, true);
            } else if (property.equals("SensorWaitCommand")) {
                String action = (String) e.getOldValue();
                String sensorName = (String) e.getNewValue();
                if (action != null) {
                    setFrameStatusText(Bundle.getMessage("waitSensor",
                            bean.getTrainName(), sensorName, action), myGreen, true);
                } else {
                    setFrameStatusText(Bundle.getMessage("waitSensorChange",
                            bean.getTrainName(), sensorName), myGreen, true);
                }
                fireCellUpdate(row, CONTROL_COLUMN);                    
            } else if (property.equals("throttleFail")) {
                setFrameStatusText(Bundle.getMessage("ThrottleFail",
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
