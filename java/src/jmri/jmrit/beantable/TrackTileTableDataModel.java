package jmri.jmrit.beantable;

import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JTable;

import jmri.tracktiles.TrackTile;
import jmri.tracktiles.TrackTileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data model for Track Tiles table.
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public class TrackTileTableDataModel extends javax.swing.table.AbstractTableModel {

    private final TrackTileManager manager;
    private final List<TrackTile> tiles;
    private final String userLanguage;
    
    static public final int SYSNAME_COLUMN = 0;
    static public final int VENDOR_COLUMN = 1;
    static public final int FAMILY_COLUMN = 2;
    static public final int JMRITYPE_COLUMN = 3;
    static public final int PARTCODE_COLUMN = 4;
    static public final int CAPTION_COLUMN = 5;
    static public final int NUMCOLUMN = 6;

    private static final Logger log = LoggerFactory.getLogger(TrackTileTableDataModel.class);

    public TrackTileTableDataModel(TrackTileManager manager) {
        super();
        this.manager = manager;
        this.tiles = new java.util.ArrayList<>(manager.getNamedBeanSet());
        this.userLanguage = Locale.getDefault().getLanguage();
        log.debug("TrackTileTableDataModel created with {} tiles, user language: {}", 
                  tiles.size(), userLanguage);
    }

    @Override
    public int getRowCount() {
        return tiles.size();
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case SYSNAME_COLUMN:
                return Bundle.getMessage("ColumnSystemName");
            case VENDOR_COLUMN:
                return Bundle.getMessage("TrackTilesVendor");
            case FAMILY_COLUMN:
                return Bundle.getMessage("TrackTilesFamily");
            case JMRITYPE_COLUMN:
                return Bundle.getMessage("TrackTilesType");
            case PARTCODE_COLUMN:
                return Bundle.getMessage("TrackTilesPartCode");
            case CAPTION_COLUMN:
                return Bundle.getMessage("TrackTilesCaption");
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false; // All cells are read-only
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= tiles.size()) {
            log.error("row index greater than tiles size");
            return null;
        }
        
        TrackTile tile = tiles.get(row);
        
        switch (col) {
            case SYSNAME_COLUMN:
                return tile.getSystemName();
            case VENDOR_COLUMN:
                return tile.getVendor();
            case FAMILY_COLUMN:
                return tile.getFamily();
            case JMRITYPE_COLUMN:
                return tile.getJmriType();
            case PARTCODE_COLUMN:
                return tile.getPartCode();
            case CAPTION_COLUMN:
                return tile.hasLocalizations() ? tile.getCaption(userLanguage) : "";
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // All cells are read-only, no-op
    }

    /**
     * Configure columns for the specified table.
     * 
     * @param table The JTable to configure
     */
    public void configureTable(JTable table) {
        // Set column widths
        table.getColumnModel().getColumn(SYSNAME_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(VENDOR_COLUMN).setPreferredWidth(100);
        table.getColumnModel().getColumn(FAMILY_COLUMN).setPreferredWidth(100);
        table.getColumnModel().getColumn(JMRITYPE_COLUMN).setPreferredWidth(80);
        table.getColumnModel().getColumn(PARTCODE_COLUMN).setPreferredWidth(80);
        table.getColumnModel().getColumn(CAPTION_COLUMN).setPreferredWidth(300);
        
        // Allow sorting
        table.setAutoCreateRowSorter(true);
    }

    public void dispose() {
        // Nothing to dispose currently
    }
}
