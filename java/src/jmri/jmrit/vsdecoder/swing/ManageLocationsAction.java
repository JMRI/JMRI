package jmri.jmrit.vsdecoder.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.table.AbstractTableModel;
import jmri.Block;
import jmri.BlockManager;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loading of Reporters, Blocks, Locations and Listener attributes.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class ManageLocationsAction extends AbstractAction {

    // Note: the four tables don't have the same structure
    static final int SYSNAMECOL     = 0;             // system name
    static final int NAMECOL        = 0;             // name (special case for Operations Locations)
    static final int USERNAMECOL    = 1;             // user name
    static final int USECOL         = 1;             // usage flag for Operations Locations and Listener
    static final int XCOL           = 2;             // X value
    static final int YCOL           = 3;             // Y value
    static final int ZCOL           = 4;             // Z value
    static final int TUNNELCOL      = 5;             // tunnel flag (not for Listener)
    static final int BEARINGCOL     = 5;             // bearing attribute (Listener only)
    static final int AZIMUTHCOL     = 6;             // azimuth attribute (Listener only)

    private ManageLocationsFrame f = null;
    private ListeningSpot listenerLoc;

    public ManageLocationsAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            // Handle the Listener
            listenerLoc = VSDecoderManager.instance().getVSDecoderPreferences().getListenerPosition();

            // Handle Reporters
            ReporterManager rmgr = jmri.InstanceManager.getDefault(ReporterManager.class);
            Set<Reporter> reporterSet = rmgr.getNamedBeanSet();
            Object[][] reporterTable = new Object[reporterSet.size()][7];
            int i = 0;
            for (Reporter r : reporterSet) {
                if (r != null) {
                    if (r instanceof PhysicalLocationReporter) {
                        PhysicalLocation p = ((PhysicalLocationReporter) r).getPhysicalLocation();
                        reporterTable[i][SYSNAMECOL] = r.getSystemName();
                        reporterTable[i][USERNAMECOL] = r.getUserName();
                        reporterTable[i][USECOL + 1] = true;
                        reporterTable[i][XCOL + 1] = p.getX();
                        reporterTable[i][YCOL + 1] = p.getY();
                        reporterTable[i][ZCOL + 1] = p.getZ();
                        reporterTable[i][TUNNELCOL + 1] = p.isTunnel();
                    } else {
                        reporterTable[i][SYSNAMECOL] = r.getSystemName();
                        reporterTable[i][USERNAMECOL] = r.getUserName();
                        reporterTable[i][USECOL + 1] = false;
                        reporterTable[i][XCOL + 1] = Float.valueOf(0.0f);
                        reporterTable[i][YCOL + 1] = Float.valueOf(0.0f);
                        reporterTable[i][ZCOL + 1] = Float.valueOf(0.0f);
                        reporterTable[i][TUNNELCOL + 1] = false;
                    }
                }
                i++;
            }

            // Handle Blocks
            BlockManager bmgr = jmri.InstanceManager.getDefault(BlockManager.class);
            Set<Block> blockSet = bmgr.getNamedBeanSet();
            Object[][] blockTable = new Object[blockSet.size()][7];
            i = 0;
            for (Block b : blockSet) {
                if (b != null) {
                    PhysicalLocation p = ((PhysicalLocationReporter) b).getPhysicalLocation();
                    blockTable[i][SYSNAMECOL] = b.getSystemName();
                    blockTable[i][USERNAMECOL] = b.getUserName();
                    blockTable[i][USECOL + 1] = true;
                    blockTable[i][XCOL + 1] = p.getX();
                    blockTable[i][YCOL + 1] = p.getY();
                    blockTable[i][ZCOL + 1] = p.getZ();
                    blockTable[i][TUNNELCOL + 1] = p.isTunnel();
                }
                i++;
            }

            // Handle Ops Locations
            LocationManager lmgr = jmri.InstanceManager.getDefault(LocationManager.class);
            List<Location> locations = lmgr.getLocationsByIdList();
            log.debug("TableSize: {}", locations.size());
            Object[][] opsTable = new Object[locations.size()][6];
            i = 0;
            for (Location l : locations) {
                log.debug("i: {}, MLA: {}, Name: {}, table: {}", i, l.getId(), l.getName(), java.util.Arrays.toString(opsTable[i]));
                opsTable[i][NAMECOL] = l.getName();
                PhysicalLocation p = l.getPhysicalLocation();
                if (p == PhysicalLocation.Origin) {
                    opsTable[i][USECOL] = false;
                } else {
                    opsTable[i][USECOL] = true;
                }
                opsTable[i][XCOL] = p.getX();
                opsTable[i][YCOL] = p.getY();
                opsTable[i][ZCOL] = p.getZ();
                opsTable[i][TUNNELCOL] = p.isTunnel();
                i++;
            }

            f = new ManageLocationsFrame(listenerLoc, reporterTable, opsTable, blockTable);
        }
        f.setExtendedState(Frame.NORMAL);
    }

    private final static Logger log = LoggerFactory.getLogger(ManageLocationsAction.class);

    /**
     * class to serve as TableModel for Reporters and Blocks
     */
    static class ReporterBlockTableModel extends AbstractTableModel {

        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[7];
        private Object[][] rowData;

        public ReporterBlockTableModel(Object[][] dataMap) {
            super();
            // Use i18n-ized column titles.
            columnNames[SYSNAMECOL] = Bundle.getMessage("Name");
            columnNames[USERNAMECOL] = Bundle.getMessage("ColumnUserName");
            columnNames[USECOL + 1] = Bundle.getMessage("FieldTableUseColumn");
            columnNames[XCOL + 1] = Bundle.getMessage("FieldTableXColumn");
            columnNames[YCOL + 1] = Bundle.getMessage("FieldTableYColumn");
            columnNames[ZCOL + 1] = Bundle.getMessage("FieldTableZColumn");
            columnNames[TUNNELCOL + 1] = Bundle.getMessage("FieldTableIsTunnelColumn");
            rowData = dataMap;
        }

        public HashMap<String, PhysicalLocation> getDataMap() {
            // Includes only the ones with the checkbox made
            HashMap<String, PhysicalLocation> retv = new HashMap<>();
            for (Object[] row : rowData) {
                if ((Boolean) row[USECOL + 1]) {
                    if (row[XCOL + 1] == null) { 
                        row[XCOL + 1] = 0.0f;
                    }
                    if (row[YCOL + 1] == null) { 
                        row[YCOL + 1] = 0.0f;
                    }
                    if (row[ZCOL + 1] == null) { 
                        row[ZCOL + 1] = 0.0f;
                    }
                    retv.put((String) row[SYSNAMECOL], 
                            new PhysicalLocation((Float) row[XCOL + 1], (Float) row[YCOL + 1], (Float) row[ZCOL + 1], (Boolean) row[TUNNELCOL + 1]));
                }
            }
            return retv;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return rowData.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return rowData[row][col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case USECOL + 1:
                case TUNNELCOL + 1:
                    return Boolean.class;
                case ZCOL + 1:
                case YCOL + 1:
                case XCOL + 1:
                    return Float.class;
                case USERNAMECOL:
                case SYSNAMECOL:
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }

    /**
     * class to serve as TableModel for Ops Locations
     */
    static class LocationTableModel extends AbstractTableModel {

        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[6];
        private Object[][] rowData;

        public LocationTableModel(Object[][] dataMap) {
            super();
            // Use i18n-ized column titles.
            columnNames[NAMECOL] = Bundle.getMessage("Name");
            columnNames[USECOL] = Bundle.getMessage("FieldTableUseColumn");
            columnNames[XCOL] = Bundle.getMessage("FieldTableXColumn");
            columnNames[YCOL] = Bundle.getMessage("FieldTableYColumn");
            columnNames[ZCOL] = Bundle.getMessage("FieldTableZColumn");
            columnNames[TUNNELCOL] = Bundle.getMessage("FieldTableIsTunnelColumn");
            rowData = dataMap;
        }

        public HashMap<String, PhysicalLocation> getDataMap() {
            // Includes only the ones with the checkbox made
            HashMap<String, PhysicalLocation> retv = new HashMap<>();
            for (Object[] row : rowData) {
                if ((Boolean) row[USECOL]) {
                    if (row[XCOL] == null) { 
                        row[XCOL] = 0.0f;
                    }
                    if (row[YCOL] == null) { 
                        row[YCOL] = 0.0f;
                    }
                    if (row[ZCOL] == null) { 
                        row[ZCOL] = 0.0f;
                    }
                    retv.put((String) row[NAMECOL], 
                            new PhysicalLocation((Float) row[XCOL], (Float) row[YCOL], (Float) row[ZCOL], (Boolean) row[TUNNELCOL]));
                }
            }
            return retv;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return rowData.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return rowData[row][col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case USECOL:
                case TUNNELCOL:
                    return Boolean.class;
                case ZCOL:
                case YCOL:
                case XCOL:
                    return Float.class;
                case NAMECOL:
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }

    /**
     * class for use as TableModel for Listener Locations
     */
    static class ListenerTableModel extends AbstractTableModel {

        // These get internationalized at runtime in the constructor below.
        private String[] columnNames = new String[7];
        private Object[][] rowData = null;

        public ListenerTableModel(Object[][] dataMap) {
            super();
            // Use i18n-ized column titles.
            columnNames[NAMECOL] = Bundle.getMessage("Name");
            columnNames[USECOL] = Bundle.getMessage("FieldTableUseColumn");
            columnNames[XCOL] = Bundle.getMessage("FieldTableXColumn");
            columnNames[YCOL] = Bundle.getMessage("FieldTableYColumn");
            columnNames[ZCOL] = Bundle.getMessage("FieldTableZColumn");
            columnNames[BEARINGCOL] = Bundle.getMessage("FieldTableBearingColumn");
            columnNames[AZIMUTHCOL] = Bundle.getMessage("FieldTableAzimuthColumn");
            rowData = dataMap;
        }

        @SuppressWarnings("unused")
        public HashMap<String, ListeningSpot> getDataMap() {
            // Includes only the ones with the checkbox made
            HashMap<String, ListeningSpot> retv = new HashMap<>();
            ListeningSpot spot = null;
            for (Object[] row : rowData) {
                if ((Boolean) row[USECOL]) {
                    spot = new ListeningSpot();
                    spot.setName((String) row[NAMECOL]);
                    spot.setLocation((Double) row[XCOL], (Double) row[YCOL], (Double) row[ZCOL]);
                    spot.setOrientation((Double) row[BEARINGCOL], (Double) row[AZIMUTHCOL]);
                    retv.put((String) row[NAMECOL], spot);
                }
            }
            return retv;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return rowData.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return rowData[row][col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case USECOL:
                    return Boolean.class;
                case AZIMUTHCOL:
                case BEARINGCOL:
                case ZCOL:
                case YCOL:
                case XCOL:
                    return Double.class;
                case NAMECOL:
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }
}
