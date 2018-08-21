package jmri.jmrix.loconet.slotmon;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Frame providing a command station slot manager.
 * <p>
 * Slots 102 through 127 are normally not used for loco control, so are shown
 * separately.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SlotMonPane extends jmri.jmrix.loconet.swing.LnPanel {

    /**
     * Controls whether not-in-use slots are shown
     */
    protected final JCheckBox showUnusedCheckBox = new JCheckBox();
    /**
     * Controls individual statuses to be displayed
     */
    protected final JCheckBox showCommonCheckBox = new JCheckBox();
    protected final JCheckBox showInUseCheckBox = new JCheckBox();
    protected final JCheckBox showIdleCheckBox = new JCheckBox();
    protected final JCheckBox showFreeCheckBoxe = new JCheckBox();
   /**
     * Controls whether system slots (0, 121-127) are shown
     */
    protected final JCheckBox showSystemCheckBox = new JCheckBox();

    private final JButton estopAllButton = new JButton(Bundle.getMessage("ButtonSlotMonEStopAll"));

    //Added by Jeffrey Machacek 2013
    private final JButton clearAllButton = new JButton(Bundle.getMessage("ButtonSlotMonClearAll"));
    private final JButton refreshAllButton = new JButton(Bundle.getMessage("ButtonSlotRefresh"));

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

        slotModel = new SlotMonDataModel(memo.getSlotManager().getNumSlots(), 39, memo);
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

        showCommonCheckBox.setText(Bundle.getMessage("TextSlotMonShowCommon"));
        showCommonCheckBox.setVisible(true);
        showCommonCheckBox.setSelected(false);
        showCommonCheckBox.setToolTipText(Bundle.getMessage("TooltipSlotMonShowCommon"));

        showIdleCheckBox.setText(Bundle.getMessage("TextSlotMonShowIdle"));
        showIdleCheckBox.setVisible(true);
        showIdleCheckBox.setSelected(false);
        showIdleCheckBox.setToolTipText(Bundle.getMessage("TooltipSlotMonShowIdle"));

        showFreeCheckBoxe.setText(Bundle.getMessage("TextSlotMonShowFree"));
        showFreeCheckBoxe.setVisible(true);
        showFreeCheckBoxe.setSelected(false);
        showFreeCheckBoxe.setToolTipText(Bundle.getMessage("TooltipSlotMonShowFree"));

        showInUseCheckBox.setText(Bundle.getMessage("TextSlotMonShowInUse"));
        showInUseCheckBox.setVisible(true);
        showInUseCheckBox.setSelected(true);
        showInUseCheckBox.setToolTipText(Bundle.getMessage("TooltipSlotMonShowInUse"));

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

        // add listener object so checkboxes function
        showUnusedCheckBox.addActionListener((ActionEvent e) -> {
            filter();
        });
        showSystemCheckBox.addActionListener((ActionEvent e) -> {
            filter();
        });
        showCommonCheckBox.addActionListener((ActionEvent e) -> {
            filter();
        });
        showInUseCheckBox.addActionListener((ActionEvent e) -> {
            filter();
        });
        showIdleCheckBox.addActionListener((ActionEvent e) -> {
            filter();
        });
        showFreeCheckBoxe.addActionListener((ActionEvent e) -> {
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

        refreshAllButton.addActionListener((ActionEvent e) -> {
            slotModel.refreshSlots();
        });

        // adjust model to default settings
        filter();

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(showUnusedCheckBox);
        pane1.add(showCommonCheckBox);
        pane1.add(showFreeCheckBoxe);
        pane1.add(showIdleCheckBox);
        pane1.add(showInUseCheckBox);
        pane1.add(showSystemCheckBox);
        pane1.add(estopAllButton);
        pane1.add(clearAllButton);
        pane1.add(refreshAllButton);

        add(pane1);
        add(slotScroll);

        // set scroll size
        //pane1.setMaximumSize(new java.awt.Dimension(100,300));
        if (pane1.getMaximumSize().height > 0 && pane1.getMaximumSize().width > 0) {
            pane1.setMaximumSize(pane1.getPreferredSize());
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
                boolean include = false;
                if (!include && showUnusedCheckBox.isSelected() && !slot.isSystemSlot()) {
                    include = true;
                }
                if (!include && showInUseCheckBox.isSelected() && slot.slotStatus() == LnConstants.LOCO_IN_USE && !slot.isSystemSlot()) {
                    include = true;
                }
                if (!include && showCommonCheckBox.isSelected() && slot.slotStatus() == LnConstants.LOCO_COMMON && !slot.isSystemSlot()) {
                    include = true;
                }
                if (!include && showIdleCheckBox.isSelected()  && slot.slotStatus() == LnConstants.LOCO_IDLE && !slot.isSystemSlot()) {
                    include = true;
                }
                if (!include && showFreeCheckBoxe.isSelected()  && slot.slotStatus() == LnConstants.LOCO_FREE && !slot.isSystemSlot()) {
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

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemSlotMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    SlotMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

}
