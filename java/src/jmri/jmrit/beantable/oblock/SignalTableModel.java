package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks 
 *<P> 
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
 * @author	Pete Cressman (C) 2010
 * @version     $Revision$
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import jmri.InstanceManager;
import jmri.NamedBean;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

public class SignalTableModel extends AbstractTableModel {

    public static final int NAME_COLUMN = 0;
    public static final int FROM_BLOCK_COLUMN = 1;
    public static final int PORTAL_COLUMN = 2;
    public static final int TO_BLOCK_COLUMN = 3;
    public static final int TIME_OFFSET = 4;
    static public final int DELETE_COL = 6;
    public static final int NUMCOLS = 7;

    static final ResourceBundle rbo = ResourceBundle.getBundle("jmri.jmrit.beantable.OBlockTableBundle");
    
    private ArrayList <SignalRow> _signalList = new ArrayList <SignalRow>();

    static class SignalRow {
        NamedBean _signal;
        OBlock _fromBlock;
        Portal _portal;
        OBlock _toBlock;
        long   _delayTime;
        SignalRow(NamedBean signal, OBlock fromBlock, Portal portal, OBlock toBlock, long delayTime) {
            _signal = signal;
            _fromBlock = fromBlock;
            _portal = portal;
            _toBlock = toBlock;
            _delayTime = delayTime;
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
        void setDelayTime (long time) {
            _delayTime = time;
        }
        long getDelayTime() {
            return _delayTime;
        }
    }

    private String[] tempRow= new String[NUMCOLS];

    TableFrames _parent;

    public SignalTableModel(TableFrames parent) {
        super();
        _parent = parent;
    }

    public void init() {
        makeList();
        initTempRow();
    }

