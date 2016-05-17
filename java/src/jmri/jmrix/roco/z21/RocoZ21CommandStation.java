/*
 * RocoZ21CommandStation.java
 */
package jmri.jmrix.roco.z21;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the standard/common routines used in multiple classes related to 
 * a Roco z21 Command Station.
 * <P>
 * This class keeps track of the broadcast flags associated with the 
 * currently connected Roco Z21 Command Station.
 * <P>
 * Brief descriptions of the flags are as follows (losely
 * translated from  section 2.16 of the manual from the German 
 * with the aid of google translate).
 * <P>
 * <UL>
 * <LI>0x00000001 send XPressNet related information (track
 * power on/off, programming mode, short circuit, broadcast stop,
 * locomotive information, turnout information).</LI>
 * <LI>0x00000002 send data changes that occur on the RMBUS.</LI>
 * <LI>0x00000004 (deprecated by Roco) send Railcom Data</LI>
 * <LI>0x00000100 send changes in system state (such as track voltage)
 * <LI>0x00010000 send changes to locomotives on XPressNet (must also have
 * 0x00000001 set.</LI>
 * <LI>0x01000000 forward LocoNet data to the client.  Does not send
 * Locomotive or turnout data.</LI>
 * <LI>0x02000000 send Locomotive specific LocoNet data to the client.</LI>
 * <LI>0x04000000 send Turnout specific LocoNet data to the client.</LI>
 * <LI>0x08000000 send Occupancy information from LocoNet to the client</LI
 * </UL>
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001 
 * @author      Paul Bender Copyright (C) 2016
 */
public class RocoZ21CommandStation extends jmri.jmrix.roco.RocoCommandStation implements jmri.jmrix.DccCommandStation, jmri.CommandStation {

    private int broadcast_flags = 0; // holds the value of the broadcast flags.
    private int serial_number = 0; // holds the serial number of the Z21.
    private float software_version = 0; // holds the software version of the Z21.
    private int hardware_version = 0; // holds the hardware version of the Z21.

    /**
     * Roco does use a service mode
     */
    @Override
    public boolean getHasServiceMode() {
        return true;
    }


   /**
    * get the serial number.
    * @return int serial number of the connected Z21 command station
    */
   public int getSerialNumber(){
      return serial_number;
   }

   /**
    * set the serial number associated with this Z21 command station.
    * @param int sn serial number
    */
   public void setSerialNumber(int sn){
      serial_number = sn;
   }
 
  /**
    * get the software version.
    * @return int software version of the connected Z21 command station
    */
   public float getSoftwareVersion(){
      return software_version;
   }

   /**
    * set the software version associated with this Z21 command station.
    * @param int sv software version
    */
   public void setSoftwareVersion(float sv){
      software_version=sv;
   }

  /**
    * get the hardware version.
    * @return int hardware version of the connected Z21 command station
    */
   public int getHardwareVersion(){
      return hardware_version;
   }

   /**
    * set the hardware version associated with this Z21 command station.
    * @param int hv software version
    */
   public void setHardwareVersion(int hv){
      hardware_version=hv;
   }

   /**
    * get the current value of the broadcast flags as an int
    * @return int value representing the broadcast flags. 
    */
   public int getZ21BroadcastFlags(){
        return broadcast_flags;
   }

   /**
    * set the current value of the broadcast flags as an int
    * @param int value representing the broadcast flags. 
    */
   public void setZ21BroadcastFlags(int flags){
        broadcast_flags = flags;
   }

   /**
    * Is flag bit 0x00000001 which tells the command station to send 
    * XPressNet related information (track power on/off, programming 
    * mode, short circuit, broadcast stop, locomotive information, 
    * turnout information) set?
    * @return boolean true if flag is set.
    */
    public boolean getXPressNetMessagesFlag(){
        return((broadcast_flags & 0x00000001) == 0x00000001);
    }

   /**
    * Set flag bit 0x00000001 which tells the command station to send 
    * XPressNet related information (track power on/off, programming 
    * mode, short circuit, broadcast stop, locomotive information, 
    * turnout information).
    * @param boolean flag true if flag is to be set.
    */
    public void setXPressNetMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x00000001;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x00000001));
        }
    }



   /**
    * Is flag bit 0x00000002 which tells the command station to send 
    * data changes on the RMBus to the client set?
    * @return boolean true if flag is set.
    */
    public boolean getRMBusMessagesFlag(){
        return((broadcast_flags & 0x00000002) == 0x00000002);
    }

   /**
    * Set flag bit 0x00000002 which tells the command station to send 
    * data changes on the RMBus to the client set?
    * @param boolean flag true if flag is to be set.
    */
    public void setRMBusMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x00000002;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x00000002));
        }
    }

   /**
    * Is flag bit 0x00000004, which tells the command station to 
    * send Railcom data to the client set (this flag may no longer
    * be supported by Roco). 
    * @return boolean true if flag is set.
    */
    public boolean getRailComMessagesFlag(){
        return((broadcast_flags & 0x00000004) == 0x00000004);
    }

   /**
    * Set flag bit 0x00000004, which tells the command station to 
    * send Railcom data to the client set (this flag may no longer
    * be supported by Roco). 
    * @param boolean flag true if flag is to be set.
    */
    public void setRailComMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x00000004;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x00000004));
        }
    }

   /**
    * Is flag bit 0x00000100 which tells the command station to send 
    * changes in system state (such as track voltage) set?
    * @return boolean true if flag is set.
    */
    public boolean getSystemStatusMessagesFlag(){
        return((broadcast_flags & 0x00000100) == 0x00000100);
    }

   /**
    * Set flag bit 0x00000100 which tells the command station to send 
    * changes in system state (such as track voltage).
    * @param boolean flag true if flag is to be set.
    */
    public void setSystemStatusMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x00000100;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x00000100));
        }
    }

   /**
    * Is flag bit 0x00010000 which tells the command station to send 
    * XPressNet related locomoitve information to the client set?
    * @return boolean true if flag is set.
    */
    public boolean getXPressNetLocomotiveMessagesFlag(){
        return((broadcast_flags & 0x00010000) == 0x00010000);
    }

   /**
    * Set flag bit 0x00010000 which tells the command station to send 
    * XPressNet related locomoitve information to the client.
    * @param boolean flag true if flag is to be set.
    */
    public void setXPressNetLocomotiveMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x00010000;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x00010000));
        }
    }

   /**
    * Is flag bit 0x01000000 which tells the command station to send 
    * LocoNet data,except Locomotive and Turnout data, to the client set?
    * @return boolean true if flag is set.
    */
    public boolean getLocoNetMessagesFlag(){
        return((broadcast_flags & 0x01000000) == 0x01000000);
    }

   /**
    * set flag bit 0x01000000 which tells the command station to send 
    * LocoNet data,except Locomotive and Turnout data, to the client.
    * @param boolean flag true if flag is to be set.
    */
    public void setLocoNetMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x01000000;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x01000000));
        }
    }

   /**
    * Is flag bit 0x02000000 which tells the command station to send 
    * Locomotive specific LocoNet data to the client set?
    * @return boolean true if flag is set.
    */
    public boolean getLocoNetLocomotiveMessagesFlag(){
        return((broadcast_flags & 0x02000000) == 0x02000000);
    }

   /**
    * Set flag bit 0x02000000 which tells the command station to send 
    * Locomotive specific LocoNet data to the client.
    * @param boolean flag true if flag is to be set.
    */
    public void setLocoNetLocomotiveMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x02000000;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x02000000));
        }
    }

   /**
    * Is flag bit 0x04000000 which tells the command station to send 
    * Turnout specific LocoNet data to the client set?
    * @return boolean true if flag is set.
    */
    public boolean getLocoNetTurnoutMessagesFlag(){
        return((broadcast_flags & 0x04000000) == 0x04000000);
    }

   /**
    * Set flag bit 0x04000000 which tells the command station to send 
    * Turnout specific LocoNet data to the client set?
    * @param boolean flag true if flag is to be set.
    */
    public void setLocoNetTurnoutMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x04000000;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x04000000));
        }
    }

   /**
    * Is flag bit 0x08000000 which tells the command station to send 
    * Occupancy information from LocoNet to the client set?
    * @return boolean true if flag is set.
    */
    public boolean getLocoNetOccupancyMessagesFlag(){
        return((broadcast_flags & 0x08000000) == 0x08000000);
    }

   /**
    * Set flag bit 0x08000000 which tells the command station to send 
    * Occupancy information from LocoNet to the client
    * @param boolean flag true if flag is to be set.
    */
    public void setLocoNetOccupancyMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | 0x08000000;
        }
        else {
           broadcast_flags = broadcast_flags & (~(0x08000000));
        }
    }

    /*
     * We need to register for logging
     */
    private final static Logger log = LoggerFactory.getLogger(RocoZ21CommandStation.class.getName());

}


/* @(#)RocoCommandStation.java */
