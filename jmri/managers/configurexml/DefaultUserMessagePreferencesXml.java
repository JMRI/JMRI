package jmri.managers.configurexml;

import org.jdom.Element;
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

        //Element userPref;
        
        storeDisplayMsg(messages, "displayRememberMsg", p.getDisplayRememberMsg());
        /*if (!p.getDisplayRememberMsg()){
            userPref = new Element("displayRememberMsg").setAttribute("display", "no");
            messages.addContent(userPref);
        }*/
        
        storeDisplayMsg(messages, "routeSaveMsg", p.getRouteSaveMsg());
        /*if (!p.getRouteSaveMsg()){
            userPref = new Element("routeSaveMsg").setAttribute("display", "no");
            messages.addContent(userPref);
        }*/
        
        storeQuestion(messages, "warnRouteDelete", p.getWarnDeleteRoute());
        /*if (p.getWarnRouteDelete()!=0x00){
            if (p.getWarnRouteDelete()==0x01)
                userPref = new Element("warnRouteDelete").setAttribute("action", "no");
            else
                userPref = new Element("warnRouteDelete").setAttribute("action", "yes");
            messages.addContent(userPref);
        }*/
        
        storeQuestion(messages, "quitAfterSave", p.getQuitAfterSave());
        /*if (p.getQuitAfterSave()!=0x00){
            if (p.getQuitAfterSave()==0x01)
                userPref = new Element("quitAfterSave").setAttribute("action", "no");
            else
                userPref = new Element("quitAfterSave").setAttribute("action", "yes");
            messages.addContent(userPref);
        }*/
        
        storeQuestion(messages, "warnTurnoutInUse", p.getWarnTurnoutInUse());
        /*if (p.getWarnTurnoutInUse()!=0x00){
            if (p.getWarnTurnoutInUse()==0x01)
                userPref = new Element("warnTurnoutInUse").setAttribute("action", "no");
            else
                userPref = new Element("warnTurnoutInUse").setAttribute("action", "yes");
            messages.addContent(userPref);
        }*/
        
        storeQuestion(messages, "warnSensorInUse", p.getWarnSensorInUse());
        storeQuestion(messages, "warnSignalHeadInUse", p.getWarnSignalHeadInUse());
        storeQuestion(messages, "warnTransitInUse", p.getWarnTransitInUse());
        storeQuestion(messages, "warnSignalMastInUse", p.getWarnSignalMastInUse());
        storeQuestion(messages, "warnSectionInUse", p.getWarnSectionInUse());
        storeQuestion(messages, "warnReporterInUse", p.getWarnReporterInUse());
        storeQuestion(messages, "warnMemoryInUse", p.getWarnMemoryInUse());
        storeQuestion(messages, "warnLogixInUse", p.getWarnLogixInUse());
        storeQuestion(messages, "warnLightInUse", p.getWarnLightInUse());
        storeQuestion(messages, "warnLRouteInUse", p.getWarnLRouteInUse());
        storeQuestion(messages, "warnBlockInUse", p.getWarnBlockInUse());
        storeQuestion(messages, "warnAudioInUse", p.getWarnAudioInUse());

        return messages;
    }
    
    private void storeQuestion(Element messages, String attr, int value){
        Element userPref;
        if (value!=0x00){
            if (value==0x01)
                userPref = new Element(attr).setAttribute("action", "no");
            else
                userPref = new Element(attr).setAttribute("action", "yes");
            messages.addContent(userPref);
        }
    }
    
    private void storeDisplayMsg(Element messages, String attr, boolean value){
        Element userPref;
        if (!value){
            userPref = new Element(attr).setAttribute("display", "no");
            messages.addContent(userPref);
        }
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
    public boolean load(Element messages) {
        // ensure the master object exists
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        p.setLoading();
        
        p.setDisplayRememberMsg(loadDisplayMsg(messages, "displayRememberMsg"));
        /*List<Element> messageList = messages.getChildren("displayRememberMsg");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("display")!=null) {
                String yesno = messageList.get(i).getAttribute("display").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setDisplayRememberMsg(true);
                    else if (yesno.equals("no")) p.setDisplayRememberMsg(false);
                }
            }
        }*/
        
        p.setRouteSaveMsg(loadDisplayMsg(messages, "routeSaveMsg"));
        /*messageList = messages.getChildren("routeSaveMsg");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("display")!=null) {
                String yesno = messageList.get(i).getAttribute("display").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setRouteSaveMsg(true);
                    else if (yesno.equals("no")) p.setRouteSaveMsg(false);
                }
            }
        }*/
        
        p.setQuitAfterSave(loadQuestion(messages, "quitAfterSave"));
        /*messageList = messages.getChildren("quitAfterSave");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("action")!=null) {
                String yesno = messageList.get(i).getAttribute("action").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setQuitAfterSave(0x02);
                    else if (yesno.equals("no")) p.setQuitAfterSave(0x01);
                }
            }
        }*/
        
        p.setWarnTurnoutInUse(loadQuestion(messages, "warnTurnoutInUse"));
        /*messageList = messages.getChildren("warnTurnoutInUse");
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("action")!=null) {
                String yesno = messageList.get(i).getAttribute("action").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) p.setWarnTurnoutInUse(0x02);
                    else if (yesno.equals("no")) p.setWarnTurnoutInUse(0x01);
                }
            }
        }*/
        p.setWarnSensorInUse(loadQuestion(messages, "warnSensorInUse"));
        p.setWarnSignalHeadInUse(loadQuestion(messages, "warnSignalHeadInUse"));
        p.setWarnTransitInUse(loadQuestion(messages, "warnTransitInUse"));
        p.setWarnSignalMastInUse(loadQuestion(messages, "warnSignalMastInUse"));
        p.setWarnSectionInUse(loadQuestion(messages, "warnSectionInUse"));
        p.setWarnReporterInUse(loadQuestion(messages, "warnReporterInUse"));
        p.setWarnMemoryInUse(loadQuestion(messages, "warnMemoryInUse"));
        p.setWarnLogixInUse(loadQuestion(messages, "warnLogixInUse"));
        p.setWarnLightInUse(loadQuestion(messages, "warnLightInUse"));
        p.setWarnLRouteInUse(loadQuestion(messages, "warnLRouteInUse"));
        p.setWarnBlockInUse(loadQuestion(messages, "warnBlockInUse"));
        p.setWarnAudioInUse(loadQuestion(messages, "warnAudioInUse"));
        p.setWarnDeleteRoute(loadQuestion(messages, "routeSaveMsg"));
        p.finishLoading();
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private int loadQuestion(Element messages, String attr){
        List<Element> messageList = messages.getChildren(attr);
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("action")!=null) {
                String yesno = messageList.get(i).getAttribute("action").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) return(0x02);
                    else if (yesno.equals("no")) return(0x01);
                }
            }
        }
        return(0x00);
    }
    
    @SuppressWarnings("unchecked")
    private boolean loadDisplayMsg(Element messages, String attr){
        List<Element> messageList = messages.getChildren(attr);
        for (int i=0; i<messageList.size();i++){
            if (messageList.get(i).getAttribute("display")!=null) {
                String yesno = messageList.get(i).getAttribute("display").getValue();
                if ( (yesno!=null) && (!yesno.equals("")) ) {
                    if (yesno.equals("yes")) return(true);
                    else if (yesno.equals("no")) return(false);
                }
            }
        }
        return (false);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultUserMessagePreferencesXml.class.getName());
}