    void initTempRow() {
        for (int i=0; i<NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[TIME_OFFSET] = "0";
    }
    private void makeList() {
        ArrayList <SignalRow> tempList = new ArrayList <SignalRow>();
        // save signals that do not have all their blocks yet
        for (int i=0; i<_signalList.size(); i++) {
            SignalRow sr = _signalList.get(i);
            if (sr.getToBlock()==null || sr.getFromBlock()==null) {
                tempList.add(sr);
            }
        }
        // collect signals entered into Portals
        Iterator<Portal> bIter = _parent.getPortalModel().getPortalList().iterator();
        while (bIter.hasNext()) {
            Portal  portal = bIter.next();
            NamedBean signal = portal.getFromSignal();
            SignalRow sr = null;
            if (signal!=null) {
                sr = new SignalRow(signal, portal.getFromBlock(), portal,
                                         portal.getToBlock(), portal.getFromSignalDelay());
                addToList(tempList, sr);
            }
            signal = portal.getToSignal();
            if (signal!=null) {
                sr = new SignalRow(signal, portal.getToBlock(), portal,
                                           portal.getFromBlock(), portal.getToSignalDelay());
                addToList(tempList, sr);
            }
        }
        // remove duplicates
        for (int j=0; j<tempList.size(); j++) {
            SignalRow sr = tempList.get(j);
            NamedBean srSignal = sr.getSignal();
            if (srSignal==null) {
                continue;
            }
            for (int i=tempList.size()-1; i>j; i--) {
                SignalRow srowi = tempList.get(i);
                if (srowi.equals(sr)) {
                    continue;
                }
                if (srSignal.equals(srowi.getSignal())) {
                    if (log.isDebugEnabled()) log.debug("makeList: duplicate signal "+srSignal.getDisplayName());

                    if (sr.getFromBlock()!=null && !sr.getFromBlock().equals(srowi.getFromBlock())) {
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbo.getString("IllegalUseOfSignal"), srSignal.getDisplayName(), 
                            sr.getToBlock().getDisplayName(), 
                            sr.getFromBlock().getDisplayName(), srowi.getFromBlock().getDisplayName()),
                                rbo.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        _signalList = tempList;
        if (log.isDebugEnabled()) log.debug("makeList exit: _signalList has "
                                            +_signalList.size()+" rows.");
    }

    private void addToList (List <SignalRow> tempList, SignalRow sr) {
        // not in list, for the sort, insert at correct position
        boolean add = true;
        for (int j=0; j<tempList.size(); j++) {
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

    private boolean verifySignalRow(SignalRow sr) {
        NamedBean srSignal = sr.getSignal();
        if (srSignal==null) {
            return true;
        }
        if (log.isDebugEnabled()) log.debug("verifySignalRow: signal= "+srSignal.getDisplayName()+
                                            " _signalList.size= "+_signalList.size());
        for (int i=0; i<_signalList.size(); i++)  {
            SignalRow rowSignal = _signalList.get(i);
            if (rowSignal.equals(sr)) {
                continue;
            }
            if (srSignal.equals(rowSignal.getSignal()) ) {
                // duplicate signals
                OBlock srFromBlk = sr.getFromBlock();
                if (srFromBlk==null) {
                    return true;
                }
                if (srFromBlk.equals(rowSignal.getFromBlock()) ) {
                    // OK if from blocks the same, but inform user
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbo.getString("DuplSignalName"), srSignal.getDisplayName(),
                            srFromBlk.getDisplayName(), rowSignal.getToBlock().getDisplayName()),
                        rbo.getString("InfoTitle"), JOptionPane.INFORMATION_MESSAGE);
                } else if (rowSignal.getFromBlock()!= null) {
                    // error. Cannot use from 2 different blocks
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rbo.getString("IllegalUseOfSignal"), srSignal.getDisplayName(), 
                        rowSignal.getToBlock().getDisplayName(), 
                        srFromBlk.getDisplayName(), rowSignal.getFromBlock().getDisplayName()),
                            rbo.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public int getColumnCount () {
        return NUMCOLS;
    }

    public int getRowCount() {
        return _signalList.size() + 1;
    }

    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:       return rbo.getString("SignalName");
            case FROM_BLOCK_COLUMN: return rbo.getString("FromBlockName");
            case PORTAL_COLUMN:     return rbo.getString("ThroughPortal");
            case TO_BLOCK_COLUMN:   return rbo.getString("ToBlockName");
            case TIME_OFFSET:       return rbo.getString("TimeOffset");
        }
        return "";
    }

    String _saveSignalName;
    public Object getValueAt(int rowIndex, int columnIndex) {
        //if (log.isDebugEnabled()) log.debug("getValueAt rowIndex= "+rowIndex+" _lastIdx= "+_lastIdx);
        if (_signalList.size() == rowIndex) {
            if (_saveSignalName!=null && _parent.getSignalTablePane()!=null) {
                int idx = getSignalIndex(_saveSignalName);
                if (idx > -1) {
                    if (log.isDebugEnabled()) log.debug("Signal Scroll of "+_saveSignalName+", to "+idx*TableFrames.ROW_HEIGHT); 
                    _parent.getSignalTablePane().getVerticalScrollBar().setValue(idx*TableFrames.ROW_HEIGHT);
                    _saveSignalName = null;
                }
            }
            return tempRow[columnIndex];
        }
        switch(columnIndex) {
            case NAME_COLUMN:
                if (_signalList.get(rowIndex).getSignal()!=null) {
                    return _signalList.get(rowIndex).getSignal().getDisplayName();
                }
                break;
            case FROM_BLOCK_COLUMN:
                if (_signalList.get(rowIndex).getFromBlock()!=null) {
                    return _signalList.get(rowIndex).getFromBlock().getDisplayName();
                }
                break;
            case PORTAL_COLUMN:
                if (_signalList.get(rowIndex).getPortal()!=null) {
                    return  _signalList.get(rowIndex).getPortal().getName();
                }
                break;
            case TO_BLOCK_COLUMN:
                if (_signalList.get(rowIndex).getToBlock()!=null) {
                    return _signalList.get(rowIndex).getToBlock().getDisplayName();
                }
                break;
            case TIME_OFFSET:
                return Long.toString(_signalList.get(rowIndex).getDelayTime());
            case DELETE_COL:
                return rbo.getString("ButtonDelete");
        }
        return "";
    }

    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (_signalList.size() == row) {
            if (log.isDebugEnabled()) log.debug("setValueAt: col= "+col+", row= "+row+", value= "+(String)value);
            switch (col) {
                case NAME_COLUMN:
                    String name = (String)value;
                    NamedBean signal = Portal.getSignal((String)value); 
                    if (signal!=null) {
                        _saveSignalName = name;
                        // Note: Portal ctor will add this Portal to each of its 'from' & 'to' Block.
                        OBlock fromBlock = InstanceManager.oBlockManagerInstance()
                                                    .getOBlock(tempRow[FROM_BLOCK_COLUMN]);
                        OBlock toBlock = InstanceManager.oBlockManagerInstance()
                                                    .getOBlock(tempRow[TO_BLOCK_COLUMN]);
                        long time = 0;
                        try {
                            time = Long.parseLong(tempRow[TIME_OFFSET]);
                        } catch (NumberFormatException nfe) {
                            msg = rbo.getString("DelayTriggerTime");
                        }
                        Portal portal = _parent.getPortalModel().getPortalByName(tempRow[PORTAL_COLUMN]);
                        if (portal==null && fromBlock!=null && toBlock!=null) {
                            portal = _parent.getPortalModel().getPortal(fromBlock, toBlock);
                            if (portal==null && msg==null) {
                                msg = java.text.MessageFormat.format(rbo.getString("NoSuchPortal"), 
                                                fromBlock.getDisplayName(), toBlock.getDisplayName());
                            }
                        }
                        SignalRow signalRow = new SignalRow(signal, fromBlock, portal, toBlock, time);
                        checkPortal(signalRow);
                        fromBlock = signalRow.getFromBlock();
                        toBlock = signalRow.getToBlock();
                        if ( msg==null && fromBlock != null && fromBlock.equals(toBlock)) {
                            msg = java.text.MessageFormat.format(
                                    rbo.getString("SametoFromBlock"), fromBlock.getDisplayName());
                        }
                        if (msg==null && portal!=null) {
                            if (toBlock!=null) {
                                if (portal.setProtectSignal(signal, time, toBlock)) {
                                    if (fromBlock==null) {
                                        signalRow.setFromBlock(portal.getOpposingBlock(toBlock));
                                    }
                                } else {
                                    signalRow.setPortal(null);
                                }
/*                            } else {
                                if (portal.setApproachSignal(signal, time, fromBlock)) {
                                    signalRow.setToBlock(portal.getOpposingBlock(fromBlock));
                                } else {
                                    signalRow.setPortal(null);
                                }*/
                            }
                        }
                        if (verifySignalRow(signalRow)) {
                            _signalList.add(signalRow);
                        }
                        makeList();
                        initTempRow();
                        fireTableDataChanged();
                    } else {
                        msg = java.text.MessageFormat.format(rbo.getString("NoSuchSignal"), (String)value);
                    }
                    break;
                case FROM_BLOCK_COLUMN:
                    OBlock block = InstanceManager.oBlockManagerInstance().getOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), (String)value);
                    } else {
                        tempRow[FROM_BLOCK_COLUMN] = (String)value;
                    }
                    break;
                case PORTAL_COLUMN:
                    Portal portal = _parent.getPortalModel().getPortalByName((String)value);
                    if (portal==null) {
                        msg = java.text.MessageFormat.format(
                                rbo.getString("NoSuchPortalName"), (String)value);
                    } else {
                        tempRow[PORTAL_COLUMN] = (String)value;
                    }
                    break;
                case TO_BLOCK_COLUMN:
                    block = InstanceManager.oBlockManagerInstance().getOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), (String)value);
                    } else {
                        tempRow[TO_BLOCK_COLUMN] = (String)value;
                    }
                    break;
                case TIME_OFFSET:
                    try {
                        Long.parseLong((String)value);
                        tempRow[TIME_OFFSET] = (String)value;
                    } catch (NumberFormatException nfe) {
                        msg = rbo.getString("DelayTriggerTime");
                    }
                    break;
            }
        } else {
            SignalRow signalRow =_signalList.get(row);
            switch(col) {
                case NAME_COLUMN:
                    NamedBean signal = Portal.getSignal((String)value); 
                    if (signal==null) {
                        msg = java.text.MessageFormat.format(rbo.getString("NoSuchSignal"), (String)value);
                        break;
                    }
                    Portal portal = getPortal(signalRow.getPortal(), signalRow.getFromBlock(), signalRow.getToBlock());
                    if (portal!=null) {
                        if (signalRow.getToBlock()!=null) {
                            portal.setProtectSignal(signal, signalRow.getDelayTime(), signalRow.getToBlock());
                            if (signalRow.getFromBlock()==null) {
                                signalRow.setFromBlock(portal.getOpposingBlock(signalRow.getToBlock()));
                            }
/*                        } else {
                            portal.setApproachSignal(signal, signalRow.getDelayTime(), signalRow.getFromBlock());
                            if (signalRow.getToBlock()==null) {
                                signalRow.setToBlock(portal.getOpposingBlock(signalRow.getFromBlock()));
                            }*/
                        }
                    }
                    signalRow.setSignal(signal);
                    if (!verifySignalRow(signalRow)) {
                        deleteSignal(signalRow);
                        _signalList.remove(signalRow);
                    }
                    makeList();
                    fireTableRowsUpdated(row,row);
                    break;
                case FROM_BLOCK_COLUMN:
                    OBlock block = InstanceManager.oBlockManagerInstance().getOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), (String)value);
                        break;
                    }
                    if (block.equals(signalRow.getFromBlock())) {
                        break;      // no change
                    }
                    if (block.equals(signalRow.getToBlock())){
                        msg = java.text.MessageFormat.format(
                                rbo.getString("SametoFromBlock"), block.getDisplayName());
                        break;
                    }
                    deleteSignal(signalRow);    // delete old
                     // get new portal
                    portal = getPortal(signalRow.getPortal(), block , signalRow.getToBlock());
                    if (portal!=null) {
                        if (!checkPortalBlock(portal, block)) {
                            portal = null;
                            signalRow.setToBlock(null);
                        }
                    }
                    signalRow.setPortal(portal);
                    signalRow.setFromBlock(block);
