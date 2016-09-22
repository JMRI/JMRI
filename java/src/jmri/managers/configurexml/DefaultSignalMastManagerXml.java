package jmri.managers.configurexml;

import java.util.List;
import jmri.InstanceManager;
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
     * DefaultSignalMastManager
     *
     * @param o Object to store
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DefaultSignalMastManager m = (DefaultSignalMastManager) o;

        Element element = new Element("signalmasts");
        element.setAttribute("class", this.getClass().getName());
        if (m != null) {
            // include contents
            List<String> names = m.getSystemNameList();
            for (int i = 0; i < names.size(); i++) {
                //Element e = new Element("signalmast");
                SignalMast p = m.getSignalMast(names.get(i));
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(p);
                    if (e != null) {
                        element.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing signalmast: " + e);
                    e.printStackTrace();
                }

            }
            List<SignalMastRepeater> repeaterList = m.getRepeaterList();
            if (repeaterList.size() > 0) {
                //Element repeatElem= new Element("signalmastrepeaters");
                for (SignalMastRepeater smr : repeaterList) {
                    if (smr.getMasterMast() != null && smr.getSlaveMast() != null) {
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
                        element.addContent(e);
                    }
                }
                //element.add(repeatElem);
            }
        }
        return element;
    }

    /**
     * Create a DefaultSignalMastManager
     *
     * @param shared Top level Element to unpack.
     * @param perNode Top level Element that is per-node.
     * @return true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        // loop over contained signalmast elements
        List<Element> list = shared.getChildren("signalmast");

        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
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
                log.debug("load via " + adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e, null);
                } catch (Exception ex) {
                    log.error("Exception while loading " + e.getName() + ":" + ex);
                    ex.printStackTrace();
                }
            }
        }

        list = shared.getChildren("turnoutsignalmast");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via " + adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e, null);
                } catch (Exception ex) {
                    log.error("Exception while loading " + e.getName() + ":" + ex);
                    ex.printStackTrace();
                }
            }
        }

        list = shared.getChildren("virtualsignalmast");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via " + adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e, null);
                } catch (Exception ex) {
                    log.error("Exception while loading " + e.getName() + ":" + ex);
                    ex.printStackTrace();
                }
            }
        }

        list = shared.getChildren("dccsignalmast");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via " + adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e, null);
                } catch (Exception ex) {
                    log.error("Exception while loading " + e.getName() + ":" + ex);
                    ex.printStackTrace();
                }
            }
        }

        list = shared.getChildren("signalmastrepeater");
        if (list != null) {
            DefaultSignalMastManager m = (DefaultSignalMastManager) InstanceManager.getDefault(jmri.SignalMastManager.class);
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String masterName = e.getChild("masterMast").getText();
                String slaveName = e.getChild("slaveMast").getText();
                SignalMastRepeater smr = new SignalMastRepeater(masterName, slaveName);
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
                try {
                    m.addRepeater(smr);
                } catch (jmri.JmriException ex) {
                    log.error("Unable to add mast repeater " + masterName + " : " + slaveName);
                }
            }
            m.initialiseRepeaters();
        }
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public int loadOrder() {
        return InstanceManager.getDefault(jmri.SignalMastManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastManagerXml.class.getName());
}
