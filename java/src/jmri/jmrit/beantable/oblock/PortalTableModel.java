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
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;

public class PortalTableModel extends AbstractTableModel {

    public static final int FROM_BLOCK_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static final int TO_BLOCK_COLUMN = 2;
    static public final int DELETE_COL = 3;
    public static final int NUMCOLS = 4;

    static final ResourceBundle rbo = ResourceBundle.getBundle("jmri.jmrit.beantable.OBlockTableBundle");
    
    private ArrayList <Portal> _portalList = new ArrayList <Portal>();
    private String[] tempRow= new String[NUMCOLS];

    TableFrames _parent;

    public PortalTableModel(TableFrames parent) {
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
        tempRow[DELETE_COL] = rbo.getString("ButtonClear");
    }
    private void makeList() {
         ArrayList <Portal> tempList = new ArrayList <Portal>();
         // save portals that do not have all their blocks yet
         for (int i=0; i<_portalList.size(); i++) {
             Portal portal = _portalList.get(i);
             if (portal.getToBlock()==null && portal.getFromBlock()==null) {
                 tempList.add(portal);
             }
         }
        // find portals with Blocks assigned
        Iterator<NamedBean> bIter = _parent.getBlockModel().getBeanList().iterator();
        while (bIter.hasNext()) {
            OBlock block = (OBlock)bIter.next();
            List <Portal> list = block.getPortals();
            for (int i=0; i<list.size(); i++) {
                Portal portal = list.get(i);
                String pName = portal.getName();
                boolean skip = false;
                for (int j=0; j<tempList.size(); j++) {
                    if (pName.equals(tempList.get(j).getName())) {
                        skip = true;
                        break;
                    }
                }
                if (skip)  { continue;  }
                // not in list, for the sort, insert at correct position
                boolean add = true;
                for (int j=0; j<tempList.size(); j++) {
                    if (pName.compareTo(tempList.get(j).getName()) < 0) {
                        tempList.add(j, portal);
                        add = false;
                        break;
                    }
                }
                if (add) {
                    tempList.add(portal);
                }
            }
        }
        _portalList = tempList;
        if (log.isDebugEnabled()) log.debug("makeList exit: _portalList has "
                                            +_portalList.size()+" rows.");
    }

    protected List <Portal> getPortalList() {
        return _portalList;
    }

    public int getColumnCount () {
        return NUMCOLS;
    }

    public int getRowCount() {
        return _portalList.size() + 1;
    }

