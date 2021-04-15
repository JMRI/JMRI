package jmri.jmrit.beantable;

import jmri.jmrit.beantable.oblock.*;
import jmri.swing.RowSorterUtil;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.util.table.ToggleButtonEditor;
import jmri.util.table.ToggleButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Objects;

/**
 * GUI for tabbed OBlock editing since 2020. Based on AudioTablePanel.
 * OBlock parts adapted from {@link jmri.jmrit.beantable.oblock.TableFrames}
 * Which interface will be presented is user settable in Display prefs.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse copyright (c) 2020
 */
public class OBlockTablePanel extends JPanel {

    private OBlockTableModel oblockDataModel;
    private PortalTableModel portalDataModel;
    private SignalTableModel signalDataModel;
    private BlockPortalTableModel blockportalDataModel;

    private JTable oblockTable;
    private JTable portalTable;
    private JTable signalTable;
    private JTable blockportalTable;

    private JScrollPane oblockDataScroll;
    private JScrollPane portalDataScroll;
    private JScrollPane signalDataScroll;
    private JScrollPane blockportalDataScroll;

    private final JTabbedPane oblockTabs;
    TableFrames _tf;
    Box bottomBox;                  // panel at bottom for extra buttons etc
    int bottomBoxIndex;             // index to insert extra stuff

