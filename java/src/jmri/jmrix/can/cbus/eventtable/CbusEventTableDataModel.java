package jmri.jmrix.can.cbus.eventtable;

import java.util.ArrayList;
import java.util.Arrays;
import jmri.InstanceManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.ShutDownTask;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CBUS events
 *
 * @author Andrew Crosland (C) 2009
 * @author Steve Young (c) 2018 2019
 * 
 */
public class CbusEventTableDataModel extends CbusBasicEventTableModel implements CanListener {

    
    private final CbusPreferences preferences;
    private final ShutDownTask shutDownTask;
    
    public CbusEventTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        super(memo);
        log.info("Starting MERG CBUS Event Table");
        
        addTc(_memo);
        
        
        preferences = jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
        
        checkRestoreEvents();
        
        shutDownTask = new CbusEventTableShutdownTask("CbusEventTableShutdownTask",this);
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(shutDownTask);
        ta.updatejmricols();
        
    }
    
    public final static void checkCreateNewEventModel(CanSystemConnectionMemo memo){
        CbusEventTableDataModel model = InstanceManager.getNullableDefault(CbusEventTableDataModel.class);
        if (model == null) {        
            ThreadingUtil.runOnLayout(() -> {
                CbusEventTableDataModel eventModel = new CbusEventTableDataModel(memo, 5, CbusEventTableDataModel.MAX_COLUMN);
                InstanceManager.store(eventModel, CbusEventTableDataModel.class);
            });
        }    
    }
    
    private void checkRestoreEvents(){
        if ( preferences !=null && preferences.getSaveRestoreEventTable() ){
                CbusEventTableXmlAction.restoreEventsFromXmlTablestart(this);
        }
    }

    /**
     * De-register the shut down task which saves table details.
     */
    public void skipSaveOnDispose(){
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(shutDownTask);
    }
    
    /**
     * Get the Column Tooltips.
     *<p>
     * Order needs to match column list
     */
    public static final String[] CBUS_EV_TABLE_COL_TOOLTIPS = {
        Bundle.getMessage("NodeColTip"),  // NOI18N
        Bundle.getMessage("EventColTip"),  // NOI18N
        Bundle.getMessage("NameColTip"),  // NOI18N
        Bundle.getMessage("CbusNodeNameTip"),  // NOI18N
        Bundle.getMessage("CommentColTip"),  // NOI18N
        Bundle.getMessage("TypeColTip"), // NOI18N
        Bundle.getMessage("SendToggleTip"), // NOI18N
        Bundle.getMessage("SendOntip"), // NOI18N
        Bundle.getMessage("SendOfftip"), // NOI18N
        Bundle.getMessage("IDColTip"), // NOI18N
        Bundle.getMessage("ColumnLastHeard"), // NOI18N
        Bundle.getMessage("ColumnRequestStatusTip"), // NOI18N
        Bundle.getMessage("ColumnTotalSession"), // NOI18N
        Bundle.getMessage("ColumnOnSession"), // NOI18N
        Bundle.getMessage("ColumnOffSession"), // NOI18N
        Bundle.getMessage("ColumnInSessionTip"), // NOI18N
        Bundle.getMessage("ColumnOutSessionTip"), // NOI18N
        null,
        null,
        null,
        null,
        null,
        Bundle.getMessage("ColumnEventDeleteTip"), // NOI18N
        Bundle.getMessage("StlrOnTip"), // NOI18N
        Bundle.getMessage("StlrOffTip"), // NOI18N
        null,
        null,
        null

    }; // Length = number of items in array should (at least) match number of columns
    
    private final static String[] COLUMN_NAMES = new String[] {
        "CbusNode","CbusEvent","ColumnName","CbusNodeName","ColumnComment","CbusEventOnOrOff", //0-5
        "ColumnToggle", "CbusSendOnHeader", "CbusSendOffHeader","CanID","ColumnLastHeard","ColumnStatusRequest", // 6-11
        "ColumnTotalSession","ColumnOnSession","ColumnOffSession","ColumnInSession","ColumnOutSession", // 12-16
        "ColumnTotalAll", "ColumnTotalOn", "ColumnTotalOff", "ColumnTotalIn", "ColumnTotalOut", // 17-21
        "ColumnEventDelete","JmriOnEv","JmriOffEv","Dat1","Dat2","Dat3" }; // NOI18N
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) {
        return Bundle.getMessage(COLUMN_NAMES[col]);
    }
    
    public final static int[]BUTTON_COLUMNS = new int[]{TOGGLE_BUTTON_COLUMN, ON_BUTTON_COLUMN,
        OFF_BUTTON_COLUMN, STATUS_REQUEST_BUTTON_COLUMN, DELETE_BUTTON_COLUMN};
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        if (_mainArray.isEmpty() || null == getValueAt(0, col)) {
            return Object.class;
        }
        return getValueAt(0, col).getClass();
    }
    
    private final static int[] EDITABLE_COLS =new int[]{ 
        NAME_COLUMN, COMMENT_COLUMN, TOGGLE_BUTTON_COLUMN, ON_BUTTON_COLUMN, 
        OFF_BUTTON_COLUMN, STATUS_REQUEST_BUTTON_COLUMN, DELETE_BUTTON_COLUMN }; 
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return Arrays.stream(EDITABLE_COLS).anyMatch(i -> i == col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col==DELETE_BUTTON_COLUMN) {
            ThreadingUtil.runOnGUIEventually(() -> ta.buttonDeleteClicked(row));
        } else {                
            super.setValueAt(value, row, col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        ta.parseMessage( m);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        ta.parseMessage( m);
    }
    
    /**
     * Remove all events from table
     */
    protected void clearAllEvents() {
        _mainArray = new ArrayList<>();
    }
    
    public CbusEventBeanData getEventBeans(int nn, int en, CbusTableEvent.EvState state) {
        return provideEvent(nn, en).getBeans(state);
    }
    
    /**
     * Disconnect from the CBUS.
     * Check and trigger if need to save table to xml.
     */
    public void dispose() {
        ta.dispose();
        
        if ( preferences !=null && preferences.getSaveRestoreEventTable() ){
            CbusEventTableXmlAction.storeEventsToXml(this);
        }
        removeTc(_memo);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventTableDataModel.class);
}
