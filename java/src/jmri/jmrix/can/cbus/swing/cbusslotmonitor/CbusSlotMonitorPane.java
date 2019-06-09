package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.UIManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*Created with Notepad++*/
/**
 * Pane for monitoring and configuring a MERG CBUS Command Station.
 *
 * @author Steve Young Copyright (C) 2018
 * @since 4.13.4
 */
public class CbusSlotMonitorPane extends jmri.jmrix.can.swing.CanPanel {

    private JScrollPane scrolltablefeedback;
    private JSplitPane split;
    private double _splitratio = 0.95;
    protected JScrollPane slotScroll;
    
    protected CbusSlotMonitorDataModel slotModel=null;
    protected JTable slotTable=null;
    protected final XTableColumnModel tcm = new XTableColumnModel();
    private JMenu colMenu = new JMenu((Bundle.getMessage("SessCol")));
    
    // private JMenu cancmdMenu = new JMenu("CANCMD Setup");
    protected List<JCheckBoxMenuItem> colMenuList = new ArrayList<JCheckBoxMenuItem>();
    
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        slotModel = new CbusSlotMonitorDataModel(memo, 5,
            CbusSlotMonitorDataModel.MAX_COLUMN); // controller, row, column
        init();
    }

    public void init() {
        JTable slotTable = new JTable(slotModel) {
            // Override JTable Header to implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        try {
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return CbusSlotMonitorDataModel.columnToolTips[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };        
        
        // Use XTableColumnModel so we can control which columns are visible
        slotTable.setColumnModel(tcm);
        slotTable.createDefaultColumnsFromModel();
        
        for (int i = 0; i < slotTable.getColumnCount(); i++) {
            int colnumber=i;
            String colName = slotTable.getColumnName(colnumber);
            StayOpenCBItem showcol = new StayOpenCBItem(colName);
            colMenuList.add(showcol);
            if (colnumber<10) {
                colMenu.add(showcol); // session columnds
            }
            
        }

        for (int i = 0; i < CbusSlotMonitorDataModel.MAX_COLUMN; i++) {
            int colnumber=i;
                TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                
            if (Arrays.stream(CbusSlotMonitorDataModel.startupColumns).anyMatch(j -> j == colnumber)) {
                colMenuList.get(colnumber).setSelected(true);
                tcm.setColumnVisible(column, true);
            } else {
                colMenuList.get(colnumber).setSelected(false);
                tcm.setColumnVisible(column, false);
            }
        
            colMenuList.get(colnumber).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                    boolean     visible = tcm.isColumnVisible(column);
                    tcm.setColumnVisible(column, !visible);
                }
            });
        }
        
        slotTable.setAutoCreateRowSorter(true);
        
        final TableRowSorter<CbusSlotMonitorDataModel> sorter = new TableRowSorter<CbusSlotMonitorDataModel>(slotModel);
        slotTable.setRowSorter(sorter);
        slotTable.setRowHeight(26); // to match estop button icon size
        
        // configure items for GUI
        slotModel.configureTable(slotTable);
        
        TableColumn estopColumn = tcm.getColumnByModelIndex(CbusSlotMonitorDataModel.ESTOP_COLUMN);                
        estopColumn.setMinWidth(60);
        estopColumn.setCellRenderer( new ButtonRenderer() );
        estopColumn.setCellEditor( new ButtonEditor( new JButton() ) );    

        slotScroll = new JScrollPane(slotTable);
        slotScroll.setPreferredSize(new Dimension(400, 200));
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        // p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setLayout(new BorderLayout());
        // p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutEvents")));
        
        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        scrolltablefeedback = new JScrollPane (slotModel.tablefeedback());
        
        Dimension scrolltablefeedbackminimumSize = new Dimension(150, 20);
        scrolltablefeedback.setMinimumSize(scrolltablefeedbackminimumSize);
        
        JButton estopButton = new JButton("Stop All");
        estopButton.setIcon(new NamedIcon("resources/icons/throttles/estop.png", "resources/icons/throttles/estop.png"));
        estopButton.setToolTipText(("ThrottleToolBarStopAllToolTip"));
       // estopButton.setVerticalTextPosition(JButton.BOTTOM);
       // estopButton.setHorizontalTextPosition(JButton.CENTER);
        estopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slotModel.sendcbusestop();
            }
        });
        
        toppanelcontainer.add(estopButton);
        toppanelcontainer.add(new LargePowerManagerButton(true));
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            slotScroll, scrolltablefeedback);
        split.setResizeWeight(_splitratio);
        split.setContinuousLayout(true);

        p1.add(toppanelcontainer, BorderLayout.PAGE_START);
        p1.add(split, BorderLayout.CENTER);        
        add(p1);
        
        Dimension p1size = new Dimension(450, 200);
        p1.setMinimumSize(p1size);
        
        p1.setVisible(true);
        log.debug("class name {}", CbusSlotMonitorPane.class.getName());
    }
    
    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemCbusSlotMonitor"));
        }
        return Bundle.getMessage("MenuItemCbusSlotMonitor");
    }

    public CbusSlotMonitorPane() {
        super();
    }
    
    /**
     * Creates a Menu List
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        menuList.add(colMenu);
        return menuList;
    }

    /**
     * Checkbox item which does not appear to close the menu pane when clicked
     */  
    public static class StayOpenCBItem extends JCheckBoxMenuItem {
    
        private MenuElement[] path;
        {
            getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (getModel().isArmed() && isShowing()) {
                        path = MenuSelectionManager.defaultManager().getSelectedPath();
                    }
                }
            });
        }
    
        public StayOpenCBItem(String text) {
            super(text);
        }
    
        @Override
        public void doClick(int pressTime) {
            super.doClick(pressTime);
            MenuSelectionManager.defaultManager().setSelectedPath(path);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane";
    }    
    
    @Override
    public void dispose() {
        slotTable = null;
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

    private static final Logger log = LoggerFactory.getLogger(CbusSlotMonitorPane.class);

}
