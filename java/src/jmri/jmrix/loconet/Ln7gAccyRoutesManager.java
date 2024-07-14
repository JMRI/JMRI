package jmri.jmrix.loconet;

import javax.annotation.Nonnull;

import jmri.jmrix.loconet.alm.LnSimple7thGenDevicesRoutes;
import jmri.jmrix.loconet.alm.LnSimple7thGenDeviceRoutes;
import jmri.jmrix.loconet.alm.LnSimple7thGenRoute;
import jmri.jmrix.loconet.alm.LnSimpleRouteEntry;
import jmri.jmrix.loconet.alm.RouteSwitchPositionEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aptly
 */
public class Ln7gAccyRoutesManager implements LocoNetListener  {

    private static final String DEVICE_DS74 = "DS74";
    private static final String DEVICE_DS78V = "DS78V";
    private static final String DEVICE_PM74 = "PM74";
    private static final String DEVICE_SE74 = "SE74";

    private LocoNetSystemConnectionMemo memo;
    private final LnSimple7thGenDevicesRoutes devicesRoutes;
    private int activeDeviceType;
    private int activeDeviceSerNum;
    private int activeDeviceBaseAddr;
    private int activeEntrySet;
    private int activeRouteNum;
    
    public Ln7gAccyRoutesManager() {
        this.devicesRoutes = new LnSimple7thGenDevicesRoutes();
    }
    
    public void initComponents(LocoNetSystemConnectionMemo c) {
        memo = c;
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        deselectAlmRoutesDevice();
        // nothing more to do
    }

    public Ln7gAccyRoutesManager initContext(Object context) {
        if (context instanceof LocoNetSystemConnectionMemo) {
            initComponents((LocoNetSystemConnectionMemo)context);
        }
        return this;
    }
    private void deselectAlmRoutesDevice() {
        activeDeviceType = 0;
        activeDeviceSerNum = 0;
        activeDeviceBaseAddr = 0;
        activeEntrySet = -1;
        activeRouteNum = -1;
    }
    
    private void selectAlmRoutesDevice(int deviceType, int serNum, int baseAddr) {
        activeDeviceType = deviceType;
        activeDeviceSerNum = serNum;
        activeDeviceBaseAddr = baseAddr;
        activeEntrySet = -1;
        activeRouteNum = -1;
    }

    /**
     * Update from saved data.
     *
     * Read the file of devices/routes.
     */
    public  void initializeDeviceRoutes() {
        loadTheXML();
    }

    /**
     * Get String which shows all 7th Gen Accy devices which can store routes, 
     * with any route data.
     * @return string of 7th-gen Accy devices which can have enabled routes
     */
    public String showStoredDevicesWithRoutes() {
        if (getCountOfDevicesWithRoutes() < 1) {
            return "No Digitrax 7th-gen Accessory devices with stored/storable routes.";
        }
        StringBuilder s = new StringBuilder("Known Digitrax 7th-gen Accessory devices with stored/storable routes:\n");
        for (int i = 0; i < getCountOfDevicesWithRoutes(); i++) {
            LnSimple7thGenDeviceRoutes d = devicesRoutes.getDeviceRoutes(i);
            s.append("\tDevice ");
            s.append(d.getDeviceType());
            s.append(", ser. num. ");
            s.append(d.getSerNum());
            s.append(", base addr. ");
            s.append(d.getBaseAddr());

            for (int j = 0; j < d.getRoutes().length; ++j) {
                LnSimple7thGenRoute route = d.getRoutes(j);
                s.append("\n\tRoute ");
                s.append(j);
                s.append(": ");
                for (int l = 0; l < 8; l++) {
                    s.append(", ");
                    s.append(route.getRouteEntry(l).getNumber());
                    s.append(route.getRouteEntry(l).getPosition());
                }
            }
            s.append(".\n");
        }
        return s.toString();
    }


    /**
     * Returns the manager's memo.
     * @return a LocoNetSystemConnectionMemo
     */
    @Nonnull
    public LocoNetSystemConnectionMemo getMemo() {
        return memo;
    }

    private void checkMessage4Byte(LocoNetMessage m) {
        if ((m.getNumDataElements() == 4) &&
                (m.getOpCode() == 0xb4) && (m.getElement(1) == 0x6e) &&
                (m.getElement(2) == 0x7f)) {
            // Ignore the ALM device route write long ack response
            //      rather than accepting at the write request!
            log.trace("message: write acknowledge- Ignored.");
        }
    }
    
