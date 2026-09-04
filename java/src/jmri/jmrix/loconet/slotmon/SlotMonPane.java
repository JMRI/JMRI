package jmri.jmrix.loconet.slotmon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.SlotMapEntry.SlotType;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.MenuScroller;
import jmri.util.swing.JmriMouseAdapter;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import jmri.util.swing.WrapLayout;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.*;

/**
 * Frame providing a command station slot manager.
 * <p>
 * Slots 102 through 127 are normally not used for loco control, so are shown
 * separately.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SlotMonPane extends jmri.jmrix.loconet.swing.LnPanel implements SlotListener {

    /**
     * Controls whether not-in-use slots are shown
     */
    protected final JCheckBox showUnusedCheckBox = new JCheckBox();
    /**
     * Controls whether system slots (0, 121-127) are shown
     */
    protected final JCheckBox showSystemCheckBox = new JCheckBox();

    private JLabel dcsCSLabel = new JLabel(Bundle.getMessage("SlotMonCSLabel"));
    private JTextField dcsType = new JTextField();
    private JLabel dcsSlotsLabel = new JLabel(Bundle.getMessage("SlotMonTotalSlots"));
    private JTextField dcsSlots = new JTextField();

    private final JButton estopAllButton = new JButton(Bundle.getMessage("ButtonSlotMonEStopAll"));

    private JLabel inUseCount = new JLabel("      ");

    //Added by Jeffrey Machacek 2013
    private final JButton clearAllButton = new JButton(Bundle.getMessage("ButtonSlotMonClearAll"));
    private final JButton refreshAllButton = new JButton(Bundle.getMessage("ButtonSlotRefresh"));

    private JPanel topPanel;  // the panel across the top that holds buttons

    private SlotMonDataModel slotModel;
    private JTable slotTable;
    private JScrollPane slotScroll;
    private transient TableRowSorter<SlotMonDataModel> sorter;

    // Options menu
    private JCheckBoxMenuItem optionHideTopPanel = new JCheckBoxMenuItem(Bundle.getMessage("SlotMonHideTopPanel"));

    // filter menu
    private JCheckBoxMenuItem filterShowIdle = new JCheckBoxMenuItem(Bundle.getMessage("SlotMonShowIdle"));
    private JCheckBoxMenuItem filterShowUnUsed = new JCheckBoxMenuItem(Bundle.getMessage("TextSlotMonShowUnused"));
    private JCheckBoxMenuItem filterShowSystem = new JCheckBoxMenuItem(Bundle.getMessage("TextSlotMonShowSystem"));

    // Actions menu
    private JMenuItem actionRefreshAll = new JMenuItem(Bundle.getMessage("ButtonSlotRefresh"));
    private JMenuItem actionEStopAll = new JMenuItem(Bundle.getMessage("ButtonSlotMonEStopAll"));
    private JMenuItem actionSaveFiltersAndOptions = new JMenuItem(Bundle.getMessage("SlotMonSaveFiltersAndOptions"));
    private JMenuItem actionResetColumnsLayout = new JMenuItem(Bundle.getMessage("SlotMonResetColumnsLayout"));

    // popup menu
    private JPopupMenu popUp = new JPopupMenu();
    private JMenuItem popUpRefresh = new JMenuItem("Refresh");
    private int selectedRow = -1;

    private final String slotMonHideTopPanel = this.getClass().getName() + "SlotMonHideTopPanel"; // NOI18N
    private final String slotMonShowIdle = this.getClass().getName() + "SlotMonShowIdle"; // NOI18N
    private final String slotMonShowUnused = this.getClass().getName() + "SlotMonShowUnused"; // NOI18N
    private final String slotMonShowSystem = this.getClass().getName() + "SlotMonShowSystem"; // NOI18N

    private void restorePreferences() {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        optionHideTopPanel.setSelected(pm.getSimplePreferenceState(slotMonHideTopPanel));
        filterShowIdle.setSelected(pm.getSimplePreferenceState(slotMonShowIdle));
        filterShowUnUsed.setSelected(pm.getSimplePreferenceState(slotMonShowUnused));
        filterShowSystem.setSelected(pm.getSimplePreferenceState(slotMonShowSystem));
    }

    private void resetColumnLayout() {
        //slotTable.getColumnModel();
        slotTable.createDefaultColumnsFromModel();
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent( tpm ->
        tpm.cacheState(slotTable) );
    }

    private void savePreferences() {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        pm.setSimplePreferenceState(slotMonHideTopPanel, optionHideTopPanel.isSelected());
        pm.setSimplePreferenceState(slotMonShowIdle, filterShowIdle.isSelected());
        pm.setSimplePreferenceState(slotMonShowUnused, filterShowUnUsed.isSelected());
        pm.setSimplePreferenceState(slotMonShowSystem, filterShowSystem.isSelected());
    }

    public SlotMonPane() {
        super();
    }

    @Override
    public boolean isMultipleInstances() {
        return false;
    }

    private boolean runningFilterSystem = false;
    private boolean runningFilterUnUsed = false;

    @Override
    public void initComponents(jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        boolean hideColumnsF9toF28 = true; //(memo.getSlotManager().getLoconetProtocol() != LnConstants.LOCONETPROTOCOL_TWO);
        slotModel = new SlotMonDataModel(memo.getSlotManager().getNumSlots(), memo);
        slotTable = new JTable(slotModel);
        slotTable.setColumnModel(new XTableColumnModel());
        slotTable.createDefaultColumnsFromModel();
        //XTableColumnModel SlotMonDataModel = (XTableColumnModel)slotTable.getColumnModel();

        slotTable.setName(this.getTitle());

        sorter = new TableRowSorter<>(slotModel);
        slotTable.setRowSorter(sorter);
        slotScroll = new JScrollPane(slotTable);

        // configure items for GUI
        showUnusedCheckBox.setText(Bundle.getMessage("TextSlotMonShowUnused"));
        showUnusedCheckBox.setVisible(true);
        showUnusedCheckBox.setSelected(false);
        showUnusedCheckBox.setToolTipText(Bundle.getMessage("TooltipSlotMonShowUnused"));

        showSystemCheckBox.setText(Bundle.getMessage("TextSlotMonShowSystem"));
        showSystemCheckBox.setVisible(true);
        showSystemCheckBox.setSelected(false);
        showSystemCheckBox.setToolTipText(Bundle.getMessage("TooltipSlotMonShowSystem"));

        // allow reordering of the columns
        slotTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        slotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < slotTable.getColumnCount(); i++) {
            int width = slotModel.getPreferredWidth(i);
            slotTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        slotTable.sizeColumnsToFit(-1);

        // install a button renderer & editor in the "DISP" column for freeing a slot
        setColumnToHoldButton(slotTable, slotTable.convertColumnIndexToView(SlotMonDataModel.ColumnNumber.DISPCOLUMN.ordinal()));

        // install a button renderer & editor in the "ESTOP" column for stopping a loco
        setColumnToHoldEStopButton(slotTable, slotTable.convertColumnIndexToView(SlotMonDataModel.ColumnNumber.ESTOPCOLUMN.ordinal()));

        // Install a numeric format for ConsistAddress
        slotTable.getColumnModel().getColumn(SlotMonDataModel.ColumnNumber.CONSISTADDRESS.ordinal()).setCellRenderer(new TableRendererToBlankToNA());
        slotTable.getColumnModel().getColumn(slotTable.convertColumnIndexToView(SlotMonDataModel.ColumnNumber.JMRITHROTTLECOUNT.ordinal())).setCellRenderer(new TableRendererToBlankToNA());

        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            try {
                tpm.persist(slotTable, true);
            } catch (IllegalArgumentException Ex) {
                log.warn("SlotMon Can only save layout changes for second and subsequent invocations");
            }
        });

        // Hide functions F9 thru F28 if not used.
        if (hideColumnsF9toF28) {
            XTableColumnModel tcm = (XTableColumnModel) slotTable.getColumnModel();
            for (int ixCol = SlotMonDataModel.ColumnNumber.F9COLUMN.ordinal() ;
                    ixCol <= SlotMonDataModel.ColumnNumber.F28COLUMN.ordinal() ;
                    ixCol ++ ) {
                TableColumn tc = tcm.getColumnByModelIndex(ixCol);
                tcm.setColumnVisible(tc, false);
            }
        }

        ActionListener refreshListener = e -> {
            slotModel.refreshSlots();
        };
        // add listener object so checkboxes functio
        refreshAllButton.addActionListener(refreshListener);
        actionRefreshAll.addActionListener(refreshListener);

        ItemListener filterShowSystemListen = e -> {
            if (runningFilterSystem) {
                return;
            }
            runningFilterSystem = true;
            boolean state;
            if (e.getSource() instanceof JCheckBox) {
                state = ((JCheckBox) e.getSource()).isSelected();
            } else {
                state = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            }
            filterShowSystem.setSelected(state);
            showSystemCheckBox.setSelected(state);
            runningFilterSystem = false;
            filter();
        } ;
        showSystemCheckBox.addItemListener(filterShowSystemListen);
        filterShowSystem.addItemListener(filterShowSystemListen);

        ItemListener filterShowUnUsedListen = e -> {
            if (runningFilterUnUsed) {
                return;
            }
            runningFilterUnUsed = true;
            boolean state;
            if (e.getSource() instanceof JCheckBox) {
                state = ((JCheckBox) e.getSource()).isSelected();
            } else {
                state = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            }
            filterShowUnUsed.setSelected(state);
            showUnusedCheckBox.setSelected(state);
            runningFilterUnUsed = false;
            filter();
        };
        filterShowUnUsed.addItemListener(filterShowUnUsedListen);
        showUnusedCheckBox.addItemListener(filterShowUnUsedListen);

        // add listener object so stop all button functions
        optionHideTopPanel.addActionListener((ActionEvent e) -> {
            topPanel.setVisible(!optionHideTopPanel.getState());
        });

        estopAllButton.addActionListener((ActionEvent e) -> {
            slotModel.estopAll();
        });
        actionEStopAll.addActionListener((ActionEvent e) -> {
            slotModel.estopAll();
        });
        actionSaveFiltersAndOptions.addActionListener((ActionEvent e) -> {
            savePreferences();
        });
        actionResetColumnsLayout.addActionListener((ActionEvent e) -> {
            resetColumnLayout();
        });

        //Jeffrey 6/29/2013
        clearAllButton.addActionListener((ActionEvent e) -> {
            slotModel.clearAllSlots();
        });

        // adjust model to default settings
        filter();

        // This listener is needed when there are a large number of slots being updated
        //
        slotTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (slotTable.getSelectedRow() != -1) { // Ensure a row is actually selected
                    selectedRow = slotTable.getSelectedRow();
                    Object value = slotTable.getValueAt(selectedRow, 0);
                    log.debug("Selected Row:[{}] Value[{}]", selectedRow, value);
                }
            }
        });
        popUp.add(popUpRefresh);
        popUpRefresh.addActionListener((ActionEvent e) -> {
            int row = slotTable.getSelectedRow();
            if (row != -1) {
                memo.getSlotManager().sendReadSlot((int)slotTable.getValueAt(row, 0));
            } else {
                String response = jmri.util.swing.JmriJOptionPane.showInputDialog(this,
                        "Enter Slot", Integer.toString(selectedRow) );
                try {
                    int slotNo = Integer.parseInt(response) ;
                    memo.getSlotManager().sendReadSlot(slotNo);
                } catch (NumberFormatException ex) {
                    log.error("Non Number[{}], ignored",response);
                }
            }
        });
        slotTable.setComponentPopupMenu(popUp);

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        topPanel = new JPanel();
        topPanel.setLayout(new WrapLayout());

        topPanel.add(dcsCSLabel);
        dcsType.setEditable(false);
        topPanel.add(dcsType);
        topPanel.add(dcsSlotsLabel);
        dcsSlots.setEditable(false);
        topPanel.add(dcsSlots);
        showHideSlot250Data(false);
        topPanel.add(refreshAllButton);
        topPanel.add(showUnusedCheckBox);
        topPanel.add(showSystemCheckBox);
        topPanel.add(estopAllButton);
        topPanel.add(clearAllButton);
        topPanel.add(clearAllButton);
        topPanel.add(new JLabel(Bundle.getMessage("SlotMonUnUseCountLabel")));
        topPanel.add(inUseCount);

        add(topPanel);
        add(slotScroll);
 
        restorePreferences();

        addMouseListenerToHeader(slotTable);


        memo.getSlotManager().addSlotListener(this);

        // set top panel size
        if (topPanel.getMaximumSize().height > 0 && topPanel.getMaximumSize().width > 0) {
            topPanel.setMaximumSize(topPanel.getPreferredSize());
        }
        topPanel.setVisible(!optionHideTopPanel.getState());
    }

    void setColumnToHoldButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        // ensure the table rows, columns have enough room for buttons
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        slotTable.setDefaultEditor(JButton.class, buttonEditor);
        slotTable.setRowHeight(new JButton("  " + slotModel.getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
                .setPreferredWidth(new JButton("  " + slotModel.getValueAt(1, column)).getPreferredSize().width);
    }


    class TableRendererToBlankToNA extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if ((Integer) value == 0) {
                setText("");
            } else if ((Integer) value == -1) {
                setText(Bundle.getMessage("SlotDataNotApplicable"));
            } else {
                setText(Integer.toString((Integer) value));
            }
            return this;
        }
    }

    void setColumnToHoldEStopButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        // ensure the table rows, columns have enough room for buttons
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        slotTable.setDefaultEditor(JButton.class, buttonEditor);
        slotTable.setRowHeight(new JButton("  " + slotModel.getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
                .setPreferredWidth(new JButton("  " + slotModel.getValueAt(1, column)).getPreferredSize().width);
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.slotmon.SlotMonFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemSlotMonitor"));
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent( tpm ->
            tpm.stopPersisting(slotTable) );
        savePreferences();
        slotModel.dispose();
        slotModel = null;
        slotTable = null;
        slotScroll = null;
        super.dispose();
    }


    private void filter() {
        RowFilter<SlotMonDataModel, Integer> rf = new RowFilter<SlotMonDataModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends SlotMonDataModel, ? extends Integer> entry) {
                // default filter is IN-USE and regular systems slot
                // the default is whatever the person last closed it with
                jmri.jmrix.loconet.LocoNetSlot slot =  entry.getModel().getSlot(entry.getIdentifier());
                boolean include = entry.getModel().getSlot(entry.getIdentifier()).slotStatus() != LnConstants.LOCO_FREE
                        && slot.getSlotType() == SlotType.LOCO;
                if (slot.getSlotType() == SlotType.UNKNOWN) {
                    return false;        // dont ever show unknown
                }
                if (!include && showUnusedCheckBox.isSelected() && !slot.isSystemSlot()) {
                    include = true;
                }
                if (!include && showSystemCheckBox.isSelected() && slot.isSystemSlot()) {
                    include = true;
                }
                return include;
            }
        };
        sorter.setRowFilter(rf);
    }

    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        menuList.add(getFileMenu());
        menuList.add(getOptionsMenu());
        menuList.add(getFiltersMenu());
        menuList.add(getActionsMenu());
        return menuList;
    }

    private JMenu getOptionsMenu() {
        JMenu optionsMenu = new JMenu(Bundle.getMessage("MenuOptions"));
        optionsMenu.add(optionHideTopPanel);
        return optionsMenu;
    }

    private JMenu getFiltersMenu() {
        JMenu filtersMenu = new JMenu(Bundle.getMessage("MenuFilters"));
        filtersMenu.add(filterShowIdle);
        filtersMenu.add(filterShowSystem);
        filtersMenu.add(filterShowUnUsed);
        return filtersMenu;
    }

    private JMenu getActionsMenu() {
        JMenu actionsMenu = new JMenu(Bundle.getMessage("MenuActions"));
        actionsMenu.add(actionRefreshAll);
        actionsMenu.add(actionEStopAll);
        actionsMenu.add(actionResetColumnsLayout);
        actionsMenu.add(actionSaveFiltersAndOptions);
        return actionsMenu;
    }

    private JMenu getFileMenu(){
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile")); // NOI18N
        fileMenu.add(new JTableToCsvAction((Bundle.getMessage("ExportCsvAll")),
            null, slotModel, "Slot_Monitor_All.csv", new int[]{
            SlotMonDataModel.ColumnNumber.ESTOPCOLUMN.ordinal()})); // NOI18N
        fileMenu.add(new JTableToCsvAction((Bundle.getMessage("ExportCsvView")),
            slotTable, slotModel, "Slot_Monitor_View.csv", new int[]{
            SlotMonDataModel.ColumnNumber.ESTOPCOLUMN.ordinal()})); // NOI18N
     return fileMenu;
    }

    // methods to communicate with SlotManager
    @Override
    public synchronized void notifyChangedSlot(LocoNetSlot s) {
        // update model from this slot
        if (s.getSlot() == 250) {
            if (memo.getSlotManager().getSlot250CSSlots() > 0) {
                showHideSlot250Data(true);
                dcsSlots.setText(Integer.toString(memo.getSlotManager().getSlot250CSSlots()));
                dcsType.setText(memo.getSlotManager().getSlot248CommandStationType());

                // set scroll size
                if (topPanel.getMaximumSize().height > 0 && topPanel.getMaximumSize().width > 0) {
                    topPanel.setMaximumSize(topPanel.getPreferredSize());
                }
                topPanel.revalidate();

            }
        }
        
        // update count
        int inUseSlotCount = 0;
        for (var slot : memo.getSlotManager().getSlots()) {
            if (!slot.isSystemSlot() && slot.slotStatus() == LnConstants.LOCO_IN_USE) {
                inUseSlotCount++;
            }
        }
        inUseCount.setText(""+inUseSlotCount);
    }

    void showHideSlot250Data(boolean b) {
        dcsCSLabel.setVisible(b);
        dcsSlots.setVisible(b);
        dcsSlotsLabel.setVisible(b);
        dcsType.setVisible(b);
        // set scroll size
        if (topPanel.getMaximumSize().height > 0 && topPanel.getMaximumSize().width > 0) {
            topPanel.setMaximumSize(topPanel.getPreferredSize());
        }
        topPanel.revalidate();
    }

    /*
     * Mouse popup stuff
     */

    /**
     * Process the column header click
     * @param e     the evnt data
     * @param table the JTable
     */
    protected void showTableHeaderPopup(JmriMouseEvent e, JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(false); i++) {
            TableColumn tc = tcm.getColumnByModelIndex(i);
            String columnName = table.getModel().getColumnName(i);
            if (columnName != null && !columnName.equals("")) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(table.getModel().getColumnName(i), tcm.isColumnVisible(tc));
                menuItem.addActionListener(new HeaderActionListener(tc, tcm));
                popupMenu.add(menuItem);
            }
        }
        MenuScroller.setScrollerFor(popupMenu,10,200,0,0);
        popupMenu.show(e.getComponent(), e.getX(), e.getY());

    }

    /**
     * Adds the column header pop listener to a JTable using XTableColumnModel
     * @param table The JTable effected.
     */
    protected void addMouseListenerToHeader(JTable table) {
        JmriMouseListener mouseHeaderListener = new TableHeaderListener(table);
        table.getTableHeader().addMouseListener(JmriMouseListener.adapt(mouseHeaderListener));
    }

    protected static class HeaderActionListener implements ActionListener {

        TableColumn tc;
        XTableColumnModel tcm;

        HeaderActionListener(TableColumn tc, XTableColumnModel tcm) {
            this.tc = tc;
            this.tcm = tcm;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && tcm.getColumnCount(true) == 1) {
                return;
            }
            tcm.setColumnVisible(tc, check.isSelected());
        }
    }

    /**
     * Class to support Columnheader popup menu on XTableColum model.
     */
    class TableHeaderListener extends JmriMouseAdapter {

        JTable table;

        TableHeaderListener(JTable tbl) {
            super();
            table = tbl;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlotMonPane.class);

}
