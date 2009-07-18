/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmri.jmrix.ecos;

/**
 *
 * @author Kevin Dickerson
 */
public class EcosLocoAddress {
    int _ecosObject = 0;
    int _dccAddress = 0;

    public EcosLocoAddress(int dCCAddress){

        _dccAddress=dCCAddress;
    }

    public int getEcosObject(){

        return _ecosObject;
    }

    public int getDCCAddress(){
        return _dccAddress;
    }

    public void setDCCAddress(int dCCAddress){
        _dccAddress = dCCAddress;
    }

   public void setEcosObject(int ecosObject){
        _ecosObject = ecosObject;
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
