/**
 * Consist Manager for use with the NceConsist class for the
 * consists it builds.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Daniel Boudreau Copyright (C) 2012
 */
package jmri.jmrix.nce;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NceConsistManager extends AbstractConsistManager {

    private NceSystemConnectionMemo memo = null;

    public NceConsistManager(NceSystemConnectionMemo m) {
        super();
        memo = m;
    }

    /**
     * Request an update from the layout, loading
     * Consists from the command station.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (shouldRequestUpdateFromLayout()) {
            startConsistReader();
        }
    }

    /**
     * This implementation does not support command station assisted consists,
     * so return false.
     *
     */
    @Override
    public boolean isCommandStationConsistPossible() {
        return false;
    }

    /**
     * Does a CS consist require a separate consist address?
     *
     */
    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return false;
    }

    /**
     * Add a new NceConsist with the given address to consistTable/consistList
     */
    @Override
    public Consist addConsist(LocoAddress locoAddress) {
        if (! (locoAddress instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("locoAddress is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(locoAddress)) // no duplicates allowed.
        {
            return consistTable.get(locoAddress);
        }
        log.debug("Add consist, address {}", locoAddress);
        NceConsist consist = new NceConsist((DccLocoAddress) locoAddress, memo);
        consistTable.put(locoAddress, consist);
        notifyConsistListChanged();
        return consist;
    }

    private void addConsist(NceConsist consist) {
        log.debug("Add consist {}", consist.getConsistAddress());
        //delConsist(consist.getConsistAddress()); // remove consist if one exists
        consistTable.put(consist.getConsistAddress(), consist);
        notifyConsistListChanged();
    }

    @Override
    public Consist getConsist(LocoAddress locoAddress) {
        log.debug("Requesting NCE consist {}", locoAddress);
        NceConsist consist = (NceConsist) super.getConsist(locoAddress);
        // Checking the CS memory each time a consist is requested creates
        // lots of NCE messages. Instead note the request: a consist asked for
        // again and again (a panel or throttle showing it) is checked more
        // often by the background check (see checkLookedAt).
        if (consist != null) {
            Long last = lastAsked.put(locoAddress, System.currentTimeMillis());
            if (last != null) {
                prevAsked.put(locoAddress, last);
            }
        }
        return consist;
    }

    // remove the old Consist
    @Override
    public void delConsist(LocoAddress locoAddress) {
        NceConsist consist = (NceConsist) getConsist(locoAddress);
        // kill this consist
        consist.dispose();
        super.delConsist(locoAddress);
        // the kill may not have reached the command station before the next
        // background read: don't take the consist back from memory yet
        deleted.put(locoAddress.getNumber(), System.currentTimeMillis());
    }

    public void startConsistReader() {
        // read command station memory (not USB and not simulator) Can't determine if simulator selected, but can determine if port name is the default
        if (memo.getNceUsbSystem() == NceTrafficController.USB_SYSTEM_NONE && !memo.getNceTrafficController().getPortName().equals(JmrixConfigPane.NONE_SELECTED)
                && memo.getNceTrafficController().getCommandOptions() > NceTrafficController.OPTION_1999) {
            new NceConsistReader().start();
        }
    }

    public class NceConsistReader extends Thread {

        int _consistNum = NceConsist.CONSIST_MAX;

        NceConsistReader() {
            setName("Initialize NCE consists");
        }

        @Override
        public void run() {
            searchNext();
        }

        // NCE allocates consist numbers starting at 127, so we do the same
        private void searchNext() {
            synchronized (this) {
                // we need to wait for the connection to be up and running
                while (!ConnectionStatus.instance().getConnectionState(memo)
                        .equals(ConnectionStatus.CONNECTION_UP)) {
                    log.debug("Waiting for NCE connected");
                    try {
                        wait(2000); // wait 2 seconds and try again
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
                }
            }
            while (_consistNum >= NceConsist.CONSIST_MIN) {
                if (log.isDebugEnabled()) {
                    log.debug("Reading consist from command station: {}", _consistNum);
                }
                // A consist already loaded (from the consist file, which is
                // read at start-up too) is checked against the command
                // station rather than replaced by a fresh copy: the file holds
                // the loco directions, which command station memory does not,
                // and listeners (the JSON server) are attached to it.
                Consist existing = consistTable.get(new DccLocoAddress(_consistNum, false));
                NceConsist consist;
                if (existing instanceof NceConsist) {
                    consist = (NceConsist) existing;
                    consist.checkConsist();
                } else {
                    consist = new NceConsist(_consistNum, memo);
                }
                // wait until consist finishes CS read
                while (!consist.isValid()) {
                    synchronized (this) {
                        try {
                            //log.debug("Waiting for consist "+_consistNum+" to be valid");
                            wait(100); // wait 100 milliseconds and check again
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }
                }
                if (existing == null && consist.getConsistList().size() > 0) {
                    addConsist(consist);
                }
                _consistNum--;
            }
            // when we finish reading, notify any listeners.
            notifyConsistListChanged();
            // off unless asked for: it keeps the command station busy
            if (Boolean.getBoolean(BACKGROUND_CHECK_PROPERTY)) {
                startBackgroundCheck();
            }
        }
    }

    /**
     * Java system property that turns on the background check of consists
     * against command station memory (see below), e.g.
     * {@code -Djmri.jmrix.nce.consistBackgroundCheck=true}. Off by default.
     */
    public static final String BACKGROUND_CHECK_PROPERTY = "jmri.jmrix.nce.consistBackgroundCheck";

    /*
     * The command station does not report consist changes made on a cab, so
     * JMRI's copy of a consist would stay out of date until JMRI restarts.
     * Once the consists have been read, a background thread reads command
     * station consist memory in 16 byte blocks, one every READ_PAUSE_MS: one
     * block holds the lead (or rear) slot of 8 consists, or the mid slots of
     * 2. A consist whose memory no longer matches JMRI's copy gets a full
     * check (NceConsist.checkConsist). A consist found in memory that JMRI
     * doesn't have (built on a cab) is added. Between blocks, a consist that
     * is being looked at (asked for again and again, by a panel or throttle)
     * is checked every LOOKED_AT_CHECK_MS.
     */
    static final int READ_PAUSE_MS = 650;
    static final long LOOKED_AT_CHECK_MS = 3000;
    static final long LOOKED_AT_ASKED_MS = 10000;  // asked for twice within this: being looked at
    static final long DELETED_HOLD_MS = 10000;
    private static final int REPLY_16 = 16;
    private static final int NO_LOCO = -1;

    private volatile Thread scanner = null;
    private final java.util.Map<LocoAddress, Long> lastAsked = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<LocoAddress, Long> prevAsked = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<LocoAddress, Long> lastChecked = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<Integer, Long> deleted = new java.util.concurrent.ConcurrentHashMap<>();

    private synchronized void startBackgroundCheck() {
        if (scanner != null) {
            return;
        }
        scanner = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    scanRound(READ_PAUSE_MS);
                }
            } catch (InterruptedException e) {
                // stopped
            }
        }, "NCE consist check");
        scanner.setDaemon(true);
        scanner.start();
        log.info("Checking NCE consists against the command station in the background");
    }

    /**
     * Stop the background check of consists against command station memory.
     */
    public synchronized void stopBackgroundCheck() {
        if (scanner != null) {
            scanner.interrupt();
            scanner = null;
        }
    }

    // consist numbers JMRI has, lowest first
    private java.util.List<Integer> knownNumbers() {
        java.util.List<Integer> numbers = new java.util.ArrayList<>();
        try {
            for (LocoAddress a : getConsistList()) {
                int n = a.getNumber();
                if (n >= NceConsist.CONSIST_MIN && n <= NceConsist.CONSIST_MAX && !numbers.contains(n)) {
                    numbers.add(n);
                }
            }
        } catch (java.util.ConcurrentModificationException e) {
            // being changed; the next round sees it
        }
        java.util.Collections.sort(numbers);
        return numbers;
    }

    // The background check runs on its own thread, but consistTable is only
    // changed on the layout thread; read and add to it there.
    private Consist consistOnLayout(int number) {
        Consist[] found = new Consist[1];
        jmri.util.ThreadingUtil.runOnLayout(() -> found[0] = consistTable.get(new DccLocoAddress(number, false)));
        return found[0];
    }

    private void addConsistOnLayout(NceConsist consist) {
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            if (!consistTable.containsKey(consist.getConsistAddress())) {
                log.info("Consist {} found on the command station, added in JMRI", consist.getConsistAddress().getNumber());
                addConsist(consist);
            }
        });
    }

    /*
     * First consist number of each block read needed to cover these numbers
     * (lowest first), with perBlock consists in a block.
     */
    static java.util.List<Integer> blockStarts(java.util.List<Integer> numbers, int perBlock) {
        java.util.List<Integer> starts = new java.util.ArrayList<>();
        int i = 0;
        while (i < numbers.size()) {
            int start = numbers.get(i);
            starts.add(start);
            while (i < numbers.size() && numbers.get(i) < start + perBlock) {
                i++;
            }
        }
        return starts;
    }

    /**
     * One round of the background check: read the lead, rear and mid slots
     * of every consist JMRI has, check the ones that differ, add consists
     * found that JMRI doesn't have.
     *
     * @param pauseMs pause after each memory read
     * @throws InterruptedException if stopped
     */
    void scanRound(int pauseMs) throws InterruptedException {
        java.util.List<Integer> numbers = knownNumbers();
        if (numbers.isEmpty()) {
            Thread.sleep(Math.max(pauseMs, 1000));
            return;
        }
        NceTrafficController tc = memo.getNceTrafficController();
        int max = NceConsist.CONSIST_MAX + 1;
        int[] leads = new int[max + 8];
        int[] rears = new int[max + 8];
        int[][] mids = new int[max + 2][];
        boolean[] seen = new boolean[max + 8];
        java.util.Arrays.fill(leads, NO_LOCO);
        java.util.Arrays.fill(rears, NO_LOCO);
        for (int start : blockStarts(numbers, 8)) {
            NceReply r = readBlock(tc, tc.csm.getConsistHeadAddr() + start * 2, pauseMs);
            if (r == null) {
                return; // no answer; try again next round
            }
            for (int i = 0; i < 8; i++) {
                leads[start + i] = locoAt(r, i * 2);
                seen[start + i] = true;
            }
        }
        for (int start : blockStarts(numbers, 8)) {
            NceReply r = readBlock(tc, tc.csm.getConsistTailAddr() + start * 2, pauseMs);
            if (r == null) {
                return;
            }
            for (int i = 0; i < 8; i++) {
                rears[start + i] = locoAt(r, i * 2);
            }
        }
        // mid slots only for consists that can have mids: a rear loco on the
        // command station, or mids in JMRI's copy
        java.util.List<Integer> withMids = new java.util.ArrayList<>();
        for (int n : numbers) {
            Consist c = consistOnLayout(n);
            if (rears[n] != NO_LOCO || (c != null && c.getConsistList() != null && c.getConsistList().size() > 2)) {
                withMids.add(n);
            }
        }
        for (int start : blockStarts(withMids, 2)) {
            NceReply r = readBlock(tc, tc.csm.getConsistMidAddr() + start * 8, pauseMs);
            if (r == null) {
                return;
            }
            for (int i = 0; i < 2 && start + i < mids.length; i++) {
                mids[start + i] = new int[]{locoAt(r, i * 8), locoAt(r, i * 8 + 2), locoAt(r, i * 8 + 4), locoAt(r, i * 8 + 6)};
            }
        }
        // consists that no longer match: full check
        for (int n : numbers) {
            Consist c = consistOnLayout(n);
            if (c instanceof NceConsist && differs((NceConsist) c, leads[n], rears[n], mids[n])) {
                log.debug("Consist {} differs from the command station, checking it", n);
                checkAndWait((NceConsist) c);
            }
        }
        // consists built on a cab, in the blocks read
        long now = System.currentTimeMillis();
        for (int n = NceConsist.CONSIST_MIN; n <= NceConsist.CONSIST_MAX; n++) {
            if (!seen[n] || leads[n] == NO_LOCO || numbers.contains(n)
                    || now - deleted.getOrDefault(n, 0L) < DELETED_HOLD_MS) {
                continue;
            }
            NceConsist consist = new NceConsist(n, memo);
            waitValid(consist);
            if (!consist.getConsistList().isEmpty()) {
                addConsistOnLayout(consist);
            }
        }
    }

    /*
     * true if JMRI's copy of the consist doesn't match these command station
     * slots (loco keys from locoAt; mids null if not read: then there should
     * be none)
     */
    static boolean differs(NceConsist c, int lead, int rear, int[] mids) {
        java.util.List<DccLocoAddress> list = c.getConsistList();
        if (list == null) {
            return false; // disposed
        }
        java.util.List<Integer> jmriLead = new java.util.ArrayList<>();
        java.util.List<Integer> jmriRear = new java.util.ArrayList<>();
        java.util.List<Integer> jmriMids = new java.util.ArrayList<>();
        for (DccLocoAddress a : new java.util.ArrayList<>(list)) {
            int key = key(a);
            int position = c.getPosition(a);
            if (position == Consist.POSITION_LEAD) {
                jmriLead.add(key);
            } else if (position == Consist.POSITION_TRAIL) {
                jmriRear.add(key);
            } else {
                jmriMids.add(key);
            }
        }
        if (lead == NO_LOCO && rear == NO_LOCO) {
            return !list.isEmpty();
        }
        if (!sameSlot(jmriLead, lead) || !sameSlot(jmriRear, rear)) {
            return true;
        }
        java.util.List<Integer> csMids = new java.util.ArrayList<>();
        if (mids != null) {
            for (int m : mids) {
                if (m != NO_LOCO) {
                    csMids.add(m);
                }
            }
        }
        java.util.Collections.sort(jmriMids);
        java.util.Collections.sort(csMids);
        return !jmriMids.equals(csMids);
    }

    private static boolean sameSlot(java.util.List<Integer> jmri, int cs) {
        return cs == NO_LOCO ? jmri.isEmpty() : jmri.size() == 1 && jmri.get(0) == cs;
    }

    // a loco as a number: address, plus 0x10000 if long
    private static int key(DccLocoAddress a) {
        return a.getNumber() + (a.isLongAddress() ? 0x10000 : 0);
    }

    // the loco in 2 bytes of command station memory, or NO_LOCO for an empty
    // slot or the NCE Consist Editor's dummy (Replace) loco
    static int locoAt(NceReply r, int index) {
        int high = r.getElement(index) & 0xFF;
        int address = ((high << 8) & 0x3F00) + (r.getElement(index + 1) & 0xFF);
        if (address == 0 || address == NceConsist.NCE_REPLACE_PLACEHOLDER) {
            return NO_LOCO;
        }
        return address + ((high & 0xC0) == 0xC0 ? 0x10000 : 0);
    }

    // read 16 bytes of memory, then check a consist being looked at, then pause
    private NceReply readBlock(NceTrafficController tc, int memAddr, int pauseMs) throws InterruptedException {
        NceReply r = new BlockRead().read(tc, memAddr);
        checkLookedAt();
        if (pauseMs > 0) {
            Thread.sleep(pauseMs);
        }
        return r;
    }

    /*
     * A consist asked for twice in the last LOOKED_AT_ASKED_MS (a panel or
     * throttle showing it asks every few seconds; a client listing all
     * consists asks far less often) and not checked in the last
     * LOOKED_AT_CHECK_MS: check it now. The one longest unchecked first.
     */
    private void checkLookedAt() throws InterruptedException {
        long now = System.currentTimeMillis();
        LocoAddress pick = null;
        long oldest = Long.MAX_VALUE;
        for (java.util.Map.Entry<LocoAddress, Long> e : prevAsked.entrySet()) {
            LocoAddress a = e.getKey();
            long checked = lastChecked.getOrDefault(a, 0L);
            if (now - e.getValue() <= LOOKED_AT_ASKED_MS && now - checked >= LOOKED_AT_CHECK_MS && checked < oldest) {
                oldest = checked;
                pick = a;
            }
        }
        if (pick != null) {
            Consist c = consistTable.get(pick);
            lastChecked.put(pick, now);
            if (c instanceof NceConsist) {
                checkAndWait((NceConsist) c);
            }
        }
    }

    private void checkAndWait(NceConsist c) throws InterruptedException {
        lastChecked.put(c.getConsistAddress(), System.currentTimeMillis());
        if (c.checkIfQuiet()) {
            waitValid(c);
        }
    }

    // wait (up to 10 s) for a consist's read of command station memory
    private static void waitValid(NceConsist c) throws InterruptedException {
        for (int i = 0; i < 100 && !c.isValid(); i++) {
            Thread.sleep(100);
        }
    }

    // one 16 byte read of command station memory, waiting for the answer
    private static class BlockRead implements NceListener {
        private NceReply reply = null;

        synchronized NceReply read(NceTrafficController tc, int memAddr) throws InterruptedException {
            tc.sendNceMessage(NceMessage.createBinaryMessage(tc, NceBinaryCommand.accMemoryRead(memAddr), REPLY_16), this);
            long end = System.currentTimeMillis() + 3000;
            while (reply == null) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    log.debug("No answer reading command station memory at {}", memAddr);
                    return null;
                }
                wait(left);
            }
            return reply.getNumDataElements() == REPLY_16 ? reply : null;
        }

        @Override
        public synchronized void reply(NceReply r) {
            reply = r;
            notifyAll();
        }

        @Override
        public void message(NceMessage m) {
        }
    }

    private static final Logger log = LoggerFactory.getLogger(NceConsistManager.class);

}
