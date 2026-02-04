package jmri.jmrix.roco.z21;

/**
 * Defines the standard/common routines used in multiple classes related to 
 * a Roco z21 Command Station.
 * <p>
 * This class keeps track of the broadcast flags associated with the 
 * currently connected Roco Z21 Command Station.
 * <p>
 * Brief descriptions of the flags are as follows (loosely
 * translated from  section 2.16 of the manual from the German 
 * with the aid of google translate).
 * <ul>
 * <li>0x00000001 send XpressNet related information (track
 * power on/off, programming mode, short circuit, broadcast stop,
 * locomotive information, turnout information).</li>
 * <li>0x00000002 send data changes that occur on the RMBUS.</li>
 * <li>0x00000004 (deprecated by Roco) send Railcom Data</li>
 * <li>0x00000100 send changes in system state (such as track voltage)
 * <li>0x00010000 send changes to locomotives on XpressNet (must also have
 * 0x00000001 set.</li>
 * <li>0x01000000 forward LocoNet data to the client.  Does not send
 * Locomotive or turnout data.</li>
 * <li>0x02000000 send Locomotive specific LocoNet data to the client.</li>
 * <li>0x04000000 send Turnout specific LocoNet data to the client.</li>
 * <li>0x08000000 send Occupancy information from LocoNet to the client</li>
 * <li>0x00040000 Automatically send updates for Railcom data to the client</li>
 * <li>0x00080000 send CAN detector messages to the client</li>
 * <li>0x00020000 send CAN booster status messages to the client.</li>
 * <li>0x00000010 send Fast Clock time messages to the client.</li>
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001 
 * @author      Paul Bender Copyright (C) 2016,2025
 */
public class RocoZ21CommandStation extends jmri.jmrix.roco.RocoCommandStation {

    private static final int XPressNetFlag = 0x00000001;
    private static final int RMBusFlag = 0x00000002;
    private static final int RailComFlag = 0x00000004;
    private static final int SystemStateFlag = 0x00000100;
    private static final int XPressNetLocomotiveFlag = 0x00010000;
    private static final int LocoNetDataFlag = 0x01000000;
    private static final int LocoNetLocomotiveFlag = 0x02000000;
    private static final int LocoNetTurnoutFlag = 0x04000000;
    private static final int LocoNetOccupancyFlag = 0x08000000;
    private static final int AutoMaticRailComFlag = 0x00040000;
    private static final int CANDetectorFlag = 0x00080000;
    private static final int CANBoosterFlag = 0x00020000;
    private static final int FastClockFlag = 0x00000010;
    private int broadcast_flags = 0; // holds the value of the broadcast flags.
    private int serial_number = 0; // holds the serial number of the Z21.
    private float software_version = 0; // holds the software version of the Z21.
    private int hardware_version = 0; // holds the hardware version of the Z21.

   /**
    * get the serial number.
    * @return serial number of the connected Z21 command station
    */
   public int getSerialNumber(){
      return serial_number;
   }

   /**
    * set the serial number associated with this Z21 command station.
    * @param sn serial number
    */
   public void setSerialNumber(int sn){
      serial_number = sn;
   }
 
  /**
    * get the software version.
    * @return software version of the connected Z21 command station
    */
   public float getSoftwareVersion(){
      return software_version;
   }

   /**
    * set the software version associated with this Z21 command station.
    * @param sv software version
    */
   public void setSoftwareVersion(float sv){
      software_version=sv;
   }

  /**
    * get the hardware version.
    * @return hardware version of the connected Z21 command station
    */
   public int getHardwareVersion(){
      return hardware_version;
   }

   /**
    * set the hardware version associated with this Z21 command station.
    * @param hv software version
    */
   public void setHardwareVersion(int hv){
      hardware_version=hv;
   }

   /**
    * get the current value of the broadcast flags as an int
    * @return value representing the broadcast flags. 
    */
   public int getZ21BroadcastFlags(){
        return broadcast_flags;
   }

   /**
    * set the current value of the broadcast flags as an int
    * @param flags representing the broadcast flags. 
    */
   public void setZ21BroadcastFlags(int flags){
        broadcast_flags = flags;
   }

   /**
    * get the current value of the broadcast flags as a string
    * @return string representing the broadcast flags.
    */
    public String getZ21BroadcastFlagsString() {
        return getZ21BroadcastFlagsString(broadcast_flags);
    }

