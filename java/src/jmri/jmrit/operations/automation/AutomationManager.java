package jmri.jmrit.operations.automation;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainManagerXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages automations.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2016
 */
public class AutomationManager implements InstanceManagerAutoDefault, PropertyChangeListener {

    public static final String LISTLENGTH_CHANGED_PROPERTY = "automationListLength"; // NOI18N
    private int _id = 0; // retain highest automation Id seen to ensure no Id collisions

    public AutomationManager() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized AutomationManager instance() {
        return InstanceManager.getDefault(AutomationManager.class);
    }

    // stores known Automation instances by id
    protected Hashtable<String, Automation> _automationHashTable = new Hashtable<>();

    /**
     * @return Number of automations
     */
    public int getSize() {
        return _automationHashTable.size();
    }

    /**
     * @param name The string name of the automation to be returned.
     * @return requested Automation object or null if none exists
     */
    public Automation getAutomationByName(String name) {
        Automation automation;
        Enumeration<Automation> en = _automationHashTable.elements();
        while (en.hasMoreElements()) {
            automation = en.nextElement();
            if (automation.getName().equals(name)) {
                return automation;
            }
        }
        return null;
    }

    public Automation getAutomationById(String id) {
        return _automationHashTable.get(id);
    }

    /**
     * Finds an existing automation or creates a new automation if needed
     * requires automation's name creates a unique id for this automation
     *
     * @param name The string name of the automation.
     *
     *
     * @return new automation or existing automation
     */
    public Automation newAutomation(String name) {
        Automation automation = getAutomationByName(name);
        if (automation == null) {
            _id++;
            automation = new Automation(Integer.toString(_id), name);
            Integer oldSize = Integer.valueOf(_automationHashTable.size());
            _automationHashTable.put(automation.getId(), automation);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_automationHashTable
                    .size()));
        }
        return automation;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param automation The automation that is being registered.
     */
    public void register(Automation automation) {
        Integer oldSize = Integer.valueOf(_automationHashTable.size());
        _automationHashTable.put(automation.getId(), automation);
        // find last id created
        int id = Integer.parseInt(automation.getId());
        if (id > _id) {
            _id = id;
        }
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                Integer.valueOf(_automationHashTable.size()));
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     *
     * @param automation The automation to be deleted.
     */
    public void deregister(Automation automation) {
        if (automation == null) {
            return;
        }
        automation.dispose();
        Integer oldSize = Integer.valueOf(_automationHashTable.size());
        _automationHashTable.remove(automation.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                Integer.valueOf(_automationHashTable.size()));
    }

    /**
     * Sort by automation name
     *
     * @return list of automations ordered by name
     */
    public List<Automation> getAutomationsByNameList() {
        List<Automation> sortList = getList();
        // now re-sort
        List<Automation> out = new ArrayList<>();
        for (Automation automation : sortList) {
            for (int j = 0; j < out.size(); j++) {
                if (automation.getName().compareToIgnoreCase(out.get(j).getName()) < 0) {
                    out.add(j, automation);
                    break;
                }
            }
            if (!out.contains(automation)) {
                out.add(automation);
            }
        }
        return out;

    }

    /**
     * Sort by automation id number
     *
     * @return list of automations ordered by id number
     */
    public List<Automation> getAutomationsByIdList() {
        List<Automation> sortList = getList();
        // now re-sort
        List<Automation> out = new ArrayList<>();
        for (Automation automation : sortList) {
            for (int j = 0; j < out.size(); j++) {
                try {
                    if (Integer.parseInt(automation.getId()) < Integer.parseInt(out.get(j).getId())) {
                        out.add(j, automation);
                        break;
                    }
                } catch (NumberFormatException e) {
                    log.debug("list id number isn't a number");
                }
            }
            if (!out.contains(automation)) {
                out.add(automation);
            }
        }
        return out;
    }

    private List<Automation> getList() {
        List<Automation> out = new ArrayList<>();
        Enumeration<Automation> en = _automationHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    /**
     * Gets a JComboBox loaded with automations.
     *
     * @return JComboBox with a list of automations.
     */
    public JComboBox<Automation> getComboBox() {
        JComboBox<Automation> box = new JComboBox<>();
        updateComboBox(box);
        return box;
    }

    /**
     * Update a JComboBox with the latest automations.
     *
     * @param box the JComboBox needing an update.
     */
    public void updateComboBox(JComboBox<Automation> box) {
        box.removeAllItems();
        box.addItem(null);
        for (Automation automation : getAutomationsByNameList()) {
            box.addItem(automation);
        }
    }
    
    /**
     * Restarts all automations that were running when the operations program
     * was last saved.
     */
    public void resumeAutomations() {
        for (Automation automation : getAutomationsByNameList()) {
            if (!automation.isActionRunning() && !automation.isReadyToRun()) {
                automation.resume();
            }
        }
    }

    /**
     * Makes a new copy of automation
     *
     * @param automation the automation to copy
     * @param newName    name for the copy of automation
     * @return new copy of automation
     */
    public Automation copyAutomation(Automation automation, String newName) {
        Automation newAutomation = newAutomation(newName);
        newAutomation.copyAutomation(automation);
        return newAutomation;
    }
    
    public void dispose() {
        _automationHashTable.clear();
        _id = 0;
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-trains.dtd
     *
     * @param root Consist XML element
     */
    public void load(Element root) {
        if (root.getChild(Xml.AUTOMATIONS) != null) {
            List<Element> eAutomations = root.getChild(Xml.AUTOMATIONS).getChildren(Xml.AUTOMATION);
            log.debug("readFile sees {} automations", eAutomations.size());
            for (Element eAutomation : eAutomations) {
                register(new Automation(eAutomation));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-trains.dtd.
     *
     * @param root Contents in a JDOM Element
     */
    public void store(Element root) {
        Element values;
        root.addContent(values = new Element(Xml.AUTOMATIONS));
        // add entries
        for (Automation automation : getAutomationsByNameList()) {
            values.addContent(automation.store());
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
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
        InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(AutomationManager.class);

}
