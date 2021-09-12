package jmri.jmrit.operations.rollingstock.engines;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockGroupManager;

/**
 * Manages Consists.
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class ConsistManager extends RollingStockGroupManager implements InstanceManagerAutoDefault {

    public ConsistManager() {
    }

    /**
     * Create a new Consist
     *
     * @param name string name for this Consist
     *
     * @return Consist
     */
    public Consist newConsist(String name) {
        Consist consist = getConsistByName(name);
        if (consist == null && !name.equals(NONE)) {
            consist = new Consist(name);
            int oldSize = _groupHashTable.size();
            _groupHashTable.put(name, consist);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _groupHashTable
                    .size());
        }
        return consist;
    }

    /**
     * Delete a Consist by name
     *
     * @param name string name for the Consist
     *
     */
    public void deleteConsist(String name) {
        Consist consist = getConsistByName(name);
        if (consist != null) {
            consist.dispose();
            Integer oldSize = Integer.valueOf(_groupHashTable.size());
            _groupHashTable.remove(name);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_groupHashTable
                    .size()));
        }
    }

    /**
     * Get a Consist by name
     *
     * @param name string name for the Consist
     *
     * @return named Consist
     */
    public Consist getConsistByName(String name) {
        return (Consist) _groupHashTable.get(name);
    }

    public void replaceConsistName(String oldName, String newName) {
        Consist oldConsist = getConsistByName(oldName);
        if (oldConsist != null) {
            Consist newConsist = newConsist(newName);
            // keep the lead engine
            Engine leadEngine = oldConsist.getLead();
            if (leadEngine != null) {
                leadEngine.setConsist(newConsist);
            }
            for (Engine engine : oldConsist.getEngines()) {
                engine.setConsist(newConsist);
            }
        }
    }
 
    public void load(Element root) {
        // new format using elements starting version 3.3.1
        if (root.getChild(Xml.NEW_CONSISTS) != null) {
            List<Element> eConsists = root.getChild(Xml.NEW_CONSISTS).getChildren(Xml.CONSIST);
            log.debug("Consist manager sees {} consists", eConsists.size());
            Attribute a;
            for (Element eConsist : eConsists) {
                if ((a = eConsist.getAttribute(Xml.NAME)) != null) {
                    newConsist(a.getValue());
                }
            }
        } // old format
        else if (root.getChild(Xml.CONSISTS) != null) {
            String names = root.getChildText(Xml.CONSISTS);
            if (!names.isEmpty()) {
                String[] consistNames = names.split("%%"); // NOI18N
                log.debug("consists: {}", names);
                for (String name : consistNames) {
                    newConsist(name);
                }
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-engines.dtd.
     *
     * @param root The common Element for operations-engines.dtd.
     */
    public void store(Element root) {
        List<String> names = getNameList();
        Element consists = new Element(Xml.NEW_CONSISTS);
        for (String name : names) {
            Element consist = new Element(Xml.CONSIST);
            consist.setAttribute(new Attribute(Xml.NAME, name));
            consists.addContent(consist);
        }
        root.addContent(consists);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(EngineManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(ConsistManager.class);
}
