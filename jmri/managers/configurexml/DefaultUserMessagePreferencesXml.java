package jmri.managers.configurexml;

import org.jdom.Element;
import jmri.managers.*;
import java.util.List;

public class DefaultUserMessagePreferencesXml extends jmri.configurexml.AbstractXmlAdapter{

    public DefaultUserMessagePreferencesXml() {
        super();
    }

     /**
     * Default implementation for storing the contents of a
     * User Messages Preferences
     * @param o Object to store, but not really used, because 
     *              info to be stored comes from the DefaultUserMessagePreferences
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        jmri.UserPreferencesManager p = (jmri.UserPreferencesManager) o;

        Element messages = new Element("UserMessagePreferences");
        setStoreElementClass(messages);

        Element userPref;
        
        if (!p.getDisplayRememberMsg()){
            userPref = new Element("displayRememberMsg").setAttribute("display", "no");
            messages.addContent(userPref);
        }
        
        if (!p.getRouteSaveMsg()){
            userPref = new Element("routeSaveMsg").setAttribute("display", "no");
            messages.addContent(userPref);
        }
        
        if (p.getQuitAfterSave()!=0x00){
            if (p.getQuitAfterSave()==0x01)
                userPref = new Element("quitAfterSave").setAttribute("action", "no");
            else
                userPref = new Element("quitAfterSave").setAttribute("action", "yes");
            messages.addContent(userPref);
        }
        
        if (p.getWarnTurnoutInUse()!=0x00){
            if (p.getWarnTurnoutInUse()==0x01)
                userPref = new Element("warnTurnoutInUse").setAttribute("action", "no");
            else
                userPref = new Element("warnTurnoutInUse").setAttribute("action", "yes");
            messages.addContent(userPref);
        }
        

        return messages;
    }
    
    public void setStoreElementClass(Element messages) {
        messages.setAttribute("class","jmri.managers.configurexml.DefaultUserMessagePreferencesXml");
    }
    
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    /**
     * Create a MemoryManager object of the correct class, then
     * register and fill it.
     * @param messages Top level Element to unpack.
     * @return true if successful
     */
     @SuppressWarnings("unchecked")
    public boolean load(Element messages) {
        // ensure the master object exists
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        p.setLoading();
        List<Element> messageList = messages.getChildren("displayRememberMsg");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("display")!=null) {
                String yesno = messageList.get(i).getAttribute("display").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setDisplayRememberMsg(true);
                    else if (yesno.equals("no")) p.setDisplayRememberMsg(false);
                }
            }
        }
        
        messageList = messages.getChildren("routeSaveMsg");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("display")!=null) {
                String yesno = messageList.get(i).getAttribute("display").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setRouteSaveMsg(true);
                    else if (yesno.equals("no")) p.setRouteSaveMsg(false);
                }
            }
        }
        messageList = messages.getChildren("quitAfterSave");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("action")!=null) {
                String yesno = messageList.get(i).getAttribute("action").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setQuitAfterSave(0x02);
                    else if (yesno.equals("no")) p.setQuitAfterSave(0x01);
                }
            }
        }
        
        messageList = messages.getChildren("warnTurnoutInUse");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("action")!=null) {
                String yesno = messageList.get(i).getAttribute("action").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setWarnTurnoutInUse(0x02);
                    else if (yesno.equals("no")) p.setWarnTurnoutInUse(0x01);
                }
            }
        }
        p.finishLoading();
        return true;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultUserMessagePreferencesXml.class.getName());
}