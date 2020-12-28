package jmri.jmrix.loconet.uhlenbrock;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aptly
 */
public class LncvDevices {

    public LncvDevices() {
        deviceList = new ArrayList<>();
    }

    private List<LncvDevice> deviceList;
    public void addDevice(LncvDevice d) {
        if (!deviceExists(d)) {
            deviceList.add(d);
            log.debug("added device with prod {}, addr {}",
                    d.getProductID(), d.getDestAddr());
        } else {
            log.debug("device already in list: prod {}, addr {}",
                    d.getProductID(), d.getDestAddr());
        }
    }

    public void removeAllDevices() {
        deviceList.clear();
    }

    /**
     * Get index of device with matching Mfg, ProdID, Num and
     * Device Address.
     * Where a deviceToBeFound parameter is -1, that parameter is not compared.
     *
     * @param deviceToBeFound Device to try to find in known LNCV devices list
     * @return index of found device, else -1 if matching device not found
     */
    public int isDeviceExistant(LncvDevice deviceToBeFound) {
        log.debug("Looking for a known LNCV device which matches characteristics: prodId {}, addr {}.",
                deviceToBeFound.getProductID(),
                deviceToBeFound.getDestAddr());
        for (int i = 0; i < deviceList.size(); ++i) {
            LncvDevice dev = deviceList.get(i);
            log.trace("Comparing against known device: prodId {}, addr {}.",
                    dev.getProductID(),
                    deviceToBeFound.getDestAddr());
            if ((deviceToBeFound.getProductID() == -1) ||
                    (dev.getProductID() == deviceToBeFound.getProductID())) {
                if ((deviceToBeFound.getDestAddr() == -1) ||
                        (dev.getDestAddr() == deviceToBeFound.getDestAddr())) {
                    log.debug("Match Found! Searched device matched against known device: prodId {}, addr {}.",
                            dev.getProductID(),
                            dev.getDestAddr());
                    return i;
                }
            }
        }
        log.debug("No matching known device was found!");
        return -1;
    }

    public boolean deviceExists(LncvDevice d) {
        int i = isDeviceExistant(d);
        return (i >= 0);
    }

    public LncvDevice getDevice(int index) {
        return deviceList.get(index);
    }

    public LncvDevice[] getDevices() {
        LncvDevice[] d = {};
        return deviceList.toArray(d);
    }
    public int size() {
        return deviceList.size();
    }

    private final static Logger log = LoggerFactory.getLogger(LncvDevices.class);

}
