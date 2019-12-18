package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
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

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus request events
 *
 * @author Steve Young (c) 2018
 * 
 */
public class CbusEventRequestDataModel extends javax.swing.table.AbstractTableModel implements CanListener {

    private boolean sessionConfirmDeleteRow=true; // display confirm popup
    private final int _defaultFeedback= 1;
    protected int _contype=0; // event table pane console message type
    protected String _context=null; // event table pane console text
    private final int _defaultfeedbackdelay = 4000;
    private static final int MAX_LINES = 500; // tablefeedback screen log size
    
    protected ArrayList<CbusEventRequestMonitorEvent> _mainArray;
    private final CbusNameService nameService;
    protected TextAreaFIFO tablefeedback;
    // private CanSystemConnectionMemo _memo;
    private final TrafficController tc;
    
    // column order needs to match list in column tooltips
    static public final int EVENT_COLUMN = 0; 
    static public final int NODE_COLUMN = 1; 
    static public final int NAME_COLUMN = 2;
    static public final int LATEST_TIMESTAMP_COLUMN = 3;
    static public final int STATUS_REQUEST_BUTTON_COLUMN = 4;
    static public final int LASTFEEDBACK_COLUMN = 5;
    static public final int FEEDBACKOUTSTANDING_COLUMN = 6;
    static public final int FEEDBACKREQUIRED_COLUMN = 7;
    static public final int FEEDBACKTIMEOUT_COLUMN = 8;
    static public final int FEEDBACKEVENT_COLUMN = 9;
    static public final int FEEDBACKNODE_COLUMN = 10;
    static public final int DELETE_BUTTON_COLUMN = 11;
    
    static public final int MAX_COLUMN = 12;

    CbusEventRequestDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        _mainArray = new ArrayList<>();
        tablefeedback = new TextAreaFIFO(MAX_LINES);
        // _memo = memo;
        tc = memo.getTrafficController();
        addTc(tc);
        nameService = new CbusNameService();
    }

    // order needs to match column list top of dtabledatamodel
    static protected final String[] columnToolTips = {
        Bundle.getMessage("EventColTip"),
        Bundle.getMessage("NodeColTip"),
        Bundle.getMessage("NameColTip"),
        Bundle.getMessage("ColumnLastHeard") + Bundle.getMessage("TypeColTip"),
        Bundle.getMessage("ColumnRequestStatusTip"),
        Bundle.getMessage("FBLastTip"),        
        Bundle.getMessage("FBOutstandingTip"),
        Bundle.getMessage("FBNumTip"),
        Bundle.getMessage("FBTimeoutTip"),
        Bundle.getMessage("FBEventTip"),
        Bundle.getMessage("FBNodeTip"),
        Bundle.getMessage("ColumnEventDeleteTip")

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
            case NODE_COLUMN:
                return Bundle.getMessage("CbusNode");
            case NAME_COLUMN:
                return Bundle.getMessage("ColumnName");
            case EVENT_COLUMN:
                return Bundle.getMessage("CbusEvent");
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnEventDelete");
            case STATUS_REQUEST_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnStatusRequest");
            case LATEST_TIMESTAMP_COLUMN:
                return Bundle.getMessage("ColumnLastHeard");
            case LASTFEEDBACK_COLUMN:
                return Bundle.getMessage("FBLast");                
            case FEEDBACKREQUIRED_COLUMN:
                return Bundle.getMessage("FBRequired");
            case FEEDBACKOUTSTANDING_COLUMN:
                return Bundle.getMessage("FBOutstanding");
            case FEEDBACKEVENT_COLUMN:
                return Bundle.getMessage("FBEvent");
            case FEEDBACKNODE_COLUMN:
                return Bundle.getMessage("FBNode");
            case FEEDBACKTIMEOUT_COLUMN:
                return Bundle.getMessage("FBTimeout");
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
            case NODE_COLUMN:
            case EVENT_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case LASTFEEDBACK_COLUMN:
            case FEEDBACKREQUIRED_COLUMN:
            case FEEDBACKOUTSTANDING_COLUMN:
            case FEEDBACKTIMEOUT_COLUMN:
            case FEEDBACKNODE_COLUMN:
            case FEEDBACKEVENT_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case LATEST_TIMESTAMP_COLUMN:
            case STATUS_REQUEST_BUTTON_COLUMN:
            case DELETE_BUTTON_COLUMN:
                return new JTextField(7).getPreferredSize().width;                
            case NAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            default:
                return new JTextField(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }

    /**
    * Returns column class type.
    * {@inheritDoc} 
    */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case EVENT_COLUMN:
            case NODE_COLUMN:
            case FEEDBACKREQUIRED_COLUMN:
            case FEEDBACKOUTSTANDING_COLUMN:
            case FEEDBACKEVENT_COLUMN:
            case FEEDBACKNODE_COLUMN:
            case FEEDBACKTIMEOUT_COLUMN:
                return Integer.class;
            case NAME_COLUMN:
                return String.class;
            case DELETE_BUTTON_COLUMN:
            case STATUS_REQUEST_BUTTON_COLUMN:
                return JButton.class;
            case LATEST_TIMESTAMP_COLUMN:
                return Date.class;
            case LASTFEEDBACK_COLUMN:
                return Enum.class;
            default:
                return null;
        }
    }
    
    /**
    * Boolean return to edit table cell or not
    * {@inheritDoc} 
    * @return boolean
    */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case DELETE_BUTTON_COLUMN:
            case STATUS_REQUEST_BUTTON_COLUMN:
            case FEEDBACKREQUIRED_COLUMN:
            case FEEDBACKEVENT_COLUMN:
            case FEEDBACKNODE_COLUMN:
            case FEEDBACKTIMEOUT_COLUMN:
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
                return  _mainArray.get(row).getNn();
            case EVENT_COLUMN:
                return _mainArray.get(row).getEn();
            case NAME_COLUMN:
                return nameService.getEventName(_mainArray.get(row).getNn(),_mainArray.get(row).getEn() );
            case STATUS_REQUEST_BUTTON_COLUMN:
                return Bundle.getMessage("StatusButton");
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            case LATEST_TIMESTAMP_COLUMN:
                return _mainArray.get(row).getDate();
            case LASTFEEDBACK_COLUMN:
                return _mainArray.get(row).getLastFb();
            case FEEDBACKREQUIRED_COLUMN:
                return _mainArray.get(row).getFeedbackTotReqd();
            case FEEDBACKOUTSTANDING_COLUMN:
                return _mainArray.get(row).getFeedbackOutstanding();
            case FEEDBACKEVENT_COLUMN:
                return _mainArray.get(row).getExtraEvent();
            case FEEDBACKNODE_COLUMN:
                return _mainArray.get(row).getExtraNode();
            case FEEDBACKTIMEOUT_COLUMN:
                return _mainArray.get(row).getFeedbackTimeout();
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
        // log.debug("427 set valueat called row: {} col: {}", row, col);
        switch (col) {
            case DELETE_BUTTON_COLUMN:
                buttonDeleteClicked(row);
                break;
            case STATUS_REQUEST_BUTTON_COLUMN:
                _mainArray.get(row).sendEvent(CbusEventRequestMonitorEvent.EvState.REQUEST); // gui updates from outgoing msg
                break;
            case LATEST_TIMESTAMP_COLUMN:
                _mainArray.get(row).setDate( new Date() );
                updateGui(row, col);
                break;
            case FEEDBACKREQUIRED_COLUMN:
                _mainArray.get(row).setFeedbackTotReqd( (int) value );
                updateGui(row, col);
                break;
            case FEEDBACKOUTSTANDING_COLUMN:
                _mainArray.get(row).setFeedbackOutstanding( (Integer) value );
                updateGui(row, col);
                break;
            case FEEDBACKEVENT_COLUMN:
                _mainArray.get(row).setExtraEvent( (int) value );
                updateGui(row, col);
                break;
            case FEEDBACKNODE_COLUMN:
                _mainArray.get(row).setExtraNode( (int) value );
                updateGui(row, col);
                break;
            case FEEDBACKTIMEOUT_COLUMN:
                _mainArray.get(row).setFeedbackTimeout( (int) value );
                updateGui(row, col);
                break;
            case LASTFEEDBACK_COLUMN:
                _mainArray.get(row).setLastFb( (CbusEventRequestMonitorEvent.FbState) value );
                updateGui(row, col);
                break;
            default:
                break;
        }
    }
    
    private void updateGui(int row, int col){
        ThreadingUtil.runOnGUIEventually( ()->{
            fireTableCellUpdated(row, col); 
        });
    }

    // outgoing cbus message
    // or incoming CanReply
    @Override
    public void message(CanMessage m) {
        if ( m.extendedOrRtr() ) {
            return;
        }
        int opc = CbusMessage.getOpcode(m);
        if (CbusOpCodes.isEventRequest(opc)) {
            processEvRequest( CbusMessage.getNodeNumber(m) , CbusMessage.getEvent(m) );
        }
        else if (CbusOpCodes.isEventNotRequest(opc)) {
            processEvent( CbusMessage.getNodeNumber(m) , CbusMessage.getEvent(m) );
        }
    }
    
    // incoming cbus message
    // handled the same as outgoing
    @Override
    public void reply(CanReply r) {
        if ( r.extendedOrRtr() ) {
            return;
        }
        CanMessage m = new CanMessage(r);
        message(m);
    }

    // called when event heard as CanReply / CanMessage
    private void processEvent( int nn, int en ){
        
        int existingRow = eventRow( nn, en);
        int fbRow = extraFeedbackRow( nn, en);
        if ( existingRow > -1 ) {
            _mainArray.get(existingRow).setResponseReceived();
            setValueAt(1, existingRow, LATEST_TIMESTAMP_COLUMN);
        }
        else if ( fbRow > -1 ){
            _mainArray.get(fbRow).setResponseReceived();
        }
    }
    
    // called when request heard as CanReply / CanMessage
    private void processEvRequest( int nn, int en ) {
        
        int existingRow = eventRow( nn, en);
        if (existingRow<0) {
            addEvent(nn,en,CbusEventRequestMonitorEvent.EvState.REQUEST,null); 
        }
        existingRow = eventRow( nn, en);
        _mainArray.get(existingRow).setRequestReceived();
        
    }
    
    protected int eventRow(int nn, int en) {
        for (int i = 0; i < getRowCount(); i++) {
            if (_mainArray.get(i).matches(nn, en)) {
                return i;
            }
        }
        return -1;
    }

    protected int extraFeedbackRow(int nn, int en) {
        for (int i = 0; i < getRowCount(); i++) {
            if (_mainArray.get(i).matchesFeedback(nn, en)) {
                return i;
            }
        }
        return -1;
    }

    public void addEvent(int node, int event, CbusEventRequestMonitorEvent.EvState state, Date timestamp) {
        
        CbusEventRequestMonitorEvent newmonitor = new CbusEventRequestMonitorEvent(
            node, event, state, timestamp, _defaultfeedbackdelay, 
            _defaultFeedback, this );
        
        _mainArray.add(newmonitor);
        
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsInserted((getRowCount()-1), (getRowCount()-1)); });
        addToLog(1,newmonitor.toString() + Bundle.getMessage("AddedToTable"));
    }

    /**
     * Remove Row from table
     * @see #buttonDeleteClicked
     * @param row int row number
     */    
    private void removeRow(int row) {
        _context = _mainArray.get(row).toString() + Bundle.getMessage("TableConfirmDelete");
        _mainArray.remove(row);
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsDeleted(row,row); });
        addToLog(3,_context);
    }
    
    /**
     * Delete Button Clicked
     * See whether to display confirm popup
     * @see #removeRow
     * @param row int row number
     */
    private void buttonDeleteClicked(int row) {
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
                    removeRow(row);
            }
        } else {
            // no need to show warning, just delete
            removeRow(row);
        }
    }
    
    /**
     * Add to Event Table Console Log
     * @param cbuserror int
     * @param cbustext String console message
     */
    public void addToLog(int cbuserror, String cbustext){
        ThreadingUtil.runOnGUI( ()->{  
            if (cbuserror==3) {
            tablefeedback.append ("\n * * * * * * * * * * * * * * * * * * * * * * " + cbustext);
            } else {
                tablefeedback.append( "\n"+cbustext);
            }
        });
    }
        
    /**
     * disconnect from the CBUS
     */
    public void dispose() {
        // eventTable.removeAllElements();
        // eventTable = null;
        for (int i = 0; i < getRowCount(); i++) {
            _mainArray.get(i).stopTheTimer();
        }
        _mainArray = null;
        
        tablefeedback.dispose();
        tc.removeCanListener(this);
        
    }

    protected TextAreaFIFO tablefeedback(){
        return tablefeedback;
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusEventRequestDataModel.class);
}
