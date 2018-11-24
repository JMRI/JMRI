package jmri.jmrit.operations.automation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.automation.actions.Action;
import jmri.jmrit.operations.automation.actions.HaltAction;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainManagerXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automation for operations
 *
 * @author Daniel Boudreau Copyright (C) 2016
 */
public class Automation implements java.beans.PropertyChangeListener {

    protected String _id = "";
    protected String _name = "";
    protected String _comment = "";
    protected AutomationItem _currentAutomationItem = null;
    protected AutomationItem _lastAutomationItem = null;
    protected AutomationItem _gotoAutomationItem = null;
    protected boolean _running = false;

    // stores AutomationItems for this automation
    protected HashMap<String, AutomationItem> _automationHashTable = new HashMap<>();
    protected int _IdNumber = 0; // each item in a automation gets its own unique id

    public static final String REGEX = "c"; // NOI18N

    public static final String LISTCHANGE_CHANGED_PROPERTY = "automationListChange"; // NOI18N
    public static final String CURRENT_ITEM_CHANGED_PROPERTY = "automationCurrentItemChange"; // NOI18N
    public static final String RUNNING_CHANGED_PROPERTY = "automationRunningChange"; // NOI18N
    public static final String DISPOSE = "automationDispose"; // NOI18N

    public Automation(String id, String name) {
        log.debug("New automation ({}) id: {}", name, id);
        _name = name;
        _id = id;
    }

    public String getId() {
        return _id;
    }

    public void setName(String name) {
        String old = _name;
        _name = name;
        if (!old.equals(name)) {
            setDirtyAndFirePropertyChange("AutomationName", old, name); // NOI18N
        }
    }

