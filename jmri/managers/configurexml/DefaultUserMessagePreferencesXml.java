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
      
        storeQuestion(messages, "warnRouteDelete", p.getWarnDeleteRoute());
        
        storeQuestion(messages, "quitAfterSave", p.getQuitAfterSave());
        
        storeQuestion(messages, "warnTurnoutInUse", p.getWarnTurnoutInUse());
        
        java.util.ArrayList<String> preferenceList = ((jmri.managers.DefaultUserMessagePreferences)p).getPreferenceStateList();
        for (int i = 0; i < preferenceList.size(); i++) {
            Element pref = new Element("setting");
            pref.addContent(preferenceList.get(i));
            messages.addContent(pref);
        }
        
        int comboBoxSize = p.getComboBoxSelectionSize();
        if (comboBoxSize >0){
            Element comboList = new Element("comboBoxLastValue");
                for(int i = 0; i<comboBoxSize; i++){
                    //No point in storing the last entered/selected value if it is blank
                    if ((p.getComboBoxLastSelection(i)!=null)&&(!p.getComboBoxLastSelection(i).equals(""))){
                        Element combo = new Element("comboBox");
                        combo.setAttribute("name", p.getComboBoxName(i));
                        combo.setAttribute("lastSelected", p.getComboBoxLastSelection(i));
                        comboList.addContent(combo);
                    }
                }
            messages.addContent(comboList);
        }
        
        storeQuestion(messages, "warnSensorInUse", p.getWarnSensorInUse());
        storeQuestion(messages, "warnSignalHeadInUse", p.getWarnSignalHeadInUse());
        storeQuestion(messages, "warnTransitInUse", p.getWarnTransitInUse());
        storeQuestion(messages, "warnSignalMastInUse", p.getWarnSignalMastInUse());
        storeQuestion(messages, "warnSectionInUse", p.getWarnSectionInUse());
        storeQuestion(messages, "warnReporterInUse", p.getWarnReporterInUse());
        storeQuestion(messages, "warnMemoryInUse", p.getWarnMemoryInUse());
        storeQuestion(messages, "warnLogixInUse", p.getWarnLogixInUse());
        storeQuestion(messages, "warnDeleteLogix", p.getWarnDeleteLogix());
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
                
        p.setQuitAfterSave(loadQuestion(messages, "quitAfterSave"));
        
        p.setWarnTurnoutInUse(loadQuestion(messages, "warnTurnoutInUse"));
       
        @SuppressWarnings("unchecked")
        List<Element> settingList = messages.getChildren("setting");
        
        for (int i = 0; i < settingList.size(); i++) {
            String name = settingList.get(i).getText();
            p.setPreferenceState(name, true);
        }
        @SuppressWarnings("unchecked")
        List<Element> comboList = messages.getChildren("comboBoxLastValue");
        
        for (int i = 0; i < comboList.size(); i++) {    
            @SuppressWarnings("unchecked")
            List<Element> comboItem = comboList.get(i).getChildren("comboBox");
            for (int x = 0; x<comboItem.size(); x++){
                String combo = comboItem.get(x).getAttribute("name").getValue();
                String setting = comboItem.get(x).getAttribute("lastSelected").getValue();                
                p.addComboBoxLastSelection(combo, setting);
            }
        }
        
        p.setWarnSensorInUse(loadQuestion(messages, "warnSensorInUse"));
        p.setWarnSignalHeadInUse(loadQuestion(messages, "warnSignalHeadInUse"));
        p.setWarnTransitInUse(loadQuestion(messages, "warnTransitInUse"));
        p.setWarnSignalMastInUse(loadQuestion(messages, "warnSignalMastInUse"));
        p.setWarnSectionInUse(loadQuestion(messages, "warnSectionInUse"));
        p.setWarnReporterInUse(loadQuestion(messages, "warnReporterInUse"));
        p.setWarnDeleteRoute(loadQuestion(messages, "warnRouteDelete"));
        p.setWarnMemoryInUse(loadQuestion(messages, "warnMemoryInUse"));
        p.setWarnLogixInUse(loadQuestion(messages, "warnLogixInUse"));
        p.setWarnDeleteLogix(loadQuestion(messages, "warnDeleteLogix"));
        p.setWarnLightInUse(loadQuestion(messages, "warnLightInUse"));
        p.setWarnLRouteInUse(loadQuestion(messages, "warnLRouteInUse"));
        p.setWarnBlockInUse(loadQuestion(messages, "warnBlockInUse"));
        p.setWarnAudioInUse(loadQuestion(messages, "warnAudioInUse"));
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
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultUserMessagePreferencesXml.class.getName());
}
    