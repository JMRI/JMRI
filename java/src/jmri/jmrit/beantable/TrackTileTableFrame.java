package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;

import jmri.util.JmriJFrame;

/**
 * Frame to display Track Tiles table.
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public class TrackTileTableFrame extends JmriJFrame {

    private final TrackTileTableDataModel dataModel;
    private final JTable dataTable;
    private final TableRowSorter<TrackTileTableDataModel> sorter;
    private final JTextField vendorFilter;
    private final JTextField familyFilter;
    private final JTextField typeFilter;
    private final JTextField partCodeFilter;
    private final JTextField captionFilter;
    private final JLabel infoLabel;

    /**
     * Constructor for TrackTileTableFrame.
     * 
     * @param model     The table data model
     * @param table     The JTable
     * @param helpTarget Help system target
     */
    public TrackTileTableFrame(TrackTileTableDataModel model, JTable table, String helpTarget) {
        super();
        this.dataModel = model;
        this.dataTable = table;

        setTitle(Bundle.getMessage("TitleTrackTilesTable"));
        buildMenus();
        
        // Create row sorter for filtering
        this.sorter = new TableRowSorter<>(dataModel);
        dataTable.setRowSorter(sorter);
        
        // Create filter text fields
        vendorFilter = new JTextField(10);
        familyFilter = new JTextField(10);
        typeFilter = new JTextField(10);
        partCodeFilter = new JTextField(10);
        captionFilter = new JTextField(15);
        
        // Add document listeners to filters
        addFilterListener(vendorFilter);
        addFilterListener(familyFilter);
        addFilterListener(typeFilter);
        addFilterListener(partCodeFilter);
        addFilterListener(captionFilter);
        
        // Create main panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Add filter panel
        contentPanel.add(createFilterPanel());

        // Add table in scroll pane
        JScrollPane scrollPane = new JScrollPane(dataTable);
        contentPanel.add(scrollPane);

        // Bottom panel with information
        JPanel bottomPanel = new JPanel();
        infoLabel = new JLabel();
        updateInfoLabel();
        bottomPanel.add(infoLabel);
        contentPanel.add(bottomPanel);

        setContentPane(contentPanel);
        
        // Set help target
        addHelpMenu("package.jmri.jmrit.beantable.TrackTileTable", true);

        pack();
        
        // Set minimum size
        setMinimumSize(new java.awt.Dimension(800, 400));
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        
        // Vendor filter
        gbc.gridx = 0;
        panel.add(new JLabel(Bundle.getMessage("TrackTilesVendor") + ":"), gbc);
        gbc.gridx = 1;
        panel.add(vendorFilter, gbc);
        
        // Family filter
        gbc.gridx = 2;
        panel.add(new JLabel(Bundle.getMessage("TrackTilesFamily") + ":"), gbc);
        gbc.gridx = 3;
        panel.add(familyFilter, gbc);
        
        // Type filter
        gbc.gridx = 4;
        panel.add(new JLabel(Bundle.getMessage("TrackTilesType") + ":"), gbc);
        gbc.gridx = 5;
        panel.add(typeFilter, gbc);
        
        // Part Code filter (second row)
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel(Bundle.getMessage("TrackTilesPartCode") + ":"), gbc);
        gbc.gridx = 1;
        panel.add(partCodeFilter, gbc);
        
        // Caption filter
        gbc.gridx = 2;
        panel.add(new JLabel(Bundle.getMessage("TrackTilesCaption") + ":"), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 3;
        panel.add(captionFilter, gbc);
        
        return panel;
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
    
    /**
     * Normalize text for umlaut-insensitive searching.
     * Converts search input to match both umlaut and non-umlaut spellings in the data:
     * - "a" → matches both "a" and "ä" 
     * - "ae" → matches both "ae" and "ä"
     * - "ä" → matches "ä"
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
            
            // Check for two-character umlaut replacements (ae, oe, ue)
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
    
    private void applyFilters() {
        List<RowFilter<TrackTileTableDataModel, Integer>> filters = new ArrayList<>();
        
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
        
        // Add part code filter with umlaut support
        String partCodeText = partCodeFilter.getText().trim();
        if (!partCodeText.isEmpty()) {
            String normalizedPartCode = normalizeForSearch(partCodeText);
            filters.add(RowFilter.regexFilter("(?i)" + normalizedPartCode, TrackTileTableDataModel.PARTCODE_COLUMN));
        }
        
        // Add caption filter with umlaut support
        String captionText = captionFilter.getText().trim();
        if (!captionText.isEmpty()) {
            String normalizedCaption = normalizeForSearch(captionText);
            filters.add(RowFilter.regexFilter("(?i)" + normalizedCaption, TrackTileTableDataModel.CAPTION_COLUMN));
        }
        
        // Combine all filters with AND logic
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
        
        // Update info label to show filtered count
        updateInfoLabel();
    }
    
    private void updateInfoLabel() {
        int totalCount = dataModel.getRowCount();
        int visibleCount = dataTable.getRowCount();
        
        String infoText;
        if (visibleCount < totalCount) {
            infoText = Bundle.getMessage("TrackTilesCatalogInfoFiltered", visibleCount, totalCount);
        } else {
            infoText = Bundle.getMessage("TrackTilesCatalogInfo", totalCount);
        }
        infoLabel.setText(infoText);
    }

    /**
     * Build the menu bar for this frame.
     */
    private void buildMenus() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu could be added here if needed
        
        setJMenuBar(menuBar);
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.dispose();
        }
        super.dispose();
    }
}
