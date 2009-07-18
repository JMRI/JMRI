/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmri.jmrix.ecos;
import java.util.*;
//import jmri.implementation.AbstractManager;

/**
 *
 * @author Kevin Dickerson
 */
public class EcosLocoAddressManager implements java.beans.PropertyChangeListener, EcosListener{

    protected static Hashtable <Integer, EcosLocoAddress> _tecos = new Hashtable<Integer, EcosLocoAddress>();   // stores known Ecos Object ids to DCC
    protected static Hashtable <Integer, EcosLocoAddress> _tdcc = new Hashtable<Integer, EcosLocoAddress>();

    public char systemLetter() { return 'U'; }
    public char typeLetter() { return 'Z'; }

    private EcosLocoAddress l;
    private boolean wait = false;

    public EcosLocoAddressManager(){
         if (jmri.InstanceManager.configureManagerInstance()!=null) {
            jmri.InstanceManager.configureManagerInstance().registerConfig(this);
         }
        tc = EcosTrafficController.instance();
        tc.addEcosListener(this);
        // ask to be notified
        // We won't look to add new locos created on the ecos yet this can be added in at a later date.
        /*EcosMessage m = new EcosMessage("request(10, view)");
        tc.sendEcosMessage(m, this);*/

        EcosMessage m = new EcosMessage("queryObjects(10, addr)");
        tc.sendEcosMessage(m, this);

        //if (mInstance!=null) log.warn("Creating too many objects");
        //mInstance = this;


    }
    EcosTrafficController tc;

    public EcosLocoAddress provideEcosLoco(int EcosObject, int DCCAddress) {
        EcosLocoAddress l;
        l = getByEcosObject(EcosObject);
        if (l!=null) return l;
        l = new EcosLocoAddress(DCCAddress);
        l.setEcosObject(EcosObject);
        register(l);
        return l;
    }
    public EcosLocoAddress provideByDccAddress(int dccAddress) {
        //EcosLocoAddress l;
        l = getByDccAddress(dccAddress);
        //Loco doesn't exist, so we sshall create it.
        if (l!=null) return l;
        
        l = new EcosLocoAddress(dccAddress);
        register(l);
        return (EcosLocoAddress)_tdcc.get(dccAddress);
    }


    public EcosLocoAddress getByEcosObject(int ecosObject) { 
		return (EcosLocoAddress)_tecos.get(ecosObject);
    }
    
    public EcosLocoAddress getByDccAddress(int dccAddress) { 
		return (EcosLocoAddress)_tdcc.get(dccAddress);
    }

    static EcosLocoAddressManager _instance = null;
    static public EcosLocoAddressManager instance() {
        if (_instance == null) {
            _instance = new EcosLocoAddressManager();
        }
        return (_instance);
    }

    public void register(EcosLocoAddress s) {
        int ecosObject = s.getEcosObject();
        _tecos.put(ecosObject, s);
        //String userName = s.getUserName();
        //if (userName != null)
        int dccAddress = s.getDCCAddress();
        _tdcc.put(dccAddress, s);
        firePropertyChange("length", null, new Integer(_tecos.size()));
        firePropertyChange("length", null, new Integer(_tdcc.size()));
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this);
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager
     * uses this method.
     */
    public void deregister(EcosLocoAddress s) {
        s.removePropertyChangeListener(this);
        int ecosObject = s.getEcosObject();
        _tecos.remove(ecosObject);
        int dccAddress = s.getDCCAddress();
        _tdcc.remove(dccAddress);
        firePropertyChange("length", null, new Integer(_tecos.size()));
        firePropertyChange("length", null, new Integer(_tdcc.size()));
        // listen for name and state changes to forward
    }
    
    
    public void dispose() {
        if (jmri.InstanceManager.configureManagerInstance()!= null)
            jmri.InstanceManager.configureManagerInstance().deregister(this);
        _tecos.clear();
        _tdcc.clear();
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

        /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {

    }

    public void reply(EcosReply m) {
    // is this a list of turnouts?
        String msg = m.toString();
      //  System.out.println(msg);
        //This needs restructuring it processes the list of locos
        if (msg.contains("<REPLY queryObjects(10, addr)>")) {
            //System.out.println("it is the response to our request");
            String[] lines = msg.split("\n");
            log.debug("found "+(lines.length-2)+" Loco objects");
            for (int i = 1; i<lines.length-1; i++) {
                if (lines[i].contains("addr[")) { // skip odd lines
                    int start = 0;
                    int end = lines[i].indexOf(' ');
                    int object = Integer.parseInt(lines[i].substring(start, end));

                    if ( (1000<=object) && (object<2000)) {
                        start = lines[i].indexOf('[')+1;
                        end = lines[i].indexOf(']');
                        int addr = Integer.parseInt(lines[i].substring(start, end));
                        provideEcosLoco(object,addr);
                        //May want to get current status of the loco??
                    }

                 }
            }
        }
        else if (msg.contains("<REPLY create(10, addr")){
            String[] lines = msg.split("\n");
            log.debug("Found" +(lines.length-2));
            for(int i =1; i<lines.length-1; i++) {
                if(lines[i].contains("10 id[")){
                    int start = lines[i].indexOf("[")+1;
                    int end = lines[i].indexOf("]");
                    int EcosAddr = Integer.parseInt(lines[i].substring(start, end));
                    l.setEcosObject(EcosAddr);
                    register(l);
                   // System.out.println(EcosAddr);
                }
            }
        }
    }
    public void message(EcosMessage m){
        
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoAddressManager.class.getName());

}
