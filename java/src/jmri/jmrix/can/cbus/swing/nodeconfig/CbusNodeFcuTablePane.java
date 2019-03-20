package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeFromFcuTableDataModel;
import jmri.util.swing.XTableColumnModel;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane providing a Cbus node table.
 *
 * @author Steve Young (C) 2019
 * @see CbusNodeFromFcuTableDataModel
 *
 * @since 4.15.5
 */
public class CbusNodeFcuTablePane extends JPanel {

    private CbusNodeFromFcuTableDataModel nodeModel=null;
    protected JTable nodeTable=null;
    private TableRowSorter<CbusNodeFromFcuTableDataModel> sorter;

    public static final Color WHITE_GREEN = new Color(0xf5,0xf5,0xf5);
    
    public CbusNodeFcuTablePane() {
        super();
    }
    
    public void initComponents(CanSystemConnectionMemo memo, CbusNodeFromFcuTableDataModel model) {
        
        nodeModel = model;
        init();
        
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
                            return CbusNodeFromFcuTableDataModel.columnToolTips[realIndex];    
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
        
        sorter = new TableRowSorter<CbusNodeFromFcuTableDataModel>(nodeModel);
        nodeTable.setRowSorter(sorter);
        
        // prevent the TableColumnModel from being recreated and loosing custom cell renderers
        nodeTable.setAutoCreateColumnsFromModel(false); 
        
        // configure items for GUI
        nodeModel.configureTable(nodeTable);
        
        nodeTable.setRowSelectionAllowed(true);
        nodeTable.setColumnSelectionAllowed(false);
        nodeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        tcm.getColumn(CbusNodeFromFcuTableDataModel.NODE_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.NODE_TYPE_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.NODE_USER_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.NODE_EVENTS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.NODE_NV_TOTAL_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.NODE_TOTAL_BYTES_COLUMN).setCellRenderer(getRenderer());
        
        nodeTable.setRowHeight(22);
        
        setLayout(new BorderLayout());
        JScrollPane eventScroll = new JScrollPane(nodeTable);
        eventScroll.setVisible(true);
        setPreferredSize(new Dimension(650, 100));
        add(eventScroll);

    }

    /**
     * Cell Renderer for string table columns, highlights any text in filter input
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
    
    public void dispose() {
        nodeTable = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFcuTablePane.class);

}
