package jmri.jmrit.operations.automation;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.automation.actions.Action;
import jmri.jmrit.operations.automation.actions.ActionCodes;
import jmri.jmrit.operations.automation.actions.ActivateTrainScheduleAction;
import jmri.jmrit.operations.automation.actions.ApplyTrainScheduleAction;
import jmri.jmrit.operations.automation.actions.BuildTrainAction;
import jmri.jmrit.operations.automation.actions.BuildTrainIfSelectedAction;
import jmri.jmrit.operations.automation.actions.DeselectTrainAction;
import jmri.jmrit.operations.automation.actions.GotoAction;
import jmri.jmrit.operations.automation.actions.GotoFailureAction;
import jmri.jmrit.operations.automation.actions.GotoSuccessAction;
import jmri.jmrit.operations.automation.actions.HaltAction;
import jmri.jmrit.operations.automation.actions.IsTrainEnRouteAction;
import jmri.jmrit.operations.automation.actions.MessageYesNoAction;
import jmri.jmrit.operations.automation.actions.MoveTrainAction;
import jmri.jmrit.operations.automation.actions.NoAction;
import jmri.jmrit.operations.automation.actions.PrintSwitchListAction;
import jmri.jmrit.operations.automation.actions.PrintTrainManifestAction;
import jmri.jmrit.operations.automation.actions.PrintTrainManifestIfSelectedAction;
import jmri.jmrit.operations.automation.actions.ResetTrainAction;
import jmri.jmrit.operations.automation.actions.ResumeAutomationAction;
import jmri.jmrit.operations.automation.actions.RunAutomationAction;
import jmri.jmrit.operations.automation.actions.RunSwitchListAction;
import jmri.jmrit.operations.automation.actions.RunSwitchListChangesAction;
import jmri.jmrit.operations.automation.actions.RunTrainAction;
import jmri.jmrit.operations.automation.actions.SelectTrainAction;
import jmri.jmrit.operations.automation.actions.StopAutomationAction;
import jmri.jmrit.operations.automation.actions.TerminateTrainAction;
import jmri.jmrit.operations.automation.actions.UpdateSwitchListAction;
import jmri.jmrit.operations.automation.actions.WaitSwitchListAction;
import jmri.jmrit.operations.automation.actions.WaitTrainAction;
import jmri.jmrit.operations.automation.actions.WaitTrainTerminatedAction;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents one automation item of a automation
 *
 * @author Daniel Boudreau Copyright (C) 2016
 */
public class AutomationItem implements java.beans.PropertyChangeListener {

    public static final String NONE = ""; // NOI18N

    protected String _id = NONE;
    protected int _sequenceId = 0; // used to determine order in automation
    
    protected boolean _actionRunning = false; // when true action is running, for example waiting for a train
    protected boolean _actionSuccessful = false;
    protected boolean _actionRan = false;
    protected boolean _haltFail = true;
    
    protected Action _action = null;
    protected String _message = NONE;
    protected String _messageFail = NONE;
    
    // the following are associated with actions
    protected Train _train = null;
    protected RouteLocation _routeLocation = null;
    protected String _automationIdToRun = NONE;
    protected String _gotoAutomationItemId = NONE; // the goto automationItem
    protected boolean _gotoAutomationBranched = false;
    protected String _trainScheduleId = NONE;

    public static final String DISPOSE = "automationItemDispose"; // NOI18N

    public AutomationItem(String id) {
        log.debug("New automation item id: {}", id);
        _id = id;
        setAction(new NoAction()); // the default
    }

    public String getId() {
        return _id;
    }

    @Override
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
        if (old != null) {
            old.cancelAction();
        }
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

