package jmri.jmrit.withrottle;


import org.jdom.Attribute;
import org.jdom.Element;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.2 $
 */
public class WiThrottlePreferences extends AbstractWiThrottlePreferences{
    
    //  Flag that prefs have not been saved:
    private boolean isDirty = false;


    private boolean useEStop = true;
    private int eStopDelay = 10;
    
    private boolean useJmdns = true;
    private boolean useFixedPort = false;
    private String port = null;

    private boolean allowTrackPower = true;
    
    public WiThrottlePreferences(String fileName){
        super.openFile(fileName);
            
    }
    
    public WiThrottlePreferences(){}

    public void load(Element child) {
        Attribute a;
    	if ((a = child.getAttribute("isUseEStop")) != null )  setUseEStop(a.getValue().equalsIgnoreCase("true"));
    	if ((a = child.getAttribute("getEStopDelay")) != null )
            try{
                setEStopDelay(new Integer(a.getValue()));
            }catch (NumberFormatException e){
                log.debug(e);
            }
    	if ((a = child.getAttribute("isUseJmdns")) != null )  setUseJmdns(a.getValue().equalsIgnoreCase("true"));
    	if ((a = child.getAttribute("isUseFixedPort")) != null )  setUseFixedPort(a.getValue().equalsIgnoreCase("true"));
    	if ((a = child.getAttribute("getPort")) != null ) setPort(a.getValue());
            
    	if ((a = child.getAttribute("isAllowTrackPower")) != null )  setAllowTrackPower(a.getValue().equalsIgnoreCase("true"));

    }

    public boolean compareValuesDifferent(WiThrottlePreferences prefs){
        WiThrottlePreferences stored = WiThrottleManager.withrottlePreferencesInstance();
        if (isAllowTrackPower() != prefs.isAllowTrackPower()) return true;
        if (isUseEStop() != prefs.isUseEStop()) return true;
        if (getEStopDelay() != prefs.getEStopDelay()) return true;
        if (isUseFixedPort() != prefs.isUseFixedPort()) return true;
        if (!(getPort().equals(prefs.getPort()))) return true;
        if (isUseJmdns() != prefs.isUseJmdns()) return true;
        return false;
    }

    public void apply(WiThrottlePreferences prefs){
        setUseEStop(prefs.isUseEStop());
        setEStopDelay(prefs.getEStopDelay());
        setUseJmdns(prefs.isUseJmdns());
        setUseFixedPort(prefs.isUseFixedPort());
        setPort(prefs.getPort());
        setAllowTrackPower(prefs.isAllowTrackPower());
    }

    public Element store() {
    	Element element = new Element("WiThrottlePreferences");
        element.setAttribute("isUseEStop", "" + isUseEStop());
        element.setAttribute("getEStopDelay", "" + getEStopDelay());
        element.setAttribute("isUseJmdns", "" + isUseJmdns());
        element.setAttribute("isUseFixedPort", "" + isUseFixedPort());
        element.setAttribute("getPort", "" + getPort());
        element.setAttribute("isAllowTrackPower", "" + isAllowTrackPower());
        setIsDirty(false);  //  Resets only when stored
        return element;
    }
    
    public boolean getIsDirty(){
        return isDirty;
    }
    public void setIsDirty(boolean value){
        isDirty = value;
    }


    public boolean isUseEStop(){
        return useEStop;
    }
    public void setUseEStop(boolean value){
        useEStop = value;
    }

    public int getEStopDelay(){
        return eStopDelay;
    }
    public void setEStopDelay(int value){
        eStopDelay = value;
    }

    public boolean isUseJmdns(){
        return useJmdns;
    }
    public void setUseJmdns(boolean value){
        useJmdns = value;
    }

    public boolean isUseFixedPort(){
        return useFixedPort;
    }
    public void setUseFixedPort(boolean value){
        useFixedPort = value;
    }

    public String getPort(){
        return port;
    }
    public void setPort(String value){
        port = value;
    }

    public boolean isAllowTrackPower(){
        return allowTrackPower;
    }
    public void setAllowTrackPower(boolean value){
        allowTrackPower = value;
    }

    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WiThrottlePreferences.class.getName());

}
