package jmri.jmrix.openlcb.configurexml;

import java.util.List;
import jmri.InstanceManager;
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
        Element unlit = new Element("unlit");
        if (p.allowUnLit()) {
            unlit.setAttribute("allowed", "yes");
            unlit.addContent(new Element("aspect").addContent(p.getUnlitId()));
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);
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
        m = new OlcbSignalMast(sys);

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        return loadCommonOlcbMast(m, shared);
    }

    protected boolean loadCommonOlcbMast(OlcbSignalMast m, Element element) {
        loadCommon(m, element);
        if (element.getChild("unlit") != null) {
            Element unlit = element.getChild("unlit");
            if (unlit.getAttribute("allowed") != null) {
                if (unlit.getAttribute("allowed").getValue().equals("no")) {
                    m.setAllowUnLit(false);
                } else {
                    m.setAllowUnLit(true);
                    m.setUnlitId(unlit.getChild("aspect").getValue());
                }
            }
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

        System.out.println("register "+m);
        InstanceManager.getDefault(jmri.SignalMastManager.class)
                .register(m);
        return true;

    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSignalMastXml.class);
}
