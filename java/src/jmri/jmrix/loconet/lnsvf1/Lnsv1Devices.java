package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrix.loconet.Lnsv1DevicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage an array of LnSv1Device items. See {@link Lnsv1DevicesManager}
 * Based on Lnsvf2Devices by B. Milhaupt
 * @author Egbert Broerse 2020, 2025
 */
public class Lnsv1Devices {

    @GuardedBy("this")
    private final List<Lnsv1Device> deviceList;

    public Lnsv1Devices() {
        deviceList = new ArrayList<>();
    }

    /**
     * Add a device that responded to a PROG_START request to the list of LNCV Devices.
     *
     * @param d the device object, containing its properties
     * @return true if device was added, false if not eg it was already in the list
     */
    public synchronized boolean addDevice(Lnsv1Device d) {
        if (!deviceExists(d)) {
            deviceList.add(d);
            log.debug("added device with version {}, addr {}",
                    d.getSwVersion(), d.getDestAddr());
            return true;
        } else {
            log.debug("device already in list: version {}, addr {}",
                    d.getSwVersion(), d.getDestAddr());
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
     * @param deviceToBeFound Device we try to find in known LNSV1 devices list
     * @return index of found device, else -1 if matching device not found
     */
    public synchronized int isDeviceExistant(Lnsv1Device deviceToBeFound) {
        log.debug("Looking for a known LNSV1 device which matches characteristics: version {}, addr {}.",
                deviceToBeFound.getSwVersion(),
                deviceToBeFound.getDestAddr());
        for (int i = 0; i < deviceList.size(); ++i) {
            Lnsv1Device dev = deviceList.get(i);
            log.trace("Comparing against known device: version {}, addr {}.",
                    dev.getSwVersion(),
                    deviceToBeFound.getDestAddr());
            if ((deviceToBeFound.getSwVersion() == -1) ||
                    (dev.getSwVersion() == deviceToBeFound.getSwVersion())) {
                if ((deviceToBeFound.getDestAddr() == -1) ||
                        (dev.getDestAddr() == deviceToBeFound.getDestAddr())) {
                    log.debug("Match Found! Searched device matched against known device: article {}, addr {}.",
                            dev.getSwVersion(),
                            dev.getDestAddr());
                    return i;
                }
            }
        }
        log.debug("No matching known device was found!");
        return -1;
    }

    public boolean deviceExists(Lnsv1Device d) {
        int i = isDeviceExistant(d);
        log.debug("deviceExists found {}", i);
        return (i >= 0);
    }

    public synchronized Lnsv1Device getDevice(int index) {
        return deviceList.get(index);
    }

    public synchronized Lnsv1Device[] getDevices() {
        Lnsv1Device[] d = {};
        return deviceList.toArray(d);
    }
    public synchronized int size() {
        return deviceList.size();
    }

    private final static Logger log = LoggerFactory.getLogger(Lnsv1Devices.class);

}
