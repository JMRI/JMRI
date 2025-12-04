package jmri.jmrit.beantable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import jmri.Manager;
import jmri.InstanceManager;
import jmri.tracktiles.TrackTile;
import jmri.tracktiles.TrackTileManager;

/**
 * Swing action to create and register a TrackTile table GUI.
 *
 * @author Ralf Lang Copyright (C) 2025
 */
public class TrackTileTableAction extends AbstractTableAction<TrackTile> {

    private TrackTileManager trackTileManager;

    // Filter components
    private JTextField vendorFilter;
    private JTextField familyFilter;
    private JTextField typeFilter;
    private TableRowSorter<BeanTableDataModel<TrackTile>> sorter;

    /**
     * Create an action with a specific title.
     *
     * @param actionName title of the action
     */
    public TrackTileTableAction(String actionName) {
        super(actionName);
        
        // Try to get the TrackTileManager, but don't fail if it's not available
        try {
            trackTileManager = InstanceManager.getNullableDefault(TrackTileManager.class);
        } catch (Exception e) {
            log.warn("Failed to get TrackTileManager during initialization: {}", e.getMessage());
            trackTileManager = null;
        }
        
        // disable ourself if there is no primary TrackTile manager available
        if (trackTileManager == null) {
            super.setEnabled(false);
            log.debug("TrackTileTableAction disabled - no TrackTileManager available");
        }
    }

    /**
     * Default constructor
     */
    public TrackTileTableAction() {
        this(Bundle.getMessage("TitleTrackTilesTable"));
    }

    @Override
    public void setManager(@Nonnull Manager<TrackTile> man) {
        if (man instanceof TrackTileManager) {
            trackTileManager = (TrackTileManager) man;
        }
    }

