package jmri.jmrix.loconet;

import java.util.ArrayList;
import java.util.List;

import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import jmri.jmrix.ProgrammingTool;
import jmri.jmrix.loconet.uhlenbrock.LncvMessageContents;
import jmri.jmrix.loconet.uhlenbrock.LncvDevice;
import jmri.jmrix.loconet.uhlenbrock.LncvDevices;
import jmri.managers.DefaultProgrammerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocoNet LNCV Devices Manager
 *
 * A centralized resource to help identify LocoNet "LNCV Format"
 * devices and "manage" them.
 *
 * Supports the following features:
 *  - LNCV "discovery" process ? not supported
 *  - LNCV Device "destination address" change ? not supported
 *  - LNCV Device "reconfigure/reset" ? not supported
 *  - identification of devices with conflicting "destination address"es
 *  - identification of a matching JMRI "decoder definition" for each discovered
 *    device, if an appropriate definition exists
 *  - identification of matching JMRI "roster entry" which matches each
 *    discovered device, if an appropriate roster entry exists
 *  - ability to open a symbolic programmer for a given discovered device, if
 *    an appropriate roster entry exists
 *
 * @author B. Milhaupt Copyright (c) 2020
 *
 */

public class LncvDevicesManager extends PropertyChangeSupport
        implements LocoNetListener {
    private final LocoNetSystemConnectionMemo memo;
    private volatile LncvDevices lncvDevices;
    private List<Integer> readLncvAddressList;
    //private java.util.TimerTask delayTask = null;
    private volatile boolean waitingForDiscoveryReplies;

    public LncvDevicesManager(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        } else {
            log.error("No LocoNet connection available, this tool cannot function"); // NOI18N
        }
        lncvDevices = new LncvDevices();
        readLncvAddressList = new ArrayList<>();
        waitingForDiscoveryReplies = false;
    }

    public LncvDevices getDeviceList() {
        return lncvDevices;
    }

    public void clearDevicesList() {
        lncvDevices.removeAllDevices();

        jmri.util.ThreadingUtil.runOnLayoutEventually( ()->{
            firePropertyChange("DeviceListChanged", true, false);
        });
    }

    public void sendLncvDiscoveryRequest() {
        memo.getLnTrafficController().sendLocoNetMessage(
                LncvMessageContents.createAllProgStartRequest(0));
    }

    public void message(LocoNetMessage m) {
//            if (LncvMessageContents.isLnMessageASpecificLncvCommand(m,
//                    LncvMessageContents.LncvCommand.SV2_DISCOVER_DEVICE_REPORT)) {
//                // deal with a discovery reply!
//                LncvMessageContents sv2c = new LncvMessageContents(m);
//
//                int mfgId = sv2c.getLncvManufacturerID();
//                int prodId = sv2c.getLncvProductID();
//                int devId = sv2c.getLncvDeveloperID();
//                int serNum = sv2c.getLncvSerialNum();
//                int addr = sv2c.getDestAddr();
//
//                sv2Devices.addDevice(new LncvDevice(mfgId, devId, prodId, addr, serNum, "", "", -1));
//                log.debug("new sv2device added");
//                firePropertyChange("DeviceListChanged", true, false);
//                waitingForDiscoveryReplies = true;
//                if (delayTask == null) {
//                    delayTask = new java.util.TimerTask() {
//                        @Override
//                        public void run() {
//                            if (!waitingForDiscoveryReplies) {
//                                if (readLncvAddressList.size() > 0) {
//                                    queryLncvValues();
//                                }
//                            } else {
//                                waitingForDiscoveryReplies = false;
//                            }
//                        }};
//                    jmri.util.TimerUtil.scheduleAtFixedRateOnLayoutThread(delayTask, 500, 500);
//                }
//                if (addr > 0) {
//                    readLncvAddressList.add(addr);
//                }
//            } else
        if (LncvMessageContents.isSupportedLncvMessage(m)) {
            //LncvMessageContents lncvMsg = new LncvMessageContents(m);
            LncvMessageContents.LncvCommand type = LncvMessageContents.extractMessageType(m);
            if (type == null) {
                log.error("Unknown LNCV Message Type");
                return;
            }
//            switch (type) {
//                    case SV2_REPORT_ONE:
//                        if (lncvMsg.getSVNum() == 2) {
//                            int addr = lncvMsg.getDestAddr();
//                            int val = lncvMsg.getLncvD1();
//                            log.debug("SVF2 read reply: device address {} of LNCV returns {}", addr, val);
//                            // Annotate the discovered device LNCV data based on address
//                            int count = sv2Devices.size();
//                            for (int i = 0; i < count; ++ i) {
//                                LncvDevice d = sv2Devices.getDevice(i);
//                                if (d.getDestAddr() == addr) {
//                                    d.setSwVersion(val);
//
//                                    // need to find a corresponding roster entry?
//                                    if(d.getRosterName().length() == 0) {
//                                        // Yes. Try to find a roster entry which matches the device characteristics
//                                        List<RosterEntry> l = Roster.getDefault().matchingList(null,
//                                                null,
//                                                Integer.toString(d.getDestAddr()),
//                                                null,
//                                                null,
//                                                null,
//                                                null,
//                                                Integer.toString(d.getDeveloperID()),
//                                                Integer.toString(d.getManufacturerID()),
//                                                Integer.toString(d.getProductID()));
//                                        if (l.size() == 0) {
//                                            log.debug("Did not find a corresponding roster entry");
//                                        } else if (l.size() == 1) {
//                                            log.debug("Found a matching roster entries.");
//                                            d.setRosterEntry(l.get(0));
//                                        } else {
//                                            log.info("Found multiple matching roster entries. "
//                                                    + "Cannot associate any one to this device.");
//                                        }
//                                    }
//                                    // notify listeners of pertinent change to device
//                                    firePropertyChange("DeviceListChanged", true, false);
//                                }
//                            }
//                        }
//                        break;
//                    case SV2_CHANGE_DEVICE_ADDRESS_REPLY:
//                        int destAddr = svMsg.getDestAddr();
//                        int serNum = svMsg.getLncvSerialNum();
//                        int mfgId = svMsg.getLncvManufacturerID();
//                        int develId = svMsg.getLncvDeveloperID();
//                        int prodId = svMsg.getLncvProductID();
//                        log.debug("got chg addr reply for mfg {}, prod {}, devel {}, serNum {}, destAddr {}",
//                                mfgId, prodId, develId, serNum, destAddr);
//                        LncvDevice foundDevice = new LncvDevice(mfgId, develId,
//                                prodId, -1, serNum, null, null, -1);
//
//                        int index = sv2Devices.isDeviceExistant(foundDevice);
//                        if (index >=0) {
//                            log.debug("found device {}, setting destAddr {}", index, destAddr);
//                            sv2Devices.getDevice(index).setDestAddr(destAddr);
//                            firePropertyChange("DeviceListChanged", true, false);
//                        }
//                        break;
//                default:
//                    break;
//            }
        }
    }

    private void queryLncvValues() {
        if (readLncvAddressList.size() > 0) {
            int art = readLncvAddressList.get(1);
            int addr = readLncvAddressList.get(0);
            readLncvAddressList.remove(0);
            memo.getLnTrafficController().sendLocoNetMessage(
                    LncvMessageContents.createCvReadRequest(art, addr, 2));
        }
    }

//        public void reconfigResetDevice(LncvDevice dev) {
//            memo.getLnTrafficController().sendLocoNetMessage(
//                    LncvMessageContents.createLncvMessage(
//                            0,
//                            LncvMessageContents.LncvCommand.SV2_RECONFIGURE_DEVICE.getCmd(),
//                            dev.getDestAddr(),
//                            0, 0, 0, 0, 0));
//        }
    public void reprogramDeviceAddress(LncvDevice dev, int newAddr) {
        int art = dev.getClassNum();
        memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createCvWriteRequest(
                art, 0, newAddr));
    }

    public ProgrammingResult prepareForSymbolicProgrammer(LncvDevice dev, ProgrammingTool t) {

        if (lncvDevices.isDeviceExistant(dev) < 0) {
            return ProgrammingResult.FAIL_NO_SUCH_DEVICE;
        }
        int destAddr = dev.getDestAddr();
        if (destAddr == 0) {
            return ProgrammingResult.FAIL_DESTINATION_ADDRESS_IS_ZERO;
        }
        int deviceCount = 0;
        for (LncvDevice d: lncvDevices.getDevices()) {
            if (destAddr == d.getDestAddr()) {
                deviceCount++;
            }
        }
        if (deviceCount > 1) {
            return ProgrammingResult.FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS;
        }

        if ((dev.getRosterName()==null) || (dev.getRosterName().length()==0)) {
            return ProgrammingResult.FAIL_NO_MATCHING_ROSTER_ENTRY;
        }

        DefaultProgrammerManager pm = memo.getProgrammerManager();
        if (pm == null) {
            return ProgrammingResult.FAIL_NO_APPROPRIATE_PROGRAMMER;
        }
        Programmer p = pm.getAddressedProgrammer(false, dev.getDestAddr());
        if (p == null) {
            return ProgrammingResult.FAIL_NO_ADDRESSED_PROGRAMMER;
        }

        if (!p.getSupportedModes().contains(LnProgrammerManager.LOCONETLNCVMODE)) {
            return ProgrammingResult.FAIL_NO_LNCV_PROGRAMMER;
        }
        p.setMode(LnProgrammerManager.LOCONETLNCVMODE);
        ProgrammingMode prgMode = p.getMode();
        if (!prgMode.equals(LnProgrammerManager.LOCONETLNCVMODE)) {
            return ProgrammingResult.FAIL_NO_LNCV_PROGRAMMER;
        }

        RosterEntry re = Roster.getDefault().entryFromTitle(dev.getRosterName());
        String name = re.getId();

        t.openPaneOpsProgFrame(re, name, "programmers/Comprehensive.xml", p); // NOI18N
        return ProgrammingResult.SUCCESS_PROGRAMMER_OPENED;
    }

    public enum ProgrammingResult {
        SUCCESS_PROGRAMMER_OPENED,
        FAIL_NO_SUCH_DEVICE,
        FAIL_NO_APPROPRIATE_PROGRAMMER,
        FAIL_NO_MATCHING_ROSTER_ENTRY,
        FAIL_DESTINATION_ADDRESS_IS_ZERO,
        FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS,
        FAIL_NO_ADDRESSED_PROGRAMMER,
        FAIL_NO_LNCV_PROGRAMMER
    }

    private final static Logger log = LoggerFactory.getLogger(LncvDevicesManager.class);

}
