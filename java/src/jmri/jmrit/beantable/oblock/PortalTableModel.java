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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;

public class PortalTableModel extends jmri.jmrit.picker.PickListModel {

    public static final int FROM_BLOCK_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static final int TO_BLOCK_COLUMN = 2;
    static public final int DELETE_COL = 3;
    public static final int NUMCOLS = 4;

    PortalManager _manager;
    private String[] tempRow= new String[NUMCOLS];

    TableFrames _parent;

    public PortalTableModel(TableFrames parent) {
        super();
        _parent = parent;
    }

    public void init() {
        initTempRow();
    }

    void initTempRow() {
        for (int i=0; i<NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }
    
    @Override
    public Manager getManager() {
        _manager = InstanceManager.getDefault(PortalManager.class);
        return _manager;
    }
    @Override
    public NamedBean getBySystemName(String name) {
        return _manager.getBySystemName(name);
    }

    @Override
    public NamedBean addBean(String name) {
        return _manager.providePortal(name);
    }
    @Override
    public NamedBean addBean(String sysName, String userName) {
        return _manager.createNewPortal(sysName, userName);
    }
    @Override
    public boolean canAddBean() {
        return true;
    }

    @Override
    public int getColumnCount () {
        return NUMCOLS;
    }
    @Override
    public int getRowCount () {
        return super.getRowCount() + 1;
    }
    public String getColumnName(int col) {
        switch (col) {
            case FROM_BLOCK_COLUMN: return Bundle.getMessage("BlockName");
            case NAME_COLUMN: return Bundle.getMessage("PortalName");
            case TO_BLOCK_COLUMN: return Bundle.getMessage("BlockName");
        }
        return "";
    }

    public Object getValueAt(int row, int col) {
        if (getRowCount() == row) {
            return tempRow[col];
        }
        Portal portal = (Portal)getBeanAt(row);
    	if (portal==null) {
            if (col==DELETE_COL) {
            	return Bundle.getMessage("ButtonClear");
            }
    		return tempRow[col];
    	} else {
            switch(col) {
            	case FROM_BLOCK_COLUMN:
            		return portal.getFromBlockName();
            	case NAME_COLUMN:
            		return portal.getName();
            	case TO_BLOCK_COLUMN:
            		return portal.getToBlockName();
            	case DELETE_COL:
            		return Bundle.getMessage("ButtonDelete");
            }    		
    	}
        return "";
    }

    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (super.getRowCount() == row) {
        	 if (col==DELETE_COL) {
        		 initTempRow();
                 fireTableRowsUpdated(row,row);
                 return;
             } else { 
             	tempRow[col] = (String)value; 
             }
        	 String name = tempRow[NAME_COLUMN];
        	 if (name == null || name.trim().length()==0) {
        		 return;
        	 }
        	 OBlockManager OBlockMgr = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
             OBlock fromBlock = OBlockMgr.getOBlock(tempRow[FROM_BLOCK_COLUMN]);
             OBlock toBlock = OBlockMgr.getOBlock(tempRow[TO_BLOCK_COLUMN]);
             if (fromBlock==null || toBlock==null) {
            	 if (fromBlock==null && tempRow[FROM_BLOCK_COLUMN]!=null) {
                     msg = Bundle.getMessage("NoSuchBlock", tempRow[FROM_BLOCK_COLUMN]);            		 
            	 } else if (toBlock==null && tempRow[TO_BLOCK_COLUMN]!=null) {
                     msg = Bundle.getMessage("NoSuchBlock", tempRow[TO_BLOCK_COLUMN]);            		 
            	 } else {
                     msg = Bundle.getMessage("PortalNeedsBlock", name);            		 
            	 }
             } else if (fromBlock.equals(toBlock)) {
                 msg = Bundle.getMessage("SametoFromBlock", fromBlock.getDisplayName());
             } else {            	 
                 Portal portal = _manager.createNewPortal(null, name);
                 if (portal!=null) {
                     portal.setToBlock(toBlock, false);
                     portal.setFromBlock(fromBlock, false);
                     initTempRow();
                     fireTableDataChanged();                	 
                 } else {
                	 msg = Bundle.getMessage("DuplPortalName", (String)value);
                 }
             }
             if (msg!=null) {
                 JOptionPane.showMessageDialog(null, msg,
                         Bundle.getMessage("WarningTitle"),  JOptionPane.WARNING_MESSAGE);
             }       	 
             return;
        }

        Portal portal = (Portal)getBeanAt(row);
        if (log.isDebugEnabled() && portal==null) {
        	log.debug("getValueAt row= "+row+", col= "+col+", " +
        			"portalListSize= "+_manager.getSystemNameArray().length);
        }

        switch(col) {
            case FROM_BLOCK_COLUMN:
                OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock((String)value);
                if (block==null) {
                    msg = Bundle.getMessage("NoSuchBlock", (String)value);
                    break;
                }
                if (block.equals(portal.getToBlock())){
                    msg = Bundle.getMessage("SametoFromBlock", block.getDisplayName());
                    break;
                }
                if ( !portal.setFromBlock(block, false)) {
                    int response = JOptionPane.showConfirmDialog(null, 
                    		Bundle.getMessage("BlockPathsConflict", value, portal.getFromBlockName()),
                    		Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                    		JOptionPane.WARNING_MESSAGE);
                    if (response==JOptionPane.NO_OPTION) {
                        break;
                    }

                }
                portal.setFromBlock(block, true);
                fireTableRowsUpdated(row,row);
                break;
            case NAME_COLUMN:
                if (_manager.getPortal((String)value)!=null) {
                    msg = Bundle.getMessage("DuplPortalName", (String)value);
                    break;
                }
                if ( _manager.getPortal((String)value)!=null ) {
                    msg = Bundle.getMessage("PortalNameConflict", (String)value);
                } else {
                    portal.setName((String)value);
                    fireTableRowsUpdated(row,row);
                }
                break;
            case TO_BLOCK_COLUMN:
                block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock((String)value);
                if (block==null) {
                    msg = Bundle.getMessage("NoSuchBlock", (String)value);
                    break;
                }
                if (block.equals(portal.getFromBlock())){
                    msg = Bundle.getMessage("SametoFromBlock", block.getDisplayName());
                    break;
                }
                if ( !portal.setToBlock(block, false)) {
                    int response = JOptionPane.showConfirmDialog(null, 
                    		Bundle.getMessage("BlockPathsConflict", value, portal.getToBlockName()),
                    		Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                    		JOptionPane.WARNING_MESSAGE);
                    if (response==JOptionPane.NO_OPTION) {
                        break;
                    }

                }
                portal.setToBlock(block, true);
                fireTableRowsUpdated(row,row);
                break;
            case DELETE_COL:
                if (deletePortal(portal)) {
                    fireTableDataChanged();
                }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean deletePortal(Portal portal) {
        if (JOptionPane.showConfirmDialog(null, 
                        Bundle.getMessage("DeletePortalConfirm",
                        portal.getName()), Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    ==  JOptionPane.YES_OPTION) {
            OBlockManager OBlockMgr = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
            String[] sysNames = OBlockMgr.getSystemNameArray();
            for (int i = 0; i < sysNames.length; i++) {
            	OBlockMgr.getBySystemName(sysNames[i]).removePortal(portal);
            }
            portal.dispose();
            return true;
        }
        return false;
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
            case FROM_BLOCK_COLUMN:
            case TO_BLOCK_COLUMN: return new JTextField(20).getPreferredSize().width;
            case NAME_COLUMN: return new JTextField(20).getPreferredSize().width;
            case DELETE_COL: return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    static Logger log = LoggerFactory.getLogger(PortalTableModel.class.getName());
}
