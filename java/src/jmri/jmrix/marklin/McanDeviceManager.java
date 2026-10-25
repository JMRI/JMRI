package jmri.jmrix.marklin;

import java.util.Objects;
import jmri.Manager;
import jmri.managers.AbstractManager;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.cdb.CdBLampenChefCommands;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;
import jmri.jmrix.marklin.mcan.protocol.NativeDeviceInfo;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for discovering, tracking and maintaining MCAN devices on a Märklin connection.
 * <p>
 * Registered with {@link MarklinSystemConnectionMemo}. Listens to both native MCAN
 * messages ({@link MarklinListener}) and CdB config frames ({@link CdBConfigListener}).
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanDeviceManager extends AbstractManager<McanDevice> implements MarklinListener, CdBConfigListener {

    private final MarklinSystemConnectionMemo memo;

    public McanDeviceManager(MarklinSystemConnectionMemo memo) {
        super(Objects.requireNonNull(memo, "memo cannot be null"));
        this.memo = memo;
        if (memo.getTrafficController() != null) {
            memo.getTrafficController().addMarklinListener(this);
            memo.getTrafficController().getCdBFilter().addCdBConfigListener(this);
        }
    }

    /**
     * Provide or create an {@link McanDevice} for the given identity.
     *
     * @param identity device identity
     * @return McanDevice bean
     */
    public McanDevice provideDevice(McanDeviceIdentity identity) {
        Objects.requireNonNull(identity, "identity cannot be null");
        String sysName = identity.getSystemName(getSystemPrefix());
        McanDevice device = getBySystemName(sysName);
        if (device == null) {
            device = new McanDevice(getSystemPrefix(), identity);
            register(device);
        }
        return device;
    }

    /**
     * Get device by identity.
     * @param identity device identity
     * @return McanDevice or null if not found
     */
    public McanDevice getDevice(McanDeviceIdentity identity) {
        if (identity == null) {
            return null;
        }
        return getBySystemName(identity.getSystemName(getSystemPrefix()));
    }

    /**
     * Trigger discovery of native Märklin MCAN devices (CS2, MS2, Gleisbox, etc.).
     * Sends a standard MCAN PING (command 0x18, DLC=0).
     * Replies will be automatically captured and processed by this manager.
     */
    public void discoverMarklinDevices() {
        if (memo.getTrafficController() == null) {
            return;
        }
        int[] encodedPing = jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands.buildPing();
        MarklinMessage msg = new MarklinMessage(encodedPing);
        msg.setReplyExpected(false);
        memo.getTrafficController().sendMarklinMessage(msg, null);
    }

    /**
     * Trigger discovery of a specific CdB device class.
     * <p>
     * NEW-protocol classes: sends a single zero-DLC class-specific ping.
     * OLD-protocol classes (LampenChef, WeichenChef Motor): iterates all 128
     * address slots synchronously, sending one addressed ping per slot.
     * Replies will be automatically captured and processed by this manager.
     *
     * @param deviceClass target CdB device class to discover
     */
    public void discoverCdBDevice(CdBDeviceClass deviceClass) {
        MarklinTrafficController trafficController = memo.getTrafficController();
        if (trafficController == null || deviceClass == null) {
            return;
        }
        if (deviceClass.getProtocolGeneration() == CdBDeviceClass.ProtocolGeneration.OLD) {
            // TODO: OLD-protocol ping replies use a different frame format ({0x01,0x40,class,addr+0x80,...})
            // which is NOT matched by CdBClassFilter.isCdBConfigFrame() (requires 0x0A80 hash pattern).
            // Until an OLD-protocol reply parser is implemented, discovery pings are sent but replies
            // will never land in the device table.
            for (int addr = 0; addr < 128; addr++) {
                final int a = addr;
                ThreadingUtil.runOnGUIDelayed(() -> {
                    if (memo.getTrafficController() != trafficController) {
                        return;
                    }
                    int[] rawFrame = CdBLampenChefCommands.buildOldProtocolPing(deviceClass.getClassId(), a);
                    MarklinMessage msg = new MarklinMessage(rawFrame);
                    msg.setReplyExpected(false);
                    trafficController.sendMarklinMessage(msg, null);
                }, a * 5);
            }
        } else {
            int[] rawFrame = CdBConfigCommands.buildPing(deviceClass.getClassId());
            log.debug("CdB discovery ping for {} (class 0x{}) frame: {}", deviceClass.getDisplayName(),
                    Integer.toHexString(deviceClass.getClassId()), formatFrame(rawFrame));
            MarklinMessage msg = new MarklinMessage(rawFrame);
            msg.setReplyExpected(false);
            trafficController.sendMarklinMessage(msg, null);
        }
    }

    private static String formatFrame(int[] frame) {
        StringBuilder sb = new StringBuilder();
        for (int b : frame) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    /**
     * Trigger discovery of all known CdB device classes.
     * Pings are staggered 250 ms apart to give firmware time to respond
     * before the next class ping arrives on the bus.
     */
    public void discoverAllCdBDevices() {
        discoverCdBDevices(java.util.Arrays.asList(CdBDeviceClass.values()));
    }

    /**
     * Trigger discovery for a specific subset of CdB device classes.
     * Pings are staggered 250 ms apart to give firmware time to respond.
     *
     * @param classes ordered list of classes to ping
     */
    public void discoverCdBDevices(java.util.List<CdBDeviceClass> classes) {
        CdBDeviceClass[] arr = classes.toArray(new CdBDeviceClass[0]);
        for (int i = 0; i < arr.length; i++) {
            final CdBDeviceClass dc = arr[i];
            ThreadingUtil.runOnGUIDelayed(() -> discoverCdBDevice(dc), i * 250);
        }
    }

    @Override
    public Class<McanDevice> getNamedBeanClass() {
        return McanDevice.class;
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameMcanDevices" : "BeanNameMcanDevice");
    }

    @Override
    public int getXMLOrder() {
        return Manager.SENSORS;
    }

    @Override
    public char typeLetter() {
        return 'D';
    }

    @Override
    public String getSystemNamePrefix() {
        return getSystemPrefix() + ":dev:";
    }

    @Override
    public void message(MarklinMessage m) {
        processRawMessage(extractBytes(m));
    }

    @Override
    public void reply(MarklinReply r) {
        processRawMessage(extractBytes(r));
    }

    private static int[] extractBytes(MarklinMessage m) {
        int[] raw = new int[13];
        for (int i = 0; i < 13; i++) raw[i] = m.getElement(i);
        return raw;
    }

    private static int[] extractBytes(MarklinReply r) {
        int[] raw = new int[13];
        for (int i = 0; i < 13; i++) raw[i] = r.getElement(i);
        return raw;
    }

    private void processRawMessage(int[] raw) {
        ClassifiedFrame classified = ProtocolClassifier.classify(raw);
        Cs2ConfigCommands.parsePingReply(classified).ifPresent(info -> {
            McanDeviceIdentity.Native identity = McanDeviceIdentity.forNative(info.uid(), info.deviceType());
            McanDevice device = provideDevice(identity);
            device.touch();
            device.setSoftwareVersion(info.softwareVersion());
            device.setDeviceState(McanDeviceState.ONLINE);
        });
    }

    @Override
    public void cdBConfigReply(CdBFrameParser.CdBFrame frame, MarklinReply rawReply) {
        if (frame == null || frame.frameType() == CdBFrameType.UNKNOWN) {
            return;
        }

        int canClass = frame.classId();
        int module = frame.moduleId();

        log.debug("CdB config reply received: frameType={} canClass=0x{} module={}",
                frame.frameType(), Integer.toHexString(canClass), module);

        if (module < 0) {
            return;
        }

        // Route READ_REPLY frames for AMBIGUOUS classes to their attached service.
        // These frames carry probe responses needed to finish classification after
        // discovery; they must NOT create new device entries.
        if (frame.frameType() == CdBFrameType.READ_REPLY
                && canClass == WeichenChefMagnetService.CDB_CLASS_ID) {
            McanDevice existing = getDevice(McanDeviceIdentity.forCdB(canClass, module));
            if (existing != null && existing.getDeviceService() instanceof WeichenChefMagnetService) {
                ((WeichenChefMagnetService) existing.getDeviceService()).handleCdBReply(frame, rawReply);
            }
            return;
        }

        // Only PING_REPLY frames prove a device exists at this class+module combination.
        // READ_REPLY and WRITE frames are config protocol traffic for an already-selected
        // module and must not create phantom entries in the device table.
        if (frame.frameType() != CdBFrameType.PING_REPLY) {
            return;
        }

        // A genuine discovery ping reply always carries payload (DLC > 0).
        // Post-deselect status broadcasts use DLC=0 and are classified as PING_REPLY
        // due to their CAN ID structure, but they are not device advertisements.
        if ((rawReply.getElement(4) & 0xFF) == 0) {
            log.debug("Ignoring DLC=0 PING_REPLY for canClass=0x{} module={} (post-deselect broadcast)",
                    Integer.toHexString(canClass), module);
            return;
        }

        McanDeviceIdentity.CdB identity = McanDeviceIdentity.forCdB(canClass, module);
        McanDevice device = provideDevice(identity);
        device.touch();

        // Factory-fresh module: stash challenge bytes so the configure dialog can use them
        CdBFrameParser.PingReplyFrame pingReply = (CdBFrameParser.PingReplyFrame) frame;
        if (pingReply.isFactoryFresh()) {
            pingReply.challengeBytes().ifPresent(cb ->
                device.setFactoryChallengeBytes(cb[0], cb[1]));
        }

        // WeichenChef Magnet (0x22) is AMBIGUOUS: two hardware generations share the same
        // class ID and produce identical ping replies.  Attach a WeichenChefMagnetService
        // and stay INITIALIZING until the firmware probe reply arrives; the service then
        // calls setDeviceState(ONLINE) itself.  All other classes go ONLINE immediately.
        if (canClass == WeichenChefMagnetService.CDB_CLASS_ID && pingReply.isOperational()) {
            if (device.getDeviceService() == null) {
                WeichenChefMagnetService svc = new WeichenChefMagnetService(device, buildSender());
                device.setDeviceService(svc);
                device.setDeviceState(McanDeviceState.INITIALIZING);
                svc.sendGenerationProbe();
            }
            // If service already attached (re-discovery ping): just touch, leave state as-is
        } else {
            device.setDeviceState(McanDeviceState.ONLINE);
        }

        // Hardware fingerprint tracking  -  strictly class 0x27 (WeichenChef Kombi) only.
        // Bytes 9–12 of the ping reply are stable per physical device across ID changes.
        // Factory-fresh modules use module==0 and DLC==6; commissioned modules use DLC==3.
        // Only extract fingerprint from DLC==3 commissioned-module replies.
        // Migration scan only runs when the device is brand-new this session (fingerprint == -1)
        // to avoid spurious cross-contamination on subsequent discoveries.
        if (canClass == 0x27 && pingReply.isOperational()) {
            int b9  = rawReply.getElement(9)  & 0xFF;
            int b10 = rawReply.getElement(10) & 0xFF;
            int b11 = rawReply.getElement(11) & 0xFF;
            int b12 = rawReply.getElement(12) & 0xFF;
            {
                long fp = ((long) b9 << 24) | ((long) b10 << 16) | ((long) b11 << 8) | b12;
                long existingFp = device.getHwFingerprint();
                if (existingFp == -1L) {
                    device.setHwFingerprint(fp);
                    // fp==0 means the device did not populate the fingerprint bytes;
                    // treat as "no fingerprint" and skip migration to avoid all-zero
                    // matches collapsing multiple distinct modules into one entry.
                    // Only scan for a stale entry when this device is brand-new this session.
                    // Scanning on every ping reply causes spurious migrations when two devices
                    // share the same fingerprint bytes.
                    if (fp != 0L) {
                        for (McanDevice existing : getNamedBeanSet()) {
                            if (existing == device) continue;
                            McanDeviceIdentity existingId = existing.getIdentity();
                            if (!existingId.isCdB()) continue;
                            if (((McanDeviceIdentity.CdB) existingId).getCanClass() != 0x27) continue;
                            if (existing.getHwFingerprint() != fp) continue;
                            // Same physical device, different module ID  -  carry user name across
                            String oldUserName = existing.getUserName();
                            log.info("WeichenChef Kombi fingerprint match: migrating user name '{}' from {} to {}",
                                    oldUserName, existing.getSystemName(), device.getSystemName());
                            deregister(existing);
                            if (oldUserName != null && !oldUserName.isEmpty()) {
                                device.setUserName(oldUserName);
                            }
                            device.setHwFingerprint(fp);
                            break;
                        }
                    }
                } else if (existingFp != fp) {
                    log.warn("WeichenChef Kombi fingerprint mismatch on {}: had 0x{} got 0x{}",
                            device.getSystemName(),
                            Long.toHexString(existingFp), Long.toHexString(fp));
                }
            }
        }
    }

    private McanFrameSender buildSender() {
        return frame -> {
            MarklinMessage msg = new MarklinMessage(frame);
            msg.setReplyExpected(false);
            memo.getTrafficController().sendMarklinMessage(msg, null);
        };
    }

    @Override
    public void dispose() {
        if (memo.getTrafficController() != null) {
            memo.getTrafficController().removeMarklinListener(this);
            memo.getTrafficController().getCdBFilter().removeCdBConfigListener(this);
        }
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(McanDeviceManager.class);
}
