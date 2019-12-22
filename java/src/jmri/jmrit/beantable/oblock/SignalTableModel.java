package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks
 * <p>
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
 */
import java.beans.PropertyChangeEvent;
import java.text.ParseException;

import java.util.*;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.util.IntlUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalTableModel extends AbstractTableModel {

    public static final int NAME_COLUMN = 0;
    public static final int FROM_BLOCK_COLUMN = 1;
    public static final int PORTAL_COLUMN = 2;
    public static final int TO_BLOCK_COLUMN = 3;
    public static final int LENGTHCOL = 4;
    public static final int UNITSCOL = 5;
    public static final int DELETE_COL = 6;
    public static final int NUMCOLS = 7;

    private ArrayList<SignalRow> _signalList = new ArrayList<SignalRow>();
    PortalManager _portalMgr;
    private float _tempLen = 0.0f;      // mm for length col of tempRow

    static class SignalRow {

        NamedBean _signal;
        OBlock _fromBlock;
        Portal _portal;
        OBlock _toBlock;
        float _length;  // adjustment to speed change point
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

    private String[] tempRow = new String[NUMCOLS];
    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    TableFrames _parent;

    public SignalTableModel(TableFrames parent) {
        super();
        _parent = parent;
        _portalMgr = InstanceManager.getDefault(PortalManager.class);
    }

    public void init() {
        makeList();
        initTempRow();
    }

    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[LENGTHCOL] = twoDigit.format(0.0);
        tempRow[UNITSCOL] = Bundle.getMessage("in");
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }

    private void makeList() {
        ArrayList<SignalRow> tempList = new ArrayList<SignalRow>();
        Collection<Portal> portals = _portalMgr.getPortalSet();
        for (Portal portal : portals) {
            // check portal is well formed
            OBlock fromBlock = portal.getFromBlock();
            OBlock toBlock = portal.getToBlock();
            if (fromBlock != null && toBlock != null) {
                NamedBean signal = portal.getFromSignal();
                SignalRow sr = null;
                if (signal != null) {
                    sr = new SignalRow(signal, fromBlock, portal, toBlock,
                             portal.getFromSignalOffset(), toBlock.isMetric());
                    addToList(tempList, sr);
                }
                signal = portal.getToSignal();
                if (signal != null) {
                    sr = new SignalRow(signal, toBlock, portal, fromBlock, 
                            portal.getToSignalOffset(), fromBlock.isMetric());
                    addToList(tempList, sr);
                }
            } else {
//                Can't get jmri.util.JUnitAppender.assertErrorMessage recognized in TableFramesTest! OK just warn then
                log.warn("Portal {} needs an OBlock on each side", portal.getName());
            }
        }
        _signalList = tempList;
        if (log.isDebugEnabled()) {
            log.debug("makeList exit: _signalList has "
                    + _signalList.size() + " rows.");
        }
    }

    static private void addToList(List<SignalRow> tempList, SignalRow sr) {
        // not in list, for the sort, insert at correct position
        boolean add = true;
        for (int j = 0; j < tempList.size(); j++) {
            if (sr.getSignal().getDisplayName().compareTo(tempList.get(j).getSignal().getDisplayName()) < 0) {
                tempList.add(j, sr);
                add = false;
                break;
            }
        }
        if (add) {
            tempList.add(sr);
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
            Portal p = getPortalwithBlocks(fromBlock, toBlock);
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

    private Portal getPortalwithBlocks(OBlock fromBlock, OBlock toBlock) {
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

    private String checkDuplicateSignal(NamedBean signal) {
        if (signal == null) {
            return null;
        }
        for (int i = 0; i < _signalList.size(); i++) {
            SignalRow srow = _signalList.get(i);
            if (signal.equals(srow.getSignal())) {
                return Bundle.getMessage("DuplSignalName",
                        signal.getDisplayName(), srow.getToBlock().getDisplayName(),
                        srow.getPortal().getName(), srow.getFromBlock().getDisplayName());

            }
        }
        return null;
    }

    private String checkDuplicateSignal(SignalRow row) {
        NamedBean signal = row.getSignal();
        if (signal == null) {
            return null;
        }
        for (int i = 0; i < _signalList.size(); i++) {
            SignalRow srow = _signalList.get(i);
            if (srow.equals(row)) {
                continue;
            }
            if (signal.equals(srow.getSignal())) {
                return Bundle.getMessage("DuplSignalName",
                        signal.getDisplayName(), srow.getToBlock().getDisplayName(),
                        srow.getPortal().getName(), srow.getFromBlock().getDisplayName());

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
        for (int i = 0; i < _signalList.size(); i++) {
            SignalRow srow = _signalList.get(i);
            if (srow.equals(row)) {
                continue;
            }
            if (block.equals(srow.getToBlock()) && portal.equals(srow.getPortal())) {
                return Bundle.getMessage("DuplProtection", block.getDisplayName(), portal.getName(),
                        srow.getFromBlock().getDisplayName(), srow.getSignal().getDisplayName());
            }
        }
        return null;

    }

    @Override
    public int getColumnCount() {
        return NUMCOLS;
    }

    @Override
    public int getRowCount() {
        return _signalList.size() + 1;
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
                return "  ";
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        //if (log.isDebugEnabled()) log.debug("getValueAt rowIndex= "+rowIndex+" _lastIdx= "+_lastIdx);
        if (_signalList.size() == rowIndex) {
            if (columnIndex==LENGTHCOL) {
                if (tempRow[UNITSCOL].equals(Bundle.getMessage("cm"))) {
                    return (twoDigit.format(_tempLen/10));
                }
                return (twoDigit.format(_tempLen/25.4f));
            }
            if (columnIndex==UNITSCOL) {
                return Boolean.valueOf(tempRow[UNITSCOL].equals(Bundle.getMessage("cm")));
            }
            return tempRow[columnIndex];
        }
        SignalRow signalRow = _signalList.get(rowIndex);
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
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (_signalList.size() == row) {
            if (col == DELETE_COL) {
                initTempRow();
                fireTableRowsUpdated(row, row);
                return;
            } else if (col == UNITSCOL) {
                if (((Boolean)value).booleanValue()) {
                    tempRow[UNITSCOL] = Bundle.getMessage("cm");
                } else {
                    tempRow[UNITSCOL] = Bundle.getMessage("in");
                }
                fireTableRowsUpdated(row, row);
                return;               
            } else if (col == LENGTHCOL) {
                try {
                    _tempLen = IntlUtilities.floatValue(value.toString());
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
            OBlock fromBlock = null;
            OBlock toBlock = null;
            Portal portal = null;
            NamedBean signal = null;
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
                        portal = getPortalwithBlocks(fromBlock, toBlock);
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
                if (msg==null) {
                    if (fromBlock != null && toBlock != null) {
                        portal = getPortalwithBlocks(fromBlock, toBlock);
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
                        SignalRow signalRow = new SignalRow(signal, fromBlock, portal, toBlock, length, isMetric);
                        msg = setSignal(signalRow, false);
                        if (msg==null) {
                            _signalList.add(signalRow);                            
                        }
                        initTempRow();
                        fireTableDataChanged();                        
                    }
                }
            }
        } else { // Editing existing signal configurations
            SignalRow signalRow = _signalList.get(row);
            OBlockManager OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
            switch (col) {
                case NAME_COLUMN:
                    NamedBean signal = Portal.getSignal((String) value);
                    if (signal == null) {
                        msg = Bundle.getMessage("NoSuchSignal", (String) value);
//                        signalRow.setSignal(null);                              
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
                        msg = Bundle.getMessage("NoSuchBlock", (String) value);
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
                        portal = getPortalwithBlocks(block, signalRow.getToBlock());
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
                        msg = Bundle.getMessage("NoSuchPortalName", (String) value);
                        break;
                    }
                    deleteSignal(signalRow);    // delete old
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
                        msg = Bundle.getMessage("NoSuchBlock", (String) value);
                        break;
                    }
                    if (block.equals(signalRow.getToBlock())) {
                        break;      // no change
                    }
                    deleteSignal(signalRow);    // delete old
                    signalRow.setToBlock(block);
                    portal = signalRow.getPortal();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setFromBlock(null);
                    } else {
                        // get new portal
                        portal = getPortalwithBlocks(signalRow.getFromBlock(), block);
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
                case LENGTHCOL:
                    try {
                        float len = IntlUtilities.floatValue(value.toString());
                        if (signalRow.isMetric()) {
                            signalRow.setLength(len * 10.0f);
                        } else {
                            signalRow.setLength(len * 25.4f);
                        }
                        fireTableRowsUpdated(row, row);                    
                    } catch (ParseException e) {
                        msg = Bundle.getMessage("BadNumber", value);                    
                    }
                    if (msg == null && signalRow.getPortal() != null) {
                        msg = setSignal(signalRow, false);
                    } else {
                        signalRow.setPortal(null);
                    }
                    fireTableRowsUpdated(row, row);
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
                default:
                    // fall through
                    break;
            }
        }

        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSignal(SignalRow signalRow) {
        Portal portal = signalRow.getPortal();
        if (portal == null) {
            portal = getPortalwithBlocks(signalRow.getFromBlock(), signalRow.getToBlock());
        }
        if (portal != null) {
            // remove signal from previous portal
            portal.deleteSignal(signalRow.getSignal());
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
        if (block==null) {
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
        if (col == DELETE_COL) {
            return JButton.class;
        } else if (col == UNITSCOL ) {
            return Boolean.class;
        }
        return String.class;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
    static public int getPreferredWidth(int col) {
        switch (col) {
            case NAME_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case FROM_BLOCK_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case PORTAL_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case TO_BLOCK_COLUMN:
                return new JTextField(18).getPreferredSize().width;
            case LENGTHCOL:
                return new JTextField(5).getPreferredSize().width;
            case UNITSCOL:
                return new JTextField(2).getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width;
            default:
                // fall through
                break;
        }
        return 5;
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
