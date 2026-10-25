package jmri.jmrix.marklin;

import java.util.Objects;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device service for WeichenChef Magnet (CdB class 0x22) modules.
 * <p>
 * Implements {@link McanDeviceService} and is attached to an {@link McanDevice} by the
 * classifier layer ({@link McanDeviceManager}) after the initial PING_REPLY is received.
 * <p>
 * <h3>Classification flow</h3>
 * WeichenChef Magnet is the only {@link jmri.jmrix.marklin.mcan.device.CdBDeviceClass.HardwareCapability#AMBIGUOUS}
 * device class. Two hardware generations share class ID 0x22 and produce identical ping
 * replies; they can only be distinguished after discovery via a firmware-version probe:
 * <ol>
 *   <li>PING_REPLY arrives → {@link McanDeviceManager} creates {@code McanDevice},
 *       attaches a new {@code WeichenChefMagnetService} via
 *       {@link McanDevice#setDeviceService(McanDeviceService)}, sets state
 *       {@link McanDeviceState#INITIALIZING} and calls {@link #sendGenerationProbe()}.
 *   <li>READ_REPLY for slot=0, reg=1 arrives → {@link McanDeviceManager} routes it to
 *       {@link #handleCdBReply(CdBFrameParser.CdBFrame, MarklinReply)} on this service.
 *   <li>Service classifies the generation from the firmware version and sets
 *       {@link #getGeneration()}.  It then calls
 *       {@link McanDevice#setDeviceState(McanDeviceState) setDeviceState(ONLINE)} so the
 *       device bean becomes visible to the rest of JMRI.
 * </ol>
 * <p>
 * <h3>Generation discrimination</h3>
 * Gen-001 and gen-002+ are distinguished by firmware major version.
 * TODO: The exact firmware version boundary must be confirmed against real hardware.
 * Current heuristic: {@code firmwareHi >= GEN_002_PLUS_MIN_FW_HI} → {@link WeichenChefMagnetGeneration#GEN_002_PLUS}.
 * Observed firmware versions from decomp:
 * <ul>
 *   <li>gen-001 tools show versions like 2.x (004, 005 series)</li>
 *   <li>gen-002+ tools (from 012) show "Ab Firmware 2.21" as minimum  -  same major, different
 *       interpretation: the 012 tool handles both gen variants by the user's explicit selection,
 *       not by auto-detection</li>
 * </ul>
 * Because neither reference tool auto-detects the generation at runtime, the safest
 * discriminant from {@link jmri.jmrix.marklin.mcan.cdb.CdBWeichenChefCommands#buildCoilFireTest}
 * documentation is the coil-fire header bytes; those are write-path only and cannot be
 * read back. This service therefore uses firmware major version as a proxy and marks the
 * boundary as a TODO.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class WeichenChefMagnetService implements McanDeviceService {

    /** CdB class ID for WeichenChef Magnet. */
    public static final int CDB_CLASS_ID = 0x22;

    /** Delay in milliseconds between select → read → deselect during a generation probe. */
    static final long PROBE_STEP_DELAY_MS = 300L;

    /**
     * Functional interface for scheduling a deferred action.
     * The default production wiring uses {@link ThreadingUtil#runOnGUIDelayed}.
     */
    @FunctionalInterface
    public interface DelayScheduler {
        /**
         * Schedule {@code action} to run after {@code delayMs} milliseconds.
         *
         * @param action  the action to run
         * @param delayMs delay in milliseconds
         */
        void schedule(Runnable action, long delayMs);
    }

    /**
     * Firmware major-version threshold for gen-002+ classification.
     * Devices whose reg=1 READ_REPLY returns {@code firmwareHi >= GEN_002_PLUS_MIN_FW_HI}
     * are classified as {@link WeichenChefMagnetGeneration#GEN_002_PLUS}.
     * <p>
     * TODO: Confirm this boundary against real gen-001 and gen-002+ hardware.
     * The value 3 is a conservative placeholder  -  gen-001 units observed at fw 2.x,
     * gen-002+ units also at fw 2.x per decomp, so a version-only boundary may be
     * insufficient. A secondary register probe (e.g. read reg=8 to see if it echoes back)
     * may be required for definitive discrimination.
     */
    static final int GEN_002_PLUS_MIN_FW_HI = 3;

    private final McanDevice device;
    private final McanFrameSender sender;
    private final DelayScheduler scheduler;
    private WeichenChefMagnetGeneration generation = WeichenChefMagnetGeneration.UNKNOWN;

    /**
     * Create a service for the given device, using the default GUI-thread scheduler.
     *
     * @param device the McanDevice this service belongs to (must be class 0x22)
     * @param sender transport to use for sending probe frames
     */
    public WeichenChefMagnetService(McanDevice device, McanFrameSender sender) {
        this(device, sender, (action, delayMs) -> ThreadingUtil.runOnGUIDelayed(action::run, (int) delayMs));
    }

    /**
     * Create a service with an explicit delay scheduler (for testing).
     *
     * @param device    the McanDevice this service belongs to (must be class 0x22)
     * @param sender    transport to use for sending probe frames
     * @param scheduler delay scheduler; replaces {@link ThreadingUtil#runOnGUIDelayed}
     */
    public WeichenChefMagnetService(McanDevice device, McanFrameSender sender, DelayScheduler scheduler) {
        this.device    = Objects.requireNonNull(device,    "device cannot be null");
        this.sender    = Objects.requireNonNull(sender,    "sender cannot be null");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler cannot be null");
    }

    /**
     * Return the hardware generation or {@link WeichenChefMagnetGeneration#UNKNOWN} if the
     * firmware probe has not yet returned a classifiable reply.
     *
     * @return generation
     */
    public synchronized WeichenChefMagnetGeneration getGeneration() {
        return generation;
    }

    /**
     * Send the generation probe sequence: select the module, wait 300 ms, send
     * the firmware-version read (slot=0, reg=1), wait another 300 ms, then deselect.
     * <p>
     * The reply is received passively via
     * {@link #handleCdBReply(CdBFrameParser.CdBFrame, MarklinReply)}  -  no explicit
     * reply tracking is needed because the TC dispatches all CdB config frames to
     * {@link CdBConfigListener}s regardless of who sent the request.
     * <p>
     * Called by the classifier layer immediately after attaching this service and
     * also on demand when the user clicks "Probe" in the device table UI.
     */
    public void sendGenerationProbe() {
        int moduleId = getModuleId();
        if (moduleId < 0) {
            log.warn("WeichenChefMagnetService.sendGenerationProbe: non-CdB identity on device {}",
                    device.getSystemName());
            return;
        }
        int[] selectFrame   = CdBConfigCommands.buildSelect(CDB_CLASS_ID, moduleId);
        int[] readFrame     = CdBConfigCommands.buildRead(CDB_CLASS_ID, moduleId, 0, 1);
        int[] deselectFrame = CdBConfigCommands.buildDeselect(CDB_CLASS_ID, moduleId);

        scheduler.schedule(() -> {
            log.debug("Sending select for generation probe on {} (module {}): {}",
                    device.getSystemName(), moduleId, formatFrame(selectFrame));
            sender.sendFrame(selectFrame);

            scheduler.schedule(() -> {
                log.debug("Sending read reg=1 for generation probe on {} (module {}): {}",
                        device.getSystemName(), moduleId, formatFrame(readFrame));
                sender.sendFrame(readFrame);

                scheduler.schedule(() -> {
                    log.debug("Sending deselect after generation probe on {} (module {}): {}",
                            device.getSystemName(), moduleId, formatFrame(deselectFrame));
                    sender.sendFrame(deselectFrame);
                }, PROBE_STEP_DELAY_MS);
            }, PROBE_STEP_DELAY_MS);
        }, PROBE_STEP_DELAY_MS);
    }

    /**
     * Handle an incoming CdB reply frame directed at this service.
     * <p>
     * Only READ_REPLY frames for class 0x22, the matching module ID, slot=0 and reg=1
     * are consumed. All other frames are ignored.
     * <p>
     * When a matching probe reply arrives the service classifies the generation from the
     * firmware major version, updates {@link #getGeneration()} and advances the device
     * state to {@link McanDeviceState#ONLINE}.
     *
     * @param frame parsed CdB frame
     * @param raw   raw 13-byte reply
     */
    public void handleCdBReply(CdBFrameParser.CdBFrame frame, MarklinReply raw) {
        if (frame.classId() != CDB_CLASS_ID) {
            return;
        }
        if (frame.frameType() != CdBFrameType.READ_REPLY) {
            return;
        }
        int moduleId = getModuleId();
        if (moduleId < 0 || frame.moduleId() != moduleId) {
            return;
        }

        CdBFrameParser.ReadReplyFrame rrf = (CdBFrameParser.ReadReplyFrame) frame;
        if (rrf.slot() != 0 || rrf.reg() != 1) {
            return;
        }

        int firmwareHi = (raw.getElement(7) & 0xFF);
        int firmwareLo = (raw.getElement(8) & 0xFF);

        WeichenChefMagnetGeneration classified = classify(firmwareHi);
        log.debug("WeichenChef Magnet probe reply on {}: fw={}.{} → {}",
                device.getSystemName(), firmwareHi, firmwareLo, classified);

        synchronized (this) {
            generation = classified;
        }
        device.setSoftwareVersion((firmwareHi << 8) | firmwareLo);
        device.setDeviceState(McanDeviceState.ONLINE);
    }

    /**
     * Classify generation from firmware major version.
     *
     * @param firmwareHi firmware major version byte from reg=1 reply
     * @return classified generation
     */
    static WeichenChefMagnetGeneration classify(int firmwareHi) {
        return (firmwareHi >= GEN_002_PLUS_MIN_FW_HI)
                ? WeichenChefMagnetGeneration.GEN_002_PLUS
                : WeichenChefMagnetGeneration.GEN_001;
    }

    private int getModuleId() {
        McanDeviceIdentity identity = device.getIdentity();
        if (!identity.isCdB()) {
            return -1;
        }
        return ((McanDeviceIdentity.CdB) identity).getModuleId();
    }

    private static String formatFrame(int[] frame) {
        StringBuilder sb = new StringBuilder();
        for (int b : frame) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    private static final Logger log = LoggerFactory.getLogger(WeichenChefMagnetService.class);
}
