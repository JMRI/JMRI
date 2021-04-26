package jmri.jmrit.beantable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import jmri.*;
import jmri.swing.RowSorterUtil;
import jmri.util.AlphanumComparator;
import jmri.util.gui.GuiLafPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to the various tables in the tabbed Tables interface via a listed pane (normally to the left).
 * <p>
 * Based upon the {@link apps.gui3.tabbedpreferences.TabbedPreferences} by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright 2010
 * @author Bob Jacobsen Copyright 2010
 */
public class ListedTableFrame<E extends NamedBean> extends BeanTableFrame<E> {

    ActionJList actionList;

    public boolean isMultipleInstances() {
        return true;
    }

    static ArrayList<TabbedTableItemListArray> tabbedTableItemListArrayArray = new ArrayList<>();
    ArrayList<TabbedTableItem<E>> tabbedTableArray = new ArrayList<>();

    final UserPreferencesManager pref = InstanceManager.getDefault(UserPreferencesManager.class);
    JSplitPane cardHolder;
    JList<String> list;
    JScrollPane listScroller;
    JPanel listPanel;
    JPanel detailPanel;
    static boolean init = false;

    /**
     * Create a new Listed Table Frame.
     * Call initTables() before initComponents()
     */
    public ListedTableFrame() {
        this(Bundle.getMessage("TitleListedTable"));
    }

    /**
     * Create a new Listed Table Frame.
     * Call initTables() before initComponents()
     * @param s Initial Frame Title
     */
    public ListedTableFrame(String s) {
        super(s);
        if (InstanceManager.getNullableDefault(jmri.jmrit.beantable.ListedTableFrame.class) == null) {
            // We add this to the InstanceManager so that other components can add to the table
            InstanceManager.store(ListedTableFrame.this, jmri.jmrit.beantable.ListedTableFrame.class);
        }
    }
    
    /**
     * Initialise all tables to be added to Frame.
     * Should be called after ListedTableFrame construction and before initComponents()
     */
    public void initTables() {
        if (!init) {
            // Add the default tables to the static list array,
            // this should only be done once on first loading
            addTable("jmri.jmrit.beantable.TurnoutTableTabAction", Bundle.getMessage("MenuItemTurnoutTable"), false);
            addTable("jmri.jmrit.beantable.SensorTableTabAction", Bundle.getMessage("MenuItemSensorTable"), false);
            addTable("jmri.jmrit.beantable.LightTableTabAction", Bundle.getMessage("MenuItemLightTable"), false);
            addTable("jmri.jmrit.beantable.SignalHeadTableAction", Bundle.getMessage("MenuItemSignalTable"), true);
            addTable("jmri.jmrit.beantable.SignalMastTableAction", Bundle.getMessage("MenuItemSignalMastTable"), true);
            addTable("jmri.jmrit.beantable.SignalGroupTableAction", Bundle.getMessage("MenuItemSignalGroupTable"), true);
            addTable("jmri.jmrit.beantable.SignalMastLogicTableAction", Bundle.getMessage("MenuItemSignalMastLogicTable"), true);
            addTable("jmri.jmrit.beantable.ReporterTableTabAction", Bundle.getMessage("MenuItemReporterTable"), false);
            addTable("jmri.jmrit.beantable.MemoryTableAction", Bundle.getMessage("MenuItemMemoryTable"), true);
            addTable("jmri.jmrit.beantable.RouteTableAction", Bundle.getMessage("MenuItemRouteTable"), true);
            addTable("jmri.jmrit.beantable.LRouteTableAction", Bundle.getMessage("MenuItemLRouteTable"), true);
            addTable("jmri.jmrit.beantable.LogixTableAction", Bundle.getMessage("MenuItemLogixTable"), true);
            addTable("jmri.jmrit.beantable.LogixNGTableAction", Bundle.getMessage("MenuItemLogixNGTable"), true);
            addTable("jmri.jmrit.beantable.LogixNGModuleTableAction", Bundle.getMessage("MenuItemLogixNGModuleTable"), true);
            addTable("jmri.jmrit.beantable.LogixNGTableTableAction", Bundle.getMessage("MenuItemLogixNGTableTable"), true);
            addTable("jmri.jmrit.beantable.BlockTableAction", Bundle.getMessage("MenuItemBlockTable"), true);
            if (InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed()) { // select _tabbed in prefs
                addTable("jmri.jmrit.beantable.OBlockTableAction", Bundle.getMessage("MenuItemOBlockTable"), false);
            } // requires restart after changing the interface setting (on Display tab)
            addTable("jmri.jmrit.beantable.SectionTableAction", Bundle.getMessage("MenuItemSectionTable"), true);
            addTable("jmri.jmrit.beantable.TransitTableAction", Bundle.getMessage("MenuItemTransitTable"), true);
            addTable("jmri.jmrit.beantable.AudioTableAction", Bundle.getMessage("MenuItemAudioTable"), false);
            addTable("jmri.jmrit.beantable.IdTagTableTabAction", Bundle.getMessage("MenuItemIdTagTable"), false);
            addTable("jmri.jmrit.beantable.RailComTableAction", Bundle.getMessage("MenuItemRailComTable"), true);
            ListedTableFrame.setInit(true);
        }
    }

