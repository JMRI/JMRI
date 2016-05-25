package jmri.managers.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.managers.DefaultSignalMastManager;
import jmri.implementation.SignalMastRepeater;
import jmri.configurexml.XmlAdapter;
import java.util.List;

import org.jdom.*;

/**
 * Handle XML configuration for a DefaultSignalMastManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 */
public class DefaultSignalMastManagerXml 
            extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultSignalMastManagerXml() {}

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     * @param o Object to store
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DefaultSignalMastManager m = (DefaultSignalMastManager)o;

        Element element = new Element("signalmasts");
        element.setAttribute("class", this.getClass().getName());
        if(m!=null){
            // include contents
            List<String> names = m.getSystemNameList();
            for (int i = 0; i < names.size(); i++) {
                //Element e = new Element("signalmast");
                SignalMast p = m.getSignalMast(names.get(i));
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(p);
                    if (e!=null) element.addContent(e);
                } catch (Exception e) {
                    log.error("Error storing signalmast: "+e);
                    e.printStackTrace();
                }
                
            }
            List<SignalMastRepeater> repeaterList = m.getRepeaterList();
            if(repeaterList.size()>0){
                //Element repeatElem= new Element("signalmastrepeaters");
                for(SignalMastRepeater smr:repeaterList){
                    if(smr.getMasterMast()!=null && smr.getSlaveMast()!=null){
                        Element e = new Element("signalmastrepeater");
                        e.addContent(new Element("masterMast").addContent(smr.getMasterMastName()));
                        e.addContent(new Element("slaveMast").addContent(smr.getSlaveMastName()));
                        e.addContent(new Element("enabled").addContent(smr.getEnabled()?"true":"false"));
                        switch (smr.getDirection()){
                            case 1 : e.addContent(new Element("update").addContent("MasterToSlave")); break;
                            case 2 : e.addContent(new Element("update").addContent("SlaveToMaster")); break;
                            default : e.addContent(new Element("update").addContent("BothWay")); break;
                        }
                        element.addContent(e);
                    }
                }
                //element.add(repeatElem);
            }
        }
        return element;
    }

    /**
     * Create a DefaultSignalMastManager
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
    public boolean load(Element element) {
        // loop over contained signalmast elements
        List<Element> list = element.getChildren("signalmast");

        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
            if(e.getAttribute("class")==null){
                SignalMast m;
                String sys = getSystemName(e);
                m = InstanceManager.signalMastManagerInstance()
                            .provideSignalMast(sys);
                
                if (getUserName(e) != null)
                    m.setUserName(getUserName(e));
                
                loadCommon(m, e);
            } else {
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via "+adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e);
                } catch (Exception ex) {
                    log.error("Exception while loading "+e.getName()+":"+ex);
                    ex.printStackTrace();
                }
            }
        }
        
        
        list = element.getChildren("turnoutsignalmast");
        if(list!=null){
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via "+adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e);
                } catch (Exception ex) {
                    log.error("Exception while loading "+e.getName()+":"+ex);
                    ex.printStackTrace();
                }
            }
        }
        
        list = element.getChildren("virtualsignalmast");
        if(list!=null){
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via "+adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e);
                } catch (Exception ex) {
                    log.error("Exception while loading "+e.getName()+":"+ex);
                    ex.printStackTrace();
                }
            }
        }
        
        list = element.getChildren("dccsignalmast");
        if(list!=null){
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String adapterName = e.getAttribute("class").getValue();
                log.debug("load via "+adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(e);
                } catch (Exception ex) {
                    log.error("Exception while loading "+e.getName()+":"+ex);
                    ex.printStackTrace();
                }
            }
        }
        
        list = element.getChildren("signalmastrepeater");
        if(list!=null){
            DefaultSignalMastManager m = (DefaultSignalMastManager)InstanceManager.signalMastManagerInstance();
            for (int i = 0; i < list.size(); i++) {
                Element e = list.get(i);
                String masterName = e.getChild("masterMast").getText();
                String slaveName = e.getChild("slaveMast").getText();
                SignalMastRepeater smr = new SignalMastRepeater(masterName, slaveName);
                if(e.getChild("enabled")!=null && e.getChild("enabled").getText().equals("false"))
                    smr.setEnabled(false);
                if(e.getChild("update")!=null){
                    if(e.getChild("update").getText().equals("MasterToSlave"))
                        smr.setDirection(SignalMastRepeater.MASTERTOSLAVE);
                    else if (e.getChild("update").getText().equals("SlaveToMaster"))
                        smr.setDirection(SignalMastRepeater.SLAVETOMASTER);
                }
                try {
                    m.addRepeater(smr);
                } catch (jmri.JmriException ex){
                    log.error("Unable to add mast repeater " + masterName + " : " + slaveName);
                }
            }
            m.initialiseRepeaters();
        }
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    public int loadOrder(){
        return InstanceManager.signalMastManagerInstance().getXMLOrder();
    }

    static Logger log = LoggerFactory.getLogger(DefaultSignalMastManagerXml.class.getName());
}
