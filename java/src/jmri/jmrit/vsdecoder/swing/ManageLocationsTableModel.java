package jmri.jmrit.vsdecoder.swing;

import java.util.HashMap;
import javax.swing.table.AbstractTableModel;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.PhysicalLocation;

/**
 * Table Models for Loading of Reporters, Blocks, Locations and Listener.
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
 * @author Klaus Killinger Copyright (C) 2020
 */
class ManageLocationsTableModel {

    // Note: the four tables covered here do not have the same structure
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
