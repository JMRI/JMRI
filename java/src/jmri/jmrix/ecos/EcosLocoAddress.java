package jmri.jmrix.ecos;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.util.List;
import jmri.DccThrottle;
/**
 * Stores all the loco information from the Ecos into JMRI
 *
 * @author Kevin Dickerson
 * @version     $Revision$
 */
public class EcosLocoAddress {
    private String _ecosObject = null;
    private int _dccAddress = 0;
    private String _ecosDescription = null;
    private String _rosterId = null;
    private String _protocol = null;
    private String _cv8 = null;
    private String _cv7 = null;
    boolean direction;
    int currentSpeed;
    private boolean doNotAddToRoster = false;
    

    public EcosLocoAddress(int dCCAddress){
        _dccAddress=dCCAddress;
    }
    
    public EcosLocoAddress(String ecosObject, String rosterAtt){
        _ecosObject=ecosObject;
        //We see if there is a matching roster entry with out object against it
        //if so we add the rosterId to the ecoclocoaddress entry.
        List<RosterEntry> l = Roster.instance().getEntriesWithAttributeKeyValue(rosterAtt, ecosObject);
        //It should be unique
        if (l.size()>0){
           _rosterId = l.get(0).getId();
        }
    }
    
    public void setCV8(String cv8){
        _cv8=cv8;
    }
    
    public String getCV8(){
        return _cv8;
    }
    
    public void setCV7(String cv7){
        _cv7=cv7;
    }
    
    public String getCV7(){
        return _cv7;
    }
    
    public void setEcosLocoAddress(int dCCAddress){
        _dccAddress=dCCAddress;
    }
    
    /**
     * @return the loco address configured on the ECOS for this loco
     */
    public int getEcosLocoAddress(){
        return _dccAddress;
    }
    /**
     * @return the loco object as a string on the ECOS for this loco
     */
    public String getEcosObject(){
        return _ecosObject;
    }
    /**
     * @return the loco object as a integer on the ECOS for this loco
     */    
    public int getEcosObjectAsInt(){
        return Integer.parseInt(_ecosObject);
    }

    public void doNotAddToRoster(){
        doNotAddToRoster=true;
    }
    
    public void allowAddToRoster(){
        doNotAddToRoster=false;
    }

    public boolean addToRoster(){
        return !doNotAddToRoster;
    }
    
    protected void setSpeed(int speed){
        int oldspeed = currentSpeed;
        currentSpeed = speed;
        firePropertyChange("Speed", oldspeed, currentSpeed);
    }
    
    public int getSpeed() { return currentSpeed; }
    protected void setDirection(String line){
        setDirection(getDirection(line));
    }
    
    protected void setDirection(boolean dir){
        boolean olddir = direction;
        direction = dir;
        firePropertyChange("Direction", olddir, direction);
    }
    
    public boolean getDirection() { return direction; }
    
    public String getDirectionAsString() {
        if(direction)
            return "Forward";
        return "Reverse";
    }
    

    //Should this option be made public? should setting the object only be available when the Loco is created.
    //It needs a bit of a re-think!
    //It is set this way because of adhoc locos being created for throttles.
    public void setEcosObject(String ecosObject){
        _ecosObject = ecosObject;
    }

    /**
     * @return the loco object description held on the ECOS for this loco
     */
    public String getEcosDescription(){
        return _ecosDescription;
    }

    public void setEcosDescription(String description){
        String oldValue = _ecosDescription;
        _ecosDescription = description;
        firePropertyChange("name", oldValue, _ecosDescription);
    }    
    
    /**
     * @return the JMRI Roster ID for this loco
     */
    public String getRosterId(){
        return _rosterId;
    }

    public void setRosterId(String roster){
        String oldValue = _rosterId;
        _rosterId=roster;
        firePropertyChange("RosterId", oldValue, _rosterId);
    }    
    //Protocol is here as it is a potential value from the ecos
    // We may not actually use it.
    public String getProtocol(){
        return _protocol;
    }
    //@TODO Need to udate this to return the new Protocol option from LocoAddress
    public int getSpeedStepMode(){
        if(_protocol.equals("DCC128"))
            return DccThrottle.SpeedStepMode128;
        if(_protocol.equals("DCC28"))
            return DccThrottle.SpeedStepMode28;
        if(_protocol.equals("DCC14"))
            return DccThrottle.SpeedStepMode14;
        if(_protocol.equals("MM14"))
            return DccThrottle.SpeedStepMode14;
        if(_protocol.equals("MM28"))
            return DccThrottle.SpeedStepMode28;
        //ESU does also support MM27, SX32, MMFKT, not sure how these shouldbe handled
        return DccThrottle.SpeedStepMode128;
    }

    public void setProtocol(String protocol){
        String oldValue = _protocol;
        _protocol = protocol;
        firePropertyChange("protocol", oldValue, _protocol);
        
    }   
    
    /*
    The Temporary Entry Field is used to determine if JMRI has had to create the entry on an ad-hoc basis
    for the throttle.  If this is set to True, the throttle can evaluate this field to determine if the 
    loco should be removed from the Ecos Database when closing the application.
    */
    
    boolean _tempEntry = false;
    
    public void setEcosTempEntry(boolean boo){
        _tempEntry = boo;
    }
    
    public boolean getEcosTempEntry(){
        return _tempEntry;
    }
    
    public void reply(EcosReply m) {
        String msg = m.toString();
        if (m.getResultCode()!=0) return; //The result is not valid therefore we can not set it.
        if (msg.startsWith("<REPLY get("+_ecosObject+",") || msg.startsWith("<EVENT "+_ecosObject+">")) {
            if (msg.contains("speed")){
                setSpeed(Integer.parseInt(EcosReply.getContentDetails(msg, "speed")));
            }
            if (msg.contains("dir")){
                setDirection(getDirection(msg));
            }
            if (msg.contains("protocol")){
                setProtocol(EcosReply.getContentDetails(msg, "protocol"));
            }
            if (msg.contains("name")){
                String name = EcosReply.getContentDetails(msg, "name").trim();
                setEcosDescription(name);
            }
        }
    }

    boolean getDirection(String line){
        boolean newDirection = false;
        if (EcosReply.getContentDetails(line, "dir").equals("0")) newDirection=true;
        return newDirection;
    }
    
    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //public void firePropertyChange(String propertyName, Object oldValue, Object newValue);
    // _once_ if anything has changed state

    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    public void dispose() {
        pcs = null;
    }
}