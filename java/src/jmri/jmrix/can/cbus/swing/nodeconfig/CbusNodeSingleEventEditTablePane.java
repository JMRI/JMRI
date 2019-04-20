package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.can.cbus.node.CbusNodeSingleEventTableDataModel;
import jmri.jmrix.can.CanSystemConnectionMemo;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane providing a Cbus Node Event Edit table.
 * @author Steve Young (C) 2019
 *
 * @since 4.15.5
 */
public class CbusNodeSingleEventEditTablePane extends jmri.jmrix.can.swing.CanPanel {

    private CbusNodeSingleEventTableDataModel singleEVModel;
    private JScrollPane eventScroll;
    private JPanel pane1;
    private int largerFont;
    private JTable singleEvTable;
    
    NodeConfigToolPane mainpane;

    protected CbusNodeSingleEventEditTablePane( CbusNodeSingleEventTableDataModel eVModel ) {
        super();
        singleEVModel = eVModel;
        singleEvTable = new JTable(singleEVModel);
    }

    public void initComponents(CanSystemConnectionMemo memo, NodeConfigToolPane pane ) {
        super.initComponents(memo);
        mainpane = pane;
        
        singleEvTable = new JTable(singleEVModel);
        init();
        
    }

    private void init() {
        
        pane1 = null;
        pane1 = new JPanel();
        
        TableColumnModel tableModel = singleEvTable.getColumnModel();
        
        singleEvTable.setRowSelectionAllowed(true);
        singleEvTable.setColumnSelectionAllowed(false);
        singleEvTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        singleEvTable.setRowHeight(27);
        
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN).setCellRenderer( getRenderer() );
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN).setCellRenderer( getRenderer() );        
        
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN).setCellRenderer(
            new CbusNodeNVEditTablePane.SpinnerRenderer() );
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN).setCellEditor(
            new CbusNodeNVEditTablePane.NvSpinnerEditor() );
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN).setCellRenderer( getRenderer() );        
        
        JTextField f = new JTextField();
        largerFont = f.getFont().getSize()+2;
        
        // configure items for GUI
        singleEVModel.configureTable(singleEvTable);   
        
        setLayout(new BorderLayout() );
        
        pane1.setLayout(new BorderLayout());
        
        // scroller for main table
        eventScroll = new JScrollPane(singleEvTable);

        pane1.add(eventScroll);
        
        add(pane1);
        pane1.setVisible(true);
        
    }
    
    public static final Color WHITE_GREEN = new Color(0xf5,0xf5,0xf5);
    
    /**
     * Cell Renderer for string table columns, highlights any text in filter input
     */    
    private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
            
            JTextField f = new JTextField();
            
            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object arg1, boolean isSelected, boolean hasFocus, int row, int col) {
                
                f.setHorizontalAlignment(JTextField.CENTER);
                f.setBorder( table.getBorder() );
                
                int tablecol = singleEvTable.convertColumnIndexToModel(col);

                if ( tablecol == CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN ){
                    f.setFont(f.getFont().deriveFont(Font.BOLD, largerFont ));
                }
                
                int oldval = (int) singleEVModel.getValueAt(row, CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN);
                int newval = (int) singleEVModel.getValueAt(row, CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN);
                
                String string="";
                if(arg1 != null){
                    string = arg1.toString();
                    if (string.equals("0000 0000")) {
                        string = "";
                    }
                    f.setText(string.toUpperCase() );
                } else {
                    f.setText("");
                }
                if (isSelected) {
                    if ( oldval != newval ) {
                        f.setBackground( Color.orange );
                    }
                    else {
                        f.setBackground( table.getSelectionBackground() );
                    }
                } else {
                    if ( oldval != newval ) {
                        f.setBackground( Color.yellow );
                    }
                    else {
                        if ( row % 2 == 0 ) {
                            f.setBackground( table.getBackground() );
                        }
                        else {
                            f.setBackground( WHITE_GREEN );
                        }
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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeSingleEventEditTablePane.class);

}
