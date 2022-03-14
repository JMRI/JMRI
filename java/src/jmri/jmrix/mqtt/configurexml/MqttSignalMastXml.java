package jmri.jmrix.mqtt.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.jmrix.mqtt.MqttSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009, 2021
 *
 */
public class MqttSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public MqttSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        MqttSignalMast p = (MqttSignalMast) o;
        Element e = new Element("mqttsignalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));
        storeCommon(p, e);
        Element unlit = new Element("unlit");
        if (p.allowUnLit()) {
            unlit.setAttribute("allowed", "yes");
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);
        List<String> disabledAspects = p.getDisabledAspects();
        if (disabledAspects != null) {
            Element el = new Element("disabledAspects");
            for (String aspect : disabledAspects) {
                Element ele = new Element("disabledAspect");
                ele.addContent(aspect);
                el.addContent(ele);
            }
            if (disabledAspects.size() != 0) {
                e.addContent(el);
            }
        }
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        MqttSignalMast m;
        String sys = getSystemName(shared);
        SignalMast previous = InstanceManager.getDefault(jmri.SignalMastManager.class)
                .getBySystemName(sys);
        if (previous != null) {
            if (previous instanceof MqttSignalMast) {
                m = (MqttSignalMast) previous;
            } else {
                log.error("Cannot load signal mast because system name {} is already in use.", sys);
                return false;
            }
        } else {
            m = new MqttSignalMast(sys);
        }

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        loadCommon(m, shared);
        if (shared.getChild("unlit") != null) {
            Element unlit = shared.getChild("unlit");
            if (unlit.getAttribute("allowed") != null) {
                if (unlit.getAttribute("allowed").getValue().equals("no")) {
                    m.setAllowUnLit(false);
                } else {
                    m.setAllowUnLit(true);
                }
            }
        }
        Element e = shared.getChild("disabledAspects");
        if (e != null) {
            List<Element> list = e.getChildren("disabledAspect");
            for (Element aspect : list) {
                m.setAspectDisabled(aspect.getText());
            }
        }

        InstanceManager.getDefault(jmri.SignalMastManager.class)
                .register(m);

        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(MqttSignalMastXml.class);
}
