package jmri.jmrix.ecos;

/**
 * Stores all the loco information from the Ecos into JMRI
 *
 * @author Kevin Dickerson
 * @version     $Revision: 1.2 $
 */
public class EcosLocoAddress {
    String _ecosObject = null;
    int _dccAddress = 0;
    String _ecosDescription = null;
    String _rosterId = null;
    String _protocol = null;
    String _cv8 = null;
    String _cv7 = null;
    

    public EcosLocoAddress(int dCCAddress){
        _dccAddress=dCCAddress;
    }
    
    public EcosLocoAddress(String ecosObject){
        _ecosObject=ecosObject;
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
    loco should be removed from the Ecos Database when it has been released.
    
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
    //		public void firePropertyChange(String propertyName,
    //					       	Object oldValue,
    //						Object newValue)
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