    public String getColumnName(int col) {
        switch (col) {
            case FROM_BLOCK_COLUMN: return rbo.getString("BlockName");
            case NAME_COLUMN: return rbo.getString("PortalName");
            case TO_BLOCK_COLUMN: return rbo.getString("BlockName");
        }
        return "";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (_portalList.size() == rowIndex) {
            return tempRow[columnIndex];
        }
        switch(columnIndex) {
            case FROM_BLOCK_COLUMN:
                return _portalList.get(rowIndex).getFromBlockName();
            case NAME_COLUMN:
                return _portalList.get(rowIndex).getName();
            case TO_BLOCK_COLUMN:
                return _portalList.get(rowIndex).getToBlockName();
            case DELETE_COL:
                return rbo.getString("ButtonDelete");
        }
        return "";
    }

    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (_portalList.size() == row) {
        	 if (col==DELETE_COL) {
        		 initTempRow();
                 fireTableRowsUpdated(row,row);
                 return;
             } else { 
             	tempRow[col] = (String)value; 
             }
        	 String name = tempRow[NAME_COLUMN];
        	 if (name != null) {
        		 name = name.trim();
        		 if (name.length()==0) {
        			 return;
        		 }
        	 } else {
        		 return;
        	 }
             // Note: Portal ctor will add this Portal to each of its 'from' & 'to' Blocks.
             OBlock fromBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class)
                                         .getOBlock(tempRow[FROM_BLOCK_COLUMN]);
             OBlock toBlock = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class)
                                         .getOBlock(tempRow[TO_BLOCK_COLUMN]);
             if (fromBlock==null || toBlock==null) {
            	 if (fromBlock==null && tempRow[FROM_BLOCK_COLUMN]!=null) {
                     msg = java.text.MessageFormat.format(
                             rbo.getString("NoSuchBlock"), tempRow[FROM_BLOCK_COLUMN]);            		 
            	 } else if (toBlock==null && tempRow[TO_BLOCK_COLUMN]!=null) {
                     msg = java.text.MessageFormat.format(
                             rbo.getString("NoSuchBlock"), tempRow[TO_BLOCK_COLUMN]);            		 
            	 } else {
                     msg = java.text.MessageFormat.format(rbo.getString("PortalNeedsBlock"), name);            		 
            	 }
             } else if (fromBlock.equals(toBlock)) {
                 msg = java.text.MessageFormat.format(
                         rbo.getString("SametoFromBlock"), fromBlock.getDisplayName());
             } else if (getPortalByName(name)==null) {            	 
                 Portal portal = new Portal(fromBlock, name, toBlock);
                 _portalList.add(portal);
                 makeList();
                 initTempRow();
                 fireTableDataChanged();
             } else {
               	 msg = java.text.MessageFormat.format(
                         rbo.getString("DuplPortalName"), (String)value);
             }
             if (msg!=null) {
                 JOptionPane.showMessageDialog(null, msg,
                         rbo.getString("WarningTitle"),  JOptionPane.WARNING_MESSAGE);
             }       	 
             return;
        }

        Portal portal =_portalList.get(row);
 
        switch(col) {
            case FROM_BLOCK_COLUMN:
                OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock((String)value);
                if (block==null) {
                    msg = java.text.MessageFormat.format(
                        rbo.getString("NoSuchBlock"), (String)value);
                    break;
                }
                if (block.equals(portal.getToBlock())){
                    msg = java.text.MessageFormat.format(
                            rbo.getString("SametoFromBlock"), block.getDisplayName());
                    break;
                }
                if ( !portal.setFromBlock(block, false)) {
                    int response = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                        rbo.getString("BlockPathsConflict"), value, portal.getFromBlockName()),
                        rbo.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE);
                    if (response==JOptionPane.NO_OPTION) {
                        break;
                    }

                }
                portal.setFromBlock(block, true);
                fireTableRowsUpdated(row,row);
                break;
            case NAME_COLUMN:
                if (getPortalByName((String)value)!=null) {
                    msg = java.text.MessageFormat.format(
                        rbo.getString("DuplPortalName"), (String)value);
                    break;
                }
                if ( listContains((String)value) ) {
                    msg = java.text.MessageFormat.format(
                        rbo.getString("PortalNameConflict"), (String)value);
                } else {
                    portal.setName((String)value);
                    fireTableRowsUpdated(row,row);
                }
                break;
            case TO_BLOCK_COLUMN:
                block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock((String)value);
                if (block==null) {
                    msg = java.text.MessageFormat.format(
                        rbo.getString("NoSuchBlock"), (String)value);
                    break;
                }
                if (block.equals(portal.getFromBlock())){
                    msg = java.text.MessageFormat.format(
                            rbo.getString("SametoFromBlock"), block.getDisplayName());
                    break;
                }
                if ( !portal.setToBlock(block, false)) {
                    int response = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                        rbo.getString("BlockPathsConflict"), value, portal.getToBlockName()),
                        rbo.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, 
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
                    rbo.getString("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean listContains(String name) {
        for (int i=0; i<_portalList.size(); i++)  {
            if (_portalList.get(i).getName().equals(name)) { return true; }
        }
        return false;
    }

    private boolean deletePortal(Portal portal) {
        if (JOptionPane.showConfirmDialog(null, 
                        java.text.MessageFormat.format(rbo.getString("DeletePortalConfirm"),
                        portal.getName()), rbo.getString("WarningTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    ==  JOptionPane.YES_OPTION) {
            //_portalList.remove(portal);
            String name = portal.getName();
            for (int i = 0; i < _portalList.size(); i++) {
                if (name.equals(_portalList.get(i).getName())) {
                    _portalList.remove(i);
                    i--;
                }
            }
            OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
            String[] sysNames = manager.getSystemNameArray();
            for (int i = 0; i < sysNames.length; i++) {
                manager.getBySystemName(sysNames[i]).removePortal(portal);
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

    protected Portal getPortal(OBlock fromBlock, OBlock toBlock) {
        for (int i=0; i<_portalList.size(); i++) {
            Portal portal = _portalList.get(i);
            if ((portal.getFromBlock().equals(fromBlock) || portal.getToBlock().equals(fromBlock)) 
                && (portal.getFromBlock().equals(toBlock) || portal.getToBlock().equals(toBlock))) {
                return portal; 
            }
        }
        return null;
    }

    protected Portal getPortalByName(String name) {
        for (int i=0; i<_portalList.size(); i++) {
            if (_portalList.get(i).getName().equals(name) ) {
                return _portalList.get(i);
            }
        }
        return null;
    }

/*    private int getPortalIndex(String name) {
        for (int i=0; i<_portalList.size(); i++) {
            if (_portalList.get(i).getName().equals(name) ) {
                return i;
            }
        }
        return -1;
    }*/

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (log.isDebugEnabled()) log.debug("PropertyChange = "+property);
        if (property.equals("length") || property.equals("portalCount")
                            || property.equals("UserName")) {
            makeList();
            fireTableDataChanged();
        }
        _parent.getSignalModel().propertyChange(e);
    }

    static Logger log = LoggerFactory.getLogger(PortalTableModel.class.getName());
}
