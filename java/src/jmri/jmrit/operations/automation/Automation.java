package jmri.jmrit.operations.automation;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
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
 * @version $Revision$
 */
public class Automation implements java.beans.PropertyChangeListener {

    protected String _id = "";
    protected String _name = "";
    protected String _comment = "";
    protected AutomationItem _currentAutomationItem = null;
    protected AutomationItem _lastAutomationItem = null;
    protected boolean _running = false;

    // stores AutomationItems for this automation
    protected Hashtable<String, AutomationItem> _automationHashTable = new Hashtable<String, AutomationItem>();
    protected int _IdNumber = 0; // each item in a automation gets its own unique id

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

    public String getLastActionResults() {
        if (getLastAutomationItem() != null) {
            return getLastAutomationItem().getStatus();
        }
        return "";
    }

    public String getMessage() {
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() != null) {
            return getCurrentAutomationItem().getAction().getFormatedMessage(getCurrentAutomationItem().getMessage());
        }
        return "";
    }

    public void run() {
        if (getSize() > 0) {
            log.debug("run automation ({})", getName());
            setCurrentAutomationItem(getItemsBySequenceList().get(0));
            setRunning(true);
            step();
        }
    }

    public void step() {
        log.debug("step automation ({})", getName());
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() != null) {
            log.debug("Perform action ({})", getCurrentAutomationItem().getAction().getName());
            getCurrentAutomationItem().getAction().removePropertyChangeListener(this);
            getCurrentAutomationItem().getAction().addPropertyChangeListener(this);
            getCurrentAutomationItem().getAction().doAction();
        }
    }

    public void stop() {
        log.debug("stop automation ({})", getName());
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() != null) {
            setRunning(false);
            getCurrentAutomationItem().getAction().cancelAction();
            getCurrentAutomationItem().getAction().removePropertyChangeListener(this);
            if (getCurrentAutomationItem().getAction().getClass().equals(HaltAction.class)) {
                setNextAutomationItem();
            }
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
        }
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
        if (getCurrentAutomationItem() != null) {
            return getCurrentAutomationItem().isActionRunning();
        }
        return false;
    }

    public void setNextAutomationItem() {
        log.debug("set next automation ({})", getName());
        if (getSize() > 0) {
            List<AutomationItem> items = getItemsBySequenceList();
            for (int index = 0; index < items.size(); index++) {
                AutomationItem item = items.get(index);
                if (item == getCurrentAutomationItem()) {
                    if (index + 1 < items.size()) {
                        setCurrentAutomationItem(items.get(index + 1));
                    } else {
                        setCurrentAutomationItem(getItemsBySequenceList().get(0));
                        setRunning(false); // reached the end of the list
                    }
                    return;
                }
            }
        }
        setCurrentAutomationItem(null);
    }

    public void setCurrentAutomationItem(AutomationItem item) {
        _lastAutomationItem = _currentAutomationItem;
        _currentAutomationItem = item;
        if (_lastAutomationItem != item) {
            setDirtyAndFirePropertyChange(CURRENT_ITEM_CHANGED_PROPERTY, _lastAutomationItem, item); // NOI18N
        }
    }

    public AutomationItem getLastAutomationItem() {
        return _lastAutomationItem;
    }

    public AutomationItem getCurrentAutomationItem() {
        return _currentAutomationItem;
    }

    public void dispose() {
        firePropertyChange(DISPOSE, null, DISPOSE);
    }

    public AutomationItem addItem() {
        _IdNumber++;
        String id = getId() + "c" + Integer.toString(_IdNumber);
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
     * @param sequence
     * @return automation item
     */
    public AutomationItem addItem(int sequence) {
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
     */
    public void register(AutomationItem item) {
        _automationHashTable.put(item.getId(), item);
        // find last id created
        String[] getId = item.getId().split("c");
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
     * @param item
     */
    public void deleteItem(AutomationItem item) {
        if (item != null) {
            if (getCurrentAutomationItem() == item) {
                stop();
            }
            String id = item.getId();
            item.dispose();
            int old = getSize();
            _automationHashTable.remove(id);
            resequenceIds();
            if (getCurrentAutomationItem() == item) {
                setNextAutomationItem();
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
     * @param id
     * @return automation item
     */
    public AutomationItem getItemById(String id) {
        return _automationHashTable.get(id);
    }

    private List<AutomationItem> getItemsByIdList() {
        String[] arr = new String[getSize()];
        List<AutomationItem> out = new ArrayList<AutomationItem>();
        Enumeration<String> en = _automationHashTable.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i++] = en.nextElement();
        }
        jmri.util.StringUtil.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(getItemById(arr[i]));
        }
        return out;
    }

    /**
     * Get a list of AutomationItems sorted by automation order
     *
     * @return list of AutomationItems ordered by sequence
     */
    public List<AutomationItem> getItemsBySequenceList() {
        List<AutomationItem> items = new ArrayList<AutomationItem>();
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
     * Places a AutomationItem earlier in the automation
     *
     * @param item
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
     * Places a AutomationItem later in the automation
     *
     * @param item
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
            @SuppressWarnings("unchecked")
            List<Element> eAutomationItems = e.getChildren(Xml.ITEM);
            if (log.isDebugEnabled()) {
                log.debug("automation: {} has {} items", getName(), eAutomationItems.size());
            }
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

    private void CheckForActionPropertyChange(PropertyChangeEvent e) {
        if (getCurrentAutomationItem() != null && getCurrentAutomationItem().getAction() == e.getSource()) {
            if (e.getPropertyName().equals(Action.ACTION_COMPLETE_CHANGED_PROPERTY) ||
                    e.getPropertyName().equals(Action.ACTION_HALT_CHANGED_PROPERTY)) {
                getCurrentAutomationItem().getAction().removePropertyChangeListener(this);
                getCurrentAutomationItem().getAction().cancelAction();
                if (e.getPropertyName().equals(Action.ACTION_COMPLETE_CHANGED_PROPERTY)) {
                    setNextAutomationItem();
                    if (isRunning()) {
                        step();
                    }
                } else if (e.getPropertyName().equals(Action.ACTION_HALT_CHANGED_PROPERTY)) {
                    stop();
                }
            }
            if (e.getPropertyName().equals(Action.ACTION_RUNNING_CHANGED_PROPERTY)) {
                firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (Control.showProperty)
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        CheckForActionPropertyChange(e);
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

    static Logger log = LoggerFactory.getLogger(Automation.class.getName());

}
