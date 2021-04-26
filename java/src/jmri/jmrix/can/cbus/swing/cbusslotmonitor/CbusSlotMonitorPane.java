package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
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

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane for monitoring and configuring a MERG CBUS Command Station.
 *
 * @author Steve Young Copyright (C) 2018
 * @since 4.13.4
 */
public class CbusSlotMonitorPane extends jmri.jmrix.can.swing.CanPanel {

    private JScrollPane scrolltablefeedback;
    private JSplitPane split;
    private final double _splitratio = 0.95;
    private JScrollPane slotScroll;
    
    protected CbusSlotMonitorDataModel slotModel;
    private JTable _slotTable;
    private final XTableColumnModel tcm = new XTableColumnModel();
    private final JMenu colMenu = new JMenu((Bundle.getMessage("SessCol")));
    
    // private JMenu cancmdMenu = new JMenu("CANCMD Setup");
    
    public CbusSlotMonitorPane() {
        super();
    }
    
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        slotModel = new CbusSlotMonitorDataModel(memo, 5,
            CbusSlotMonitorDataModel.MAX_COLUMN); // controller, row, column
        
        _slotTable = new JTableWithColumnToolTips(slotModel,CbusSlotMonitorDataModel.CBUSSLOTMONTOOLTIPS);
        init();
    }

    public void init() {
        
        // Use XTableColumnModel so we can control which columns are visible
        _slotTable.setColumnModel(tcm);
        
        setupColumnsMenuLinks();
        
        final TableRowSorter<CbusSlotMonitorDataModel> sorter = new TableRowSorter<>(slotModel);
        _slotTable.setRowSorter(sorter);
        
        TableColumn estopColumn = tcm.getColumnByModelIndex(CbusSlotMonitorDataModel.ESTOP_COLUMN);                
        estopColumn.setMinWidth(60);
        estopColumn.setCellRenderer( new ButtonRenderer() );
        estopColumn.setCellEditor( new ButtonEditor( new JButton() ) );    

        slotScroll = new JScrollPane(_slotTable);
        slotScroll.setPreferredSize(new Dimension(400, 200));
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        scrolltablefeedback = new JScrollPane (slotModel.tablefeedback());
        scrolltablefeedback.setMinimumSize(new Dimension(150, 20));
        
        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        toppanelcontainer.add(getStopButton());
        toppanelcontainer.add(new LargePowerManagerButton(true));
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            slotScroll, scrolltablefeedback);
        split.setResizeWeight(_splitratio);
        split.setContinuousLayout(true);

        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
        
        p1.add(toppanelcontainer, BorderLayout.PAGE_START);
        p1.add(split, BorderLayout.CENTER);        
        add(p1);
        
        p1.setMinimumSize(new Dimension(450, 200));
        p1.setVisible(true);
    }
    
    private JButton getStopButton(){
        JButton estopButton = new JButton("Stop All");
        estopButton.setIcon(new NamedIcon("resources/icons/throttles/estop.png", "resources/icons/throttles/estop.png"));
        estopButton.setToolTipText(Bundle.getMessage("ThrottleToolBarStopAllToolTip"));
        estopButton.addActionListener((ActionEvent e) -> {
            slotModel.sendcbusestop();
        });
        return estopButton;
    }
    
    private void setupColumnsMenuLinks() {
        
        // configure items for GUI
        CbusCommonSwing.configureTable(_slotTable);
        
        StayOpenCheckBoxItem[] cbArray = new StayOpenCheckBoxItem[slotModel.getColumnCount()];
        
        // initialise and set default column visibiity
        for (int i = 0; i < slotModel.getColumnCount(); i++) {
            StayOpenCheckBoxItem cbi = new StayOpenCheckBoxItem(slotModel.getColumnName(i));
            cbArray[i] = cbi;
            TableColumn column  = tcm.getColumnByModelIndex(i);
            cbi.addActionListener((ActionEvent e) -> {
                tcm.setColumnVisible(column, cbi.isSelected());
            });
            final int ii = i;
            tcm.setColumnVisible(tcm.getColumnByModelIndex(i),
                Arrays.stream(CbusSlotMonitorDataModel.CBUSSLOTMONINITIALCOLS).anyMatch(j -> j == ii)
                );
            
        }
        
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            tpm.persist(_slotTable, true);
        });
        
        
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            tpm.stopPersisting(_slotTable);
        });
        _slotTable = null;
        slotModel.dispose();
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemCbusSlotMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusSlotMonitorPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusSlotMonitorPane.class);

}
