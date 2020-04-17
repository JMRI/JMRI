package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.swing.CbusCommonSwing;
import jmri.util.ThreadingUtil;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane providing a CBUS node table.
 *
 * @author Steve Young (C) 2019
 * @see CbusNodeTableDataModel
 *
 * @since 2.99.2
 */
public class CbusNodeTablePane extends JPanel {

    private CbusNodeTableDataModel nodeModel;
    protected JTable nodeTable;
    
    private TableRowSorter<CbusNodeTableDataModel> sorter;
    
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
        
        nodeTable = new JTableWithColumnToolTips(nodeModel,CbusNodeTableDataModel.COLUMNTOOLTIPS){
        
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                int modelRow = convertRowIndexToModel(row);
                int modelColumn = convertColumnIndexToModel(column);
                Component comp = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(modelRow) && 
                        ( modelColumn ==  CbusNodeTableDataModel.NODE_IN_LEARN_MODE_COLUMN 
                        || modelColumn ==  CbusNodeTableDataModel.NODE_EVENT_INDEX_VALID_COLUMN
                        )) {
                    comp.setBackground(( row % 2 == 0 ) ? this.getBackground() : jmri.jmrix.can.cbus.swing.CbusCommonSwing.WHITE_GREEN );
                }
                return comp;
            }
        };

        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        nodeTable.setColumnModel(tcm);
        
        sorter = new TableRowSorter<>(nodeModel);
        nodeTable.setRowSorter(sorter);
        
        // configure items for GUI
        CbusCommonSwing.configureTable(nodeTable);
        
        tcm.getColumn(CbusNodeTableDataModel.NODE_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_USER_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_EVENTS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.COMMAND_STAT_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.CANID_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN).setCellRenderer(new ProgressCellRender(CbusCommonSwing.WHITE_GREEN));
        tcm.getColumn(CbusNodeTableDataModel.NUMBER_BACKUPS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.SESSION_BACKUP_STATUS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeTableDataModel.LAST_BACKUP_COLUMN).setCellRenderer(getRenderer());
        
        tcm.getColumn(CbusNodeTableDataModel.NODE_RESYNC_BUTTON_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        tcm.getColumn(CbusNodeTableDataModel.NODE_RESYNC_BUTTON_COLUMN).setCellRenderer(new ButtonRenderer());
        
       ((JComponent) tcm.getColumn(CbusNodeTableDataModel.NODE_RESYNC_BUTTON_COLUMN).getCellRenderer()).setOpaque(false);
        
        setLayout(new BorderLayout());
        JScrollPane eventScroll = new JScrollPane(nodeTable);
        eventScroll.setVisible(true);
        eventScroll.setPreferredSize(new Dimension(300, 40));
        add(eventScroll);
        
        validate();
        repaint();
        
        nodeModel.addTableModelListener((TableModelEvent e) -> {
            if (nodeModel.getRequestNodeRowToDisplay()>-1) {
                setRowToNode();
            }
        });
        nodeModel.fireTableDataChanged();
    }
    
    private void setRowToNode(){
        ThreadingUtil.runOnGUIEventually(() -> {
            int newRow = nodeModel.getRequestNodeRowToDisplay();
            if ( newRow>-1 ) {
                nodeTable.setRowSelectionInterval(newRow, newRow);
                nodeTable.scrollRectToVisible(nodeTable.getCellRect(newRow, 0, true));
                nodeModel.setRequestNodeDisplay(-1);
            }
        });
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
                String string;
                if(arg1 != null){
                    string = arg1.toString();
                    f.setText(string);
                    f.setHorizontalAlignment(JTextField.CENTER);
                    CbusCommonSwing.hideNumbersLessThan(0, string, f);
                    CbusCommonSwing.setCellFromDate(arg1,f,DATE_FORMAT);
                    CbusCommonSwing.setCellFromBackupEnum(arg1,f);
                    
                } else {
                    f.setText("");
                }
                
                CbusCommonSwing.setCellBackground(isSelected, f, table,row);
                CbusCommonSwing.setCellFocus(hasFocus, f, table);
                
                return f;
            }
        };
    }
    
    public void dispose() {
        nodeTable = null;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeTablePane.class);

}
