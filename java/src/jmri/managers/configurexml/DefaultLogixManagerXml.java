package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.managers.DefaultLogixManager;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for configuring LogixManagers.
 *
 * @author Dave Duchamp Copyright (c) 2007
 */
public class DefaultLogixManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultLogixManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a LogixManager.
     *
     * @param o Object to store, of type LogixManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element logixs = new Element("logixs");
        setStoreElementClass(logixs);
        LogixManager lxm = (LogixManager) o;
        if (lxm != null) {
            SortedSet<Logix> logixList = lxm.getNamedBeanSet();
            // don't return an element if there are no Logix to include
            if (logixList.isEmpty()) {
                return null;
            }
            // store the Logix
            for (Logix x : logixList) {
                String xName = x.getSystemName();
                log.debug("Logix system name is {}", xName);  // NOI18N
                boolean enabled = x.getEnabled();

                Element elem = new Element("logix");  // NOI18N
                elem.addContent(new Element("systemName").addContent(xName));  // NOI18N

                // As a work-around for backward compatibility, store systemName and username as attribute.
                // TODO Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                String uName = x.getUserName();
                if ((uName != null) && !uName.isEmpty()) {
                    elem.setAttribute("userName", uName);  // NOI18N
                }

                // store common part
                storeCommon(x, elem);

                if (enabled) {
                    elem.setAttribute("enabled", "yes");  // NOI18N
                } else {
                    elem.setAttribute("enabled", "no");  // NOI18N
                }
                // save child Conditionals
                int numConditionals = x.getNumConditionals();
                if (numConditionals > 0) {
                    String cSysName = "";
                    Element cElem = null;
                    for (int k = 0; k < numConditionals; k++) {
                        cSysName = x.getConditionalByNumberOrder(k);
                        cElem = new Element("logixConditional");  // NOI18N
                        cElem.setAttribute("systemName", cSysName);  // NOI18N
                        cElem.setAttribute("order", Integer.toString(k));  // NOI18N
                        elem.addContent(cElem);
                    }
                }
                logixs.addContent(elem);
            }
        }
        return (logixs);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param logixs The top-level element being created
     */
    public void setStoreElementClass(Element logixs) {
        logixs.setAttribute("class", this.getClass().getName());  // NOI18N
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");  // NOI18N
    }

    /**
     * Create a LogixManager object of the correct class, then register and fill
     * it.
     *
     * @param sharedLogix  Shared top level Element to unpack.
     * @param perNodeLogix Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedLogix, Element perNodeLogix) {
        // create the master object
        replaceLogixManager();
        // load individual sharedLogix
        loadLogixs(sharedLogix);
        return true;
    }

    /**
     * Utility method to load the individual Logix objects. If there's no
     * additional info needed for a specific logix type, invoke this with the
     * parent of the set of Logix elements.
     *
     * @param logixs Element containing the Logix elements to load.
     */
    public void loadLogixs(Element logixs) {
        List<Element> logixList = logixs.getChildren("logix");  // NOI18N
        log.debug("Found {} Logixs", logixList.size());  // NOI18N
        LogixManager lxm = InstanceManager.getDefault(jmri.LogixManager.class);

        for (Element elem : logixList) {
            String sysName = getSystemName(elem);
            if (sysName == null) {
                log.warn("unexpected null in systemName {}", elem);  // NOI18N
                break;
            }

            String userName = getUserName(elem);
            log.debug("create logix: ({})({})", sysName,  // NOI18N
                    (userName == null ? "<null>" : userName));  // NOI18N

            String yesno = "";
            if (elem.getAttribute("enabled") != null) {  // NOI18N
                yesno = elem.getAttribute("enabled").getValue();  // NOI18N
            }

            Logix x = lxm.createNewLogix(sysName, userName);
            if (x != null) {
                // load common part
                loadCommon(x, elem);

                // set enabled/disabled if attribute was present
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {  // NOI18N
                        x.setEnabled(true);
                    } else if (yesno.equals("no")) {  // NOI18N
                        x.setEnabled(false);
                    }
                }
                // load conditionals, if there are any
                List<Element> logixConditionalList = elem.getChildren("logixConditional");  // NOI18N
                // add conditionals
                for (Element lxcond : logixConditionalList) {
                    String cSysName = getAttributeString(lxcond, "systemName");
                    if (cSysName == null) {
                        log.warn("unexpected null in systemName {} {}", // NOI18N
                                lxcond, lxcond.getAttributes());
                        break;
                    }
                    int cOrder = Integer.parseInt(lxcond
                            .getAttribute("order").getValue()); // NOI18N
                    // add the conditional to logix
                    x.addConditional(cSysName, cOrder);
                }
            }
        }
    }

    /**
     * Replace the current LogixManager, if there is one, with one newly created
     * during a load operation. This is skipped if they are of the same absolute
     * type.
     */
    protected void replaceLogixManager() {
        if (InstanceManager.getDefault(jmri.LogixManager.class).getClass().getName()
                .equals(DefaultLogixManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.LogixManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.LogixManager.class));
            }
        }

        // register new one with InstanceManager
        DefaultLogixManager pManager = InstanceManager.getDefault(DefaultLogixManager.class);
        InstanceManager.store(pManager, LogixManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXS);
        }
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.LogixManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultLogixManagerXml.class);

}
