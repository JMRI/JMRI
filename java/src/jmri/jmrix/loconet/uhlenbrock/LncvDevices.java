package jmri.jmrix.loconet.uhlenbrock;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrix.loconet.LncvDevicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;

/**
 * Manage an array of LncvDevice items. See {@link LncvDevicesManager}
 * Based on Lnsvf2Devices by B. Milhaupt
 * @author Egbert Broerse 2020
 */
public class LncvDevices {

    @GuardedBy("this")
    private final List<LncvDevice> deviceList;

    public LncvDevices() {
        deviceList = new ArrayList<>();
    }

    /**
     * Add a device that responded to a PROG_START request to the list of LNCV Devices.
     *
     * @param d the device object, containing its properties
     * @return true if device was addded, false if not eg it was already in the list
     */
    public synchronized boolean addDevice(LncvDevice d) {
        if (!deviceExists(d)) {
            deviceList.add(d);
            log.debug("added device with prod {}, addr {}",
                    d.getProductID(), d.getDestAddr());
            return true;
        } else {
            log.debug("device already in list: prod {}, addr {}",
                    d.getProductID(), d.getDestAddr());
            return false;
        }
    }

    public synchronized void removeAllDevices() {
        deviceList.clear();
    }

    /**
     * Get index of device with matching Mfg, ProdID, Num and
     * Device Address.
     * Where a deviceToBeFound parameter is -1, that parameter is not compared.
     *
     * @param deviceToBeFound Device we try to find in known LNCV devices list
     * @return index of found device, else -1 if matching device not found
     */
    public synchronized int isDeviceExistant(LncvDevice deviceToBeFound) {
        log.debug("Looking for a known LNCV device which matches characteristics: article {}, addr {}.",
                deviceToBeFound.getProductID(),
                deviceToBeFound.getDestAddr());
        for (int i = 0; i < deviceList.size(); ++i) {
            LncvDevice dev = deviceList.get(i);
            log.trace("Comparing against known device: article {}, addr {}.",
                    dev.getProductID(),
                    deviceToBeFound.getDestAddr());
            if ((deviceToBeFound.getProductID() == -1) ||
                    (dev.getProductID() == deviceToBeFound.getProductID())) {
                if ((deviceToBeFound.getDestAddr() == -1) ||
                        (dev.getDestAddr() == deviceToBeFound.getDestAddr())) {
                    log.debug("Match Found! Searched device matched against known device: article {}, addr {}.",
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
        log.debug("deviceExists found {}", i);
        return (i >= 0);
    }

    public synchronized LncvDevice getDevice(int index) {
        return deviceList.get(index);
    }

    public synchronized LncvDevice[] getDevices() {
        LncvDevice[] d = {};
        return deviceList.toArray(d);
    }
    public synchronized int size() {
        return deviceList.size();
    }

    private final static Logger log = LoggerFactory.getLogger(LncvDevices.class);

}
