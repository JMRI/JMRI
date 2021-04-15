package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.text.ParseException;

import java.util.*;
//import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.util.IntlUtilities;
import jmri.util.gui.GuiLafPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define the Signals within an OBlock.
 * <p>
 * Can be used with two interfaces:
 * <ul>
 *     <li>original "desktop" InternalFrames (parent class TableFrames, an extended JmriJFrame)
 *     <li>JMRI standard Tabbed tables (parent class JPanel)
 * </ul>
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
 * @author Pete Cressman (C) 2010
 * @author Egbert Broerse (C) 2020
 */
public class SignalTableModel extends AbstractTableModel {

    public static final int NAME_COLUMN = 0;
    public static final int FROM_BLOCK_COLUMN = 1;
    public static final int PORTAL_COLUMN = 2;
    public static final int TO_BLOCK_COLUMN = 3;
    public static final int LENGTHCOL = 4;
    public static final int UNITSCOL = 5;
    public static final int DELETE_COL = 6;
    static public final int EDIT_COL = 7; // only used on _tabbed UI
    public static final int NUMCOLS = 7;  // returns 7+1 for _tabbed
    int _lastIdx; // for debug

    PortalManager _portalMgr;
    TableFrames _parent;
    private SignalArray _signalList = new SignalArray();
    private final boolean _tabbed; // updated from prefs (restart required)
    private float _tempLen = 0.0f; // mm for length col of tempRow
    private String[] tempRow;
    boolean inEditMode = false;
    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    protected static class SignalRow {

        NamedBean _signal;
        OBlock _fromBlock;
        Portal _portal;
        OBlock _toBlock;
        float _length;  // offset from signal to speed change point, stored in mm
        boolean _isMetric;

        SignalRow(NamedBean signal, OBlock fromBlock, Portal portal, OBlock toBlock, float length, boolean isMetric) {
            _signal = signal;
            _fromBlock = fromBlock;
            _portal = portal;
            _toBlock = toBlock;
            _length = length;
            _isMetric = isMetric;
        }

        void setSignal(NamedBean signal) {
            _signal = signal;
        }
        NamedBean getSignal() {
            return _signal;
        }
        void setFromBlock(OBlock fromBlock) {
            _fromBlock = fromBlock;
        }
        OBlock getFromBlock() {
            return _fromBlock;
        }
        void setPortal(Portal portal) {
            _portal = portal;
        }
        Portal getPortal() {
            return _portal;
        }
        void setToBlock(OBlock toBlock) {
            _toBlock = toBlock;
        }
        OBlock getToBlock() {
            return _toBlock;
        }
        void setLength(float length) {
            _length = length;
        }
        float getLength() {
            return _length;
        }
        void setMetric(boolean isMetric) {
            _isMetric = isMetric;
        }
        boolean isMetric() {
            return _isMetric;
        }

    }

    static class SignalArray extends ArrayList<SignalRow> {

        public int numberOfSignals() {
            return size();
        }

    }

    public SignalTableModel(TableFrames parent) {
        super();
        _parent = parent;
        _portalMgr = InstanceManager.getDefault(PortalManager.class);
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
    }

    public void init() {
        makeList();
        initTempRow();
    }

    void initTempRow() {
        if (!_tabbed) {
            tempRow = new String[NUMCOLS];
            tempRow[LENGTHCOL] = twoDigit.format(0.0);
            tempRow[UNITSCOL] = Bundle.getMessage("in");
            tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
        }
    }