    private static final int bottomStrutWidth = 20;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public OBlockTablePanel(OBlockTableModel oblocks,
                            PortalTableModel portals,
                            SignalTableModel signals,
                            BlockPortalTableModel blockportals,
                            TableFrames tf,
                            String helpTarget) {

        super(); // required? nothing set
        _tf = tf;

        log.debug("Building tables");

        // OBlock Table
        oblockDataModel = oblocks;
        TableRowSorter<OBlockTableModel> sorter = new TableRowSorter<>(oblockDataModel);
        // use NamedBean's built-in Comparator interface for sorting the system name column
        RowSorterUtil.setSortOrder(sorter, OBlockTableModel.SYSNAMECOL, SortOrder.ASCENDING);
        oblockTable = makeJTable(OBlockTableAction.class.getName(), oblockDataModel, sorter); // use our own
        // style table, check overlap with configureWarrantTable done next
        oblockTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        oblockTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        oblockTable.getColumnModel().getColumn(OBlockTableModel.UNITSCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.UNITSCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("cm"), Bundle.getMessage("in")));
        oblocks.configCurveColumn(oblockTable); // use real combo
        oblockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        oblocks.configSpeedColumn(oblockTable); // use real combo
        oblockTable.getColumnModel().getColumn(OBlockTableModel.PERMISSIONCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.PERMISSIONCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        oblockTable.setColumnModel(tcm);
        oblockTable.getTableHeader().setReorderingAllowed(true); // makeJTable not used for oblockTable
        oblockTable.createDefaultColumnsFromModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORTERCOL), false); // doesn't hide them?
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORT_CURRENTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.PERMISSIONCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.WARRANTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.ERR_SENSORCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.CURVECOL), false);
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            int width = oblockDataModel.getPreferredWidth(i);
            tcm.getColumn(i).setPreferredWidth(width);
        }
        oblockDataModel.addHeaderListener(oblockTable); // HeaderListeners not set up for the other 3 small tables
        oblockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(550, 300)); // a wide table
        oblockDataScroll = new JScrollPane(oblockTable);

        // Portal Table
        portalDataModel = portals;
        TableRowSorter<PortalTableModel> portalsorter = new TableRowSorter<>(portalDataModel);
        RowSorterUtil.setSortOrder(portalsorter, portalDataModel.NAME_COLUMN, SortOrder.ASCENDING);
        portalTable = makeJTable("Portal", portalDataModel, portalsorter);
        // style table
        portalTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        portalTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        portalTable.doLayout();
        //portalTable.setColumnModel(new XTableColumnModel());
        portalTable.createDefaultColumnsFromModel();
        for (int i = 0; i < portalDataModel.getColumnCount(); i++) {
            int width = portalDataModel.getPreferredWidth(i);
            portalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        portalDataScroll = new JScrollPane(portalTable);

        // Signal Table
        signalDataModel = signals;
        TableRowSorter<SignalTableModel> sigsorter = new TableRowSorter<>(signalDataModel);
        RowSorterUtil.setSortOrder(sigsorter, SignalTableModel.NAME_COLUMN, SortOrder.ASCENDING);
        signalTable = makeJTable("Signals", signalDataModel, sigsorter);
        // style table
        signalTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        signalTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        signalTable.getColumnModel().getColumn(SignalTableModel.UNITSCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        signalTable.getColumnModel().getColumn(SignalTableModel.UNITSCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("cm"), Bundle.getMessage("in")));
        signalTable.doLayout();
        //signalTable.setColumnModel(new XTableColumnModel());
        signalTable.createDefaultColumnsFromModel();
        for (int i = 0; i < signalDataModel.getColumnCount(); i++) {
            int width = SignalTableModel.getPreferredWidth(i);
            signalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        signalDataScroll = new JScrollPane(signalTable);

        // Block-Portal Xreference table
        blockportalDataModel = blockportals; // cross-reference (not editable)
        //sorter = new TableRowSorter<>(blockportalDataModel);
        RowSorterUtil.setSortOrder(sorter, BlockPortalTableModel.BLOCK_NAME_COLUMN, SortOrder.ASCENDING);
        blockportalTable = makeJTable("Block-Portal X-ref", blockportalDataModel, sorter); // cannot directly access
        // style table
        blockportalTable.setDefaultRenderer(String.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        blockportalTable.doLayout();
        //blockportalTable.setColumnModel(new XTableColumnModel());
        blockportalTable.createDefaultColumnsFromModel();
        for (int i = 0; i < blockportalDataModel.getColumnCount(); i++) {
            int width = blockportalDataModel.getPreferredWidth(i);
            blockportalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        blockportalDataScroll = new JScrollPane(blockportalTable);

        // configure items for GUI
        configureWarrantTable(oblockTable); // only class to extend BeanTableDataModel
        //oblockDataModel.configEditColumn(oblockTable);
        for (int i = 0; i < oblockTable.getColumnCount(); i++) {
            // copied from TableFrames#makeOBlockTable() l729 as it needs table so can't copy to oblockDataModel
            int width = oblockDataModel.getPreferredWidth(i);
            oblockTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        oblockDataModel.persistTable(oblockTable); // only oblockDataModel contains this method

        configureWarrantTable(signalTable);
        // pathDataModel.configEditColumn(pathTable);
        //oblockDataModel.persistTable(signalTable);

        configureWarrantTable(portalTable);
        // portalDataModel.configEditColumn(portalTable);
        //oblockDataModel.persistTable(portalTable);

        configureWarrantTable(blockportalTable);
        // portalDataModel.configEditColumn(blockportalTable);
        //oblockDataModel.persistTable(blockportalTable);

        // add more changeListeners for table (example load, created) to update tables?

        // general GUI config
        this.setLayout(new BorderLayout());

        // install the four items in GUI as tabs
        oblockTabs = new JTabbedPane();
        oblockTabs.addTab(Bundle.getMessage("BeanNameOBlocks"), oblockDataScroll);
        oblockTabs.addTab(Bundle.getMessage("BeanNamePortals"), portalDataScroll);
        oblockTabs.addTab(Bundle.getMessage("Signals"), signalDataScroll);
        oblockTabs.addTab(Bundle.getMessage("TitleBlockPortalXRef"), blockportalDataScroll);
        // turnouts not on a tab: via Edit button in Path Edit pane (or a Tables submenu)

        add(oblockTabs, BorderLayout.CENTER);
        log.debug("tabs complete");

        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue()); // stays at end of box
        bottomBoxIndex = 0;

        add(bottomBox, BorderLayout.SOUTH);

        // add extras, if desired by subclass
        extras();

        log.debug("bottomBox complete");
        // set preferred scrolling options
        oblockDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        portalDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        signalDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        blockportalDataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    /**
     * Hook to allow sub-types to install more items in GUI
     */
    void extras() {
    }

    protected Box getBottomBox() {
        return bottomBox;
    }

    public JMenuItem getPrintItem() { // copied from AudioTablePanel
        log.debug("OBLOCK TABBED getPrintItem() called");
        return _tf.getPrintMenuItems(oblockTable, portalTable, signalTable, blockportalTable);
    }

    public JMenu getOptionMenu() {
        log.debug("OBLOCK TABBED getOptionMenu() called");
        return _tf.getOptionMenu();
    }

    public JMenu getTablesMenu() {
        log.debug("OBLOCK TABBED getTablesMenu() called");
        return _tf.getTablesMenu();
    }

    /**
     * Add a component to the bottom box. Takes care of organising glue, struts
     * etc
     *
     * @param comp {@link Component} to add
     */
    protected void addToBottomBox(Component comp) {
        bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
        ++bottomBoxIndex;
        bottomBox.add(comp, bottomBoxIndex);
        ++bottomBoxIndex;
    }

    public void dispose() {
        if (oblockDataModel != null) {
            oblockDataModel.stopPersistingTable(oblockTable);
            oblockDataModel.dispose();
        }
        oblockDataModel = null;
        oblockTable = null;
        oblockDataScroll = null;

        //if (portalDataModel != null) {
            // portalDataModel.stopPersistingTable(portalTable);
            // portalDataModel.dispose();
        //}
        portalDataModel = null;
        portalTable = null;
        portalDataScroll = null;

        //if (signalDataModel != null) {
            // signalDataModel.stopPersistingTable(signalTable);
            // signalDataModel.dispose();
        //}
        signalDataModel = null;
        signalTable = null;
        signalDataScroll = null;

        //if (blockportalDataModel != null) {
            // blockportalDataModel.stopPersistingTable(blockportalTable);
            // blockportalDataModel.dispose();
        //}
        blockportalDataModel = null;
        blockportalTable = null;
        blockportalDataScroll = null;
    }

    /**
     * Create and configure a new table using the given model and row sorter.
     *
     * @param name   the name of the table
     * @param model  the data model for the table
     * @param sorter the row sorter for the table; if null, the table will not
     *               be sortable
     * @return the table
     * @throws NullPointerException if name or model is null
     */
    public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model, @CheckForNull RowSorter<? extends TableModel> sorter) {
        Objects.requireNonNull(name, "the table name must be nonnull " + name);
        Objects.requireNonNull(model, "the table model must be nonnull " + name);
        JTable table = this.configureJTable(name, new JTable(model), sorter);
        //model.addHeaderListener(table);
        return table;
    }

    /**
     * Configure a new table using the given model and row sorter.
     *
     * @param table  the table to configure
     * @param name   the table name
     * @param sorter the row sorter for the table; if null, the table will not
     *               be sortable
     * @return the table
     * @throws NullPointerException if table or the table name is null
     */
    protected JTable configureJTable(@Nonnull String name, @Nonnull JTable table, @CheckForNull RowSorter<? extends TableModel> sorter) {
        Objects.requireNonNull(table, "the table must be nonnull");
        Objects.requireNonNull(name, "the table name must be nonnull");
        table.setRowSorter(sorter);
        table.setName(name);
        //table.getTableHeader().setReorderingAllowed(true); // already assigned per table above
        //table.setColumnModel(new XTableColumnModel());
        //table.createDefaultColumnsFromModel();
        return table;
    }

    /**
     * Configure a table to have our standard rows and columns.
     * This also persists the table user interface state.
     * Adapted from {@link BeanTableDataModel} for tables 1-4, EBR 2020
     *
     * @param table {@link JTable} to configure
     */
    public void configureWarrantTable(JTable table) {
        // ignore Property columns
        table.setDefaultRenderer(JButton.class, new ButtonRenderer());
        table.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        table.setDefaultRenderer(JToggleButton.class, new ToggleButtonRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in"))); // overrides
        table.setDefaultEditor(JToggleButton.class, new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("cm"), Bundle.getMessage("in")));
        table.setDefaultRenderer(JRadioButton.class, new ToggleButtonRenderer(Bundle.getMessage("Current"), Bundle.getMessage("Last"))); // overrides
        table.setDefaultEditor(JRadioButton.class, new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        table.setDefaultRenderer(JCheckBox.class, new ToggleButtonRenderer(Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        table.setDefaultEditor(JCheckBox.class, new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        table.setDefaultEditor(OBlockTableModel.SpeedComboBoxPanel.class, new OBlockTableModel.SpeedComboBoxPanel());
        table.setDefaultRenderer(OBlockTableModel.SpeedComboBoxPanel.class, new OBlockTableModel.SpeedComboBoxPanel());
        table.setDefaultEditor(OBlockTableModel.CurveComboBoxPanel.class, new OBlockTableModel.CurveComboBoxPanel());
        table.setDefaultRenderer(OBlockTableModel.CurveComboBoxPanel.class, new OBlockTableModel.CurveComboBoxPanel());
        // allow reordering of the columns
        //table.getTableHeader().setReorderingAllowed(true);
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(TableFrames.ROW_HEIGHT);
        // resize columns per table
//        table.doLayout();
        // resize columns as requested (for OBlocks tabbed: throws java.lang.IllegalArgumentException: "Identifier not found")
//        for (int i = 0; i < table.getColumnCount(); i++) {
//            int width = table.getColumn(i).getPreferredWidth();
//            table.getColumnModel().getColumn(i).setPreferredWidth(width);
//        }
    }

    private static final Logger log = LoggerFactory.getLogger(OBlockTablePanel.class);

}
