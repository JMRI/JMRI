package jmri.jmrix.loconet;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ProgrammingTool;
import jmri.jmrix.loconet.lnsvf1.Lnsv1Device;
import jmri.jmrix.loconet.lnsvf1.Lnsv1Devices;
import jmri.jmrix.loconet.lnsvf1.Lnsv1MessageContents;
import jmri.managers.DefaultProgrammerManager;
//import jmri.progdebugger.ProgDebugger;

import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

import javax.annotation.concurrent.GuardedBy;
import java.util.List;

/**
 * LocoNet LNSV1 Devices Manager
 * <p>
 * A centralized resource to help identify LocoNet "LNSV1 Format"
 * devices and "manage" them.
 * <p>
 * Supports the following features:
 *  - LNSV1 "discovery" process supported via BROADCAST call
 *  - LNSV1 Device "destination address" change supported by writing a new value to LNSV 0 (close session next)
 *  - LNSV1 Device "reconfigure/reset" not supported/documented
 *  - identification of devices with conflicting "destination address"es (warning before program start)
 *  - identification of a matching JMRI "decoder definition" for each discovered
 *    device, if an appropriate definition exists (only 1 value is matched, checks for LNSVf1 protocol support)
 *  - identification of matching JMRI "roster entry" which matches each
 *    discovered device, if an appropriate roster entry exists
 *  - ability to open a symbolic programmer for a given discovered device, if
 *    an appropriate roster entry exists
 *
 * @author B. Milhaupt Copyright (c) 2020
 * @author Egbert Broerse (c) 2021, 2025
 */