    /**
     * Get the value of the specified broadcast flags as a string
     *
     * @param flags the flags to be interpreted
     * @return string representing the broadcast flags.
     */
    static public String getZ21BroadcastFlagsString(int flags) {
        StringBuilder flagStringBuilder = new StringBuilder();
        if((flags & XPressNetFlag) == XPressNetFlag){
            flagStringBuilder.append("XpressNet Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & RMBusFlag) == RMBusFlag){
            flagStringBuilder.append("RMBus Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & RailComFlag) == RailComFlag){
            flagStringBuilder.append("Railcom Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & SystemStateFlag) == SystemStateFlag){
            flagStringBuilder.append("System State Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & XPressNetLocomotiveFlag) == XPressNetLocomotiveFlag){
            flagStringBuilder.append("XpressNet Locomotive Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & LocoNetDataFlag) == LocoNetDataFlag){
            flagStringBuilder.append("LocoNet Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & LocoNetLocomotiveFlag) == LocoNetLocomotiveFlag){
            flagStringBuilder.append("LocoNet Locomotive Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & LocoNetTurnoutFlag) == LocoNetTurnoutFlag){
            flagStringBuilder.append("LocoNet Turnout Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & LocoNetOccupancyFlag) == LocoNetOccupancyFlag){
            flagStringBuilder.append("LocoNet Occupancy Messages");
            flagStringBuilder.append("\n");
        }
        if ((flags & AutoMaticRailComFlag) == AutoMaticRailComFlag) {
            flagStringBuilder.append("Railcom Automatic Messages");
            flagStringBuilder.append("\n");
        }
        if ((flags & CANDetectorFlag) == CANDetectorFlag) {
            flagStringBuilder.append("CAN Detector Messages");
            flagStringBuilder.append("\n");
        }

        if((flags & CANBoosterFlag) == CANBoosterFlag){
            flagStringBuilder.append("CAN Booster Status Messages");
            flagStringBuilder.append("\n");
        }
        if((flags & FastClockFlag) == FastClockFlag){
            flagStringBuilder.append("Fast Clock Messages");
            flagStringBuilder.append("\n");
        }

        return flagStringBuilder.toString();
    }

    /**
    * Is flag bit 0x00000001 which tells the command station to send 
    * XpressNet related information (track power on/off, programming
    * mode, short circuit, broadcast stop, locomotive information, 
    * turnout information) set?
    * @return true if flag is set.
    */
    public boolean getXPressNetMessagesFlag(){
        return((broadcast_flags & XPressNetFlag) == XPressNetFlag);
    }

   /**
    * Set flag bit 0x00000001 which tells the command station to send 
    * XpressNet related information (track power on/off, programming
    * mode, short circuit, broadcast stop, locomotive information, 
    * turnout information).
    * @param flag true if flag is to be set.
    */
    public void setXPressNetMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | XPressNetFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~XPressNetFlag);
        }
    }

   /**
    * Is flag bit 0x00000002 which tells the command station to send 
    * data changes on the RMBus to the client set?
    * @return true if flag is set.
    */
    public boolean getRMBusMessagesFlag(){
        return((broadcast_flags & RMBusFlag) == RMBusFlag);
    }

   /**
    * Set flag bit 0x00000002 which tells the command station to send 
    * data changes on the RMBus to the client set?
    * @param flag true if flag is to be set.
    */
    public void setRMBusMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | RMBusFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~RMBusFlag);
        }
    }

   /**
    * Is flag bit 0x00000004, which tells the command station to 
    * send Railcom data to the client set (this flag may no longer
    * be supported by Roco). 
    * @return true if flag is set.
    */
    public boolean getRailComMessagesFlag(){
        return((broadcast_flags & RailComFlag) == RailComFlag);
    }

   /**
    * Set flag bit 0x00000004, which tells the command station to 
    * send Railcom data to the client set (this flag may no longer
    * be supported by Roco). 
    * @param flag true if flag is to be set.
    */
    public void setRailComMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | RailComFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~RailComFlag);
        }
    }

   /**
    * Is flag bit 0x00040000, which tells the command station to 
    * automatically send Railcom data to the client set. 
    * @return true if flag is set.
    */
    public boolean getRailComAutomaticFlag(){
        return((broadcast_flags & AutoMaticRailComFlag) == AutoMaticRailComFlag);
    }

   /**
    * Set flag bit 0x00040000, which tells the command station to 
    * automatically send Railcom data to the client set. 
    * @param flag true if flag is to be set.
    */
    public void setRailComAutomaticFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | AutoMaticRailComFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~AutoMaticRailComFlag);
        }
    }

   /**
    * Is flag bit 0x00080000, which tells the command station to 
    * send CAN detector data to the client set. 
    * @return true if flag is set.
    */
    public boolean getCanDetectorFlag(){
        return((broadcast_flags & CANDetectorFlag) == CANDetectorFlag);
    }

   /**
    * Set flag bit 0x00080000, which tells the command station to 
    * send CAN detector data to the client. 
    * @param flag true if flag is to be set.
    */
    public void setCanDetectorFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | CANDetectorFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~CANDetectorFlag);
        }
    }

    /**
     * Is flag bit 0x00020000, which tells the command station to
     * send CAN booster data to the client set.
     * @return true if flag is set.
     */
    public boolean getCanBoosterFlag(){
        return((broadcast_flags & CANBoosterFlag) == CANBoosterFlag);
    }

    /**
     * Set flag bit 0x00020000, which tells the command station to
     * send CAN detector data to the client.
     * @param flag true if flag is to be set.
     */
    public void setCanBoosterFlag(boolean flag) {
        if (flag) {
            broadcast_flags = broadcast_flags | CANBoosterFlag;
        } else {
            broadcast_flags = broadcast_flags & (~CANBoosterFlag);
        }
    }


   /**
    * Is flag bit 0x00000100 which tells the command station to send 
    * changes in system state (such as track voltage) set?
    * @return true if flag is set.
    */
    public boolean getSystemStatusMessagesFlag(){
        return((broadcast_flags & SystemStateFlag) == SystemStateFlag);
    }

   /**
    * Set flag bit 0x00000100 which tells the command station to send 
    * changes in system state (such as track voltage).
    * @param flag true if flag is to be set.
    */
    public void setSystemStatusMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | SystemStateFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~SystemStateFlag);
        }
    }

   /**
    * Is flag bit 0x00010000 which tells the command station to send 
    * XpressNet related locomoitve information to the client set?
    *
    * @return true if flag is set
    */
    public boolean getXPressNetLocomotiveMessagesFlag(){
        return((broadcast_flags & XPressNetLocomotiveFlag) == XPressNetLocomotiveFlag);
    }

   /**
    * Set flag bit 0x00010000 which tells the command station to send 
    * XpressNet related locomoitve information to the client.
    * @param flag true if flag is to be set.
    */
    public void setXPressNetLocomotiveMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | XPressNetLocomotiveFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~XPressNetLocomotiveFlag);
        }
    }

   /**
    * Is flag bit 0x01000000 which tells the command station to send 
    * LocoNet data,except Locomotive and Turnout data, to the client set?
    * @return true if flag is set.
    */
    public boolean getLocoNetMessagesFlag(){
        return((broadcast_flags & LocoNetDataFlag) == LocoNetDataFlag);
    }

   /**
    * Set flag bit 0x01000000 which tells the command station to send
    * LocoNet data,except Locomotive and Turnout data, to the client.
    * @param flag true if flag is to be set.
    */
    public void setLocoNetMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | LocoNetDataFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~LocoNetDataFlag);
        }
    }

   /**
    * Is flag bit 0x02000000 which tells the command station to send 
    * Locomotive specific LocoNet data to the client set?
    * @return true if flag is set.
    */
    public boolean getLocoNetLocomotiveMessagesFlag(){
        return((broadcast_flags & LocoNetLocomotiveFlag) == LocoNetLocomotiveFlag);
    }

   /**
    * Set flag bit 0x02000000 which tells the command station to send 
    * Locomotive specific LocoNet data to the client.
    * @param flag true if flag is to be set.
    */
    public void setLocoNetLocomotiveMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | LocoNetLocomotiveFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~LocoNetLocomotiveFlag);
        }
    }

   /**
    * Is flag bit 0x04000000 which tells the command station to send 
    * Turnout specific LocoNet data to the client set?
    * @return true if flag is set.
    */
    public boolean getLocoNetTurnoutMessagesFlag(){
        return((broadcast_flags & LocoNetTurnoutFlag) == LocoNetTurnoutFlag);
    }

   /**
    * Set flag bit 0x04000000 which tells the command station to send 
    * Turnout specific LocoNet data to the client set?
    * @param flag true if flag is to be set.
    */
    public void setLocoNetTurnoutMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | LocoNetTurnoutFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~LocoNetTurnoutFlag);
        }
    }

   /**
    * Is flag bit 0x08000000 which tells the command station to send 
    * Occupancy information from LocoNet to the client set?
    * @return true if flag is set.
    */
    public boolean getLocoNetOccupancyMessagesFlag(){
        return((broadcast_flags & LocoNetOccupancyFlag) == LocoNetOccupancyFlag);
    }

   /**
    * <p>
    * Set flag bit 0x08000000 which tells the command station to send 
    * Occupancy information from LocoNet to the client
    * </p>
    * <p>
    * If this flag is set to true, the Z21 will format Transponding messages
    * as described in section 9.5 of the Z21 Lan Protocol.  This message format
    * is currentlyunsupported by JMRI.
    * </p>
    *
    * @param flag true if flag is to be set.
    */
    public void setLocoNetOccupancyMessagesFlag(boolean flag){
        if(flag) {
           broadcast_flags = broadcast_flags | LocoNetOccupancyFlag;
        }
        else {
           broadcast_flags = broadcast_flags & (~LocoNetOccupancyFlag);
        }
    }
    //* <li>0x00000010 send Fast Clock time messages to the client.</li>
    /**
     * Is flag bit 0x00000100 which tells the command station to send
     * changes in system state (such as track voltage) set?
     * @return true if flag is set.
     */
    public boolean getFastClockFlag(){
        return((broadcast_flags & FastClockFlag) == FastClockFlag);
    }

    /**
     * Set flag bit 0x00000010 which tells the command station to send
     * changes in the fast clock to.
     * @param flag true if flag is to be set.
     */
    public void setFastClockFlag(boolean flag){
        if(flag) {
            broadcast_flags = broadcast_flags | FastClockFlag;
        }
        else {
            broadcast_flags = broadcast_flags & (~FastClockFlag);
        }
    }

}
