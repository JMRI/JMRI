package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.swing.CbusCommonSwing;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.swing.StayOpenCheckBoxItem;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.util.table.JTableWithColumnToolTips;

/**
 * Pane for monitoring and configuring a MERG CBUS Command Station.
 *
 * @author Steve Young Copyright (C) 2018
 * @since 4.13.4
 */
public class CbusSlotMonitorPane extends jmri.jmrix.can.swing.CanPanel {

    protected CbusSlotMonitorDataModel slotModel;
    private JTable slotTable;
    private final XTableColumnModel tcm = new XTableColumnModel();
    private final JMenu colMenu = new JMenu((Bundle.getMessage("SessCol")));

    // private JMenu cancmdMenu = new JMenu("CANCMD Setup");

    public CbusSlotMonitorPane() {
        super();
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        slotModel = memo.get(CbusSlotMonitorDataModel.class);
        slotTable = new JTableWithColumnToolTips(slotModel,CbusSlotMonitorDataModel.CBUSSLOTMONTOOLTIPS);
        init();
    }

    public void init() {

        // Use XTableColumnModel so we can control which columns are visible
        slotTable.setColumnModel(tcm);

        setupColumnsMenuLinks();

        final TableRowSorter<CbusSlotMonitorDataModel> sorter = new TableRowSorter<>(slotModel);
        slotTable.setRowSorter(sorter);

        setCellRenderers();

        JScrollPane slotScroll = new JScrollPane(slotTable);
        slotScroll.setPreferredSize(new Dimension(400, 200));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JScrollPane scrolltablefeedback = new JScrollPane (slotModel.tablefeedback());
        scrolltablefeedback.setMinimumSize(new Dimension(150, 20));

        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        toppanelcontainer.add(getStopButton());
        toppanelcontainer.add(new LargePowerManagerButton(true));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            slotScroll, scrolltablefeedback);
        split.setResizeWeight(0.95d);
        split.setContinuousLayout(true);

        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());

        p1.add(toppanelcontainer, BorderLayout.PAGE_START);
        p1.add(split, BorderLayout.CENTER);        
        add(p1);

        p1.setMinimumSize(new Dimension(450, 200));
        p1.setVisible(true);
    }

    private void setCellRenderers(){
        for (int i = 0; i < CbusSlotMonitorDataModel.MAX_COLUMN; i++) {
            TableColumn col = tcm.getColumnByModelIndex(i);
            switch (i) {
                case CbusSlotMonitorDataModel.LOCO_ID_LONG_COLUMN:
                    break; // use default CellRenderer for boolean values
                case CbusSlotMonitorDataModel.ESTOP_COLUMN:
                    col.setMinWidth(55);
                    col.setCellRenderer( new ButtonRenderer() );
                    col.setCellEditor( new ButtonEditor( new JButton() ) );
                    break;
                case CbusSlotMonitorDataModel.KILL_SESSION_COLUMN:
                case CbusSlotMonitorDataModel.LAUNCH_THROTTLE:
                    col.setCellRenderer( new ButtonRenderer() );
                    col.setCellEditor( new ButtonEditor( new JButton() ) );
                    break;
                default:
                    col.setCellRenderer( getRenderer());
                    break;
            }
        }
    }

    private JButton getStopButton(){
        JButton estopButton = new JButton("Stop All");
        estopButton.setIcon(new NamedIcon("resources/icons/throttles/estop.png", "resources/icons/throttles/estop.png"));
        estopButton.setToolTipText(Bundle.getMessage("ThrottleToolBarStopAllToolTip"));
        estopButton.addActionListener((ActionEvent e) -> slotModel.sendcbusestop() );
        return estopButton;
    }

    private void setupColumnsMenuLinks() {

        // configure items for GUI
        CbusCommonSwing.configureTable(slotTable);
        slotTable.setName("CbusSlotMonitorPane.5.9.7"); // to reset UI xml when table format changes

        StayOpenCheckBoxItem[] cbArray = new StayOpenCheckBoxItem[slotModel.getColumnCount()];

        // initialise and set default column visibiity
        for (int i = 0; i < slotModel.getColumnCount(); i++) {
            StayOpenCheckBoxItem cbi = new StayOpenCheckBoxItem(slotModel.getColumnName(i));
            cbArray[i] = cbi;
            TableColumn column  = tcm.getColumnByModelIndex(i);
            cbi.addActionListener((ActionEvent e) ->
                tcm.setColumnVisible(column, cbi.isSelected()));
            final int ii = i;
            tcm.setColumnVisible(tcm.getColumnByModelIndex(i),
                Arrays.stream(CbusSlotMonitorDataModel.CBUSSLOTMONINITIALCOLS).anyMatch(j -> j == ii)
                );

        }

        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent( tpm ->
            tpm.persist(slotTable, true));

        for (int i = 0; i < slotModel.getColumnCount(); i++) {
            cbArray[i].setSelected(tcm.isColumnVisible(tcm.getColumnByModelIndex(i)));
            colMenu.add(cbArray[i]); // count columns
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("MenuItemCbusSlotMonitor"));
    }

    /**
     * Creates a Menu List.
     * {@inheritDoc}
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        menuList.add(colMenu);
        return menuList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane";
    }    

    private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
            JTextField f = new JTextField();

            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object arg1, boolean isSelected, boolean hasFocus,
                int row, int col) {

                if(arg1 != null){
                    String string = arg1.toString();
                    f.setText(string);
                    f.setHorizontalAlignment(JTextField.CENTER);

                } else {
                    f.setText("");
                }

                CbusCommonSwing.setCellBackground(isSelected, f, table,row);
                CbusCommonSwing.setCellFocus(hasFocus, f, table);
                return f;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent( tpm ->
            tpm.stopPersisting(slotTable) );
        slotTable = null;
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemCbusSlotMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusSlotMonitorPane.class.getName(),
                    InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusSlotMonitorPane.class);

}
