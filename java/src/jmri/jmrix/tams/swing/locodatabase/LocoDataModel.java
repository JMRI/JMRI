package jmri.jmrix.tams.swing.locodatabase;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.tams.TamsListener;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsReply;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display the loco database of the Tams MC
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class LocoDataModel extends javax.swing.table.AbstractTableModel implements TamsListener {

    static public final int ADDRCOLUMN = 0;
    static public final int SPDCOLUMN = 1;
    static public final int FMTCOLUMN = 2;
    static public final int NAMECOLUMN = 3;
    static public final int DELCOLUMN = 4;

    static public final int NUMCOLUMN = 5;

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.tams.swing.locodatabase.TamsLocoBundle");

    jmri.jmrix.tams.TamsSystemConnectionMemo memo;

    ArrayList<String[]> locolist = new ArrayList<String[]>();

    LocoDataModel(int row, int column, jmri.jmrix.tams.TamsSystemConnectionMemo memo) {
        this.memo = memo;
        TamsMessage m = new TamsMessage("xLOCDUMP");
        memo.getTrafficController().sendTamsMessage(m, this);
    }

    /**
     * Returns the number of rows to be displayed. This can vary depending on
     * whether only active rows are displayed, and whether the system slots
     * should be displayed.
     * <P>
     * This should probably use a local cache instead of counting/searching each
     * time.
     */
    @Override
    public int getRowCount() {
        return locolist.size();
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case ADDRCOLUMN:
                return rb.getString("ColAddress");
            case SPDCOLUMN:
                return rb.getString("ColSteps");
            case FMTCOLUMN:
                return rb.getString("ColFormat");
            case NAMECOLUMN:
                return rb.getString("ColName");
            case DELCOLUMN:
                return rb.getString("ColDelete");
            default:
                return "unknown";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {

        switch (col) {
            case DELCOLUMN:
                return JButton.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case DELCOLUMN:
                return true;
            default:
                return false;
        }
    }

    @SuppressWarnings("null")
    @Override
    public Object getValueAt(int row, int col) {
        if (locolist.size() == 0) {
            return null;
        }
        String[] loco = locolist.get(row);
        try {
            switch (col) {
                case ADDRCOLUMN:  //
                    return loco[0];//Integer.valueOf(s.locoAddr());
                case SPDCOLUMN:  //
                    return loco[1];
                case FMTCOLUMN:  //
                    return loco[2];
                case NAMECOLUMN:  //
                    return loco[3];
                case DELCOLUMN:
                    return "delete";
                default:
                    log.error("internal state inconsistent with table requst for " + row + " " + col);
                    return null;
            }
        } catch (RuntimeException ex) {

        }
        return null;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                    justification="better to keep cases in column order rather than to combine")
    public int getPreferredWidth(int col) {
        switch (col) {
            case ADDRCOLUMN:
                return new JTextField(5).getPreferredSize().width;
            case SPDCOLUMN:
                return new JTextField(6).getPreferredSize().width;
            case FMTCOLUMN:
                return new JTextField(6).getPreferredSize().width;
            case NAMECOLUMN:
                return new JTextField(12).getPreferredSize().width;
            case DELCOLUMN:
                return new JButton(rb.getString("DeleteLoco")).getPreferredSize().width;
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == DELCOLUMN) {
            deleteLoco(row);
        }
    }

    //to delete a loco in the MC we have to clear all the locos in the list and add in the ones that we want to keep.
    void deleteLoco(int row) {
        locolist.remove(row);
        TamsMessage m = new TamsMessage("xLOCCLEAR");
        memo.getTrafficController().sendTamsMessage(m, this);
        for (String[] loco : locolist) {
            m = new TamsMessage("xLOCADD " + loco[0] + ", " + loco[1] + ", " + loco[2] + ", '" + loco[3] + "'");
            memo.getTrafficController().sendTamsMessage(m, this);
        }
        m = new TamsMessage("xLOCDUMP");
        memo.getTrafficController().sendTamsMessage(m, this);
    }

    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent.
     *
     * @param slotTable the table to configure
     */
    public void configureTable(JTable slotTable) {
        // allow reordering of the columns
        slotTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        slotTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < slotTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            slotTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        slotTable.sizeColumnsToFit(-1);

        // install a button renderer & editor in the "DISP" column for freeing a slot
        setColumnToHoldButton(slotTable, LocoDataModel.DELCOLUMN);

        // install a button renderer & editor in the "ESTOP" column for stopping a loco
        //setColumnToHoldEStopButton(slotTable, LocoDataModel.ESTOPCOLUMN);
    }

    void setColumnToHoldButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        slotTable.setRowHeight(new JButton("  " + getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
                .setPreferredWidth(new JButton("  " + getValueAt(1, column)).getPreferredSize().width);
    }

    void setColumnToHoldEStopButton(JTable slotTable, int column) {
        TableColumnModel tcm = slotTable.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton()) {
            @Override
            public void mousePressed(MouseEvent e) {
                stopCellEditing();
            }
        };
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        slotTable.setRowHeight(new JButton("  " + getValueAt(1, column)).getPreferredSize().height);
        slotTable.getColumnModel().getColumn(column)
                .setPreferredWidth(new JButton("  " + getValueAt(1, column)).getPreferredSize().width);
    }

    public void dispose() {

    }

    @Override
    public void message(TamsMessage m) {

    }

    @Override
    public void reply(TamsReply r) {
        if (r != null) {
            if (r.match("xLOCADD") >= 0) {
                //loco added so will request a fresh update
                TamsMessage m = new TamsMessage("xLOCDUMP");
                memo.getTrafficController().sendTamsMessage(m, this);
            } else {
                locolist = new ArrayList<String[]>();
                String msg = r.toString();
                String[] rawlocolist = msg.split("\\r");
                log.info("Raw loco list length: " + rawlocolist.length);
                for (String loco : rawlocolist) {
                    log.info(loco);
                    if (!loco.equals("*END*")) {
                        String[] locodetails = loco.split(",");
                        locolist.add(locodetails);
                    } else {
                        break;
                    }
                }
                fireTableDataChanged();
            }
        }
    }

    protected void addLoco(TamsMessage m) {
        memo.getTrafficController().sendTamsMessage(m, this);
    }

    private final static Logger log = LoggerFactory.getLogger(LocoDataModel.class);

}
