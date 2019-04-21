package jmri.jmrix.can.cbus.eventtable;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.Date;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusNameService;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.TrafficController;
import jmri.util.swing.TextAreaFIFO;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus events
 *
 * @author Andrew Crosland (C) 2009
 * @author Steve Young (c) 2018 2019
 * 
 */
public class CbusEventTableDataModel extends javax.swing.table.AbstractTableModel implements CanListener {

    protected ArrayList<CbusTableEvent> _mainArray;
    private String _context=null; // event table pane console text
    static private int MAX_LINES = 500; // tablefeedback screen log size
    public TextAreaFIFO tablefeedback;
  //  private CanSystemConnectionMemo _memo;
    private TrafficController tc;
    public CbusEventTableAction ta;
    
    // column order needs to match list in column tooltips
    static public final int NODE_COLUMN = 0; 
    static public final int EVENT_COLUMN = 1; 
    static public final int NAME_COLUMN = 2; 
    static public final int NODENAME_COLUMN = 3;
    static public final int COMMENT_COLUMN = 4;
    static public final int STATE_COLUMN = 5;
    static public final int TOGGLE_BUTTON_COLUMN = 6;
    static public final int ON_BUTTON_COLUMN = 7; 
    static public final int OFF_BUTTON_COLUMN = 8; 
    static public final int CANID_COLUMN = 9;
    static public final int LATEST_TIMESTAMP_COLUMN = 10;
    static public final int STATUS_REQUEST_BUTTON_COLUMN = 11;
    static public final int SESSION_TOTAL_COLUMN = 12;
    static public final int SESSION_ON_COLUMN = 13;
    static public final int SESSION_OFF_COLUMN = 14;
    static public final int SESSION_IN_COLUMN = 15;
    static public final int SESSION_OUT_COLUMN = 16;
    static public final int DELETE_BUTTON_COLUMN = 17;
    static public final int STLR_ON_COLUMN = 18;
    static public final int STLR_OFF_COLUMN = 19;
    static public final int STLR_ICON_COLUMN = 99;
    
    static public final int MAX_COLUMN = 20;
    
    // order + which columns to use when saving
    protected static int[] saveColumns = {0,1,2,3,10,4}; // will need to change CVS file order if changed
    protected static int[] whichPrintColumns = {0,1,2,3,4}; // no changes needed to other files if changed    

    public CbusEventTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        log.info("Starting MERG CBUS Event Table");
        _mainArray = new ArrayList<CbusTableEvent>();
        tablefeedback = new TextAreaFIFO(MAX_LINES);

        jmri.InstanceManager.store(this,jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel.class);
        
