package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.CanInterface;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.util.FileUtil;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus events
 *
 * @author Andrew Crosland (C) 2009
 */
public class CbusEventTableDataModel extends javax.swing.table.AbstractTableModel implements CanListener {

    static public final int IDCOLUMN = 0;
    static public final int NODECOLUMN = IDCOLUMN + 1;
    static public final int NAMECOLUMN = NODECOLUMN + 1;
    static public final int EVENTCOLUMN = NAMECOLUMN + 1;
    static public final int TYPECOLUMN = EVENTCOLUMN + 1;
    static public final int COMMENTCOLUMN = TYPECOLUMN + 1;

    static public final int NUMCOLUMN = 6;

    final JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    CanSystemConnectionMemo memo;

    CbusEventTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        _id = new int[CbusConstants.MAX_TABLE_EVENTS];
        _node = new int[CbusConstants.MAX_TABLE_EVENTS];
        _name = new String[CbusConstants.MAX_TABLE_EVENTS];
        _event = new int[CbusConstants.MAX_TABLE_EVENTS];
        _type = new boolean[CbusConstants.MAX_TABLE_EVENTS];
        _comment = new String[CbusConstants.MAX_TABLE_EVENTS];
        // connect to the CanInterface
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    /**
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {
        return _rowCount;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case IDCOLUMN:
                return Bundle.getMessage("ColumnID");
            case NODECOLUMN:
                return Bundle.getMessage("ColumnNode");
            case NAMECOLUMN:
                return Bundle.getMessage("ColumnName");
            case EVENTCOLUMN:
                return Bundle.getMessage("ColumnEvent");
            case TYPECOLUMN:
                return Bundle.getMessage("ColumnType");
            case COMMENTCOLUMN:
                return Bundle.getMessage("ColumnComment");
            default:
                return "unknown"; // NOI18N
        }
    }

    public int getColumnWidth(int col) {
        switch (col) {
            case IDCOLUMN:
                return 7;
            case NODECOLUMN:
                return 7;
            case NAMECOLUMN:
                return 20;
            case EVENTCOLUMN:
                return 7;
            case TYPECOLUMN:
                return 7;
            case COMMENTCOLUMN:
                return 0; // to get writer recognize it as the last column, will fill with spaces
            default:
                return -1;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case IDCOLUMN:
            case NODECOLUMN:
            case EVENTCOLUMN:
                return Integer.class;
            case TYPECOLUMN:
            case NAMECOLUMN:
            case COMMENTCOLUMN:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NAMECOLUMN:
            case COMMENTCOLUMN:
                return true;
            default:
                return false;
        }
    }

    static final Boolean True = Boolean.valueOf("true");
    static final Boolean False = Boolean.valueOf("false");

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case IDCOLUMN:
                return _id[row];
            case NODECOLUMN:
                return _node[row];
            case NAMECOLUMN:
                return _name[row];
            case EVENTCOLUMN:
                return _event[row];
            case TYPECOLUMN:
                if (_type[row]) {
                    return Bundle.getMessage("PowerStateOn");
                } else {
                    return Bundle.getMessage("PowerStateOff");
                }
            case COMMENTCOLUMN:
                return _comment[row];

            default:
                log.error("internal state inconsistent with table request for row {} col {}", row, col);
                return null;
        }
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case IDCOLUMN:
                return new JTextField(5).getPreferredSize().width;
            case NODECOLUMN:
                return new JTextField(5).getPreferredSize().width;
            case NAMECOLUMN:
                return new JTextField(18).getPreferredSize().width;
            case EVENTCOLUMN:
                return new JTextField(5).getPreferredSize().width;
            case TYPECOLUMN:
                return new JTextField(3).getPreferredSize().width;
            case COMMENTCOLUMN:
                return new JTextField(30).getPreferredSize().width;
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }

    /**
     * Capture new comments or node names.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == NAMECOLUMN) {
            _name[row] = (String) value;
            fireTableRowsUpdated(row, row);
            // look for other occurrences of the same node
            for (int i = 0; i < _rowCount; i++) {
                // ignore this one
                if (i != row) {
                    if (_node[i] == _node[row]) {
                        // copy the name
                        _name[i] = _name[row];
                        fireTableRowsUpdated(i, i);
                    }
                }
            }
        }
        if (col == COMMENTCOLUMN) {
            _comment[row] = (String) value;
            fireTableRowsUpdated(row, row);
        }
        // table is dirty
        _saved = false;
    }

    /**
     * Configure a table to have our standard rows and columns.
     * <p>
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     *
     */
    public void configureTable(JTable eventTable) {
        // allow reordering of the columns
        eventTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        eventTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < eventTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            eventTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        eventTable.sizeColumnsToFit(-1);
    }

    /**
     * Report whether table has changed.
     *
     */
    public boolean isTableDirty() {
        return(_saved == false);
    }

