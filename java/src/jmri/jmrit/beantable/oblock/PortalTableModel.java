package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.InstanceManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks
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
public class PortalTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int FROM_BLOCK_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static final int TO_BLOCK_COLUMN = 2;
    static public final int DELETE_COL = 3;
    public static final int NUMCOLS = 4;

    PortalManager _manager;
    private String[] tempRow = new String[NUMCOLS];

    TableFrames _parent;

    public PortalTableModel(TableFrames parent) {
        super();
        _parent = parent;
        _manager = InstanceManager.getDefault(PortalManager.class);
        _manager.addPropertyChangeListener(this);
        initTempRow();
    }

    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS;
    }

    @Override
    public int getRowCount() {
        return _manager.getPortalCount() + 1;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case FROM_BLOCK_COLUMN:
                return Bundle.getMessage("BlockName");
            case NAME_COLUMN:
                return Bundle.getMessage("PortalName");
            case TO_BLOCK_COLUMN:
                return Bundle.getMessage("BlockName");
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public Object getValueAt(int row, int col) {
//        log.debug("getValueAt row= {} col= {}", row, col);
        if (row == _manager.getPortalCount()) {
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
                default:
                    // fall through
                    break;
            }
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
//        log.debug("setValueAt value= {}, row= {} col= {}", row, col);
        String msg = null;
        if (row == _manager.getPortalCount()) {
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
            if (msg==null && tempRow[TO_BLOCK_COLUMN] != null) {
                toBlock = OBlockMgr.getOBlock(tempRow[TO_BLOCK_COLUMN]);
                if (toBlock==null) {
                    msg = Bundle.getMessage("NoSuchBlock", tempRow[TO_BLOCK_COLUMN]);
                }
            }
            if (msg==null && tempRow[NAME_COLUMN] != null) {
                if (fromBlock != null && toBlock!=null ) {
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
                            msg = Bundle.getMessage("DuplPortalName", (String) value);
                        }
                    }
                } else if (fromBlock == null ^ toBlock==null ) {
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
            log.error("Portal null, getValueAt row= " + row + ", col= " + col + ", "
                    + "portalListSize= " + _manager.getPortalCount());
            return;
        }

        switch (col) {
            case FROM_BLOCK_COLUMN:
                OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock((String) value);
                if (block == null) {
                    msg = Bundle.getMessage("NoSuchBlock", (String) value);
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
                    msg = Bundle.getMessage("NoSuchBlock", (String) value);
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

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == DELETE_COL) {
            return JButton.class;
        }
        return String.class;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
    public int getPreferredWidth(int col) {
        switch (col) {
            case FROM_BLOCK_COLUMN:
            case TO_BLOCK_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            case NAME_COLUMN:
                return new JTextField(20).getPreferredSize().width;
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
        if (log.isDebugEnabled()) {
            log.debug("property = {} source= {}", property, e.getSource().getClass().getName());
        }
        if (property.equals("pathCount") || property.equals("numPortals")) {
            initTempRow();
            fireTableDataChanged();
        } else if (property.equals("NameChange")) {
            int row = _manager.getIndexOf((Portal)e.getNewValue());
            fireTableRowsUpdated(row, row);   
        } else if (property.equals("signals")) {
            _parent.getSignalModel().propertyChange(e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PortalTableModel.class);
}
