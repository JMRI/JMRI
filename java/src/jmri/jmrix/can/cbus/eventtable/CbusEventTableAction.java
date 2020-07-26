package jmri.jmrix.can.cbus.eventtable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.jmrix.can.cbus.CbusEventInterface;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.util.ThreadingUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young (c) 2018, 2019
 * 
 */
public class CbusEventTableAction implements PropertyChangeListener {

    private final CbusBasicEventTableModel _model;
    
    public CbusEventTableAction( @Nonnull CbusBasicEventTableModel model) {
        _model = model;
        addRemoveListenersToNbManagers(true);
    }
    
    protected boolean sessionConfirmDeleteRow=true; // display confirm popup
    
    private void linkHwaddtoEvent(NamedBean bean, boolean beanState, CanMessage m){
        if (m==null){
            return;
        }
        int en = CbusMessage.getEvent(m);
        int nn = CbusMessage.getNodeNumber(m);
        CbusTableEvent event = _model.provideEvent(nn,en);
        event.appendOnOffBean(bean, beanState, CbusEvent.getEvState(m));
        fireJmriCellsChanged(_model.getEventTableRow(nn, en));
    }
    
    private void fireJmriCellsChanged(int row){
        ThreadingUtil.runOnGUIEventually( ()->{
            _model.fireTableCellUpdated(row, CbusEventTableDataModel.STLR_ON_COLUMN);
            _model.fireTableCellUpdated(row, CbusEventTableDataModel.STLR_OFF_COLUMN);
        });
    }
    
    private void fireAllJmriCellsChanged(){
        for (int i=0; i < _model.getRowCount(); i++) {
            fireJmriCellsChanged(i);
        }
    }
    
    /**
     * Update all columns for JMRI Sensor, Turnout and light details
     */
    public void updatejmricols(){
        
        // reset all columns
        _model._mainArray.forEach((n) -> n.resetBeans() );
        fireAllJmriCellsChanged();
        for (Class<?> classType : classTypes) {
            jmri.Manager<?> sm = (jmri.Manager) InstanceManager.getDefault(classType);
            sm.getNamedBeanSet().forEach((nb) -> {
                if (nb instanceof CbusEventInterface) {
                    linkHwaddtoEvent( nb, true, ((CbusEventInterface)nb).getBeanOnMessage());
                    linkHwaddtoEvent( nb, false, ((CbusEventInterface)nb).getBeanOffMessage());
                }
            });
        }
    }
    
    public void resetAllSessionTotals() {
        _model.getEvents().forEach( ( ev) ->{
            ev.resetSessionTotals();
            updateStatColumnsinGui(_model.getEventTableRow(ev.getNn(), ev.getEn()));
        });
    }
    
    private final Class<?>[] classTypes = new Class<?>[]{
        jmri.TurnoutManager.class,jmri.SensorManager.class,jmri.LightManager.class};
    
    protected final void addRemoveListenersToNbManagers(boolean add){
        for (Class<?> classType : classTypes) {
            if (add) {
                ((jmri.Manager) InstanceManager.getDefault(classType)).addPropertyChangeListener(this);
            } else {
                ((jmri.Manager) InstanceManager.getDefault(classType)).removePropertyChangeListener(this);
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        updatejmricols();
    }
    
    /**
     * Delete Button Clicked
     * See whether to display confirm popup
     * @param row int row number
     */
    public void buttonDeleteClicked(int row) {
        if (sessionConfirmDeleteRow) {
            // confirm deletion with the user
            ThreadingUtil.runOnGUI( ()-> {
                JCheckBox checkbox = new JCheckBox(Bundle.getMessage("PopupSessionConfirmDel")); // NOI18N
                String message = Bundle.getMessage("DelConfirmOne") + "\n"   
                + Bundle.getMessage("DelConfirmTwo"); // NOI18N
                Object[] params = {message, checkbox};

                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                        null, params, Bundle.getMessage("DelEvPopTitle"),  // NOI18N
                        JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE)) {

                        sessionConfirmDeleteRow=!checkbox.isSelected();
                        removeRow(row);
                }
            });
        } else {
            // no need to show warning, just delete
            removeRow(row);
        }
    }
    
    /**
     * Remove Row from table
     * @param row int row number
     */
    protected void removeRow(int row) {
        _model._mainArray.remove(row);
        ThreadingUtil.runOnGUIEventually( ()-> _model.fireTableRowsDeleted(row,row));
    }
    
    protected void updateGuiCell( int row, int col){
        ThreadingUtil.runOnGUIEventually(() -> _model.fireTableCellUpdated(row, col));
    }
    
    private void updateStatColumnsinGui( int row){
        for (int i : CbusEventTableDataModel.canFrameCols) {
            updateGuiCell(row,i);
        }
    }
    
        /**
     * If new event add to table, else update table.
     * @param m Message to process
     */
    protected void parseMessage( AbstractMessage m) {
        if (!CbusMessage.isEvent(m)) { // also checks for extended & rtr
            return;
        }
        CbusTableEvent ev = _model.provideEvent(CbusMessage.getNodeNumber(m),CbusMessage.getEvent(m));
        ev.setState(CbusTableEvent.getEvState(m)); // sets state, timestamp and on / off count
        ev.setCanId(CbusMessage.getId(m));
        ev.setDataFromFrame(m);
        ev.bumpDirection( (m instanceof CanReply) ? CbusConstants.EVENT_DIR_IN : CbusConstants.EVENT_DIR_OUT);
        updateStatColumnsinGui(_model.getEventTableRow(CbusMessage.getNodeNumber(m), CbusMessage.getEvent(m)));
        
    }
    
    public void dispose(){
        addRemoveListenersToNbManagers(false);
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableAction.class);
}