    // Rebuild _signalList CopyOnWriteArrayList<SignalRow>, copying Signals from Portal table
    private void makeList() {
        //CopyOnWriteArrayList<SignalRow> tempList = new CopyOnWriteArrayList<>();
        SignalArray tempList = new SignalArray();
        Collection<Portal> portals = _portalMgr.getPortalSet();
        for (Portal portal : portals) {
            // check portal is well formed
            OBlock fromBlock = portal.getFromBlock();
            OBlock toBlock = portal.getToBlock();
            if (fromBlock != null && toBlock != null) {
                SignalRow sr;
                NamedBean signal = portal.getFromSignal();
                if (signal != null) {
                    sr = new SignalRow(signal, fromBlock, portal, toBlock, portal.getFromSignalOffset(), toBlock.isMetric());
                    //_signalList.add(sr);
                    addToList(tempList, sr);
                    //log.debug("1 SR added to tempList, new size = {}", tempList.numberOfSignals());
                }
                signal = portal.getToSignal();
                if (signal != null) {
                    sr = new SignalRow(signal, toBlock, portal, fromBlock, portal.getToSignalOffset(), fromBlock.isMetric());
                    //_signalList.add(sr);
                    addToList(tempList, sr);
                    //log.debug("1 SR added to tempList, new size = {}", tempList.numberOfSignals());
                }
            } else {
                // Can't get jmri.util.JUnitAppender.assertErrorMessage recognized in TableFramesTest! OK just warn then
                log.warn("Portal {} needs an OBlock on each side", portal.getName());
            }
        }
        //_signalList = tempList;
        _signalList = (SignalArray) tempList.clone();
        _lastIdx = tempList.numberOfSignals();
        //log.debug("TempList copied, size = {}", tempList.numberOfSignals());
        _signalList.sort(new NameSorter());
        //log.debug("makeList exit: _signalList size {} items.", _signalList.numberOfSignals());
    }

    private static void addToList(SignalArray array, SignalRow sr) {
        // not in array, for the sort, insert at correct position // TODO add + sort instead?
        boolean add = true;
        for (int j = 0; j < array.numberOfSignals(); j++) {
            if (sr.getSignal().getDisplayName().compareTo(array.get(j).getSignal().getDisplayName()) < 0) {
                array.add(j, sr); // added first time
                add = false;
                //log.debug("comparing list item {} name {}", j, sr.getSignal().getDisplayName());
                break;
            }
        }
        if (add) {
            array.add(sr);
            //log.debug("comparing list item at last pos {} name {}", array.numberOfSignals() , sr.getSignal().getDisplayName());
        }
    }

    public static class NameSorter implements Comparator<SignalRow>
    {
        @Override
        public int compare(SignalRow o1, SignalRow o2) {
            return o2.getSignal().compareTo(o1.getSignal());
        }
    }

    private String checkSignalRow(SignalRow sr) {
        Portal portal = sr.getPortal();
        OBlock fromBlock = sr.getFromBlock();
        OBlock toBlock = sr.getToBlock();
        String msg = null;
        if (portal != null) {
            if (toBlock == null && fromBlock == null) {
                msg = Bundle.getMessage("SignalDirection",
                        portal.getName(),
                        portal.getFromBlock().getDisplayName(),
                        portal.getToBlock().getDisplayName());
                return msg;
            }
            OBlock pToBlk = portal.getToBlock();
            OBlock pFromBlk = portal.getFromBlock();
            if (pToBlk.equals(toBlock)) {
                if (fromBlock == null) {
                    sr.setFromBlock(pFromBlk);
                }
            } else if (pFromBlk.equals(toBlock)) {
                if (fromBlock == null) {
                    sr.setFromBlock(pToBlk);
                }
            } else if (pToBlk.equals(fromBlock)) {
                if (toBlock == null) {
                    sr.setToBlock(pFromBlk);
                }
            } else if (pFromBlk.equals(fromBlock)) {
                if (toBlock == null) {
                    sr.setToBlock(pToBlk);
                }
            } else {
                msg = Bundle.getMessage("PortalBlockConflict", portal.getName(),
                        (toBlock != null ? toBlock.getDisplayName() : "(null to-block reference)"));
            }
        } else if (fromBlock != null && toBlock != null) {
            Portal p = getPortalWithBlocks(fromBlock, toBlock);
            if (p == null) {
                msg = Bundle.getMessage("NoSuchPortal", fromBlock.getDisplayName(), toBlock.getDisplayName());
            } else {
                sr.setPortal(p);
            }
        }
        if (msg == null && fromBlock != null && fromBlock.equals(toBlock)) {
            msg = Bundle.getMessage("SametoFromBlock", fromBlock.getDisplayName());
        }
        return msg;
    }