    private boolean checkReportAlmCSRoutesCapabilities(LocoNetMessage m) {
        if ((m.getNumDataElements() == 16) &&
                (m.getOpCode() == LnConstants.OPC_ALM_READ) && (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 0x1) && (m.getElement(3) == 0x0) &&
                (m.getElement(10) == 0) &&
                (m.getElement(11) == 0) && (m.getElement(12) == 0) &&
                (m.getElement(13) == 0) && (m.getElement(14) == 0)) {
            // [E6 10 01 00 00 02 03 02 10 7F 00 00 00 00 00 64]  Command Station Routes Capabilities reply: 64 routes, 16 entries each route.
            log.debug("Report ALM CS Routes Capabilities");
            // nothing to do at this time
            return true;
        }
        return false;
    }
    
    private boolean checkRequestDeviceRoutes(LocoNetMessage m) {
        if ((m.getNumDataElements() == 16) &&
                (m.getOpCode() == 0xeE) && (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 0x2) && (m.getElement(3) == 0x0e) &&
                (m.getElement(4) == 0) && (m.getElement(5) == 0) &&
                (m.getElement(6) == 0) && (m.getElement(7) == 0) &&
                (m.getElement(8) == 0)) {
            log.debug("request select ALM Device Routes");
            // Ignore byte 10: devopsw!
            // force the device as unselected (for now)!
            deselectAlmRoutesDevice();
            return true;
        }
        return false;
    }
    
    private boolean checkAlmDeviceIsSelected(LocoNetMessage m) {
        if ((m.getNumDataElements() == 16) &&
                (m.getOpCode() == 0xe6) && (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 0x2) && (m.getElement(3) == 0x0e) &&
                ((m.getElement(4) & 0x4f) == 0) && // bits 5:1 imply # routes
                (m.getElement(5) == 0) &&
                (m.getElement(6) == 0) && (m.getElement(7) == 0x02) &&
                (m.getElement(8) == 8)) {
            log.debug("response from device: ALM Device Routes selected");
            // Ignore byte 10: devopsw!
            // selected the currect ALM Routes device
            int dev = m.getElement(9);
            int ser = (m.getElement(12) << 7) + m.getElement(11);
            int base = (m.getElement(14) << 7) + m.getElement(13);

            // set the device as selected for routes read/write
            selectAlmRoutesDevice(dev, ser, base);
            return true;
        }
        return false;
    }

    private boolean checkAlmDeviceIsDeselected(LocoNetMessage m) {
        if ((m.getNumDataElements() == 16) &&
                (m.getOpCode() == 0xe6) && (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 0x2) && (m.getElement(3) == 0) &&
                (m.getElement(4) == 0) && (m.getElement(5) == 0) &&
                (m.getElement(7) == 0) && (m.getElement(8) == 0) &&
                (m.getElement(9) == 0) && (m.getElement(10) == 0) &&
                (m.getElement(11) == 0) && (m.getElement(12) == 0) &&
                (m.getElement(13) == 0) && (m.getElement(14) == 0)) {
            // Ignoring byte 6!

            // deselect any currect ALM Routes device
            log.debug("message: deselect the ALM routes device at the de-select request");
            deselectAlmRoutesDevice();
            return true;
        }
        return false;
    }

    private boolean checkRequestAlmDeviceReadRequest(LocoNetMessage m) {
        if ((m.getNumDataElements() == 16) &&
                (m.getOpCode() == 0xee) && (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 0x2) && (m.getElement(3) == 0x2) &&
                (m.getElement(7) == 0) && (m.getElement(8) == 0) &&
                (m.getElement(9) == 0) && (m.getElement(10) == 0) &&
                (m.getElement(11) == 0) && (m.getElement(12) == 0) &&
                (m.getElement(13) == 0) && (m.getElement(14) == 0)
                ) {
            // Ignoring byte 6!

            // watch for ALM device route read request; capture data for later use
            activeEntrySet = m.getElement(4) & 1;
            activeRouteNum = (m.getElement(4) >> 1) + (m.getElement(5) << 6);
            log.debug("message: read request - ALM 7th gen read - entry {} entrySet {}", activeRouteNum, activeEntrySet);
            return true;
        }
        return false;
    }

