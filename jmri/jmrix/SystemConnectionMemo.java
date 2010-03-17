// SystemConnectionMemo.java

package jmri.jmrix;
import java.util.ArrayList;
/**
 * Lightweight abstract class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.5 $
 */
abstract public class SystemConnectionMemo {

    protected SystemConnectionMemo(String prefix, String userName) {
        
        if(!setSystemPrefix(prefix)){
            for (int x = 2; x<50; x++){
                if(setSystemPrefix(prefix+x)){
                    break;
                }
            }
        }

        if(!setUserName(userName)){
            for (int x = 2; x<50; x++){
                if(setUserName(userName+x)){
                    break;
                }
            }
        }
        //Adds and registered an internal connection to the system.
        if (!this.getClass().getName().equals("jmri.jmrix.internal.InternalSystemConnectionMemo")){
            java.util.List<Object> list 
                    = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
            
            if (list != null){
                boolean internalSetup = false;
                for (Object memo : list) {
                    if (((jmri.jmrix.SystemConnectionMemo)memo).getClass().getName().equals("jmri.jmrix.internal.InternalSystemConnectionMemo"))
                        internalSetup = true;
                }
                if (!internalSetup){
                    jmri.jmrix.internal.InternalSystemConnectionMemo memo = new jmri.jmrix.internal.InternalSystemConnectionMemo();
                    memo.configureManagers();
                }
            }
        }
    }
    
    private static ArrayList<String> userNames = new ArrayList<String>();
    private static ArrayList<String> sysPrefixes = new ArrayList<String>();
    
    //This should probably throwing an exception
    private static boolean addUserName(String userName){
        if(userNames!=null){
            if (userNames.contains(userName))
                return false;
        }
        userNames.add(userName);
        return true;
    }
    
    //This should probably throwing an exception
    private static boolean addSystemPrefix(String systemPrefix){
        if(sysPrefixes!=null){
            if (sysPrefixes.contains(systemPrefix))
                return false;
        }
        sysPrefixes.add(systemPrefix);
        return true;
    }
    
    private static void removeUserName(String userName){
        if(userNames!=null){
            if (userNames.contains(userName)){
                int index = userNames.indexOf(userName);
                userNames.remove(index);
            }
        }
    }
    
    private static void removeSystemPrefix(String systemPrefix){
        if(sysPrefixes!=null){
            if (sysPrefixes.contains(systemPrefix)){
                int index = sysPrefixes.indexOf(systemPrefix);
                sysPrefixes.remove(index);
            }
        }
    }

    /**
     * Store in InstanceManager with 
     * proper ID for later retrieval as a 
     * generic system
     */
    public void register() {
        jmri.InstanceManager.store(this, SystemConnectionMemo.class);
    }
    
    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    public String getSystemPrefix() { return prefix; }
    private String prefix;
    //This should probably throwing an exception
    public boolean setSystemPrefix(String systemPrefix) {
        if (systemPrefix.equals(this.prefix)) {
            return true;
        }
        String oldPrefix = this.prefix;
        if(addSystemPrefix(systemPrefix)){
            this.prefix = systemPrefix;
            removeSystemPrefix(oldPrefix);
            return true;
        }
        return false;
        //this.prefix = prefix;
    }
    
    /**
     * Provides access to the system user name string.
     * This was previously fixed at configuration time.
     */
    public String getUserName() { return userName; }
    private String userName;
    //This should probably throwing an exception
    public boolean setUserName(String name) {
        if (name.equals(userName))
            return true;
        String oldUserName = this.userName;
        if(addUserName(name)){
            this.userName = name;
            removeUserName(oldUserName);
            return true;
        }
        return false;
    }
    
    public void dispose(){
        removeUserName(userName);
        removeSystemPrefix(prefix);
        jmri.InstanceManager.deregister(this, SystemConnectionMemo.class);
    }
        
}


/* @(#)SystemConnectionMemo.java */
