
package jmri.jmrix.ecos;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * Managers the Ecos Loco entries within JMRI.
 * @author Kevin Dickerson
 * @version     $Revision: 1.2 $
 */
public class EcosLocoAddressManager implements java.beans.PropertyChangeListener, EcosListener{

    protected static Hashtable <String, EcosLocoAddress> _tecos = new Hashtable<String, EcosLocoAddress>();   // stores known Ecos Object ids to DCC
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

        EcosMessage m = new EcosMessage("queryObjects(10, addr, name)");
        tc.sendEcosMessage(m, this);
        
        //We want to keep an eye on locos being created on the ecos, even if they are done so by JMRI.
        //Not sure what is going on here need to look at it properly
        m = new EcosMessage("request(10, view)");
        tc.sendEcosMessage(m, this);

        //if (mInstance!=null) log.warn("Creating too many objects");
        //mInstance = this;


    }
    EcosTrafficController tc;

    public EcosLocoAddress provideEcosLoco(String EcosObject, int DCCAddress) {
        //EcosLocoAddress l;
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
        //Loco doesn't exist, so we shall create it.
        if (l!=null) return l;
        
        l = new EcosLocoAddress(dccAddress);
        register(l);
        return (EcosLocoAddress)_tdcc.get(dccAddress);
    }
    
    public EcosLocoAddress provideByEcosObject(String ecosObject) {
        //EcosLocoAddress l;
        l = getByEcosObject(ecosObject);
        //Loco doesn't exist, so we shall create it.
        if (l!=null) return l;
        
        l = new EcosLocoAddress(ecosObject);
        register(l);
        return (EcosLocoAddress)_tecos.get(ecosObject);
    }

    public EcosLocoAddress getByEcosObject(String ecosObject) { 
		return (EcosLocoAddress)_tecos.get(ecosObject);
    }
    
    public EcosLocoAddress getByDccAddress(int dccAddress) { 
		return (EcosLocoAddress)_tdcc.get(dccAddress);
    }
    
    public String[] getEcosObjectArray() {
        String[] arr = new String[_tecos.size()];
        Enumeration<String> en = _tecos.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    }

    public List<String> getEcosObjectList() {
        String[] arr = new String[_tecos.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _tecos.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }

    protected static EcosLocoAddressManager _instance = null;
    static public synchronized EcosLocoAddressManager instance() {
        if (_instance == null) {
            _instance = new EcosLocoAddressManager();
        }
        return (_instance);
    }

    public void register(EcosLocoAddress s) {
        //We should always have at least a DCC address to register a loco.
        //We may not always first time round on initial registration have the Ecos Object.
        String ecosObject = s.getEcosObject();

        if(ecosObject !=null){
            _tecos.put(ecosObject, s);
        }

        int dccAddress = s.getEcosLocoAddress();
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
        String ecosObject = s.getEcosObject();
        _tecos.remove(ecosObject);
        int dccAddress = s.getEcosLocoAddress();
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
    // is this a list of Locos?
        int startval;
        int endval;
        int addr;
        String description;
        String protocol;
        String strde;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        log.debug("found "+(lines.length)+" response from Ecos");
        if (lines[lines.length-1].contains("<END 0 (OK)>")){
            //This needs restructuring it processes the list of locos
            if (lines[0].startsWith("<REPLY queryObjects(10")){
                //First line is the response header, the last line is the OK
                for (int i = 1; i<lines.length-1; i++) {
                    if (lines[i].contains("addr[")) { // skip odd lines
                        String[] objectdetail = lines[i].split(" ");
                        EcosLocoAddress tmploco;
                        //The first part of the messages is always the object id.
                        strde = objectdetail[0];
                        int object = Integer.parseInt(strde);
                        if ( (1000<=object) && (object<2000)) {
                            tmploco = provideByEcosObject(strde);
                            getEcosCVs(tmploco);
                        } else return;
                        if (lines[i].contains("addr")){
                            startval=lines[i].indexOf("addr[")+5;
                            endval=(lines[i].substring(startval)).indexOf("]")+startval;
                            addr = Integer.parseInt(lines[i].substring(startval, endval));
                            tmploco.setEcosLocoAddress(addr);
                        }
                        if (lines[i].contains("name")){
                            startval=lines[i].indexOf("name[")+6;
                            endval=(lines[i].substring(startval)).indexOf("]")+startval-1;
                            description=lines[i].substring(startval, endval);
                            tmploco.setEcosDescription(description);
                        }
                        if (lines[i].contains("protocol")){
                            startval=lines[i].indexOf("protocol[")+9;
                            endval=(lines[i].substring(startval)).indexOf("]")+startval;
                            protocol=lines[i].substring(startval, endval);
                            tmploco.setProtocol(protocol);
                        }
                        register(tmploco);

                     }
                }
            }
            //Don't think that the reply create bit is used here, need to check!
            else if (lines[0].startsWith("<REPLY create(10, addr")){
                for(int i =1; i<lines.length-1; i++) {
                    if(lines[i].contains("10 id[")){
                        int start = lines[i].indexOf("[")+1;
                        int end = lines[i].indexOf("]");
                        String EcosAddr = lines[i].substring(start, end);
                        l.setEcosObject(EcosAddr);
                        register(l);
                    }
                }
            }
            //Need to really check if this all fits together correctly!  Might need to get the loco id from the reply string to
            //identify the loco correctly
            else if (lines[0].startsWith("<REPLY get(")){
                startval=lines[0].indexOf("(")+1;
                endval=(lines[0].substring(startval)).indexOf(",")+startval;
                EcosLocoAddress tmploco;
                //The first part of the messages is always the object id.
                Integer.parseInt(lines[0].substring(startval, endval));
                int object = Integer.parseInt(lines[0].substring(startval, endval));
                if ( (1000<=object) && (object<2000)) {
                    tmploco = provideByEcosObject(lines[0].substring(startval, endval));
                } else return;
                for(int i =1; i<lines.length-1; i++) {
                    if(lines[i].contains("cv[8")){
                        int start = lines[i].indexOf(", ")+2;
                        int end = lines[i].indexOf("]");
                        tmploco.setCV8(lines[i].substring(start, end));
                    }
                    if(lines[i].contains("cv[7")){
                        int start = lines[i].indexOf(", ")+2;
                        int end = lines[i].indexOf("]");
                        tmploco.setCV7(lines[i].substring(start, end));
                    }

                }
            }
        }
    }
    public void message(EcosMessage m){
        
    }
    /*
    *The purpose of this is to get some of the basic cv details that are required
    *for selecting the decoder mfg and family in the roster file.
    *This might work as sending a single request rather than multiple.
    */
    private void getEcosCVs(EcosLocoAddress tmploco){
        tc = EcosTrafficController.instance();
        tc.addEcosListener(this);
        // ask to be notified
        // We won't look to add new locos created on the ecos yet this can be added in at a later date.

        EcosMessage m = new EcosMessage("get("+tmploco.getEcosObject()+", cv[7])");
        tc.sendEcosMessage(m, this);
        
        m = new EcosMessage("get("+tmploco.getEcosObject()+", cv[8])");
        tc.sendEcosMessage(m, this);
        
        
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoAddressManager.class.getName());

}
