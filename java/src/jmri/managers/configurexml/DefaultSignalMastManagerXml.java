package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import jmri.configurexml.XmlAdapter;
import jmri.implementation.SignalMastRepeater;
import jmri.managers.DefaultSignalMastManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 */
public class DefaultSignalMastManagerXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultSignalMastManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager.
     *
     * @param o Object to store
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element signalmasts = new Element("signalmasts");
        signalmasts.setAttribute("class", this.getClass().getName());
        DefaultSignalMastManager smm = (DefaultSignalMastManager) o;
        if (smm != null) {
            SortedSet<SignalMast> smList = smm.getNamedBeanSet();
            // don't return an element if there are no SignalMasts to include
            if (smList.isEmpty()) {
                return null;
            }
            // include contents
            for (SignalMast sm : smList) {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sm);
                if (e != null) {
                    signalmasts.addContent(e);
                }
            }
            List<SignalMastRepeater> repeaterList = smm.getRepeaterList();
            if (repeaterList.size() > 0) {
                for (SignalMastRepeater smr : repeaterList) {
                    Element e = new Element("signalmastrepeater");
                    e.addContent(new Element("masterMast").addContent(smr.getMasterMastName()));
                    e.addContent(new Element("slaveMast").addContent(smr.getSlaveMastName()));
                    e.addContent(new Element("enabled").addContent(smr.getEnabled() ? "true" : "false"));
                    switch (smr.getDirection()) {
                        case 1:
                            e.addContent(new Element("update").addContent("MasterToSlave"));
                            break;
                        case 2:
                            e.addContent(new Element("update").addContent("SlaveToMaster"));
                            break;
                        default:
                            e.addContent(new Element("update").addContent("BothWay"));
                            break;
                    }
                    signalmasts.addContent(e);
                }
            }
        }
        return signalmasts;
    }

    /**
     * Create a DefaultSignalMastManager.
     *
     * @param shared Top level Element to unpack.
     * @param perNode Top level Element that is per-node.
     * @return true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        // loop over contained signalmast elements
        List<Element> mastList = shared.getChildren("signalmast");
        boolean result = true;

        for (Element e : mastList) {
            if (e.getAttribute("class") == null) {
                SignalMast m;
                String sys = getSystemName(e);
                try {
                    m = InstanceManager.getDefault(jmri.SignalMastManager.class)
                            .provideSignalMast(sys);

                    if (getUserName(e) != null) {
                        m.setUserName(getUserName(e));
                    }

                    loadCommon(m, e);
                } catch (IllegalArgumentException ex) {
                    log.warn("Failed to provide SignalMast \"{}\" in load", sys);
                }
            } else {
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via {}", adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).getDeclaredConstructor().newInstance();
                    // and do it
                    adapter.load(e, null);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException 
                            | IllegalAccessException | java.lang.reflect.InvocationTargetException
                            | jmri.configurexml.JmriConfigureXmlException ex) {
                    log.error("Exception while loading {}: {}", e.getName(), ex, ex);
                }
            }
        }

        loadSignalMastClass(shared, "turnoutsignalmast");
        loadSignalMastClass(shared, "virtualsignalmast");
        loadSignalMastClass(shared, "matrixsignalmast");
        loadSignalMastClass(shared, "dccsignalmast");
        loadSignalMastClass(shared, "olcbsignalmast");

        mastList = shared.getChildren("signalmastrepeater");
        if (mastList != null) {
            DefaultSignalMastManager m = (DefaultSignalMastManager) InstanceManager.getDefault(jmri.SignalMastManager.class);
            for (Element e : mastList) {
                String masterName = e.getChild("masterMast").getText();
                String slaveName = e.getChild("slaveMast").getText();
                SignalMast masterMast = m.getSignalMast(masterName);
                if (masterMast == null) {
                    log.error("Unable to add mast repeater {}: {}. Master mast must exist.", masterName, slaveName);
                    result = false;
                    continue;
                }
                SignalMast slaveMast = m.getSignalMast(slaveName);
                if (slaveMast == null) {
                    log.error("Unable to add mast repeater {}: {}. Slave mast must exist.", masterName, slaveName);
                    result = false;
                    continue;
                }

                SignalMastRepeater smr = null;
                try {
                    smr = m.provideRepeater(masterMast, slaveMast);
                } catch (JmriException e1) {
                    log.error("Unable to add mast repeater {}: {}. {}", masterName, slaveName, e1);
                    result = false;
                    continue;
                }
                if (e.getChild("enabled") != null && e.getChild("enabled").getText().equals("false")) {
                    smr.setEnabled(false);
                }
                if (e.getChild("update") != null) {
                    if (e.getChild("update").getText().equals("MasterToSlave")) {
                        smr.setDirection(SignalMastRepeater.MASTERTOSLAVE);
                    } else if (e.getChild("update").getText().equals("SlaveToMaster")) {
                        smr.setDirection(SignalMastRepeater.SLAVETOMASTER);
                    }
                }
            }
            m.initialiseRepeaters();
        }
        return result;
    }

    private void loadSignalMastClass(Element shared, String signalMastClass) {
        List<Element> mastClassList = shared.getChildren(signalMastClass);
        log.debug("Found {} signal masts", mastClassList.size());
        // load the contents
        boolean result = loadInAdapter(mastClassList, null);
        if (!result) {
            log.warn("error loading signalmasts");
        }
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.SignalMastManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastManagerXml.class);

}