    // for combo boxes
    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return _name;
    }

    public int getSize() {
        return _automationHashTable.size();
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("AutomationComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    public String getCurrentActionString() {
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() != null) {
            return getCurrentAutomationItem().getId() + " " + getCurrentAutomationItem().getAction().getActionString();
        }
        return "";
    }

    public String getActionStatus() {
        if (getCurrentAutomationItem() != null) {
            return getCurrentAutomationItem().getStatus();
        }
        return "";
    }

    public String getMessage() {
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() != null) {
            return getCurrentAutomationItem().getAction().getFormatedMessage(getCurrentAutomationItem().getMessage());
        }
        return "";
    }

    public void setRunning(boolean running) {
        boolean old = _running;
        _running = running;
        if (old != running) {
            firePropertyChange(RUNNING_CHANGED_PROPERTY, old, running); // NOI18N
        }
    }

    public boolean isRunning() {
        return _running;
    }

    public boolean isActionRunning() {
        for (AutomationItem item : getItemsBySequenceList()) {
            if (item.isActionRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to determine if automation is at the start of its sequence.
     *
     * @return true if the current action is the first action in the list.
     */
    public boolean isReadyToRun() {
        return (getSize() > 0 && getCurrentAutomationItem() == getItemsBySequenceList().get(0));
    }

    public void run() {
        if (getSize() > 0) {
            log.debug("run automation ({})", getName());
            _gotoAutomationItem = null;
            setCurrentAutomationItem(getItemsBySequenceList().get(0));
            setRunning(true);
            step();
        }
    }

    public void step() {
        log.debug("step automation ({})", getName());
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() != null) {
            if (getCurrentAutomationItem().getAction().getClass().equals(HaltAction.class)
                    && getCurrentAutomationItem().isActionRan()
                    && getCurrentAutomationItem() != getItemsBySequenceList().get(0)) {
                setNextAutomationItem();
            }
            if (getCurrentAutomationItem() == getItemsBySequenceList().get(0)) {
                resetAutomationItems();
            }
            performAction(getCurrentAutomationItem());
        }
    }

    private void performAction(AutomationItem item) {
        if (item.isActionRunning()) {
            log.debug("Action ({}) item id: {} already running", item.getAction().getName(), item.getId());
        } else {
            log.debug("Perform action ({}) item id: {}", item.getAction().getName(), item.getId());
            item.getAction().removePropertyChangeListener(this);
            item.getAction().addPropertyChangeListener(this);
            Thread runAction = new Thread(() -> {
                item.getAction().doAction();
            });
            runAction.setName("Run action item: " + item.getId()); // NOI18N
            runAction.start();
        }
    }

    public void stop() {
        log.debug("stop automation ({})", getName());
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() != null) {
            setRunning(false);
            cancelActions();
        }
    }

    private void cancelActions() {
        for (AutomationItem item : getItemsBySequenceList()) {
            item.getAction().cancelAction();
            item.getAction().removePropertyChangeListener(this);
        }
    }

    public void resume() {
        if (getSize() > 0) {
            log.debug("resume automation ({})", getName());
            setRunning(true);
            step();
        }
    }

    public void reset() {
        stop();
        if (getSize() > 0) {
            setCurrentAutomationItem(getItemsBySequenceList().get(0));
            resetAutomationItems();
        }
    }

    private void resetAutomationItems() {
        resetAutomationItems(getCurrentAutomationItem());
    }

    private void resetAutomationItems(AutomationItem item) {
        boolean found = false;
        for (AutomationItem automationItem : getItemsBySequenceList()) {
            if (!found && automationItem != item) {
                continue;
            }
            found = true;
            automationItem.reset();
        }
    }

    public void setNextAutomationItem() {
        log.debug("set next automation ({})", getName());
        if (getSize() > 0) {
            // goto?
            if (_gotoAutomationItem != null) {
                getCurrentAutomationItem().setGotoBranched(true);
                setCurrentAutomationItem(_gotoAutomationItem);
                resetAutomationItems(_gotoAutomationItem);
                _gotoAutomationItem = null;
                return; // done with goto
            }
            List<AutomationItem> items = getItemsBySequenceList();
            for (int index = 0; index < items.size(); index++) {
                AutomationItem item = items.get(index);
                if (item == getCurrentAutomationItem()) {
                    if (index + 1 < items.size()) {
                        item = items.get(index + 1);
                        setCurrentAutomationItem(item);
                        if (item.isActionRan()) {
                            continue;
                        }
                    } else {
                        setCurrentAutomationItem(getItemsBySequenceList().get(0));
                        setRunning(false); // reached the end of the list
                    }
                    return; // done
                }
            }
        }
        setCurrentAutomationItem(null);
    }

    /*
     * Returns the next automationItem in the sequence
     */
    private AutomationItem getNextAutomationItem(AutomationItem item) {
        List<AutomationItem> items = getItemsBySequenceList();
        for (int index = 0; index < items.size(); index++) {
            if (item == items.get(index)) {
                if (index + 1 < items.size()) {
                    return items.get(index + 1);
                } else {
                    break;
                }
            }
        }
        return null;
    }

    public void setCurrentAutomationItem(AutomationItem item) {
        _lastAutomationItem = _currentAutomationItem;
        _currentAutomationItem = item;
        if (_lastAutomationItem != item) {
            setDirtyAndFirePropertyChange(CURRENT_ITEM_CHANGED_PROPERTY, _lastAutomationItem, item); // NOI18N
        }
    }

    public AutomationItem getCurrentAutomationItem() {
        return _currentAutomationItem;
    }

    public AutomationItem getLastAutomationItem() {
        return _lastAutomationItem;
    }

    public boolean isLastActionSuccessful() {
        if (getLastAutomationItem() != null) {
            return getLastAutomationItem().isActionSuccessful();
        }
        return false;
    }

    public void dispose() {
        firePropertyChange(DISPOSE, null, DISPOSE);
    }

    public AutomationItem addItem() {
        _IdNumber++;
        String id = getId() + REGEX + Integer.toString(_IdNumber);
        log.debug("Adding new item to ({}) id: {}", getName(), id);
        AutomationItem item = new AutomationItem(id);
        _automationHashTable.put(item.getId(), item);
        item.setSequenceId(getSize());

        if (getCurrentAutomationItem() == null) {
            setCurrentAutomationItem(item);
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, getSize() - 1, getSize());
        return item;
    }

    /**
     * Add a automation item at a specific place (sequence) in the automation
     * Allowable sequence numbers are 0 to max size of automation. 0 = start of
     * list.
     *
     * @param sequence where to add a new item in the automation
     *
     * @return automation item
     */
    public AutomationItem addNewItem(int sequence) {
        AutomationItem item = addItem();
        if (sequence < 0 || sequence > getSize()) {
            return item;
        }
        for (int i = 0; i < getSize() - sequence - 1; i++) {
            moveItemUp(item);
        }
        return item;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param item the item to be added to this automation.
     */
    public void register(AutomationItem item) {
        _automationHashTable.put(item.getId(), item);
        // find last id created
        String[] getId = item.getId().split(Automation.REGEX);
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber) {
            _IdNumber = id;
        }
        if (getCurrentAutomationItem() == null) {
            setCurrentAutomationItem(item); // default is to load the first item saved.
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, getSize() - 1, getSize());
    }

    /**
     * Delete a AutomationItem
     *
     * @param item The item to be deleted.
     *
     */
    public void deleteItem(AutomationItem item) {
        if (item != null) {
            if (item.isActionRunning()) {
                stop();
            }
            if (getCurrentAutomationItem() == item) {
                setNextAutomationItem();
            }
            String id = item.getId();
            item.dispose();
            int old = getSize();
            _automationHashTable.remove(id);
            resequenceIds();
            if (getSize() <= 0) {
                setCurrentAutomationItem(null);
            }
            setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, getSize());
        }
    }

    /**
     * Reorder the item sequence numbers for this automation
     */
    private void resequenceIds() {
        int i = 1; // start sequence numbers at 1
        for (AutomationItem item : getItemsBySequenceList()) {
            item.setSequenceId(i++);
        }
    }

    /**
     * Get a AutomationItem by id
     *
     * @param id The string id of the item.
     *
     * @return automation item
     */
    public AutomationItem getItemById(String id) {
        return _automationHashTable.get(id);
    }

    private List<AutomationItem> getItemsByIdList() {
        List<AutomationItem> out = new ArrayList<>();
        _automationHashTable.keySet().stream().sorted().forEach((id) -> {
            out.add(getItemById(id));
        });
        return out;
    }

    /**
     * Get a list of AutomationItems sorted by automation order
     *
     * @return list of AutomationItems ordered by sequence
     */
    public List<AutomationItem> getItemsBySequenceList() {
        List<AutomationItem> items = new ArrayList<>();
        for (AutomationItem item : getItemsByIdList()) {
            for (int j = 0; j < items.size(); j++) {
                if (item.getSequenceId() < items.get(j).getSequenceId()) {
                    items.add(j, item);
                    break;
                }
            }
            if (!items.contains(item)) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Gets a JComboBox loaded with automation items.
     *
     * @return JComboBox with a list of automation items.
     */
    public JComboBox<AutomationItem> getComboBox() {
        JComboBox<AutomationItem> box = new JComboBox<>();
        for (AutomationItem item : getItemsBySequenceList()) {
            box.addItem(item);
        }
        return box;
    }

    /**
     * Places a AutomationItem earlier in the automation
     *
     * @param item The item to move up one position in the automation.
     *
     */
    public void moveItemUp(AutomationItem item) {
        int sequenceId = item.getSequenceId();
        if (sequenceId - 1 <= 0) {
            item.setSequenceId(getSize() + 1); // move to the end of the list
            resequenceIds();
        } else {
            // adjust the other item taken by this one
            AutomationItem replaceSi = getItemBySequenceId(sequenceId - 1);
            if (replaceSi != null) {
                replaceSi.setSequenceId(sequenceId);
                item.setSequenceId(sequenceId - 1);
            } else {
                resequenceIds(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, sequenceId);
    }

    /**
     * Places a AutomationItem later in the automation.
     *
     * @param item The item to move later in the automation.
     *
     */
    public void moveItemDown(AutomationItem item) {
        int sequenceId = item.getSequenceId();
        if (sequenceId + 1 > getSize()) {
            item.setSequenceId(0); // move to the start of the list
            resequenceIds();
        } else {
            // adjust the other item taken by this one
            AutomationItem replaceSi = getItemBySequenceId(sequenceId + 1);
            if (replaceSi != null) {
                replaceSi.setSequenceId(sequenceId);
                item.setSequenceId(sequenceId + 1);
            } else {
                resequenceIds(); // error the sequence number is missing
            }
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, sequenceId);
    }

    public AutomationItem getItemBySequenceId(int sequenceId) {
        for (AutomationItem item : getItemsByIdList()) {
            if (item.getSequenceId() == sequenceId) {
                return item;
            }
        }
        return null;
    }

    /**
     * Copies automation.
     *
     * @param automation the automation to copy
     */
    public void copyAutomation(Automation automation) {
        if (automation != null) {
            setComment(automation.getComment());
            for (AutomationItem item : automation.getItemsBySequenceList()) {
                addItem().copyItem(item);
            }
            // now adjust GOTOs to reference the new automation
            for (AutomationItem item : getItemsBySequenceList()) {
                if (item.getGotoAutomationItem() != null) {
                    item.setGotoAutomationItem(getItemBySequenceId(item.getGotoAutomationItem().getSequenceId()));
                }
            }
        }
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-trains.dtd
     *
     * @param e Consist XML element
     */
    public Automation(Element e) {
        org.jdom2.Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in automation element when reading operations");
        }
        if ((a = e.getAttribute(Xml.NAME)) != null) {
            _name = a.getValue();
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = a.getValue();
        }
        if (e.getChildren(Xml.ITEM) != null) {
            List<Element> eAutomationItems = e.getChildren(Xml.ITEM);
            log.debug("automation: {} has {} items", getName(), eAutomationItems.size());
            for (Element eAutomationItem : eAutomationItems) {
                register(new AutomationItem(eAutomationItem));
            }
        }
        // get the current item after all of the items above have been loaded
        if ((a = e.getAttribute(Xml.CURRENT_ITEM)) != null) {
            _currentAutomationItem = getItemById(a.getValue());
        }

    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-trains.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new org.jdom2.Element(Xml.AUTOMATION);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.COMMENT, getComment());
        if (getCurrentAutomationItem() != null) {
            e.setAttribute(Xml.CURRENT_ITEM, getCurrentAutomationItem().getId());
        }
        for (AutomationItem item : getItemsBySequenceList()) {
            e.addContent(item.store());
        }
        return e;
    }

    @SuppressFBWarnings(value = {"UW_UNCOND_WAIT", "WA_NOT_IN_LOOP"},
            justification = "Need to pause for user action")
    private void checkForActionPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Action.ACTION_COMPLETE_CHANGED_PROPERTY)
                || evt.getPropertyName().equals(Action.ACTION_HALT_CHANGED_PROPERTY)) {
            Action action = (Action) evt.getSource();
            action.removePropertyChangeListener(this);
        }
        // the following code causes multiple wait actions to run concurrently
        if (evt.getPropertyName().equals(Action.ACTION_RUNNING_CHANGED_PROPERTY)) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            // when new value is true the action is running
            if ((boolean) evt.getNewValue()) {
                Action action = (Action) evt.getSource();
                log.debug("Action ({}) is running", action.getActionString());
                if (action.isConcurrentAction()) {
                    AutomationItem item = action.getAutomationItem();
                    AutomationItem nextItem = getNextAutomationItem(item);
                    if (nextItem != null && nextItem.getAction().isConcurrentAction()) {
                        performAction(nextItem); // start this wait action
                    }
                }
            }
        }
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() == evt.getSource()) {
            if (evt.getPropertyName().equals(Action.ACTION_COMPLETE_CHANGED_PROPERTY)
                    || evt.getPropertyName().equals(Action.ACTION_HALT_CHANGED_PROPERTY)) {
                getCurrentAutomationItem().getAction().cancelAction();
                if (evt.getPropertyName().equals(Action.ACTION_COMPLETE_CHANGED_PROPERTY)) {
                    setNextAutomationItem();
                    if (isRunning()) {
                        step(); // continue running by doing the next action
                    }
                } else if (evt.getPropertyName().equals(Action.ACTION_HALT_CHANGED_PROPERTY)) {
                    if ((boolean) evt.getNewValue() == true) {
                        log.debug("User halted successful action");
                        setNextAutomationItem();
                    }
                    stop();
                }
            }
            if (evt.getPropertyName().equals(Action.ACTION_GOTO_CHANGED_PROPERTY)) {
                // the old property value is used to control branch
                // if old = null, then it is a unconditional branch
                // if old = true, branch if success
                // if old = false, branch if failure
                if (evt.getOldValue() == null || (boolean) evt.getOldValue() == isLastActionSuccessful()) {
                    _gotoAutomationItem = (AutomationItem) evt.getNewValue();
                    // pause thread in case goto is a tight loop
                    // this gives the user a chance to "Stop" the automation
                    synchronized (this) {
                        try {
                            wait(250);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            log.error("Thread interrupeted while waiting", e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        checkForActionPropertyChange(e);
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

    private final static Logger log = LoggerFactory.getLogger(Automation.class);

}
