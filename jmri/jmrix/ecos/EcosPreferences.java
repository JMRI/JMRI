// EcosPreferences.java

package jmri.jmrix.ecos;

/**
 * Stores User Preferences on how to deal with syncronising the Ecos Database
 * with JMRI.
 *
 * @author	Kevin Dickerson  Copyright (C) 2009
 * @version     $Revision: 1.22 $
 */

public class EcosPreferences {
 
    public EcosPreferences(){
       // instance();
        /*if (jmri.InstanceManager.configureManagerInstance()!=null) {
            jmri.InstanceManager.configureManagerInstance().registerConfig(this);
         }*/
    }
    
     /**
     * Stores the users preferance for when a loco is created in the Roster
     * should it also be created in the ECOS, if it does not exist.
     * Currently not implemented.
     */
     
    protected static boolean _addlocotoecos = false;
    
    public boolean getAddLocoToEcos(){
        return _addlocotoecos;
    }
    
    public void setAddLocoToEcos(boolean boo){
        _addlocotoecos = boo;
    }

     /**
     * Stores the users preferance if a loco has been created on the Ecos,
     * should an entry in the JMRI Roster be created.
     * Currently not implemented.
     */
     
    protected static boolean _addlocotojmri = false;
    
    public boolean getAddLocoToJMRI(){
        return _addlocotojmri;
    }
    
    public void setAddLocoToJMRI(boolean boo){
        _addlocotojmri = boo;
    }
    
     /**
     * Stores the users preferance on how the ecos loco description,
     * should be formated.
     * Currently not implemented
     */    
    
    protected static String _ecoslocodescription = null;
    
    public String getEcosLocoDescription(){
        return _ecoslocodescription;
    }
    
    public void setEcosLocoDescription(String descript){
        _ecoslocodescription = descript;
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
    
    protected static int _locomaster = 0x00;
    
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
     
    protected static int _adhoclocofromecos = 0x00;
     
    public int getAdhocLocoFromEcos(){
        return _adhoclocofromecos;
    }
    
    public void setAdhocLocoFromEcos(int boo){
        _adhoclocofromecos = boo;
    }

        /**
    * Stores the users preferance if a loco has been created ad-hoc,
    * on the Throttle, should the entry created for it in the ECOS be deleted.
    * Currently not implemented.
    */

    protected static String _defaultecosprotocol = "DCC128";

    public String getDefaultEcosProtocol(){
        return _defaultecosprotocol;
    }

    public void setDefaultEcosProtocol(String boo){
        _defaultecosprotocol = boo;
    }

    /**
    * Stores the users preferance for deleting a loco from the roster should it,
    * also be deleted from the ECOS.
    * Currently not implemented.
    */
      
    protected static boolean _removelocofromecos = false;
    
    public boolean getRemoveLocoFromEcos(){
        return _removelocofromecos;
    }
    
    public void setRemoveLocoFromEcos(boolean boo){
        _removelocofromecos = boo;
    }
    
    /**
    * Stores the users preferance for deleting a loco from the ECOS should it,
    * also be deleted from the JMRI Roster.
    * Currently not implemented.
    */
    
    protected static boolean _removelocofromjmri = false;

    public boolean getRemoveLocoFromJMRI(){
        return _removelocofromjmri;
    }
    
    public void setRemoveLocoFromJMRI(boolean boo){
        _removelocofromjmri = boo;
    }

    /**
    * Stores the users preferance when creating a turnout in JMRI should it
    * also be created on the ECOS.
    * Currently not implemented.
    */    
    protected static boolean _addturnoutstoecos = false;
    
    public boolean getAddTurnoutsToEcos(){
        return _addturnoutstoecos;
    }
    
    public void setAddTurnoutsToEcos(boolean boo){
        _addturnoutstoecos = boo;
    }
    
    /**
    * Stores the users preferance when a new turnout is created on the ECOS should it
    * also be created in JMRI.
    * Currently not implemented.
    */
    
    protected static boolean _addturnoutstojmri = false;

    public boolean getAddTurnoutsToJMRI(){
        return _addturnoutstojmri;
    }
    
    public void setAddTurnoutsToJMRI(boolean boo){
        _addturnoutstojmri = boo;
    }

    /**
    * Stores the users preferance when a new turnout is removed from the ECOS should it
    * also be removed from JMRI..
    * Currently not implemented.
    */
    
    protected static boolean _removeturnoutsfromjmri = false;

    public boolean getRemoveTurnoutsFromJMRI(){
        return _removeturnoutsfromjmri;
    }
    
    public void setRemoveTurnoutsFromJMRI(boolean boo){
        _removeturnoutsfromjmri = boo;
    }
    
    /**
    * Stores the users preferance when a new turnout is removed from JMRI should it
    * also be removed from the ECOS.
    * Currently not implemented.
    */    
    protected static boolean _removeturnoutsfromecos = false;
    
    public boolean getRemoveTurnoutsFromEcos(){
        return _removeturnoutsfromecos;
    }
    
    public void setRemoveTurnoutsFromEcos(boolean boo){
        _removeturnoutsfromecos = boo;
    }
/*    public void setInstance(){

    }*/
    public String name(){
        return null;
    }
/*    static public synchronized EcosPreferences instance() {
       if (_instance == null) _instance = new EcosPreferences();
        return _instance; 
    }
    protected static EcosPreferences _instance = null;*/

        static public EcosPreferences instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new EcosPreferences object");
            self = new EcosPreferences();
            // set as command station too
        }
        return self;
    }

    static protected EcosPreferences self = null;
    protected void setInstance() { self = this; }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosPreferences.class.getName());

 }
 