    private boolean checkAlmDeviceReadReport(LocoNetMessage m) {
        if ((m.getNumDataElements() == 16) &&
                (m.getOpCode() == 0xe6) && (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 0x2) && (m.getElement(3) == 0x2) &&
                (m.getElement(7) == 0) && (m.getElement(8) == 0) &&
                (m.getElement(9) == 0) && (m.getElement(10) == 0) &&
                (m.getElement(11) == 0) && (m.getElement(12) == 0) &&
                (m.getElement(13) == 0) && (m.getElement(14) == 0)
                ) {
            // Ignoring byte 6!

            // watch for ALM device route read report and keep the route data
            activeEntrySet = m.getElement(4) & 1;
            activeRouteNum = (m.getElement(4) >> 1) + (m.getElement(5) << 6);
            int entrya = m.getElement(6) + (m.getElement(7) << 7);
            int entryb = m.getElement(8) + (m.getElement(8) << 7);
            int entryc = m.getElement(10) + (m.getElement(11) << 7);
            int entryd = m.getElement(12) + (m.getElement(13) << 7);

            log.debug("message: read report - ALM 7th gen read - entry {} entrySet {}: entryA = {}, entryB = {}, entryC = {}, entryD = {}.",
                    activeEntrySet, activeEntrySet, entrya, entryb, entryc, entryd);
            saveData(activeDeviceType, activeDeviceSerNum,
                    activeDeviceBaseAddr, activeEntrySet, activeRouteNum,
                    entrya, entryb, entryc, entryd);
            return true;
        }
        return false;
    }

    private boolean checkAlmDeviceWriteRequest(LocoNetMessage m) {
        if ((m.getNumDataElements() == 16) &&
                (m.getOpCode() == 0xee) && (m.getElement(1) == 0x10) &&
                (m.getElement(2) == 0x2) && (m.getElement(3) == 0x3)) {
            // Ignoring byte 6!

            // watch for ALM device route write request
            activeEntrySet = m.getElement(4) & 1;
            activeRouteNum = (m.getElement(4)>>1) + (m.getElement(5) << 6);
            // get 4 entries
            int entrya = m.getElement(7) + (m.getElement(8) << 7);
            int entryb = m.getElement(9) + (m.getElement(10) << 7);
            int entryc = m.getElement(11) + (m.getElement(12) << 7);
            int entryd = m.getElement(13) + (m.getElement(14) << 7);

            log.debug("message: write request - ALM 7th gen write - entry {} entrySet {}: entryA = {}, entryB = {}, entryC = {}, entryD = {}.\n",
                    activeEntrySet, activeEntrySet, entrya, entryb, entryc, entryd);

            saveData(activeDeviceType, activeDeviceSerNum, activeDeviceBaseAddr,
                activeEntrySet, activeRouteNum,
                entrya, entryb, entryc, entryd);
            return true;
        }
        return false;
    }

    @Override
    public void message(LocoNetMessage m) {
        log.trace("RtsMgr:message - length {}", m.getNumDataElements());
        if (checkReportAlmCSRoutesCapabilities(m)) {
        } else if (checkRequestDeviceRoutes(m)) {
        } else if (checkAlmDeviceIsSelected(m)) {
        } else if (checkAlmDeviceIsDeselected(m)) {
        } else if (checkRequestAlmDeviceReadRequest(m)) {
        } else if (checkAlmDeviceReadReport(m)) {
        } else if (checkAlmDeviceWriteRequest(m)) {
        } else {
            checkMessage4Byte(m);
        }
    }

    /**
     * Save 4 entries of a route.
     *
     * @param deviceType Device type number
     * @param deviceSerNum Device serial number
     * @param deviceBaseAddr Device base address
     * @param entrySet Entry "set" number
     * @param routeNum Route number
     * @param entrya Entry A
     * @param entryb Entry B
     * @param entryc Entry C
     * @param entryd Entry D
     */
     public void saveData(int deviceType, int deviceSerNum,
            int deviceBaseAddr, int entrySet, int routeNum,
            int entrya, int entryb, int entryc, int entryd) {

        log.debug("saveData: updating 4 entries for device {} serNum {} routeNum {}"
                + " entries {} to {}, \n\twith entrya = {}, entryb = {}, entryc = {}, entryd = {}.",
                deviceType, deviceSerNum, routeNum,
                entrySet * 4, (entrySet *4) + 3,
                entrya, entryb, entryc, entryd);

        // find/create the device's storage
        LnSimple7thGenDeviceRoutes device = devicesRoutes.getDeviceRoutes(deviceType, deviceSerNum);
        if (device == null) {
            //  Didn't find one.  Create one
            addDevice(new LnSimple7thGenDeviceRoutes(deviceType, deviceSerNum));

            device = devicesRoutes.getDeviceRoutes(deviceType, deviceSerNum);
            device.setBaseAddr(deviceBaseAddr);
            log.debug("saveData: New device's type = {}, serNum = {}, baseAddr = {}",
                    device.getDeviceType(), device.getSerNum(), device.getBaseAddr());
        }
        // update the four entries at routeNum, "entrySet" half
        device.setFourEntries(routeNum, entrySet, entrya, entryb, entryc, entryd);

        LnSimple7thGenDeviceRoutes d = devicesRoutes.getDeviceRoutes(deviceType, deviceSerNum);
        LnSimpleRouteEntry ea = d.getRoutes(routeNum).getRouteEntry(4 * entrySet);
        LnSimpleRouteEntry eb = d.getRoutes(routeNum).getRouteEntry((4 * entrySet) + 1);
        LnSimpleRouteEntry ec = d.getRoutes(routeNum).getRouteEntry(4 * entrySet + 2);
        LnSimpleRouteEntry ed = d.getRoutes(routeNum).getRouteEntry(4 * entrySet + 3);
        log.debug("saveData: device {} serNum {} route {}. entrySet {}, a {}, b {}, c {}, d {}",
                d.getDeviceType(), d.getSerNum(), routeNum, entrySet,
                Integer.toString(ea.getNumber())+ea.getPosition().toString(),
                Integer.toString(eb.getNumber())+eb.getPosition().toString(),
                Integer.toString(ec.getNumber())+ec.getPosition().toString(),
                Integer.toString(ed.getNumber())+ed.getPosition().toString()
        );
    }

