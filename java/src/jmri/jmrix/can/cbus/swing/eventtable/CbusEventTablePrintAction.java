package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.davidflanagan.HardcopyWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Print or Print Preview Action for CBUS Event Table
 */
public class CbusEventTablePrintAction extends AbstractAction {
    
    private final static int[] whichPrintColumns = {CbusEventTableDataModel.NODE_COLUMN,
        CbusEventTableDataModel.EVENT_COLUMN,CbusEventTableDataModel.NAME_COLUMN,
        CbusEventTableDataModel.NODENAME_COLUMN,CbusEventTableDataModel.COMMENT_COLUMN};

    private final String _title;
    private final CbusEventTableDataModel _model;
    private final boolean _preview;
    
    /**
     * Create a new Save to CSV Action.
     * 
     * @param actionName Action Name
     * @param model Table Model to use.
     * @param title Page Title.
     * @param preview True to preview, false to print.
     */
    public CbusEventTablePrintAction(String actionName, @Nonnull CbusEventTableDataModel model, 
        @Nonnull String title, boolean preview ){
        super(actionName);
        _model = model;
        _title = title;
        _preview = preview;
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        jmri.util.ThreadingUtil.runOnGUIEventually( () -> {
            HardcopyWriter writer;
            try {
                writer = new HardcopyWriter(new Frame(), _title, 10, .8, .5, .5, .5, _preview);
            } catch (HardcopyWriter.PrintCanceledException ex) {
                // log.debug("Preview cancelled");
                return;
            }
            writer.increaseLineSpacing(20);
            printTable(writer); // close() is taken care of in printTable()
            writer.close();
        });
    }
    
    /**
     * Self print or print preview the table.
     * <p>
     * Copied from BeanTableDataModel modified to print variable column widths.
     * Final column with size zero runs to extent of page width.
     * <p>
     * Printed with headings and vertical lines between each column. Data is
     * word wrapped within a column. 
     * 
     * @param w the writer to print to
     */
    private void printTable(HardcopyWriter w ) {
        
        // [AC] variable column sizes
        
        // column header labels
        String[] columnStrings = new String[whichPrintColumns.length];
        
        int[] columnWidth = new int[whichPrintColumns.length];
        // in a test, thats 86 chars on a line
        
        colWidthLoop(columnStrings, columnWidth, w);
        
        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());
        
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnWidth);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());

        getEachRow(w, columnStrings, columnWidth);
        
        
    }
    
    private void colWidthLoop(String[] columnStrings, int[] columnWidth, HardcopyWriter w){
        int columnTotal = 0;
        for (int i = 0; i < whichPrintColumns.length; i++) {
            // Put each column header in the array
            columnStrings[i] = _model.getColumnName(whichPrintColumns[i]);
            
            int columnworkedon=whichPrintColumns[i];
            
            if (getColumnWidth(columnworkedon) == 0) {
                // Fill to end of line
                columnWidth[i] = w.getCharactersPerLine() - columnTotal;
            } else {
                columnWidth[i] = getColumnWidth(columnworkedon);
                columnTotal = columnTotal + columnWidth[i] + 1;
            }
        }
    }
    
    private void getEachRow(HardcopyWriter w, String[] columnStrings, int[] columnWidth){
    
        // now print each row of data
        // create a base string the width of the column
        for (int i = 0; i < _model.getRowCount(); i++) {
            for (int k = 0; k < whichPrintColumns.length; k++) {
                
                int j=whichPrintColumns[k];
                
                //check for special, non string contents
                if (_model.getValueAt(i, j) instanceof Integer) {
                    columnStrings[k] = (_model.getValueAt(i, j)).toString();
                } else {
                    columnStrings[k] = (String) _model.getValueAt(i, j);
                }
            }
            
            printColumns(w, columnStrings, columnWidth);
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    w.getCharactersPerLine());
        }
    }

    // [AC] modified to take an array of column widths
    private void printColumns(HardcopyWriter w, String columnStrings[], int columnWidth[]) {
        String columnString = "";
        String lineString = "";
        String spaces;
        // loop through each column
        boolean complete = false;
        while (!complete) {
            complete = true;
            for (int i = 0; i < columnStrings.length; i++) {
                // create a base string the width of the column
                StringBuilder buf = new StringBuilder();
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
                        // log.debug("1050 columnString {}",columnString);
                    }
                    
                    // log.debug("1053 noword is {} ",noWord);
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
           for (int i = 0; i < whichPrintColumns.length; i++) {
                    w.write(w.getCurrentLineNumber(), column, w.getCurrentLineNumber() + 1, column);
                    column = column + columnWidth[i] + 1;
                    // log.debug("1167 i is {} column is {} columnWidth[i] is {} ", i, column, columnWidth[i]);
                }
                w.write(w.getCurrentLineNumber(), w.getCharactersPerLine(), 
                    w.getCurrentLineNumber() + 1, w.getCharactersPerLine());
                lineString = "\n";
                w.write(lineString);
                lineString = "";
            } catch (IOException e) {
                log.warn("error during printing: {}", e);
            }
        }
    }
    
    
    /**
     * Returns int of column width.
     * <p>
     * Just used for printing.
     * in a test, there is 86 chars on a line
     * -1 is invalid
     * 0 is final column extend to end
     *
     * @param col int col number
     * @return print width
     */
    private static int getColumnWidth(int col) {
        switch (col) {
            case CbusEventTableDataModel.NAME_COLUMN:
                return 14;
            case CbusEventTableDataModel.COMMENT_COLUMN:
                return 0; // 0 to get writer recognize it as the last column, will fill with spaces
            default:
                return 8;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventTablePrintAction.class);
    
}
