package jmri.jmrix.ecos;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.util.List;
/**
 * Stores all the loco information from the Ecos into JMRI
 *
 * @author Kevin Dickerson
 * @version     $Revision: 1.4 $
 */
public class EcosLocoAddress {
    private String _ecosObject = null;
    private int _dccAddress = 0;
    private String _ecosDescription = null;
    private String _rosterId = null;
    private String _protocol = null;
    private String _cv8 = null;
    private String _cv7 = null;
    private boolean doNotAddToRoster = false;
    

    public EcosLocoAddress(int dCCAddress){
        _dccAddress=dCCAddress;
    }
    
    public EcosLocoAddress(String ecosObject){
        _ecosObject=ecosObject;
        //We see if there is a matching roster entry with out object against it
        //if so we add the rosterId to the ecoclocoaddress entry.
        List<RosterEntry> l = Roster.instance().getEntriesWithAttributeKeyValue("EcosObject", ecosObject);
        //It should be unique
        if (l.size()==1){
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
    
    public int getEcosLocoAddress(){
        return _dccAddress;
    }

    public String getEcosObject(){

        return _ecosObject;
    }

    public void doNotAddToRoster(){
        doNotAddToRoster=true;
    }

    public boolean addToRoster(){
        return !doNotAddToRoster;
    }

    //Should this option be made public? should setting the object only be available when the Loco is created.
    //It needs a bit of a re-think!
    //It is set this way because of adhoc locos being created for throttles.
    public void setEcosObject(String ecosObject){
        _ecosObject = ecosObject;
    }

    public String getEcosDescription(){
        return _ecosDescription;
    }

    public void setEcosDescription(String description){
        _ecosDescription = description;
    }    
    
    public String getRosterId(){
        return _rosterId;
    }

    public void setRosterId(String roster){
        firePropertyChange("RosterId", _rosterId, roster);
        _rosterId = roster;
    }    
    //Protocol is here as it is a potential value from the ecos
    // We may not actually use it.
    public String getProtocol(){
        return _protocol;
    }

    public void setProtocol(String protocol){
        _protocol = protocol;
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
