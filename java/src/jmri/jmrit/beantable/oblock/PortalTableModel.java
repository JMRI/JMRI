package jmri.jmrit.beantable.oblock;

import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import jmri.InstanceManager;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.logix.*;
import jmri.util.gui.GuiLafPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlock Portals.
* <p>
* Can be used with two interfaces:
* <ul>
*     <li>original "desktop" InternalFrames (parent class TableFrames, an extended JmriJFrame)
*     <li>JMRI standard Tabbed tables (parent class JPanel)
* </ul>
* The _tabbed field decides, it is set in prefs (restart required).
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
public class PortalTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int FROM_BLOCK_COLUMN = 0;
    public final int NAME_COLUMN = 1; // not static to fetch from _tabbed OBlockTablePanel
    public static final int TO_BLOCK_COLUMN = 2;
    static public final int DELETE_COL = 3;
    static public final int EDIT_COL = 4;
    public static final int NUMCOLS = 4;
    // reports + 1 for EDIT column if _tabbed

    PortalManager _manager;
    private final String[] tempRow = new String[NUMCOLS];
    private final boolean _tabbed; // set from prefs (restart required)
    TableFrames _parent;

    public PortalTableModel(@Nonnull TableFrames parent) {
        super();
        _parent = parent;
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        _manager = InstanceManager.getDefault(PortalManager.class);
        _manager.addPropertyChangeListener(this);
        if (!_tabbed) {
            // specific stuff for _desktop
            initTempRow();
        }
    }

    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS + (_tabbed ? 1 : 0); // add Edit column on _tabbed
    }

    @Override
    public int getRowCount() {
        return _manager.getPortalCount() + (_tabbed ? 0 : 1); // + 1 row in _desktop to create entry row
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case FROM_BLOCK_COLUMN:
                return Bundle.getMessage("FromBlockName");
            case NAME_COLUMN:
                return Bundle.getMessage("PortalName");
            case TO_BLOCK_COLUMN:
                return Bundle.getMessage("OppBlockName");
            case EDIT_COL:
                return "  ";
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public Object getValueAt(int row, int col) {
        log.debug("getValueAt row= {} col= {}", row, col);
        if (row == _manager.getPortalCount()) { // this must be tempRow
            return tempRow[col];
        }
        Portal portal = _manager.getPortal(row);
        if (portal == null) {
            if (col == DELETE_COL) {
                return Bundle.getMessage("ButtonClear");
            }
            return tempRow[col];
        } else {
            switch (col) {
                case FROM_BLOCK_COLUMN:
                    return portal.getFromBlockName();
                case NAME_COLUMN:
                    return portal.getName();
                case TO_BLOCK_COLUMN:
                    return portal.getToBlockName();
                case DELETE_COL:
                    return Bundle.getMessage("ButtonDelete");
                case EDIT_COL:
                    return Bundle.getMessage("ButtonEdit");
                default:
                    // fall through
                    break;
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
//        log.debug("setValueAt value= {}, row= {} col= {}", row, col);
        String msg = null;
        if (row == _manager.getPortalCount()) { // set tempRow, only used on _desktop
            if (col == DELETE_COL) {
                initTempRow();
                fireTableRowsUpdated(row, row);
                return;
            } else {
                String str = (String) value;
                if (str == null || str.trim().length() == 0) {
                    tempRow[col] = null;
                    return;
              } else {
                    tempRow[col] = str.trim();
                }
            }
            OBlockManager OBlockMgr = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
            OBlock fromBlock = null;
            OBlock toBlock = null;
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
            if (msg == null && tempRow[NAME_COLUMN] != null) {
                if (fromBlock != null && toBlock != null ) {
                    if (fromBlock.equals(toBlock)) { 
                        msg = Bundle.getMessage("SametoFromBlock", fromBlock.getDisplayName());
                    } else {
                        Portal portal = _manager.createNewPortal(tempRow[NAME_COLUMN]);
                        if (portal != null) {
                            portal.setToBlock(toBlock, false);
                            portal.setFromBlock(fromBlock, false);
                            initTempRow();
                            fireTableDataChanged();
                        } else {
                            msg = Bundle.getMessage("DuplPortalName", value);
                        }
                    }
                } else if ((fromBlock == null) ^ (toBlock == null)) {
                    msg = Bundle.getMessage("PortalNeedsBlock", tempRow[NAME_COLUMN]);                   
                }
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        Portal portal = _manager.getPortal(row);
        if (portal == null) {
            log.error("Portal null, getValueAt row= {}, col= {}, portalListSize= {}", row, col, _manager.getPortalCount());
            return;
        }

        switch (col) { // existing Portals in table
            case FROM_BLOCK_COLUMN:
                OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock((String) value);
                if (block == null) {
                    msg = Bundle.getMessage("NoSuchBlock", value);
                    break;
                }
                if (block.equals(portal.getToBlock())) {
                    msg = Bundle.getMessage("SametoFromBlock", block.getDisplayName());
                    break;
                }
                if (!portal.setFromBlock(block, false)) {
                    int val = _parent.verifyWarning(Bundle.getMessage("BlockPathsConflict", value, portal.getFromBlockName()));
                    if (val == 2) {
                        break;
                    }
                }
                portal.setFromBlock(block, true);
                fireTableRowsUpdated(row, row);
                break;
            case NAME_COLUMN:
                msg = portal.setName((String)value);
                if (msg == null ) {
                    fireTableRowsUpdated(row, row);
                }
                break;
            case TO_BLOCK_COLUMN:
                block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock((String) value);
                if (block == null) {
                    msg = Bundle.getMessage("NoSuchBlock", value);
                    break;
                }
                if (block.equals(portal.getFromBlock())) {
                    msg = Bundle.getMessage("SametoFromBlock", block.getDisplayName());
                    break;
                }
                if (!portal.setToBlock(block, false)) {
                    int val = _parent.verifyWarning(Bundle.getMessage("BlockPathsConflict", value, portal.getToBlockName()));
                    if (val == 2) {
                        break;
                    }
                }
                portal.setToBlock(block, true);
                fireTableRowsUpdated(row, row);
                break;
            case DELETE_COL:
                if (deletePortal(portal)) {
                    fireTableDataChanged();
                }
                break;
            case EDIT_COL:
                editPortal(portal);
                break;
            default:
                log.warn("Unhandled column: {}", col);
                break;
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean deletePortal(Portal portal) {
        int val = _parent.verifyWarning(Bundle.getMessage("DeletePortalConfirm", portal.getName()));
        if (val != 2) {
            return portal.dispose();
        }
        return false;
    }

    private void editPortal(Portal portal) {
        if (_tabbed) {
            // open PortalEditFrame
            PortalEditFrame portalFrame = new PortalEditFrame(Bundle.getMessage("TitleEditPortal", portal.getName()), portal, this);
            portalFrame.setVisible(true);
        }
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
            default:
                return String.class;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case FROM_BLOCK_COLUMN:
            case NAME_COLUMN:
            case TO_BLOCK_COLUMN:
                return new JTextField(15).getPreferredSize().width;
            case DELETE_COL:
            case EDIT_COL:
                return new JButton("DELETE").getPreferredSize().width;
            default:
                // fall through
                break;
        }
        return 5;
    }

    // for Print
    protected String getBeanType() {
        return "Portal";
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (log.isDebugEnabled()) {
            log.debug("PropertyChangeEvent property = {} source= {}", property, e.getSource().getClass().getName());
        }
        switch (property) {
            case "pathCount":
            case "numPortals":
                initTempRow();
                fireTableDataChanged();
                break;
            case "NameChange":
                int row = _manager.getIndexOf((Portal) e.getNewValue());
                fireTableRowsUpdated(row, row);
                break;
            case "signals":
                _parent.getSignalTableModel().propertyChange(e);
                break;
            default:
        }
    }

    protected int verifyWarning(String message) {
        return (_parent.verifyWarning(message));
    }

    private final static Logger log = LoggerFactory.getLogger(PortalTableModel.class);

}
