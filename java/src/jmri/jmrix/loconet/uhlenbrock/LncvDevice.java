package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrix.loconet.LncvDevicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to hold LocoNet LNCV device identity information.
 * See jmri.jmrix.loconet.lnsvf2.Sv2DiscoverPane
 *
 * @author B. Milhaupt 2020
 */
public class LncvDevice {
    private int destinationAddr;
    private String deviceName;
    private String rosterName;
    private int swVersion;
    private RosterEntry rosterEntry;
    private DecoderFile decoderFile;
    private final int classNum; // used as LNCV ProductID
    private int address = 0; // Module address in reply
    private int cvNum;
    private int cvValue;

    public LncvDevice(int classNum, int address, int lastCv, int lastVal, String deviceName, String rosterName, int swVersion) {
        this.classNum = classNum;
        this.address = address;
        cvNum = lastCv;
        cvValue = lastVal;
        this.deviceName = deviceName;
        this.rosterName = rosterName;
        this.swVersion = swVersion;
        log.debug("Added Module {}/{} by 6 params", classNum, address);
    }

    public LncvDevice(int classNum, int address, int lastCv, int lastVal) {
        this.classNum = classNum;
        this.address = address;
        cvNum = lastCv;
        cvValue = lastVal;
        log.debug("Added Module {}/{} by 3 params", classNum, address);
    }

    public int getProductID() {return classNum;}
    public int getDestAddr() {return destinationAddr;}
    public String getDeviceName() {return deviceName;}
    public String getRosterName() {return rosterName;}
    public int getSwVersion() {return swVersion;}

    /**
     * Set the table view of the device's destination address.
     * This routine does _not_ program the device's destination address.
     *
     * @param destAddr device destination address
     */
    public void setDestAddr(int destAddr) {this.destinationAddr = destAddr;}
    public void setDevName(String s) {deviceName = s;}
    public void setRosterName(String s) {rosterName = s;}
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
        setRosterName(e.getId());
    }


    public int getAddress() {
        return address;
    }
    public void setAddress(int addr) {
        address = addr;
    }
    public int getClassNum() {
        return classNum;
    }
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

    private final static Logger log = LoggerFactory.getLogger(LncvDevice.class);

}
