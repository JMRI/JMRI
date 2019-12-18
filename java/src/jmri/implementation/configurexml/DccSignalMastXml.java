package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalAppearanceMap;
import jmri.implementation.DccSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * 
 */
public class DccSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DccSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     *
     * @param o Object to store, of type TripleDccSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        DccSignalMast p = (DccSignalMast) o;
        Element e = new Element("dccsignalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, e);
        Element unlit = new Element("unlit");
        if (p.allowUnLit()) {
            unlit.setAttribute("allowed", "yes");
            unlit.addContent(new Element("aspect").addContent(Integer.toString(p.getUnlitId())));
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);

        e.addContent(new Element("packetsendcount").addContent(Integer.toString(p.getDccSignalMastPacketSendCount())));

        SignalAppearanceMap appMap = p.getAppearanceMap();
        if (appMap != null) {
            java.util.Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                Element el = new Element("aspect");
                el.setAttribute("defines", key);
                el.addContent(new Element("number").addContent(Integer.toString(p.getOutputForAppearance(key))));
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
        DccSignalMast m;
        String sys = getSystemName(shared);
        try {
            m = (DccSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideCustomSignalMast(sys, DccSignalMast.class);
        } catch (JmriException e) {
            log.error("Failed to load DccSignalMast {}: {}", sys, e);
            return false;
        }

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        if (shared.getChild("packetsendcount") != null) {
            m.setDccSignalMastPacketSendCount(Integer.parseInt(shared.getChild("packetsendcount").getValue()));
        }

        return loadCommonDCCMast(m, shared);
    }

    protected boolean loadCommonDCCMast(DccSignalMast m, Element element) {
        loadCommon(m, element);
        if (element.getChild("unlit") != null) {
            Element unlit = element.getChild("unlit");
            if (unlit.getAttribute("allowed") != null) {
                if (unlit.getAttribute("allowed").getValue().equals("no")) {
                    m.setAllowUnLit(false);
                } else {
                    m.setAllowUnLit(true);
                    m.setUnlitId(Integer.parseInt(unlit.getChild("aspect").getValue()));
                }
            }
        }
        List<Element> list = element.getChildren("aspect");
        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
            String aspect = e.getAttribute("defines").getValue();
            int number = -1;
            try {
                String value = e.getChild("number").getValue();
                number = Integer.parseInt(value);

            } catch (Exception ex) {
                log.error("failed to convert DCC number");
            }
            m.setOutputForAppearance(aspect, number);
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

    private final static Logger log = LoggerFactory.getLogger(DccSignalMastXml.class);
}
