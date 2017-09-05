package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.implementation.SignalHeadSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * 
 */
public class SignalHeadSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SignalHeadSignalMastXml() {
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
        SignalHeadSignalMast p = (SignalHeadSignalMast) o;
        Element e = new Element("signalmast");
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
        SignalMast m;
        String sys = getSystemName(shared);
        try {
            m = InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideSignalMast(sys);
        } catch (Exception e) {
            log.error("An error occurred while trying to create the signal '" + sys + "' " + e.toString());
            return false;
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
                ((SignalHeadSignalMast) m).setAspectDisabled(aspect.getText());
            }
        }
        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadSignalMastXml.class);
}
