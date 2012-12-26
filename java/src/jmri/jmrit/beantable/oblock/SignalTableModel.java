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
    static public final int DELETE_COL = 5;
    public static final int NUMCOLS = 6;

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
        tempRow[DELETE_COL] = rbo.getString("ButtonClear");
    }
    private void makeList() {
        ArrayList <SignalRow> tempList = new ArrayList <SignalRow>();
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
    
    private String checkSignalRow(SignalRow sr) {
    	Portal portal = sr.getPortal();
    	OBlock fromBlock = sr.getFromBlock();
    	OBlock toBlock = sr.getToBlock();
    	String msg = null;
        if (portal!=null) {
            if (toBlock==null && sr.getFromBlock()==null) {
                msg = java.text.MessageFormat.format(rbo.getString("SignalDirection"),
                                                     portal.getName(), 
                                                     portal.getFromBlock().getDisplayName(),
                                                     portal.getToBlock().getDisplayName());
                return msg;
            }
    		OBlock pToBlk = portal.getToBlock();
    		OBlock pFromBlk = portal.getFromBlock();
    		if (pToBlk.equals(toBlock)) {
    			if (fromBlock==null) {
    				sr.setFromBlock(pFromBlk);
/*    			} else if (!fromBlock.equals(pFromBlk)) {
                	msg = java.text.MessageFormat.format(
                            rbo.getString("PortalBlockConflict"), portal.getName(), 
                            fromBlock.getDisplayName());    */				
    			}
    		} else if (pFromBlk.equals(toBlock)) {
    			if (fromBlock==null) {
    				sr.setFromBlock(pToBlk);
/*    			} else if (!toBlock.equals(pToBlk)) {
                	msg = java.text.MessageFormat.format(
                            rbo.getString("PortalBlockConflict"), portal.getName(),
                            toBlock.getDisplayName()); */
    			}       			       			
    		} else if (pToBlk.equals(fromBlock)) {
    			if (toBlock==null) {
    				sr.setToBlock(pFromBlk);
    			}       			       			
    		} else if (pFromBlk.equals(fromBlock)) {
    			if (toBlock==null) {
    				sr.setToBlock(pToBlk);
    			}       			       			
    		} else {
            	msg = java.text.MessageFormat.format(
                        rbo.getString("PortalBlockConflict"), portal.getName(),
                        toBlock.getDisplayName());    			
    		}
        } else if (fromBlock!=null && toBlock!=null) {
        	Portal p = _parent.getPortalModel().getPortal(fromBlock, toBlock);
            if (p==null) {
                msg = java.text.MessageFormat.format(rbo.getString("NoSuchPortal"), 
                                fromBlock.getDisplayName(), toBlock.getDisplayName());
            } else {
            	sr.setPortal(p);
            }
        }
        if ( msg==null && fromBlock != null && fromBlock.equals(toBlock)) {
            msg = java.text.MessageFormat.format(
                    rbo.getString("SametoFromBlock"), fromBlock.getDisplayName());
        }
    	return msg;
    }

    private String checkDuplicateSignal(NamedBean signal) {
        if (signal==null) {
            return null;
        }
        for (int i=0; i<_signalList.size(); i++)  {
            SignalRow srow = _signalList.get(i);
            if (signal.equals(srow.getSignal())) {
                return java.text.MessageFormat.format(rbo.getString("DuplSignalName"), 
                		signal.getDisplayName(), srow.getToBlock().getDisplayName(), 
                		srow.getPortal().getName(), srow.getFromBlock().getDisplayName());
                
            }
        }
        return null;
    }
    private String checkDuplicateSignal(SignalRow row) {
    	NamedBean signal = row.getSignal();
        if (signal==null) {
            return null;
        }
        for (int i=0; i<_signalList.size(); i++)  {
            SignalRow srow = _signalList.get(i);
            if (srow.equals(row)) {
            	continue;
            }
            if (signal.equals(srow.getSignal())) {
                return java.text.MessageFormat.format(rbo.getString("DuplSignalName"), 
                		signal.getDisplayName(), srow.getToBlock().getDisplayName(), 
                		srow.getPortal().getName(), srow.getFromBlock().getDisplayName());
                
            }
        }
        return null;
    }
    
    private String checkDuplicateProtection(SignalRow row) {
    	Portal portal = row.getPortal();
    	OBlock block = row.getToBlock();
        if (block==null || portal==null) {
        	return null;
        }
        for (int i=0; i<_signalList.size(); i++)  {
            SignalRow srow = _signalList.get(i);
            if (srow.equals(row)) {
            	continue;
            }
            if (block.equals(srow.getToBlock()) && portal.equals(srow.getPortal())) {
                return java.text.MessageFormat.format(rbo.getString("DuplProtection"), 
                		block.getDisplayName(), portal.getName(), 
                		srow.getFromBlock().getDisplayName(), srow.getSignal().getDisplayName());              
            }
        }
        return null;
    	
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

    public Object getValueAt(int rowIndex, int columnIndex) {
        //if (log.isDebugEnabled()) log.debug("getValueAt rowIndex= "+rowIndex+" _lastIdx= "+_lastIdx);
        if (_signalList.size() == rowIndex) {
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
        	if (col==DELETE_COL) {
        		initTempRow();
        		fireTableRowsUpdated(row,row);
        		return;
            } else {
            	String str = (String)value;
            	if (str!=null && str.trim().length()>0) {               	
                 	tempRow[col] = str.trim();             		
            	} else {
                 	tempRow[col] = null;             		            		
            	}
            }
        	String name = tempRow[NAME_COLUMN];
        	if (name == null) {
        		return;
        	}
        	OBlock fromBlock = null;
        	OBlock toBlock = null;
        	Portal portal = null;
        	if (tempRow[FROM_BLOCK_COLUMN]!=null) {
                fromBlock = InstanceManager.oBlockManagerInstance().getOBlock(tempRow[FROM_BLOCK_COLUMN]);
                if (fromBlock==null) {
                    msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), tempRow[FROM_BLOCK_COLUMN]);                	
                }        		
        	}
        	if (tempRow[TO_BLOCK_COLUMN]!=null) {
                toBlock = InstanceManager.oBlockManagerInstance().getOBlock(tempRow[TO_BLOCK_COLUMN]);
                if (toBlock==null && msg==null) {
                    msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), tempRow[TO_BLOCK_COLUMN]);                	
                }       		
        	}
        	if (tempRow[PORTAL_COLUMN]!=null) {
            	portal = _parent.getPortalModel().getPortalByName(tempRow[PORTAL_COLUMN]);
            	if (portal==null && msg==null) {
                    msg = java.text.MessageFormat.format(rbo.getString("NoSuchPortalName"), tempRow[PORTAL_COLUMN]);            		
            	}        		
        	}
            NamedBean signal = Portal.getSignal(name);
        	if (msg==null) {
                if (signal==null) {
                    msg = java.text.MessageFormat.format(rbo.getString("NoSuchSignal"), name);            	
                } else {
                	msg = checkDuplicateSignal(signal);            	
                }        		
        	}
            long time = 0;
            try {
                time = Long.parseLong(tempRow[TIME_OFFSET]);
            } catch (NumberFormatException nfe) {
                if (msg==null) {            	
                    msg = rbo.getString("DelayTriggerTime");
                }
            }
            if (msg==null) {
                SignalRow signalRow = new SignalRow(signal, fromBlock, portal, toBlock, time);
                msg =  checkSignalRow(signalRow);       		
                if (msg==null) {
                	portal = signalRow.getPortal();
                	toBlock = signalRow.getToBlock();
                	msg = checkDuplicateProtection(signalRow);
                    if (msg==null && portal!=null) {
                        if (portal.setProtectSignal(signal, time, toBlock)) {
                            if (signalRow.getFromBlock()==null) {
                                signalRow.setFromBlock(portal.getOpposingBlock(toBlock));
                            }
                            _signalList.add(signalRow);
                            initTempRow();
                            fireTableDataChanged();
                        } else {
                        	msg = java.text.MessageFormat.format(rbo.getString("PortalBlockConflict"),
                        					portal.getName(), toBlock.getDisplayName());
                        }
                    }
                }
        	}
        	
        } else {	// Editing existing signal configurations
            SignalRow signalRow =_signalList.get(row);
            switch(col) {
                case NAME_COLUMN:
                    NamedBean signal = Portal.getSignal((String)value); 
                    if (signal==null) {
                        msg = java.text.MessageFormat.format(rbo.getString("NoSuchSignal"), (String)value);
//                        signalRow.setSignal(null);                            		
                        break;
                    }
                    Portal portal = signalRow.getPortal();
                    if (portal!=null && signalRow.getToBlock()!=null) {
                    	NamedBean oldSignal = signalRow.getSignal();
                        signalRow.setSignal(signal);                            		
                    	msg = checkDuplicateSignal(signalRow);
                    	if (msg==null) {
                            deleteSignal(signalRow);    // delete old
                            msg = setSignal(signalRow, false);
                            fireTableRowsUpdated(row,row);                            	
                    	} else {
                            signalRow.setSignal(oldSignal);                            		
                    		
                    	}
                    }
                    break;
                case FROM_BLOCK_COLUMN:
                    OBlock block = InstanceManager.oBlockManagerInstance().getOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), (String)value);