    public String getDevName(int devType) {
        switch (devType) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
                return DEVICE_DS74;
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
                return DEVICE_DS78V;
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
                return DEVICE_PM74;
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                return DEVICE_SE74;
            default:
                throw new IllegalArgumentException("Bad Device Type: " +
                    Integer.toString(devType) + ".");
        }
    }

    public int getDevType(String deviceName) {
        switch (deviceName) {
            case DEVICE_DS74:
                return LnConstants.RE_IPL_DIGITRAX_HOST_DS74;
            case DEVICE_DS78V:
                return LnConstants.RE_IPL_DIGITRAX_HOST_DS78V;
            case DEVICE_PM74:
                return LnConstants.RE_IPL_DIGITRAX_HOST_PM74;
            case DEVICE_SE74:
                return LnConstants.RE_IPL_DIGITRAX_HOST_SE74;
            default:
                throw new IllegalArgumentException("Wrong device name: "+deviceName+".");
        }
    }

    public boolean loadTheXML() {
        log.debug("loadXML: starting; current getCountOfDevicesWithRoutes is {}",
                getCountOfDevicesWithRoutes());
        // get info from file
        jmri.jmrix.loconet.configurexml.Digitrax7thGenAccyRoutesXML xmlThingy =
                new jmri.jmrix.loconet.configurexml.Digitrax7thGenAccyRoutesXML(this);
        xmlThingy.loadXML();

        log.debug("loadTheXML: Finished reading the 'DigitraxRoutes' file.");
        return true;
    }

    public  int getCountOfDevicesWithRoutes() {
        return devicesRoutes.size();
    }

    public  void addDeviceRoutesRoute(String devType, int serNum, int baseNum,
            int[][] turnouts, RouteSwitchPositionEnum[][] positions) {
        // get device type (IPL) number
        int idevNumber = LnSimple7thGenDeviceRoutes.getDeviceType(devType);

        log.debug("addDeviceRoutesRoute: checking for device already entered - devNumber {}, serNum {}.",
                idevNumber, serNum);
        // get a device by number & serial number
        LnSimple7thGenDeviceRoutes  existingDevRts =
                devicesRoutes.getDeviceRoutes(idevNumber, serNum );

        if (existingDevRts != null) {
            devicesRoutes.removeExistingDevice(idevNumber, serNum);
            log.debug("removed previous device in favor of the current data");
        }
        LnSimple7thGenDeviceRoutes ls7gdr = new LnSimple7thGenDeviceRoutes(idevNumber, serNum);
        ls7gdr.setBaseAddr(baseNum);

        for (int i = 0; i < (devType.equalsIgnoreCase(DEVICE_DS78V)?16:8); ++i) {
            for (int j = 0; j < 8; ++j) {
                ls7gdr.setOneEntry(i, j, turnouts[i][j], positions[i][j]);
            }
        }
        addDevice(ls7gdr);
    }

    public  void addDevice(LnSimple7thGenDeviceRoutes dev) {
        devicesRoutes.add(dev);
        log.debug("addDevice: done adding the device!");
    }

    public  LnSimple7thGenDeviceRoutes getDevice(int i) {
        return devicesRoutes.getDeviceRoutes(i);
    }


    public void dispose() {
        memo.getLnTrafficController().removeLocoNetListener(~0, this);
    }

    private final static Logger log = LoggerFactory.getLogger(Ln7gAccyRoutesManager.class);
}
