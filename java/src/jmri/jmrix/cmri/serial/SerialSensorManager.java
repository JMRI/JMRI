// SerialSensorManager.java

package jmri.jmrix.cmri.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Sensor;
import jmri.jmrix.AbstractNode;
import jmri.JmriException;

/**
 * Manage the C/MRI serial-specific Sensor implementation.
 * <P>
 * System names are "CSnnnn", where nnnn is the sensor number without padding.
 * <P>
 * Sensors are numbered from 1.
 * <P>
 * This is a SerialListener to handle the replies to poll messages.
 * Those are forwarded to the specific SerialNode object corresponding
 * to their origin for processing of the data.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003, 2007
 * @author                      Dave Duchamp, multi node extensions, 2004
 * @version			$Revision: 18178 $
 */
public class SerialSensorManager extends jmri.managers.AbstractSensorManager
                            implements SerialListener {

    /**
     * Number of sensors per UA in the naming scheme.
     * <P>
     * The first UA (node address) uses sensors from 1 to SENSORSPERUA-1,
     * the second from SENSORSPERUA+1 to SENSORSPERUA+(SENSORSPERUA-1), etc.
     * <P>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     *
     */
    static final int SENSORSPERUA = 1000;

    public SerialSensorManager() {
        super();
    }

    /**
     * Return the C/MRI system letter
     */
    public String getSystemPrefix() { return "C"; }

    /**
     * Create a new sensor if all checks are passed
     *    System name is normalized to ensure uniqueness.
     */
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            log.error("Invalid CMRI Sensor system name - "+systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s!=null) {
            log.error("Sensor with this name already exists - "+systemName);
            return null;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName);
        s = getBySystemName(altName);
        if (s!=null) {
            log.error("Sensor with name '"+systemName+"' already exists as '"+altName+"'");
            return null;
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName);
        if ( (bit<=0) || (bit>=SENSORSPERUA) ) {
            log.error("Sensor bit number, "+Integer.toString(bit)+
                    ", is outside the supported range, 1-"+Integer.toString(SENSORSPERUA-1));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null)
            s = new SerialSensor(sName);
        else
            s = new SerialSensor(sName, userName);

        // ensure that a corresponding Serial Node exists
        SerialNode node = (SerialNode) SerialAddress.getNodeFromSystemName(sName);
        if (node==null) {
            log.warn("Sensor " + sName + " refers to an undefined Serial Node.");
            return s;
        }
        // register this sensor with the Serial Node
        node.registerSensor(s, bit-1);
        return s;
    }
    
    /**
     * Dummy routine
     */
    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }

    /**
     *  Process a reply to a poll of Sensors of one node
     *  with CMRI-Extended messages 
     */
    public void reply(SerialReply r) {
        // determine which node
        SerialNode node = (SerialNode)SerialTrafficController.instance().getNodeFromAddress(r.getUA());
        if (node!=null) {
         // Response 'R'  input bytes
           if (r.isRcv())
            { 
              node.markChanges(r);
            }
         // Response 'E'  EOT response Do no processing, none need 
           if (r.isEOT()) 
            {
            }
         // Response 'Q'  Node query
           if (r.isQUERY()) 
            {
               log.info("QUERY Seen");
           }
         // Response 'D'  Datagram Read Data
           if (r.isDGREAD())  
            {
               log.info("Datagram In Data Seen");
           }
         // Response 'W'  Datagram Write Data
           if (r.isDGWRITE()) 
            {
               if (true) //(node.validateCRC()) 
                {
                    log.info("Datagram ACK Seen");
                }
            }
         // Response 'A'+ACK  Datagram negative acknowledge
           if (r.isDGACK()) 
            {
               if (true) //(node.validateCRC()) 
                {
                    log.info("Datagram NAK Seen");
                           
                }
            }
         // Response 'A'+NAK  Datagram negative acknowledge
           if (r.isDGNAK()) 
            {
               if (true) //(node.validateCRC()) 
                {
                    log.info("Datagram NAK Seen");
                           
                }
            }
        }
    }
    
    /**
     * Method to register any orphan Sensors when a new Serial Node is created
     */
    public void registerSensorsForNode(SerialNode node) {
        // get list containing all Sensors
        java.util.Iterator<String> iter =
                                    getSystemNameList().iterator();
        // Iterate through the sensors
        AbstractNode tNode = null;
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName==null) {
                log.error("System name null during register Sensor");
            }
            else {
                log.debug("system name is "+sName);
                if ( (sName.charAt(0) == 'C') && (sName.charAt(1) == 'S') ) {
                    // This is a C/MRI Sensor
                    tNode = SerialAddress.getNodeFromSystemName(sName);
                    if (tNode==node) {
                        // This sensor is for this new Serial Node - register it
                        node.registerSensor(getBySystemName(sName),
                                    (SerialAddress.getBitFromSystemName(sName)-1));
                    }
                }
            }
        }
    }

    /**
     * static function returning the SerialSensorManager instance to use.
     * @return The registered SerialSensorManager instance for general use,
     *         if need be creating one.
     */
    static public SerialSensorManager instance() {
        if (_instance == null) _instance = new SerialSensorManager();
        return _instance;
    }

    static SerialSensorManager _instance = null;
    
    public boolean allowMultipleAdditions(String systemName) { return true;  }
    
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        String tmpSName = "";
        if(curAddress.contains(":")){
            //Address format passed is in the form node:address
            int seperator = curAddress.indexOf(":");
            try{
                nAddress = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
                bitNum = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = SerialAddress.makeSystemName("S", nAddress, bitNum);
        } else if (curAddress.contains("B")||(curAddress.contains("b"))) {
            curAddress = curAddress.toUpperCase();
            try{
                //We do this to simply check that we have numbers in the correct places ish
                Integer.parseInt(curAddress.substring(0,1));
                int b = (curAddress.toUpperCase()).indexOf("B")+1;
                Integer.parseInt(curAddress.substring(b));
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = prefix+typeLetter()+curAddress;
            bitNum = SerialAddress.getBitFromSystemName(tmpSName);
            nAddress = SerialAddress.getNodeAddressFromSystemName(tmpSName);
        } else {
            try {
                //We do this to simply check that the value passed is a number!
                Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = prefix+typeLetter()+curAddress;
            bitNum = SerialAddress.getBitFromSystemName(tmpSName);
            nAddress = SerialAddress.getNodeAddressFromSystemName(tmpSName);
        }
        
        return tmpSName;
    }
    
    int bitNum = 0;
    int nAddress = 0;
    
    public String getNextValidAddress(String curAddress, String prefix){
        //If the hardware address past does not already exist then this can
        //be considered the next valid address.
        
        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showErrorMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false);

            return null;
        }
        
         
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if(s!=null){
            for(int x = 1; x<10; x++){
                bitNum++;
                tmpSName = SerialAddress.makeSystemName("S", nAddress, bitNum);
                s = getBySystemName(tmpSName);
                if(s==null){
                    int seperator = tmpSName.lastIndexOf("S")+1;
                    curAddress = tmpSName.substring(seperator);
                    return curAddress;
                }
            }
            return null;
        } else {
            int seperator = tmpSName.lastIndexOf("S")+1;
            curAddress = tmpSName.substring(seperator);
            return curAddress;
        }
    }
    
    static Logger log = LoggerFactory.getLogger(SerialSensorManager.class.getName());
}

/* @(#)SerialSensorManager.java */
