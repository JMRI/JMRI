package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeEventTableDataModel;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrix.can.CanSystemConnectionMemo;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane providing a Cbus Node Event table for a single node.
 *
 * @author Steve Young (C) 2019
 * @see CbusNodeEventTableDataModel
 *
 * @since 4.15.5
 */
public class CbusNodeEventTablePane extends jmri.jmrix.can.swing.CanPanel {

    public CbusNodeEventTableDataModel nodeEventModel;
    public JScrollPane eventVarScroll;
    protected JPanel pane1;
    JTable nodeEventTable;
    TableRowSorter<CbusNodeEventTableDataModel> sorter;
    
    public CbusNodeEventTablePane( CbusNodeEventTableDataModel model ) {
        super();
        nodeEventModel = model;
        nodeEventTable = new JTable(nodeEventModel);
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
    }
    
    public void setNode( CbusNode node) {
        
        if (node == null ){ 
            pane1.setVisible(false);
            return;
        }
        nodeEventModel.setNode(node);
        nodeEventTable = new JTable(nodeEventModel);
        init();
    }

    public void init() {
        // log.info("init");
        
        if (pane1 != null ){ 
            pane1.setVisible(false);
        }
        
        pane1 = null;
        
        if ( nodeEventTable == null ){
            return;
        }
        
        TableColumnModel tcm = nodeEventTable.getColumnModel();

        nodeEventTable.createDefaultColumnsFromModel();
        
        sorter = null;
        
        if (nodeEventModel.getRowCount() > 0 ) {
            nodeEventTable.setAutoCreateRowSorter(true);
            sorter = new TableRowSorter<CbusNodeEventTableDataModel>(nodeEventModel);
            nodeEventTable.setRowSorter(sorter);
        }
        
        nodeEventTable.setRowHeight(26);
        
        nodeEventTable.setRowSelectionAllowed(true);
        nodeEventTable.setColumnSelectionAllowed(false);
        nodeEventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        tcm.getColumn(CbusNodeEventTableDataModel.NODE_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeEventTableDataModel.NODE_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeEventTableDataModel.EVENT_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeEventTableDataModel.EV_VARS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeEventTableDataModel.EV_INDEX_COLUMN).setCellRenderer(getRenderer());
        
        TableColumn delBColumn = tcm.getColumn(CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN);
        delBColumn.setCellEditor(new ButtonEditor(new JButton()));
        delBColumn.setCellRenderer(new ButtonRenderer());
        
        if ( hideEditButton ) {
            
            delBColumn.setMinWidth(0);
            delBColumn.setMaxWidth(0);
            delBColumn.setWidth(0);
            
        }
        
        // configure items for GUI
        nodeEventModel.configureTable(nodeEventTable);   
        
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
        nodeEventTable.setTransferHandler(new CbusNodeEventTableRowDnDHandler());
        
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
                        if (Integer.parseInt(string)<1){
                            string="";
                        }
                    } catch (NumberFormatException ex) {
                    }
                    if (string.equals("-1")) {
                        string = "";
                    }

                    f.setText(string);
                    
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
                        f.setBackground( CbusNodeTablePane.WHITE_GREEN );
                    }
                }
                return f;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("MenuItemNodeTable"));
    }
    
    @Override
    public void dispose() {
        //   nodeTable = null;
        eventVarScroll = null;
        super.dispose();
    }

    public class CbusNodeEventTableRowDnDHandler extends TransferHandler {
    
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    
        @Override
        public Transferable createTransferable(JComponent c) {
            
            if (!(c instanceof JTable )){
                return null;
            }
            
            JTable table = (JTable) c;
            int row = table.getSelectedRow();
            if (row < 0) {
                return null;
            }
            row = table.convertRowIndexToModel(row);
            
            int nn = (Integer) nodeEventTable.getModel().getValueAt(row, CbusNodeEventTableDataModel.NODE_NUMBER_COLUMN); // node number
            int en = (Integer) nodeEventTable.getModel().getValueAt(row, CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN); // event number
            
            StringBuilder jmriAddress = new StringBuilder(13);
            jmriAddress.append("+");
            if ( nn > 0 ) {
                jmriAddress.append("N");
                jmriAddress.append(nn);
                jmriAddress.append("E");
            }
            jmriAddress.append(en);
            
            return new StringSelection( jmriAddress.toString() );
            
        }
    }

 //   private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePane.class);

}
