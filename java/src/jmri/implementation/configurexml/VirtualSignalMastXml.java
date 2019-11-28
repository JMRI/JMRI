package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.implementation.VirtualSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * 
 */
public class VirtualSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public VirtualSignalMastXml() {
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
        VirtualSignalMast p = (VirtualSignalMast) o;
        Element e = new Element("virtualsignalmast");
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
        VirtualSignalMast m;
        String sys = getSystemName(shared);
        SignalMast previous = InstanceManager.getDefault(jmri.SignalMastManager.class)
                .getBySystemName(sys);
        if (previous != null) {
            if (previous instanceof VirtualSignalMast) {
                m = (VirtualSignalMast) previous;
            } else {
                log.error("Cannot load signal mast because system name {} is already in use.", sys);
                return false;
            }
        } else {
            m = new VirtualSignalMast(sys);
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

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(VirtualSignalMastXml.class);
}