    // From the PortalSet get the single portal using the given To and From OBlock.
    private Portal getPortalWithBlocks(OBlock fromBlock, OBlock toBlock) {
        Collection<Portal> portals = _portalMgr.getPortalSet();
        for (Portal portal : portals) {
            OBlock fromBlk = portal.getFromBlock();
            OBlock toBlk = portal.getToBlock();
            if ((fromBlk.equals(fromBlock) &&  toBlk.equals(toBlock)) ||
                    (fromBlk.equals(toBlock) && toBlk.equals(fromBlock))) {
                return portal;
            }
        }
        return null;
    }

    protected String checkDuplicateSignal(NamedBean signal) {
        //log.debug("checkDuplSig checking for duplicate Signal in list by the same name");
        if (signal == null) {
            return null;
        }
        for (SignalRow srow : _signalList) {
            if (signal.equals(srow.getSignal())) {
                return Bundle.getMessage("DuplSignalName", signal.getDisplayName(),
                        srow.getToBlock().getDisplayName(), srow.getPortal().getName(),
                        srow.getFromBlock().getDisplayName());
            }
        }
        return null;
    }

    private String checkDuplicateSignal(SignalRow row) {
        //log.debug("checkDuplSig checking for duplicate Signal in list using new entry row");
        NamedBean signal = row.getSignal();
        if (signal == null) {
            return null;
        }
        for (SignalRow srow : _signalList) {
            if (srow.equals(row)) {
                continue;
            }
            if (signal.equals(srow.getSignal())) {
                return Bundle.getMessage("DuplSignalName", signal.getDisplayName(), srow.getToBlock().getDisplayName(), srow.getPortal().getName(), srow.getFromBlock().getDisplayName());

            }
        }
        return null;
    }

