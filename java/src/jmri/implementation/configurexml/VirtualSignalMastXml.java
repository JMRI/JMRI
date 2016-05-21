package jmri.implementation.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.implementation.VirtualSignalMast;
import java.util.List;
import org.jdom.Element;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 18102 $
 */
public class VirtualSignalMastXml 
            extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public VirtualSignalMastXml() {}

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        VirtualSignalMast p = (VirtualSignalMast)o;
        Element e = new Element("virtualsignalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));
        storeCommon(p, e);
        Element unlit = new Element("unlit");
        if(p.allowUnLit()){
            unlit.setAttribute("allowed", "yes");
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);
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
        VirtualSignalMast m;
        String sys = getSystemName(element);
        m = new jmri.implementation.VirtualSignalMast(sys);
        
        if (getUserName(element) != null)
            m.setUserName(getUserName(element));
        
        loadCommon(m, element);
        if(element.getChild("unlit")!=null){
            Element unlit = element.getChild("unlit");
            if(unlit.getAttribute("allowed")!=null){
                if(unlit.getAttribute("allowed").getValue().equals("no")){
                    m.setAllowUnLit(false);
                } else {
                    m.setAllowUnLit(true);
                }
            }
        }
        Element e = element.getChild("disabledAspects");
        if(e!=null){
            @SuppressWarnings("unchecked")
            List<Element> list = e.getChildren("disabledAspect");
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
    
    static Logger log = LoggerFactory.getLogger(VirtualSignalMastXml.class.getName());
}
