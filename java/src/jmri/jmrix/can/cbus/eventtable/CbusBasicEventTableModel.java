package jmri.jmrix.can.cbus.eventtable;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.jmrix.can.cbus.CbusNameService;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to provide access to the EventTableData.xml file.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusBasicEventTableModel extends javax.swing.table.AbstractTableModel {

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

    static public final int ALL_TOTAL_COLUMN = 17;
    static public final int ALL_ON_COLUMN = 18;
    static public final int ALL_OFF_COLUMN = 19;
    static public final int ALL_IN_COLUMN = 20;
    static public final int ALL_OUT_COLUMN = 21;

    static public final int DELETE_BUTTON_COLUMN = 22;
    static public final int STLR_ON_COLUMN = 23;
    static public final int STLR_OFF_COLUMN = 24;

    public final static int EVENT_DAT_1 = 25;
    public final static int EVENT_DAT_2 = 26;
    public final static int EVENT_DAT_3 = 27;

    static public final int MAX_COLUMN = 28;

    protected final CanSystemConnectionMemo _memo;
    protected ArrayList<CbusTableEvent> _mainArray;
    public final CbusEventTableAction ta;
    public final static int[] INITIAL_COLS = new int[]{ 0,1,2,4,5,6,23,24 };

    // list of Columns which may update on receipt of CanFrame.
    static final int[] canFrameCols = new int[]{
        CANID_COLUMN, LATEST_TIMESTAMP_COLUMN, STATE_COLUMN, TOGGLE_BUTTON_COLUMN,
        SESSION_TOTAL_COLUMN, SESSION_IN_COLUMN, SESSION_OUT_COLUMN, SESSION_OFF_COLUMN, SESSION_ON_COLUMN,
        ALL_TOTAL_COLUMN, ALL_ON_COLUMN, ALL_OFF_COLUMN, ALL_IN_COLUMN, ALL_OUT_COLUMN,
        EVENT_DAT_1, EVENT_DAT_2, EVENT_DAT_3 };

    public CbusBasicEventTableModel(CanSystemConnectionMemo memo) {
        _memo = memo;
        _mainArray = new ArrayList<>();
        ta = new CbusEventTableAction( this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return MAX_COLUMN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return _mainArray.size();
    }

    /**
     * {@inheritDoc}
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
                return new CbusNameService(_memo).getNodeName( _mainArray.get(row).getNn() );
            case COMMENT_COLUMN:
                return _mainArray.get(row).getComment();
            case STATE_COLUMN:
                return _mainArray.get(row).getState();
            default:
                return getValueAtPt2(row,col);
        }
    }

    private Object getValueAtPt2(int row, int col) {
        switch (col) {
            case TOGGLE_BUTTON_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if ( _mainArray.get(row).getState()==CbusTableEvent.EvState.OFF ) {
                    return Bundle.getMessage("CbusSendOn"); // NOI18N
                } else {
                    return Bundle.getMessage("CbusSendOff"); // NOI18N
                }
            case ON_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOn"); // NOI18N
            case OFF_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOff");
            case CANID_COLUMN:
                return _mainArray.get(row).getEventCanId();
            case LATEST_TIMESTAMP_COLUMN:
                return _mainArray.get(row).getDate();
            default:
                return getValueAtPt3(row,col);
        }
    }

    private Object getValueAtPt3(int row, int col) {
        switch (col) {
            case SESSION_TOTAL_COLUMN:
                return (_mainArray.get(row).getSessionOnOff(true) + _mainArray.get(row).getSessionOnOff(false) );
            case SESSION_ON_COLUMN:
                return _mainArray.get(row).getSessionOnOff(true);
            case SESSION_OFF_COLUMN:
                return _mainArray.get(row).getSessionOnOff(false);
            case SESSION_IN_COLUMN:
                return _mainArray.get(row).getSessionInOut(true);
            case SESSION_OUT_COLUMN:
                return _mainArray.get(row).getSessionInOut(false);
            default:
                return getValueAtPt4(row,col);
        }
    }

    private Object getValueAtPt4(int row, int col) {
        switch (col) {
            case ALL_TOTAL_COLUMN:
                return (_mainArray.get(row).getTotalInOut(true) + _mainArray.get(row).getTotalInOut(false) );
            case ALL_ON_COLUMN:
                return _mainArray.get(row).getTotalOnOff(true);
            case ALL_OFF_COLUMN:
                return _mainArray.get(row).getTotalOnOff(false);
            case ALL_IN_COLUMN:
                return _mainArray.get(row).getTotalInOut(true);
            case ALL_OUT_COLUMN:
                return _mainArray.get(row).getTotalInOut(false);
            default:
                return getValueAtPt5(row,col);
        }
    }

    private Object getValueAtPt5(int row, int col) {
        switch (col) {
            case STATE_COLUMN:
                return _mainArray.get(row).getState();
            case STATUS_REQUEST_BUTTON_COLUMN:
                return Bundle.getMessage("StatusButton"); // NOI18N
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ButtonDelete"); // NOI18N
            case STLR_ON_COLUMN:
                return _mainArray.get(row).getBeans(CbusTableEvent.EvState.ON);
            case STLR_OFF_COLUMN:
                return _mainArray.get(row).getBeans(CbusTableEvent.EvState.OFF);
            default:
                return getValueAtPt6(row,col);
        }
    }

    private Object getValueAtPt6(int row, int col) {
        switch (col) {
            case EVENT_DAT_1:
                return _mainArray.get(row).getData(1);
            case EVENT_DAT_2:
                return _mainArray.get(row).getData(2);
            case EVENT_DAT_3:
                return _mainArray.get(row).getData(3);
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case NAME_COLUMN:
                _mainArray.get(row).setName( (String) value );
                break;
            case COMMENT_COLUMN:
                _mainArray.get(row).setComment( (String) value );
                break;
            case ON_BUTTON_COLUMN:
                _mainArray.get(row).sendEvent(CbusTableEvent.EvState.ON);
                break;
            case OFF_BUTTON_COLUMN:
                _mainArray.get(row).sendEvent(CbusTableEvent.EvState.OFF);
                break;
            default:
                setValueAt2( value,  row,  col);
        }
    }

    public void setValueAt2(Object value, int row, int col) {
        switch (col) {
            case TOGGLE_BUTTON_COLUMN:
                _mainArray.get(row).sendEvent(CbusTableEvent.EvState.TOGGLE);
                ta.updateGuiCell(row,col);
                break;
            case STATUS_REQUEST_BUTTON_COLUMN:
                _mainArray.get(row).sendEvent(CbusTableEvent.EvState.REQUEST);
                break;
            default:
                log.error("Invalid Column {}",col);
                break;
        }
    }



    /**
     * Provide a new Event and add to Table.
     * @param nn Node Number
     * @param en Event Number
     * @return New or existing table event.
     */
    @Nonnull
    public CbusTableEvent provideEvent(int nn, int en){
        if (getEventTableRow(nn,en)>-1){
            return _mainArray.get(getEventTableRow(nn,en));
        }
        // not existing so creating new
        CbusTableEvent newtabev = new CbusTableEvent(_memo,nn,en );
        _mainArray.add(newtabev);
        fireTableDataChanged();
        return newtabev;
    }

    /**
     * Do Node + Event check, returns -1 if not on table, otherwise the row id
     * @since 4.13.3
     * @param event int
     * @param node int
     * @return int of row, otherwise -1
     */
    public int getEventTableRow( int node, int event) {
        return _mainArray.indexOf(new CbusEvent(node,event));
    }

    /**
     * Get event name for an event in the table
     * @param event int
     * @param node int
     * @return String of event name, empty string if not present.
     */
    @Nonnull
    public String getEventName( int node, int event ) {
        int row = getEventTableRow(node,event);
        if (row > -1 ) {
            return _mainArray.get(row).getName();
        }
        return "";
    }

    /**
     * Get event String for an event in the table
     * @param event int
     * @param node int
     * @return String of event name, empty string if not present.
     */
    @Nonnull
    public String getEventString( int node, int event ) {
        int row = getEventTableRow(node,event);
        if (row > -1 ) {
            return _mainArray.get(row).toString();
        }
        return("");
    }

    /**
     * Get the core list containing all table events
     * @return actual array of events
     */
    @Nonnull
    public ArrayList<CbusTableEvent> getEvents() {
        return new ArrayList<>(_mainArray);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusBasicEventTableModel.class);

}
