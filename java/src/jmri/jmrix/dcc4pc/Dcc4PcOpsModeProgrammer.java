package jmri.jmrix.dcc4pc;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.RailCom;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.CommandStation;
import jmri.NmraPacket;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Provides an Ops mode proxy programming interface for a RailCom Reader. This
 * forwards the read request to the command station to forward on and handles
 * sending back the CV reading results from the RailCom message
 * <p>
 * A read sends the POM read packet through the other connection's command
 * station, and waits only for the decoder's RailCom reply, decoded by the
 * reader. It doesn't use that connection's own ops-mode read: a command
 * station that can't read RailCom keeps its programmer busy for seconds after
 * each read, waiting for a value it will never get.
 * <p>
 * Packets are paced: each is handed to the command station only once it has
 * had time to send the last one with its repeats, because a LocoNet command
 * station drops a packet it's given while still busy, without telling
 * sendPacket. A packet with no reply after RETRY_AFTER is sent once more.
 * Resending every second instead (2026-10-13) built a backlog the command
 * station was still working through 20 s later.
 * <p>
 * Without a command station, the read goes through the other connection's
 * programmer instead, and finishes with the first value that arrives: the
 * RailCom reply, or the value that programmer reads back.
 * <p>
 * A write is sent the same way. The decoder answers a POM write with the
 * value it then holds, so the write is done when that matches; if it never
 * does, the write ends with ConfirmFailed. Without a command station, the
 * other connection's programmer does the write and reports the result.
 *
 * @see jmri.Programmer
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class Dcc4PcOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements PropertyChangeListener, AddressedProgrammer {

    int pAddress = 0;
    boolean pLongAddress;
    RailCom rcTag;
    int cv;
    jmri.ProgListener progListener = null;

    /** True while a read or confirm is waiting for its value. */
    boolean awaitingValue = false;

    /**
     * Longest a read waits. A decoder answers a POM read in the RailCom
     * cutout after the packet, so a value normally arrives well within a
     * second; the rest is for the command station's connection.
     */
    static final int READ_TIMEOUT = 5000;

    /**
     * How many times the command station sends a POM read packet. A decoder
     * may need a moment to fetch the value, and then sends it in later
     * cutouts; only one that follows the read packet itself can be matched to
     * the CV, so it helps if the packet is still being repeated by then.
     */
    static final int READ_REPEATS = 4;

    /**
     * How many times the command station sends a POM write packet. A decoder
     * only writes after two identical write packets in a row.
     */
    static final int WRITE_REPEATS = 4;

    /**
     * Time to leave per repeat before handing the command station another
     * packet. A LocoNet command station drops a packet it's given while still
     * sending the last one (it answers "busy", which sendPacket doesn't
     * report), so on 2026-10-13 every write sent straight after another was
     * lost. This is an estimate: about one DCC packet per repeat, with room
     * for the command station's own refresh traffic.
     * <p>
     * 2026-10-15: 150 ms made no difference to how many sends went
     * unanswered, so the losses aren't the command station being busy.
     */
    static final long GAP_PER_REPEAT = 60;

    /**
     * If no reply has come after this long, the packet is sent once more, in
     * case it or its answer was lost. Answers normally come 160-270 ms after
     * the send (2026-10-15); 1500 ms made each lost one cost far more than
     * the answer itself.
     */
    static final int RETRY_AFTER = 600;

    /** Earliest time any DCC4PC programmer may hand the command station its next packet. */
    private static long nextSendAllowed = 0;

    /** Counts operations, so a delayed send or retry from an earlier one is dropped. */
    private int operation = 0;

    AddressedProgrammerManager defaultManager;
    Programmer defaultProgrammer;
    /** Sends the POM packets; null to go through defaultProgrammer instead. */
    final CommandStation commandStation;

    public Dcc4PcOpsModeProgrammer(boolean pLongAddress, int pAddress, AddressedProgrammerManager dp) {
        this(pLongAddress, pAddress, dp, null);
    }

    /**
     * @param pLongAddress true for a long address
     * @param pAddress     the loco's address
     * @param dp           the other connection's programmer manager, for writes
     * @param cs           the other connection's command station, which sends
     *                     the POM read packets; null to read through the
     *                     programmer instead
     */
    public Dcc4PcOpsModeProgrammer(boolean pLongAddress, int pAddress, AddressedProgrammerManager dp,
            @CheckForNull CommandStation cs) {
        defaultManager = dp;
        defaultProgrammer = defaultManager.getAddressedProgrammer(pLongAddress, pAddress);
        commandStation = cs;
        this.pAddress = pAddress;
        this.pLongAddress = pLongAddress;
        LONG_TIMEOUT = READ_TIMEOUT;
        rcTag = (RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("" + pAddress);
    }

    /**
     * The NMRA ops-mode "verify byte" packet (S-9.2.1 long-form CV access,
     * instruction 1110 01VV), which a RailCom decoder answers with the CV's
     * value on channel 2. The data byte doesn't matter for a read.
     *
     * @param address  the loco's address
     * @param longAddr true for a long address
     * @param cvNum    the CV, 1 to 1024
     * @return the packet, as {@link NmraPacket} builds it for
     *         {@link CommandStation#sendPacket}, or empty if the address or CV is invalid
     */
    @Nonnull
    static byte[] pomReadPacket(int address, boolean longAddr, int cvNum) {
        if (cvNum < 1 || cvNum > 1024) {
            return new byte[0];
        }
        int instruction = 0xE4 | (((cvNum - 1) >> 8) & 0x03);
        byte[] packet = NmraPacket.threeBytePacket(address, longAddr, (byte) instruction, (byte) ((cvNum - 1) & 0xFF), (byte) 0);
        return packet == null ? new byte[0] : packet;
    }

    /**
     * The value being written, or -1 for a read. A decoder answers a POM
     * write with the value it then holds, so the write is done when that
     * matches.
     */
    int writeValue = -1;

    /** The last value a write was answered with, or -1 for none yet. */
    int lastWriteReply = -1;

    /**
     * {@inheritDoc}
     *
     * The POM write packet goes out through the command station, and the
     * write is done when the decoder's RailCom reply shows the new value.
     * Without a command station, the other connection's programmer does the
     * write and reports the result.
     */
    @Override
    synchronized public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        if (commandStation == null) {
            progListener = p;
            defaultProgrammer.setMode(getMode());
            defaultProgrammer.writeCV(CVname, val, (value, status) -> finishWrite(value, status));
            return;
        }
        byte[] packet = NmraPacket.opsCvWriteByte(pAddress, pLongAddress, Integer.parseInt(CVname), val);
        if (packet == null) {
            throw new ProgrammerException("Can't write CV " + CVname + " of " + getAddress());
        }
        start(CVname, p, val);
        send(packet, WRITE_REPEATS);
    }

    private synchronized void finishWrite(int value, int status) {
        ProgListener l = progListener;
        progListener = null;
        notifyProgListenerEnd(l, value, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        if (commandStation == null) {
            start(CVname, p, -1);
            defaultProgrammer.readCV(CVname, (value, status) -> commandStationReplied(value, status));
            return;
        }
        byte[] packet = pomReadPacket(pAddress, pLongAddress, Integer.parseInt(CVname));
        if (packet.length == 0) {
            throw new ProgrammerException("Can't read CV " + CVname + " of " + getAddress());
        }
        start(CVname, p, -1);
        send(packet, READ_REPEATS);
    }

    /**
     * Hand the packet to the command station once it has had time to finish
     * the last one, and once more if no reply has come after RETRY_AFTER.
     * Must be called while the operation is waiting for its reply.
     */
    private void send(byte[] packet, int repeats) {
        int op = operation;
        sendPaced(packet, repeats, op);
        jmri.util.TimerUtil.scheduleOnLayoutThread(new java.util.TimerTask() {
            @Override
            public void run() {
                if (isWaiting(op)) {
                    log.debug("No reply yet for CV {} of {}; sending once more", cv, pAddress);
                    sendPaced(packet, repeats, op);
                }
            }
        }, RETRY_AFTER);
    }

    /** For tests: forget the pacing left by earlier sends. */
    static synchronized void resetPacing() {
        nextSendAllowed = 0;
    }

    private synchronized boolean isWaiting(int op) {
        return awaitingValue && operation == op;
    }

    private void sendPaced(byte[] packet, int repeats, int op) {
        long delay;
        synchronized (Dcc4PcOpsModeProgrammer.class) {
            long now = System.currentTimeMillis();
            long sendAt = Math.max(now, nextSendAllowed);
            nextSendAllowed = sendAt + repeats * GAP_PER_REPEAT;
            delay = sendAt - now;
        }
        if (delay == 0) {
            log.debug("Sent CV {} of {} x{}", cv, pAddress, repeats);
            commandStation.sendPacket(packet, repeats);
            return;
        }
        jmri.util.TimerUtil.scheduleOnLayoutThread(new java.util.TimerTask() {
            @Override
            public void run() {
                if (isWaiting(op)) {
                    log.debug("Sent CV {} of {} x{} after {} ms pacing", cv, pAddress, repeats, delay);
                    commandStation.sendPacket(packet, repeats);
                }
            }
        }, delay);
    }

    /**
     * {@inheritDoc}
     *
     * Reads the CV and lets the listener know whether it matches. The command
     * station's own confirm can't be used: it may only report that it sent
     * the packets.
     */
    @Override
    public synchronized void confirmCV(String cvName, int val, ProgListener p) throws ProgrammerException {
        readCV(cvName, (value, status) -> {
            if (status == ProgListener.OK && value != val) {
                status = ProgListener.ConfirmFailed;
            }
            if (p != null) {
                p.programmingOpReply(value, status);
            }
        });
    }

    /**
     * Start waiting for the decoder's RailCom reply for a CV.
     *
     * @param CVname the CV
     * @param p      told the result
     * @param value  the value being written, or -1 for a read
     */
    private void start(String CVname, ProgListener p, int value) {
        cv = Integer.parseInt(CVname);
        progListener = p;
        writeValue = value;
        lastWriteReply = -1;
        operation++;
        awaitingValue = true;
        rcTag.addPropertyChangeListener(this);
        rcTag.setExpectedCv(cv);
        startLongTimer();
        defaultProgrammer.setMode(getMode());
        log.debug("{} CV {} of {}", value < 0 ? "Read" : "Write " + value + " to", cv, pAddress);
    }

    /** The command station's connection has answered the read. */
    private void commandStationReplied(int value, int status) {
        if (status == ProgListener.OK) {
            log.debug("CV {} = {}, read by the command station", cv, value);
            finishRead(value, ProgListener.OK);
        } else {
            // keep waiting: the RailCom reply comes through the reader, not the command station
            log.debug("Command station could not read CV {} (status {}); waiting for RailCom", cv, status);
        }
    }

    /** End the operation, once, whichever of the RailCom reply, the command station and the timer comes first. */
    private synchronized void finishRead(int value, int status) {
        if (!awaitingValue) {
            return;
        }
        awaitingValue = false;
        log.debug("CV {} of {} finished: value {} status {}", cv, pAddress, value, status);
        stopTimer();
        rcTag.removePropertyChangeListener(this);
        rcTag.setExpectedCv(-1);
        ProgListener l = progListener;
        progListener = null;
        notifyProgListenerEnd(l, value, status);
    }

    /**
     * {@inheritDoc}
     *
     * Types implemented here.
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        return defaultProgrammer.getSupportedModes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized protected void timeout() {
        if (writeValue >= 0 && lastWriteReply >= 0) {
            log.warn("CV {} of {} still reads {} after writing {}", cv, pAddress, lastWriteReply, writeValue);
            finishRead(lastWriteReply, ProgListener.ConfirmFailed);
        } else {
            log.debug("No RailCom reply for CV {} within {} ms", cv, READ_TIMEOUT);
            finishRead(0, ProgListener.FailedTimeout);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The loco's RailCom tag has a POM reply for the CV being read or
     * written.
     */
    @Override
    public synchronized void propertyChange(java.beans.PropertyChangeEvent e) {
        if (!"cvvalue".equals(e.getPropertyName()) || !awaitingValue) {
            return;
        }
        int repliedCv = (Integer) e.getOldValue();
        int newValue = (Integer) e.getNewValue();
        if (repliedCv != cv) {
            // normal: the command station keeps repeating earlier POM packets
            log.debug("RailCom reply for CV {} while waiting for CV {}; ignored", repliedCv, cv);
        } else if (writeValue < 0 || newValue == writeValue) {
            log.debug("CV {} = {}, from RailCom", repliedCv, newValue);
            finishRead(newValue, ProgListener.OK);
        } else {
            // the reply may have come before the write took effect: wait for the next one
            log.debug("CV {} reads {} while writing {}; waiting", cv, newValue, writeValue);
            lastWriteReply = newValue;
            rcTag.setExpectedCv(cv); // the tag only delivers one value per expected CV
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() {
        return pLongAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAddressNumber() {
        return pAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Dcc4PcOpsModeProgrammer.class);

}