public class Lnsv1DevicesManager extends PropertyChangeSupport
        implements LocoNetListener, jmri.Disposable {
    private final LocoNetSystemConnectionMemo memo;
    @GuardedBy("this")
    private final Lnsv1Devices lnsv1Devices;

    // constant for thread name, with memo prefix appended.
    static final String ROSTER_THREAD_NAME = "rosterMatchingListLnsv1DM";

    public Lnsv1DevicesManager(@javax.annotation.Nonnull LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        } else {
            log.error("No LocoNet connection available, this tool cannot function"); // NOI18N
        }
        synchronized (this) {
            lnsv1Devices = new Lnsv1Devices();
        }
    }

    public synchronized Lnsv1Devices getDeviceList() {
        return lnsv1Devices;
    }

    public synchronized int getDeviceCount() {
        return lnsv1Devices.size();
    }

    public void clearDevicesList() {
        synchronized (this) {
            lnsv1Devices.removeAllDevices();
        }
        jmri.util.ThreadingUtil.runOnLayoutEventually( ()-> firePropertyChange("DeviceListChanged", true, false));
    }

    /**
     * Extract module information from a LNSVf1 READ_ONE REPLY message.
     * If not already in the lnsv1Devices list, try to find a matching decoder definition (by address number and programming mode)
     * and add it. Skip if already in the list.
     *
     * @param m The received LocoNet message. Note that this same object may
     *            be presented to multiple users. It should not be modified
     *            here.
     */
    @Override
    public void message(LocoNetMessage m) {
        if (Lnsv1MessageContents.isSupportedSv1Message(m)) {
            if ((Lnsv1MessageContents.extractMessageType(m) == Lnsv1MessageContents.Sv1Command.SV1_READ) &&
                    (Lnsv1MessageContents.extractMessageVersion(m) > 0)) { // which marks replies from devices
                // it's an LNSV1 Read_One Reply message, decode contents:
                Lnsv1MessageContents contents = new Lnsv1MessageContents(m);
                int vrs = contents.getVersionNum();
                int addrL = contents.getSrcL();
                int subAddr = contents.getSubAddress();
                int sv = contents.getSvNum();
                int val = contents.getSvValue();
                log.debug("Lnsv1DevicesManager got read reply: vrs:{}, address:{}/{} cv:{} val:{}", vrs, addrL, subAddr, sv, val);

                synchronized (this) {
                    if (lnsv1Devices.addDevice(new Lnsv1Device(addrL, subAddr, sv, val, "", "", vrs))) {
                        log.debug("new Lnsv1Device added to table");
                        // Annotate the discovered device LNSV1 data based on address
                        for (int i = 0; i < lnsv1Devices.size(); ++i) { // find the added item
                            Lnsv1Device dev = lnsv1Devices.getDevice(i);
                            if ((dev.getDestAddrLow() == addrL) && (dev.getDestAddrHigh() == subAddr)) {
                                // Try to find a roster entry which matches the device characteristics
                                log.debug("Looking for adr {} in Roster", dev.getDestAddr());

                                // threadUtil off GUI for Roster reading decoderfiles cf. LncvDevicesManager
                                ThreadingUtil.newThread(() -> {
                                    List<RosterEntry> rl;
                                    try {
                                        // requires nonnull default for jmri.jmrit.roster.RosterConfigManager
                                        rl = Roster.getDefault().getEntriesMatchingCriteria(
                                                Integer.toString(dev.getDestAddr()), // composite DCC address
                                                null, null, null,
                                                null); // TODO filter on progMode LNSV1 only on new roster entries
                                        log.debug("Lnsv1DeviceManager found {} matches in Roster", rl.size());
                                        if (rl.isEmpty()) {
                                            log.debug("No corresponding roster entry found");
                                        } else if (rl.size() == 1) {
                                            log.debug("Matching roster entry found");
                                            dev.setRosterEntry(rl.get(0)); // link this device to the entry
                                            String title = rl.get(0).getDecoderModel() + " (" + rl.get(0).getDecoderFamily() + ")";
                                            // fileFromTitle() matches by model + " (" + family + ")"
                                            DecoderFile decoderFile = InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle(title);
                                            if (decoderFile != null) {
                                                // TODO check for LNSV1 mode
                                                dev.setDecoderFile(decoderFile); // link to decoderFile (to check programming mode from table)
                                                log.debug("Attached a decoderfile");
                                            } else {
                                                log.warn("Could not attach decoderfile {} to entry", rl.get(0).getFileName());
                                            }
                                        } else { // matches > 1
                                            JmriJOptionPane.showMessageDialog(null,
                                                    Bundle.getMessage("WarnMultipleLnsv1ModsFound", rl.size(), addrL, subAddr),
                                                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
                                            log.info("Found multiple matching LNSV1 roster entries. " + "Cannot associate any one to this device.");
                                        }

                                    } catch (Exception e) {
                                        log.error("Error creating Roster.matchingList: {}", e.getMessage());
                                    }
                                }, ROSTER_THREAD_NAME + memo.getSystemPrefix()).start();
                                // this will block until the thread completes, either by finishing or by being cancelled

                                // notify listeners of pertinent change to device list
                                firePropertyChange("DeviceListChanged", true, false);
                            }
                        }
                    } else {
                        log.debug("LNSV1 device was already in list");
                    }
                }
            } else {
                log.debug("LNSV1 message not a READ REPLY [{}]", m);
            }
        } else {
            log.debug("LNSV1 message not recognized");
        }
    }

    public synchronized Lnsv1Device getDevice(int vrs, int addr) {
        for (int i = 0; i < lnsv1Devices.size(); ++ i) {
            Lnsv1Device dev = lnsv1Devices.getDevice(i);
            if ((dev.getSwVersion() == vrs) && (dev.getDestAddr() == addr)) {
                return dev;
            }
        }
        return null;
    }

    public ProgrammingResult prepareForSymbolicProgrammer(Lnsv1Device dev, ProgrammingTool t) {
        synchronized(this) {
            if (lnsv1Devices.isDeviceExistant(dev) < 0) {
                return ProgrammingResult.FAIL_NO_SUCH_DEVICE;
            }
            int destAddr = dev.getDestAddr();
            if (destAddr == 0) {
                return ProgrammingResult.FAIL_DESTINATION_ADDRESS_IS_ZERO;
            }
            int deviceCount = 0;
            for (Lnsv1Device d : lnsv1Devices.getDevices()) {
                if (destAddr == d.getDestAddr()) {
                    deviceCount++;
                }
            }
            log.debug("prepareForSymbolicProgrammer found {} matches", deviceCount);
            if (deviceCount > 1) {
                return ProgrammingResult.FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS;
            }
        }

        if ((dev.getRosterName() == null) || (dev.getRosterName().isEmpty())) {
            return ProgrammingResult.FAIL_NO_MATCHING_ROSTER_ENTRY;
        }

        // check if roster entry still present in Roster
        RosterEntry re = Roster.getDefault().entryFromTitle(dev.getRosterName());
        if (re == null) {
            log.warn("Could not open LNSV1 Programmer because {} not found in Roster. Removed from device",
                    dev.getRosterName());
            dev.setRosterEntry(null);
            jmri.util.ThreadingUtil.runOnLayoutEventually( ()-> firePropertyChange("DeviceListChanged", true, false));
            return ProgrammingResult.FAIL_NO_MATCHING_ROSTER_ENTRY;
        }
        String name = re.getId();

        DefaultProgrammerManager pm = memo.getProgrammerManager();
        if (pm == null) {
            return ProgrammingResult.FAIL_NO_APPROPRIATE_PROGRAMMER;
        }
        Programmer p = pm.getAddressedProgrammer(false, dev.getDestAddr());
        if (p == null) {
            return ProgrammingResult.FAIL_NO_ADDRESSED_PROGRAMMER;
        }

        //if (p.getClass() != ProgDebugger.class) { // Debug in Simulator
            // ProgDebugger is used for LocoNet HexFile Sim; uncommenting above line allows testing of LNSV1 Tool
            if (!p.getSupportedModes().contains(LnProgrammerManager.LOCONETOPSBOARD)) {
                return ProgrammingResult.FAIL_NO_LNSV1_PROGRAMMER;
            }
            p.setMode(LnProgrammerManager.LOCONETSV1MODE);
            ProgrammingMode prgMode = p.getMode();
            if (!prgMode.equals(LnProgrammerManager.LOCONETSV1MODE)) {
                return ProgrammingResult.FAIL_NO_LNSV1_PROGRAMMER;
            }
        //}

        t.openPaneOpsProgFrame(re, name, "programmers/Comprehensive.xml", p); // NOI18N
        return ProgrammingResult.SUCCESS_PROGRAMMER_OPENED;
    }

    @Override
    public void dispose(){
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
    }

    public enum ProgrammingResult {
        SUCCESS_PROGRAMMER_OPENED,
        FAIL_NO_SUCH_DEVICE,
        FAIL_NO_APPROPRIATE_PROGRAMMER,
        FAIL_NO_MATCHING_ROSTER_ENTRY,
        FAIL_DESTINATION_ADDRESS_IS_ZERO,
        FAIL_MULTIPLE_DEVICES_SAME_DESTINATION_ADDRESS,
        FAIL_NO_ADDRESSED_PROGRAMMER,
        FAIL_NO_LNSV1_PROGRAMMER
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Lnsv1DevicesManager.class);

}
