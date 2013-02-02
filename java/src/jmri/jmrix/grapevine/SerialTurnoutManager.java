// SerialTurnoutManager.java

package jmri.jmrix.grapevine;

import org.apache.log4j.Logger;
import jmri.managers.AbstractTurnoutManager;
import jmri.Turnout;
import jmri.JmriException;

/**
 * Implement turnout manager for Grapevine systems
 * <P>
 * System names are "GTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @version	$Revision$
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {
 
    }

    public String getSystemPrefix() { return "G"; }

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
        t = new SerialTurnout(sName,userName);
        
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
    public boolean allowMultipleAdditions() { return false;  }
    
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        String tmpSName = prefix+"T"+curAddress;
        
        if(curAddress.contains(":")){
            //Address format passed is in the form of node:cardOutput or node:card:address
            int seperator = curAddress.indexOf(":");
            try {
                nNode = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
                int nxSeperator = curAddress.indexOf(":", seperator+1);
                if (nxSeperator == -1){
                    //Address has been entered in the format node:cardOutput
                    bitNum = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
                } else {
                    //Address has been entered in the format node:card:output
                    nCard = Integer.valueOf(curAddress.substring(seperator+1,nxSeperator)).intValue()*100;
                    bitNum = Integer.valueOf(curAddress.substring(nxSeperator+1)).intValue();
                }
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Hardware Address passed should be a number");
            }
            tmpSName = prefix+"T"+nNode+(nCard+bitNum);
        } else {
            bitNum = SerialAddress.getBitFromSystemName(tmpSName);
            nNode = SerialAddress.getNodeAddressFromSystemName(tmpSName);
            tmpSName = prefix+"T"+nNode+bitNum;
        }
        return (tmpSName);
    }
    
    int nCard = 0;
    int bitNum = 0;
    int nNode = 0;
    
    /**
    * A method that returns the next valid free turnout hardware address
    */
    
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException{
        
        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            throw ex;
        }

        //If the hardware address past does not already exist then this can
        //be considered the next valid address.
        Turnout t = getBySystemName(tmpSName);
        if(t==null){
            return Integer.toString(nNode)+Integer.toString((nCard+bitNum));
            //return ""+nNode+(nCard+bitNum);
        }
        
        //The Number of Output Bits of the previous turnout will help determine the next
        //valid address.
        bitNum = bitNum + t.getNumberOutputBits();
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        tmpSName = prefix+"T"+nNode+(nCard+bitNum);
        t = getBySystemName(tmpSName);
        if(t!=null){
            for(int x = 1; x<10; x++){
                bitNum = bitNum + t.getNumberOutputBits();
                tmpSName = prefix+"T"+nNode+(nCard+bitNum);
                t = getBySystemName(tmpSName);
                if(t==null)
                    return Integer.toString(nNode)+Integer.toString((nCard+bitNum));
                    //return ""+nNode+(nCard+bitNum);
            }
            return null;
        } else {
            return Integer.toString(nNode)+Integer.toString((nCard+bitNum));
        }
    }

    static Logger log = Logger.getLogger(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */
