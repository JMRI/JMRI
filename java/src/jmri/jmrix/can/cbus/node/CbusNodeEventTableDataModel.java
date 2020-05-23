package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusNameService;
import jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CBUS Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusNodeEventTableDataModel extends javax.swing.table.AbstractTableModel 
    implements PropertyChangeListener {

    private final CbusNameService nameService;
    private CbusNode nodeOfInterest;
    private final NodeConfigToolPane _mainpane;
    
    // column order needs to match list in column tooltips
    static public final int NODE_NUMBER_COLUMN = 0;
    static public final int EVENT_NUMBER_COLUMN = 1;
    static public final int NODE_EDIT_BUTTON_COLUMN = 2;
    static public final int NODE_NAME_COLUMN = 3;
    static public final int EVENT_NAME_COLUMN = 4;
    static public final int EV_VARS_COLUMN = 5;
    static public final int EV_INDEX_COLUMN = 6;
    static public final int MAX_COLUMN = 7;
    
    private final CanSystemConnectionMemo _memo;

    public CbusNodeEventTableDataModel( NodeConfigToolPane mainpane, CanSystemConnectionMemo memo, int row, int column) {
        
        log.debug("Starting MERG CBUS Node Event Table");
        _mainpane = mainpane;
        _memo = memo;
        nameService = new CbusNameService(memo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        try {
            return Math.max(0,nodeOfInterest.getNodeEventManager().getTotalNodeEvents() );
        } catch (NullPointerException e) { // in case no node loaded
            return 0;
        }
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
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case EVENT_NUMBER_COLUMN:
                return ("Event Number");
            case NODE_NUMBER_COLUMN:
                return ("Node Number");
            case NODE_EDIT_BUTTON_COLUMN:
                return ("Edit");
            case NODE_NAME_COLUMN:
                return ("Node Name");
            case EVENT_NAME_COLUMN:
                return ("Event Name");
            case EV_VARS_COLUMN:
                return ("Event Variables");
            case EV_INDEX_COLUMN:
                return ("Index");
            default:
                return "unknown " + col; // NOI18N
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case EVENT_NUMBER_COLUMN:
            case NODE_NUMBER_COLUMN:
            case EV_INDEX_COLUMN:
                return Integer.class;
            case NODE_NAME_COLUMN:
            case EVENT_NAME_COLUMN:
            case EV_VARS_COLUMN:
                return String.class;
            case NODE_EDIT_BUTTON_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NODE_EDIT_BUTTON_COLUMN:
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        
        if ( nodeOfInterest == null ){
            return null;
        }
        CbusNodeEvent toFetch = nodeOfInterest.getNodeEventManager().getNodeEventByArrayID(row);
        if (toFetch==null){
            return null;
        }
        
        switch (col) {
            case NODE_NUMBER_COLUMN:
                return toFetch.getNn();
            case EVENT_NUMBER_COLUMN:
                return toFetch.getEn();
            case NODE_EDIT_BUTTON_COLUMN:
                return "Edit";
            case NODE_NAME_COLUMN:
                if ( !toFetch.getTempFcuNodeName().isEmpty() ) {
                    return toFetch.getTempFcuNodeName();
                }
                else {
                    return nameService.getNodeName( toFetch.getNn() );
                }
            case EVENT_NAME_COLUMN:
                if ( !toFetch.getName().isEmpty() ) {
                    return toFetch.getName();
                }
                else {
                    return nameService.getEventName( toFetch.getNn(), toFetch.getEn()  );
                }
            case EV_VARS_COLUMN:
                return toFetch.getEvVarString();
            case EV_INDEX_COLUMN:
                return toFetch.getIndex();
            default:
                return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == NODE_EDIT_BUTTON_COLUMN && _mainpane!=null) {
            CbusNodeEvent ndEv = nodeOfInterest.getNodeEventManager().getNodeEventByArrayID(row);
            ThreadingUtil.runOnGUIDelayed( ()->{
                _mainpane.getEditEvFrame().initComponents(_memo, ndEv );                
            },10);
        }
    }
    
    public void setNode( CbusNode node){
        
        if (node == nodeOfInterest){
            return;
        }
        if ( nodeOfInterest != null ){
            nodeOfInterest.removePropertyChangeListener(this);
        }
        nodeOfInterest = node;
        if ( nodeOfInterest != null ){
            nodeOfInterest.addPropertyChangeListener(this);
        }
        fireTableDataChanged();
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        if (ev.getPropertyName().equals("SINGLEEVUPDATE")) {
            int newValue = (Integer) ev.getNewValue();
            fireTableRowsUpdated(newValue,newValue);
        }
        else if (ev.getPropertyName().equals("ALLEVUPDATE")) {
            fireTableDataChanged();
        }
    }
    
    /**
     * Removes Node Listener if still monitoring a Node.
     */
    public void dispose(){
        setNode( null);
    }
    
    /**
     * Remove Row from table
     * @param row int row number
     */    
    void removeRow(int row) {
    //   _mainArray.remove(row);
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsDeleted(row,row); });
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTableDataModel.class);
}
