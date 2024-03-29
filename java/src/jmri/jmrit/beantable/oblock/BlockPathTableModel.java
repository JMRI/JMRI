package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import jmri.InstanceManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.util.IntlUtilities;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * GUI to define the OPaths within an OBlock. An OPath is the setting of turnouts
 * from one Portal to another Portal within an OBlock.  It may also be assigned
 * a length.
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

public class BlockPathTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int FROM_PORTAL_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static final int TO_PORTAL_COLUMN = 2;
    static public final int LENGTHCOL = 3;
    static public final int UNITSCOL = 4;
    public static final int EDIT_COL = 5;
    public static final int DELETE_COL = 6;
    public static final int NUMCOLS = 7;

    private final String[] tempRow = new String[NUMCOLS];

    private OBlock _block;
    private TableFrames _parent;
    private final boolean _tabbed;     // read from prefs (restart required)
    private ArrayList<Boolean> _units; // list to toggle units of length col for each path
    
    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    public BlockPathTableModel() {
        super();
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
    }

    public BlockPathTableModel(@Nonnull OBlock block, @Nonnull TableFrames parent) {
        super();
        _block = block;
        _parent = parent;
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        initTempRow();
        _block.addPropertyChangeListener(this);
    }

    public void removeListener() {
        if (_block == null) {
            return;
        }
        try {
            _block.removePropertyChangeListener(this);
        } catch (NullPointerException npe) {
            // OK when block is removed
        }
    }

    protected OBlock getBlock() {
        return _block;
    }

    void initTempRow() {
        if (!_tabbed) {
            for (int i = 0; i < NUMCOLS; i++) {
                tempRow[i] = null;
            }
            tempRow[LENGTHCOL] = twoDigit.format(0.0);
            if (_block.isMetric()) {
                tempRow[UNITSCOL] = Bundle.getMessage("cm");
            } else {
                tempRow[UNITSCOL] = Bundle.getMessage("in");
            }
            tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
        }
        // refresh the Unit column values list
        _units = new ArrayList<>();
        for (int i = 0; i <= _block.getPaths().size(); i++) {
            _units.add(_block.isMetric());
        }
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS; // Edit Turnouts column button is replaced by Edit for _tabbed
    }

    @Override
    public int getRowCount() {
        return _block.getPaths().size() + (_tabbed ? 0 : 1);
        // +1 adds the extra empty row at the bottom of the table display for _desktop
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case FROM_PORTAL_COLUMN:
                return Bundle.getMessage("FromPortal");
            case NAME_COLUMN:
                return Bundle.getMessage("PathName");
            case TO_PORTAL_COLUMN:
                return Bundle.getMessage("ToPortal");
            case LENGTHCOL:
                return Bundle.getMessage("BlockLengthColName");
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
        OPath path = null;
        if (rowIndex < _block.getPaths().size()) {
            path = (OPath) _block.getPaths().get(rowIndex);
        }
        switch (columnIndex) {
            case FROM_PORTAL_COLUMN:
                if (path != null) {
                    Portal portal = path.getFromPortal();
                    if (portal == null) {
                        return "";
                    }
                    return portal.getName();
                } else {
                    return tempRow[columnIndex];
                }
            case NAME_COLUMN:
                if (path != null) {
                    return path.getName();
                } else {
                    return tempRow[columnIndex];
                }
            case TO_PORTAL_COLUMN:
                if (path != null) {
                    Portal portal = path.getToPortal();
                    if (portal == null) {
                        return "";
                    }
                    return portal.getName();
                } else {
                    return tempRow[columnIndex];
                }
            case LENGTHCOL:
                if (path != null) {
                    if (_units.get(rowIndex)) {
                        return (twoDigit.format(path.getLengthCm()));
                    } else {
                        return (twoDigit.format(path.getLengthIn()));
                    }
                }
                break;
            case UNITSCOL:
                return _units.get(rowIndex);
            case EDIT_COL:
                if (path != null) {
                    if (_tabbed) {
                        return Bundle.getMessage("ButtonEdit");
                    } else {
                        return Bundle.getMessage("ButtonEditTO");
                    }
                } else {
                    return "";
                }
            case DELETE_COL:
                if (path != null) {
                    return Bundle.getMessage("ButtonDelete");
                } else {
                    return Bundle.getMessage("ButtonClear"); // for _desktop
                }
            default:
                // fall through
                break;
         }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        String msg = null;
        if (_block.getPaths().size() == row) { // must be a new BlockPath in tempRow (_desktop interface)
            switch (col) {
                case NAME_COLUMN:
                    String strValue = (String)value;
                    if (_block.getPathByName(strValue) != null) { // check for duplicate Path name in this OBlock
                        msg = Bundle.getMessage("DuplPathName", strValue);
                        tempRow[col] = strValue;
                    } else {
                        Portal fromPortal = _block.getPortalByName(tempRow[FROM_PORTAL_COLUMN]);
                        Portal toPortal = _block.getPortalByName(tempRow[TO_PORTAL_COLUMN]);
                        if (fromPortal != null || toPortal != null) {
                            OPath path = new OPath(strValue, _block, fromPortal, toPortal, null);
                            float len = 0.0f;
                            try {
                                len = IntlUtilities.floatValue(tempRow[LENGTHCOL]);
                            } catch (ParseException e) {
                                msg = Bundle.getMessage("BadNumber", tempRow[LENGTHCOL]);                    
                            }
                            if (tempRow[UNITSCOL].equals((Bundle.getMessage("cm")))) {
                                path.setLength(len * 10.0f);
                            } else {
                                path.setLength(len * 25.4f);
                            }
                            
                            if (!_block.addPath(path)) {
                                msg = Bundle.getMessage("AddPathFailed", strValue);
                                tempRow[NAME_COLUMN] = strValue;
                            } else {
                                initTempRow();
                                _parent.updateOBlockTablesMenu();
                                fireTableDataChanged();
                            }
                        } else {
                            tempRow[NAME_COLUMN] = strValue; // initial entry of Path name in cell
                        }
                    }
                    break;
                case UNITSCOL:
                    _units.set(row, (Boolean)value); // true = metric
                    fireTableRowsUpdated(row, row);
                    return;
                case DELETE_COL:
                    initTempRow();
                    fireTableRowsUpdated(row, row);
                    break;
                default:
                    // fall through
                    break;
            }
            tempRow[col] = (String)value;
            if (msg != null) {
                JmriJOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // edit existing BlockPath row
        OPath path = (OPath) _block.getPaths().get(row);
        switch (col) {
            case FROM_PORTAL_COLUMN:
                String strValue = (String)value;
                if (strValue != null) {
                    PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
                    Portal portal = _block.getPortalByName(strValue);
                    if (portal == null || portalMgr.getPortal(strValue) == null) {
                        int val = _parent.verifyWarning(Bundle.getMessage("BlockPortalConflict", value, _block.getDisplayName()));
                        if (val == 2) {
                            break;
                        }
                        portal = portalMgr.providePortal(strValue);
                        if (portal == null) {
                            msg = Bundle.getMessage("NoSuchPortalName", strValue);
                            break;
                        } else {
                            if (!portal.setFromBlock(_block, false)) {
                                val = _parent.verifyWarning(Bundle.getMessage("BlockPathsConflict", value, portal.getFromBlockName()));
                                if (val == 2) {
                                    break;
                                }
                            }
                            portal.setFromBlock(_block, true);
                            _parent.getPortalTableModel().fireTableDataChanged();
                        }
                    }
                    path.setFromPortal(portal);
                    if (!portal.addPath(path)) {
                        msg = Bundle.getMessage("AddPathFailed", strValue);
                    }
                } else {
                    path.setFromPortal(null);
                }
                fireTableRowsUpdated(row, row);
                break;
            case NAME_COLUMN:
                strValue = (String)value;
                if (strValue != null) {
                    if (_block.getPathByName(strValue) != null) {
                        msg = Bundle.getMessage("DuplPathName", strValue);
                    }
                    path.setName(strValue);
                    fireTableRowsUpdated(row, row);
                }
                break;
            case TO_PORTAL_COLUMN:
                strValue = (String)value;
                if (strValue != null) {
                    PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
                    Portal portal = _block.getPortalByName(strValue);
                    if (portal == null || portalMgr.getPortal(strValue) == null) {
                        int val = _parent.verifyWarning(Bundle.getMessage("BlockPortalConflict", value, _block.getDisplayName()));
                        if (val == 2) {
                            break;  // no response
                        }
                        portal = portalMgr.providePortal(strValue);
                        if (portal == null) {
                            msg = Bundle.getMessage("NoSuchPortalName", strValue);
                            break;
                        } else {
                            if (!portal.setToBlock(_block, false)) {
                                val = _parent.verifyWarning(Bundle.getMessage("BlockPathsConflict", value, portal.getToBlockName()));
                                if (val == 2) {
                                    break;
                                }
                            }
                            portal.setToBlock(_block, true);
                            _parent.getPortalTableModel().fireTableDataChanged();
                        }
                    }
                    path.setToPortal(portal);
                    if (!portal.addPath(path)) {
                        msg = Bundle.getMessage("AddPathFailed", strValue);
                    }
                } else {
                    path.setToPortal(null);
                }
                fireTableRowsUpdated(row, row);
                break;
            case LENGTHCOL:
                try {
                    float len = IntlUtilities.floatValue(value.toString());
                    if (_units.get(row)) {
                        path.setLength(len * 10.0f);
                    } else {
                        path.setLength(len * 25.4f);
                    }
                    fireTableRowsUpdated(row, row);                    
                } catch (ParseException e) {
                    JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("BadNumber", value),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.WARNING_MESSAGE);                    
                }
                return;
            case UNITSCOL:
                _units.set(row, (Boolean)value);
                fireTableRowsUpdated(row, row);
                return;
            case EDIT_COL: // button is called [Edit] (_tabbed) or [Edit Turnouts] (_desktop)
                if (_tabbed && !_parent.isPathEdit()) { // everything is handled in BlockPathEdit panel
                    _parent.openPathEditor(_block.getSystemName(), path.getName(), this);
                } else {
                    _parent.openPathTurnoutFrame(_parent.makePathTurnoutName(_block.getSystemName(), path.getName()));
                }
                break;
            case DELETE_COL:
                if (deletePath(path)) {
                    _units.remove(row);
                    fireTableDataChanged();
                }
                break;
            default:
                // fall through
                break;
        }
        if (msg != null) {
            JmriJOptionPane.showMessageDialog(null, msg,
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
        }
    }

    boolean deletePath(OPath path) {
        int val = _parent.verifyWarning(Bundle.getMessage("DeletePathConfirm", path.getName()));
        if (val == 2) {
            return false;
        }
        return _block.removeOPath(path);
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
            default:
                return String.class;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case FROM_PORTAL_COLUMN:
            case NAME_COLUMN:
            case TO_PORTAL_COLUMN:
                return new JTextField(25).getPreferredSize().width;
            case LENGTHCOL:
                return new JTextField(10).getPreferredSize().width;
            case UNITSCOL:
                return new JTextField(6).getPreferredSize().width;
            case EDIT_COL:
                return new JButton("TURNOUT").getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width;
            default:
                // fall through
                break;
        }
        return 5;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (_block.equals(e.getSource())) {
            String property = e.getPropertyName();
            if (log.isDebugEnabled()) {
                log.debug("propertyChange \"{}\".  source= {}", property, e.getSource());
            }
            if (property.equals("portalCount") || 
                    property.equals("pathCount") || property.equals("pathName")) {
                fireTableDataChanged();
            } else if (property.equals("deleted")) {
                _parent.disposeBlockPathFrame(_block);
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockPathTableModel.class);

}
