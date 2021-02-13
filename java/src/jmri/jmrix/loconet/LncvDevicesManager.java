package jmri.jmrix.loconet;

//import java.util.ArrayList;
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

import jmri.progdebugger.ProgDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

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
 */

public class LncvDevicesManager extends PropertyChangeSupport
        implements LocoNetListener {
    private final LocoNetSystemConnectionMemo memo;
    private final LncvDevices lncvDevices;
    //private List<Integer> readLncvAddressList;
    //private java.util.TimerTask delayTask = null;
    //private volatile boolean waitingForDiscoveryReplies;

    public LncvDevicesManager(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        } else {
            log.error("No LocoNet connection available, this tool cannot function"); // NOI18N
        }
        lncvDevices = new LncvDevices();
        //readLncvAddressList = new ArrayList<>();
        //waitingForDiscoveryReplies = false;
    }

    public LncvDevices getDeviceList() {
        return lncvDevices;
    }

    public int getDeviceCount() {
        return lncvDevices.size();
    }

    public void clearDevicesList() {
        lncvDevices.removeAllDevices();

        jmri.util.ThreadingUtil.runOnLayoutEventually( ()-> firePropertyChange("DeviceListChanged", true, false));
    }

    public void message(LocoNetMessage m) {
        if (LncvMessageContents.isSupportedLncvMessage(m)) {
            if (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_READ_REPLY) {
                // it's an LNCV ReadReply message, decode contents:
                LncvMessageContents contents = new LncvMessageContents(m);
                int art = contents.getLncvArticleNum();
                int addr = -1;
                int cv = contents.getCvNum();
                int val = contents.getCvValue();
                log.debug("LNCV read reply: art:{}, address:{} cv:{} val:{}", art, addr, cv, val);
                if (cv == 0) { // trust last used address
                    addr = val; // if cvNum = 0, this is the LNCV module address
                    log.debug("LNCV read reply: device address {} of LNCV returns {}", addr, val);

                    lncvDevices.addDevice(new LncvDevice(art, addr, cv, val, "", "", -1));
                    log.debug("new LncvDevice added to table");
                    //firePropertyChange("DeviceListChanged", true, false);
//                    waitingForDiscoveryReplies = true;
//                    if (delayTask == null) {
//                        delayTask = new java.util.TimerTask() {
//                            @Override
//                            public void run() {
//                                if (!waitingForDiscoveryReplies) {
//                                    if (readLncvAddressList.size() > 0) {
//                                        queryLncvValues();
//                                    }
//                                } else {
//                                    waitingForDiscoveryReplies = false;
//                                }
//                            }};
//                        jmri.util.TimerUtil.scheduleAtFixedRateOnLayoutThread(delayTask, 500, 500);
//                    }
//                    if (addr > 0) {
//                        readLncvAddressList.add(addr); // TODO check for identical addresses? see SV2
//                    }

                    // Annotate the discovered device LNCV data based on address
                    int count = lncvDevices.size();
                    for (int i = 0; i < count; ++ i) {
                        LncvDevice dev = lncvDevices.getDevice(i);
                        if ((dev.getProductID() == art) && (dev.getDestAddr() == addr)) {
                            // need to find a corresponding roster entry?
                            if (dev.getRosterName() != null && dev.getRosterName().length() == 0) {
                                // Yes. Try to find a roster entry which matches the device characteristics
                                log.debug("Looking for prodID {}/adr {} in Roster", dev.getProductID(), dev.getDestAddr());
                                List<RosterEntry> l = Roster.getDefault().matchingList(
                                        Integer.toString(dev.getDestAddr()),
                                        Integer.toString(dev.getProductID()));
                                log.debug("LncvDeviceManager found {} matches in roster", l.size());
                                if (l.size() == 0) {
                                    log.debug("Did not find a corresponding roster entry");
                                } else if (l.size() == 1) {
                                    log.debug("Found a matching roster entry.");
                                    dev.setRosterEntry(l.get(0)); // link this device to the entry
                                } else {
                                    JOptionPane.showMessageDialog(null, // TODO I18N
                                            "The Roster contains multiple LNCV art. " + addr
                                            + " modules with address " + l.size() + ".\nMake sure each module"
                                            + "has a unique address.\n(disconnect all but 1 of these modules,"
                                            + "open new Module Programming Session and write new address to CV0)",
                                            "Open Roster Entry", JOptionPane.WARNING_MESSAGE);
                                    log.info("Found multiple matching roster entries. "
                                            + "Cannot associate any one to this device.");
                                }
                            }
                            // notify listeners of pertinent change to device
                            firePropertyChange("DeviceListChanged", true, false);
                        }
                    }
                } else {
                    log.debug("LNCV device check skipped as value not CV0/module address");
                }
            }
        }
    }

    public LncvDevice getDevice(int art, int addr) {
        int count = lncvDevices.size();
        for (int i = 0; i < count; ++ i) {
            LncvDevice dev = lncvDevices.getDevice(i);
            if ((dev.getProductID() == art) && (dev.getDestAddr() == addr)) {
                return dev;
            }
        }
        return null;
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
        log.debug("prepareForSymbolicProgrammer found {} matches", deviceCount);
        if (deviceCount > 1) {
            return ProgrammingResult.FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS;
        }

        if ((dev.getRosterName() == null) || (dev.getRosterName().length() == 0)) {
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

        if (p.getClass() != ProgDebugger.class) {
            // ProgDebugger used for LocoNet HexFile Sim, setting progMode not required so skip allows testing of LNCV Tool
            if (!p.getSupportedModes().contains(LnProgrammerManager.LOCONETLNCVMODE)) {
                return ProgrammingResult.FAIL_NO_LNCV_PROGRAMMER;
            }
            p.setMode(LnProgrammerManager.LOCONETLNCVMODE);
            ProgrammingMode prgMode = p.getMode();
            if (!prgMode.equals(LnProgrammerManager.LOCONETLNCVMODE)) {
                return ProgrammingResult.FAIL_NO_LNCV_PROGRAMMER;
            }
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
