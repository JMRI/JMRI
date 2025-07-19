package jmri.jmrix.loconet.slotmon;

import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.SlotMapEntry.SlotType;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.swing.WrapLayout;
import jmri.util.table.*;

/**
 * Frame providing a command station slot manager.
 * <p>
 * Slots 102 through 127 are normally not used for loco control, so are shown
 * separately.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SlotMonPane extends jmri.jmrix.loconet.swing.LnPanel implements SlotListener  {

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

    //Added by Jeffrey Machacek 2013
    private final JButton clearAllButton = new JButton(Bundle.getMessage("ButtonSlotMonClearAll"));
    private final JButton refreshAllButton = new JButton(Bundle.getMessage("ButtonSlotRefresh"));

    private JPanel topPanel;  // the panel across the top that holds buttons

    private SlotMonDataModel slotModel;
    private JTable slotTable;
    private JScrollPane slotScroll;
    private transient TableRowSorter<SlotMonDataModel> sorter;

    public SlotMonPane() {
        super();
    }

    @Override
    public void initComponents(jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        int columns = 40;
        if (memo.getSlotManager().getLoconetProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
            columns=20;
        }
        slotModel = new SlotMonDataModel(memo.getSlotManager().getNumSlots(), columns, memo);
        slotTable = new JTable(slotModel);
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

        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            // unable to persist because Default class provides no mechanism to
            // ensure window is destroyed when closed or that existing window is
            // reused when hidden and user reopens it from menu
            // tpm.persist(slotTable, true);
        });

        // install a button renderer & editor in the "DISP" column for freeing a slot
        setColumnToHoldButton(slotTable, slotTable.convertColumnIndexToView(SlotMonDataModel.DISPCOLUMN));

        // install a button renderer & editor in the "ESTOP" column for stopping a loco
        setColumnToHoldEStopButton(slotTable, slotTable.convertColumnIndexToView(SlotMonDataModel.ESTOPCOLUMN));

        // Install a numeric format for ConsistAddress
        setColumnForBlankWhenZero(slotTable, slotTable.convertColumnIndexToView(SlotMonDataModel.CONSISTADDRESS));
        // add listener object so checkboxes function

        refreshAllButton.addActionListener((ActionEvent e) -> {
            slotModel.refreshSlots();
        });

        showUnusedCheckBox.addActionListener((ActionEvent e) -> {
            filter();
        });
        showSystemCheckBox.addActionListener((ActionEvent e) -> {
            filter();
        });

        // add listener object so stop all button functions
        estopAllButton.addActionListener((ActionEvent e) -> {
            slotModel.estopAll();
        });

        //Jeffrey 6/29/2013
        clearAllButton.addActionListener((ActionEvent e) -> {
            slotModel.clearAllSlots();
        });

        // adjust model to default settings
        filter();

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

        add(topPanel);
        add(slotScroll);

        memo.getSlotManager().addSlotListener(this);

        // set top panel size
        if (topPanel.getMaximumSize().height > 0 && topPanel.getMaximumSize().width > 0) {
            topPanel.setMaximumSize(topPanel.getPreferredSize());
        }
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

    /*
     * Helper class to format number and optionally make blank when zero
     */
    private static class NumberFormatRenderer extends DefaultTableCellRenderer
    {
        public NumberFormatRenderer(String pattern, boolean suppressZero) {
            super();
            this.pattern = pattern;
            this.suppressZero = suppressZero;
            setHorizontalAlignment(JLabel.RIGHT);
        }
        @Override
        public void setValue(Object value)
        {
            try
            {
                if (value != null && value instanceof Number) {
                    if (suppressZero && ((Number) value).doubleValue() == 0.0 ) {
                        value = "";
                    }
                    NumberFormat formatter = new DecimalFormat(pattern);
                    value = formatter.format(value);
                }
            }
            catch(IllegalArgumentException e) {}
            super.setValue(value);
        }
        private String pattern;
        private boolean suppressZero;
    }

    void setColumnForBlankWhenZero(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        TableCellRenderer renderer = new NumberFormatRenderer("####",true);
        tcm.getColumn(column).setCellRenderer(renderer);
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
        return menuList;
    }

    private JMenu getFileMenu(){
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile")); // NOI18N
        fileMenu.add(new JTableToCsvAction((Bundle.getMessage("ExportCsvAll")),
            null, slotModel, "Slot_Monitor_All.csv", new int[]{
            SlotMonDataModel.ESTOPCOLUMN})); // NOI18N
        fileMenu.add(new JTableToCsvAction((Bundle.getMessage("ExportCsvView")),
            slotTable, slotModel, "Slot_Monitor_View.csv", new int[]{
            SlotMonDataModel.ESTOPCOLUMN})); // NOI18N
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


}
