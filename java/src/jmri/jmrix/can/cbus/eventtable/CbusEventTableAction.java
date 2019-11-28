package jmri.jmrix.can.cbus.eventtable;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.cbus.CbusLight;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.CbusSensor;
import jmri.jmrix.can.cbus.CbusTurnout;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.FileUtil;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data actions 
 * xml file operations modified from TimeTableXml.java
 *
 * @author Steve Young (c) 2018, 2019
 * 
 */
public class CbusEventTableAction {

    CbusEventTableDataModel _model;
    
    final JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    public CbusEventTableAction( CbusEventTableDataModel model) {
        _model = model;
    }
    
    public File _saveFile = null;
    private String _saveFileName = null;
    protected boolean sessionConfirmDeleteRow=true; // display confirm popup
    protected SimpleDateFormat xmlDateStyle = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
    
    private void updatejmricell(int row, Boolean ison, String name){
        String bb;
        if (ison) {
            bb = _model.getValueAt(row, CbusEventTableDataModel.STLR_ON_COLUMN).toString();
            _model.setValueAt(bb + " " + name, row, CbusEventTableDataModel.STLR_ON_COLUMN);
        }
        else {
            bb = _model.getValueAt(row, CbusEventTableDataModel.STLR_OFF_COLUMN).toString();
            _model.setValueAt(bb + " " + name, row, CbusEventTableDataModel.STLR_OFF_COLUMN);
        }
    }
    
    private void linkHwaddtoEvent(CanMessage m, String text, String name ){
        int event = CbusMessage.getEvent(m);
        int node = CbusMessage.getNodeNumber(m);
        int opc = CbusMessage.getOpcode(m);
        int row = _model.seeIfEventOnTable( node, event);
        if (row<0) {
            _model.addEvent(node,event,0,CbusTableEvent.EvState.UNKNOWN,name,"",0,0,0,0);
            row = _model.seeIfEventOnTable( node, event );
        }
        updatejmricell(row, CbusOpCodes.isOnEvent(opc), text );
    }
    
    /**
     * Update all columns for JMRI Sensor, Turnout and light details
     */
    public void updatejmricols(){
        // reset all columns
        for (int i = 0; i < _model.getRowCount(); i++) {
            _model.setValueAt("", i, CbusEventTableDataModel.STLR_ON_COLUMN);
            _model.setValueAt("", i, CbusEventTableDataModel.STLR_OFF_COLUMN);
        }
        jmri.SensorManager sm = InstanceManager.getDefault(jmri.SensorManager.class);
        sm.getNamedBeanSet().forEach((nb) -> {
            if (nb instanceof CbusSensor) {
                CbusSensor cs = (CbusSensor) sm.provideSensor(nb.toString());
                String text = Bundle.getMessage("cbSensActive",nb.getDisplayName());
                linkHwaddtoEvent( cs.getAddrActive(), text, nb.getDisplayName() );
                text = Bundle.getMessage("cbSensInactive",nb.getDisplayName());
                linkHwaddtoEvent( cs.getAddrInactive(), text, nb.getDisplayName() );
            }
        });
        jmri.TurnoutManager tm = InstanceManager.getDefault(jmri.TurnoutManager.class);
        tm.getNamedBeanSet().forEach((nb) -> {
            if (nb instanceof CbusTurnout) {
                CbusTurnout ct = (CbusTurnout) tm.provideTurnout(nb.toString());
                String text = Bundle.getMessage("cbTurnThrown",nb.getDisplayName());
                linkHwaddtoEvent( ct.getAddrThrown(), text, nb.getDisplayName() );
                text = Bundle.getMessage("cbTurnClosed",nb.getDisplayName());
                linkHwaddtoEvent( ct.getAddrClosed(), text, nb.getDisplayName() );
            }
        });
        jmri.LightManager lm = InstanceManager.getDefault(jmri.LightManager.class);
        lm.getNamedBeanSet().forEach((nb) -> {
            if (nb instanceof CbusLight) {
                CbusLight cl = (CbusLight) lm.provideLight(nb.toString());
                String text = Bundle.getMessage("cbLightOn",nb.getDisplayName());
                linkHwaddtoEvent( cl.getAddrOn(), text, nb.getDisplayName() );
                text = Bundle.getMessage("cbLightOff",nb.getDisplayName());
                linkHwaddtoEvent( cl.getAddrOff(), text, nb.getDisplayName() );
            }
        });
    }
    
