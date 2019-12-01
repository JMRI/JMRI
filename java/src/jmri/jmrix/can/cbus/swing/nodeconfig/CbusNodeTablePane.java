package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a Cbus node table.
 *
 * @author Steve Young (C) 2019
 * @see CbusNodeTableDataModel
 *
 * @since 2.99.2
 */
public class CbusNodeTablePane extends JPanel {

    private CbusNodeTableDataModel nodeModel=null;
    protected JTable nodeTable=null;
    
    private TableRowSorter<CbusNodeTableDataModel> sorter;

    public static final Color WHITE_GREEN = new Color(0xf5,0xf5,0xf5);
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);
    public static final Color GOLD = new Color(255,204,51);
    
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm EEE d MMM");
    
    public void initComponents(CanSystemConnectionMemo memo) {
        try {
            nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
        } catch (NullPointerException e) {
            log.error("Unable to get Node Table from Instance Manager");
        }
        
        init();
        
    }

    public CbusNodeTablePane() {
        super();
    }

    public void init() {  
        
        nodeTable = new JTable(nodeModel) {
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
                            return CbusNodeTableDataModel.COLUMNTOOLTIPS[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };

        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        nodeTable.setColumnModel(tcm);
        nodeTable.createDefaultColumnsFromModel();
        
       // nodeTable.setAutoCreateRowSorter(true);
        
        sorter = new TableRowSorter<>(nodeModel);
        nodeTable.setRowSorter(sorter);
        
        // prevent the TableColumnModel from being recreated and loosing custom cell renderers
        nodeTable.setAutoCreateColumnsFromModel(false); 
        
        // configure items for GUI
        nodeModel.configureTable(nodeTable);
        
        nodeTable.setRowSelectionAllowed(true);
        nodeTable.setColumnSelectionAllowed(false);
        nodeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        tcm.getColumn(CbusNodeTableDataModel.NODE_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_USER_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_EVENTS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.COMMAND_STAT_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.CANID_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN).setCellRenderer(new ProgressCellRender());
        tcm.getColumn(CbusNodeTableDataModel.NUMBER_BACKUPS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.SESSION_BACKUP_STATUS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.LAST_BACKUP_COLUMN).setCellRenderer(getRenderer());
        
        TableColumn delBColumn = tcm.getColumn(CbusNodeTableDataModel.NODE_RESYNC_BUTTON_COLUMN);
        delBColumn.setCellEditor(new ButtonEditor(new JButton()));
        delBColumn.setCellRenderer(new ButtonRenderer());
        
        nodeTable.setRowHeight(22);
        
        setLayout(new BorderLayout());
        JScrollPane eventScroll = new JScrollPane(nodeTable);
        eventScroll.setVisible(true);
        eventScroll.setPreferredSize(new Dimension(300, 40));
        add(eventScroll);
        
        validate();
        repaint();
        
        nodeModel.fireTableDataChanged();

    }
    

    /**
     * Cell Renderer for string table columns
     */    
    private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
            
            JTextField f = new JTextField();
            
            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object arg1, boolean isSelected, boolean hasFocus, 
                int row, int col) {
                
                f.setHorizontalAlignment(JTextField.CENTER);
                f.setBorder( table.getBorder() );
                
                String string;
                if(arg1 != null){
                    string = arg1.toString();
                    try {
                        if (Integer.parseInt(string)<0){
                            string="";
                        }
                    } catch (NumberFormatException ex) {
                    }
                    f.setText(string);
                    if (arg1 instanceof java.util.Date) {
                        f.setText(DATE_FORMAT.format((java.util.Date) arg1));
                    }
                } else {
                    f.setText("");
                }
                if (isSelected) {
                    f.setBackground( table.getSelectionBackground() );
                } else {
                    if ( row % 2 == 0 ) {
                        f.setBackground( table.getBackground() );
                    }
                    else {
                        f.setBackground( WHITE_GREEN );
                    }
                }
                if ( arg1 instanceof CbusNodeConstants.BackupType ) {
                    if ( Objects.equals(arg1 , CbusNodeConstants.BackupType.INCOMPLETE )) {
                        f.setBackground( VERY_LIGHT_RED );
                        f.setText(Bundle.getMessage("BackupIncomplete"));
                    }
                    else if ( Objects.equals(arg1 , CbusNodeConstants.BackupType.COMPLETE )) {
                        f.setBackground( VERY_LIGHT_GREEN );
                        f.setText(Bundle.getMessage("BackupComplete"));
                    }
                    else if ( Objects.equals(arg1 , CbusNodeConstants.BackupType.COMPLETEDWITHERROR )) {
                        f.setBackground( VERY_LIGHT_RED );
                        f.setText(Bundle.getMessage("BackupCompleteError"));
                    }
                    else if ( Objects.equals(arg1 , CbusNodeConstants.BackupType.NOTONNETWORK )) {
                        f.setBackground( VERY_LIGHT_RED );
                        f.setText(Bundle.getMessage("BackupNotOnNetwork"));
                    }
                    else if ( Objects.equals(arg1 , CbusNodeConstants.BackupType.OUTSTANDING )) {
                        f.setBackground( GOLD );
                        f.setText(Bundle.getMessage("BackupOutstanding"));
                    }
                }
                return f;
            }
        };
    }

    /**
     * Cell Renderer for a progress bar
     */ 
    public static class ProgressCellRender extends JProgressBar implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
            int progress = 0;
            int fullValprogress = 0;
            float fp = 0.00f;
            if (value instanceof Float) {
                fp = (Float) value;
                progress = Math.round( fp * 100f);
                fullValprogress = Math.round( fp * 1000f);
                if ( progress==100 && fullValprogress<1000 ){
                    progress = 99;
                }
            }
            
            // progress value from 0 to 100
            // As progress increases bar changes from red to green via yellow
            setForeground(new Color(Math.min(0.8f, 2.0f * (1 - fp)),Math.min(0.8f, 2.0f * fp ),0));
            setBorderPainted(false);
            setStringPainted(true);
            setValue(fullValprogress);
            if ( progress < 99 ) {
                setMaximum(1000);
            }
            setString(progress + "%");
            if (isSelected) {
                setBackground( table.getSelectionBackground() );
            } else {
                if ( row % 2 == 0 ) {
                    setBackground( table.getBackground() );
                }
                else {
                    setBackground( WHITE_GREEN );
                }
            }
            return this;
        }
    }
    
    public void dispose() {
        nodeTable = null;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeTablePane.class);

}
