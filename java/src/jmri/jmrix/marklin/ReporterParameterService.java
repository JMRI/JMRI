package jmri.jmrix.marklin;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.cdb.CdBReporterCommands;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.util.ThreadingUtil;

/**
 * Protocol service for GleisReporter deLuxe (CdB class 0x0D) and
 * StromSniffer (CdB class 0x0B) modules.
 * <p>
 * TODO: implement {@code McanDeviceService} once that interface exists, so the
 * classifier layer can attach this service to an {@link McanDevice} automatically
 * after discovery instead of requiring manual wiring by UI panels.
 * <p>
 * Owns all CAN bus interaction including module discovery, property
 * read/write and occupancy status frame dispatch. All listener
 * callbacks are dispatched on the Swing EDT via
 * {@link ThreadingUtil#runOnGUI}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class ReporterParameterService {

    /**
     * Callback interface for Reporter protocol events.
     */
    public interface ReporterListener {
        default void moduleFound(int rawId) {}
        default void factoryIdModeEntered() {}
        default void firmwareReceived(int hi, int lo, boolean showSonderfunktion, boolean showSlaveRow) {}
        default void hardwareReceived(int hi, int lo, Integer chipType, String manualText) {}
        default void voltageReceived(String text, boolean warn) {}
        default void moduleNameReceived(String name) {}
        default void einschaltzeitReceived(int channel, int ms) {}
        default void ausschaltzeitReceived(int channel, int ms) {}
        default void sonderfunktionReceived(int index) {}
        default void freezCheckReceived(boolean on) {}
        default void freezAdresseReceived(int addr) {}
        default void freezStatusReceived(Boolean boosterOn) {}
        default void slaveAdresseReceived(int addr, boolean enabled) {}
        default void stoerungAktReceived(boolean on) {}
        default void rmkStoerungReceived(int addr) {}
        default void stoerungStatusReceived(Boolean ok) {}
        default void occupancyReceived(int channel, Boolean occupied) {}
        default void inputNumbersUpdated(int channel, String rmkAddr) {}
        default void moduleControlsEnabled(boolean enabled) {}
    }

    /**
     * Configuration data for writing back to a Reporter module.
     */
    public static class ReporterConfig {
        public int[] einSchaltzeiten = new int[8];
        public int[] ausSchaltzeiten = new int[8];
        public int sonderfunktionIdx = 0;
        public String modulname = "";
        public int rmkStoerung = 101;
        public boolean stoerungAkt = false;
        public int rmkAdresFreez = 100;
        public int slaveAdresse = 0;
    }

    private final McanDevice device;
    private final McanFrameSender sender;
    private final List<ReporterListener> listeners = new CopyOnWriteArrayList<>();

    private int currentRawId = 0;
    private int rmk = 0;
    private int firmwareHi = 0;
    private int firmwareLo = 0;
    private int rmkStoerungValue = 101;
    private int freezAdresse = 0;

    private final StringBuilder modulnameBuffer = new StringBuilder();

    public ReporterParameterService(McanDevice device, McanFrameSender sender) {
        this.device = Objects.requireNonNull(device, "device cannot be null");
        this.sender = Objects.requireNonNull(sender, "sender cannot be null");
    }

    public void addListener(ReporterListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ReporterListener listener) {
        listeners.remove(listener);
    }

    /**
     * Select a module: deselect current (if any), select new, notify input numbers, start query.
     *
     * @param rawId module raw ID (1..127)
     */
    public void selectModule(int rawId) {
        int canClass = getCanClass();
        if (currentRawId > 0) {
            sender.sendFrame(CdBConfigCommands.buildDeselect(canClass, currentRawId));
        }
        currentRawId = rawId;
        rmk = rawId * 8 - 8;

        // Notify input number labels
        for (int i = 0; i < 8; i++) {
            final int ch = i;
            final String addr = String.valueOf(rmk + i + 1);
            ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.inputNumbersUpdated(ch, addr)));
        }

        sender.sendFrame(CdBConfigCommands.buildSelect(canClass, rawId));
        startPropertyQuery();
    }

    /**
     * Deselect the currently selected module (if any).
     */
    public void deselectModule() {
        if (currentRawId > 0) {
            sender.sendFrame(CdBConfigCommands.buildDeselect(getCanClass(), currentRawId));
            currentRawId = 0;
        }
    }

    /**
     * Send all property read frames.
     */
    public void startPropertyQuery() {
        if (currentRawId == 0) return;
        for (int[] frame : CdBReporterCommands.buildReadAllProperties(getCanClass(), currentRawId)) {
            sender.sendFrame(frame);
        }
    }

    /**
     * Write all configuration properties to the module.
     *
     * @param config configuration data
     */
    public void writeConfiguration(ReporterConfig config) {
        if (currentRawId == 0) return;
        int canClass = getCanClass();

        // Sonderfunktion (reg 12)
        sender.sendFrame(CdBReporterCommands.buildWriteSonderfunktion(canClass, currentRawId,
            config.sonderfunktionIdx));

        // Module name (reg 250) via raw write  -  up to 29 chars in 6-char packets
        writeModulname(canClass, config.modulname);

        // RMK Störung address (reg 10)
        sender.sendFrame(CdBReporterCommands.buildWriteStorungsRmk(canClass, currentRawId,
            config.rmkStoerung));

        // Störung active (reg 11)
        sender.sendFrame(CdBReporterCommands.buildWriteStorungsFlag(canClass, currentRawId,
            config.stoerungAkt ? 1 : 0));

        // Freez RMK (reg 18)  -  StromSniffer only
        if (isStromSniffer()) {
            sender.sendFrame(CdBReporterCommands.buildWriteFreezeRmkAddr(canClass, currentRawId,
                config.rmkAdresFreez));
        }

        // Slave address (reg 21)
        sender.sendFrame(CdBReporterCommands.buildWriteSlaveAddr(canClass, currentRawId, 21,
            config.slaveAdresse));

        // EinSchaltzeiten (reg 2, channels 1-8)
        for (int ch = 0; ch < 8; ch++) {
            sender.sendFrame(CdBReporterCommands.buildWriteChEinSchaltzeit(canClass, currentRawId,
                ch + 1, config.einSchaltzeiten[ch]));
        }

        // AusSchaltzeiten (reg 3, channels 1-8)
        for (int ch = 0; ch < 8; ch++) {
            sender.sendFrame(CdBReporterCommands.buildWriteChAusSchaltzeit(canClass, currentRawId,
                ch + 1, config.ausSchaltzeiten[ch]));
        }
    }

    private void writeModulname(int canClass, String name) {
        int len = Math.min(name.length(), 29);
        int packets = len / 6 + 1;
        for (int i = 0; i < packets; i++) {
            int[] chars = new int[6];
            for (int j = 0; j < 6; j++) {
                int pos = i * 6 + j;
                chars[j] = (pos < len) ? (name.charAt(pos) & 0xFF) : 0;
            }
            sender.sendFrame(CdBConfigCommands.buildWriteModulnamePacket(canClass, currentRawId, i, chars));
        }
    }

    /**
     * Toggle self-test (Verbindungstest, reg 238).
     *
     * @param on true to enable
     */
    public void setSelbstTest(boolean on) {
        if (currentRawId == 0) return;
        sender.sendFrame(CdBConfigCommands.buildWrite(getCanClass(), currentRawId, 238, 3, on ? 1 : 0));
    }

    /**
     * Toggle booster freeze (reg 16, StromSniffer only).
     *
     * @param on true to enable
     */
    public void setFreezCheck(boolean on) {
        if (currentRawId == 0 || !isStromSniffer()) return;
        sender.sendFrame(CdBConfigCommands.buildWrite(getCanClass(), currentRawId, 16, 3, on ? 1 : 0));
        if (!on) {
            ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.freezStatusReceived(null)));
        }
    }

    /**
     * Write new module ID (factory ID assignment).
     *
     * @param newId new module ID
     */
    public void writeNewModuleId(int newId) {
        sender.sendFrame(CdBConfigCommands.buildWriteNewModuleId(getCanClass(), newId, 0, 0));
    }

    /**
     * Send module reset.
     */
    public void resetModule() {
        if (currentRawId == 0) return;
            sender.sendFrame(CdBConfigCommands.buildDeselect(getCanClass(), currentRawId));
        currentRawId = 0;
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
            } else if (pingReply.isFactoryFresh()) {
                ThreadingUtil.runOnGUI(() -> listeners.forEach(ReporterListener::factoryIdModeEntered));
            }
            return;
        }

        if (frame.frameType() == CdBFrameType.READ_REPLY) {
            if (frame.moduleId() != currentRawId) return;
            dispatchReadReply(raw);
        }
    }

    /**
     * Handle a standard MCAN S88/feedback status frame (occupancy broadcast).
     * Byte pattern: byte[0]=0, byte[1]=35, bytes[2-3] encodes module class,
     * bytes[7-8]=absolute RMK address, byte[9]=0 occupied / 1 free.
     *
     * @param raw raw 13-byte reply
     */
    public void handleStatusFrame(MarklinReply raw) {
        if (raw.getElement(0) != 0 || raw.getElement(1) != 35) return;
        int hash16 = ((raw.getElement(2) & 0xFF) << 8) | (raw.getElement(3) & 0xFF);
        // Check module matches: (hash16 - 2816) == adresseRaw - 128
        int check = hash16 - 2816;
        if (check != currentRawId - 128) return;

        int absRmk = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        int status = raw.getElement(9) & 0xFF;

        // Störung check
        if (rmkStoerungValue == absRmk) {
            boolean ok = (status == 1);
            ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.stoerungStatusReceived(ok)));
        }

        // Freez check
        if (freezAdresse == absRmk) {
            boolean boosterOn = (status == 0);
            ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.freezStatusReceived(boosterOn)));
        }

        // Channel occupancy: offset = absRmk - (rawId*8 - 7)
        int offset = absRmk - (currentRawId * 8 - 7);
        if (offset >= 0 && offset < 8) {
            boolean occupied = (status == 0);
            final int ch = offset;
            ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.occupancyReceived(ch, occupied)));
        }
    }

    // ---- private dispatch ----

    private void dispatchReadReply(MarklinReply raw) {
        int subcmd = raw.getElement(5) & 0xFF;
        int reg = raw.getElement(6) & 0xFF;

        switch (reg) {
            case 250: handleModulname(raw, subcmd);    break;
            case 1:   handleFirmware(raw);             break;
            case 2:   handleEinschaltzeit(raw, subcmd); break;
            case 3:   handleAusschaltzeit(raw, subcmd); break;
            case 12:  handleSonderfunktion(raw);       break;
            case 244: handleHardware(raw);             break;
            case 245: handleSubtype(raw);              break;
            case 246: handleVoltage(raw);              break;
            case 11:  handleStoerungAkt(raw);          break;
            case 10:  handleRmkStoerung(raw);          break;
            case 16:  handleFreezOrSlave16(raw);       break;
            case 17:  handleFreezStatus17(raw);        break;
            case 18:  handleFreezAdresse18(raw);       break;
            case 21:  handleSlaveAdresse21(raw);       break;
            default:  break;
        }
    }

    private void handleModulname(MarklinReply raw, int subcmd) {
        if (subcmd == 0) modulnameBuffer.setLength(0);
        for (int i = 7; i <= 12; i++) {
            int ch = raw.getElement(i) & 0xFF;
            if (ch > 0 && ch < 255) modulnameBuffer.append((char) ch);
        }
        final String name = modulnameBuffer.toString();
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.moduleNameReceived(name)));
    }

    private void handleFirmware(MarklinReply raw) {
        firmwareHi = raw.getElement(7) & 0xFF;
        firmwareLo = raw.getElement(8) & 0xFF;
        final int hi = firmwareHi;
        final int lo = firmwareLo;
        boolean sonder = showSonderfunktion(hi, lo);
        boolean slave = showSlaveRow(hi, lo);
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.firmwareReceived(hi, lo, sonder, slave)));
    }

    private void handleEinschaltzeit(MarklinReply raw, int subcmd) {
        if (subcmd < 1 || subcmd > 8) return;
        int v = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        v = clampEinschaltzeit(v);
        final int ch = subcmd;
        final int val = v;
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.einschaltzeitReceived(ch, val)));
    }

    private void handleAusschaltzeit(MarklinReply raw, int subcmd) {
        if (subcmd < 1 || subcmd > 8) return;
        int v = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        v = clampAusschaltzeit(v);
        final int ch = subcmd;
        final int val = v;
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.ausschaltzeitReceived(ch, val)));
    }

    private void handleSonderfunktion(MarklinReply raw) {
        int idx = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        final int i = Math.min(idx, 2);
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.sonderfunktionReceived(i)));
    }

    private void handleHardware(MarklinReply raw) {
        final int hi = raw.getElement(7) & 0xFF;
        final int lo = raw.getElement(8) & 0xFF;
        Integer chip = null;
        if ((raw.getElement(4) & 0xFF) == 5) chip = raw.getElement(9) & 0xFF;
        int hwVal = hi;
        String anlText = null;
        if (hwVal > 7) anlText = "See manual version 2.2";
        else if (hwVal > 6) anlText = "See manual version 2.1";
        final Integer chipFinal = chip;
        final String anl = anlText;
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.hardwareReceived(hi, lo, chipFinal, anl)));
    }

    private void handleSubtype(MarklinReply raw) {
        // Subtype 3 = no StromSniffer-specific features
    }

    private void handleVoltage(MarklinReply raw) {
        long v = (raw.getElement(7) & 0xFF);
        v <<= 8;
        v = (v + (raw.getElement(8) & 0xFF)) * 68L;
        v /= 1000L;
        long vor = v / 100L;
        long nach = v - vor * 100L;
        boolean warn = (vor < 9);
        final String txt = "Operating voltage: " + vor + "." + nach + " V";
        final boolean w = warn;
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.voltageReceived(txt, w)));
    }

    private void handleStoerungAkt(MarklinReply raw) {
        final boolean on = (raw.getElement(8) == 1);
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.stoerungAktReceived(on)));
    }

    private void handleRmkStoerung(MarklinReply raw) {
        int v = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        rmkStoerungValue = v;
        final int val = v;
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.rmkStoerungReceived(val)));
    }

    private void handleFreezOrSlave16(MarklinReply raw) {
        if (isStromSniffer()) {
            if (raw.getElement(5) == 0 && (raw.getElement(8) & 0xFF) < 2) {
                final boolean on = (raw.getElement(8) == 1);
                ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.freezCheckReceived(on)));
            }
        } else if (firmwareHi < 6) {
            handleSlaveAdresseFromRaw(raw);
        }
    }

    private void handleFreezStatus17(MarklinReply raw) {
        if (!isStromSniffer()) return;
        final boolean boosterOn = (raw.getElement(8) == 1);
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.freezStatusReceived(boosterOn)));
    }

    private void handleFreezAdresse18(MarklinReply raw) {
        if (!isStromSniffer()) return;
        int v = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        v = (v >= 1 && v <= 65000) ? v : 100;
        freezAdresse = v;
        final int val = v;
        ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.freezAdresseReceived(val)));
    }

    private void handleSlaveAdresse21(MarklinReply raw) {
        handleSlaveAdresseFromRaw(raw);
    }

    private void handleSlaveAdresseFromRaw(MarklinReply raw) {
        int v = ((raw.getElement(7) & 0xFF) << 8) + (raw.getElement(8) & 0xFF);
        if (v < 255) {
            final int val = v;
            final boolean enabled = (v != 0);
            ThreadingUtil.runOnGUI(() -> listeners.forEach(l -> l.slaveAdresseReceived(val, enabled)));
        }
    }

    // ---- static helpers ----

    /** Clamp Einschaltzeit to valid range 1-6000; default 30 if out of range. */
    public static int clampEinschaltzeit(int v) {
        return (v >= 1 && v <= 6000) ? v : 30;
    }

    /** Clamp Ausschaltzeit to valid range 1-6000; default 120 if out of range. */
    public static int clampAusschaltzeit(int v) {
        return (v >= 1 && v <= 6000) ? v : 120;
    }

    /** Returns true if firmware version gates the Sonderfunktion (fw hi >= 8). */
    public static boolean showSonderfunktion(int firmwareHi, int firmwareLo) {
        return firmwareHi >= 8;
    }

    /** Returns true if slave row should be visible (hidden only for fw 7.1). */
    public static boolean showSlaveRow(int firmwareHi, int firmwareLo) {
        return !(firmwareHi == 7 && firmwareLo == 1);
    }

    private boolean isStromSniffer() {
        return getCanClass() == 0x0B;
    }

    private int getCanClass() {
        McanDeviceIdentity identity = device.getIdentity();
        if (identity.isCdB()) {
            return ((McanDeviceIdentity.CdB) identity).getCanClass();
        }
        return 0x0D; // fallback GleisReporter
    }
}