//                        signalRow.setFromBlock(null);                    	
                        break;
                    }
                    if (block.equals(signalRow.getFromBlock())) {
                        break;      // no change
                    }
                    deleteSignal(signalRow);    // delete old
//                    OBlock oldBlock = signalRow.getFromBlock();
                    signalRow.setFromBlock(block);                    	
                    portal = signalRow.getPortal();
                    if (checkPortalBlock(portal, block)) {
                        signalRow.setToBlock(null);                    	
                    } else {
                        // get new portal
                        portal = _parent.getPortalModel().getPortal(block, signalRow.getToBlock());
                        signalRow.setPortal(portal);                        	
                    }
                    msg = checkSignalRow(signalRow);
                    if (msg==null) {
                    	msg = checkDuplicateProtection(signalRow);                    	
                    } else {
                        signalRow.setPortal(null);
                        break;
                    }
                    if (msg==null && signalRow.getPortal()!=null) {
                        msg = setSignal(signalRow, true);
                    } else {
                        signalRow.setPortal(null);                    	
                    }
                    fireTableRowsUpdated(row,row);
                    break;
                case PORTAL_COLUMN:
                    portal = _parent.getPortalModel().getPortalByName((String)value);
                    if (portal==null) {
                        msg = java.text.MessageFormat.format(rbo.getString("NoSuchPortalName"), (String)value);
//                        signalRow.setPortal(null);
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
                    msg =  checkSignalRow(signalRow);       		
                    if (msg==null) {
                    	msg = checkDuplicateProtection(signalRow);
                    } else {
                        signalRow.setToBlock(null);
                        break;
                    }
                    if (msg==null) {
                        signalRow.setPortal(portal);
                        msg = setSignal(signalRow, false);
                        fireTableRowsUpdated(row,row);                         	
                    }
                    break;
                case TO_BLOCK_COLUMN:
                    block = InstanceManager.oBlockManagerInstance().getOBlock((String)value);
                    if (block==null) {
                        msg = java.text.MessageFormat.format(
                            rbo.getString("NoSuchBlock"), (String)value);
//                        signalRow.setToBlock(null);
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
                        portal = _parent.getPortalModel().getPortal(signalRow.getFromBlock(), block);
                        signalRow.setPortal(portal);
                    }
                    msg = checkSignalRow(signalRow);                    
                    if (msg==null) {
                    	msg = checkDuplicateProtection(signalRow);                    	
                    } else {
                        signalRow.setPortal(null);
                        break;
                    }
                    if (msg==null && signalRow.getPortal()!=null) {
                        msg = setSignal(signalRow, true);
                    } else {
                        signalRow.setPortal(null);                    	
                    }
                    fireTableRowsUpdated(row,row);
                    break;
                case TIME_OFFSET:
                    long time = 0;
                    try {
                        time = Long.parseLong((String)value);
                    } catch (NumberFormatException nfe) {
                        msg = rbo.getString("DelayTriggerTime");
                        signalRow.setDelayTime(0);
                        break;
                    }
                    signalRow.setDelayTime(time);
                    msg = setSignal(signalRow, false);
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

    private Portal getPortal(Portal p, OBlock fromBlock, OBlock toBlock) {
        if (p!= null) {
            return p;
        } else {
            return _parent.getPortalModel().getPortal(fromBlock, toBlock);
        }
    }

//    private int getSignalIndex(String name) {
//        for (int i=0; i<_signalList.size(); i++)  {
//            if (_signalList.get(i).getSignal().getDisplayName().equals(name)) { 
//                return i;
//            }
//        }
//        return -1;
//    }

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
    
    private String setSignal(SignalRow signalRow, boolean deletePortal) {
    	Portal portal = signalRow.getPortal();
        if (portal.setProtectSignal(signalRow.getSignal(), signalRow.getDelayTime(), signalRow.getToBlock())) {
            if (signalRow.getFromBlock()==null) {
                signalRow.setFromBlock(portal.getOpposingBlock(signalRow.getToBlock()));
            }
        } else {
        	if (deletePortal) {
                signalRow.setPortal(null);                    	       		
        	} else {
                signalRow.setToBlock(null);                    	        		
        	}
        	return java.text.MessageFormat.format(
                    rbo.getString("PortalBlockConflict"), portal.getName(), 
                    signalRow.getToBlock().getDisplayName()); 
        }
    	return null;
    }

/*    private boolean checkPortal (SignalRow signalRow) {
        // check that blocks belong to portal
    	boolean ret = true;
        Portal portal = signalRow.getPortal();
        if (portal==null) {
            return false;
        }
        if (!checkPortalBlock(portal, signalRow.getFromBlock())) {
            signalRow.setFromBlock(null);
            ret = false;
        }
        if (!checkPortalBlock(portal, signalRow.getToBlock())) {
            signalRow.setToBlock(null);
            ret = false;
        }
        return ret;
    }*/
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
            case NAME_COLUMN:       return new JTextField(18).getPreferredSize().width;
            case FROM_BLOCK_COLUMN: return new JTextField(18).getPreferredSize().width;
            case PORTAL_COLUMN:     return new JTextField(18).getPreferredSize().width;
            case TO_BLOCK_COLUMN:   return new JTextField(18).getPreferredSize().width;
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
