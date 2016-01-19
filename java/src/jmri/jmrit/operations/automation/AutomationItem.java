package jmri.jmrit.operations.automation;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import jmri.jmrit.operations.automation.actions.Action;
import jmri.jmrit.operations.automation.actions.ActionCodes;
import jmri.jmrit.operations.automation.actions.BuildTrainAction;
import jmri.jmrit.operations.automation.actions.BuildTrainIfSelectedAction;
import jmri.jmrit.operations.automation.actions.HaltAction;
import jmri.jmrit.operations.automation.actions.MoveTrainAction;
import jmri.jmrit.operations.automation.actions.NoAction;
import jmri.jmrit.operations.automation.actions.PrintTrainManifestAction;
import jmri.jmrit.operations.automation.actions.PrintTrainManifestIfSelectedAction;
import jmri.jmrit.operations.automation.actions.ResetTrainAction;
import jmri.jmrit.operations.automation.actions.ResumeAutomationAction;
import jmri.jmrit.operations.automation.actions.RunAutomationAction;
import jmri.jmrit.operations.automation.actions.StopAutomationAction;
import jmri.jmrit.operations.automation.actions.TerminateTrainAction;
import jmri.jmrit.operations.automation.actions.UpdateSwitchListAction;
import jmri.jmrit.operations.automation.actions.WaitTrainAction;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents one automation item of a automation
 *
 * @author Daniel Boudreau Copyright (C) 2016
 * @version $Revision$
 */
public class AutomationItem implements java.beans.PropertyChangeListener {

    public static final String NONE = ""; // NOI18N

    protected String _id = NONE;
    protected int _sequenceId = 0; // used to determine order in automation
    protected Action _action = null;
    protected Train _train = null;
    protected RouteLocation _routeLocation = null;
    protected String _automationId = NONE;
    protected String _message = NONE;
    protected String _messageFail = NONE;
    protected boolean _haltFail = true;
    
    protected boolean _actionRunning = false; // when true action is running, for example waiting for a train
    protected boolean _actionSuccessful = true;

    public static final String ACTION_RUNNING_CHANGED_PROPERTY = "actionRunningChange"; // NOI18N
    public static final String DISPOSE = "automationItemDispose"; // NOI18N

    /**
     *
     * @param id
     */
    public AutomationItem(String id) {
        log.debug("New automation item id: {}", id);
        _id = id;
        setAction(new NoAction()); // the default
    }

    public String getId() {
        return _id;
    }

    public String toString() {
        return getId(); // for property changes
    }

    public int getSequenceId() {
        return _sequenceId;
    }

    public void setSequenceId(int sequence) {
        // property change not needed
        _sequenceId = sequence;
    }

    public void setAction(Action action) {
        Action old = _action;
        _action = action;
        if (action != null) {
            action.setAutomationItem(this); // associate action with this item
        }
        if (old != action) {
            setDirtyAndFirePropertyChange("AutomationItemActionChange", old, action); // NOI18N
        }
    }

    public Action getAction() {
        return _action;
    }

    public int getActionCode() {
        if (getAction() != null) {
            return getAction().getCode();
        }
        return ActionCodes.NO_ACTION;
    }

    public void doAction() {
        if (getAction() != null) {
            getAction().doAction();
        }
    }

    public void setTrain(Train train) {
        Train old = _train;
        _train = train;
        if (old != train) {
            setDirtyAndFirePropertyChange("AutomationItemTrainChange", old, train); // NOI18N
        }
    }

    public Train getTrain() {
        if (getAction() != null && getAction().isTrainMenuEnabled()) {
            return _train;
        }
        return null;
    }

    public void setRouteLocation(RouteLocation rl) {
        RouteLocation old = _routeLocation;
        _routeLocation = rl;
        if (old != rl) {
            setDirtyAndFirePropertyChange("AutomationItemRouteLocationChange", old, rl); // NOI18N
        }
    }

    public RouteLocation getRouteLocation() {
        if (getAction() != null && getAction().isRouteMenuEnabled()) {
            return _routeLocation;
        }
        return null;
    }

    /**
     * The automation for actions, not the automation associated with this item.
     * 
     * @param automation
     */
    public void setAutomation(Automation automation) {
        Automation old = AutomationManager.instance().getAutomationById(_automationId);
        if (automation != null)
            _automationId = automation.getId();
        else
            _automationId = NONE;
        if (old != automation) {
            setDirtyAndFirePropertyChange("AutomationItemAutomationChange", old, automation); // NOI18N
        }
    }

    /**
     * The automation for actions, not the automation associated with this item.
     * 
     * @return Automation for this action
     */
    public Automation getAutomation() {
        if (getAction() != null && getAction().isAutomationMenuEnabled()) {
            return AutomationManager.instance().getAutomationById(_automationId);
        }
        return null;
    }

    public void setMessage(String message) {
        String old = _message;
        _message = message;
        if (!old.equals(message)) {
            setDirtyAndFirePropertyChange("AutomationItemMessageChange", old, message); // NOI18N
        }
    }

    public String getMessage() {
        return _message;
    }

    public void setMessageFail(String message) {
        String old = _messageFail;
        _messageFail = message;
        if (!old.equals(message)) {
            setDirtyAndFirePropertyChange("AutomationItemMessageFailChange", old, message); // NOI18N
        }
    }

    public String getMessageFail() {
        return _messageFail;
    }

    public boolean isHaltFailureEnabled() {
        return _haltFail;
    }

