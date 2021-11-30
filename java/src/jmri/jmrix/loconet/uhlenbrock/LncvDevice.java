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
 * @author Egbert Broerse 2020
 */
public class LncvDevice {
    private int deviceAddress; // Module address in reply, value of -1 is ignored, LNCV default address : 1
    private final int artNum; // used as LNCV ProductID, must be int to pass as part of CV "art.cv", usually 4 digits
    private String deviceName;
    private String rosterEntryName;
    private int swVersion;
    private RosterEntry rosterEntry;
    private DecoderFile decoderFile;
    private int cvNum;
    private int cvValue;

    public LncvDevice(int productID, int address, int lastCv, int lastVal, String deviceName, String rosterName, int swVersion) {
        this.artNum = productID;
        this.deviceAddress = address;
        cvNum = lastCv;
        cvValue = lastVal;
        this.deviceName = deviceName;
        this.rosterEntryName = rosterName;
        this.swVersion = swVersion;
    }

    public int getProductID() {return artNum;}
    public int getDestAddr() {return deviceAddress;}
    public String getDeviceName() {return deviceName;}
    public String getRosterName() {return rosterEntryName;}
    public int getSwVersion() {return swVersion;}

    /**
     * Set the table view of the device's destination address.
     * This routine does _not_ program the device's destination address.
     *
     * @param destAddr device destination address
     */
    public void setDestAddr(int destAddr) {this.deviceAddress = destAddr;}
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
        setRosterName(e.getId()); // is a name (String)
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

    //private final static Logger log = LoggerFactory.getLogger(LncvDevice.class);

}
