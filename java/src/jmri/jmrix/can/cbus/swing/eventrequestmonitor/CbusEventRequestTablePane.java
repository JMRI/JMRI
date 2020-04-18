package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.swing.CbusNewEventPane;
import jmri.jmrix.can.cbus.swing.CbusCommonSwing;
import jmri.util.swing.StayOpenCheckBoxItem;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.util.table.JTableWithColumnToolTips;

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

    protected CbusEventRequestDataModel eventModel;
    protected JTable eventTable;
    private JScrollPane eventScroll;
    private JSplitPane split;
    
    private CbusNewEventPane neweventcontainer;
    
    private final double _splitratio = 0.95;

    private JScrollPane scrolltablefeedback;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);
    public static final Color GOLD = new Color(255,204,51);
    private final JMenu evColMenu = new JMenu(Bundle.getMessage("evColMenuName"));
    private final JMenu evFbMenu = new JMenu(Bundle.getMessage("evFbMenuName"));

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
        
        JTable _evReqTable = new JTableWithColumnToolTips(eventModel,CbusEventRequestDataModel.columnToolTips);

        // Use XTableColumnModel so we can control which columns are visible
        final  XTableColumnModel tcm = new XTableColumnModel();
        _evReqTable.setColumnModel(tcm);
        
        scrolltablefeedback = new JScrollPane (eventModel.tablefeedback());
        
        CbusCommonSwing.configureTable(_evReqTable);
        
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

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            int colnumber=i;
            
            String colName = _evReqTable.getColumnName(colnumber);
            StayOpenCheckBoxItem showcol = new StayOpenCheckBoxItem(colName);
            showcol.setToolTipText(CbusEventRequestDataModel.columnToolTips[i]);
            showcol.setSelected(true);
            switch (colnumber) {
                case CbusEventRequestDataModel.NAME_COLUMN:
                case CbusEventRequestDataModel.NODE_COLUMN:
                case CbusEventRequestDataModel.EVENT_COLUMN:
                case CbusEventRequestDataModel.LATEST_TIMESTAMP_COLUMN:
                case CbusEventRequestDataModel.DELETE_BUTTON_COLUMN:
                    evColMenu.add(showcol); // event columns
                    break;
                case CbusEventRequestDataModel.STATUS_REQUEST_BUTTON_COLUMN:
                case CbusEventRequestDataModel.FEEDBACKREQUIRED_COLUMN:
                case CbusEventRequestDataModel.LASTFEEDBACK_COLUMN:
                case CbusEventRequestDataModel.FEEDBACKOUTSTANDING_COLUMN:
                case CbusEventRequestDataModel.FEEDBACKTIMEOUT_COLUMN:
                case CbusEventRequestDataModel.FEEDBACKEVENT_COLUMN:
                case CbusEventRequestDataModel.FEEDBACKNODE_COLUMN:
                    evFbMenu.add(showcol); // feedback columns
                    break;
                default:
                    log.warn("No menuitem defined for {}",colnumber);
                    break;
            }
            
            showcol.addActionListener((ActionEvent e) -> {
                TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                boolean visible1 = tcm.isColumnVisible(column);
                tcm.setColumnVisible(column, !visible1);
            });
        }
                
        eventModel.addTableModelListener(this);
        	
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
        // main pane
        JPanel _pane1 = new JPanel();
        _pane1.setLayout(new BorderLayout());
        
        JPanel _toppanelcontainer = new JPanel();
        _toppanelcontainer.setLayout(new BoxLayout(_toppanelcontainer, BoxLayout.X_AXIS));
        // scroller for main table
        eventScroll = new JScrollPane(_evReqTable);
        eventScroll.setPreferredSize(new Dimension(450, 200));

        // add new event
        
        neweventcontainer = new CbusNewEventPane(this);
        
        _toppanelcontainer.add(neweventcontainer);
        _pane1.add(_toppanelcontainer, BorderLayout.PAGE_START);
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            eventScroll, scrolltablefeedback);
        split.setResizeWeight(_splitratio);
        split.setContinuousLayout(true);

        _pane1.add(split, BorderLayout.CENTER);
        
        add(_pane1);
        _pane1.setVisible(true);
    }
    
    public void addEvent(int nn,int en){
        if ( eventModel.eventRow(nn,en) < 0 ) {
            eventModel.addEvent(nn,en,CbusEventRequestMonitorEvent.EvState.UNKNOWN,null);
        }
        tableChanged(null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        neweventcontainer.setNewButtonActive(
            eventModel.eventRow( neweventcontainer.getNn(),neweventcontainer.getEn())<0) ;
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
        List<JMenu> menuList = new ArrayList<>();
        JMenu displayMenu = new JMenu(Bundle.getMessage("Display"));
        
        JCheckBoxMenuItem shownewevent = new JCheckBoxMenuItem((Bundle.getMessage("NewEvent")));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        shownewevent.setSelected(true);
        shownewevent.addActionListener((ActionEvent e) -> 
            neweventcontainer.setVisible(shownewevent.isSelected()));
        
        JCheckBoxMenuItem showinfopanel = new JCheckBoxMenuItem(Bundle.getMessage("ShowInfoPanel"));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        showinfopanel.setSelected(true);
        showinfopanel.addActionListener((ActionEvent e) -> {
            scrolltablefeedback.setVisible(showinfopanel.isSelected());
            validate();
            repaint();
            split.setDividerLocation(_splitratio);
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
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("MenuItemEvRequestMon"));
    }
    
    /**
     * {@inheritDoc}
     */
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