/*                    if (portal!=null) {
                        checkPortal(signalRow);
                        if (portal.setApproachSignal(signalRow.getSignal(), signalRow.getDelayTime(), block)) {
                            if (signalRow.getToBlock()==null) {
                                signalRow.setToBlock(portal.getOpposingBlock(signalRow.getFromBlock()));
                            }
                        } else {
                            signalRow.setPortal(null);
                        }
                        if (!verifySignalRow(signalRow)) {
                            deleteSignal(signalRow);
                            _signalList.remove(signalRow);
                        }
                        makeList();
                        fireTableRowsUpdated(row,row);
                    }*/
                    break;
                case PORTAL_COLUMN:
                    deleteSignal(signalRow);    // delete old
                    portal = _parent.getPortalModel().getPortalByName((String)value);
                    if (portal==null) {
                        msg = java.text.MessageFormat.format(rbo.getString("NoSuchPortalName"), (String)value);
                        break;
                    } else {
                        signalRow.setPortal(portal);
                        checkPortal(signalRow);
                        if (signalRow.getToBlock()!=null && portal.setProtectSignal(
                                                                    signalRow.getSignal(), 
                                                                    signalRow.getDelayTime(), 
                                                                    signalRow.getToBlock())) {
                            if (signalRow.getFromBlock()==null) {
                                signalRow.setFromBlock(portal.getOpposingBlock(signalRow.getToBlock()));
                            }
/*                        } else if (signalRow.getFromBlock()!=null && portal.setApproachSignal(
                                                                        signalRow.getSignal(),
                                                                        signalRow.getDelayTime(), 
                                                                        signalRow.getFromBlock())) {
                            if (signalRow.getToBlock()==null) {
                                signalRow.setToBlock(portal.getOpposingBlock(signalRow.getFromBlock()));
                            }*/
                        } else if (signalRow.getToBlock()==null && signalRow.getFromBlock()==null) {
                            signalRow.setFromBlock(null);
                            signalRow.setToBlock(null);
                            msg = java.text.MessageFormat.format(rbo.getString("SignalDirection"),
                                                                 portal.getName(), 
                                                                 portal.getFromBlock().getDisplayName(),
                                                                 portal.getToBlock().getDisplayName());
                            break;
                        }
                        if (!verifySignalRow(signalRow)) {
                            deleteSignal(signalRow);
                            _signalList.remove(signalRow);
                        }
                        makeList();
                        fireTableRowsUpdated(row,row);
                    }
                    break;
                case TO_BLOCK_COLUMN:
                    block = InstanceManager.oBlockManagerInstance().getOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), (String)value);
                        break;
                    }
                    if (block.equals(signalRow.getToBlock())) {
                        break;      // no change
                    }
                    if (block.equals(signalRow.getFromBlock())){
                        msg = java.text.MessageFormat.format(
                                rbo.getString("SametoFromBlock"), block.getDisplayName());
                        break;
                    }
                    deleteSignal(signalRow);    // delete old
                    portal = getPortal(signalRow.getPortal(), signalRow.getFromBlock(), block);
                    if (portal!=null) {
                        if (!checkPortalBlock(portal, block)) {
                            portal = null;
                            signalRow.setFromBlock(null);
                        }
                    }
                    signalRow.setPortal(portal);
                    signalRow.setToBlock(block);
                    if (portal!=null) {
                        checkPortal(signalRow);
                        if (portal.setProtectSignal(signalRow.getSignal(), signalRow.getDelayTime(), block)) {
                            if (signalRow.getFromBlock()==null) {
                                signalRow.setFromBlock(portal.getOpposingBlock(signalRow.getToBlock()));
                            }
                        } else {
                            signalRow.setPortal(null);
                        }
                        if (!verifySignalRow(signalRow)) {
                            deleteSignal(signalRow);
                            _signalList.remove(signalRow);
                        }
                        makeList();
                        fireTableRowsUpdated(row,row);
                    }
                    break;
                case TIME_OFFSET:
                    long time = 0;
                    try {
                        time = Long.parseLong((String)value);
                    } catch (NumberFormatException nfe) {
                        msg = rbo.getString("DelayTriggerTime");
                        break;
                    }
                    signalRow.setDelayTime(time);
                    portal = getPortal(signalRow.getPortal(), signalRow.getFromBlock(), signalRow.getToBlock());
                    if (portal!=null) {
                    	portal.setProtectSignal(signalRow.getSignal(), time, signalRow.getToBlock());
                    }
                    fireTableRowsUpdated(row,row);
                    break;
                case DELETE_COL:
                    deleteSignal(signalRow);
                    _signalList.remove(signalRow);
                    fireTableDataChanged();

            }
        }

        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    rbo.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }
/*
    private SignalRow getSignalRow(String name) {
        for (int i=0; i<_signalList.size(); i++)  {
            if (_signalList.get(i).getSignal().getDisplayName().equals(name)) { 
                return _signalList.get(i);
            }
        }
        return null;
    }  */

    private Portal getPortal(Portal p, OBlock fromBlock, OBlock toBlock) {
        if (p!= null) {
            return p;
        } else {
            return _parent.getPortalModel().getPortal(fromBlock, toBlock);
        }
    }

    private int getSignalIndex(String name) {
        for (int i=0; i<_signalList.size(); i++)  {
            if (_signalList.get(i).getSignal().getDisplayName().equals(name)) { 
                return i;
            }
        }
        return -1;
    }

    private void deleteSignal(SignalRow signalRow) {
        Portal portal = signalRow.getPortal();
        if (portal==null) {
            portal = getPortal(null, signalRow.getFromBlock(), signalRow.getToBlock());
        }
        if (portal!=null) {
            // remove signal from previous portal
            portal.deleteSignal(signalRow.getSignal());
        }
    }

    private void checkPortal (SignalRow signalRow) {
        // check that blocks belong to portal
        Portal portal = signalRow.getPortal();
        if (portal==null) {
            return;
        }
        if (!checkPortalBlock(portal, signalRow.getFromBlock())) {
            signalRow.setFromBlock(null);
        }
        if (!checkPortalBlock(portal, signalRow.getToBlock())) {
            signalRow.setToBlock(null);
        }
    }
    private boolean checkPortalBlock(Portal portal, OBlock block) {
        return (portal.getToBlock().equals(block) || portal.getFromBlock().equals(block));
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public Class<?> getColumnClass(int col) {
        if (col == DELETE_COL) {
            return JButton.class;
        }
        return String.class;
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case NAME_COLUMN:       return new JTextField(15).getPreferredSize().width;
            case FROM_BLOCK_COLUMN: return new JTextField(15).getPreferredSize().width;
            case PORTAL_COLUMN:     return new JTextField(15).getPreferredSize().width;
            case TO_BLOCK_COLUMN:   return new JTextField(15).getPreferredSize().width;
            case TIME_OFFSET:       return new JTextField(6).getPreferredSize().width;
            case DELETE_COL:        return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (property.equals("length") || property.equals("portalCount")
                            || property.equals("UserName")) {
            makeList();
            fireTableDataChanged();
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalTableModel.class.getName());
}
