// PacketDataModel.java

package jmri.jmrix.pricom.pockettester;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.SlotManager;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

/**
 * Table data model for display of DCC packet contents
 * @author		Bob Jacobsen   Copyright (C) 2005
 * @version		$Revision: 1.1 $
 */
public class PacketDataModel extends javax.swing.table.AbstractTableModel  {

    static public final int ADDRESSCOLUMN = 0;
    static public final int TYPECOLUMN = 1;
    static public final int DETAILBUTTONCOLUMN = 2;

    static public final int NUMCOLUMN = 3;
    PacketDataModel(int row, int column) {

        // connect to source for updates
    }

    /**
     * Returns the number of rows to be displayed.  This
     * can vary depending on what has been seen
     */
    public int getRowCount() {
        return 3;
    }


    public int getColumnCount( ){ return NUMCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case ADDRESSCOLUMN: return "Address";
        case TYPECOLUMN: return "Type";   
        case DETAILBUTTONCOLUMN: return "";   // no heading, as button is clear
        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case ADDRESSCOLUMN:
            return Integer.class;
        case TYPECOLUMN:
            return String.class;
        case DETAILBUTTONCOLUMN:
            return JButton.class;
        default:
            return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case DETAILBUTTONCOLUMN:
            return true;
        default:
            return false;
        }
    }

    static final Boolean True = new Boolean(true);
    static final Boolean False = new Boolean(false);

    public Object getValueAt(int row, int col) {

        switch (col) {
        case ADDRESSCOLUMN:  // slot number
            return new Integer(row);
        case DETAILBUTTONCOLUMN:  //
            return "Details";          // will be name of button in default GUI
        case TYPECOLUMN:  //
            return "Dummy";
        default:
            log.error("internal state inconsistent with table requst for "+row+" "+col);
            return null;
        }
    };

    public int getPreferredWidth(int col) {
        switch (col) {
        case ADDRESSCOLUMN:
            return new JTextField(5).getPreferredSize().width;
        case TYPECOLUMN:
            return new JTextField(2).getPreferredSize().width;
        case DETAILBUTTONCOLUMN:
            return new JButton("Details").getPreferredSize().width;
        default:
            return new JLabel(" <unknown> ").getPreferredSize().width;
        }
    }

    public void setValueAt(Object value, int row, int col) {
    }

    /**
     * Configure a table to have our standard rows and columns.
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param slotTable
     */
    public void configureTable(JTable slotTable) {
        // allow reordering of the columns
        slotTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        slotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i=0; i<slotTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            slotTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        slotTable.sizeColumnsToFit(-1);

        // install a button renderer & editor in the "DISP" column for freeing a slot
        setColumnToHoldButton(slotTable, PacketDataModel.DETAILBUTTONCOLUMN);

    }

    void setColumnToHoldButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        slotTable.setRowHeight(new JButton("  "+getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
			.setPreferredWidth(new JButton("  "+getValueAt(1, column)).getPreferredSize().width);
    }

    void setColumnToHoldEStopButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton()){
            public void mousePressed(MouseEvent e) {
                stopCellEditing();
            }
        };
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        slotTable.setRowHeight(new JButton("  "+getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
			.setPreferredWidth(new JButton("  "+getValueAt(1, column)).getPreferredSize().width);
    }


    public void dispose() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PacketDataModel.class.getName());

}
