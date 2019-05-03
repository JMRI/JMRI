package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
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
                            return CbusNodeTableDataModel.columnToolTips[realIndex];    
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
        
        sorter = new TableRowSorter<CbusNodeTableDataModel>(nodeModel);
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
        
        TableColumn delBColumn = tcm.getColumn(CbusNodeTableDataModel.NODE_RESYNC_BUTTON_COLUMN);
        delBColumn.setCellEditor(new ButtonEditor(new JButton()));
        delBColumn.setCellRenderer(new ButtonRenderer());
        
        nodeTable.setRowHeight(22);
        
        setLayout(new BorderLayout());
        JScrollPane eventScroll = new JScrollPane(nodeTable);
        eventScroll.setVisible(true);
        setPreferredSize(new Dimension(300, 80));
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
                
                String string="";
                if(arg1 != null){
                    string = arg1.toString();
                    try {
                        if (Integer.parseInt(string)<0){
                            string="";
                        }
                    } catch (NumberFormatException ex) {
                    }

                    f.setText(string);
                    // log.debug(" string :{}:",string );
                    
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
                
                return f;
            }
        };
    }

    /**
     * Cell Renderer for a progress bar
     */ 
    public static class ProgressCellRender extends JProgressBar implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
            return this;
        }
    }
    
    public void dispose() {
        nodeTable = null;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeTablePane.class);

}
