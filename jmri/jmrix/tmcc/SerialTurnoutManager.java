// SerialTurnoutManager.java

package jmri.jmrix.tmcc;

import jmri.managers.AbstractTurnoutManager;
import jmri.Turnout;

/**
 * Implement turnout manager for TMCC serial systems
 * <P>
 * System names are "TTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 * @version	$Revision: 1.8 $
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {
    	
    }

    public String getSystemPrefix() { return "T"; }

    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName);
        if (sName=="") {
            // system name is not valid
            return null;
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t!=null) {
            return null;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName);
        t = getBySystemName(altName);
        if (t!=null) {
            return null;
        }
        // create the turnout
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new SerialTurnout(addr);
        t.setUserName(userName);
        
        // does system name correspond to configured hardware
        if ( !SerialAddress.validSystemNameConfig(sName,'T') ) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '"+sName+"' refers to an undefined Serial Node.");
        }
        return t;
    }

    static public SerialTurnoutManager instance() {
        if (_instance == null) _instance = new SerialTurnoutManager();
        return _instance;
    }
    static SerialTurnoutManager _instance = null;
    
    //Turnout address format is more than a simple number.
    //public boolean allowMultipleAdditions(String systemName) { return true;  }
    
    /**
    * A method that creates an array of systems names to allow bulk
    * creation of turnouts.
    */
    //further work needs to be done on how to format a number of CMRI turnout, therefore this method will only return one entry.
    public String[] formatRangeOfAddresses(String start, int numberToAdd, String prefix){
        numberToAdd = 1;
        String range[] = new String[numberToAdd];
        for (int x = 0; x < numberToAdd; x++){
            range[x] = prefix+"T"+start;
        }
        return range;
    }
    
    public String getNextValidAddress(String curAddress, String prefix){
        int nAddress = 0;
        int bitNum = 0;
        int seperator=0;
        String tmpSName;
        
        if(curAddress.contains(":")){
            //Address format passed is in the form node:address
            seperator = curAddress.indexOf(":");
            nAddress = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
            bitNum = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
            tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum);
        } else {
            tmpSName = prefix+"T"+curAddress;
            bitNum = SerialAddress.getBitFromSystemName(tmpSName);
            nAddress = SerialAddress.getNodeAddressFromSystemName(tmpSName);
        }
        
       // System.out.println(tmpSName);
        //If the hardware address past does not already exist then this can
        //be considered the next valid address.
        Turnout t = getBySystemName(tmpSName);
        if(t==null){
            seperator = tmpSName.lastIndexOf("T")+1;
            curAddress = tmpSName.substring(seperator);
            return curAddress;
        }
        
        //The Number of Output Bits of the previous turnout will help determine the next
        //valid address.
        bitNum = bitNum + t.getNumberOutputBits();
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum);
        t = getBySystemName(tmpSName);
        if(t!=null){
            for(int x = 1; x<10; x++){
                bitNum = bitNum + t.getNumberOutputBits();
                //System.out.println("This should increment " + bitNum);
                tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum);
                t = getBySystemName(tmpSName);
                if(t==null)
                    seperator = tmpSName.lastIndexOf("T")+1;
                    curAddress = tmpSName.substring(seperator);
                    return curAddress;
            }
            return null;
        } else {
            seperator = tmpSName.lastIndexOf("T")+1;
            curAddress = tmpSName.substring(seperator);
            return curAddress;
        }
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */
