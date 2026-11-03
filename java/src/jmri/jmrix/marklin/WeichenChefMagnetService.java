package jmri.jmrix.marklin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.cdb.CdBWeichenChefCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2AccessoryCommands;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared configuration and positive capability detection for WeichenChef Magnet.
 * All mutable protocol state is owned by the Swing EDT. Missing replies never
 * identify older hardware. Valid extended timing replies establish newer support.
 */
public class WeichenChefMagnetService implements McanDeviceService {

    public static final int CDB_CLASS_ID = 0x22;
    private static final int FRAME_INTERVAL_MS = 30;
    private static final int REPLY_TIMEOUT_MS = 2000;
    private static final int MAX_RETRIES = 2;

    /** State of the current configuration operation. */
    public enum State {
        IDLE, LOADING, READY, UNRESOLVED, LOAD_FAILED, VERIFY_FAILED, WRITING, RECONNECT
    }

    /** Save scopes match the three reference-tool buttons. */
    public enum SaveScope {
        PROPERTIES, ADDRESSES, FEEDBACK
    }

    /** Scheduling seam for deterministic protocol tests. */
    @FunctionalInterface
    public interface DelayScheduler {
        void schedule(Runnable action, long delayMs);
    }

    /** Events are delivered on the EDT. */
    public interface Listener {
        default void stateChanged() {}
        default void valueReceived(int register, int slot, int value) {}
        default void positionReceived(int turnout, int status) {}
    }

    private final McanDevice device;
    private final McanFrameSender sender;
    private final DelayScheduler scheduler;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Map<Integer, Integer> values = new LinkedHashMap<>();
    private final Map<Integer, Integer> expected = new LinkedHashMap<>();
    private final Set<Integer> pending = new LinkedHashSet<>();
    private volatile WeichenChefMagnetGeneration detected = WeichenChefMagnetGeneration.UNKNOWN;
    private volatile WeichenChefMagnetGeneration profile = WeichenChefMagnetGeneration.UNKNOWN;
    private volatile State state = State.IDLE;
    private volatile int firmware = -1;
    private int operation;
    private boolean configurationOpen;
    private volatile boolean disposed;
    private boolean probing;
    private boolean verifying;
    private int[] coilOff;

    public WeichenChefMagnetService(McanDevice device, McanFrameSender sender) {
        this(device, sender, (action, delay) ->
                ThreadingUtil.runOnGUIDelayed(action, (int) delay));
    }

