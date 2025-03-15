package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * A class to hold LocoNet LNSVf1 (LocoIO) device identity information.
 * See jmri.jmrix.loconet.swing.lnsv1prog.Lnsv1ProgPane
 *
 * @author B. Milhaupt 2020
 * @author Egbert Broerse 2020, 2025
 */
public class Lnsv1Device {
    private int deviceAddressLow; // Module address in reply, value of -1 is ignored, LNSV1 default address: 88
    // High byte of the Address is fixed to 0x01 (not displayed as part of board address)
    // valid deviceAddressLow (aka low byte "Address") is in the range of 0x01 .. 0x4F, 0x51 .. 0x7F
    // (deviceAddressLow 0x50 is reserved for the LocoBuffer)
    private int deviceAddressHi;
    // valid deviceAddressHi (aka "subAddress") is in the range of 0x01 .. 0x7E (0x7F is reserved)
    private final int deviceAddress; // required by symbolicProgrammer
    private String deviceName;
    private String rosterEntryName;
    private int swVersion;
    private RosterEntry rosterEntry;
    private DecoderFile decoderFile;
    private int cvNum;
    private int cvValue;

    public Lnsv1Device(int addressL, int addressH, int lastCv, int lastVal, String deviceName, String rosterName, int swVersion) {
        this.deviceAddressLow = addressL;
        // Low byte Address must be in the range of 0x01 .. 0x4F, 0x51 .. 0x7F
        this.deviceAddressHi = addressH;
        // The subAddress is in the range of 0x01 .. 0x7E (0x7F is reserved)
        this.deviceAddress = 256 * (addressH - 1) + addressL; // equals: addressH << 7 + addressL
        cvNum = lastCv;
        cvValue = lastVal;
        this.deviceName = deviceName;
        this.rosterEntryName = rosterName;
        this.swVersion = swVersion;
    }

    public int getDestAddr() {return deviceAddress;}
    public int getDestAddrLow() {return deviceAddressLow;}
    public int getDestAddrHigh() {return deviceAddressHi;}
    public String getDeviceName() {return deviceName;}
    public String getRosterName() {return rosterEntryName;}
    public int getSwVersion() {return swVersion;}

    /**
     * Set the table view of the device's low and high address.
     * This routine does _not_ program the device's destination address.
     *
     * @param destAddrL device low address
     */
    public void setDestAddrLow(int destAddrL) {this.deviceAddressLow = destAddrL;}
    public void setDestAddrHigh(int destAddrH) {this.deviceAddressHi = destAddrH;}
    public void setDevName(String s) {deviceName = s;}
    public void setRosterName(String s) {rosterEntryName = s;}
    public void setSwVersion(int version) {swVersion = version;}
    public DecoderFile getDecoderFile() {
        return decoderFile;
    }
    public void setDecoderFile(DecoderFile f) {
        decoderFile = f;
    }

    public RosterEntry getRosterEntry() {
        return rosterEntry;
    }
    public void setRosterEntry(RosterEntry e) {
        rosterEntry = e;
        if (e == null) {
            setRosterName("");
        } else {
            setRosterName(e.getId()); // is a name (String)
        }
    }

    // optional: remember last used CV
    public int getCvNum() {
        return cvNum;
    }
    public void setCvNum(int num) {
        cvNum = num;
    }
    public int getCvValue() {
        return cvValue;
    }
    public void setCvValue(int val) {
        cvValue = val;
    }

    // private final static Logger log = LoggerFactory.getLogger(Lnsv1Device.class);

}