    /**
     * Initialise Frame Components.
     * Should be called after initTables()
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        if (tabbedTableItemListArrayArray.isEmpty()) {
            log.error("No tables loaded: {}",this);
            return;
        }
        actionList = new ActionJList(this);

        detailPanel = new JPanel();
        detailPanel.setLayout(new CardLayout());
        tabbedTableArray = new ArrayList<>(tabbedTableItemListArrayArray.size());
        ArrayList<TabbedTableItemListArray> removeItem = new ArrayList<>(5);
        for (TabbedTableItemListArray item : tabbedTableItemListArrayArray) {
            // Here we add all the tables into the panel
            try {
                TabbedTableItem<E> itemModel = new TabbedTableItem<>(item.getClassAsString(), item.getItemString(), item.getStandardTableModel());
                detailPanel.add(itemModel.getPanel(), itemModel.getClassAsString());
                tabbedTableArray.add(itemModel);
                itemModel.getAAClass().addToFrame(this);
            } catch (Exception ex) {
                detailPanel.add(errorPanel(item.getItemString()), item.getClassAsString());
                log.error("Error when adding {} to display", item.getClassAsString(), ex);
                removeItem.add(item);
            }
        }

        for (TabbedTableItemListArray dead : removeItem) {
            tabbedTableItemListArrayArray.remove(dead);
        }

        list = new JList<>(new Vector<>(getChoices()));
        listScroller = new JScrollPane(list);

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addMouseListener(actionList);

        listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout(5, 0));
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.add(listScroller);
        listPanel.setMinimumSize(new Dimension(140, 400)); // guarantees minimum width of left divider list

        buildMenus(tabbedTableArray.get(0));
        setTitle(tabbedTableArray.get(0).getItemString());

        cardHolder = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listPanel, detailPanel);

        cardHolder.setDividerSize(8);
        if (this.getDividerLocation() != 0) {
            cardHolder.setDividerLocation(this.getDividerLocation());
        } else { // if no specific size has been given we set it to the lists preferred width
            cardHolder.setDividerLocation(listScroller.getPreferredSize().width);
        }
        cardHolder.addPropertyChangeListener((PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals("dividerLocation")) {
                InstanceManager.getDefault(UserPreferencesManager.class)
                        .setProperty(ListedTableFrame.class.getName(), "dividerLocation", e.getNewValue());
            }
        });

        cardHolder.setOneTouchExpandable(true);
        getContentPane().add(cardHolder);
        pack();
        actionList.selectListItem(0);
    }

    JPanel errorPanel(String text) {
        JPanel error = new JPanel();
        error.add(new JLabel(Bundle.getMessage("ErrorAddingTable", text)));
        return error;
    }

    /* Method allows for the table to go to a specific list item */
    public void gotoListItem(String selection) {
        for (int x = 0; x < tabbedTableArray.size(); x++) {
            try {
                if (tabbedTableArray.get(x).getClassAsString().equals(selection)) {
                    actionList.selectListItem(x);
                    return;
                }
            } catch (Exception ex) {
                log.error("An error occurred in the goto list for {}, {}", selection,ex.getMessage());
            }
        }
    }