    public WeichenChefMagnetService(McanDevice device, McanFrameSender sender, DelayScheduler scheduler) {
        this.device = Objects.requireNonNull(device);
        this.sender = Objects.requireNonNull(sender);
        this.scheduler = Objects.requireNonNull(scheduler);
        McanDeviceIdentity identity = device.getIdentity();
        if (!identity.isCdB() || ((McanDeviceIdentity.CdB) identity).getCanClass() != CDB_CLASS_ID) {
            throw new IllegalArgumentException("WeichenChef Magnet requires CdB class 0x22");
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public WeichenChefMagnetGeneration getGeneration() {
        return profile;
    }

    public WeichenChefMagnetGeneration getDetectedGeneration() {
        return detected;
    }

    public State getState() {
        return state;
    }

    public int getFirmware() {
        return firmware;
    }

    public boolean isReady() {
        return state == State.READY;
    }

    public boolean isConfigurationOpen() {
        return configurationOpen;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public int getModuleId() {
        return ((McanDeviceIdentity.CdB) device.getIdentity()).getModuleId();
    }

    public void openConfiguration() {
        requireActive();
        if (configurationOpen) {
            throw new IllegalStateException("A Magnet configuration panel is already open");
        }
        configurationOpen = true;
        if (getModuleId() == 0) {
            setState(State.RECONNECT);
        } else {
            load(true);
        }
    }

    /** Cancel this panel's work without destroying the connection-owned service. */
    public void closeConfiguration() {
        if (disposed) {
            return;
        }
        configurationOpen = false;
        cancelOperation();
        setState(State.IDLE);
    }

    /**
     * Retest using reads only. All twelve extended timing values must be valid.
     * A partial or absent response remains inconclusive, never "older".
     */
    public void sendGenerationProbe() {
        requireActive();
        ThreadingUtil.runOnGUI(() -> {
            if (state == State.WRITING) {
                log.warn("Cannot probe Magnet capabilities while writing module {}", getModuleId());
                return;
            }
            if (getModuleId() == 0) {
                setState(State.RECONNECT);
                return;
            }
            detected = WeichenChefMagnetGeneration.UNKNOWN;
            profile = WeichenChefMagnetGeneration.UNKNOWN;
            load(true);
        });
    }

    /** Apply a user-confirmed override; the UI must warn before calling this. */
    public void useProfile(WeichenChefMagnetGeneration selected) {
        requireActive();
        Objects.requireNonNull(selected);
        if (state == State.WRITING || getModuleId() == 0) {
            throw new IllegalStateException("Profile cannot change during a write or before commissioning");
        }
        profile = selected;
        load(false);
    }

    public void reload() {
        requireActive();
        if (getModuleId() == 0 || state == State.WRITING) {
            throw new IllegalStateException("Cannot reload this module now");
        }
        load(profile == WeichenChefMagnetGeneration.UNKNOWN);
    }

    private void load(boolean probe) {
        cancelOperation();
        values.clear();
        expected.clear();
        probing = probe;
        verifying = false;
        setState(State.LOADING);
        List<int[]> frames = CdBWeichenChefCommands.buildMagnetReads(getModuleId(),
                probe || profile == WeichenChefMagnetGeneration.GEN_002_PLUS);
        pending.clear();
        for (int[] frame : frames) {
            if (frame[6] != 224) {
                pending.add(key(frame[6], frame[5]));
            }
        }
        int token = operation;
        sendSequence(frames, token, () -> awaitReplies(token, 0));
    }

    private void sendSequence(List<int[]> frames, int token, Runnable finished) {
        for (int i = 0; i < frames.size(); i++) {
            int[] frame = frames.get(i);
            later(token, (long) i * FRAME_INTERVAL_MS, () -> sender.sendFrame(frame));
        }
        later(token, (long) frames.size() * FRAME_INTERVAL_MS + 300, finished);
    }

    private void later(int token, long delay, Runnable action) {
        scheduler.schedule(() -> ThreadingUtil.runOnGUI(() -> {
            if (token == operation) {
                action.run();
            }
        }), delay);
    }

    private void awaitReplies(int token, int retries) {
        later(token, REPLY_TIMEOUT_MS, () -> {
            if (pending.isEmpty()) {
                finishLoad();
                return;
            }
            if (retries == MAX_RETRIES) {
                boolean missingRequired = pending.stream().anyMatch(k -> !optionalProbeKey(k));
                if (missingRequired) {
                    log.warn("Magnet module {}: {} register replies missing or unverified",
                            getModuleId(), pending.size());
                    setState(verifying ? State.VERIFY_FAILED : State.LOAD_FAILED);
                } else {
                    finishLoad();
                }
                return;
            }
            List<int[]> reads = new ArrayList<>();
            for (int k : pending) {
                reads.add(CdBConfigCommands.buildRead(CDB_CLASS_ID, getModuleId(), k & 0xFF, k >> 8));
            }
            sendSequence(reads, token, () -> awaitReplies(token, retries + 1));
        });
    }

    private boolean optionalProbeKey(int k) {
        return probing && profile == WeichenChefMagnetGeneration.UNKNOWN
                && CdBWeichenChefCommands.isMagnetTimingRegister(k >> 8) && (k & 0xFF) > 4;
    }

    private void finishLoad() {
        setState(profile == WeichenChefMagnetGeneration.UNKNOWN ? State.UNRESOLVED : State.READY);
    }

    private void setState(State next) {
        state = next;
        listeners.forEach(Listener::stateChanged);
        // The table listens to the bean rather than to this service.
        device.notifyDeviceServiceChanged();
    }

    private void cancelOperation() {
        operation++;
        probing = false;
        pending.clear();
        stopCoil();
    }

    public void dispose() {
        ThreadingUtil.runOnGUI(() -> {
            cancelOperation();
            disposed = true;
            configurationOpen = false;
            setState(State.RECONNECT);
            listeners.clear();
        });
    }

    private void requireActive() {
        if (disposed) {
            throw new IllegalStateException("Magnet service is disposed");
        }
    }

    /**
     * Handle parsed configuration replies. Address/RMK readback is Little Endian,
     * although every reference tool configuration write is Big Endian.
     * @param frame typed frame
     * @param raw original reply
     */
    public void handleCdBReply(CdBFrameParser.CdBFrame frame, MarklinReply raw) {
        if (!(frame instanceof CdBFrameParser.ReadReplyFrame)
                || frame.classId() != CDB_CLASS_ID || frame.moduleId() != getModuleId()
                || raw.getElement(4) != 4) {
            return;
        }
        CdBFrameParser.ReadReplyFrame read = (CdBFrameParser.ReadReplyFrame) frame;
        int reg = read.reg();
        int slot = read.slot();
        int value = reg == 6 || reg == 7 || reg == 8
                ? (raw.getElement(8) << 8) | raw.getElement(7) : read.value();
        ThreadingUtil.runOnGUI(() -> receive(reg, slot, value));
    }

    private void receive(int reg, int slot, int value) {
        if (state != State.LOADING || !pending.contains(key(reg, slot))) {
            return;
        }
        if (reg == 1 && slot == 0) {
            firmware = value;
            device.setSoftwareVersion(value);
            device.setDeviceState(McanDeviceState.ONLINE);
        } else if (!validValue(reg, slot, value)) {
            log.warn("Magnet module {}: invalid reg {} slot {} value {}", getModuleId(), reg, slot, value);
            return;
        }
        int k = key(reg, slot);
        if (expected.containsKey(k) && expected.get(k) != value) {
            return;
        }
        pending.remove(k);
        values.put(k, value);
        if (probing && supportsExtendedTiming()) {
            detected = WeichenChefMagnetGeneration.GEN_002_PLUS;
            profile = detected;
        }
        listeners.forEach(l -> l.valueReceived(reg, slot, value));
        if (pending.isEmpty()) {
            finishLoad();
        } else {
            listeners.forEach(Listener::stateChanged);
        }
    }

    private boolean supportsExtendedTiming() {
        for (int reg : new int[]{2, 16, 17}) {
            for (int slot = 5; slot <= 8; slot++) {
                if (!values.containsKey(key(reg, slot))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Register value for the current load (EDT callers). */
    public Integer getValue(int reg, int slot) {
        return values.get(key(reg, slot));
    }

    private static int key(int reg, int slot) {
        return (reg << 8) | slot;
    }

    /** Validate reference tool ranges, not the inconsistent designer maxima. */
    public static boolean validValue(int reg, int slot, int value) {
        if (slot < 1) {
            return false;
        }
        if (CdBWeichenChefCommands.isMagnetTimingRegister(reg)) {
            return slot <= 8 && value >= (reg == 2 ? 5 : reg == 16 ? 10 : 1)
                    && value <= (reg == 2 ? 10000 : 65000);
        }
        if (reg == 6 || reg == 8) {
            return slot <= (reg == 6 ? 5 : 4)
                    && ((value >= 0x3000 && value < 0x3000 + 320)
                    || (value >= 0x3800 && value < 0x3800 + 9999));
        }
        if (reg == 7) {
            return slot <= 8 && value >= 1 && value <= 1024;
        }
        return slot <= 4 && (reg == 3 || reg == 4 || reg == 5 || reg == 9 || reg == 18)
                && (value == 0 || value == 1);
    }

    private List<Integer> scopeKeys(SaveScope scope) {
        List<Integer> keys = new ArrayList<>();
        int[] registers = scope == SaveScope.ADDRESSES ? new int[]{6, 8}
                : scope == SaveScope.FEEDBACK ? new int[]{7} : new int[]{2, 3, 4, 5, 16, 17, 18, 9};
        for (int reg : registers) {
            int count = reg == 6 ? 5 : reg == 7 ? 8 : 4;
            for (int slot = 1; slot <= count; slot++) {
                keys.add(key(reg, slot));
                if (CdBWeichenChefCommands.isMagnetTimingRegister(reg)
                        && profile == WeichenChefMagnetGeneration.GEN_002_PLUS) {
                    keys.add(key(reg, slot + 4));
                }
            }
        }
        return keys;
    }

    /** Snapshot of one save scope. Keys are (register &lt;&lt; 8) | slot. */
    public Map<Integer, Integer> configuration(SaveScope scope) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (int k : scopeKeys(scope)) {
            Integer value = values.get(k);
            if (value == null) {
                throw new IllegalStateException("Configuration is incomplete");
            }
            result.put(k, value);
        }
        return result;
    }

    /** Write exactly one scope. Then verify every written register by readback. */
    public void save(SaveScope scope, Map<Integer, Integer> configuration) {
        if (!isReady()) {
            throw new IllegalStateException("Magnet configuration is not ready");
        }
        List<Integer> keys = scopeKeys(scope);
        if (!configuration.keySet().equals(new LinkedHashSet<>(keys))) {
            throw new IllegalArgumentException("Incorrect Magnet configuration scope");
        }
        List<int[]> frames = new ArrayList<>();
        Map<Integer, Integer> snapshot = new LinkedHashMap<>();
        for (int k : keys) {
            int value = configuration.get(k);
            if (!validValue(k >> 8, k & 0xFF, value)) {
                throw new IllegalArgumentException("Invalid Magnet register value");
            }
            snapshot.put(k, value);
            frames.add(CdBConfigCommands.buildWrite(CDB_CLASS_ID, getModuleId(), k & 0xFF, k >> 8, value));
        }
        if (scope != SaveScope.PROPERTIES) {
            frames.add(CdBWeichenChefCommands.buildMagnetEndSave(getModuleId()));
        }
        cancelOperation();
        setState(State.WRITING);
        int token = operation;
        sendSequence(frames, token, () -> {
            if (scope != SaveScope.PROPERTIES) {
                // Save epilogue must follow the end-save frame on this sender.
                // McanDeviceManager handles the reply like any other class ping reply
                // use McanDeviceManager.discoverCdBDevice for user-triggered scans.
                sender.sendFrame(CdBConfigCommands.buildPing(CDB_CLASS_ID));
            }
            load(false);
            expected.putAll(snapshot);
            verifying = true;
        });
    }

    public void resetModule() {
        requireActive();
        if (getModuleId() == 0 || state == State.WRITING) {
            throw new IllegalStateException("Cannot reset this module now");
        }
        cancelOperation();
        sender.sendFrame(CdBConfigCommands.buildFactoryReset(CDB_CLASS_ID, getModuleId()));
        setState(State.RECONNECT);
    }

    public void writeNewModuleId(int newId) {
        requireActive();
        if (newId < 1 || newId > 63) {
            throw new IllegalArgumentException("Magnet module ID must be 1-63");
        }
        if (state == State.WRITING) {
            throw new IllegalStateException("Cannot commission during a write");
        }
        cancelOperation();
        sender.sendFrame(CdBConfigCommands.buildWrite(CDB_CLASS_ID, getModuleId(), 0, 24, newId));
        setState(State.RECONNECT);
    }

    public void testOutput(int turnout, boolean green) {
        if (!isReady() || turnout < 1 || turnout > 4) {
            throw new IllegalStateException("Coil test requires a loaded profile and turnout 1-4");
        }
        stopCoil();
        boolean older = profile == WeichenChefMagnetGeneration.GEN_001;
        int first = values.get(key(6, 1));
        int address = values.get(key(6, turnout));
        int[] off = CdBWeichenChefCommands.buildMagnetCoilTest(first, address, green, false, older);
        coilOff = off;
        sender.sendFrame(CdBWeichenChefCommands.buildMagnetCoilTest(first, address, green, true, older));
        scheduler.schedule(() -> ThreadingUtil.runOnGUI(() -> {
            if (coilOff == off) {
                stopCoil();
            }
        }), 500);
    }

    private void stopCoil() {
        if (coilOff != null) {
            sender.sendFrame(coilOff);
            coilOff = null;
        }
    }

    public void handleStatusFrame(MarklinReply raw) {
        if (!(raw.getFrame() instanceof Cs2AccessoryCommands.AccessoryReplyFrame)) {
            return;
        }
        Cs2AccessoryCommands.AccessoryReplyFrame reply = (Cs2AccessoryCommands.AccessoryReplyFrame) raw.getFrame();
        if (reply.header().priority() != 0 || reply.header().dlc() < 5) {
            return;
        }
        // The reference tools match the module in the hash low byte and the low 16 address bits.
        int source = reply.header().hash() & 0xFF;
        int address = (int) (reply.address() & 0xFFFF);
        int status = reply.aspect();
        ThreadingUtil.runOnGUI(() -> {
            if (!configurationOpen || profile == WeichenChefMagnetGeneration.UNKNOWN
                    || (source != getModuleId()
                    && !(profile == WeichenChefMagnetGeneration.GEN_002_PLUS && source == 48))
                    || (status != 253 && status != 254 && status != 255)) {
                return;
            }
            for (int slot = 1; slot <= 4; slot++) {
                if (Integer.valueOf(address).equals(values.get(key(6, slot)))) {
                    int turnout = slot;
                    listeners.forEach(l -> l.positionReceived(turnout, status));
                }
            }
        });
    }

    private static final Logger log = LoggerFactory.getLogger(WeichenChefMagnetService.class);
}
