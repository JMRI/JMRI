package jmri.jmrix.marklin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import jmri.jmrix.marklin.mcan.address.AddressEncoding;
import jmri.jmrix.marklin.mcan.address.CdBAddressCodec;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.cdb.CdBWeichenChefCommands;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol service for WeichenChef Kombi (CdB class 0x27) modules.
 * <p>
 * Owns all CAN bus interaction for module discovery, property read/write,
 * address encoding and configuration persistence. All listener callbacks
 * are dispatched on the Swing EDT via {@link ThreadingUtil#runOnGUI}.
 * <p>
 * TODO: implement {@code McanDeviceService} once that interface exists, so the
 * classifier layer can attach this service to an {@link McanDevice} automatically
 * after discovery instead of requiring manual wiring by UI panels.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class WeichenChefKombiParameterService {

    /**
     * Voltage calculation result.
     */
    public static class VoltageResult {
        public final float volts;
        public final boolean lowVoltageWarning;
        public final String text;

        public VoltageResult(float volts, boolean lowVoltageWarning, String text) {
            this.volts = volts;
            this.lowVoltageWarning = lowVoltageWarning;
            this.text = text;
        }
    }

    /**
     * Callback interface for WeichenChef Kombi protocol events.
     */
    public interface WeichenChefKombiListener {
        default void firmwareReceived(int hi, int lo) {}
        default void hardwareReceived(int hi, int lo, Integer chipType) {}
        default void voltageReceived(VoltageResult result) {}
        default void moduleTypeReceived(int moduleType) {}
        default void addressReceived(int channel, String format, int address) {}
        default void rmkReceived(int channel, int rmkAddr, int connectionNumber) {}
        default void switchingTimeReceived(int channel, int timeMs) {}
        default void cdBFlagReceived(int channel, boolean enabled) {}
        default void positionReceived(int channel, int statusByte) {}
        default void factoryIdModeEntered() {}
        default void moduleFound(int rawId) {}
        /** Called once per selection cycle when moduleType + all 5 addresses have arrived. */
        default void configReadyReceived() {}
        /** Called when config reads time out after all retries  -  some fields may be missing. */
        default void configLoadFailedReceived() {}
    }

    // Bitmask tracking which critical replies have arrived this selection cycle.
    // Bit 0 = moduleType (reg=245), bits 1–5 = address channels 1–5 (reg=4).
    // READY_MASK is set when all 6 bits are present.
    private static final int READY_MASK        = 0x3F; // 0b111111
    private static final int RETRY_DELAY_MS    = 2000;
    private static final int MAX_RETRIES       = 3;
    private static final int FRAME_INTERVAL_MS = 30;
    private int receivedMask    = 0;
    private boolean configReadyFired = false;
    private int retryCount      = 0;

    private final McanDevice device;
    private final McanFrameSender sender;
    private final List<WeichenChefKombiListener> listeners = new CopyOnWriteArrayList<>();

    private int currentRawId = 0; // 0 = none selected
    private boolean factorySelectSent = false;
    private int factoryChallenge0 = 0;
    private int factoryChallenge1 = 0;
    private int moduleType = 0;
    private String chipType = null;
    // Generation counter incremented on each selectModule call.
    // READ_REPLY frames are only dispatched when their generation matches,
    // so late replies from a previous burst cannot corrupt the current dialog state.
    private int selectGeneration = 0;

    // Stored module addresses (5 outputs) for position matching
    private final int[] moduleAddresses = new int[5];

    public WeichenChefKombiParameterService(McanDevice device, McanFrameSender sender) {
        this.device = Objects.requireNonNull(device, "device cannot be null");
        this.sender = Objects.requireNonNull(sender, "sender cannot be null");
    }

    public void addListener(WeichenChefKombiListener listener) {
        listeners.add(listener);
    }

    public void removeListener(WeichenChefKombiListener listener) {
        listeners.remove(listener);
    }

    /**
     * Select a module: deselect current (if any), select new, send property query.
     * For factory-fresh modules (rawId == 0), also sends the normal select frame first
     * (matching the reference tool), then waits for the DLC=6 ping reply to complete
     * the challenge/response handshake reactively.
     *
     * @param rawId module raw ID (0 for factory-fresh, 1..127 for normal modules)
     */
    public void selectModule(int rawId) {
        int canClass = getCanClass();
        if (currentRawId > 0) {
            sender.sendFrame(CdBConfigCommands.buildDeselect(canClass, currentRawId));
        } else if (rawId > 0) {
            // Preemptive deselect: even if we have no record of a previous selection,
            // the hardware may still be selected from a prior session. Send a deselect
            // for the target module so the select+query sequence starts from a clean state.
            sender.sendFrame(CdBConfigCommands.buildDeselect(canClass, rawId));
        }
        currentRawId = rawId;
        moduleType = 0;
        chipType = null;
        factorySelectSent = false;
        receivedMask = 0;
        configReadyFired = false;
        retryCount = 0;
        selectGeneration++;
        if (rawId > 0) {
            final int generation = selectGeneration;
            sender.sendFrame(CdBConfigCommands.buildSelect(canClass, rawId));
            // Delay matches reference tool timing: gives the hardware time to settle
            // after select before we flood it with 25 read requests.
            ThreadingUtil.runOnGUIDelayed(() -> {
                if (generation == selectGeneration) {
                    startPropertyQuery();
                }
            }, 20);
        }
        // rawId == 0: do NOT send a normal select  -  the factory handshake (DLC=6)
        // must be the first and only select frame, sent reactively on the next ping reply.
    }

    /**
     * Deselect the currently selected module (if any).
     * <p>
     * Cleanup: Cancels any pending delayed property-query and retry tasks for the
     * current selection cycle via {@link #cancelPendingTasks()}
     *
     */
    public void deselectModule() {
        if (currentRawId > 0) {
            sender.sendFrame(CdBConfigCommands.buildDeselect(getCanClass(), currentRawId));
            currentRawId = 0;
        }
        factorySelectSent = false;
        cancelPendingTasks();
    }

    /**
     * Invalidate all pending delayed tasks (staggered property reads and the
     * ready-mask retry chain) scheduled for the current selection cycle.
     * <p>
     * Delayed tasks submitted via {@link ThreadingUtil#runOnGUIDelayed} cannot be
     * cancelled directly. Each task guards on {@link #selectGeneration}. When bumping
     * the generation, every outstanding task early-returns.
     *
     * Callers of this service (UI panels, tests) should call this or {@link #dispose()} when they are done with it.
     */
    public void cancelPendingTasks() {
        ThreadingUtil.runOnGUI(() -> {
            selectGeneration++;
            configReadyFired = true;
        });
    }

    /**
     * Release this service
     *
     * Deselect any current module and cancel all pending
     * delayed tasks.
     *
     * Use on UI widget teardown to ensure proper cleanup of resources.
     */
    public void dispose() {
        deselectModule();
    }

    /**
     * Send all property read frames for the currently selected module,
     * staggered {@value #FRAME_INTERVAL_MS} ms apart.
     * The first retry check fires {@value #RETRY_DELAY_MS} ms after the last frame is sent.
     */
    public void startPropertyQuery() {
        if (currentRawId == 0) return;
        int canClass = getCanClass();
        int[][] frames = CdBWeichenChefCommands.buildReadAllProperties(canClass, currentRawId);
        final int generation = selectGeneration;
        for (int i = 0; i < frames.length; i++) {
            final int[] frame = frames[i];
            ThreadingUtil.runOnGUIDelayed(() -> {
                if (generation != selectGeneration) return;
                sender.sendFrame(frame);
            }, i * FRAME_INTERVAL_MS);
        }
        // First retry check starts 2 s after the last read request hits the wire.
        int firstRetryDelay = (frames.length - 1) * FRAME_INTERVAL_MS + RETRY_DELAY_MS;
        scheduleReadyRetry(generation, firstRetryDelay);
    }

    /**
     * Schedules one retry check after {@code delayMs}.
     * If the ready mask is still incomplete, re-sends only the missing reads and
     * reschedules another check {@value #RETRY_DELAY_MS} ms later, up to {@value #MAX_RETRIES} times.
     * After all retries are exhausted, fires {@link WeichenChefKombiListener#configLoadFailedReceived}.
     */
    private void scheduleReadyRetry(int generation, int delayMs) {
        ThreadingUtil.runOnGUIDelayed(() -> {
            if (generation != selectGeneration) return;
            if (configReadyFired) return;
            if (retryCount >= MAX_RETRIES) {
                log.warn("WeichenChef Kombi module {}: config read incomplete after {} retries, missing mask bits 0x{}",
                        currentRawId, MAX_RETRIES, Integer.toHexString(READY_MASK & ~receivedMask));
                listeners.forEach(WeichenChefKombiListener::configLoadFailedReceived);
                return;
            }
            retryCount++;
            log.debug("WeichenChef Kombi module {}: retry {}  -  re-requesting missing mask bits 0x{}",
                    currentRawId, retryCount, Integer.toHexString(READY_MASK & ~receivedMask));
            retryMissingReads();
            scheduleReadyRetry(generation, RETRY_DELAY_MS);
        }, delayMs);
    }

    /** Re-sends read requests only for the critical registers not yet received. */
    private void retryMissingReads() {
        int canClass = getCanClass();
        int missing = READY_MASK & ~receivedMask;
        if ((missing & 0x01) != 0) {
            sender.sendFrame(CdBConfigCommands.buildRead(canClass, currentRawId, 0, 245));
        }
        for (int ch = 1; ch <= 5; ch++) {
            if ((missing & (0x01 << ch)) != 0) {
                sender.sendFrame(CdBConfigCommands.buildRead(canClass, currentRawId, ch, 4));
            }
        }
    }

    /**
     * Write all configuration properties, staggered {@value #FRAME_INTERVAL_MS} ms apart.
     *
     * @param config configuration to write
     */
    public void writeConfiguration(WeichenChefKombiConfig config) {
        if (currentRawId == 0) return;
        int canClass = getCanClass();

        List<int[]> frames = new ArrayList<>();
        for (int ch = 0; ch < 4; ch++) {
            frames.add(CdBWeichenChefCommands.buildWriteChSwitchingTime(canClass, currentRawId, ch + 1, config.switchingTimes[ch]));
        }
        for (int ch = 0; ch < 4; ch++) {
            int flag = config.cdBFlags[ch] ? (moduleType == 2 ? 2 : 1) : 0;
            frames.add(CdBWeichenChefCommands.buildWriteChCdBFlag(canClass, currentRawId, ch + 1, flag));
        }
        for (int ch = 0; ch < 5; ch++) {
            frames.add(CdBWeichenChefCommands.buildWriteChAddress(canClass, currentRawId, ch + 1, config.addresses[ch]));
        }
        for (int ch = 0; ch < 8; ch++) {
            frames.add(CdBWeichenChefCommands.buildWriteChRmk(canClass, currentRawId, ch + 1, config.rmkAddresses[ch]));
        }

        for (int i = 0; i < frames.size(); i++) {
            final int[] frame = frames.get(i);
            ThreadingUtil.runOnGUIDelayed(() -> sender.sendFrame(frame), i * FRAME_INTERVAL_MS);
        }
    }

    /**
     * Send factory reset frame, then re-read all properties.
     * The module stays selected; the UI should repopulate with factory-default values.
     */
    public void resetModule() {
        if (currentRawId == 0) return;
        sender.sendFrame(CdBConfigCommands.buildFactoryReset(getCanClass(), currentRawId));
        startPropertyQuery();
    }

    /**
     * Write new module ID (factory ID assignment flow).
     * Sends write reg=2 to the currently selected module, then deselects.
     * The module is no longer reachable at the old ID after this call.
     *
     * @param newId new raw ID to assign (1..127)
     */
    public void writeNewModuleId(int newId) {
        if (currentRawId < 0) return;
        int canClass = getCanClass();
        int[] writeFrame = CdBConfigCommands.buildWriteNewModuleId(canClass, newId,
                factoryChallenge0, factoryChallenge1);
        int[] deselectFrame = CdBConfigCommands.buildDeselect(canClass, currentRawId);
        log.info("writeNewModuleId: currentRawId={} newId={} challenge=[{},{}] writeFrame={} deselectFrame={}",
                currentRawId, newId,
                String.format("%02X", factoryChallenge0), String.format("%02X", factoryChallenge1),
                formatFrame(writeFrame), formatFrame(deselectFrame));
        sender.sendFrame(writeFrame);
        sender.sendFrame(deselectFrame);
        currentRawId = 0;
        // Keep factorySelectSent=true so subsequent pings don't re-enter the handshake
    }

    /**
     * Handle an incoming CdB reply frame.
     *
     * @param frame typed CdB frame from the mcan layer
     * @param raw   raw 13-byte reply
     */
    public void handleCdBReply(CdBFrameParser.CdBFrame frame, MarklinReply raw) {
        if (frame.classId() != getCanClass()) return;

        if (frame.frameType() == CdBFrameType.PING_REPLY) {
            CdBFrameParser.PingReplyFrame pingReply = (CdBFrameParser.PingReplyFrame) frame;
            int moduleRawId = pingReply.moduleId();
            if (pingReply.isOperational()) {
                final int id = moduleRawId;
                ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.moduleFound(id)));
            } else if (pingReply.isFactoryFresh() && !factorySelectSent) {
                pingReply.challengeBytes().ifPresent(cb -> {
                    int challenge0 = cb[0];
                    int challenge1 = cb[1];
                    int[] selectFrame = CdBConfigCommands.buildFactorySelect(getCanClass(), challenge0, challenge1);
                    log.info("Factory module ping: challenge=[{}, {}], sending select frame: {}",
                            String.format("%02X", challenge0), String.format("%02X", challenge1),
                            formatFrame(selectFrame));
                    currentRawId = 0;
                    factoryChallenge0 = challenge0;
                    factoryChallenge1 = challenge1;
                    factorySelectSent = true;
                    sender.sendFrame(selectFrame);
                    ThreadingUtil.runOnGUI(() -> listeners.forEach(WeichenChefKombiListener::factoryIdModeEntered));
                });
            }
            return;
        }

        if (frame.frameType() == CdBFrameType.READ_REPLY) {
            if (frame.moduleId() != currentRawId) return;
            dispatchReadReply(raw, selectGeneration);
        }
    }

    /**
     * Handle a standard MCAN status frame (position/occupancy from accessory decoder).
     * Packet format: byte[0]=0, byte[1]=23, byte[3]=moduleAddr-128, bytes[7-8]=address, byte[9]=status.
     *
     * @param raw raw 13-byte reply
     */
    public void handleStatusFrame(MarklinReply raw) {
        if (raw.getElement(0) != 0 || raw.getElement(1) != 23) return;
        int frameAddr = raw.getElement(3) & 0xFF;
        if (frameAddr != currentRawId && frameAddr != 48) return;

        int addr = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        int status = raw.getElement(9) & 0xFF;

        for (int i = 0; i < moduleAddresses.length; i++) {
            if (moduleAddresses[i] == addr) {
                final int ch = i;
                final int st = status;
                ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.positionReceived(ch, st)));
            }
        }
    }

    // ---- private dispatch ----

    private void dispatchReadReply(MarklinReply raw, int generation) {
        int subcmd = raw.getElement(5) & 0xFF;
        int reg = raw.getElement(6) & 0xFF;

        switch (reg) {
            case 1:   handleFirmware(raw, generation);                   break;
            case 244: handleHardware(raw, generation);                   break;
            case 246: handleVoltage(raw, subcmd, generation);            break;
            case 245: handleModuleType(raw, generation);                 break;
            case 4:   if (subcmd >= 1 && subcmd <= 5) handleAddress(raw, subcmd, generation);        break;
            case 5:   if (subcmd >= 1 && subcmd <= 8) handleRmk(raw, subcmd, generation);           break;
            case 3:   if (subcmd >= 1 && subcmd <= 4) handleSwitchingTime(raw, subcmd, generation); break;
            case 6:   if (subcmd >= 1 && subcmd <= 4) handleCdBFlag(raw, subcmd, generation);       break;
            default:  break;
        }
    }

    private void handleFirmware(MarklinReply raw, int generation) {
        final int hi = raw.getElement(7) & 0xFF;
        final int lo = raw.getElement(8) & 0xFF;
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.firmwareReceived(hi, lo));
        });
    }

    private void handleHardware(MarklinReply raw, int generation) {
        final int hi = raw.getElement(7) & 0xFF;
        final int lo = raw.getElement(8) & 0xFF;
        Integer chip = null;
        if ((raw.getElement(4) & 0xFF) == 5) {
            chip = raw.getElement(9) & 0xFF;
            chipType = String.format("%02x", chip);
        }
        final Integer chipVal = chip;
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.hardwareReceived(hi, lo, chipVal));
        });
    }

    private void handleVoltage(MarklinReply raw, int subcmd, int generation) {
        if (subcmd != 0) return;
        int rawVoltage = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        VoltageResult result = calcVoltage(rawVoltage, chipType);
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.voltageReceived(result));
        });
    }

    private void handleModuleType(MarklinReply raw, int generation) {
        moduleType = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        final int mt = moduleType;
        markReceived(0x01, generation);
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.moduleTypeReceived(mt));
        });
    }

    private void handleAddress(MarklinReply raw, int subcmd, int generation) {
        int rawAddr = ((raw.getElement(8) & 0xFF) << 8) + (raw.getElement(7) & 0xFF); // little-endian ([8]=high, [7]=low)
        if (subcmd >= 1 && subcmd <= 5) {
            moduleAddresses[subcmd - 1] = rawAddr;
        }
        AddressEncoding enc = (moduleType == 3)
            ? CdBAddressCodec.INSTANCE.decodeType3(rawAddr)
            : CdBAddressCodec.INSTANCE.decodeType2(rawAddr);
        final int ch = subcmd;
        final String fmt = enc.format();
        final int addr = enc.address();
        markReceived(0x01 << subcmd, generation); // bits 1–5 for channels 1–5
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.addressReceived(ch, fmt, addr));
        });
    }

    /** Sets a bit in receivedMask and fires configReadyReceived once when READY_MASK is reached. */
    private void markReceived(int bit, int generation) {
        receivedMask |= bit;
        if (!configReadyFired && (receivedMask & READY_MASK) == READY_MASK) {
            configReadyFired = true;
            ThreadingUtil.runOnGUI(() -> {
                if (generation != selectGeneration) return;
                listeners.forEach(WeichenChefKombiListener::configReadyReceived);
            });
        }
    }

    private void handleRmk(MarklinReply raw, int subcmd, int generation) {
        int rmkAddr = ((raw.getElement(8) & 0xFF) << 8) + (raw.getElement(7) & 0xFF); // little-endian ([8]=high, [7]=low)
        int connNum = (moduleType == 3) ? (raw.getElement(9) & 0xFF) + 1 : 0;
        final int ch = subcmd;
        final int rmk = (moduleType == 3) ? rmkAddr + 1 : rmkAddr;
        final int conn = connNum;
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.rmkReceived(ch, rmk, conn));
        });
    }

    private void handleSwitchingTime(MarklinReply raw, int subcmd, int generation) {
        int time = ((raw.getElement(8) & 0xFF) << 8) + (raw.getElement(7) & 0xFF); // little-endian ([8]=high, [7]=low)
        final int ch = subcmd;
        final int t = time;
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.switchingTimeReceived(ch, t));
        });
    }

    private void handleCdBFlag(MarklinReply raw, int subcmd, int generation) {
        boolean on = (raw.getElement(8) & 0xFF) > 0;
        final int ch = subcmd;
        final boolean flag = on;
        ThreadingUtil.runOnGUI(() -> {
            if (generation != selectGeneration) return;
            listeners.forEach(l -> l.cdBFlagReceived(ch, flag));
        });
    }

    // ---- static helpers ----

    /**
     * Calculate voltage from raw ADC value.
     *
     * @param rawVoltage 16-bit ADC value
     * @param chipType chip type string (use "61" for bus-voltage chip)
     * @return VoltageResult
     */
    public static VoltageResult calcVoltage(int rawVoltage, String chipType) {
        long voltCalc;
        String label;
        if ("61".equals(chipType)) {
            voltCalc = (long) rawVoltage * 33 / 100;
            label = "Bus voltage";
        } else {
            voltCalc = (long) rawVoltage * 68 / 1000;
            label = "Operating voltage";
        }
        long voltV = voltCalc / 100;
        long voltFrac = (voltCalc % 100) / 10;
        boolean warn = (voltV < 9 || voltV > 18);
        float voltsFloat = voltCalc / 100.0f;
        String text = label + ": " + voltV + "." + voltFrac + " V";
        return new VoltageResult(voltsFloat, warn, text);
    }

    private int getCanClass() {
        McanDeviceIdentity identity = device.getIdentity();
        if (identity.isCdB()) {
            return ((McanDeviceIdentity.CdB) identity).getCanClass();
        }
        return 0x27; // fallback
    }

    private static String formatFrame(int[] frame) {
        StringBuilder sb = new StringBuilder();
        for (int b : frame) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    /**
     * Configuration data object for WeichenChef Kombi.
     */
    public static class WeichenChefKombiConfig {
        public int[] addresses = new int[5];
        public String[] formats = new String[]{"CAN", "CAN", "CAN", "CAN", "CAN"};
        public int[] switchingTimes = new int[4];
        public boolean[] cdBFlags = new boolean[4];
        public int[] rmkAddresses = new int[8];
        public int[] connectionNumbers = new int[8];
        public int moduleType = 2;
    }

    private static final Logger log = LoggerFactory.getLogger(WeichenChefKombiParameterService.class);
}
