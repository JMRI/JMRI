package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEventTableDataModel;
import jmri.jmrix.can.cbus.swing.CbusTableRowEventDnDHandler;
import jmri.jmrix.can.cbus.swing.CbusCommonSwing;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Pane providing a CBUS Node Event table for a single node.
 *
 * @author Steve Young (C) 2019
 * @see CbusNodeEventTableDataModel
 *
 * @since 4.15.5
 */
public class CbusNodeEventTablePane extends jmri.jmrix.can.swing.CanPanel {

    private final CbusNodeEventTableDataModel nodeEventModel;
    private JScrollPane eventVarScroll;
    private JPanel pane1;
    private JTable nodeEventTable;
    private TableRowSorter<CbusNodeEventTableDataModel> sorter;
    private CbusTableRowEventDnDHandler eventDragHandler;
    
    /**
     * Create a new CBUS Node Event Table Pane
     * @param model the Table Model to use
     */
    public CbusNodeEventTablePane( CbusNodeEventTableDataModel model ) {
        super();
        nodeEventModel = model;
        nodeEventTable = new JTable(nodeEventModel);
    }
    
    /**
     * Set the Node
     * @param node the CBUS Node Events to display
     */
    public void setNode( CbusNode node) {
        
        if (node == null && pane1!=null){ 
            pane1.setVisible(false);
            return;
        }
        nodeEventModel.setNode(node);
        nodeEventTable = new JTable(nodeEventModel);
        init();
    }

    public void init() {
        
        if (pane1 != null ){ 
            pane1.setVisible(false);
        }
        
        pane1 = null;
        
        if ( nodeEventTable == null ){
            return;
        }
        
        TableColumnModel tcm = nodeEventTable.getColumnModel();
        
        sorter = null;
        
        if (nodeEventModel.getRowCount() > 0 ) {
            nodeEventTable.setAutoCreateRowSorter(true);
            sorter = new TableRowSorter<>(nodeEventModel);
            nodeEventTable.setRowSorter(sorter);
        }
        
        // configure items for GUI
        CbusCommonSwing.configureTable(nodeEventTable);
        
        setColumnRenderers(tcm);
        
        pane1 = new JPanel();
        
        setLayout(new BorderLayout() );
        pane1.setLayout(new BorderLayout());
        
        // scroller for main table
        eventVarScroll = new JScrollPane(nodeEventTable);

        pane1.add(eventVarScroll);
        
        add(pane1);
        pane1.setVisible(true);
        
        nodeEventTable.setDragEnabled(true);
        nodeEventTable.setDropMode(DropMode.ON);
        eventDragHandler = new CbusTableRowEventDnDHandler(this.memo, nodeEventTable);
        nodeEventTable.setTransferHandler(eventDragHandler);
        
    }
    
    private void setColumnRenderers(TableColumnModel tcm){
    
        for (int i = 0; i < CbusNodeEventTableDataModel.MAX_COLUMN; i++) {
            if ( CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN == i){
                tcm.getColumn(i).setCellEditor(new ButtonEditor(new JButton()));
                tcm.getColumn(i).setCellRenderer(new ButtonRenderer());
            } else {
                tcm.getColumn(i).setCellRenderer(getRenderer());
            }
        }
        
        TableColumn delBColumn = tcm.getColumn(CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN);
        
        if ( hideEditButton ) {
            
            delBColumn.setMinWidth(0);
            delBColumn.setMaxWidth(0);
            delBColumn.setWidth(0);
            
        }
    
    }
    
    private boolean hideEditButton = false;
    
    protected void setHideEditButton(){
        hideEditButton = true;
    }
    
    /**
     * Cell Renderer for string table columns, highlights any text in filter input
     */    
    private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
            
            JTextField f = new JTextField();
            
            /** {@inheritDoc} */
            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object arg1, boolean isSelected, boolean hasFocus, 
                int row, int col) {
                
                f.setHorizontalAlignment(JTextField.CENTER);
                f.setBorder( table.getBorder() );
                
                String string;
                if(arg1 != null){
                    string = arg1.toString();
                    f.setText(string);
                    CbusCommonSwing.hideNumbersLessThan(0, string, f);
                    
                } else {
                    f.setText("");
                }
                
                CbusCommonSwing.setCellBackground(isSelected, f, table,row);
                CbusCommonSwing.setCellFocus(hasFocus, f, table);
                
                return f;
            }
        };
    }

    /**
     * Only used for testing.
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("MenuItemEventTable"));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        eventDragHandler.dispose();
        eventVarScroll = null;
    }

 //   private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePane.class);

}
