package jmri.implementation.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.implementation.SignalHeadSignalMast;
import org.jdom.Element;
import java.util.List;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 18102 $
 */
public class SignalHeadSignalMastXml 
            extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SignalHeadSignalMastXml() {}

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SignalHeadSignalMast p = (SignalHeadSignalMast)o;
        Element e = new Element("signalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));
        storeCommon(p, e);
        
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
        SignalMast m;
        String sys = getSystemName(element);
        try {
            m = InstanceManager.signalMastManagerInstance()
                    .provideSignalMast(sys);
        } catch (Exception e){
            log.error("An error occured while trying to create the signal '"+sys+"' " + e.toString());
            return false;
        }
        if (getUserName(element) != null)
            m.setUserName(getUserName(element));
        
        loadCommon(m, element);
        
        Element e = element.getChild("disabledAspects");
        if(e!=null){
            @SuppressWarnings("unchecked")
            List<Element> list = e.getChildren("disabledAspect");
            for(Element aspect: list){
                ((SignalHeadSignalMast)m).setAspectDisabled(aspect.getText());
            }
        }
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    static Logger log = LoggerFactory.getLogger(SignalHeadSignalMastXml.class.getName());
}