    /**
     * Capture node and event and add to table.
     */
    @Override
    public void message(CanMessage m) {
        log.debug("Received new message event: {}", m);
        _id[_rowCount] = CbusMessage.getId(m);
        _node[_rowCount] = m.getElement(1) * 256 + m.getElement(2);
        _event[_rowCount] = m.getElement(3) * 256 + m.getElement(4);
        _type[_rowCount] = (m.getOpCode() & 1) == 0;
        addEvent();
    }

    /**
     * Capture node and event and add to table.
     */
    @Override
    public void reply(CanReply m) {
        log.debug("Received new reply event: {}", m);
        _id[_rowCount] = CbusMessage.getId(m);
        _node[_rowCount] = m.getElement(1) * 256 + m.getElement(2);
        _event[_rowCount] = m.getElement(3) * 256 + m.getElement(4);
        _type[_rowCount] = (m.getOpCode() & 1) == 0;
        addEvent();
    }

    /**
     * Register new event.
     */
    public synchronized void addEvent() {
        if (_rowCount < CbusConstants.MAX_TABLE_EVENTS) {
            // notify the JTable object that a row has changed; do that in the Swing thread!
            Runnable r = new Notify(_rowCount, this);   // -1 in first arg means all
            javax.swing.SwingUtilities.invokeLater(r);
            _rowCount++;
        }
    }

    static class Notify implements Runnable {

        private int _row;
        javax.swing.table.AbstractTableModel _model;

        public Notify(int row, javax.swing.table.AbstractTableModel model) {
            _row = row;
            _model = model;
        }

        @Override
        public void run() {
            // notify that row is added
            log.debug("Table added row: {}", _row);
            _model.fireTableRowsInserted(_row, _row);
        }
    }

    public void dispose() {
        // table.removeAllElements();
        // table = null;
    }

    /**
     * Self save as a .csv file.
     */
    public void saveTable() {
        // check for empty table
        if (this.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("EmptyTableDialogString"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (_saveFile == null) {
            saveAsTable();
        } else {
            saveToCSV();
        }
    }

    /**
     * Self save as a .csv file, first prompting for a filename.
     */
    public void saveAsTable() {
        // start at current file, show dialog
        int retVal = fileChooser.showSaveDialog(null);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            _saveFileName = fileChooser.getSelectedFile().getPath();
            if (_saveFileName != null) {
                _saveFile = new File(_saveFileName);
                log.debug("File chosen: {}", _saveFileName);
            } else {
                log.debug("saveAsTable: No file name available. Aborted");
                return;
            }
        } else {
            return; // cancelled or pane closed, prevent NPE
        }
        saveToCSV();
    }

    @SuppressWarnings("null")
    private void saveToCSV() {
        FileOutputStream out = null;
        PrintWriter p = null;
        if (_saveFileName == null) {
            log.error("saveToCSV: No file name available. Aborted");
            return;
        }
        try {
            // Create a print writer based on the file, so we can print to it.
            out = new FileOutputStream(_saveFileName);
            p = new PrintWriter(out, true);
        } catch (IOException e) {
                log.debug("Problem creating output stream");
        }

        if (out == null) {
            log.debug("Null File Output Stream");
        }
        if (p == null) { // certainly null if out == null
            log.error("Null Print Writer");
            return;
        }

        // Save table per row. We've checked for an empty table in SaveTable()
        // print header labels
        for (int i = 0; i < this.getColumnCount() - 1; i++) {
            p.print(this.getColumnName(i));
            p.print(",");
        }
        p.println(this.getColumnName(getColumnCount() - 1)); // last column, without comma
        // print rows
        for (int i = 0; i < this.getRowCount(); i++) {
            p.print(_id[i]);
            p.print(",");
            p.print(_node[i]);
            p.print(",");
            p.print(_name[i]);
            p.print(",");
            p.print(_event[i]);
            p.print(",");
            if (_type[i]) {
                p.print(Bundle.getMessage("PowerStateOn"));
            } else {
                p.print(Bundle.getMessage("PowerStateOff"));
            }
            p.print(",");
            if (_comment[i] == null) {
                p.println("");
            } else {
                p.println(_comment[i]);
            }
        }

        try {
            p.flush();
            p.close();
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {

        }
        // mark that the table has been saved
        _saved = true; // TODO disable the Save menu item in CbusEventTablePane
    }

    /**
     * Self print or print preview the table.
     * <p>
     * Copied from BeanTableDataModel modified to print variable column widths.
     * Final column with size zero runs to extent of page width.
     *
     * Printed with headings and vertical lines between each column. Data is
     * word wrapped within a column. Can handle data as strings, integers,
     * comboboxes or booleans
     */
    @SuppressWarnings("unchecked")
    public void printTable(HardcopyWriter w) {
        // [AC] variable column sizes
        int columnTotal = 0;
        int[] columnWidth = new int[this.getColumnCount()];
        // in a test, thats 86 chars on a line
        for (int i = 0; i < this.getColumnCount(); i++) {
            if (this.getColumnWidth(i) == 0) {
                // Fill to end of line
                columnWidth[i] = w.getCharactersPerLine() - columnTotal;
            } else {
                columnWidth[i] = this.getColumnWidth(i);
                columnTotal = columnTotal + columnWidth[i] + 1;
            }
        }

        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());

        // print the column header labels
        String[] columnStrings = new String[this.getColumnCount()];
        // Put each column header in the array
        for (int i = 0; i < this.getColumnCount(); i++) {
            columnStrings[i] = this.getColumnName(i);
        }
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnWidth);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());

