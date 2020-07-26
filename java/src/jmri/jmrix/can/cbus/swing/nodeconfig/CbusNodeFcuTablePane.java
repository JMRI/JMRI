package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNodeFromFcuTableDataModel;
import jmri.jmrix.can.cbus.swing.CbusCommonSwing;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.JTableWithColumnToolTips;

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
        
        nodeTable = new JTableWithColumnToolTips(nodeModel,CbusNodeFromFcuTableDataModel.FCUTABLETIPS);

        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        nodeTable.setColumnModel(tcm);
        
        sorter = new TableRowSorter<>(nodeModel);
        nodeTable.setRowSorter(sorter);
        
        // configure items for GUI
        CbusCommonSwing.configureTable(nodeTable);
        
        tcm.getColumn(CbusNodeFromFcuTableDataModel.FCU_NODE_NUMBER_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.FCU_NODE_TYPE_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.FCU_NODE_USER_NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.FCU_NODE_EVENTS_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.NODE_NV_TOTAL_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusNodeFromFcuTableDataModel.FCU_NODE_TOTAL_BYTES_COLUMN).setCellRenderer(getRenderer());
        
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
    
    public void dispose() {
        nodeTable = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFcuTablePane.class);

}
