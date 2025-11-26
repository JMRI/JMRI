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
    
    private void applyFilters() {
        List<RowFilter<TrackTileTableDataModel, Integer>> filters = new ArrayList<>();
        
        // Add vendor filter
        String vendorText = vendorFilter.getText().trim();
        if (!vendorText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + vendorText, TrackTileTableDataModel.VENDOR_COLUMN));
        }
        
        // Add family filter
        String familyText = familyFilter.getText().trim();
        if (!familyText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + familyText, TrackTileTableDataModel.FAMILY_COLUMN));
        }
        
        // Add type filter
        String typeText = typeFilter.getText().trim();
        if (!typeText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + typeText, TrackTileTableDataModel.JMRITYPE_COLUMN));
        }
        
        // Add part code filter
        String partCodeText = partCodeFilter.getText().trim();
        if (!partCodeText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + partCodeText, TrackTileTableDataModel.PARTCODE_COLUMN));
        }
        
        // Add caption filter
        String captionText = captionFilter.getText().trim();
        if (!captionText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + captionText, TrackTileTableDataModel.CAPTION_COLUMN));
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
