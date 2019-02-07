package jmri.jmrix.openlcb.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalAppearanceMap;
import jmri.jmrix.openlcb.OlcbSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for OlcbSignalMast objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2017, 2018
 * 
 */
public class OlcbSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public OlcbSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     *
     * @param o Object to store, of type OlcbSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        OlcbSignalMast p = (OlcbSignalMast) o;
        Element e = new Element("olcbsignalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, e);
        
        Element lit = new Element("lit");
        lit.addContent(new Element("lit").addContent(p.getLitEventId()));
        lit.addContent(new Element("notlit").addContent(p.getNotLitEventId()));
        e.addContent(lit);
        
        Element held = new Element("held");
        held.addContent(new Element("held").addContent(p.getHeldEventId()));
        held.addContent(new Element("notheld").addContent(p.getNotHeldEventId()));
        e.addContent(held);
        
        SignalAppearanceMap appMap = p.getAppearanceMap();
        if (appMap != null) {
            java.util.Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                Element el = new Element("aspect");
                el.setAttribute("defines", key);
                el.addContent(new Element("event").addContent(p.getOutputForAppearance(key)));
                e.addContent(el);
            }
        }
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
        OlcbSignalMast m;
        String sys = getSystemName(shared);
        try {
            m = (OlcbSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideCustomSignalMast(sys, OlcbSignalMast.class);
        } catch (JmriException e) {
            log.error("Failed to load OlcbSignalMast {}: {}", sys, e);
            return false;
        }

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        return loadCommonOlcbMast(m, shared);
    }

    protected boolean loadCommonOlcbMast(OlcbSignalMast m, Element element) {
        loadCommon(m, element);
        
        if (element.getChild("lit") != null) {
            Element lit = element.getChild("lit");
            m.setLitEventId(lit.getChild("lit").getValue());
            m.setNotLitEventId(lit.getChild("notlit").getValue());
        }
        
        if (element.getChild("held") != null) {
            Element held = element.getChild("held");
            m.setHeldEventId(held.getChild("held").getValue());
            m.setNotHeldEventId(held.getChild("notheld").getValue());
        }
        
        List<Element> list = element.getChildren("aspect");
        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
            String aspect = e.getAttribute("defines").getValue();
            String event = e.getChild("event").getValue();
            m.setOutputForAppearance(aspect, event);
        }
        Element e = element.getChild("disabledAspects");
        if (e != null) {
            list = e.getChildren("disabledAspect");
            for (Element aspect : list) {
                m.setAspectDisabled(aspect.getText());
            }
        }

        return true;

    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSignalMastXml.class);
}
