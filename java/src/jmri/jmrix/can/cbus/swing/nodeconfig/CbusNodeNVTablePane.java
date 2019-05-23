package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;

import jmri.jmrix.can.CanSystemConnectionMemo;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane providing a Cbus event table. Menu code copied from BeanTableFrame.
 *
 * @author Steve Young (C) 2019
 *
 * @since 4.15.5
 */
public class CbusNodeNVTablePane extends jmri.jmrix.can.swing.CanPanel {

    private CbusNodeNVTableDataModel nodeNVModel;
    private JScrollPane eventScroll;
    private JPanel pane1;
    private int largerFont;
    private JTable nodeNvTable;

    public CbusNodeNVTablePane( CbusNodeNVTableDataModel nVModel ) {
        super();
        nodeNVModel = nVModel;
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
    }
    
    protected void setNode( CbusNode node) {
        
        nodeNVModel.setNode( node );
        if ( node == null ) {
            return;
        }
        nodeNvTable = new JTable(nodeNVModel);
        nodeNVModel.setViewFrame();
        init();
    }

    private void init() {
        // log.info("init");
        
        if (pane1 != null ){ 
            pane1.setVisible(false);
        }
        pane1 = null;
        
        if ( nodeNvTable == null ){
            return;
        }
        
        TableColumnModel tableModel = nodeNvTable.getColumnModel();
        

        // configure items for GUI
        nodeNVModel.configureTable(nodeNvTable);  

        nodeNvTable.setRowSelectionAllowed(true);
        nodeNvTable.setColumnSelectionAllowed(false);
        nodeNvTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        nodeNvTable.setRowHeight(26);
        
        // hide the editable columns, they're used in CbusNodeNVEditTablePane
        // but we use the same model for this as well
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_COLUMN).setMinWidth(0);
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_COLUMN).setMaxWidth(0);
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_COLUMN).setWidth(0);
        
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN).setMinWidth(0);
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN).setMaxWidth(0);
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN).setWidth(0);
        
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN).setMinWidth(0);
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN).setMaxWidth(0);
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN).setWidth(0);        
        
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_CURRENT_VAL_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN).setCellRenderer(getRenderer());
        

        tableModel.getColumn(0).setPreferredWidth( CbusNodeNVTableDataModel.getPreferredWidth(0)*2 );
        tableModel.getColumn(1).setPreferredWidth( CbusNodeNVTableDataModel.getPreferredWidth(1)*2 );
        tableModel.getColumn(2).setPreferredWidth( CbusNodeNVTableDataModel.getPreferredWidth(2)*2 );
        tableModel.getColumn(3).setPreferredWidth( CbusNodeNVTableDataModel.getPreferredWidth(3)*2 );
        
        JTextField f = new JTextField();
        largerFont = f.getFont().getSize()+2;
        
        pane1 = new JPanel();
        
        setLayout(new BorderLayout() );
        
        pane1.setLayout(new BorderLayout());
        
        // scroller for main table
        eventScroll = new JScrollPane(nodeNvTable);

        pane1.add(eventScroll);
        
        add(pane1);
        pane1.setVisible(true);
        
    }
    
    public static final Color WHITE_GREEN = new Color(0xf5,0xf5,0xf5);
    // F5FFFA
    
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
                
                if ( nodeNvTable.convertColumnIndexToModel(col) == CbusNodeNVTableDataModel.NV_NUMBER_COLUMN ){
                    f.setFont(f.getFont().deriveFont(Font.BOLD, largerFont ));
                }
                
                String string="";
                if(arg1 != null){
                    string = arg1.toString();
                    
                    if (string.equals("0000 0000")) {
                        string = "";
                    }
                    
                    if (string.equals("-1")) {
                        string = "";
                    }
                    
                    f.setText(string.toUpperCase() );
                    
                } else {
                    f.setText("");
                }

                if (isSelected) {
                    f.setBackground( Color.yellow );
                    
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


    @Override
    public void dispose() {
        //   nodeTable = null;
        eventScroll = null;
        super.dispose();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeNVTablePane.class);

}