    public String getActionName() {
        if (getAction() != null) {
            return getAction().getName();
        }
        return NONE;
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
            setRouteLocation(null);
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

    public void setOther(Object other) {
        if (other != null && other.getClass().equals(Automation.class)) {
            setAutomationToRun((Automation) other);
        }
        else if (other != null && other.getClass().equals(AutomationItem.class)) {
            setGotoAutomationItem((AutomationItem) other);
        }
        else if (other == null || other.getClass().equals(TrainSchedule.class)) {
            setTrainSchedule((TrainSchedule) other);
        }
    }

    /**
     * The automation for actions, not the automation associated with this item.
     * @param automation the automation to run
     *
     */
    public void setAutomationToRun(Automation automation) {
        Automation old = InstanceManager.getDefault(AutomationManager.class).getAutomationById(_automationIdToRun);
        if (automation != null)
            _automationIdToRun = automation.getId();
        else
            _automationIdToRun = NONE;
        if (old != automation) {
            setDirtyAndFirePropertyChange("AutomationItemAutomationChange", old, automation); // NOI18N
        }
    }

    /**
     * The automation for actions, not the automation associated with this item.
     * 
     * @return Automation for this action
     */
    public Automation getAutomationToRun() {
        if (getAction() != null && getAction().isAutomationMenuEnabled()) {
            return InstanceManager.getDefault(AutomationManager.class).getAutomationById(_automationIdToRun);
        }
        return null;
    }

    /**
     * The automation for action GOTO, not this automation item.
     * @param automationItem which automation item to GOTO
     *
     */
    public void setGotoAutomationItem(AutomationItem automationItem) {
        AutomationItem oldItem = null;
        if (automationItem != null) {
            Automation automation = InstanceManager.getDefault(AutomationManager.class).getAutomationById(automationItem.getId().split(Automation.REGEX)[0]);
            oldItem = automation.getItemById(_gotoAutomationItemId);
            _gotoAutomationItemId = automationItem.getId();
        } else {
            _gotoAutomationItemId = NONE;
        }
        if (oldItem != automationItem) {
            setDirtyAndFirePropertyChange("AutomationItemAutomationChange", oldItem, automationItem); // NOI18N
        }
    }

    /**
     * The automationItem for actions not this item.
     * 
     * @return AutomationItem for GOTO
     */
    public AutomationItem getGotoAutomationItem() {
        if (getAction() != null && getAction().isGotoMenuEnabled()) {
            Automation automation = InstanceManager.getDefault(AutomationManager.class).getAutomationById(_gotoAutomationItemId.split(Automation.REGEX)[0]);
            if (automation != null) {
                return automation.getItemById(_gotoAutomationItemId);
            }
        }
        return null;
    }
    
    public void setGotoBranched(boolean branched) {
        _gotoAutomationBranched = branched;
    }
    
    public boolean isGotoBranched() {
        return _gotoAutomationBranched;
    }

    public void setTrainSchedule(TrainSchedule trainSchedule) {
        String old = _trainScheduleId;
        if (trainSchedule != null) {
            _trainScheduleId = trainSchedule.getId();
        } else {
            _trainScheduleId = NONE;
        }
        if (!old.equals(_trainScheduleId)) {
            setDirtyAndFirePropertyChange("AutomationItemTrainScheduleChange", old, _trainScheduleId); // NOI18N
        }
    }
    
    public TrainSchedule getTrainSchedule() {
        if (getAction() != null && getAction().isOtherMenuEnabled()) {
            return InstanceManager.getDefault(TrainScheduleManager.class).getScheduleById(_trainScheduleId);
        }
        return null;
    }
    
    public String getTrainScheduleId() {
        return _trainScheduleId;
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
            if (!actionRunning) {
                setActionRan(true);
            }
            firePropertyChange("actionRunningChange", old, actionRunning); // NOI18N
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

    public void setActionRan(boolean ran) {
        _actionRan = ran;
        firePropertyChange("actionRan", !ran, ran); // NOI18N
    }

    public boolean isActionRan() {
        return _actionRan;
    }

    public boolean isActionSuccessful() {
        return _actionSuccessful;
    }

    public String getStatus() {
        if (isActionRunning())
            return Bundle.getMessage("Running");
        if (!isActionRan())
            return NONE;
        if (getAction() != null)
            return isActionSuccessful() ? getAction().getActionSuccessfulString() : getAction().getActionFailedString();
        else
            return "unknown"; // NOI18N
    }
    
    public void reset() {
        setActionRan(false);
        setActionSuccessful(false);
        setGotoBranched(false);
    }

    /**
     * Copies item.
     * @param item The item to copy.
     */
    public void copyItem(AutomationItem item) {
        setAction(getActionByCode(item.getActionCode())); // must create a new action for each item
        setAutomationToRun(item.getAutomationToRun());
        setGotoAutomationItem(item.getGotoAutomationItem()); //needs an adjustment to work properly
        setTrain(item.getTrain()); // must set train before route location
        setRouteLocation(item.getRouteLocation());
        setSequenceId(item.getSequenceId());
        setTrainSchedule(item.getTrainSchedule());
        setMessage(item.getMessage());
        setMessageFail(item.getMessageFail());
        setHaltFailureEnabled(item.isHaltFailureEnabled());
    }
    
    public static Action getActionByCode(int code) {
        for (Action action : getActionList()) {
            if (action.getCode() == code)
                return action;
        }
        return new NoAction(); // default if code not found
    }

    /**
     * Gets a list of all known automation actions
     * 
     * @return list of automation actions
     */
    public static List<Action> getActionList() {
        List<Action> list = new ArrayList<>();
        list.add(new NoAction());
        list.add(new BuildTrainAction());
        list.add(new BuildTrainIfSelectedAction());
        list.add(new PrintTrainManifestAction());
        list.add(new PrintTrainManifestIfSelectedAction());
        list.add(new RunTrainAction());
        list.add(new MoveTrainAction());
        list.add(new TerminateTrainAction());
        list.add(new ResetTrainAction());
        list.add(new IsTrainEnRouteAction());
        list.add(new WaitTrainAction());
        list.add(new WaitTrainTerminatedAction());
        list.add(new ActivateTrainScheduleAction());
        list.add(new ApplyTrainScheduleAction());
        list.add(new SelectTrainAction());
        list.add(new DeselectTrainAction());
        list.add(new PrintSwitchListAction());
//        list.add(new PrintSwitchListChangesAction()); // see UpdateSwitchListAction
        list.add(new UpdateSwitchListAction());
        list.add(new WaitSwitchListAction());
        list.add(new RunSwitchListAction());
        list.add(new RunSwitchListChangesAction());
        list.add(new RunAutomationAction());
        list.add(new ResumeAutomationAction());
        list.add(new StopAutomationAction());
        list.add(new MessageYesNoAction());
        list.add(new GotoAction());
        list.add(new GotoSuccessAction());
        list.add(new GotoFailureAction());
        list.add(new HaltAction());
        return list;
    }

    public static JComboBox<Action> getActionComboBox() {
        JComboBox<Action> box = new JComboBox<>();
        for (Action action : getActionList())
            box.addItem(action);
        return box;
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
        if ((a = e.getAttribute(Xml.HALT_FAIL)) != null) {
            _haltFail = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.ACTION_RAN)) != null) {
            _actionRan = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.ACTION_SUCCESSFUL)) != null) {
            _actionSuccessful = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.TRAIN_ID)) != null) {
            _train = InstanceManager.getDefault(TrainManager.class).getTrainById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.ROUTE_LOCATION_ID)) != null && getTrain() != null) {
            _routeLocation = getTrain().getRoute().getLocationById(a.getValue());
        }
        if ((a = e.getAttribute(Xml.AUTOMATION_ID)) != null) {
            // in the process of loading automations, so we can't get them now, save id and get later.
            _automationIdToRun = a.getValue();
        }
        if ((a = e.getAttribute(Xml.GOTO_AUTOMATION_ID)) != null) {
            // in the process of loading automations, so we can't get them now, save id and get later.
            _gotoAutomationItemId = a.getValue();
        }
        if ((a = e.getAttribute(Xml.GOTO_AUTOMATION_BRANCHED)) != null) {
            _gotoAutomationBranched = a.getValue().equals(Xml.TRUE);
        }
        if ((a = e.getAttribute(Xml.TRAIN_SCHEDULE_ID)) != null) {
            _trainScheduleId = a.getValue();
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
        e.setAttribute(Xml.NAME, getActionName());
        e.setAttribute(Xml.ACTION_CODE, "0x" + Integer.toHexString(getActionCode())); // NOI18N
        e.setAttribute(Xml.HALT_FAIL, isHaltFailureEnabled() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.ACTION_RAN, isActionRan() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.ACTION_SUCCESSFUL, isActionSuccessful() ? Xml.TRUE : Xml.FALSE);
        if (getTrain() != null) {
            e.setAttribute(Xml.TRAIN_ID, getTrain().getId());
            if (getRouteLocation() != null) {
                e.setAttribute(Xml.ROUTE_LOCATION_ID, getRouteLocation().getId());
            }
        }
        if (getAutomationToRun() != null) {
            e.setAttribute(Xml.AUTOMATION_ID, getAutomationToRun().getId());
        }
        if (getGotoAutomationItem() != null) {
            e.setAttribute(Xml.GOTO_AUTOMATION_ID, getGotoAutomationItem().getId());
            e.setAttribute(Xml.GOTO_AUTOMATION_BRANCHED, isGotoBranched() ? Xml.TRUE : Xml.FALSE);
        }
        if (getTrainSchedule() != null) {
            e.setAttribute(Xml.TRAIN_SCHEDULE_ID, getTrainSchedule().getId());
        }
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

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
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
        InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(AutomationItem.class);

}