    private String checkDuplicateProtection(SignalRow row) {
        Portal portal = row.getPortal();
        OBlock block = row.getToBlock();
        if (block == null || portal == null) {
            return null;
        }
        for (SignalRow srow : _signalList) {
            if (srow.equals(row)) {
                continue;
            }
            if (block.equals(srow.getToBlock()) && portal.equals(srow.getPortal())) {
                return Bundle.getMessage("DuplProtection", block.getDisplayName(), portal.getName(), srow.getFromBlock().getDisplayName(), srow.getSignal().getDisplayName());
            }
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS + (_tabbed ? 1 : 0); // add Edit column on _tabbed
    }

    @Override
    public int getRowCount() {
        return _signalList.numberOfSignals() + (_tabbed ? 0 : 1); // + 1 row in _desktop to create entry row
        // +1 adds the extra empty row at the bottom of the table display, causes IOB when called externally when _tabbed
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("SignalName");
            case FROM_BLOCK_COLUMN:
                return Bundle.getMessage("FromBlockName");
            case PORTAL_COLUMN:
                return Bundle.getMessage("ThroughPortal");
            case TO_BLOCK_COLUMN:
                return Bundle.getMessage("ToBlockName");
            case LENGTHCOL:
                return Bundle.getMessage("Offset");
            case UNITSCOL:
            case EDIT_COL:
                return "  ";
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!_tabbed && (rowIndex == _signalList.numberOfSignals())) { // this must be tempRow, a new entry, read values from tempRow
            if (columnIndex == LENGTHCOL) {
                //log.debug("GetValue SignalTable length entered {} =============== in row {}", _tempLen, rowIndex);
                if (tempRow[UNITSCOL].equals(Bundle.getMessage("cm"))) {
                    return (twoDigit.format(_tempLen/10));
                }
                return (twoDigit.format(_tempLen/25.4f));
            }
            if (columnIndex == UNITSCOL) {
                return tempRow[UNITSCOL].equals(Bundle.getMessage("cm")); // TODO renderer/special class
            }
            return tempRow[columnIndex];
        }
        if (rowIndex >= _signalList.numberOfSignals() || rowIndex >= _lastIdx) {
            //log.error("SignalTable requested ROW {}, SIZE is {}, expected {}", rowIndex, _signalList.numberOfSignals(), _lastIdx);
            //log.debug("items in list: {}", _signalList.numberOfSignals()); // debug
            return columnIndex + "" + rowIndex + "?";
        }

        SignalRow signalRow = _signalList.get(rowIndex); // edit an existing array entry
        switch (columnIndex) {
            case NAME_COLUMN:
                if (signalRow.getSignal() != null) {
                    return signalRow.getSignal().getDisplayName();
                }
                break;
            case FROM_BLOCK_COLUMN:
                if (signalRow.getFromBlock() != null) {
                    return signalRow.getFromBlock().getDisplayName();
                }
                break;
            case PORTAL_COLUMN:
                if (signalRow.getPortal() != null) {
                    return signalRow.getPortal().getName();
                }
                break;
            case TO_BLOCK_COLUMN:
                if (signalRow.getToBlock() != null) {
                    return signalRow.getToBlock().getDisplayName();
                }
                break;
            case LENGTHCOL:
                if (signalRow.isMetric()) {
                    return (twoDigit.format(signalRow.getLength()/10));
                }
                return (twoDigit.format(signalRow.getLength()/25.4f));
            case UNITSCOL:
                return signalRow.isMetric();
            case DELETE_COL:
                return Bundle.getMessage("ButtonDelete");
            case EDIT_COL:
                return Bundle.getMessage("ButtonEdit");
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (_signalList.numberOfSignals() == row) { // this is the new entry in tempRow, not yet in _signalList
            if (col == DELETE_COL) { // labeled "Clear" in tempRow
                initTempRow();
                fireTableRowsUpdated(row, row);
                return;
            } else if (col == UNITSCOL) {
                if (value.equals(true)) {
                    tempRow[UNITSCOL] = Bundle.getMessage("cm");
                } else {
                    tempRow[UNITSCOL] = Bundle.getMessage("in");
                }
                fireTableRowsUpdated(row, row);
                return;               
            } else if (col == LENGTHCOL) {
                //log.debug("SetValue SignalTable length set {} in row {}", value.toString(), row);
                try {
                    _tempLen = IntlUtilities.floatValue(value.toString());
                    //log.debug("setValue _tempLen = {} {}", _tempLen, tempRow[UNITSCOL]);
                    if (tempRow[UNITSCOL].equals(Bundle.getMessage("cm"))) {
                        _tempLen *= 10f;
                    } else {
                        _tempLen *= 25.4f;                            
                    }
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("BadNumber", tempRow[LENGTHCOL]),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);                    
                }
                return;
            }
            String str = (String) value;
            if (str == null || str.trim().length() == 0) {
                tempRow[col] = null;
                return;
            }
            tempRow[col] = str.trim();
            // try to add new value into new row in SignalTable
            OBlock fromBlock = null;
            OBlock toBlock = null;
            Portal portal = null;
            NamedBean signal;
            OBlockManager OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
            if (tempRow[FROM_BLOCK_COLUMN] != null) {
                fromBlock = OBlockMgr.getOBlock(tempRow[FROM_BLOCK_COLUMN]);
                if (fromBlock == null) {
                    msg = Bundle.getMessage("NoSuchBlock", tempRow[FROM_BLOCK_COLUMN]);
                }
            }
            if (msg == null && tempRow[TO_BLOCK_COLUMN] != null) {
                toBlock = OBlockMgr.getOBlock(tempRow[TO_BLOCK_COLUMN]);
                if (toBlock == null) {
                    msg = Bundle.getMessage("NoSuchBlock", tempRow[TO_BLOCK_COLUMN]);
                }
            }
            if (msg == null) {
                if (tempRow[PORTAL_COLUMN] != null) {
                    portal = _portalMgr.getPortal(tempRow[PORTAL_COLUMN]);
                    if (portal == null) {
                        msg = Bundle.getMessage("NoSuchPortalName", tempRow[PORTAL_COLUMN]);
                    }                    
                } else {
                    if (fromBlock != null && toBlock != null) {
                        portal = getPortalWithBlocks(fromBlock, toBlock);
                        if (portal == null) {
                            msg = Bundle.getMessage("NoSuchPortal", tempRow[FROM_BLOCK_COLUMN], tempRow[TO_BLOCK_COLUMN]);
                        } else {
                            tempRow[PORTAL_COLUMN] = portal.getName();
                        }
                    }                    
                }
            }
            if (msg == null && tempRow[NAME_COLUMN] != null) {
                signal = Portal.getSignal(tempRow[NAME_COLUMN]);
                if (signal == null) {
                    msg = Bundle.getMessage("NoSuchSignal", tempRow[NAME_COLUMN]);
                } else {
                    msg = checkDuplicateSignal(signal);
                }
                if (msg == null) {
                    if (fromBlock != null && toBlock != null) {
                        portal = getPortalWithBlocks(fromBlock, toBlock);
                        if (portal == null) {
                            msg = Bundle.getMessage("NoSuchPortal", tempRow[FROM_BLOCK_COLUMN], tempRow[TO_BLOCK_COLUMN]);
                        } else {
                            tempRow[PORTAL_COLUMN] = portal.getName();
                        }
                    } else {
                        return;
                    }
                }
                if (msg == null) {
                    float length = 0.0f;
                    boolean isMetric = tempRow[UNITSCOL].equals(Bundle.getMessage("cm"));
                    try {
                        length = IntlUtilities.floatValue(tempRow[LENGTHCOL]);
                        if (isMetric) {
                            length *= 10f;
                        } else {
                            length *= 25.4f;                            
                        }
                    } catch (ParseException e) {
                        msg = Bundle.getMessage("BadNumber", tempRow[LENGTHCOL]);                    
                    }
                    if (isMetric) {
                        tempRow[UNITSCOL] = Bundle.getMessage("cm");
                    } else {
                        tempRow[UNITSCOL] = Bundle.getMessage("in");
                    }
                    if (msg == null) {
                        // all checks passed, create new SignalRow to add to _signalList
                        SignalRow signalRow = new SignalRow(signal, fromBlock, portal, toBlock, length, isMetric);
                        msg = setSignal(signalRow, false);
                        //if (msg == null) {
                            //if (signalRow.getLength() == 0) {
                                //log.error("#544 empty tempRow added to SignalList (now {})", _signalList.numberOfSignals());
                            //}
                            //_signalList.add(signalRow); // BUG no need to do this, as the table will be updated from the OBlock settings
                            // it caused the ghost row, which is squasehed out when the list is rebuilt
                        //}
                        initTempRow();
                        fireTableDataChanged();
                    }
                }
            }
        } else { // Editing an existing signal configuration row
            SignalRow signalRow;
            try {
                signalRow = _signalList.get(row);
                //log.debug("SetValue fetched SignalRow {}", row);
            } catch (IndexOutOfBoundsException e) {
                // ignore, happened in 4.21.2 for some reason, showed as a duplicate row after new entry, now fixed
                log.warn("setValue out of range");
                return;
            }
            OBlockManager OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
            switch (col) {
                case NAME_COLUMN:
                    NamedBean signal = Portal.getSignal((String) value);
                    if (signal == null) {
                        msg = Bundle.getMessage("NoSuchSignal", value);
                        // signalRow.setSignal(null);
                        break;
                    }
                    Portal portal = signalRow.getPortal();
                    if (portal != null && signalRow.getToBlock() != null) {
                        NamedBean oldSignal = signalRow.getSignal();
                        signalRow.setSignal(signal);
                        msg = checkDuplicateSignal(signalRow);
                        if (msg == null) {
                            deleteSignal(signalRow);    // delete old
                            msg = setSignal(signalRow, false);
                            fireTableRowsUpdated(row, row);
                        } else {
                            signalRow.setSignal(oldSignal);
                        }
                    }
                    break;
                case FROM_BLOCK_COLUMN:
                    OBlock block = OBlockMgr.getOBlock((String) value);
                    if (block == null) {
                        msg = Bundle.getMessage("NoSuchBlock", value);
                        break;
                    }
                    if (block.equals(signalRow.getFromBlock())) {
                        break;      // no change
                    }
                    deleteSignal(signalRow);    // delete old
                    signalRow.setFromBlock(block);
                    portal = signalRow.getPortal();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setToBlock(null);
                    } else {
                        // get new portal
                        portal = getPortalWithBlocks(block, signalRow.getToBlock());
                        signalRow.setPortal(portal);
                    }
                    msg = checkSignalRow(signalRow);
                    if (msg == null) {
                        msg = checkDuplicateProtection(signalRow);
                    } else {
                        signalRow.setPortal(null);
                        break;
                    }
                    if (msg == null && signalRow.getPortal() != null) {
                        msg = setSignal(signalRow, true);
                    } else {
                        signalRow.setPortal(null);
                    }
                    fireTableRowsUpdated(row, row);
                    break;
                case PORTAL_COLUMN:
                    portal = _portalMgr.getPortal((String) value);
                    if (portal == null) {
                        msg = Bundle.getMessage("NoSuchPortalName", value);
                        break;
                    }
                    deleteSignal(signalRow);    // delete old in Portal
                    signalRow.setPortal(portal);
                    block = signalRow.getToBlock();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setFromBlock(null);
                    } else {
                        block = signalRow.getFromBlock();
                        if (checkPortalBlock(portal, block)) {
                            signalRow.setToBlock(null);
                        }
                    }
                    msg = checkSignalRow(signalRow);
                    if (msg == null) {
                        msg = checkDuplicateProtection(signalRow);
                    } else {
                        signalRow.setToBlock(null);
                        break;
                    }
                    if (msg == null) {
                        signalRow.setPortal(portal);
                        msg = setSignal(signalRow, false);
                        fireTableRowsUpdated(row, row);
                    }
                    break;
                case TO_BLOCK_COLUMN:
                    block = OBlockMgr.getOBlock((String) value);
                    if (block == null) {
                        msg = Bundle.getMessage("NoSuchBlock", value);
                        break;
                    }
                    if (block.equals(signalRow.getToBlock())) {
                        break;      // no change
                    }
                    deleteSignal(signalRow);    // delete old in Portal
                    signalRow.setToBlock(block);
                    portal = signalRow.getPortal();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setFromBlock(null);
                    } else {
                        // get new portal
                        portal = getPortalWithBlocks(signalRow.getFromBlock(), block);
                        signalRow.setPortal(portal);
                    }
                    msg = checkSignalRow(signalRow);
                    if (msg == null) {
                        msg = checkDuplicateProtection(signalRow);
                    } else {
                        signalRow.setPortal(null);
                        break;
                    }
                    if (msg == null && signalRow.getPortal() != null) {
                        msg = setSignal(signalRow, true);
                    } else {
                        signalRow.setPortal(null);
                    }
                    fireTableRowsUpdated(row, row);
                    break;
                case LENGTHCOL: // named "Offset" in table header, will be stored on ToBlock
                    //log.debug("SetValue SignalTable length set {} in row {}", value.toString(), row);
                    try {
                        float len = IntlUtilities.floatValue(value.toString());
                        //log.debug("SetValue Offset copied to: {} in row {}", len, row);
                        if (signalRow.isMetric()) {
                            signalRow.setLength(len * 10.0f);
                        } else {
                            signalRow.setLength(len * 25.4f);
                        }
                        //log.debug("Length stored in SR as {}", signalRow.getLength());
                        //fireTableRowsUpdated(row, row); // reads (GetValue) from portal signal as configured? ignores the new entry
                    } catch (ParseException e) {
                        msg = Bundle.getMessage("BadNumber", value);
                        //log.error("SetValue BadNumber {}", value);
                    }
                    if (msg == null && signalRow.getPortal() != null) {
                        msg = setSignal(signalRow, false); // configures Portal & OBlock
                    } else {
                        signalRow.setPortal(null);
                    }
                    //fireTableRowsUpdated(row, row); // not needed, change will be picked up from the OBlockTable PropertyChange
                    break;
                case UNITSCOL:
                    signalRow.setMetric((Boolean)value);
                    fireTableRowsUpdated(row, row);
                    break;
                case DELETE_COL:
                    deleteSignal(signalRow);
                    _signalList.remove(signalRow);
                    fireTableDataChanged();
                    break;
                case EDIT_COL:
                    editSignal(Portal.getSignal(signalRow.getSignal().getDisplayName()), signalRow);
                    break;
                default:
                    // fall through
                    break;
            }
        }

        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            // doesn't close by clicking OK after DnD as focus lost, only Esc in JMRI 4.21.2 on macOS
        }
    }

    // also used in _tabbed EditSignalPane
    protected void deleteSignal(SignalRow signalRow) {
        Portal portal = signalRow.getPortal();
        if (portal == null) {
            portal = getPortalWithBlocks(signalRow.getFromBlock(), signalRow.getToBlock());
        }
        if (portal != null) {
            // remove signal from previous portal
            portal.deleteSignal(signalRow.getSignal());
        }
    }

    private void editSignal(NamedBean signal, SignalRow sr) {
        if (_tabbed && signal != null && !inEditMode) {
            inEditMode = true;
            // open SignalEditFrame
            SignalEditFrame sef = new SignalEditFrame(Bundle.getMessage("TitleSignalEditor", sr.getSignal().getDisplayName()),
                    signal, sr, this);
            // TODO run on separate thread?
            sef.setVisible(true);
        }
    }

    static private String setSignal(SignalRow signalRow, boolean deletePortal) {
        Portal portal = signalRow.getPortal();
        float length = signalRow.getLength();
        if (portal.setProtectSignal(signalRow.getSignal(), length, signalRow.getToBlock())) {
            if (signalRow.getFromBlock() == null) {
                signalRow.setFromBlock(portal.getOpposingBlock(signalRow.getToBlock()));
            }
        } else {
            if (deletePortal) {
                signalRow.setPortal(null);
            } else {
                signalRow.setToBlock(null);
            }
            return Bundle.getMessage("PortalBlockConflict", portal.getName(),
                    signalRow.getToBlock().getDisplayName());
        }
        return null;
    }

    static private boolean checkPortalBlock(Portal portal, OBlock block) {
        if (block == null) {
            return false;
        }
        return (block.equals(portal.getToBlock()) || block.equals(portal.getFromBlock()));
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case DELETE_COL:
            case EDIT_COL:
                return JButton.class;
            case UNITSCOL:
                return JToggleButton.class;
            case NAME_COLUMN:
            default:
                return String.class;
        }
    }

    public static int getPreferredWidth(int col) {
        switch (col) {
            case NAME_COLUMN:
            case FROM_BLOCK_COLUMN:
            case PORTAL_COLUMN:
            case TO_BLOCK_COLUMN:
                return new JTextField(11).getPreferredSize().width;
            case LENGTHCOL:
                return new JTextField(5).getPreferredSize().width;
            case UNITSCOL:
                return new JTextField(4).getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width; // NOI18N
            case EDIT_COL:
                return new JButton("EDIT").getPreferredSize().width; // NOI18N
            default:
                // fall through
                break;
        }
        return 5;
    }

    public boolean editMode() {
        return inEditMode;
    }

    public void setEditMode(boolean editing) {
        inEditMode = editing;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property.equals("length") || property.equals("portalCount")
                || property.equals("UserName") || property.equals("signalChange")) {
            makeList();
            fireTableDataChanged();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalTableModel.class);

}
