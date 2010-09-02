// EcosPreferences.java

package jmri.jmrix.ecos;

import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;

/**
 * Stores User Preferences on how to deal with syncronising the Ecos Database
 * with JMRI.
 *
 * @author	Kevin Dickerson  Copyright (C) 2009
 * @version     $Revision: 1.27 $
 */

public class EcosPreferences {
 
    public EcosPreferences(){
        //instance();
        if (jmri.InstanceManager.configureManagerInstance()!=null) {
            jmri.InstanceManager.configureManagerInstance().registerConfig(this);
        }
         
        
        
        if (ecosPreferencesShutDownTask==null) {
            ecosPreferencesShutDownTask = new QuietShutDownTask("Ecos Preferences Shutdown") {
                @Override
                public boolean doAction(){
                    if (getChangeMade()){
                        jmri.InstanceManager.configureManagerInstance().storePrefs();
                    }
                    return true;
                }
            };
            if (jmri.InstanceManager.shutDownManagerInstance() !=null){
                jmri.InstanceManager.shutDownManagerInstance().register(ecosPreferencesShutDownTask);
            }
        }
        self = this;
    }
    
    ShutDownTask ecosPreferencesShutDownTask = null;

    private static final int ASK = 0x00; // ie always ask the question
    @SuppressWarnings("unused")
    private static final int NO = 0x01; //ie never do the operation
    @SuppressWarnings("unused")
    private static final int YES = 0x02; //ie always perform the operation
    
    private boolean _changeMade = false;
    
    public boolean getChangeMade(){ return _changeMade; }
    //The reset is used after the preferences have been loaded for the first time
    public void resetChangeMade(){ _changeMade = false; }
     /**
     * Stores the users preferance for when a loco is created in the Roster
     * should it also be created in the ECOS, if it does not exist.
     * Currently not implemented.
     */
     
    private int _addlocotoecos = ASK;
    
    public int getAddLocoToEcos(){
        return _addlocotoecos;
    }
    
    public void setAddLocoToEcos(int boo){
        _addlocotoecos = boo;
        _changeMade = true;
    }

     /**
     * Stores the users preferance if a loco has been created on the Ecos,
     * should an entry in the JMRI Roster be created.
     * Currently not implemented.
     */
     
    private int _addlocotojmri = ASK;
    
    public int getAddLocoToJMRI(){
        return _addlocotojmri;
    }
    
    public void setAddLocoToJMRI(int boo){
        _addlocotojmri = boo;
        _changeMade = true;
    }
    
     /**
     * Stores the users preferance on how the ecos loco description,
     * should be formated.
     * Currently not implemented
     */    
    
    private String _ecoslocodescription = null;
    
    public String getEcosLocoDescription(){
        return _ecoslocodescription;
    }
    
    public void setEcosLocoDescription(String descript){
        _ecoslocodescription = descript;
        _changeMade = true;
    }
    
     /**
     * If there is a conflict in loco information between the Ecos and JMRI,
     * this determines which system wins.
     * Currently not implemented.
     */
     
    private static final int NOSYNC     = 0x00;
    private static final int WARN       = 0x01;
    private static final int JMRI       = 0x02;
    private static final int ECOS       = 0x03;
    
    private int _locomaster = 0x00;
    
    public int getLocoMaster(){
        return _locomaster;
    }
    
    public void setLocoMaster(int master){
        _locomaster = master;
    }
    
    public void setLocoMaster(String master){
        if (master.equals("NOSYNC")) _locomaster = NOSYNC;
        else if (master.equals("WARN")) _locomaster = WARN;
        else if (master.equals("JMRI")) _locomaster = JMRI;
        else if (master.equals("ECOS")) _locomaster = ECOS;
        else _locomaster = NOSYNC;
        _changeMade = true;
    }
    
    public String getLocoMasterAsString(){
        String result;
        switch(_locomaster){
            case 0x00 : result = "NOSYNC"; break;
            case 0x01 : result = "WARN"; break;
            case 0x02 : result = "JMRI"; break;
            case 0x03 : result = "ECOS"; break;
            default : result = "NOSYNC"; break;
            }
        return result;
    }
 
    /**
    * Stores the users preferance if a loco has been created ad-hoc,
    * on the Throttle, should the entry created for it in the ECOS be deleted.
    * Currently not implemented.
    */
     
    private int _adhoclocofromecos = ASK;
    