    @Override
    protected void createModel() {
        // Check if manager is available before creating the model
        if (trackTileManager == null) {
            log.warn("TrackTileManager not available - cannot create table model");
            // Create a minimal dummy model to prevent null pointer exceptions
            m = new BeanTableDataModel<TrackTile>() {
                @Override
                public Manager<TrackTile> getManager() { return null; }
                @Override
                public TrackTile getBySystemName(@Nonnull String name) { return null; }
                @Override
                public TrackTile getByUserName(@Nonnull String name) { return null; }
                @Override
                public void clickOn(TrackTile t) { }
                @Override
                public String getValue(String s) { return s; }
                @Override
                public String getMasterClassName() { return TrackTileTableAction.class.getName(); }
                @Override
                public int getRowCount() { return 0; }
                @Override
                public int getColumnCount() { return 1; }
                @Override
                public String getColumnName(int col) { return "No Data Available"; }
                @Override
                public Class<?> getColumnClass(int col) { return String.class; }
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
                @Override
                public Object getValueAt(int row, int col) { return "Track Tile Manager not available"; }
                @Override
                public void setValueAt(Object value, int row, int col) { }
                @Override
                public void configureTable(JTable table) { }
            };
            return;
        }
        
        // Create our custom table model that has all the columns we want
        TrackTileTableDataModel customModel = new TrackTileTableDataModel(trackTileManager);

        // Create a BeanTableDataModel wrapper that delegates to our custom model
        m = new BeanTableDataModel<TrackTile>() {
            @Override
            public Manager<TrackTile> getManager() {
                return trackTileManager;
            }

            @Override
            public TrackTile getBySystemName(@Nonnull String name) {
                return trackTileManager.getBySystemName(name);
            }

            @Override
            public TrackTile getByUserName(@Nonnull String name) {
                return trackTileManager.getByUserName(name);
            }

            @Override
            public void clickOn(TrackTile t) {
                // Default implementation
            }

            @Override
            public String getValue(String s) {
                return s;
            }

            @Override
            public String getMasterClassName() {
                return TrackTileTableAction.class.getName();
            }

            // Delegate table model methods to our custom model
            @Override
            public int getRowCount() {
                return customModel.getRowCount();
            }

            @Override
            public int getColumnCount() {
                return customModel.getColumnCount();
            }

            @Override
            public String getColumnName(int col) {
                return customModel.getColumnName(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return customModel.getColumnClass(col);
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return customModel.isCellEditable(row, col);
            }

            @Override
            public Object getValueAt(int row, int col) {
                return customModel.getValueAt(row, col);
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                customModel.setValueAt(value, row, col);
            }

            @Override
            public void configureTable(JTable table) {
                customModel.configureTable(table);
                // Set up filtering after the table is configured
                setupFiltering(table);
            }
        };
    }

    private void setupFiltering(JTable table) {
        // Set up table sorter for filtering
        sorter = new TableRowSorter<>(m);
        table.setRowSorter(sorter);
    }

    @Override
    public void addToFrame(@Nonnull BeanTableFrame<TrackTile> f) {
        super.addToFrame(f);
        // Note: Filters are only supported in tabbed interface for now
        // createFilterPanelForFrame(f);
    }

    @Override
    public void addToFrame(@Nonnull ListedTableFrame.TabbedTableItem<TrackTile> tti) {
        super.addToFrame(tti);
        createFilterPanelForTabbedItem(tti);
    }

    private void createFilterPanelForTabbedItem(ListedTableFrame.TabbedTableItem<TrackTile> tti) {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        // Create filter text fields
        vendorFilter = new JTextField(10);
        familyFilter = new JTextField(10);
        typeFilter = new JTextField(10);

        // Add labels and filter fields
        gbc.gridx = 0;
        filterPanel.add(new JLabel(Bundle.getMessage("TrackTilesVendor") + ":"), gbc);
        gbc.gridx = 1;
        filterPanel.add(vendorFilter, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel(Bundle.getMessage("TrackTilesFamily") + ":"), gbc);
        gbc.gridx = 3;
        filterPanel.add(familyFilter, gbc);

        gbc.gridx = 4;
        filterPanel.add(new JLabel(Bundle.getMessage("TrackTilesType") + ":"), gbc);
        gbc.gridx = 5;
        filterPanel.add(typeFilter, gbc);

        // Add document listeners for real-time filtering
        addFilterListener(vendorFilter);
        addFilterListener(familyFilter);
        addFilterListener(typeFilter);

        // Set up table sorter for filtering
        if (m != null && tti.getDataTable() != null) {
            sorter = new TableRowSorter<>(m);
            tti.getDataTable().setRowSorter(sorter);
        }

        // Add the filter panel to the bottom of the table via TabbedTableItem
        tti.addToBottomBox(filterPanel);
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleTrackTilesTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TrackTileTable";
    }

    @Override
    protected void addPressed(ActionEvent e) {
        log.warn("TrackTileTableAction.addPressed() not implemented");
    }

    @Override
    protected String getClassName() {
        return TrackTileTableAction.class.getName();
    }

    private void addFilterListener(JTextField filterField) {
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        if (sorter == null) {
            return;
        }

        List<RowFilter<BeanTableDataModel<TrackTile>, Integer>> filters = new ArrayList<>();

        // Add vendor filter with umlaut support
        String vendorText = vendorFilter.getText().trim();
        if (!vendorText.isEmpty()) {
            String normalizedVendor = normalizeForSearch(vendorText);
            filters.add(RowFilter.regexFilter("(?i)" + normalizedVendor, TrackTileTableDataModel.VENDOR_COLUMN));
        }

        // Add family filter with umlaut support
        String familyText = familyFilter.getText().trim();
        if (!familyText.isEmpty()) {
            String normalizedFamily = normalizeForSearch(familyText);
            filters.add(RowFilter.regexFilter("(?i)" + normalizedFamily, TrackTileTableDataModel.FAMILY_COLUMN));
        }

        // Add type filter with umlaut support
        String typeText = typeFilter.getText().trim();
        if (!typeText.isEmpty()) {
            String normalizedType = normalizeForSearch(typeText);
            filters.add(RowFilter.regexFilter("(?i)" + normalizedType, TrackTileTableDataModel.JMRITYPE_COLUMN));
        }

        // Combine all filters with AND logic
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    /**
     * Normalize text for umlaut-insensitive searching. Converts search input to
     * match both umlaut and non-umlaut spellings in the data: - "a" → matches
     * both "a" and "ä" - "ae" → matches both "ae" and "ä" - "ä" → matches "ä" -
     * "marklin" → matches "Märklin" - "märklin" → matches "Marklin" and
     * "Märklin" - "maerklin" → matches "Märklin"
     *
     * @param text The search text to normalize
     * @return Regex pattern that will match umlauts in various forms
     */
    private String normalizeForSearch(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            char cLower = Character.toLowerCase(c);

            // Check for two-character umlaut replacements (ae, oe, ue, ss)
            if (i < text.length() - 1) {
                String twoChar = text.substring(i, i + 2).toLowerCase();
                if ("ae".equals(twoChar)) {
                    result.append("(ae|ä|Ä)");
                    i += 2;
                    continue;
                } else if ("oe".equals(twoChar)) {
                    result.append("(oe|ö|Ö)");
                    i += 2;
                    continue;
                } else if ("ue".equals(twoChar)) {
                    result.append("(ue|ü|Ü)");
                    i += 2;
                    continue;
                } else if ("ss".equals(twoChar)) {
                    result.append("(ss|ß)");
                    i += 2;
                    continue;
                }
            }

            // Single character conversions
            switch (cLower) {
                case 'a':
                    result.append("[aäÄ]");
                    break;
                case 'o':
                    result.append("[oöÖ]");
                    break;
                case 'u':
                    result.append("[uüÜ]");
                    break;
                case 'ä':
                    result.append("[äaÄA]");
                    break;
                case 'ö':
                    result.append("[öoÖO]");
                    break;
                case 'ü':
                    result.append("[üuÜU]");
                    break;
                case 'ß':
                    result.append("(ß|ss)");
                    break;
                default:
                    // Escape regex special characters
                    if ("\\[]{}()*+?.^$|".indexOf(c) >= 0) {
                        result.append('\\');
                    }
                    result.append(c);
            }
            i++;
        }

        return result.toString();
    }

    /**
     * Set the table for filtering. This should be called after the table is
     * created.
     * 
     * @param table the JTable to apply filtering to
     */
    public void setTable(JTable table) {
        if (sorter != null && table != null) {
            table.setRowSorter(sorter);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackTileTableAction.class);
}
