package jmri.jmrit.withrottle;


import org.jdom.Attribute;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */
public class WiThrottlePreferences extends AbstractWiThrottlePreferences{
    
    //  Flag that prefs have not been saved:
    private boolean isDirty = false;


    private boolean useEStop = true;
    private int eStopDelay = 10;
    
    private boolean useMomF2 = true;
    
    private boolean useFixedPort = false;
    private String port = null;

    private boolean allowTrackPower = true;
    private boolean allowTurnout = true;
    private boolean allowRoute = true;
    private boolean allowConsist = true;
    private boolean useWiFiConsist = true;
    
    public WiThrottlePreferences(String fileName){
        super.openFile(fileName);
            
    }
    
    public WiThrottlePreferences(){}

    @Override
    public void load(Element child) {
        Attribute a;
    	if ((a = child.getAttribute("isUseEStop")) != null )  setUseEStop(a.getValue().equalsIgnoreCase("true"));
    	if ((a = child.getAttribute("getEStopDelay")) != null )
            try{
                setEStopDelay(Integer.valueOf(a.getValue()));
            }catch (NumberFormatException e){
                log.debug(e.getLocalizedMessage(), e);
            }
        if ((a = child.getAttribute("isUseMomF2")) != null )  setUseMomF2(a.getValue().equalsIgnoreCase("true"));
    	if ((a = child.getAttribute("isUseFixedPort")) != null )  setUseFixedPort(a.getValue().equalsIgnoreCase("true"));
    	if ((a = child.getAttribute("getPort")) != null ) setPort(a.getValue());
            
    	if ((a = child.getAttribute("isAllowTrackPower")) != null )  setAllowTrackPower(a.getValue().equalsIgnoreCase("true"));
        if ((a = child.getAttribute("isAllowTurnout")) != null )  setAllowTurnout(a.getValue().equalsIgnoreCase("true"));
        if ((a = child.getAttribute("isAllowRoute")) != null )  setAllowRoute(a.getValue().equalsIgnoreCase("true"));
        if ((a = child.getAttribute("isAllowConsist")) != null )  setAllowConsist(a.getValue().equalsIgnoreCase("true"));
        if ((a = child.getAttribute("isUseWiFiConsist")) != null )  setUseWiFiConsist(a.getValue().equalsIgnoreCase("true"));

    }

    public boolean compareValuesDifferent(WiThrottlePreferences prefs){
        if (isAllowTrackPower() != prefs.isAllowTrackPower()) return true;
        if (isAllowTurnout() != prefs.isAllowTurnout()) return true;
        if (isAllowRoute() != prefs.isAllowRoute()) return true;
        if (isAllowConsist() != prefs.isAllowConsist()) return true;
        if (isUseWiFiConsist() != prefs.isUseWiFiConsist()) return true;

        if (isUseEStop() != prefs.isUseEStop()) return true;
        if (getEStopDelay() != prefs.getEStopDelay()) return true;
        if (isUseMomF2() != prefs.isUseMomF2()) return true;
        if (isUseFixedPort() != prefs.isUseFixedPort()) return true;
        if (!(getPort().equals(prefs.getPort()))) return true;
        return false;
    }

    public void apply(WiThrottlePreferences prefs){
        setUseEStop(prefs.isUseEStop());
        setEStopDelay(prefs.getEStopDelay());
        setUseMomF2(prefs.isUseMomF2());
        setUseFixedPort(prefs.isUseFixedPort());
        setPort(prefs.getPort());
        setAllowTrackPower(prefs.isAllowTrackPower());
        setAllowTurnout(prefs.isAllowTurnout());
        setAllowRoute(prefs.isAllowRoute());
        setAllowConsist(prefs.isAllowConsist());
        setUseWiFiConsist(prefs.isUseWiFiConsist());
    }

    @Override
    public Element store() {
    	Element element = new Element("WiThrottlePreferences");
        element.setAttribute("isUseEStop", "" + isUseEStop());
        element.setAttribute("getEStopDelay", "" + getEStopDelay());
        element.setAttribute("isUseMomF2", "" + isUseMomF2());
        element.setAttribute("isUseFixedPort", "" + isUseFixedPort());
        element.setAttribute("getPort", "" + getPort());
        element.setAttribute("isAllowTrackPower", "" + isAllowTrackPower());
        element.setAttribute("isAllowTurnout", "" + isAllowTurnout());
        element.setAttribute("isAllowRoute", "" + isAllowRoute());
        element.setAttribute("isAllowConsist", "" + isAllowConsist());
        element.setAttribute("isUseWiFiConsist", "" + isUseWiFiConsist());
        setIsDirty(false);  //  Resets only when stored
        return element;
    }
    
    public boolean isDirty(){
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
    
    public boolean isUseMomF2(){
        return useMomF2;
    }
    public void setUseMomF2(boolean value){
        useMomF2 = value;
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

    public boolean isAllowTurnout(){
        return allowTurnout;
    }
    public void setAllowTurnout(boolean value){
        allowTurnout = value;
    }

    public boolean isAllowRoute(){
        return allowRoute;
    }
    public void setAllowRoute(boolean value){
        allowRoute = value;
    }

    public boolean isAllowConsist(){
        return allowConsist;
    }
    public void setAllowConsist(boolean value){
        allowConsist = value;
    }
    
    public boolean isUseWiFiConsist(){
        return useWiFiConsist;
    }
    public void setUseWiFiConsist(boolean value){
        useWiFiConsist = value;
    }

    private static Logger log = LoggerFactory.getLogger(WiThrottlePreferences.class.getName());

}
