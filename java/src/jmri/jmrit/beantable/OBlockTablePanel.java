package jmri.jmrit.beantable;

import jmri.jmrit.beantable.oblock.*;
import jmri.swing.RowSorterUtil;
import jmri.util.swing.XTableColumnModel;
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
import java.text.MessageFormat;
import java.util.Objects;

/**
 * GUI for tabbed OBlock editing since 2020. Based on AudioTablePanel.
 * OBlock parts adapted from {@link jmri.jmrit.beantable.oblock.TableFrames}
 * Which interface will be presented is user settable in Diplay prefs.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse copyright (c) 2020
 */
public class OBlockTablePanel extends JPanel {

    private OBlockTableModel oblockDataModel;
    private final PortalTableModel portalDataModel;
    private final SignalTableModel signalDataModel;
    private final BlockPortalTableModel blockportalDataModel;
    //    private BlockPathTableModel pathDataModel;

    private JTable oblockTable;
    private final JTable portalTable;
    private JTable signalTable;
    private final JTable blockportalTable;
    //    private JTable blockpathTable;

    private JScrollPane oblockDataScroll;
    private JScrollPane portalDataScroll;
    private JScrollPane signalDataScroll;
    private JScrollPane blockportalDataScroll;
    private JScrollPane blockpathDataScroll;

    private JTabbedPane oblockTabs;
    Box bottomBox;                  // panel at bottom for extra buttons etc
    int bottomBoxIndex;             // index to insert extra stuff

    static final int bottomStrutWidth = 20;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public OBlockTablePanel(OBlockTableModel oblocks,
                            PortalTableModel portals,
                            SignalTableModel signals,
                            BlockPortalTableModel blockportals,
                            String helpTarget) {

        super();
        oblockDataModel = oblocks;
        TableRowSorter<OBlockTableModel> sorter = new TableRowSorter<>(oblockDataModel);
        // use NamedBean's built-in Comparator interface for sorting the system name column
        RowSorterUtil.setSortOrder(sorter, OBlockTableModel.SYSNAMECOL, SortOrder.ASCENDING);
        oblockTable = oblockDataModel.makeJTable(OBlockTableAction.class.getName(), oblockDataModel, sorter); // cannot directly access oblock
        oblockDataScroll = new JScrollPane(oblockTable);
        oblockTable.setColumnModel(new XTableColumnModel());
        oblockTable.createDefaultColumnsFromModel();

        portalDataModel = portals;
        RowSorterUtil.setSortOrder(sorter, portalDataModel.NAME_COLUMN, SortOrder.ASCENDING);
        portalTable = makeJTable("Portals", portalDataModel, sorter); // cannot directly access oblock
        portalDataScroll = new JScrollPane(portalTable);
        portalTable.setColumnModel(new XTableColumnModel());
        portalTable.createDefaultColumnsFromModel();

        signalDataModel = signals;
        RowSorterUtil.setSortOrder(sorter, SignalTableModel.NAME_COLUMN, SortOrder.ASCENDING);
        signalTable = makeJTable("Signals", signalDataModel, sorter); // cannot directly access oblock
        signalDataScroll = new JScrollPane(signalTable);
        signalTable.setColumnModel(new XTableColumnModel());
        signalTable.createDefaultColumnsFromModel();

        blockportalDataModel = blockportals; // cross-reference (not editable)
        RowSorterUtil.setSortOrder(sorter, BlockPortalTableModel.BLOCK_NAME_COLUMN, SortOrder.ASCENDING);
        blockportalTable = makeJTable("Block-Portal X-ref", blockportalDataModel, sorter); // cannot directly access oblock
        blockportalDataScroll = new JScrollPane(blockportalTable);
        blockportalTable.setColumnModel(new XTableColumnModel());
        blockportalTable.createDefaultColumnsFromModel();

        // later created on demand
        //        blockpathDataModel = blockpaths;
        //        RowSorterUtil.setSortOrder(sorter, OBlockTableModel.SYSNAMECOL, SortOrder.ASCENDING);
        //        oblockTable = oblockDataModel.makeJTable(OBlockTableAction.class.getName(), oblockDataModel, sorter); // cannot directly access oblock
        //        oblockDataScroll = new JScrollPane(oblockTable);
        //        oblockTable.setColumnModel(new XTableColumnModel());
        //        oblockTable.createDefaultColumnsFromModel();

        // configure items for GUI
        oblockDataModel.configureTable(oblockTable);
        //oblockDataModel.configEditColumn(oblockTable);
        oblockDataModel.persistTable(oblockTable);
//        pathDataModel.configureTable(pathTable);
//        pathDataModel.configEditColumn(pathTable);
//        pathDataModel.persistTable(pathTable);
//        portalDataModel.configureTable(portalTable);
//        portalDataModel.configEditColumn(portalTable);
//        portalDataModel.persistTable(portalDataTable);

        // general GUI config
        this.setLayout(new BorderLayout());

        // install items in GUI
        oblockTabs = new JTabbedPane();
        oblockTabs.addTab(Bundle.getMessage("BeanNameOBlocks"), oblockDataScroll);
        oblockTabs.addTab(Bundle.getMessage("BeanNamePortals"), portalDataScroll);
        oblockTabs.addTab(Bundle.getMessage("Signals"), signalDataScroll);
        oblockTabs.addTab(Bundle.getMessage("Paths"), blockportalDataScroll);
        //oblockTabs.addTab(Bundle.getMessage("Paths"), blockpathDataScroll); // Paths
        // turnout: later

        add(oblockTabs, BorderLayout.CENTER);

        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue()); // stays at end of box
        bottomBoxIndex = 0;

        add(bottomBox, BorderLayout.SOUTH);

        // add extras, if desired by subclass
        extras();

        // set preferred scrolling options
        oblockDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        portalDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        pathDataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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

    public JMenuItem getPrintItem() {
        JMenuItem printItem = new JMenuItem(Bundle.getMessage("PrintTable"));

        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MessageFormat footerFormat = new MessageFormat("Page {0,number}");
                    oblockTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Listener Table"), footerFormat);
                    portalTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Buffer Table"), footerFormat);
                    //blockPathTable.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Source Table"), footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: {}", e1, e1);
                }
            }
        });
        return printItem;
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

//        if (bufferDataModel != null) {
//            bufferDataModel.stopPersistingTable(bufferDataTable);
//            bufferDataModel.dispose();
//        }
//        bufferDataModel = null;
//        bufferDataTable = null;
//        bufferDataScroll = null;
//
//        if (sourceDataModel != null) {
//            sourceDataModel.stopPersistingTable(sourceDataTable);
//            sourceDataModel.dispose();
//        }
//        sourceDataModel = null;
//        sourceDataTable = null;
//        sourceDataScroll = null;
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
        Objects.requireNonNull(name, "the table name must be nonnull" + name);
        Objects.requireNonNull(model, "the table model must be nonnull" + name);
        return this.configureJTable(name, new JTable(model), sorter);
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
        //addMouseListenerToHeader(table); // TODO must put in tableModelClass to attach listener
        return table;
    }

    private static final Logger log = LoggerFactory.getLogger(OBlockTablePanel.class);

}
