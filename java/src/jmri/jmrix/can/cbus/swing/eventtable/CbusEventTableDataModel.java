package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.RowSorter;
import javax.swing.SortOrder;

import java.util.ArrayList;
import java.util.Date;
import jmri.util.FileUtil;
import java.util.List;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import jmri.util.davidflanagan.HardcopyWriter;

// import jmri.jmrix.can.CanInterface;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;

import jmri.jmrix.can.TrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus events
 *
 * @author Andrew Crosland (C) 2009
 * @author Steve Young (c) 2018
 * @see CbusEventTablePane
 * 
 */
public class CbusEventTableDataModel extends javax.swing.table.AbstractTableModel implements CanListener {
    
    private int _rowCount = 0;
    private int[] _canid = null;
    private int[] _eventid = null;
    private int[] _node = null;
    private String[] _name = null;
    private int[] _event = null;
    private int[] _type = null;
    private String[] _comment = null;
    private int[] _sessionoff = null;
    private int[] _sessionon = null;
    private Date[] _latesttimestamp = null;
    
    private File _saveFile = null;
    private String _saveFileName = null;
    private boolean _saved = false;
    private boolean SessionConfirmDeleteRow=true; // display confirm popup
    
    final JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    CanSystemConnectionMemo memo;
    TrafficController tc;
    
    // column order needs to match list in column tooltips
    static public final int EVENTID_COLUMN = 0; 
    static public final int CANID_COLUMN =1;
    static public final int EVENT_COLUMN = 2; 
    static public final int NODE_COLUMN = 3; 
    static public final int TYPE_COLUMN = 4; 
    static public final int ON_BUTTON_COLUMN = 5; 
    static public final int OFF_BUTTON_COLUMN = 6; 
    static public final int TOGGLE_BUTTON_COLUMN = 7;
    static public final int STATUS_REQUEST_BUTTON_COLUMN = 8;
    static public final int LATEST_TIMESTAMP_COLUMN = 9;
    static public final int NAME_COLUMN = 10; 
    static public final int COMMENT_COLUMN = 11;
    static public final int SESSION_TOTAL_COLUMN = 12;
    static public final int SESSION_ON_COLUMN = 13;
    static public final int SESSION_OFF_COLUMN = 14;
    static public final int DELETE_BUTTON_COLUMN = 15;

    static public final int MAX_COLUMN = 16;
    
    
    // order + which columns to use when saving
    protected int[] Savecolumns = {0,1,2,3,4,9,10,11}; // will need to change CVS file order if changed
    
    protected int[] WhichPrintColumns = {2,3,1,10,11}; // no changes needed to other files if changed
    
    
    // todo array list order of columns to print
    

    CbusEventTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        _eventid = new int[CbusConstants.MAX_TABLE_EVENTS];
        _canid = new int[CbusConstants.MAX_TABLE_EVENTS];
        _node = new int[CbusConstants.MAX_TABLE_EVENTS];
        _name = new String[CbusConstants.MAX_TABLE_EVENTS];
        _event = new int[CbusConstants.MAX_TABLE_EVENTS];
        _type = new int[CbusConstants.MAX_TABLE_EVENTS];
        _comment = new String[CbusConstants.MAX_TABLE_EVENTS];
        _sessionoff = new int[CbusConstants.MAX_TABLE_EVENTS];
        _sessionon = new int[CbusConstants.MAX_TABLE_EVENTS];
        _latesttimestamp = new Date[CbusConstants.MAX_TABLE_EVENTS];
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
        return MAX_COLUMN;
    }

    
    
    /**
     * Returns String of column name from column int
     * used in table header
     * @param col
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case EVENTID_COLUMN:
                return Bundle.getMessage("ColumnEventID");
            case CANID_COLUMN:
                return Bundle.getMessage("ColumnID");
            case NODE_COLUMN:
                return Bundle.getMessage("ColumnNode");
            case NAME_COLUMN:
                return Bundle.getMessage("ColumnName");
            case EVENT_COLUMN:
                return Bundle.getMessage("ColumnEvent");
            case TYPE_COLUMN:
                return Bundle.getMessage("CbusEventOnOrOff");
            case COMMENT_COLUMN:
                return Bundle.getMessage("ColumnComment");
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnEventDelete");
            case ON_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOn");
            case OFF_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOff");
            case TOGGLE_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnToggle"); 
            case STATUS_REQUEST_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnStatusRequest"); 
            case SESSION_ON_COLUMN:
                return Bundle.getMessage("ColumnOnSession"); 
            case SESSION_OFF_COLUMN:
                return Bundle.getMessage("ColumnOffSession");
            case SESSION_TOTAL_COLUMN:
                return Bundle.getMessage("ColumnTotalSession");
            case LATEST_TIMESTAMP_COLUMN:
                return Bundle.getMessage("ColumnLastHeard");
            default:
                return "unknown"; // NOI18N
        }
    }

    // order needs to match column list right at top of class
    protected String[] columnToolTips = {
        Bundle.getMessage("ColumnEventIDTip"),
        Bundle.getMessage("IDColTip"),
        Bundle.getMessage("EventColTip"),
        Bundle.getMessage("NodeColTip"), 
        Bundle.getMessage("TypeColTip"),
        Bundle.getMessage("SendOntip"),
        Bundle.getMessage("SendOfftip"),
        Bundle.getMessage("SendToggleTip"),
        Bundle.getMessage("ColumnRequestStatusTip"),
        Bundle.getMessage("ColumnLastHeard"),
        Bundle.getMessage("NameColTip"),
        Bundle.getMessage("CommentColTip"),
        Bundle.getMessage("ColumnTotalSession"),
        Bundle.getMessage("ColumnOnSession"),
        Bundle.getMessage("ColumnOffSession"),
        Bundle.getMessage("ColumnEventDeleteTip")
    }; // Length = number of items in array should (at least) match number of columns

    
    /**
    * Returns int of startup column widths
    * @param col
    */
    public int getPreferredWidth(int col) {
        switch (col) {
            case EVENTID_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            case NODE_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case EVENT_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case TYPE_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case NAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case CANID_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case COMMENT_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case DELETE_BUTTON_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case ON_BUTTON_COLUMN:
                return new JTextField(8).getPreferredSize().width;
            case OFF_BUTTON_COLUMN:
                return new JTextField(8).getPreferredSize().width;                
            case TOGGLE_BUTTON_COLUMN:
                return new JTextField(8).getPreferredSize().width; 
            case STATUS_REQUEST_BUTTON_COLUMN:
                return new JTextField(8).getPreferredSize().width;
            case SESSION_ON_COLUMN:
                return new JTextField(7).getPreferredSize().width;  
            case SESSION_OFF_COLUMN:
                return new JTextField(7).getPreferredSize().width;                  
            case SESSION_TOTAL_COLUMN:
                return new JTextField(7).getPreferredSize().width;  
            case LATEST_TIMESTAMP_COLUMN:
                return new JTextField(7).getPreferredSize().width;  
                
                default:
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }
    
    
    /**
     * Returns int of column width.
     * <p>
     * Just used for printing.
     * but also needed for buttons
     * in a test, there is 86 chars on a line
     * -1 is invalid
     * 0 is final column extend to end
     * </p>
     * @param col
     */
    public int getColumnWidth(int col) {
        switch (col) {
            case EVENTID_COLUMN:
                return 3;
            case CANID_COLUMN:
                return 5;
            case NODE_COLUMN:
                return 7;
            case NAME_COLUMN:
                return 14;
            case EVENT_COLUMN:
                return 7;
            case TYPE_COLUMN: // on off
                return 8;
            case COMMENT_COLUMN:
                return 0; // 0 to get writer recognize it as the last column, will fill with spaces
            case DELETE_BUTTON_COLUMN:
                return 6;
            case ON_BUTTON_COLUMN:
                return 4;
            case OFF_BUTTON_COLUMN:
                return 4;
            case TOGGLE_BUTTON_COLUMN:
                return 4;
            case STATUS_REQUEST_BUTTON_COLUMN:
                return 4;
            case SESSION_ON_COLUMN:
                return 4;
            case SESSION_OFF_COLUMN:
                return 4;
            case SESSION_TOTAL_COLUMN:
                return 4;
            case LATEST_TIMESTAMP_COLUMN:
                return 4;
            default:
                return -1;
        }
    }
    
    /**
    * Returns column class type.
    */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case EVENTID_COLUMN:
                return Integer.class;
            case CANID_COLUMN:
                return Integer.class;
            case NODE_COLUMN:
                return Integer.class;
            case EVENT_COLUMN:
                return Integer.class;
            case TYPE_COLUMN:
                return Integer.class;
            case NAME_COLUMN:
                return String.class;
            case COMMENT_COLUMN:
                return String.class;
            case DELETE_BUTTON_COLUMN:
                return JButton.class;
            case ON_BUTTON_COLUMN:
                return JButton.class;
            case OFF_BUTTON_COLUMN:
                return JButton.class;
            case STATUS_REQUEST_BUTTON_COLUMN:
                return JButton.class;
            case TOGGLE_BUTTON_COLUMN:
                return JButton.class;
            case SESSION_ON_COLUMN:
                return Integer.class;
            case SESSION_OFF_COLUMN:
                return Integer.class;                
            case SESSION_TOTAL_COLUMN:
                return Integer.class;
            case LATEST_TIMESTAMP_COLUMN:
                return Date.class;
            default:
                return null;
        }
    }
    
    static final Boolean True = Boolean.valueOf("true");
    static final Boolean False = Boolean.valueOf("false");

    
    /**
    * Boolean return to edit table cell or not
    * @return boolean
    */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NAME_COLUMN:
                return true;
            case COMMENT_COLUMN:
                return true;
            case DELETE_BUTTON_COLUMN:
                return true;
            case ON_BUTTON_COLUMN:
                return true;
            case OFF_BUTTON_COLUMN:
                return true;
            case TOGGLE_BUTTON_COLUMN:
                return true;
            case STATUS_REQUEST_BUTTON_COLUMN:
                return true;
            default:
                return false;
        }
    }



     /**
     * Return table values
     * @param row
     * @param col
     */
    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case EVENTID_COLUMN:
                return _eventid[row];
            case NODE_COLUMN:
                if (_node[row]>0) {
                    return _node[row];
                } else {
                    return null;
                }
            case EVENT_COLUMN:
                return _event[row];
            case NAME_COLUMN:
                return _name[row];
            case CANID_COLUMN:
                return _canid[row];
            case TYPE_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if (_type[row]==1) { 
                    return Bundle.getMessage("CbusEventOff");
                } else if (_type[row]==0) {
                    return Bundle.getMessage("CbusEventOn");
                } else {
                    return(" ? ");
                }
            case ON_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOn");
            case OFF_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOff");
            case TOGGLE_BUTTON_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if (_type[row]==1) { 
                    return Bundle.getMessage("CbusSendOn");
                } else if (_type[row]==0) {
                    return Bundle.getMessage("CbusSendOff");
                } 
            case STATUS_REQUEST_BUTTON_COLUMN:
                return("Get Status");
            case COMMENT_COLUMN:
                return _comment[row];
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            case SESSION_ON_COLUMN:
                return _sessionon[row];
            case SESSION_OFF_COLUMN:
                return _sessionoff[row];
            case SESSION_TOTAL_COLUMN:
                return (_sessionon[row]+_sessionoff[row]);
            case LATEST_TIMESTAMP_COLUMN:
                return (_latesttimestamp[row]); // in Date format
            default:
                log.error("internal state inconsistent with table request for row {} col {}", row, col);
                return null;
        }
    }
    
    

    /**
     * Capture new comments or node names.
     * Button events
     * @param value
     * @param row
     * @param col
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        // log.debug("427 set valueat called row: {} col: {}", row, col);
        if (col == NAME_COLUMN) {
            _name[row] = (String) value;
            // look for other occurrences of the same node
            for (int i = 0; i < _rowCount; i++) {
                // ignore this one
                if (i != row) {
                    if (_node[i] == _node[row]) {
                        // copy the name if not present
                        if (_name[i]==null) {
                            _name[i] = _name[row];
                        }
                    }
                }
            }
            Runnable r = new Notify(-1, this);
            javax.swing.SwingUtilities.invokeLater(r);            
            
        }  else if (col == COMMENT_COLUMN) {        
            _comment[row] = (String) value;
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        
        else if (col == TYPE_COLUMN) {
            _type[row] = (int) value;
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        
        else if (col == DELETE_BUTTON_COLUMN) {
            ButtonDeleteClicked(row);
            Runnable r = new Notify(row, this);   // -1 in first arg means all
            javax.swing.SwingUtilities.invokeLater(r);        }
        else if (col == ON_BUTTON_COLUMN) {
            sendEventFromRow(row,1);
        }
        else if (col == OFF_BUTTON_COLUMN) {
            sendEventFromRow(row,0);
        }        
        else if (col == TOGGLE_BUTTON_COLUMN) {
            if ( _type[row]==1) {
            sendEventFromRow(row,1); }
            else {
                sendEventFromRow(row,0);
            }
        }
        else if (col == STATUS_REQUEST_BUTTON_COLUMN) {
            sendEventFromRow(row,2);
        }
        else if (col == SESSION_ON_COLUMN) {
            _sessionon[row] = _sessionon[row] + 1;
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
            }
        else if (col == SESSION_OFF_COLUMN) {
            _sessionoff[row] = _sessionoff[row] + 1;
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }        else if (col == LATEST_TIMESTAMP_COLUMN) {
            _latesttimestamp[row] = new Date();
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        // table is dirty
        _saved = false;
    }

    
    
    /**
     * send events
     * @param row
     * @param OfforOn 1 is on, 0 is off, 2 is either for request update
     * @since 4.13.3
     */
    public void sendEventFromRow(int row, int OfforOn){
        // log.debug( "313 send from row {} ", row);
        int nn, ev;
        nn = parseBinDecHexByte(String.valueOf(_node[row]), 65535, true, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SendEventNodeError"));
        CanMessage m = new CanMessage(tc.getCanid());
        if (nn == -1) {
            return;
        }
        ev = parseBinDecHexByte(String.valueOf(_event[row]), 65535, true, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SendEventInvalidError"));
        if (ev == -1) {
            return;
        }
        
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
    
        if (OfforOn==1) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACON);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASON);
            }
        } else if (OfforOn==0) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACOF);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASOF);
            }
        } else if (OfforOn==2) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_AREQ);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASRQ);
            }
            // set listener to display response or not from response request
            // 10 seconds ?
        }
    
        m.setElement(1, nn >> 8);
        m.setElement(2, nn & 0xff);
        m.setElement(3, ev >> 8);
        m.setElement(4, ev & 0xff);
        m.setNumDataElements(5);
        message(m);
        tc.sendCanMessage(m, this);
    }

    
    
    
    
    /**
     * Delete Button Clicked
     * See whether to display confirm popup
     * @see removeRow
     * @param row
     */
    public void ButtonDeleteClicked(int row) {
        // log.debug("297 DELETE BUTTON CLICKED row {} confirm delete {} ", row, SessionConfirmDeleteRow);
        if (SessionConfirmDeleteRow) {
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
                        SessionConfirmDeleteRow=false;
                    }
                    // log.warn("delete from dialogue ");
                    removeRow(row);
    
            } else {
                // log.warn("cancelled, not deleted ");
                }
        } else {
            // no need to show warning, just delete
            // log.warn("322 just delete ");
            removeRow(row);
        }
    }
    
    /**
     * Remove Row from table
     * @see ButtonDeleteClicked
     * @param row
     */    
   public void removeRow(int row) { // would be better off with events all in 1 array!!
        // log.warn("322 delete row {} with max rows rowcount as {} ", row, _rowCount);
        int imaxrows = _rowCount;
        for(int i = row; i < imaxrows; i++) {
            // log.warn("331 bump up row i {} eventid {} is now to be eventid {} ", i, _eventid[i], _eventid[i+1]);
            _event[i] = _event[i+1];
            _node[i] = _node[i+1];
            _name[i] = _name[i+1];
            _comment[i] = _comment[i+1];
            _canid[i] = _canid[i+1];
            _type[i] = _type[i+1];    
        }

        _rowCount--;
        if ( _rowCount<0 ) {
            _rowCount=0;
        }        

        Runnable r = new Notify(row, this);
        javax.swing.SwingUtilities.invokeLater(r);
    }
    
    


    /**
     * Configure a table to have our standard rows and columns.
     * <p>
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * </p>
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
     * @return boolean
     */
    public boolean isTableDirty() {
        return(_saved == false);
    }

    /**
     * Capture node and event, check if isevent and send to parse from message.
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) {
        // log.debug("296 Received new message: {} getevent: {} ", m, CbusMessage.getEvent(m) );
        if (( CbusMessage.getEvent(m))>0) {
            parseMessage(
            CbusMessage.getId(m), 
            (m.getElement(1) * 256 + m.getElement(2)),
            (m.getElement(3) * 256 + m.getElement(4)),
            CbusMessage.getEventType(m),
            CbusMessage.isShort(m)
            );
            
        }
    }

    /**
     * Capture node and event, check isevent and send to parse from reply.
     * @param m canmessage
     */
    @Override
    public void reply(CanReply m) {
        // log.debug("311 Received new reply : {} getevent: {} ", m, CbusMessage.getEvent(m) );
        if (( CbusMessage.getEvent(m))>0) {
            parseMessage(
            CbusMessage.getId(m), 
            (m.getElement(1) * 256 + m.getElement(2)),
            (m.getElement(3) * 256 + m.getElement(4)),
            CbusMessage.getEventType(m),
            CbusMessage.isShort(m));
        }
    }

    
    /**
     * If new event add to table, else update table.
     * takes canid, node, event, onoroff
     * @since 4.13.3
     * @param canid
     * @param node
     * @param event
     * @param eventOnOrOff
     * @param isShort
     */
    public synchronized void parseMessage( int canid, int node, int event, int eventOnOrOff, boolean isShort) {
        // log.debug("304 parseMessage  ");
        // log.debug(" 310 event: {}  node: {}  canid: {} ", event, node, canid);
        // log.debug(" 326 event onoroff 1 is off, 0 is on : {} ", eventOnOrOff);
        
        // todo - change this setting in options?
        if (isShort) {
            node=0;
        }
        
        int existingRow = seeIfEventOnTable( event, node, canid);
        // log.debug(" 339 existing event: {} ", existingRow);
        if (existingRow<0) {
            _canid[_rowCount] = canid;
            _event[_rowCount] = event;
            _node[_rowCount] = node;
            _type[_rowCount] = eventOnOrOff;
            
            if (eventOnOrOff==0) {
                _sessionon[_rowCount]=1;
            }
            if (eventOnOrOff==1) {       
                _sessionoff[_rowCount]=1;
            }
            
            _latesttimestamp[_rowCount] = new Date();
            addEvent(); 
        } else {
            updateEventfromNetwork(existingRow, eventOnOrOff);
        }
    }
    
    
    /**
     * Do Node, Event + CANID check, returns -1 if not on table, otherwise the row id
     * @since 4.13.3
     * @param event int
     * @param node int
     * @param canid int
     * @return int of row, otherwise -1
     */
    public synchronized int seeIfEventOnTable( int event, int node, int canid) {
        for (int i = 0; i < getRowCount(); i++) {
            int testnode = (_node[i]);
            int testevent = (_event[i]);
            int testcanid = (_canid[i]);
            // log.debug(" 343 testevent: {}  tetsnode: {} row {} ", testevent, testnode, i);
            if ((event==testevent) && (node == testnode) && (canid == testcanid)) {
                // log.debug(" 398 match found {} ", i);
                return i;
            }
        }
        return -1;
    }
    
    

    
    
    /**
     * Updates the table if event already on table
     * @param existingRow
     * @param eventOnOrOff
     * @since 4.13.3
     */    
    public synchronized void updateEventfromNetwork( int existingRow, int eventOnOrOff ) {
        // log.debug(" 393 updating row {} with status {} ", existingRow, eventOnOrOff);        
        setValueAt(eventOnOrOff, existingRow, TYPE_COLUMN);
        setValueAt(1, existingRow, LATEST_TIMESTAMP_COLUMN);
        if (eventOnOrOff==0) {  setValueAt(1, existingRow, SESSION_ON_COLUMN); }
        if (eventOnOrOff==1) { setValueAt(1, existingRow, SESSION_OFF_COLUMN); }
    }
    
    
    
    /**
     * Register new event to table
     */
    public synchronized void addEvent() {
        // log.debug(" adding event ");
        if (_rowCount < CbusConstants.MAX_TABLE_EVENTS) {
            _eventid[_rowCount]=_rowCount+1;         
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
              _model.fireTableDataChanged();
        }
    }

    
    
    
    
    
    /**
     * Does Nothing
     */
    public void dispose() {
        // eventTable.removeAllElements();
        // eventTable = null;
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
        
        
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
        "Comma Seperated Value", "csv");
        fileChooser.setFileFilter(filter);
        
        fileChooser.setSelectedFile(new File("myevents.csv"));
        int retVal = fileChooser.showSaveDialog(null);
        
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            _saveFileName = fileChooser.getSelectedFile().getPath().toString();
            if (_saveFileName != null) {
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
                log.error("saveAsTable: No file name available. Aborted");
                return;
            }
        } else {
            return; // cancelled or pane closed, prevent NPE
        }
        saveToCSV();
    }


    protected void saveToCSV() {
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

        // Save table per row. We've checked for an empty table in SaveTable()
        // print header labels
        
        
        for (int i = 0; i < Savecolumns.length; i++) {
            // log.debug("save column array column {}", Savecolumns[i]);
            p.print(this.getColumnName(Savecolumns[i]));
            
            // last column, without comma
            if (i!=Savecolumns.length-1) {
                p.print(",");
            }
        }
        
        p.println("");

        // print rows
        for (int i = 0; i < this.getRowCount(); i++) {
            p.print(_eventid[i]);
            p.print(",");
            p.print(_canid[i]);
            p.print(",");
            p.print(_event[i]);
            p.print(",");
            p.print(_node[i]);
            p.print(",");
            p.print(_type[i]);
            p.print(",");
            p.print(_latesttimestamp[i]); // Date format
            
            
            
            
            p.print(",");
            
            if (_name[i] == null) {
                p.print("");
            } else {
                p.print('"' + _name[i] + '"');
            }
            p.print(",");
            if (_comment[i] == null) {
                p.print("");
            } else {
                p.print('"'+ _comment[i] + '"');
            }
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
        // mark that the table has been saved
        _saved = true; // TODO disable the Save menu item in CbusEventTablePane
    }





    
    
    // move this to global cbus for sending events from other swing

    /**
     * Parse a string for binary, decimal or hex byte value
     * <P>
     * 0b, 0d or 0x prefix will force parsing of binary, decimal or hex,
     * respectively. Entries with no prefix are parsed as decimal if decimal
     * flag is true, otherwise hex.
     *
     * copied from original version at CbusConsolePane.java
     *
     * @param s        string to be parsed
     * @param limit    upper bound of value to be parsed
     * @param decimal  flag for decimal or hex default
     * @param errTitle Title of error dialogue box if Number FormatException
     *                 encountered
     * @param errMsg   Message to be displayed if Number FormatException
     *                 encountered
     * @return the byte value, -1 indicates failure
     */
    public int parseBinDecHexByte(String s, int limit, boolean decimal, String errTitle, String errMsg) {
        int data = -1;
        boolean error = false;
        int radix = 16;

        if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0x")) {
            // hex, remove the prefix
            s = s.substring(2);
            radix = 16;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0d")) {
            // decimal, remove the prefix
            s = s.substring(2);
            radix = 10;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0b")) {
            // binary, remove the prefix
            s = s.substring(2);
            radix = 2;
        } else if (decimal) {
            radix = 10;
        }

        try {
            data = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            error = true;
        }
        if ((data < 0) || (data > limit)) {
            error = true;
        }
        if (error) {
            JOptionPane.showMessageDialog(null, errMsg,
                    errTitle, JOptionPane.ERROR_MESSAGE);
            data = -1;
        }
        return data;
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
     * </p>
     */
    public void printTable(HardcopyWriter w) {
        // [AC] variable column sizes
        int columnTotal = 0;
        
        
        // WhichPrintColumns
            //    for (int i = 0; i < WhichPrintColumns.length; i++) {
            // log.debug("save column array column {}", Savecolumns[i]);
        
        int[] columnWidth = new int[this.WhichPrintColumns.length];
        // in a test, thats 86 chars on a line
        for (int i = 0; i < this.WhichPrintColumns.length; i++) {
            
            int columnworkedon=WhichPrintColumns[i];
            
            // log.debug(" 1016 print column i {} worked on is {} has width {} ",i, columnworkedon, this.getColumnWidth(columnworkedon));
            
            if (this.getColumnWidth(columnworkedon) == 0) {
                // Fill to end of line
                columnWidth[i] = w.getCharactersPerLine() - columnTotal;
            } else {
                columnWidth[i] = this.getColumnWidth(columnworkedon);
                columnTotal = columnTotal + columnWidth[i] + 1;
            }
            
            // log.debug(" 1027 print column i {} columnTotal {} has width {} ",i, columnTotal, columnWidth[i]);
        }

        // log.debug("got to 1032 ");
        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());
        
        // print the column header labels
        String[] columnStrings = new String[this.WhichPrintColumns.length];
        // Put each column header in the array
        for (int i = 0; i < this.WhichPrintColumns.length; i++) {
            int columnworkedon=WhichPrintColumns[i];
            columnStrings[i] = this.getColumnName(columnworkedon);
            
            // log.debug(" 1047 print column i {} has columnStrings {} getColumnName {} ", i, columnStrings[i], this.getColumnName(columnworkedon));
            
        }
        
        
        
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnWidth);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());

        // now print each row of data
        // create a base string the width of the column
        for (int i = 0; i < this.getRowCount(); i++) {
            
            StringBuffer buf = new StringBuffer();
            
            
            // log.debug (" 1070 row i  {} ", i);
            
            // int j = 0;
            // while ( j < columnWidth[j]) {
            //     log.warn (" 1073 j is {} columnWidth[j] is {} ", columnWidth[j]);
            //     buf.append(" ");
            //      j++;
            // }
            
            for (int k = 0; k < WhichPrintColumns.length; k++) {
                
                int j=WhichPrintColumns[k];
                
                // log.debug("1076 i is {} j is {} ", i, j);
                
                //check for special, non string contents
                if (this.getValueAt(i, j) == null) {
                    columnStrings[k] = buf.toString();
                } else if (this.getValueAt(i, j) instanceof JComboBox) {
                    // columnStrings[j] = (String) ((JComboBox<String>) this.getValueAt(i, j)).getSelectedItem();
                    columnStrings[k]=null;
                } else if (this.getValueAt(i, j) instanceof Date) {
                    columnStrings[k] = (this.getValueAt(i, j)).toString();
                    } else if (this.getValueAt(i, j) instanceof Boolean) {
                    columnStrings[k] = (this.getValueAt(i, j)).toString();
                } else if (this.getValueAt(i, j) instanceof Integer) {
                    columnStrings[k] = (this.getValueAt(i, j)).toString();
                } else {
                    columnStrings[k] = (String) this.getValueAt(i, j);
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
           for (int i = 0; i < this.WhichPrintColumns.length; i++) {
            
                    // int i=WhichPrintColumns[k];
                    w.write(w.getCurrentLineNumber(), column, w.getCurrentLineNumber() + 1, column);
                    
                    column = column + columnWidth[i] + 1;
                    
                    // log.debug("1167 i is {} column is {} columnWidth[i] is {} ", i, column, columnWidth[i]);
                    
                    
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

    private final static Logger log = LoggerFactory.getLogger(CbusEventTableDataModel.class);
}
