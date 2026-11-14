package jmri.jmrix.can.cbus.eventtable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.swing.JCheckBox;

import jmri.Manager;
import jmri.NamedBean;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.jmrix.can.cbus.CbusEventInterface;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

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

    private void linkHwaddtoEvent(NamedBean bean, boolean beanState, CanMessage m, boolean fire){
        if (m==null){
            return;
        }
        int en = CbusMessage.getEvent(m);
        int nn = CbusMessage.getNodeNumber(m);
        CbusTableEvent event = _model.provideEvent(nn,en);
        event.appendOnOffBean(bean, beanState, CbusEvent.getEvState(m));
        if (fire) {
            fireJmriCellsChanged(_model.getEventTableRow(nn, en));
        }
    }

    /**
     * Associate a Sensor, Turnout or Light with the events it uses.
     * @param nb the bean
     * @param fire true to update the rows changed in the GUI
     */
    private void linkBean(NamedBean nb, boolean fire){
        if (nb instanceof CbusEventInterface) {
            linkHwaddtoEvent( nb, true, ((CbusEventInterface)nb).getBeanOnMessage(), fire);
            linkHwaddtoEvent( nb, false, ((CbusEventInterface)nb).getBeanOffMessage(), fire);
        }
    }

    /**
     * Remove a Sensor, Turnout or Light from every event it is associated with.
     * @param nb the bean
     */
    private void unlinkBean(NamedBean nb){
        for (int i=0; i < _model.getRowCount(); i++) {
            if (_model._mainArray.get(i).removeBean(nb)) {
                fireJmriCellsChanged(i);
            }
        }
    }

    /**
     * Update in the GUI the rows a Sensor, Turnout or Light is associated with,
     * eg. after its user name changes.
     * @param nb the bean
     */
    private void fireJmriCellsChanged(NamedBean nb){
        for (int i=0; i < _model.getRowCount(); i++) {
            if (_model._mainArray.get(i).hasBean(nb)) {
                fireJmriCellsChanged(i);
            }
        }
    }

    private void fireJmriCellsChanged(int row){
        ThreadingUtil.runOnGUIEventually( ()->{
            _model.fireTableCellUpdated(row, CbusEventTableDataModel.STLR_ON_COLUMN);
            _model.fireTableCellUpdated(row, CbusEventTableDataModel.STLR_OFF_COLUMN);
        });
    }

    private void fireAllJmriCellsChanged(){
        ThreadingUtil.runOnGUIEventually( ()->{
            int rows = _model.getRowCount();
            if (rows > 0) {
                _model.fireTableRowsUpdated(0, rows - 1);
            }
        });
    }

    /**
     * Update all columns for JMRI Sensor, Turnout and light details
     */
    public void updatejmricols(){

        // reset all columns
        _model._mainArray.forEach((n) -> n.resetBeans() );
        for (Class<?> classType : classTypes) {
            jmri.Manager<?> sm = (jmri.Manager<?>) _model._memo.get(classType);
            sm.getNamedBeanSet().forEach((nb) -> linkBean(nb, false));
        }
        fireAllJmriCellsChanged();
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
                ((jmri.Manager<?>) _model._memo.get(classType)).addPropertyChangeListener(this);
            } else {
                ((jmri.Manager<?>) _model._memo.get(classType)).removePropertyChangeListener(this);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        switch (ev.getPropertyName()) {
            case Manager.PROPERTY_BEANS:
                // one bean added or removed, so update only the events it uses
                if (ev.getNewValue() instanceof NamedBean) {
                    linkBean((NamedBean) ev.getNewValue(), true);
                } else if (ev.getOldValue() instanceof NamedBean) {
                    unlinkBean((NamedBean) ev.getOldValue());
                } else {
                    // no bean given: the set as a whole may have changed, eg.
                    // "beans" events were silenced while beans were added
                    updatejmricols();
                }
                break;
            case Manager.PROPERTY_LENGTH:
                // follows each "beans" event; while "beans" events are
                // silenced, one without a bean follows when they resume
                break;
            case Manager.PROPERTY_DISPLAY_LIST_NAME:
                // a user name changed; the names are worked out when the rows
                // are drawn, so only the rows of that bean need drawing again
                NamedBean renamed = (ev.getNewValue() instanceof String && ev.getSource() instanceof Manager)
                    ? ((Manager<?>) ev.getSource()).getByUserName((String) ev.getNewValue()) : null;
                if (renamed != null) {
                    fireJmriCellsChanged(renamed);
                } else {
                    fireAllJmriCellsChanged();
                }
                break;
            default:
                updatejmricols();
                break;
        }
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

                if (JmriJOptionPane.OK_OPTION == JmriJOptionPane.showConfirmDialog(
                        null, params, Bundle.getMessage("DelEvPopTitle"),  // NOI18N
                        JmriJOptionPane.OK_CANCEL_OPTION,
                    JmriJOptionPane.WARNING_MESSAGE)) {

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
        if (row < 0) {
            return;
        }
        // One row update rather than one update per statistics column,
        // as this is called for every event frame.
        ThreadingUtil.runOnGUIEventually(() -> _model.fireTableRowsUpdated(row, row));
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

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusEventTableAction.class);
}
