package jmri.jmrix.bidib.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalAppearanceMap;
import jmri.jmrix.bidib.BiDiBSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for BiDiBSignalMast objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2017, 2018
 * @author Eckart Meyer Copyright: Copyright (c) 2020
 * 
 */
public class BiDiBSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public BiDiBSignalMastXml() {
    }

    /**
     * Implementation for storing the contents of a
     * BiDiBSignalMastManager
     *
     * @param o Object to store, of type BiDiBSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        BiDiBSignalMast p = (BiDiBSignalMast) o;
        Element e = new Element("dccsignalmast"); //XML schema is the same than "dccsignalmast"
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
        BiDiBSignalMast m;
        String sys = getSystemName(shared);
        try {
            m = (BiDiBSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideCustomSignalMast(sys, BiDiBSignalMast.class);
        } catch (JmriException e) {
            log.error("Failed to load BiDiBSignalMast {}:", sys, e);
            return false;
        }

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        boolean result = loadCommonBiDiBMast(m, shared);
        if (result) {
            // query state from layout - must be done after loadCommonBiDiBMast since this will load the aspect names
            m.queryAccessory();
        }
        return result;
    }

    protected boolean loadCommonBiDiBMast(BiDiBSignalMast m, Element element) {
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

    private final static Logger log = LoggerFactory.getLogger(BiDiBSignalMastXml.class);
}
