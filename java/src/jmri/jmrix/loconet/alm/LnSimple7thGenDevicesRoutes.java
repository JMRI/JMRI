package jmri.jmrix.loconet.alm;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author B. Milhaupt (C) 2024
 */
public class LnSimple7thGenDevicesRoutes {
    private final ArrayList<LnSimple7thGenDeviceRoutes> devicesRoutes;

    /**
     * Constructor.
     */
    public LnSimple7thGenDevicesRoutes() {
        devicesRoutes = new ArrayList<>();
    }

    /**
     * Add a LnSimple7thGenDeviceRoutes object.
     *
     * When the device (by device type and serial number) is not yet in the
     * devicesRoutes list, a new device is added.
     *
     * When a device (by device type and serial number) exists,
     * that device gets updated.
     *
     * @param dr "DeviceRoutes" to be added
     */
    public void add(LnSimple7thGenDeviceRoutes dr) {
        LnSimple7thGenDeviceRoutes deviceRoutes;

        if (isDeviceRoutes(dr)) {
            deviceRoutes = getDeviceRoutes(dr);
        } else {
            deviceRoutes = new LnSimple7thGenDeviceRoutes(dr.getDeviceType(),
                    dr.getSerNum());
        }

        // move all dr's routes to the new deviceRoutes object
        deviceRoutes.setRoutes(dr.getRoutes());

        // add the deviceRoutes object as a devicesRoutes.
        devicesRoutes.add(deviceRoutes);
    }

    /**
     * Report if device is in the list.
     *
     * Checks device type and serial number.
     *
     * @param dr LnSimple7thGenDeviceRoutes device
     * @return true if the device is in the list, else false
     */
    public boolean isDeviceRoutes(LnSimple7thGenDeviceRoutes dr) {
        for (Iterator<LnSimple7thGenDeviceRoutes> it = devicesRoutes.iterator(); it.hasNext();) {
            LnSimple7thGenDeviceRoutes lndr = it.next();
            if ((lndr.getDeviceType() == dr.getDeviceType()) &&
                    (lndr.getSerNum() == dr.getSerNum())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Report if device is in the list.
     *
     * Checks device type and serial number.
     *
     * @param deviceType - integer
     * @param serNum - integer
     * @return true if the device is in the list, else false
     */
    public boolean isDeviceRoutes(int deviceType, int serNum) {
        for (Iterator<LnSimple7thGenDeviceRoutes> it = devicesRoutes.iterator(); it.hasNext();) {
            LnSimple7thGenDeviceRoutes lndr = it.next();
            if ((lndr.getDeviceType() == deviceType) &&
                    (lndr.getSerNum() == serNum)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get LnSimple7thGenDeviceRoutes based on devType and serNum, or null.
     *
     * @param deviceType - integer
     * @param serNum - integer
     * @return LnSimple7thGenDeviceRoutes
     */
    public LnSimple7thGenDeviceRoutes getDeviceRoutes(int deviceType, int serNum) {
        for (Iterator<LnSimple7thGenDeviceRoutes> it = devicesRoutes.iterator(); it.hasNext();) {
            LnSimple7thGenDeviceRoutes lndr = it.next();
            if ((lndr.getDeviceType() == deviceType) &&
                    (lndr.getSerNum() == serNum)) {
                return lndr;
            }
        }
        return null;
    }

    /**
     * Get LnSimple7thGenDeviceRoutes based on devType and serNum, or null.
     *
     * Data from the dr parameter is ignored except the Device Type and Serial
     * Number.
     *
     * @param dr - LnSimple7thGenDeviceRoutes
     * @return LnSimple7thGenDeviceRoutes
     */
    public LnSimple7thGenDeviceRoutes getDeviceRoutes(LnSimple7thGenDeviceRoutes dr) {
        for (Iterator<LnSimple7thGenDeviceRoutes> it = devicesRoutes.iterator(); it.hasNext();) {
            LnSimple7thGenDeviceRoutes lndr = it.next();
            if ((lndr.getDeviceType() == dr.getDeviceType()) &&
                    (lndr.getSerNum() == dr.getSerNum())) {
                return lndr;
            }
        }
        return null;
    }

    /**
     * Remove device.
     *
     * @param devType - int
     * @param serNum - int
     */
    public void removeExistingDevice(int devType, int serNum) {
        for (Iterator<LnSimple7thGenDeviceRoutes> it = devicesRoutes.iterator(); it.hasNext();) {
            LnSimple7thGenDeviceRoutes lndr = it.next();
            if ((lndr.getDeviceType() == devType) &&
                    (lndr.getSerNum() == serNum)) {
                devicesRoutes.remove(lndr);
                return;
            }
        }
        log.warn("removeExistingDevice: device type/serNum not found.");
    }

    /**
     * report the number of devices.
     * @return integer
     */
    public int size() {
        return devicesRoutes.size();
    }

    /**
     * get a LnSimple7thGenDeviceRoutes by its index.
     *
     * @param i - index, 0 thru (size()-1)
     * @return LnSimple7thGenDeviceRoutes
     */
    public LnSimple7thGenDeviceRoutes getDeviceRoutes(int i) {
        if ((devicesRoutes.size() > i) && (i >= 0)) {
            return devicesRoutes.get(i);
        }
        return null;
    }
    private final static Logger log = LoggerFactory.getLogger(LnSimple7thGenDevicesRoutes.class);
}
