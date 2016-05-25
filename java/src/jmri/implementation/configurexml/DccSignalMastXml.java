package jmri.implementation.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.implementation.DccSignalMast;
import jmri.SignalAppearanceMap;
import java.util.List;
import org.jdom.Element;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 18102 $
 */
public class DccSignalMastXml 
            extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DccSignalMastXml() {}

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     * @param o Object to store, of type TripleDccSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DccSignalMast p = (DccSignalMast)o;
        Element e = new Element("dccsignalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, e);
        Element unlit = new Element("unlit");
        if(p.allowUnLit()){
            unlit.setAttribute("allowed", "yes");
            unlit.addContent(new Element("aspect").addContent(Integer.toString(p.getUnlitId())));
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);
        SignalAppearanceMap appMap = p.getAppearanceMap();
        if(appMap!=null){
            java.util.Enumeration<String> aspects = appMap.getAspects();
            while(aspects.hasMoreElements()){
                String key = aspects.nextElement();
                Element el = new Element("aspect");
                el.setAttribute("defines", key);
                el.addContent(new Element("number").addContent(Integer.toString(p.getOutputForAppearance(key))));
                e.addContent(el);
            }
        }
        List<String> disabledAspects = p.getDisabledAspects();
        if(disabledAspects!=null){
            Element el = new Element("disabledAspects");
            for(String aspect: disabledAspects){
                Element ele = new Element("disabledAspect");
                ele.addContent(aspect);
                el.addContent(ele);
            }
            if(disabledAspects.size()!=0)
                e.addContent(el);
        }
        return e;
    }

    /**
     * Create a DefaultSignalMastManager
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element element) {
        DccSignalMast m;
        String sys = getSystemName(element);
        m = new jmri.implementation.DccSignalMast(sys);
        
        if (getUserName(element) != null)
            m.setUserName(getUserName(element));
        
        return loadCommonDCCMast(m, element);
    }
    
    @SuppressWarnings("unchecked")
    protected boolean loadCommonDCCMast(DccSignalMast m, Element element){
        loadCommon(m, element);
        if(element.getChild("unlit")!=null){
            Element unlit = element.getChild("unlit");
            if(unlit.getAttribute("allowed")!=null){
                if(unlit.getAttribute("allowed").getValue().equals("no")){
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
        if(e!=null){
            list = e.getChildren("disabledAspect");
            for(Element aspect: list){
                m.setAspectDisabled(aspect.getText());
            }
        }
        
        InstanceManager.signalMastManagerInstance()
            .register(m);
        return true;
    
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    static Logger log = LoggerFactory.getLogger(DccSignalMastXml.class.getName());
}
