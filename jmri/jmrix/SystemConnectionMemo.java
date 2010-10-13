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
 * @version             $Revision: 1.11 $
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
    }
    
    final protected static ArrayList<String> userNames = new ArrayList<String>();
    final protected static ArrayList<String> sysPrefixes = new ArrayList<String>();
    
    private synchronized static boolean addUserName(String userName){      
        if (userNames.contains(userName))
            return false;

        userNames.add(userName);
        return true;
    }
    
    //This should probably throwing an exception
    private synchronized static boolean addSystemPrefix(String systemPrefix){
        if (sysPrefixes.contains(systemPrefix))
            return false;

        sysPrefixes.add(systemPrefix);
        return true;
    }
    
    private synchronized static void removeUserName(String userName){
        if(userNames!=null){
            if (userNames.contains(userName)){
                int index = userNames.indexOf(userName);
                userNames.remove(index);
            }
        }
    }
    
    private synchronized static void removeSystemPrefix(String systemPrefix){
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
        if (systemPrefix.equals(prefix)) {
            return true;
        }
        String oldPrefix = prefix;
        if(addSystemPrefix(systemPrefix)){
            prefix = systemPrefix;
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
    
    /** 
     * Does this connection provide a manager of this type?
     */
    public boolean provides(Class<?> c) {
        return false; // nothing, by default
    }
    
    /** 
     * Does this connection provide a manager of this type?
     */
    public <T> T get(Class<?> T) {
        return null; // nothing, by default
    }
    
    public void dispose(){
        removeUserName(userName);
        removeSystemPrefix(prefix);
        jmri.InstanceManager.deregister(this, SystemConnectionMemo.class);
    }
    
    private boolean mDisabled = false;
    public boolean getDisabled() { return mDisabled; }
    public void setDisabled(boolean disabled) { mDisabled = disabled; }
    
}


/* @(#)SystemConnectionMemo.java */
