package jmri.jmrit.operations.automation;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
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
    protected boolean _running = false;
    protected boolean _actionRunning = false;

    // stores AutomationItems for this automation
    protected Hashtable<String, AutomationItem> _automationHashTable = new Hashtable<String, AutomationItem>();
    protected int _IdNumber = 0; // each item in a automation gets its own id
    protected int _sequenceNum = 0; // each item has a unique sequence number

    public static final String LISTCHANGE_CHANGED_PROPERTY = "automationListChange"; // NOI18N
    public static final String CURRENT_ITEM_CHANGED_PROPERTY = "automationCurrentItemChange"; // NOI18N
    public static final String RUNNING_CHANGED_PROPERTY = "automationRunningChange"; // NOI18N
    public static final String ACTION_RUNNING_CHANGED_PROPERTY = "automationActionRunningChange"; // NOI18N
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
        return _name;
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
    
    public String getMessage() {
        if (getCurrentAutomationItem() != null) {
            return getCurrentAutomationItem().getMessage();
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
        if (getCurrentAutomationItem().getAction() != null) {
            log.debug("Perform action {}", getCurrentAutomationItem().getAction().toString());
            getCurrentAutomationItem().getAction().removePropertyChangeListener(this);
            getCurrentAutomationItem().getAction().addPropertyChangeListener(this);
            setActionRunning(true);
            getCurrentAutomationItem().getAction().doAction();
        }
    }

    public void stop() {
        log.debug("stop automation ({})", getName());
        setRunning(false);
        setActionRunning(false);
        getCurrentAutomationItem().getAction().removePropertyChangeListener(this);
        getCurrentAutomationItem().getAction().cancelAction();
    }

    public void resume() {
        if (getSize() > 0) {
            log.debug("resume automation ({})", getName());
            setRunning(true);
            step();
        }
    }

    public void setRunning(boolean running) {
        boolean old = _running;
        _running = running;
        if (old != running) {
            setDirtyAndFirePropertyChange(RUNNING_CHANGED_PROPERTY, old, running); // NOI18N
        }
    }

    public boolean isRunning() {
        return _running;
    }
    
    public void setActionRunning(boolean actionRunning) {
        boolean old = _actionRunning;
        _actionRunning = actionRunning;
        if (old != actionRunning) {
            setDirtyAndFirePropertyChange(ACTION_RUNNING_CHANGED_PROPERTY, old, actionRunning); // NOI18N
        }
    }

    public boolean isActionRunning() {
        return _actionRunning;
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
                        setRunning(false);
                    }
                    break;
                }
            }
        } else {
            setCurrentAutomationItem(null);
        }
    }

    public void setCurrentAutomationItem(AutomationItem item) {
        AutomationItem old = _currentAutomationItem;
        _currentAutomationItem = item;
        if (old != item) {
            setDirtyAndFirePropertyChange(CURRENT_ITEM_CHANGED_PROPERTY, old, item); // NOI18N
        }
    }

    public AutomationItem getCurrentAutomationItem() {
        return _currentAutomationItem;
    }

    public void dispose() {
        setDirtyAndFirePropertyChange(DISPOSE, null, DISPOSE);
    }

    public AutomationItem addItem() {
        _IdNumber++;
        _sequenceNum++;
        String id = _id + "c" + Integer.toString(_IdNumber);
        log.debug("Adding new item to ({}) id: {}", getName(), id);
        AutomationItem item = new AutomationItem(id);
        item.setSequenceId(_sequenceNum);
        Integer old = Integer.valueOf(_automationHashTable.size());
        _automationHashTable.put(item.getId(), item);

        if (_currentAutomationItem == null) {
            _currentAutomationItem = item;
        }

        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_automationHashTable.size()));
        // listen for set out and pick up changes to forward
        item.addPropertyChangeListener(this);
        return item;
    }

    /**
     * Add a automation item at a specific place (sequence) in the automation
     * Allowable sequence numbers are 0 to max size of automation. 0 = start of
     * list.
     *
     * @param item
     * @param sequence
     * @return automation item
     */
    public AutomationItem addItem(int sequence) {
        AutomationItem item = addItem();
        if (sequence < 0 || sequence > _automationHashTable.size()) {
            return item;
        }
        for (int i = 0; i < _automationHashTable.size() - sequence - 1; i++) {
            moveItemUp(item);
        }
        return item;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     */
    public void register(AutomationItem item) {
        Integer old = Integer.valueOf(_automationHashTable.size());
        _automationHashTable.put(item.getId(), item);

        // find last id created
        String[] getId = item.getId().split("c");
        int id = Integer.parseInt(getId[1]);
        if (id > _IdNumber) {
            _IdNumber = id;
        }
        // find highest sequence number
        if (item.getSequenceId() > _sequenceNum) {
            _sequenceNum = item.getSequenceId();
        }
        if (getCurrentAutomationItem() == null) {
            setCurrentAutomationItem(item); // default is to load the first item saved.
        }
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_automationHashTable.size()));
        // listen for set out and pick up changes to forward
        item.addPropertyChangeListener(this);
    }

    /**
     * Delete a AutomationItem
     *
     * @param item
     */
    public void deleteItem(AutomationItem item) {
        if (item != null) {
            if (_currentAutomationItem == item) {
                setNextAutomationItem();
            }
            item.removePropertyChangeListener(this);
            String id = item.getId();
            item.dispose();
            Integer old = Integer.valueOf(_automationHashTable.size());
            _automationHashTable.remove(id);
            resequenceIds();
            setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, old, Integer.valueOf(_automationHashTable.size()));
        }
    }

    /**
     * Reorder the item sequence numbers for this automation
     */
    private void resequenceIds() {
        List<AutomationItem> automationItems = getItemsBySequenceList();
        int i;
        for (i = 0; i < automationItems.size(); i++) {
            automationItems.get(i).setSequenceId(i + 1); // start sequence numbers at 1
        }
        _sequenceNum = i;
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
        String[] arr = new String[_automationHashTable.size()];
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
        // first get id list
        List<AutomationItem> sortList = getItemsByIdList();
        // now re-sort
        List<AutomationItem> items = new ArrayList<AutomationItem>();

        for (AutomationItem item : sortList) {
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
            item.setSequenceId(_sequenceNum + 1); // move to the end of the list
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
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
    }

    /**
     * Places a AutomationItem later in the automation
     *
     * @param item
     */
    public void moveItemDown(AutomationItem item) {
        int sequenceId = item.getSequenceId();
        if (sequenceId + 1 > _sequenceNum) {
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
        setDirtyAndFirePropertyChange(LISTCHANGE_CHANGED_PROPERTY, null, Integer.toString(sequenceId));
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
     * with the detailed DTD in operations-config.xml
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
        if ((a = e.getAttribute(Xml.CURRENT_ITEM)) != null) {
            _currentAutomationItem = getItemById(a.getValue());
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-config.xml.
     *
     * @return Contents in a JDOM Element
     */
    public org.jdom2.Element store() {
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
        if (getCurrentAutomationItem().getAction() == e.getSource()) {
            getCurrentAutomationItem().getAction().removePropertyChangeListener(this);
            getCurrentAutomationItem().getAction().cancelAction();
            setActionRunning(false);
            setNextAutomationItem();
            if (isRunning()) {
                step();
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        //        if (Control.showProperty)
        log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                .getNewValue());
        CheckForActionPropertyChange(e);

        // forward all automation item changes
        // setDirtyAndFirePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // set dirty
        TrainManagerXml.instance().setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    static Logger log = LoggerFactory.getLogger(Automation.class.getName());

}
