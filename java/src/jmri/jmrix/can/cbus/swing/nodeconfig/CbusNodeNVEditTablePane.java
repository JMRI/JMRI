package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Objects;
import javax.swing.AbstractCellEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
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
public class CbusNodeNVEditTablePane extends jmri.jmrix.can.swing.CanPanel {

    private CbusNodeNVTableDataModel nodeNVModel;
    private JScrollPane eventScroll;
    private JPanel pane1;
    private int largerFont;
    private JTable nodeNvTable;
    
    NodeConfigToolPane mainpane;

    protected CbusNodeNVEditTablePane( CbusNodeNVTableDataModel nVModel ) {
        super();
        nodeNVModel = nVModel;
        nodeNvTable = new JTable(nodeNVModel);
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        
    }
    
    protected void setNode(CbusNode node, NodeConfigToolPane pane ) {
        
        CbusNode nodeOfInterest = node;
        mainpane = pane;
        nodeNvTable = null;
        nodeNvTable = new JTable(nodeNVModel);
        
        nodeNVModel.setNode( nodeOfInterest );
        
        nodeNVModel.setEditFrame();
        init();
    }

    protected void init() {
        
        pane1 = null;
        pane1 = new JPanel();
        
        TableColumnModel tableModel = nodeNvTable.getColumnModel();
        
        nodeNvTable.setRowSelectionAllowed(true);
        nodeNvTable.setColumnSelectionAllowed(false);
        nodeNvTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        nodeNvTable.setRowHeight(27);
        
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_CURRENT_VAL_COLUMN).setCellRenderer( getRenderer() );
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN).setCellRenderer( getRenderer() );        
        
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_COLUMN).setCellRenderer( new SpinnerRenderer() );
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_COLUMN).setCellEditor( new NvSpinnerEditor() );
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN).setCellRenderer(getRenderer());
        tableModel.getColumn(CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN).setCellRenderer( getRenderer() );        
        
        JTextField f = new JTextField();
        largerFont = f.getFont().getSize()+2;
        
        // configure items for GUI
        nodeNVModel.configureTable(nodeNvTable);   
        
        setLayout(new BorderLayout() );
        
        pane1.setLayout(new BorderLayout());
        
        // scroller for main table
        eventScroll = new JScrollPane(nodeNvTable);

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
                
                int tablecol = nodeNvTable.convertColumnIndexToModel(col);

                if ( tablecol == CbusNodeNVTableDataModel.NV_NUMBER_COLUMN ){
                    f.setFont(f.getFont().deriveFont(Font.BOLD, largerFont ));
                }
                
                int oldval = (int) nodeNVModel.getValueAt(row, CbusNodeNVTableDataModel.NV_CURRENT_VAL_COLUMN);
                int newval = (int) nodeNVModel.getValueAt(row, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
                
                String string="";
                if(arg1 != null){
                    string = arg1.toString();
                    if (string.equals("0000 0000")) {
                        string = "";
                    }
                    f.setText(string.toUpperCase());
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

    protected static class NvSpinnerEditor extends AbstractCellEditor implements ChangeListener, TableCellEditor {
        
        final JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));

        public NvSpinnerEditor() {
            spinner.addChangeListener(this);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
            int row, int column) {
            spinner.setValue( value);
            spinner.setBorder(null);
            
            return spinner;
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            if (evt instanceof MouseEvent) {
                return ((MouseEvent) evt).getClickCount() >= 1;
            }
            return true;
        }

        public Object getCellEditorValue() {
            return spinner.getValue();
        }
        
        public void stateChanged(ChangeEvent eve) {
            stopCellEditing();
        }
    }

    protected static class SpinnerRenderer extends JSpinner implements TableCellRenderer {
        
        public SpinnerRenderer() {
            setOpaque(true);
            setBorder(null);
        }
   
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            
            setModel(new SpinnerNumberModel( (int) value, -1, 255, 1) );
            return this;
        }
    }

    @Override
    public void dispose() {
        eventScroll = null;
        super.dispose();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeNVEditTablePane.class);

}
