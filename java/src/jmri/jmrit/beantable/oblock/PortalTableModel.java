package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Pete Cressman (C) 2010
 * @version $Revision$
 */
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalTableModel extends jmri.jmrit.beantable.BeanTableDataModel {

    private static final long serialVersionUID = -4467086483594717590L;
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
    }

    public void init() {
        initTempRow();
    }

    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
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
    public NamedBean getByUserName(String name) {
        return _manager.getByUserName(name);
    }

    @Override
    protected String getBeanType() {
        return "Portal";
    }

    @Override
    public String getValue(String name) {
        return name;
    }

    @Override
    public void clickOn(NamedBean t) {
    }

    @Override
    protected String getMasterClassName() {
        return PortalTableModel.class.getName();
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS;
    }

    @Override
    public int getRowCount() {
        return super.getRowCount() + 1;
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
        }
        return "";
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (getRowCount() == row) {
            return tempRow[col];
        }
        if (row > sysNameList.size()) {
            return "";
        }
        Portal portal = null;
        if (row < sysNameList.size()) {
            String name = sysNameList.get(row);
            portal = _manager.getBySystemName(name);
        }
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
            }
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (super.getRowCount() == row) {
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
                if (fromBlock == null || toBlock==null ) {
                    msg = Bundle.getMessage("PortalNeedsBlock", tempRow[NAME_COLUMN]);                   
                } else if (fromBlock.equals(toBlock)){
                    msg = Bundle.getMessage("SametoFromBlock", fromBlock.getDisplayName());
                }
                if (msg==null) {
                    Portal portal = _manager.createNewPortal(null, tempRow[NAME_COLUMN]);
                    if (portal != null) {
                        portal.setToBlock(toBlock, false);
                        portal.setFromBlock(fromBlock, false);
                        initTempRow();
                        fireTableDataChanged();
                    } else {
                        msg = Bundle.getMessage("DuplPortalName", (String) value);
                    }
                }
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        String name = sysNameList.get(row);
        Portal portal = _manager.getBySystemName(name);
        if (portal == null) {
            log.error("Portal null, getValueAt row= " + row + ", col= " + col + ", "
                    + "portalListSize= " + _manager.getSystemNameArray().length);
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
                    int response = JOptionPane.showConfirmDialog(null,
                            Bundle.getMessage("BlockPathsConflict", value, portal.getFromBlockName()),
                            Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.NO_OPTION) {
                        break;
                    }

                }
                portal.setFromBlock(block, true);
                fireTableRowsUpdated(row, row);
                break;
            case NAME_COLUMN:
                if (_manager.getPortal((String) value) != null) {
                    msg = Bundle.getMessage("DuplPortalName", (String) value);
                    break;
                }
                if (_manager.getPortal((String) value) != null) {
                    msg = Bundle.getMessage("PortalNameConflict", (String) value);
                } else {
                    portal.setName((String) value);
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
                    int response = JOptionPane.showConfirmDialog(null,
                            Bundle.getMessage("BlockPathsConflict", value, portal.getToBlockName()),
                            Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.NO_OPTION) {
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
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private static boolean deletePortal(Portal portal) {
        if (JOptionPane.showConfirmDialog(null,
                Bundle.getMessage("DeletePortalConfirm",
                        portal.getName()), Bundle.getMessage("WarningTitle"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION) {
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

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case FROM_BLOCK_COLUMN:
            case TO_BLOCK_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            case NAME_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    private final static Logger log = LoggerFactory.getLogger(PortalTableModel.class.getName());
}