    /**
     * Delete Button Clicked
     * See whether to display confirm popup
     * @param row int row number
     */
    public void buttonDeleteClicked(int row) {
        if (sessionConfirmDeleteRow) {
            // confirm deletion with the user
            JCheckBox checkbox = new JCheckBox(Bundle.getMessage("PopupSessionConfirmDel"));
            String message = Bundle.getMessage("DelConfirmOne") + "\n"   
            + Bundle.getMessage("DelConfirmTwo");
            Object[] params = {message, checkbox};
            
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                    null, params, Bundle.getMessage("DelEvPopTitle"), 
                    JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE)) {
                    boolean dontShow = checkbox.isSelected();
                    if (dontShow) {
                        sessionConfirmDeleteRow=false;
                    }
                    _model.removeRow(row);
            }
        } else {
            // no need to show warning, just delete
            _model.removeRow(row);
        }
    }
    
    /**
     * Self save as a .csv file, first prompting for a filename.
     */
    public void saveAsTable() {
        // start at current file, show dialog
        
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
        "Comma Seperated Value", "csv");
        fileChooser.setFileFilter(filter);
        
        fileChooser.setSelectedFile(new File("myevents.csv"));
        int retVal = fileChooser.showSaveDialog(null);
        
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            _saveFileName = fileChooser.getSelectedFile().getPath();

            if (!_saveFileName .endsWith(".csv")) {
                _saveFileName += ".csv";
            }

            _saveFile = new File(_saveFileName);
            if (_saveFile.isFile()) {
                int response = JOptionPane.showConfirmDialog(null, //
                        Bundle.getMessage("ConfirmOverwriteFile"), //
                        Bundle.getMessage("ConfirmQuestion"), JOptionPane.YES_NO_OPTION, //
                        JOptionPane.QUESTION_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            // log.debug("File chosen: {}", _saveFileName);
        } else {
            return; // cancelled or pane closed, prevent NPE
        }
        saveToCSV();
    }

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
                log.error("Problem creating output stream");
        }

        if (out == null) {
            log.error("Null File Output Stream");
        }
        if (p == null) { // certainly null if out == null
            log.error("Null Print Writer");
            return;
        }

        // Save table per row. We've checked for an empty table
        // print header labels
        
        
        for (int i = 0; i < CbusEventTableDataModel.saveColumns.length; i++) {
            // log.debug("save column array column {}", Savecolumns[i]);
            p.print(_model.getColumnName(CbusEventTableDataModel.saveColumns[i]));
            
            // last column, without comma
            if (i!=CbusEventTableDataModel.saveColumns.length-1) {
                p.print(",");
            }
        }
        
        p.println("");

        // print rows
        for (int i = 0; i < _model.getRowCount(); i++) {
            p.print(_model._mainArray.get(i).getEn());
            p.print(",");
            p.print(_model._mainArray.get(i).getNn());
            p.print(",");
            p.print('"' + _model._mainArray.get(i).getName() + '"');          
            p.print(",");
            p.print('"' + _model._mainArray.get(i).getNodeName() + '"');
            p.print(",");
            if ( _model._mainArray.get(i).getDate() != null ) {
               p.print(_model._mainArray.get(i).getDate()); // Date format
            }
            p.print(",");
            p.print('"'+ _model._mainArray.get(i).getComment() + '"');
            p.println("");
        }

        try {
            p.flush();
            p.close();
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            log.error("879 IO Exception");
        }
    }

    /**
     * Self print or print preview the table.
     * <p>
     * Copied from BeanTableDataModel modified to print variable column widths.
     * Final column with size zero runs to extent of page width.
     * <p>
     * Printed with headings and vertical lines between each column. Data is
     * word wrapped within a column. Can handle data as strings, integers,
     * comboboxes or booleans.
     */
    public void printTable(HardcopyWriter w) {
        // [AC] variable column sizes
        int columnTotal = 0;
        
        // log.debug("save column array column {}", saveColumns[i]);
        
        int[] columnWidth = new int[CbusEventTableDataModel.whichPrintColumns.length];
        // in a test, thats 86 chars on a line
        for (int i = 0; i < CbusEventTableDataModel.whichPrintColumns.length; i++) {
            
            int columnworkedon=CbusEventTableDataModel.whichPrintColumns[i];
            // log.debug(" 1016 print column i {} worked on is {} has width {} ",
            // i, columnworkedon, this.getColumnWidth(columnworkedon));
            
            if (CbusEventTableDataModel.getColumnWidth(columnworkedon) == 0) {
                // Fill to end of line
                columnWidth[i] = w.getCharactersPerLine() - columnTotal;
            } else {
                columnWidth[i] = CbusEventTableDataModel.getColumnWidth(columnworkedon);
                columnTotal = columnTotal + columnWidth[i] + 1;
            }
            // log.debug(" 1027 print column i {} columnTotal {} has width {} ",
            // i, columnTotal, columnWidth[i]);
        }

        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());
        
        // print the column header labels
        String[] columnStrings = new String[CbusEventTableDataModel.whichPrintColumns.length];
        // Put each column header in the array
        for (int i = 0; i < CbusEventTableDataModel.whichPrintColumns.length; i++) {
            int columnworkedon=CbusEventTableDataModel.whichPrintColumns[i];
            columnStrings[i] = _model.getColumnName(columnworkedon);
            // log.debug(" 1047 print column i {} has columnStrings {} getColumnName {} ", 
            // i, columnStrings[i], this.getColumnName(columnworkedon));
        }
        
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnWidth);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());

        // now print each row of data
        // create a base string the width of the column
        for (int i = 0; i < _model.getRowCount(); i++) {
            StringBuffer buf = new StringBuffer();
            // log.debug (" 1070 row i  {} ", i);
            
            for (int k = 0; k < CbusEventTableDataModel.whichPrintColumns.length; k++) {
                
                int j=CbusEventTableDataModel.whichPrintColumns[k];
                // log.debug("1076 i is {} j is {} ", i, j);
                
                //check for special, non string contents
                if (_model.getValueAt(i, j) == null) {
                    columnStrings[k] = buf.toString();
                } else if (_model.getValueAt(i, j) instanceof JComboBox) {
                    // columnStrings[j] = (String) ((JComboBox<String>) _model.getValueAt(i, j)).getSelectedItem();
                    columnStrings[k]=null;
                } else if (_model.getValueAt(i, j) instanceof Date) {
                    columnStrings[k] = (_model.getValueAt(i, j)).toString();
                    } else if (_model.getValueAt(i, j) instanceof Boolean) {
                    columnStrings[k] = (_model.getValueAt(i, j)).toString();
                } else if (_model.getValueAt(i, j) instanceof Integer) {
                    columnStrings[k] = (_model.getValueAt(i, j)).toString();
                } else {
                    columnStrings[k] = (String) _model.getValueAt(i, j);
                }
                // log.debug("588 columnStrings :{}:", columnStrings[j]);
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
           for (int i = 0; i < CbusEventTableDataModel.whichPrintColumns.length; i++) {
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
                log.warn("error during printing: " + e);
            }
        }
    }

    // if a CBUS Event xml file is found
    // import table data into it 
    protected void restoreEventsFromXmlTablestart() {
        
        CbusEventTableXmlFile x = new CbusEventTableXmlFile();
        File file = x.getFile(false);
        if (file == null) {
            log.debug("Nothing to load");  // NOI18N
            return;
        }
        log.debug("File Found");
        
        // Find root
        Element root;
        try {
            root = x.rootFromFile(file);
            root.getChildren("Event").forEach((xmlEvent) -> {
                // NOI18N
                
                if (xmlEvent.getAttribute("NodeNum") == null || xmlEvent.getAttribute("EventNum") == null ) { // NOI18N
                    log.error("Node or event number missing in event {}", xmlEvent.getAttributes() );
                } else try {
                
                    int nn = Integer.parseInt( xmlEvent.getAttribute("NodeNum").getValue() ); // NOI18N
                    int en = Integer.parseInt( xmlEvent.getAttribute("EventNum").getValue() ); // NOI18N
                    // log.info("event found nn {} en {}",nn,en);
                    CbusTableEvent tabEv = _model.provideEvent(nn,en);
                    
                    if (xmlEvent.getChild("Name") != null ) { // NOI18N
                        tabEv.setName( xmlEvent.getChild("Name").getValue() ); // NOI18N
                    }
                    if (xmlEvent.getChild("Comment") != null ) { // NOI18N
                        tabEv.setComment( xmlEvent.getChild("Comment").getValue() ); // NOI18N
                    }
                    if (xmlEvent.getChild("LastHeard") != null ) { // NOI18N
                        try {
                            Date newDate = xmlDateStyle.parse(xmlEvent.getChild("LastHeard").getValue()); // NOI18N
                            tabEv.setDate( newDate );
                        } catch (java.text.ParseException e) { 
                            log.error("Unable to parse date {}", xmlEvent.getAttributes()); // NOI18N
                        }
                    }
                    if (xmlEvent.getChild("On") != null ) { // NOI18N
                        tabEv.setTotalOn( Integer.parseInt( xmlEvent.getChild("On").getValue() ) ); // NOI18N
                    }
                    if (xmlEvent.getChild("Off") != null ) { // NOI18N
                        tabEv.setTotalOff( Integer.parseInt( xmlEvent.getChild("Off").getValue() ) ); // NOI18N
                    }
                    if (xmlEvent.getChild("In") != null ) { // NOI18N
                        tabEv.setTotalIn( Integer.parseInt( xmlEvent.getChild("In").getValue() ) ); // NOI18N
                    }
                    if (xmlEvent.getChild("Out") != null ) { // NOI18N
                        tabEv.setTotalOut( Integer.parseInt( xmlEvent.getChild("Out").getValue() ) ); // NOI18N
                    }
                } catch (java.lang.NumberFormatException ex) {
                    log.error("Incorrect off / on / in / out value in event {}", xmlEvent.getAttributes());
                }
            });
            _model.fireTableDataChanged();
            log.debug("Completed event xml file load");
            
        } catch (JDOMException ex) {
            log.error("File invalid: " + ex);  // NOI18N
        } catch (IOException ex) {
            log.error("Error reading file: " + ex);  // NOI18N
        }
        
        // backup done on load not on save to reduce shutdown time
        try {
            FileUtil.rotate(file, 10, "bup");  // NOI18N
        } catch (IOException ex) {
            log.warn("Rotate failed");  // NOI18N
        }
        
    }
    
    /**
     * Saves table event data to the EventTableData.xml file
     */
    protected void storeEventsToXml() {
        jmri.util.ThreadingUtil.runOnLayout(()->{  layoutEventsToXml(); });
    }
    
    private void layoutEventsToXml() {
        
        log.info("Saving event xml file");
        CbusEventTableXmlFile x = new CbusEventTableXmlFile();
        File file = x.getFile(true);
        
        Element root = new Element("CbusEventDetails");  // NOI18N
        root.setAttribute("noNamespaceSchemaLocation",  // NOI18N
            "https://raw.githubusercontent.com/MERG-DEV/JMRI/master/schema/MergCBUSEventTable.xsd",  // NOI18N
            org.jdom2.Namespace.getNamespace("xsi",
            "http://www.w3.org/2001/XMLSchema-instance"));  // NOI18N
        Document doc = new Document(root);
        
        _model.getEvents().forEach((event) -> {
            Element values;
            root.addContent(values = new Element("Event"));  // NOI18N
            values.setAttribute("NodeNum",  // NOI18N
                    "" + event.getNn() );
            values.setAttribute("EventNum",  // NOI18N
                    "" + event.getEn() );
            if ( !event.getName().isEmpty() ){
                values.addContent(new Element("Name").addContent("" + event.getName() ));  // NOI18N
            }
            if ( !event.getComment().isEmpty() ){
                values.addContent(new Element("Comment").addContent("" + event.getComment() ));  // NOI18N
            }
            if ( event.getDate() != null ){
                values.addContent(new Element("LastHeard").addContent(
                        "" + xmlDateStyle.format(  event.getDate() ) ));  // NOI18N
            }
            if ( event.getTotalOn() > 0 ){
                values.addContent(new Element("On").addContent("" + event.getTotalOn() ));  // NOI18N
            }
            if ( event.getTotalOff() > 0 ){
                values.addContent(new Element("Off").addContent("" + event.getTotalOff() ));  // NOI18N
            }
            if ( event.getTotalIn() > 0 ){
                values.addContent(new Element("In").addContent("" + event.getTotalIn() ));  // NOI18N
            }
            if (event.getTotalOut() > 0) {
                values.addContent(new Element("Out").addContent("" + event.getTotalOut() ));  // NOI18N
            }
        });
        
        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing: " + ex);  // NOI18N
        } catch (IOException ex) {
            log.error("IO Exception when writing: " + ex);  // NOI18N
        }

        log.debug("Saved events ok");  // NOI18N
    
    }
    
    /**
     * Class to provide access to the EventTableData.xml file
     */
    public static class CbusEventTableXmlFile extends XmlFile {

        public static String getDefaultFileName() {
            return getFileLocation() + getFileName();
        }

        public File getFile(boolean store) {
            // Verify that directory:cbus exists
            FileUtil.createDirectory(getFileLocation());

            File file = findFile(getDefaultFileName());
            if (file == null && store) {
                log.info("create new file");  // NOI18N
                file = new File(getDefaultFileName());
            }
            return file;
        }

        public static String getFileName() {
            return "EventTableData.xml";  // NOI18N
        }

        /**
         * Absolute path to location of TimeTable files.
         *
         * @return path to location
         */
        public static String getFileLocation() {
            return FileUtil.getUserFilesPath() + "cbus" + File.separator;  // NOI18N
        }
    }


    private final static Logger log = LoggerFactory.getLogger(CbusEventTableAction.class);
}