    public void setHaltFailureEnabled(boolean enable) {
        boolean old = _haltFail;
        _haltFail = enable;
        if (old != enable) {
            setDirtyAndFirePropertyChange("AutomationItemHaltFailureChange", old, enable); // NOI18N
        }
    }
    
    public void setActionRunning(boolean actionRunning) {
        boolean old = _actionRunning;
        _actionRunning = actionRunning;
        if (old != actionRunning) {
            firePropertyChange(ACTION_RUNNING_CHANGED_PROPERTY, old, actionRunning); // NOI18N
        }
    }

    public boolean isActionRunning() {
        return _actionRunning;
    }

    public void setActionSuccessful(boolean successful) {
        boolean old = _actionSuccessful;
        _actionSuccessful = successful;
        if (old != successful) {
            setDirtyAndFirePropertyChange("actionSuccessful", old, successful); // NOI18N
        }
    }
    
    public boolean isActionSuccessful() {
        return _actionSuccessful;
    }
    
    public String getStatus() {
        return isActionSuccessful() ? Bundle.getMessage("OK") : Bundle.getMessage("FAILED");
    }

    public void copyItem(AutomationItem item) {
        setMessage(item.getMessage());
    }

    /**
     * Gets a list of all known automation actions
     * 
     * @return list of automation actions
     */
    public List<Action> getActionList() {
        List<Action> list = new ArrayList<Action>();
        list.add(new NoAction());
        list.add(new BuildTrainAction());
        list.add(new BuildTrainIfSelectedAction());
        list.add(new PrintTrainManifestAction());
        list.add(new PrintTrainManifestIfSelectedAction());
        list.add(new MoveTrainAction());
        list.add(new TerminateTrainAction());
        list.add(new ResetTrainAction());
        list.add(new WaitTrainAction());
        list.add(new UpdateSwitchListAction());
        list.add(new RunAutomationAction());
        list.add(new ResumeAutomationAction());
        list.add(new StopAutomationAction());
        list.add(new HaltAction());
        return list;
    }

    public JComboBox<Action> getActionComboBox() {
        JComboBox<Action> box = new JComboBox<>();
        for (Action action : getActionList())
            box.addItem(action);
        return box;
    }

    public Action getActionByCode(int code) {
        for (Action action : getActionList()) {
            if (action.getCode() == code)
                return action;
        }
        return null;
    }

    public void dispose() {
        setDirtyAndFirePropertyChange(DISPOSE, null, DISPOSE);
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-trains.xml
     *
     * @param e Consist XML element
     */
    public AutomationItem(Element e) {
        // if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in Automation Item element when reading operations");
        }
        if ((a = e.getAttribute(Xml.SEQUENCE_ID)) != null) {
            _sequenceId = Integer.parseInt(a.getValue());
        }
        if ((a = e.getAttribute(Xml.ACTION_CODE)) != null) {
            setAction(getActionByCode(Integer.decode(a.getValue())));
        }
        if ((a = e.getAttribute(Xml.TRAIN_ID)) != null) {
            _train = TrainManager.instance().getTrainById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.ROUTE_LOCATION_ID)) != null && getTrain() != null) {
            _routeLocation = getTrain().getRoute().getLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.AUTOMATION_ID)) != null) {
            // in the process of loading automations, so we can't get them now, save id and get later.
            _automationId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.HALT_FAIL)) != null) {
            _haltFail = a.getValue().equals(Xml.TRUE);
        }
        Element eMessages = e.getChild(Xml.MESSAGES);
        if (eMessages != null) {
            Element eMessageOk = eMessages.getChild(Xml.MESSAGE_OK);
            if (eMessageOk != null && (a = eMessageOk.getAttribute(Xml.MESSAGE)) != null) {
                _message = a.getValue();
            }
            Element eMessageFail = eMessages.getChild(Xml.MESSAGE_FAIL);
            if (eMessageFail != null && (a = eMessageFail.getAttribute(Xml.MESSAGE)) != null) {
                _messageFail = a.getValue();
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-trains.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element(Xml.ITEM);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.SEQUENCE_ID, Integer.toString(getSequenceId()));
        e.setAttribute(Xml.ACTION_CODE, "0x" + Integer.toHexString(getActionCode()));
        if (getTrain() != null) {
            e.setAttribute(Xml.TRAIN_ID, getTrain().getId());
            if (getRouteLocation() != null) {
                e.setAttribute(Xml.ROUTE_LOCATION_ID, getRouteLocation().getId());
            }
        }
        if (getAutomation() != null) {
            e.setAttribute(Xml.AUTOMATION_ID, getAutomation().getId());
        }
        e.setAttribute(Xml.HALT_FAIL, isHaltFailureEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.ACTION_SUCCESSFUL, isActionSuccessful() ? Xml.TRUE : Xml.FALSE);
        if (!getMessage().equals(NONE) || !getMessageFail().equals(NONE)) {
            Element eMessages = new Element(Xml.MESSAGES);
            e.addContent(eMessages);
            Element eMessageOk = new Element(Xml.MESSAGE_OK);
            eMessageOk.setAttribute(Xml.MESSAGE, getMessage());
            Element eMessageFail = new Element(Xml.MESSAGE_FAIL);
            eMessageFail.setAttribute(Xml.MESSAGE, getMessageFail());
            eMessages.addContent(eMessageOk);
            eMessages.addContent(eMessageFail);
        }
        return e;
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("AutomationItem id ({}) sees property change: ({}) old: ({}) new: ({})",
                    getId(), e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N
        }
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        TrainManagerXml.instance().setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    static Logger log = LoggerFactory.getLogger(AutomationItem.class.getName());

}
