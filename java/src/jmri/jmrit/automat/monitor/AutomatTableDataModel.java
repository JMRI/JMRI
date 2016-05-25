// AutomatTableDataModel.java
package jmri.jmrit.automat.monitor;

import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import jmri.jmrit.automat.AutomatSummary;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Automat instances.
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version	$Revision$
 */
public class AutomatTableDataModel extends javax.swing.table.AbstractTableModel
        implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 5450062337875366030L;
    static final int NAMECOL = 0;		// display name
    static final int TURNSCOL = 1;		// number of times through the loop
    static final int KILLCOL = 2;		//

    static final int NUMCOLUMN = 3;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.automat.monitor.AutomatTableBundle");

    AutomatSummary summary = AutomatSummary.instance();

    public AutomatTableDataModel() {
        super();
        // listen for new/gone/changed Automat instances
        summary.addPropertyChangeListener(this);
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("Insert")) {
            // fireTableRowsInserted(((Integer)e.getNewValue()).intValue(), ((Integer)e.getNewValue()).intValue());
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("Remove")) {
            //fireTableRowsDeleted(((Integer)e.getNewValue()).intValue(), ((Integer)e.getNewValue()).intValue());
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("Count")) {
            // it's a count indication, so update TURNS
            int row = ((Integer) e.getNewValue()).intValue();
            // length might have changed...
            if (row < getRowCount()) {
                fireTableCellUpdated(row, TURNSCOL);
            }
        } else {
            log.warn("Unexpected property named " + e.getPropertyName());
        }
    }

    public int getColumnCount() {
        return NUMCOLUMN;
    }

    public int getRowCount() {
        return AutomatSummary.instance().length();
    }

    public String getColumnName(int col) {
        switch (col) {
            case NAMECOL:
                return "Name";
            case TURNSCOL:
                return "Cycles";
            case KILLCOL:
                return "Kill";  // problem if this is blank?

            default:
                return "unknown";
        }
    }

    /**
     * Note that this returns String even for columns that contain buttons
     */
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case NAMECOL:
            case KILLCOL:
                return String.class;
            case TURNSCOL:
                return Integer.class;
            default:
                return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case KILLCOL:
                return true;
            default:
                return false;
        }
    }

    public Object getValueAt(int row, int col) {
        switch (col) {
            case NAMECOL:
                return summary.get(row).getName();
            case TURNSCOL:
                return Integer.valueOf(summary.get(row).getCount());
            case KILLCOL:  // return button text here
                return rb.getString("ButtonKill");
            default:
                log.error("internal state inconsistent with table requst for " + row + " " + col);
                return null;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case NAMECOL:
                return new JTextField(20).getPreferredSize().width;
            case TURNSCOL:
                return new JTextField(5).getPreferredSize().width;
            case KILLCOL:
                return new JButton(rb.getString("ButtonKill")).getPreferredSize().width;
            default:
                log.warn("Unexpected column in getPreferredWidth: " + col);
                return new JTextField(5).getPreferredSize().width;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == KILLCOL) {
            // button fired, handle
            summary.get(row).stop();
        }
    }

    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent.
     *
     * @param table
     */
    public void configureTable(JTable table) {
        // allow reordering of the columns
        table.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);

        // have the value column hold a button
        setColumnToHoldButton(table, KILLCOL, new JButton(rb.getString("ButtonKill")));
    }

    /**
     * Service method to setup a column so that it will hold a button for it's
     * values
     *
     * @param table
     * @param column
     * @param sample Typical button, used for size
     */
    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        TableColumnModel tcm = table.getColumnModel();
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth(sample.getPreferredSize().width);
    }

    synchronized public void dispose() {
        AutomatSummary.instance().removePropertyChangeListener(this);
    }

    private final static Logger log = LoggerFactory.getLogger(AutomatTableDataModel.class.getName());

}
