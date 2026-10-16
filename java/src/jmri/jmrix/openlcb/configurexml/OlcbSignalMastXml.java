package jmri.jmrix.openlcb.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalAppearanceMap;
import jmri.jmrix.openlcb.OlcbSignalMast;

import org.jdom2.Element;

import org.openlcb.EventID;

import javax.annotation.Nonnull;

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
        lit.addContent(new Element("lit").addContent(nullToEmpty(p.getLitEventId())));
        lit.addContent(new Element("notlit").addContent(nullToEmpty(p.getNotLitEventId())));
        e.addContent(lit);

        Element held = new Element("held");
        held.addContent(new Element("held").addContent(nullToEmpty(p.getHeldEventId())));
        held.addContent(new Element("notheld").addContent(nullToEmpty(p.getNotHeldEventId())));
        e.addContent(held);

        SignalAppearanceMap appMap = p.getAppearanceMap();
        if (appMap != null) {
            java.util.Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                Element el = new Element("aspect");
                el.setAttribute("defines", key);
                el.addContent(new Element("event").addContent(nullToEmpty(p.getOutputForAppearance(key))));
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
    public boolean load(@Nonnull Element shared, Element perNode) {
        OlcbSignalMast m;
        String sys = getSystemName(shared);
        try {
            m = (OlcbSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideCustomSignalMast(sys, OlcbSignalMast.class);
        } catch (JmriException e) {
            log.error("Failed to load OlcbSignalMast {}", sys, e);
            return false;
        }

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        return loadCommonOlcbMast(m, shared);
    }

    /**
     * If the input string is a zero Event ID, replace it 
     * with the empty String
     * @param event an event ID read from an element
     * @param m the relevant signal mast, used for a warning message
     * @return the event ID or the empty String
     */
    private String convertZeroEvent(String event, OlcbSignalMast m) {
        if ("".equals(event)) {
            return "";
        }
        if (event == null) {
            return "";
        }
        try {
            var eventID = new EventID(event);
            if (eventID.getContents()[0] == 0) {
                // migrate starting-with-zero event ID to blank
                log.warn("Migrating zero-initial-value event {} to a blank entry in signal mast \"{}\"", event, m.getDisplayName());
                return "";
            } else {
                // non-zero-starting event ID, normal case
                return event;
            }
        } catch (IllegalArgumentException ex) {
            // not dotted-hex, assume this is a event name
            return event;
        }
        
    }
    
    protected boolean loadCommonOlcbMast(OlcbSignalMast m, Element element) {
        loadCommon(m, element);
        
        if (element.getChild("lit") != null) {
            Element lit = element.getChild("lit");
            m.setLitEventId(convertZeroEvent(childValue(lit, "lit"), m));
            m.setNotLitEventId(convertZeroEvent(childValue(lit, "notlit"), m));
        }

        if (element.getChild("held") != null) {
            Element held = element.getChild("held");
            m.setHeldEventId(convertZeroEvent(childValue(held, "held"), m));
            m.setNotHeldEventId(convertZeroEvent(childValue(held, "notheld"), m));
        }

        List<Element> list = element.getChildren("aspect");
        for (Element e : list) {
            String aspect = e.getAttribute("defines").getValue();
            String event = convertZeroEvent(childValue(e, "event"), m);
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbSignalMastXml.class);
}
