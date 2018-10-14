package jmri.jmrix.loconet.soundloader;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Font;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import jmri.jmrix.loconet.spjfile.SpjFile;
import jmri.util.FileUtil;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Digitrax SPJ files.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
 * @author Dennis Miller Copyright (C) 2006
 */
public class EditorTableDataModel extends javax.swing.table.AbstractTableModel {

    static public final int HEADERCOL = 0;
    static public final int TYPECOL = 1;
    static public final int MAPCOL = 2;
    static public final int HANDLECOL = 3;
    static public final int FILENAMECOL = 4;
    static public final int LENGTHCOL = 5;
    static public final int PLAYBUTTONCOL = 6;
    static public final int REPLACEBUTTONCOL = 7;

    static public final int NUMCOLUMN = 8;

    SpjFile file;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "cache resource at 1st start, threading OK") // NOI18N
    public EditorTableDataModel(SpjFile file) {
        super();
        this.file = file;
    }

    @Override
    public int getRowCount() {
        // The 0th header is not displayed
        return file.numHeaders() - 1;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case HEADERCOL:
                return Bundle.getMessage("HeaderHEADERCOL");
            case TYPECOL:
                return Bundle.getMessage("HeaderTYPECOL");
            case HANDLECOL:
                return Bundle.getMessage("HeaderHANDLECOL");
            case MAPCOL:
                return Bundle.getMessage("HeaderMAPCOL");
            case FILENAMECOL:
                return Bundle.getMessage("HeaderFILENAMECOL");
            case LENGTHCOL:
                return Bundle.getMessage("HeaderLENGTHCOL");
            case PLAYBUTTONCOL:
                return ""; // no title
            case REPLACEBUTTONCOL:
                return ""; // no title

            default:
                return "unknown";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case HEADERCOL:
            case HANDLECOL:
                return Integer.class;
            case LENGTHCOL:
                return Float.class;
            case MAPCOL:
            case TYPECOL:
            case FILENAMECOL:
                return String.class;
            case REPLACEBUTTONCOL:
            case PLAYBUTTONCOL:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case REPLACEBUTTONCOL:
            case PLAYBUTTONCOL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case HEADERCOL:
                return Integer.valueOf(row);
            case HANDLECOL:
                return Integer.valueOf(file.getHeader(row + 1).getHandle());
            case MAPCOL:
                return file.getMapEntry(file.getHeader(row + 1).getHandle());
            case FILENAMECOL:
                return "" + file.getHeader(row + 1).getName();
            case TYPECOL:
                return file.getHeader(row + 1).typeAsString();
            case LENGTHCOL:
                if (!file.getHeader(row + 1).isWAV()) {
                    return null;
                }
                float rate = (new jmri.jmrit.sound.WavBuffer(file.getHeader(row + 1).getByteArray())).getSampleRate();
                if (rate == 0.f) {
                    log.error("Rate should not be zero");
                    return null;
                }
                float time = file.getHeader(row + 1).getDataLength() / rate;
                return Float.valueOf(time);
            case PLAYBUTTONCOL:
                if (file.getHeader(row + 1).isWAV()) {
                    return Bundle.getMessage("ButtonPlay");
                } else if (file.getHeader(row + 1).isTxt()) {
                    return Bundle.getMessage("ButtonView");
                } else if (file.getHeader(row + 1).isMap()) {
                    return Bundle.getMessage("ButtonView");
                } else if (file.getHeader(row + 1).isSDF()) {
                    return Bundle.getMessage("ButtonView");
                } else {
                    return null;
                }
            case REPLACEBUTTONCOL:
                if (file.getHeader(row + 1).isWAV()) {
                    return Bundle.getMessage("ButtonReplace");
                }
                if (file.getHeader(row + 1).isSDF()) {
                    return Bundle.getMessage("ButtonEdit");
                } else {
                    return null;
                }
            default:
                log.error("internal state inconsistent with table requst for " + row + " " + col);
                return null;
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
            justification = "better to keep cases in column order rather than to combine")
    public int getPreferredWidth(int col) {
        JTextField b;
        switch (col) {
            case TYPECOL:
                return new JTextField(8).getPreferredSize().width;
            case MAPCOL:
                return new JTextField(12).getPreferredSize().width;
            case HEADERCOL:
            case HANDLECOL:
                return new JTextField(3).getPreferredSize().width;
            case FILENAMECOL:
                return new JTextField(12).getPreferredSize().width;
            case LENGTHCOL:
                return new JTextField(5).getPreferredSize().width;
            case PLAYBUTTONCOL:
                b = new JTextField((String) getValueAt(1, PLAYBUTTONCOL));
                return b.getPreferredSize().width + 30;
            case REPLACEBUTTONCOL:
                b = new JTextField((String) getValueAt(1, REPLACEBUTTONCOL));
                return b.getPreferredSize().width + 30;
            default:
                log.warn("Unexpected column in getPreferredWidth: " + col);
                return new JTextField(8).getPreferredSize().width;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == PLAYBUTTONCOL) {
            // button fired, handle
            if (file.getHeader(row + 1).isWAV()) {
                playButtonPressed(value, row, col);
                return;
            } else if (file.getHeader(row + 1).isTxt()) {
                viewTxtButtonPressed(value, row, col);
                return;
            } else if (file.getHeader(row + 1).isMap()) {
                viewTxtButtonPressed(value, row, col);
                return;
            } else if (file.getHeader(row + 1).isSDF()) {
                viewSdfButtonPressed(value, row, col);
                return;
            }
        } else if (col == REPLACEBUTTONCOL) {
            // button fired, handle
            if (file.getHeader(row + 1).isWAV()) {
                replWavButtonPressed(value, row, col);
            } else if (file.getHeader(row + 1).isSDF()) {
                editSdfButtonPressed(value, row, col);
                return;
            }
        }
    }

    // should probably be abstract and put in invoking GUI
    static JFileChooser chooser;  // shared across all uses

    void replWavButtonPressed(Object value, int row, int col) {
        if (chooser == null) {
            chooser = new JFileChooser(FileUtil.getUserFilesPath());
        }
        chooser.rescanCurrentDirectory();
        int retVal = chooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        // load file
        jmri.jmrit.sound.WavBuffer buff;
        try {
            buff = new jmri.jmrit.sound.WavBuffer(chooser.getSelectedFile());
        } catch (Exception e) {
            log.error("Exception loading file: " + e);
            return;
        }
        // store to memory
        file.getHeader(row + 1).setContent(buff.getByteArray(), buff.getDataStart(), buff.getDataSize());
        // update rest of header
        file.getHeader(row + 1).setName(chooser.getSelectedFile().getName());

        // mark table changes in other rows
        fireTableRowsUpdated(row, row);
    }

    // should probably be abstract and put in invoking GUI
    void playButtonPressed(Object value, int row, int col) {
        // new jmri.jmrit.sound.WavBuffer(file.getHeader(row+1).getByteArray());
        jmri.jmrit.sound.SoundUtil.playSoundBuffer(file.getHeader(row + 1).getByteArray());
    }

    // should probably be abstract and put in invoking GUI
    // Also used to display the .map block
    void viewTxtButtonPressed(Object value, int row, int col) {
        String content = new String(file.getHeader(row + 1).getByteArray());
        JFrame frame = new JFrame();
        JTextArea text = new JTextArea(content);
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, text.getFont().getSize())); // NOI18N
        frame.getContentPane().add(new JScrollPane(text));
        frame.pack();
        frame.setVisible(true);
    }

    // should probably be abstract and put in invoking GUI
    void viewSdfButtonPressed(Object value, int row, int col) {
        jmri.jmrix.loconet.sdf.SdfBuffer buff = new jmri.jmrix.loconet.sdf.SdfBuffer(file.getHeader(row + 1).getByteArray());
        String content = buff.toString();
        JFrame frame = new jmri.util.JmriJFrame(Bundle.getMessage("TitleSdfView"));
        JTextArea text = new JTextArea(content);
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, text.getFont().getSize())); // NOI18N
        frame.getContentPane().add(new JScrollPane(text));
        frame.pack();
        frame.setVisible(true);
    }

    // should probably be abstract and put in invoking GUI
    void editSdfButtonPressed(Object value, int row, int col) {
        jmri.jmrix.loconet.sdfeditor.EditorFrame sdfEditor
                = new jmri.jmrix.loconet.sdfeditor.EditorFrame(file.getHeader(row + 1).getSdfBuffer());
        sdfEditor.setVisible(true);
    }

    /**
     * Configure a table to have our standard rows and columns. This is
     * optional, in that other table formats can use this table model. But we
     * put it here to help keep it consistent.
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
        //table.sizeColumnsToFit(-1);

        // have the value column hold a button
        setColumnToHoldButton(table, PLAYBUTTONCOL, largestWidthButton(PLAYBUTTONCOL));
        setColumnToHoldButton(table, REPLACEBUTTONCOL, largestWidthButton(REPLACEBUTTONCOL));
    }

    public JButton largestWidthButton(int col) {
        JButton retval = new JButton("TTTT");
        if (col == PLAYBUTTONCOL) {
            retval = checkLabelWidth(retval, "ButtonPlay");
            retval = checkLabelWidth(retval, "ButtonView");
        } else if (col == REPLACEBUTTONCOL) {
            retval = checkLabelWidth(retval, "ButtonEdit");
            retval = checkLabelWidth(retval, "ButtonReplace");
        }
        return retval;
    }

    private JButton checkLabelWidth(JButton now, String name) {
        JButton b = new JButton(Bundle.getMessage(name));
        b.revalidate();
        if (b.getPreferredSize().width > now.getPreferredSize().width) {
            return b;
        } else {
            return now;
        }
    }

    /**
     * Service method to set up a column so that it will hold a button for it's
     * values.
     *
     * @param sample Typical button, used for size
     */
    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        //TableColumnModel tcm = table.getColumnModel();
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        table.setDefaultEditor(JButton.class, buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth(sample.getPreferredSize().width + 30);
    }

    synchronized public void dispose() {
    }

    /**
     * Self print - or print preview - the table.
     * <p>
     * Printed in equally sized
     * columns across the page with headings and vertical lines between each
     * column. Data is word wrapped within a column. Can handle data as strings,
     * comboboxes or booleans.
     *
     * @param w the printer output to write to
     */
    public void printTable(HardcopyWriter w) {
        // determine the column size - evenly sized, with space between for lines
        int columnSize = (w.getCharactersPerLine() - this.getColumnCount() - 1) / this.getColumnCount();

        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                (columnSize + 1) * this.getColumnCount());

        // print the column header labels
        String[] columnStrings = new String[this.getColumnCount()];
        // Put each column header in the array
        for (int i = 0; i < this.getColumnCount(); i++) {
            columnStrings[i] = this.getColumnName(i);
        }
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnSize);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                (columnSize + 1) * this.getColumnCount());

        // now print each row of data
        // create a base string the width of the column
        StringBuilder spaces = new StringBuilder("");
        for (int i = 0; i < columnSize; i++) {
            spaces.append(" ");
        }
        for (int i = 0; i < this.getRowCount(); i++) {
            for (int j = 0; j < this.getColumnCount(); j++) {
                //check for special, non string contents
                if (this.getValueAt(i, j) == null) {
                    columnStrings[j] = spaces.toString();
                } else if (this.getValueAt(i, j) instanceof JComboBox) {
                    columnStrings[j] = (String) ((JComboBox<?>) this.getValueAt(i, j)).getSelectedItem();
                } else if (this.getValueAt(i, j) instanceof Boolean) {
                    columnStrings[j] = (this.getValueAt(i, j)).toString();
                } else {
                    columnStrings[j] = (String) this.getValueAt(i, j);
                }
            }
            printColumns(w, columnStrings, columnSize);
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    (columnSize + 1) * this.getColumnCount());
        }
        w.close();
    }

    protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize) {
        String columnString = "";
        StringBuilder lineString = new StringBuilder("");
        // create a base string the width of the column
        StringBuilder spaces = new StringBuilder("");
        for (int i = 0; i < columnSize; i++) {
            spaces.append(" ");
        }
        // loop through each column
        boolean complete = false;
        while (!complete) {
            complete = true;
            for (int i = 0; i < columnStrings.length; i++) {
                // if the column string is too wide cut it at word boundary (valid delimiters are space, - and _)
                // use the intial part of the text,pad it with spaces and place the remainder back in the array
                // for further processing on next line
                // if column string isn't too wide, pad it to column width with spaces if needed
                if (columnStrings[i].length() > columnSize) {
                    boolean noWord = true;
                    for (int k = columnSize; k >= 1; k--) {
                        if (columnStrings[i].substring(k - 1, k).equals(" ")
                                || columnStrings[i].substring(k - 1, k).equals("-")
                                || columnStrings[i].substring(k - 1, k).equals("_")) {
                            columnString = columnStrings[i].substring(0, k)
                                    + spaces.substring(columnStrings[i].substring(0, k).length());
                            columnStrings[i] = columnStrings[i].substring(k);
                            noWord = false;
                            complete = false;
                            break;
                        }
                    }
                    if (noWord) {
                        columnString = columnStrings[i].substring(0, columnSize);
                        columnStrings[i] = columnStrings[i].substring(columnSize);
                        complete = false;
                    }

                } else {
                    columnString = columnStrings[i] + spaces.substring(columnStrings[i].length());
                    columnStrings[i] = "";
                }
                lineString.append(columnString).append(" ");
            }
            try {
                w.write(lineString.toString());
                //write vertical dividing lines
                for (int i = 0; i < w.getCharactersPerLine(); i = i + columnSize + 1) {
                    w.write(w.getCurrentLineNumber(), i, w.getCurrentLineNumber() + 1, i);
                }
                w.write("\n"); // NOI18N
                lineString = new StringBuilder("");
            } catch (IOException e) {
                log.warn("error during printing:", e);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditorTableDataModel.class);

}
