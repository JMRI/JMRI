package jmri.jmrit.beantable;

import jmri.implementation.SignalSpeedMap;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
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

    private JTabbedPane oblockTabs;
    TableFrames _tf;
    Box bottomBox;                  // panel at bottom for extra buttons etc
    int bottomBoxIndex;             // index to insert extra stuff

    private static final int bottomStrutWidth = 20;
    //private static final int ROW_HEIGHT = 10;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public OBlockTablePanel(OBlockTableModel oblocks,
                            PortalTableModel portals,
                            SignalTableModel signals,
                            BlockPortalTableModel blockportals,
                            TableFrames tf,
                            String helpTarget) {

        super(); // EBR required? nothing set
        _tf = tf;

        log.debug("Building tables");
        // OBlock Table
        oblockDataModel = oblocks;
        TableRowSorter<OBlockTableModel> sorter = new TableRowSorter<>(oblockDataModel);
        // use NamedBean's built-in Comparator interface for sorting the system name column
        RowSorterUtil.setSortOrder(sorter, OBlockTableModel.SYSNAMECOL, SortOrder.ASCENDING);
        oblockTable = makeJTable(OBlockTableAction.class.getName(), oblockDataModel, sorter); // use our own
        // style table
        oblockTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        oblockTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        oblockTable.getColumnModel().getColumn(OBlockTableModel.UNITSCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("cm"), Bundle.getMessage("in")));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.UNITSCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("cm"), Bundle.getMessage("in")));
        JComboBox<String> box = new JComboBox<>(OBlockTableModel.curveOptions);
        oblockTable.getColumnModel().getColumn(OBlockTableModel.CURVECOL).setCellEditor(new DefaultCellEditor(box));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.REPORT_CURRENTCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Current"), Bundle.getMessage("Last")));
        box = new JComboBox<>(jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames());
        box.addItem("");
        oblockTable.getColumnModel().getColumn(OBlockTableModel.SPEEDCOL).setCellEditor(new DefaultCellEditor(box));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.PERMISSIONCOL).setCellRenderer(
                new ToggleButtonRenderer(Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        oblockTable.getColumnModel().getColumn(OBlockTableModel.PERMISSIONCOL).setCellEditor(
                new ToggleButtonEditor(new JToggleButton(), Bundle.getMessage("Permissive"), Bundle.getMessage("Absolute")));
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        oblockTable.setColumnModel(tcm);
        oblockTable.getTableHeader().setReorderingAllowed(true);
        oblockTable.createDefaultColumnsFromModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORTERCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.REPORT_CURRENTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.PERMISSIONCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.WARRANTCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.ERR_SENSORCOL), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OBlockTableModel.CURVECOL), false);
        oblockDataScroll = new JScrollPane(oblockTable);

        //oblockTable.createDefaultColumnsFromModel();

        // Portal Table
        portalDataModel = portals;
        TableRowSorter<PortalTableModel> portalsorter = new TableRowSorter<>(portalDataModel);
        RowSorterUtil.setSortOrder(portalsorter, portalDataModel.NAME_COLUMN, SortOrder.ASCENDING);
        portalTable = makeJTable("Portal", portalDataModel, portalsorter);
        // style table
        portalTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        portalTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        for (int i = 0; i < portalDataModel.getColumnCount(); i++) {
            int width = portalDataModel.getPreferredWidth(i);
            portalTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        portalDataScroll = new JScrollPane(portalTable);
        portalTable.setColumnModel(new XTableColumnModel());
        portalTable.createDefaultColumnsFromModel();

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
        signalDataScroll = new JScrollPane(signalTable);
        signalTable.setColumnModel(new XTableColumnModel());
        signalTable.createDefaultColumnsFromModel();

        // Block-Portal Xreference table
        blockportalDataModel = blockportals; // cross-reference (not editable)
        //sorter = new TableRowSorter<>(blockportalDataModel);
        RowSorterUtil.setSortOrder(sorter, BlockPortalTableModel.BLOCK_NAME_COLUMN, SortOrder.ASCENDING);
        blockportalTable = makeJTable("Block-Portal X-ref", blockportalDataModel, sorter); // cannot directly access
        // style table
        blockportalTable.setDefaultRenderer(String.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        blockportalTable.doLayout();

        blockportalDataScroll = new JScrollPane(blockportalTable);
        blockportalTable.setColumnModel(new XTableColumnModel());
        blockportalTable.createDefaultColumnsFromModel();

        // configure items for GUI
        configureWarrantTable(oblockTable); // only class to extend BeanTableDataModel
        //oblockDataModel.configEditColumn(oblockTable);
        for (int i = 0; i < oblockTable.getColumnCount(); i++) {
            // copied from TableFrames#makeOBlockTable() l729 as it needs table so can't copy to oblockDataModel
            int width = oblockDataModel.getPreferredWidth(i);
            oblockTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        oblockDataModel.persistTable(oblockTable); // contains this method

        configureWarrantTable(signalTable);
        // pathDataModel.configEditColumn(pathTable);
        oblockDataModel.persistTable(signalTable);

        configureWarrantTable(portalTable);
        // portalDataModel.configEditColumn(portalTable);
        oblockDataModel.persistTable(portalTable);

        configureWarrantTable(blockportalTable);
        // portalDataModel.configEditColumn(blockportalTable);
        oblockDataModel.persistTable(blockportalTable);

        // TODO add changeListeners for table (example load, created) to update tables EBR

        // general GUI config
        this.setLayout(new BorderLayout());

        // install the four items in GUI as tabs
        oblockTabs = new JTabbedPane();
        oblockTabs.addTab(Bundle.getMessage("BeanNameOBlocks"), oblockDataScroll);
        oblockTabs.addTab(Bundle.getMessage("BeanNamePortals"), portalDataScroll);
        oblockTabs.addTab(Bundle.getMessage("Signals"), signalDataScroll);
        oblockTabs.addTab(Bundle.getMessage("Paths"), blockportalDataScroll);
        // turnouts not on a tab: via button

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
        JMenuItem printItem = new JMenuItem(Bundle.getMessage("PrintTable")); // TODO add a submenu per table

        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MessageFormat footerFormat = new MessageFormat("Page {0,number}");
                    oblockTable.print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                    portalTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Portal Table"), footerFormat);
                    signalTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Signal Table"), footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: {}", e1, e1);
                }
            }
        });
        return printItem;
    }

    public JMenu getOptionMenu() {
        log.debug("OBLOCK TABBED getOptionMenu() called");
        return _tf.getOptionMenu();
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

        if (portalDataModel != null) {
//            portalDataModel.stopPersistingTable(portalTable);
//            portalDataModel.dispose();
        }
        portalDataModel = null;
        portalTable = null;
        portalDataScroll = null;

        if (signalDataModel != null) {
//            signalDataModel.stopPersistingTable(signalTable);
//            signalDataModel.dispose();
        }
        signalDataModel = null;
        signalTable = null;
        signalDataScroll = null;

        if (blockportalDataModel != null) {
//            blockportalDataModel.stopPersistingTable(blockportalTable);
//            blockportalDataModel.dispose();
        }
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
        table.getTableHeader().setReorderingAllowed(true);
        table.setColumnModel(new XTableColumnModel());
        table.createDefaultColumnsFromModel();
        // TODO must put in tableModelClass to attach listener?
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
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        table.setDefaultRenderer(JButton.class, new ButtonRenderer());
        table.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        table.setDefaultRenderer(JToggleButton.class, new ToggleButtonRenderer("cm", "in"));
//        table.setDefaultRenderer(Boolean.class, new ButtonRenderer());
//        table.setDefaultEditor(Boolean.class, new ButtonEditor(new JButton()));

        // allow reordering of the columns
        table.getTableHeader().setReorderingAllowed(true);
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight((new JButton().getPreferredSize().height)*9/10);
        // resize columns per table
        table.doLayout();
        // resize columns as requested
//        for (int i = 0; i < table.getColumnCount(); i++) {
//            int width = table.getColumn(i).getPreferredWidth();
//            table.getColumnModel().getColumn(i).setPreferredWidth(width);
//        }
//        configValueColumn(table);
//        configDeleteColumn(table);
//        oblockTable.persistTable(dataTable);
    }

    private static final Logger log = LoggerFactory.getLogger(OBlockTablePanel.class);

}
