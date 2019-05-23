package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.Element;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a Cbus event request table.
 *
 * @author Steve Young (C) 2019
 * @see CbusEventRequestDataModel
 *
 * @since 4.15.3
 */
public class CbusEventRequestTablePane extends jmri.jmrix.can.swing.CanPanel implements TableModelListener {

    protected CbusEventRequestDataModel eventModel=null;
    protected JTable eventTable=null;
    protected JScrollPane eventScroll;
    protected JSplitPane split;
    protected JPanel pane1;
    protected JPanel toppanelcontainer;
    protected JPanel neweventcontainer = new JPanel();
    private JSpinner newnodenumberSpinner;
    private JSpinner newevnumberSpinner;
    private JButton newevbutton;
    public String currentRowCount;
    protected JPanel filterpanel = new JPanel();
    
    private double _splitratio = 0.95;

    private JScrollPane scrolltablefeedback;
    private static DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);
    public static final Color GOLD = new Color(255,204,51);
    private JMenu evColMenu = new JMenu(Bundle.getMessage("evColMenuName"));
    private JMenu evFbMenu = new JMenu(Bundle.getMessage("evFbMenuName"));

    private List<JCheckBoxMenuItem> colMenuList = new ArrayList<JCheckBoxMenuItem>();

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        eventModel = new CbusEventRequestDataModel(memo, 10,
                CbusEventRequestDataModel.MAX_COLUMN); // controller, row, column
        init();
    }

    public CbusEventRequestTablePane() {
        super();
    }

    public void init() {
        
        JTable eventTable = new JTable(eventModel) {
            // Override JTable Header to implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        try {
                           // log.debug("131 gettttext");
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return CbusEventRequestDataModel.columnToolTips[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };

        // Use XTableColumnModel so we can control which columns are visible
        final  XTableColumnModel tcm = new XTableColumnModel();
        eventTable.setColumnModel(tcm);
        eventTable.createDefaultColumnsFromModel();
        
        eventTable.setAutoCreateRowSorter(true);
        
        final TableRowSorter<CbusEventRequestDataModel> sorter = new TableRowSorter<CbusEventRequestDataModel>(eventModel);
        eventTable.setRowSorter(sorter);
        
        scrolltablefeedback = new JScrollPane (eventModel.tablefeedback());
        
        eventTable.setRowSelectionAllowed(true);
        eventTable.setColumnSelectionAllowed(false);
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        tcm.getColumn(CbusEventRequestDataModel.FEEDBACKREQUIRED_COLUMN).setCellRenderer(new OsRenderer());        
        tcm.getColumn(CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN).setCellRenderer(new OsRenderer());
        tcm.getColumn(CbusEventRequestDataModel.FEEDBACKEVENT_COLUMN).setCellRenderer(new OsRenderer());        
        tcm.getColumn(CbusEventRequestDataModel.FEEDBACKNODE_COLUMN).setCellRenderer(new OsRenderer());
        tcm.getColumn(CbusEventRequestDataModel.FEEDBACKTIMEOUT_COLUMN).setCellRenderer(new OsRenderer());
        tcm.getColumn(CbusEventRequestDataModel.LASTFEEDBACK_COLUMN).setCellRenderer(new LafbRenderer());
        tcm.getColumn(CbusEventRequestDataModel.LATEST_TIMESTAMP_COLUMN).setCellRenderer(new TimeStampRenderer());
        
        TableColumn delBColumn = tcm.getColumn(CbusEventRequestDataModel.DELETE_BUTTON_COLUMN);
        delBColumn.setCellEditor(new ButtonEditor(new JButton()));
        delBColumn.setCellRenderer(new ButtonRenderer());
        
        TableColumn rqStatColumn = tcm.getColumn(CbusEventRequestDataModel.STATUS_REQUEST_BUTTON_COLUMN);
        rqStatColumn.setCellEditor(new ButtonEditor(new JButton()));
        rqStatColumn.setCellRenderer(new ButtonRenderer());   

        // configure items for GUI
        eventModel.configureTable(eventTable);

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            int colnumber=i;
            
            String colName = eventTable.getColumnName(colnumber);
            StayOpenCBItem showcol = new StayOpenCBItem(colName);
            colMenuList.add(showcol);
            showcol.setToolTipText(CbusEventRequestDataModel.columnToolTips[i]);
            showcol.setSelected(true);
            if (
                colnumber == CbusEventRequestDataModel.NAME_COLUMN ||
                colnumber == CbusEventRequestDataModel.NODE_COLUMN ||
                colnumber == CbusEventRequestDataModel.EVENT_COLUMN ||
                colnumber==CbusEventRequestDataModel.LATEST_TIMESTAMP_COLUMN ||
                colnumber == CbusEventRequestDataModel.DELETE_BUTTON_COLUMN
            ) {
                evColMenu.add(showcol); // event columns
            }
            else if (
                colnumber==CbusEventRequestDataModel.STATUS_REQUEST_BUTTON_COLUMN ||
                colnumber==CbusEventRequestDataModel.FEEDBACKREQUIRED_COLUMN ||
                colnumber==CbusEventRequestDataModel.LASTFEEDBACK_COLUMN ||
                colnumber==CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN ||
                colnumber==CbusEventRequestDataModel.FEEDBACKTIMEOUT_COLUMN ||
                colnumber==CbusEventRequestDataModel.FEEDBACKEVENT_COLUMN ||
                colnumber==CbusEventRequestDataModel.FEEDBACKNODE_COLUMN
            ){ 
                evFbMenu.add(showcol); // feedback columns
            }
            else {
                log.warn("No menuitem defined for {}",colnumber);
            }
            
            showcol.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                    boolean     visible = tcm.isColumnVisible(column);
                    tcm.setColumnVisible(column, !visible);
                }
            });
        }
                
        eventModel.addTableModelListener(this);
        	
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
        // main pane
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BorderLayout());
        
        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        // scroller for main table
        eventScroll = new JScrollPane(eventTable);
        eventScroll.setPreferredSize(new Dimension(450, 200));

        // add new event
        neweventcontainer.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), (Bundle.getMessage("NewEvent"))));
        
        JPanel newnode = new JPanel();
        JPanel newev = new JPanel();

        newnode.add(new JLabel(Bundle.getMessage("CbusNode")));
        newnodenumberSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        JComponent comp = newnodenumberSpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        newnodenumberSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkNewevent();
            }
        });
        newnode.add(newnodenumberSpinner);
        newnode.setToolTipText(Bundle.getMessage("NewNodeTip"));
        newnodenumberSpinner.setToolTipText(Bundle.getMessage("NewNodeTip"));
        
        newev.add(new JLabel(Bundle.getMessage("CbusEvent")));
        newevnumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 65535, 1));
        JComponent compe = newevnumberSpinner.getEditor();
        JFormattedTextField fielde = (JFormattedTextField) compe.getComponent(0);
        DefaultFormatter formattere = (DefaultFormatter) fielde.getFormatter();
        formattere.setCommitsOnValidEdit(true);
        newevnumberSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkNewevent();
            }
        });
        
        newev.add(newevnumberSpinner);
        
        newevbutton = new JButton((Bundle.getMessage("NewEvent")));
        
        ActionListener newEventaction = ae -> {
            int ev = (Integer) newevnumberSpinner.getValue();
            int nd = (Integer) newnodenumberSpinner.getValue();
            
            int existingRow = eventModel.eventRow(nd,ev);
            if ( existingRow < 0 ) {
                eventModel.addEvent(nd,ev,CbusEventRequestMonitorEvent.EvState.UNKNOWN,null);
            }
        };
        
        newevbutton.addActionListener(newEventaction);
        
        neweventcontainer.add(newnode);
        neweventcontainer.add(newev);
        neweventcontainer.add(newevbutton);
        
        toppanelcontainer.add(neweventcontainer);
        pane1.add(toppanelcontainer, BorderLayout.PAGE_START);
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            eventScroll, scrolltablefeedback);
        split.setResizeWeight(_splitratio);
        split.setContinuousLayout(true);

        pane1.add(split, BorderLayout.CENTER);
        
        add(pane1);
        pane1.setVisible(true);
    }
    
    private void checkNewevent() {
        int newno = (Integer) newnodenumberSpinner.getValue();
        int newev = (Integer) newevnumberSpinner.getValue();
        
        if (eventModel.eventRow( newno, newev ) > -1 ) {
            newevbutton.setEnabled(false);
        } else {
            newevbutton.setEnabled(true);
        }
    }
    
    @Override
    public void tableChanged(TableModelEvent e) {
        checkNewevent();
    }
  
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.eventrequestmonitor.CbusEventRequestTablePane";
    }
    
    /**
     * Creates a Menu List
     * <p>
     * File - Print, Print Preview, Save, SaveAs csv
     * <p>
     * Display - show / hide Create new event pane, show/hide bottom feedback pane
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        JMenu displayMenu = new JMenu(Bundle.getMessage("Display"));
        
        JCheckBoxMenuItem shownewevent = new JCheckBoxMenuItem((Bundle.getMessage("NewEvent")));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        shownewevent.setSelected(true);
        shownewevent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newEvShow = shownewevent.isSelected();
                neweventcontainer.setVisible(newEvShow);
            }
        });
        
        JCheckBoxMenuItem showinfopanel = new JCheckBoxMenuItem(Bundle.getMessage("ShowInfoPanel"));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        showinfopanel.setSelected(true);
        showinfopanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean infoShow = showinfopanel.isSelected();
                
                scrolltablefeedback.setVisible(infoShow);
                validate();
                repaint();
                split.setDividerLocation(_splitratio);
            }
        });
               
        displayMenu.add(shownewevent);
        displayMenu.add(showinfopanel);             
        

        menuList.add(evColMenu);
        menuList.add(evFbMenu);
        menuList.add(displayMenu);
        return menuList;
    }

    /**
     * Sets timestamp format + background to last heard column
     */    
    private static class TimeStampRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null && value instanceof Date) {
                c.setText(DATE_FORMAT.format((Date) value));
            }
            return c;
        }
    }
    
    /**
     * Sets background to spot the lesser used number columns
     */    
    private static class OsRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int val = (int) value;
            
            if (val>0) {
                cellComponent.setBackground( GOLD );
                cellComponent.setForeground( table.getForeground() );                
            }
            else {
                if (isSelected) {
                    cellComponent.setBackground( table.getSelectionBackground() );
                    cellComponent.setForeground( table.getSelectionBackground() );                    
                } else {
                    cellComponent.setBackground( table.getBackground() );
                    cellComponent.setForeground( table.getBackground() );
                }
            }
            return cellComponent;
        }
    }    
    
    /**
     * Sets background to last feedback column
     */    
    private static class LafbRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if ( value instanceof CbusEventRequestMonitorEvent.FbState ) {
            
                if ( Objects.equals(value , CbusEventRequestMonitorEvent.FbState.LfbFinding )) {
                    c.setText(Bundle.getMessage("LfbFinding"));
                    c.setBackground( GOLD );
                    c.setForeground( table.getForeground() );
                }
                else if ( Objects.equals(value , CbusEventRequestMonitorEvent.FbState.LfbGood )) {
                    c.setText(Bundle.getMessage("LfbGood"));
                    c.setBackground( VERY_LIGHT_GREEN );
                    c.setForeground( table.getForeground() );                
                }
                else if ( Objects.equals(value , CbusEventRequestMonitorEvent.FbState.LfbBad )) {
                    c.setText(Bundle.getMessage("LfbBad"));
                    c.setBackground( VERY_LIGHT_RED );
                    c.setForeground( table.getForeground() );                
                }
                
                else {
                    if (isSelected) {
                        c.setBackground( table.getSelectionBackground() );
                        c.setForeground( table.getSelectionBackground() );                    
                    } else {
                        c.setBackground( table.getBackground() );
                        c.setForeground( table.getBackground() );
                    }
                }
            
            }
            
            return c;
        }
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
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemEvRequestMon"));
        }
        return Bundle.getMessage("MenuItemEvRequestMon");
    }
    
    @Override
    public void dispose() {
        eventModel.removeTableModelListener(this);
        eventModel.dispose();
        eventModel = null;
        eventTable = null;
        eventScroll = null;
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemEvRequestMon"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusEventRequestTablePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventRequestTablePane.class);

}
