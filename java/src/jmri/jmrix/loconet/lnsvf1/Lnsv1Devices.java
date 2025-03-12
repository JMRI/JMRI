package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrix.loconet.Lnsv1DevicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage an array of Lnsv1Device items. See {@link Lnsv1DevicesManager}
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
     * Add a device that responded to a PROBE_ALL request (or simply sent a READ_ONE reply) to the list of LNSV1 Devices.
     *
     * @param d the device object, containing its properties
     * @return true if device was added, false if not e.g. it was already in the list
     */
    public synchronized boolean addDevice(Lnsv1Device d) {
        if (!deviceExists(d)) {
            deviceList.add(d);
            log.debug("added device with addr {}", d.getDestAddr());
            return true;
        } else {
            log.debug("device already in list: addr {} ", d.getDestAddr());
            return false;
        }
    }

    public synchronized void removeAllDevices() {
        deviceList.clear();
    }

    /**
     * Get index in deviceList of the first device matching (only) the Device Address.
     * Where a deviceToBeFound parameter is -1, that parameter is not compared.
     *
     * @param deviceToBeFound Device we try to find in known LNSV1 devices list
     * @return index of found device, -1 if matching device not found
     */
    public synchronized int isDeviceExistant(Lnsv1Device deviceToBeFound) {
        log.debug("Looking for a known LNSV1 device which matches characteristics: address {}.",
                deviceToBeFound.getDestAddr());
        for (int i = 0; i < deviceList.size(); ++i) {
            Lnsv1Device dev = deviceList.get(i);
            log.debug("Comparing against known device: addr {}.",
                    deviceToBeFound.getDestAddr());
            if ((deviceToBeFound.getDestAddr() == -1) ||
                    (dev.getDestAddr() == deviceToBeFound.getDestAddr())) {
                log.debug("Match Found! Searched device matched against known device: addr {}.",
                        dev.getDestAddr());
                return i;
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