        // connect to the CanInterface
        tc = memo.getTrafficController();
        if (tc != null ) {
            tc.addCanListener(this);
        }
        ta = new CbusEventTableAction(this);
    }

    // order needs to match column list top of dtabledatamodel
    public static final String[] columnToolTips = {
        Bundle.getMessage("NodeColTip"),
        Bundle.getMessage("EventColTip"),
        Bundle.getMessage("NameColTip"),
        Bundle.getMessage("CbusNodeNameTip"),
        Bundle.getMessage("CommentColTip"),
        Bundle.getMessage("TypeColTip"),
        Bundle.getMessage("SendToggleTip"),
        Bundle.getMessage("SendOntip"),
        Bundle.getMessage("SendOfftip"),
        Bundle.getMessage("IDColTip"),
        Bundle.getMessage("ColumnLastHeard"),
        Bundle.getMessage("ColumnRequestStatusTip"),
        Bundle.getMessage("ColumnTotalSession"),
        Bundle.getMessage("ColumnOnSession"),
        Bundle.getMessage("ColumnOffSession"),
        Bundle.getMessage("ColumnInSessionTip"),
        Bundle.getMessage("ColumnOutSessionTip"),
        Bundle.getMessage("ColumnEventDeleteTip"),
        Bundle.getMessage("StlrOnTip"),
        Bundle.getMessage("StlrOffTip")

    }; // Length = number of items in array should (at least) match number of columns
    
    /**
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {
        return _mainArray.size();
    }

    @Override
    public int getColumnCount() {
        return MAX_COLUMN;
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
        tablefeedback.setEditable ( false ); // set textArea non-editable
    }


    /**
     * Returns String of column name from column int
     * used in table header
     * @param col int col number
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case CANID_COLUMN:
                return Bundle.getMessage("CanID");
            case NODE_COLUMN:
                return Bundle.getMessage("CbusNode");
            case NODENAME_COLUMN:
                return Bundle.getMessage("CbusNodeName");
            case NAME_COLUMN:
                return Bundle.getMessage("ColumnName");
            case EVENT_COLUMN:
                return Bundle.getMessage("CbusEvent");
            case STATE_COLUMN:
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
            case SESSION_IN_COLUMN:
                return Bundle.getMessage("ColumnInSession"); 
            case SESSION_OUT_COLUMN:
                return Bundle.getMessage("ColumnOutSession");
            case SESSION_TOTAL_COLUMN:
                return Bundle.getMessage("ColumnTotalSession");
            case LATEST_TIMESTAMP_COLUMN:
                return Bundle.getMessage("ColumnLastHeard");
            case STLR_ON_COLUMN:
                return Bundle.getMessage("JmriOnEv");
            case STLR_OFF_COLUMN:
                return Bundle.getMessage("JmriOffEv");
            default:
                return "unknown"; // NOI18N
        }
    }

    /**
    * Returns int of startup column widths
    * @param col int col number
    */
    public static int getPreferredWidth(int col) {
        switch (col) {
            case EVENT_COLUMN:
            case CANID_COLUMN:
            case NODE_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case STATE_COLUMN:
            case SESSION_ON_COLUMN:
            case SESSION_OFF_COLUMN:
            case SESSION_IN_COLUMN:
            case SESSION_OUT_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case SESSION_TOTAL_COLUMN:
            case LATEST_TIMESTAMP_COLUMN:
            case DELETE_BUTTON_COLUMN:
                return new JTextField(7).getPreferredSize().width;
            case ON_BUTTON_COLUMN:
            case OFF_BUTTON_COLUMN:               
                return new JTextField(8).getPreferredSize().width; 
            case STATUS_REQUEST_BUTTON_COLUMN:
                return new JTextField(9).getPreferredSize().width;
            case COMMENT_COLUMN:
            case NAME_COLUMN:
            case NODENAME_COLUMN:
            case TOGGLE_BUTTON_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case STLR_ON_COLUMN:
            case STLR_OFF_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            default:
                return new JTextField(" <unknown> ").getPreferredSize().width; // NOI18N
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
     * @param col int col number
     */
    public static int getColumnWidth(int col) {
        switch (col) {
            case NODENAME_COLUMN:
                return 9;
            case NAME_COLUMN:
                return 14;
            case EVENT_COLUMN:
            case NODE_COLUMN:
            case STATE_COLUMN: // on off
                return 8;
            case COMMENT_COLUMN:
                return 0; // 0 to get writer recognize it as the last column, will fill with spaces
            case DELETE_BUTTON_COLUMN:
                return 2;
            default:
                return 4;
        }
    }
    
    /**
    * Returns column class type.
    */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case SESSION_ON_COLUMN:
            case SESSION_OFF_COLUMN:               
            case SESSION_IN_COLUMN:
            case SESSION_OUT_COLUMN:
            case SESSION_TOTAL_COLUMN:
            case CANID_COLUMN:
            case NODE_COLUMN:
            case EVENT_COLUMN:
                return Integer.class;
            case NAME_COLUMN:
            case NODENAME_COLUMN:
            case COMMENT_COLUMN:
            case STLR_ON_COLUMN:
            case STLR_OFF_COLUMN:
                return String.class;
            case DELETE_BUTTON_COLUMN:
            case ON_BUTTON_COLUMN:
            case OFF_BUTTON_COLUMN:
            case STATUS_REQUEST_BUTTON_COLUMN:
            case TOGGLE_BUTTON_COLUMN:
                return JButton.class;
            case LATEST_TIMESTAMP_COLUMN:
                return Date.class;
            case STATE_COLUMN:
                return Enum.class;
            default:
                return null;
        }
    }
    
    /**
    * Boolean return to edit table cell or not
    * @return boolean
    */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NAME_COLUMN:
            case COMMENT_COLUMN:
            case DELETE_BUTTON_COLUMN:
            case ON_BUTTON_COLUMN:
            case OFF_BUTTON_COLUMN:
            case TOGGLE_BUTTON_COLUMN:
            case STATUS_REQUEST_BUTTON_COLUMN:
                return true;
            default:
                return false;
        }
    }

     /**
     * Return table values
     * @param row int row number
     * @param col int col number
     */
    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case NODE_COLUMN:
                return _mainArray.get(row).getNn();
            case EVENT_COLUMN:
                return _mainArray.get(row).getEn();
            case NAME_COLUMN:
                return _mainArray.get(row).getName();
            case NODENAME_COLUMN:
                return new CbusNameService().getNodeName( _mainArray.get(row).getNn() );
            case CANID_COLUMN:
                return _mainArray.get(row).getEventCanId();
            case STATE_COLUMN:
                return _mainArray.get(row).getState();
            case ON_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOn");
            case OFF_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOff");
            case TOGGLE_BUTTON_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if ( _mainArray.get(row).getState()==CbusTableEvent.EvState.OFF ) { 
                    return Bundle.getMessage("CbusSendOn");
                } else if (_mainArray.get(row).getState()==CbusTableEvent.EvState.ON ) {
                    return Bundle.getMessage("CbusSendOff");
                } else
                    return Bundle.getMessage("CbusSendOff");
            case STATUS_REQUEST_BUTTON_COLUMN:
                return Bundle.getMessage("StatusButton");
            case COMMENT_COLUMN:
                return _mainArray.get(row).getComment();
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            case SESSION_ON_COLUMN:
                return _mainArray.get(row).getSessionOn();
            case SESSION_OFF_COLUMN:
                return _mainArray.get(row).getSessionOff();
            case SESSION_IN_COLUMN:
                return _mainArray.get(row).getSessionIn();
            case SESSION_OUT_COLUMN:
                return _mainArray.get(row).getSessionOut();
            case SESSION_TOTAL_COLUMN:
                return (_mainArray.get(row).getSessionOn() + _mainArray.get(row).getSessionOff() );
            case LATEST_TIMESTAMP_COLUMN:
                return _mainArray.get(row).getDate();
            case STLR_ON_COLUMN:
                return _mainArray.get(row).getStlOn();
            case STLR_OFF_COLUMN:
                return _mainArray.get(row).getStlOff();
            default:
                return null;
        }
    }
    
    /**
     * Capture new comments or node names.
     * Button events
     * @param value object value
     * @param row int row number
     * @param col int col number
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == NAME_COLUMN) {
            _mainArray.get(row).setName( (String) value );
        }  else if (col == COMMENT_COLUMN) {        
            _mainArray.get(row).setComment( (String) value );
        }
        else if (col == STATE_COLUMN) {
            _mainArray.get(row).setState( (CbusTableEvent.EvState) value );
            updateGuiCell(row,col);
        }
        else if (col == DELETE_BUTTON_COLUMN) {
            ThreadingUtil.runOnGUI( ()->{  ta.buttonDeleteClicked(row); });
        }
        else if (col == ON_BUTTON_COLUMN) {
            _mainArray.get(row).sendEvent(CbusTableEvent.EvState.ON); // gui update from outgoing msg
        }
        else if (col == OFF_BUTTON_COLUMN) {
            _mainArray.get(row).sendEvent(CbusTableEvent.EvState.OFF); // gui update from outgoing msg
        }        
        else if (col == TOGGLE_BUTTON_COLUMN) {
            _mainArray.get(row).sendEvent(CbusTableEvent.EvState.TOGGLE); // gui update from outgoing msg
        }
        else if (col == STATUS_REQUEST_BUTTON_COLUMN) {
            _mainArray.get(row).sendEvent(CbusTableEvent.EvState.REQUEST); // gui update from outgoing msg
        }
        else if (col == SESSION_ON_COLUMN) {
            _mainArray.get(row).bumpSessionOn();
            updateGuiCell(row,col);
            updateGuiCell(row,SESSION_TOTAL_COLUMN);
            }
        else if (col == SESSION_OFF_COLUMN) {
            _mainArray.get(row).bumpSessionOff();
            updateGuiCell(row,col);
            updateGuiCell(row,SESSION_TOTAL_COLUMN);
        }
        else if (col == SESSION_IN_COLUMN) {
            _mainArray.get(row).bumpSessionIn();
            updateGuiCell(row,col);
        }
        else if (col == CANID_COLUMN) {
            _mainArray.get(row).setCanId( (Integer) value);
            updateGuiCell(row,col);
        }
        else if (col == SESSION_OUT_COLUMN) {
            _mainArray.get(row).bumpSessionOut();
            updateGuiCell(row,col);
        }
        else if (col == LATEST_TIMESTAMP_COLUMN) {
            _mainArray.get(row).setDate( new Date() );
            updateGuiCell(row,col);
        }
        else if (col == STLR_ON_COLUMN) {
            _mainArray.get(row).setStlOn( (String) value );
            updateGuiCell(row,col);
        }
        else if (col == STLR_OFF_COLUMN) {
            _mainArray.get(row).setStlOff( (String) value );
            updateGuiCell(row,col);
        }
        // table is dirty
        ta._saved = false;
    }
    
    private void updateGuiCell( int row, int col){
        ThreadingUtil.runOnGUI( ()->{
            fireTableCellUpdated(row, col);
        });
    }

    /**
     * Report whether table has changed.
     * @return boolean
     */
    public boolean isTableDirty() {
        return(ta._saved);
    }

    /**
     * Capture node and event, check if isevent and send to parse from message.
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        int opc = CbusMessage.getOpcode(m);
        if (!CbusOpCodes.isEvent(opc)) {
            return;
        }
        
        int nn = CbusMessage.getNodeNumber(m);
        int en = CbusMessage.getEvent(m);
        
        CbusTableEvent.EvState state = CbusTableEvent.EvState.OFF;
        if (CbusOpCodes.isOnEvent(opc)) {
            state = CbusTableEvent.EvState.ON;
        }
        if (CbusOpCodes.isEventRequest(opc)) {
            state = CbusTableEvent.EvState.REQUEST;
            parseMessage( CbusMessage.getId(m), nn, en, state, 0 , 0 );
        }
        else {
            parseMessage( CbusMessage.getId(m), nn, en, state, 0 , 1 );
        }
    }
    
    /**
     * Capture node and event, check isevent and send to parse from reply.
     * @param m canmessage
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        int opc = CbusMessage.getOpcode(m);
        if (!CbusOpCodes.isEvent(opc)) {
            return;
        }
        
        int nn = CbusMessage.getNodeNumber(m);
        int en = CbusMessage.getEvent(m);
        
        CbusTableEvent.EvState state = CbusTableEvent.EvState.OFF;
        if (CbusOpCodes.isOnEvent(opc)) {
            state = CbusTableEvent.EvState.ON;
        }
        if (CbusOpCodes.isEventRequest(opc)) {
            state = CbusTableEvent.EvState.REQUEST;
            parseMessage( CbusMessage.getId(m), nn, en, state, 0,0 );
        } else {
            parseMessage( CbusMessage.getId(m), nn, en, state, 1,0 );
        }
    }
    
    /**
     * If new event add to table, else update table.
     * takes canid, node, event, onoroff
     * @since 4.13.3
     * @param canid of can message 
     * @param node of can message 
     * @param event of can message 
     */
    public void parseMessage( int canid, int node, int event, CbusTableEvent.EvState state, int in, int out) {
        
        int existingRow = seeIfEventOnTable( node, event);
        
        if (existingRow<0) {
            int on=0;
            int off=0;
            if (state==CbusTableEvent.EvState.ON) {
                on=1;
            }
            if (state==CbusTableEvent.EvState.OFF) {
                off=1;
            }
            addEvent(node,event,canid,state,"","",on,off,in,out); // on off in out
        } else {
            setValueAt(state, existingRow, STATE_COLUMN);
             if ( (state==CbusTableEvent.EvState.ON) || (state==CbusTableEvent.EvState.OFF) ) {
                setValueAt(1, existingRow, LATEST_TIMESTAMP_COLUMN);
            }
            setValueAt(canid, existingRow, CANID_COLUMN);
            if (state==CbusTableEvent.EvState.ON) { setValueAt(1, existingRow, SESSION_ON_COLUMN); }
            if (state==CbusTableEvent.EvState.OFF) { setValueAt(1, existingRow, SESSION_OFF_COLUMN); }
            if (in==1) {  setValueAt(1, existingRow, SESSION_IN_COLUMN); }
            if (out==1) { setValueAt(1, existingRow, SESSION_OUT_COLUMN); }
            
        }
    }
    
    /**
     * Do Node + Event check, returns -1 if not on table, otherwise the row id
     * @since 4.13.3
     * @param event int
     * @param node int
     * @return int of row, otherwise -1
     */
    public int seeIfEventOnTable( int node, int event) {
        for (int i = 0; i < getRowCount(); i++) {
            if (_mainArray.get(i).matches(node, event)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Get event name for an event in the table
     * @param event int
     * @param node int
     * @return String of event name, empty string if not present.
     */
    public String getEventName( int node, int event ) {
        for (int i = 0; i < getRowCount(); i++) {
            if (_mainArray.get(i).matches(node, event)) {
                return _mainArray.get(i).getName();
            }
        }
        return "";
    }
    
    /**
     * Get event name for an event in the table
     * @param event int
     * @param node int
     * @return String of event name, empty string if not present.
     */
    public String getEventString( int node, int event ) {
        // log.warn("fetching node {} event {}",node,event);
        for (int i = 0; i < getRowCount(); i++) {
            if (_mainArray.get(i).matches(node, event)) {
                return _mainArray.get(i).toString();
            }
        }
        return("");
    }
    
    /**
     * Register new event to table
     */
    public void addEvent(int node, int event, int canid, CbusTableEvent.EvState state, 
        String eventName, String evComment, int on, int off, int in, int out) {
        
        Date tmpdate = null;
        if ( ( in > 0 ) || ( out > 0 ) ) {
            tmpdate = new Date();
        }
        
        CbusTableEvent newtabev = new CbusTableEvent(
            node, event, 
            state, canid, eventName, evComment, 
            on, off, in, out, tmpdate );
        _mainArray.add(newtabev);
        // notify the JTable object that a row has changed; do that in the Swing thread!
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsInserted((getRowCount()-1), (getRowCount()-1)); });
        addToLog(1,newtabev.toString() + Bundle.getMessage("AddedToTable"));
    }
    
    public CbusTableEvent provideEvent(int nn, int en){
        for (int i = 0; i < getRowCount(); i++) {
            if (_mainArray.get(i).matches(nn, en)) {
                return _mainArray.get(i);
            }
        }
        // not existing so creating new
        
        CbusTableEvent newtabev = new CbusTableEvent(nn,en,CbusTableEvent.EvState.UNKNOWN, -1, "", "", 0, 0, 0, 0, null );
        _mainArray.add(newtabev);
        // notify the JTable object that a row has changed; do that in the Swing thread!
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsInserted((getRowCount()-1), (getRowCount()-1)); });
        addToLog(1,newtabev.toString() + Bundle.getMessage("AddedToTable"));
        
        return newtabev;
        
    }
    
    /**
     * Remove Row from table
     * @param row int row number
     */    
    void removeRow(int row) {
        _context = _mainArray.get(row).toString() + " " + Bundle.getMessage("TableConfirmDelete");
        _mainArray.remove(row);
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsDeleted(row,row); });
        addToLog(3,_context);
    }
        
    /**
     * Add to Event Table Console Log
     * @param cbuserror int
     * @param cbustext String console message
     */
    public void addToLog(int cbuserror, String cbustext) {
        ThreadingUtil.runOnGUI( ()->{  
            tablefeedback.append( "\n"+cbustext);
        });
    }
    
    /**
     * disconnect from the CBUS
     */
    public void dispose() {
        // eventTable.removeAllElements();
        // eventTable = null;
        tablefeedback.dispose();
        if (tc != null) {
            tc.removeCanListener(this);
        }
    }

    public TextAreaFIFO tablefeedback(){
        return tablefeedback;
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusEventTableDataModel.class);
}