    //0x00 - always ask
    //0x01 - always leave loco
    //0x02 - always remove loco
     
    public int getAdhocLocoFromEcos(){
        return _adhoclocofromecos;
    }
    
    public void setAdhocLocoFromEcos(int boo){
        _adhoclocofromecos = boo;
        _changeMade = true;
    }
    
    /**
    * Stores the users preferance to deal with if another device has control over the loco
    */
     
    private int _forcecontrolfromecos = ASK;
    
    //0x00 - always ask
    //0x01 - always always fail
    //0x02 - always force control
     
    public int getForceControlFromEcos(){
        return _forcecontrolfromecos;
    }
    
    public void setForceControlFromEcos(int boo){
        _forcecontrolfromecos = boo;
        _changeMade = true;
    }

        /**
    * Stores the users preferance if a loco has been created ad-hoc,
    * on the Throttle, should the entry created for it in the ECOS be deleted.
    * Currently not implemented.
    */

    private String _defaultecosprotocol = "DCC128";

    public String getDefaultEcosProtocol(){
        return _defaultecosprotocol;
    }

    public void setDefaultEcosProtocol(String boo){
        _defaultecosprotocol = boo;
        _changeMade = true;
    }

    /**
    * Stores the users preferance for deleting a loco from the roster should it,
    * also be deleted from the ECOS.
    * Currently not implemented.
    */

    //0x00 - always ask
    //0x01 - always leave loco
    //0x02 - always remove loco
      
    private int _removelocofromecos = ASK;
    
    public int getRemoveLocoFromEcos(){
        return _removelocofromecos;
    }
    
    public void setRemoveLocoFromEcos(int boo){
        _removelocofromecos = boo;
        _changeMade = true;
    }
    
    /**
    * Stores the users preferance for deleting a loco from the ECOS should it,
    * also be deleted from the JMRI Roster.
    * Currently not implemented.
    */
    
    private int _removelocofromjmri = ASK;

    public int getRemoveLocoFromJMRI(){
        return _removelocofromjmri;
    }
    
    public void setRemoveLocoFromJMRI(int boo){
        _removelocofromjmri = boo;
        _changeMade = true;
    }

    /**
    * Stores the users preferance when creating a turnout in JMRI should it
    * also be created on the ECOS.
    * Currently not implemented.
    */    
    private int _addturnoutstoecos = ASK;
    
    public int getAddTurnoutsToEcos(){
        return _addturnoutstoecos;
    }
    
    public void setAddTurnoutsToEcos(int boo){
        _addturnoutstoecos = boo;
        _changeMade = true;
    }
    
    /**
    * Stores the users preferance when a new turnout is created on the ECOS should it
    * also be created in JMRI.
    * Currently not implemented.
    */
    
    private int _addturnoutstojmri = ASK;

    public int getAddTurnoutsToJMRI(){
        return _addturnoutstojmri;
    }
    
    public void setAddTurnoutsToJMRI(int boo){
        _addturnoutstojmri = boo;
        _changeMade = true;
    }

    /**
    * Stores the users preferance when a new turnout is removed from the ECOS should it
    * also be removed from JMRI..
    * Currently not implemented.
    */
    
    private int _removeturnoutsfromjmri = ASK;

    public int getRemoveTurnoutsFromJMRI(){
        return _removeturnoutsfromjmri;
    }
    
    public void setRemoveTurnoutsFromJMRI(int boo){
        _removeturnoutsfromjmri = boo;
        _changeMade = true;
    }
    
    /**
    * Stores the users preferance when a new turnout is removed from JMRI should it
    * also be removed from the ECOS.
    * Currently not implemented.
    */    
    private int _removeturnoutsfromecos = ASK;
    
    public int getRemoveTurnoutsFromEcos(){
        return _removeturnoutsfromecos;
    }
    
    public void setRemoveTurnoutsFromEcos(int boo){
        _removeturnoutsfromecos = boo;
        _changeMade = true;
    }
/*    public void setInstance(){

    }*/
    public String name(){
        return null;
    }
    /*static public synchronized EcosPreferences instance() {
       if (_instance == null) _instance = new EcosPreferences();
        return _instance; 
    }
    private EcosPreferences _instance = null;*/

    static public EcosPreferences instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new EcosPreferences object");
            self = new EcosPreferences();
            // set as command station too
        }
        return self;
    }

    static private EcosPreferences self = null;
    private void setInstance() { self = this; }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosPreferences.class.getName());

 }
 