        // now print each row of data
        // create a base string the width of the column
        String spaces;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < this.getRowCount() - 1; i++) {
            //spaces = "";
            for (int j = 0; j < columnWidth[i]; j++) {
                //spaces = spaces + " ";
                buf.append(" ");
            }
            spaces = buf.toString();
            for (int j = 0; j < this.getColumnCount(); j++) {
                //check for special, non string contents
                if (this.getValueAt(i, j) == null) {
                    columnStrings[j] = spaces;
                } else if (this.getValueAt(i, j) instanceof JComboBox) {
                    columnStrings[j] = (String) ((JComboBox<String>) this.getValueAt(i, j)).getSelectedItem();
                } else if (this.getValueAt(i, j) instanceof Boolean) {
                    columnStrings[j] = (this.getValueAt(i, j)).toString();
                } else if (this.getValueAt(i, j) instanceof Integer) {
                    columnStrings[j] = (this.getValueAt(i, j)).toString();
                } else {
                    columnStrings[j] = (String) this.getValueAt(i, j);
                }
            }
            printColumns(w, columnStrings, columnWidth);
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    w.getCharactersPerLine());
        }
        w.close();
    }

    // [AC] modified to take an array of column widths
    protected void printColumns(HardcopyWriter w, String columnStrings[], int columnWidth[]) {
        String columnString = "";
        String lineString = "";
        String spaces;
        // loop through each column
        boolean complete = false;
        while (!complete) {
            complete = true;
            for (int i = 0; i < columnStrings.length; i++) {
                // create a base string the width of the column
                StringBuffer buf = new StringBuffer();
                for (int j = 0; j < columnWidth[i]; j++) {
                    buf.append(" ");
                }
                spaces = buf.toString();
                // if the column string is too wide, cut it at word boundary (valid delimiters are space, - and _)
                // Use the intial part of the text, pad it with spaces and place the remainder back in the array
                // for further processing on next line.
                // If column string isn't too wide, pad it to column width with spaces if needed
                if (columnStrings[i].length() > columnWidth[i]) {
                    boolean noWord = true;
                    for (int k = columnWidth[i]; k >= 1; k--) {
                        if (columnStrings[i].substring(k - 1, k).equals(" ")
                                || columnStrings[i].substring(k - 1, k).equals("-")
                                || columnStrings[i].substring(k - 1, k).equals("_")) {
                            columnString = columnStrings[i].substring(0, k)
                                    + spaces.substring(k);
                            columnStrings[i] = columnStrings[i].substring(k);
                            noWord = false;
                            complete = false;
                            break;
                        }
                    }
                    if (noWord) { // not breakable, hard break
                        columnString = columnStrings[i].substring(0, columnWidth[i]);
                        columnStrings[i] = columnStrings[i].substring(columnWidth[i]);
                        complete = false;
                    }

                } else {
                    columnString = columnStrings[i] + spaces.substring(columnStrings[i].length()); // pad with spaces
                    columnStrings[i] = "";
                }
                lineString = lineString + columnString + " ";
            }
            try {
                w.write(lineString);
                //write vertical dividing lines
                int column = 0;
                for (int i = 0; i < this.getColumnCount(); i++) {
                    w.write(w.getCurrentLineNumber(), column, w.getCurrentLineNumber() + 1, column);
                    column = column + columnWidth[i] + 1;
                }
                w.write(w.getCurrentLineNumber(), w.getCharactersPerLine(), w.getCurrentLineNumber() + 1, w.getCharactersPerLine());
                lineString = "\n";
                w.write(lineString);
                lineString = "";
            } catch (IOException e) {
                log.warn("error during printing: " + e);
            }
        }
    }

    // private data
    private CanInterface tc = null;
    private int _rowCount = 0;
    private int[] _id = null;
    private int[] _node = null;
    private String[] _name = null;
    private int[] _event = null;
    private boolean[] _type = null;
    private String[] _comment = null;
    private File _saveFile = null;
    private String _saveFileName = null;
    @SuppressWarnings("unused")
    private boolean _saved = false;

    private final static Logger log = LoggerFactory.getLogger(CbusEventTableDataModel.class);
}