    public void addTable(String aaClass, String choice, boolean stdModel) {
        TabbedTableItemListArray itemToAdd = null;
        for (TabbedTableItemListArray ttila : tabbedTableItemListArrayArray) {
            if (ttila.getClassAsString().equals(aaClass)) {
                log.info("Class {} is already added", aaClass);
                itemToAdd = ttila;
                break;
            }
        }
        if (itemToAdd == null) {
            itemToAdd = new TabbedTableItemListArray(aaClass, choice, stdModel);
            tabbedTableItemListArrayArray.add(itemToAdd);
        }
    }

    @Override
    public void dispose() {
        pref.setSaveAllowed(false);
        for (TabbedTableItem<E> tti : tabbedTableArray) {
            tti.dispose();
        }
        if (list != null && list.getListSelectionListeners().length > 0) {
            list.removeListSelectionListener(list.getListSelectionListeners()[0]);
        }
        super.dispose();
        pref.setSaveAllowed(true);
    }

    void buildMenus(final TabbedTableItem<E> item) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);

        JMenuItem newItem = new JMenuItem(Bundle.getMessage("MenuNewWindow"));
        fileMenu.add(newItem);
        newItem.addActionListener((ActionEvent e) -> actionList.openNewTableWindow(list.getSelectedIndex()));

        fileMenu.add(new jmri.configurexml.StoreMenu());

        JMenuItem printItem = new JMenuItem(Bundle.getMessage("PrintTable"));
        fileMenu.add(printItem);
        printItem.addActionListener((ActionEvent e) -> {
            try {
                // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                if (item.getStandardTableModel()) {
                    item.getDataTable().print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                } else {
                    item.getAAClass().print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                }
            } catch (java.awt.print.PrinterException e1) {
                log.warn("Printing error", e1);
            } catch (NullPointerException ex) {
                log.error("Trying to print returned a NPE error");
            }
        });

        JMenu viewMenu = new JMenu(Bundle.getMessage("MenuView"));
        menuBar.add(viewMenu);
        for (final TabbedTableItemListArray itemList : tabbedTableItemListArrayArray) {
            JMenuItem viewItem = new JMenuItem(itemList.getItemString());
            viewMenu.add(viewItem);
            viewItem.addActionListener((ActionEvent e) -> gotoListItem(itemList.getClassAsString()));
        }

        this.setJMenuBar(menuBar);
        try {
            item.getAAClass().setMenuBar(this);
            this.addHelpMenu(item.getAAClass().helpTarget(), true);
        } catch (Exception ex) {
            log.error("Error when trying to set menu bar for {}", item.getClassAsString(), ex);
        }
        this.revalidate();
    }

    TabbedTableItem<E> lastSelectedItem = null;

    /* This is a bit of a bodge to add the contents to the bottom box and keep
     * it backwardly compatible with the original views. When the original views
     * are deprecated then this can be re-written
     */
    //@TODO Sort out the procedure to add to bottom box
    @Override
    protected void addToBottomBox(Component comp, String c) {
        for (TabbedTableItem<E> tti : tabbedTableArray) {
            if (tti.getClassAsString().equals(c)) {
                tti.addToBottomBox(comp);
                return;
            }
        }
    }

    protected static ArrayList<String> getChoices() {
        ArrayList<String> choices = new ArrayList<>();
        for (TabbedTableItemListArray ttila : tabbedTableItemListArrayArray) {
            choices.add(ttila.getItemString());
        }
        return choices;
    }

    public void setDividerLocation(int loc) {
        if (loc == 0) {
            return;
        }
        cardHolder.setDividerLocation(loc);
        InstanceManager.getDefault(UserPreferencesManager.class)
                .setProperty(ListedTableFrame.class.getName(), "dividerLocation", loc);
    }

    public int getDividerLocation() {
        try {
            return Integer.parseInt(InstanceManager.getDefault(UserPreferencesManager.class)
                    .getProperty(ListedTableFrame.class.getName(), "dividerLocation").toString());
        } catch (NullPointerException | NumberFormatException ex) {
            // ignore, this means the divider location has never been saved
            return 0;
        }
    }

    /**
     * Flag Table initialisation started
     * @param newVal true when started
     */
    private synchronized static void setInit(boolean newVal) {
        init = newVal;
    }

    /**
     * One tabbed item on the ListedTable containing the table(s) for a NamedBean class.
     *
     * @param <E> main class of the table(s)
     */
    static class TabbedTableItem<E extends NamedBean> {

        AbstractTableAction<E> tableAction;
        String className;
        String itemText;
        BeanTableDataModel<E> dataModel;
        JTable dataTable;
        JScrollPane dataScroll;
        Box bottomBox;
        int bottomBoxIndex; // index to insert extra stuff
        static final int bottomStrutWidth = 20;

        boolean standardModel;

        final JPanel dataPanel = new JPanel();

        @SuppressWarnings("unchecked") // type ensured by reflection
        TabbedTableItem(String aaClass, String choice, boolean stdModel) {
            className = aaClass;
            itemText = choice;
            standardModel = stdModel;

            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;

            try {
                Class<?> cl = Class.forName(aaClass);
                java.lang.reflect.Constructor<?> co = cl.getConstructor(String.class);
                tableAction = (AbstractTableAction<E>) co.newInstance(choice);  // this cast is handled by reflection
            } catch (ClassNotFoundException | InstantiationException e1) {
                log.error("Not a valid class : {}", aaClass);
                return;
            } catch (NoSuchMethodException e2) {
                log.error("Not such method : {}", aaClass);
                return;
            } catch (ClassCastException e4) {
                log.error("Not part of the abstractTableActions : {}", aaClass);
                return;
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e) {
                log.error("Exception accessing {}: {}", aaClass, e.getMessage());
                return;
            }

            // If a panel model is used, it should really add to the bottom box
            // but it can be done this way if required.
            // In this case we "hijack" the TabbedTable for different (non-bean) tables to manage OBlocks.
            dataPanel.setLayout(new BorderLayout());

            if (stdModel) {
                createDataModel(); // first table of a grouped set with the primary manager, see OBlockTable
            } else {
                addPanelModel(); // for any additional table using a different manager, see Audio, OBlock
            }
        }

        void createDataModel() {
            dataModel = tableAction.getTableDataModel();
            TableRowSorter<BeanTableDataModel<E>> sorter = new TableRowSorter<>(dataModel);
            dataTable = dataModel.makeJTable(dataModel.getMasterClassName() + ":" + getItemString(), dataModel, sorter);
            dataScroll = new JScrollPane(dataTable);

            // use NamedBean's built-in Comparator interface for sorting the system name column
            RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);

            sorter.setComparator(BeanTableDataModel.USERNAMECOL, new AlphanumComparator());
            RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.USERNAMECOL, SortOrder.ASCENDING);

            dataModel.configureTable(dataTable);

            java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
            // width is fine, but if table is empty, it's not high
            // enough to reserve much space.
            dataTableSize.height = Math.max(dataTableSize.height, 400);
            dataScroll.getViewport().setPreferredSize(dataTableSize);

            // set preferred scrolling options
            dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            dataPanel.add(dataScroll, BorderLayout.CENTER);

            dataPanel.add(bottomBox, BorderLayout.SOUTH);
            if (tableAction.includeAddButton()) {
                JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
                addToBottomBox(addButton);
                addButton.addActionListener((ActionEvent e) -> tableAction.addPressed(e));
            }
            if (dataModel.getPropertyColumnCount() > 0) {
                final JCheckBox propertyVisible = new JCheckBox(Bundle.getMessage
                        ("ShowSystemSpecificProperties"));
                propertyVisible.setToolTipText(Bundle.getMessage
                        ("ShowSystemSpecificPropertiesToolTip"));
                addToBottomBox(propertyVisible);
                propertyVisible.addActionListener((ActionEvent e) -> dataModel.setPropertyColumnsVisible(dataTable, propertyVisible.isSelected()));
                dataModel.setPropertyColumnsVisible(dataTable, false);
            }
            dataModel.persistTable(dataTable);
        }

        void addPanelModel() {
            try {
                dataPanel.add(tableAction.getPanel(), BorderLayout.CENTER);
                dataPanel.add(bottomBox, BorderLayout.SOUTH);
            } catch (NullPointerException e) {
                log.error("An error occurred while trying to create the table for {}", itemText, e);
            }
        }

        boolean getStandardTableModel() {
            return standardModel;
        }

        String getClassAsString() {
            return className;
        }

        String getItemString() {
            return itemText;
        }

        AbstractTableAction<E> getAAClass() {
            return tableAction;
        }

        JPanel getPanel() {
            return dataPanel;
        }

        JTable getDataTable() {
            return dataTable;
        }

        protected void addToBottomBox(Component comp) {
            bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
            ++bottomBoxIndex;
            bottomBox.add(comp, bottomBoxIndex);
            ++bottomBoxIndex;
        }

        void dispose() {
            if (dataModel != null) {
                dataModel.stopPersistingTable(dataTable);
                dataModel.dispose();
            }
            if (tableAction != null) {
                tableAction.dispose();
            }
            dataModel = null;
            dataTable = null;
            dataScroll = null;
        }
    }

    static class TabbedTableItemListArray {

        String className;
        String itemText;
        boolean standardModel;

        TabbedTableItemListArray(String aaClass, String choice, boolean stdModel) {
            className = aaClass;
            itemText = choice;
            standardModel = stdModel;
        }

        boolean getStandardTableModel() {
            return standardModel;
        }

        String getClassAsString() {
            return className;
        }

        String getItemString() {
            return itemText;
        }

    }

    /**
     * ActionJList This deals with handling non-default mouse operations on the
     * List panel and allows for right click popups and double click to open new
     * windows of the items we are hovering over.
     */
    class ActionJList extends MouseAdapter {

        JPopupMenu popUp;
        JMenuItem menuItem;

        protected BeanTableFrame<E> frame;

        ActionJList(BeanTableFrame<E> f) {
            frame = f;
            popUp = new JPopupMenu();
            menuItem = new JMenuItem("Open in New Window"); // TODO I18N
            popUp.add(menuItem);
            menuItem.addActionListener((ActionEvent e) -> openNewTableWindow(mouseItem));
            currentItemSelected = 0;
        }

        private int currentItemSelected;

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        // records the original pre-click index
        private int beforeClickIndex;
        
        //Records the item index that the mouse is currently over
        private int mouseItem;        

        void showPopup(MouseEvent e) {
            popUp.show(e.getComponent(), e.getX(), e.getY());
            mouseItem = list.locationToIndex(e.getPoint());
        }

        void setCurrentItem(int current) {
            currentItemSelected = current;
        }

        @Override
        public void mouseClicked(MouseEvent e) {

            mouseItem = list.locationToIndex(e.getPoint());
            if (popUp.isVisible()) {
                return;
            }
            if (e.isPopupTrigger()) {
                showPopup(e);
                return;
            }
            if (e.getClickCount() == 1) {
                beforeClickIndex = currentItemSelected;
                selectListItem(mouseItem);
            } else if (e.getClickCount() == 2) {
                list.setSelectedIndex(beforeClickIndex);
                selectListItem(beforeClickIndex);
                openNewTableWindow(mouseItem);
            }
        }

        void openNewTableWindow(int index) {
            TabbedTableItem<E> item = tabbedTableArray.get(index);
            class WindowMaker implements Runnable {

                final TabbedTableItem<E> item;

                WindowMaker(TabbedTableItem<E> tItem) {
                    item = tItem;
                }

                @Override
                public void run() {
                    ListedTableAction tmp = new ListedTableAction(item.getItemString(), item.getClassAsString(), cardHolder.getDividerLocation());
                    tmp.actionPerformed();
                }
            }
            WindowMaker t = new WindowMaker(item);
            javax.swing.SwingUtilities.invokeLater(t);
        }

        void selectListItem(int index) {
            currentItemSelected = index;
            TabbedTableItem<E> item = tabbedTableArray.get(index);
            CardLayout cl = (CardLayout) (detailPanel.getLayout());
            cl.show(detailPanel, item.getClassAsString());
            frame.setTitle(item.getItemString());
            frame.generateWindowRef();
            try {
                item.getAAClass().setFrame(frame);
                buildMenus(item);
            } catch (Exception ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
            list.ensureIndexIsVisible(index);
            list.setSelectedIndex(index);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ListedTableFrame.class);

}
