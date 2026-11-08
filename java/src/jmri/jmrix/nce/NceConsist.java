package jmri.jmrix.nce;

import java.util.ArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.implementation.DccConsist;

/**
 * The Consist definition for a consist on an NCE system. It uses the NCE
 * specific commands to build a consist.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Daniel Boudreau Copyright (C) 2012
 * @author Ken Cameron Copyright (C) 2023
 */
public class NceConsist extends jmri.implementation.DccConsist implements jmri.jmrix.nce.NceListener {

    public static final int CONSIST_MIN = 1;    // NCE doesn't use consist 0
    public static final int CONSIST_MAX = 127;
    private NceTrafficController tc = null;
    private boolean _valid = false;

    // state machine stuff
    private int _busy = 0;
    private int _replyLen = 0;      // expected byte length
    private static final int REPLY_1 = 1;  // reply length of 16 bytes expected
    private byte _consistNum = 0;    // consist number (short address of consist)

    // Initialize a consist for the specific address
    // the Default consist type is an advanced consist
    public NceConsist(int address, NceSystemConnectionMemo m) {
        super(address);
        tc = m.getNceTrafficController();
        loadConsist(address);
    }

    // Initialize a consist for the specific address
    // the Default consist type is an advanced consist
    public NceConsist(DccLocoAddress locoAddress, NceSystemConnectionMemo m) {
        super(locoAddress);
        tc = m.getNceTrafficController();
        loadConsist(locoAddress.getNumber());
    }

    // Clean Up local storage
    @Override
    public void dispose() {
        if(consistList == null) {
           // already disposed;
           return;
        }
        if (!consistList.isEmpty()) {
            // kill this consist
            DccLocoAddress locoAddress = consistList.get(0);
            killConsist(locoAddress.getNumber(), locoAddress.isLongAddress());
        }
        stopReadNCEconsistThread();
        super.dispose();
        consistList = null;
    }

    // Set the Consist Type
    @Override
    public void setConsistType(int consist_type) {
        if (consist_type == Consist.ADVANCED_CONSIST) {
            consistType = consist_type;
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
        }
    }

    /* is there a size limit for this consist?
     */
    @Override
    public int sizeLimit() {
        return 6;
    }

    /**
     * Add a Locomotive to a Consist
     *
     * @param locoAddress     is the Locomotive address to add to the consist
     * @param directionNormal is True if the locomotive is traveling the same
     *                        direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void add(DccLocoAddress locoAddress, boolean directionNormal) {
        if (!contains(locoAddress)) {
            // NCE has 6 commands for adding a loco to a consist, lead, rear, and mid, plus direction
            // First loco to consist?
            if (consistList.isEmpty()) {
                // add lead loco
                byte command = NceMessage.LOCO_CMD_FWD_CONSIST_LEAD;
                if (!directionNormal) {
                    command = NceMessage.LOCO_CMD_REV_CONSIST_LEAD;
                }
                addLocoToConsist(locoAddress.getNumber(), locoAddress.isLongAddress(), command);
                consistPosition.put(locoAddress, DccConsist.POSITION_LEAD);
            } // Second loco to consist?
            else if (consistList.size() == 1) {
                // add rear loco
                byte command = NceMessage.LOCO_CMD_FWD_CONSIST_REAR;
                if (!directionNormal) {
                    command = NceMessage.LOCO_CMD_REV_CONSIST_REAR;
                }
                addLocoToConsist(locoAddress.getNumber(), locoAddress.isLongAddress(), command);
                consistPosition.put(locoAddress, DccConsist.POSITION_TRAIL);
            } else {
                // add mid loco
                byte command = NceMessage.LOCO_CMD_FWD_CONSIST_MID;
                if (!directionNormal) {
                    command = NceMessage.LOCO_CMD_REV_CONSIST_MID;
                }
                addLocoToConsist(locoAddress.getNumber(), locoAddress.isLongAddress(), command);
                consistPosition.put(locoAddress, consistPosition.size());
            }
            // add loco to lists
            consistList.add(locoAddress);
            consistDir.put(locoAddress, directionNormal);
        } else {
            log.error("Loco {} is already part of this consist {}", locoAddress, getConsistAddress());
        }

    }

    public void restore(DccLocoAddress locoAddress, boolean directionNormal, int position) {
        consistPosition.put(locoAddress, position);
        super.restore(locoAddress, directionNormal);
        //notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /*
     * NCE command station memory holds loco addresses only, not direction, so
     * a loco learned from a memory read has an unknown direction. Tools that
     * send a consist command to the command station themselves (the NCE
     * Consist Editor) report the direction they sent here, so JMRI's copy
     * matches the command station.
     */
    // dummy loco address the NCE Consist Editor puts in a slot during Replace
    static final int NCE_REPLACE_PLACEHOLDER = 0x3FFF;

    private final java.util.concurrent.ConcurrentHashMap<DccLocoAddress, Boolean> knownDir = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Record the direction a loco was given in this consist by a command sent
     * directly to the command station.
     *
     * @param locoAddress     the loco
     * @param directionNormal true if forward in the consist
     */
    public void setKnownDirection(DccLocoAddress locoAddress, boolean directionNormal) {
        knownDir.put(locoAddress, directionNormal);
        if (consistDir != null && contains(locoAddress)
                && getLocoDirection(locoAddress) != directionNormal) {
            consistDir.put(locoAddress, directionNormal);
            notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
        }
    }

    // direction for a loco learned from command station memory
    private boolean directionFor(DccLocoAddress locoAddress) {
        Boolean d = knownDir.get(locoAddress);
        return d == null ? true : d;
    }

    /**
     * Restore a locomotive from the consist file. The file can be out of date:
     * the consist may have been changed or killed on the command station while
     * JMRI was not running (or by the NCE Consist Editor). So once the file has
     * been loaded, check this consist against command station memory; locos
     * the command station no longer has are dropped (see checkLocoConsist).
     * The command station reader uses the 3-argument restore, which does not
     * come through here.
     */
    @Override
    public void restore(DccLocoAddress locoAddress, boolean directionNormal) {
        super.restore(locoAddress, directionNormal);
        scheduleVerify();
    }

    private boolean verifyScheduled = false;
    private int verifyTries = 0;
    private static final int VERIFY_DELAY_MS = 2000;
    private static final int VERIFY_MAX_TRIES = 20;   // wait up to ~40 s for a slow first read

    private synchronized void scheduleVerify() {
        if (verifyScheduled || tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            return; // already pending, or a USB system (can't read command station memory)
        }
        verifyScheduled = true;
        verifyTries = 0;
        jmri.util.ThreadingUtil.runOnLayoutDelayed(this::verifyWithCommandStation, VERIFY_DELAY_MS);
    }

    private synchronized void verifyWithCommandStation() {
        if (consistList == null) {
            verifyScheduled = false;
            return; // disposed
        }
        if (!isValid()) {
            // the first read of command station memory is still running
            if (++verifyTries < VERIFY_MAX_TRIES) {
                jmri.util.ThreadingUtil.runOnLayoutDelayed(this::verifyWithCommandStation, VERIFY_DELAY_MS);
            } else {
                verifyScheduled = false;
                log.warn("Consist {}: could not check it against the command station", getConsistAddress());
            }
            return;
        }
        verifyScheduled = false;
        log.debug("Consist {}: checking the consist file copy against the command station", getConsistAddress());
        checkConsist();
    }

    /**
     * Remove a locomotive from this consist
     *
     * @param locoAddress is the locomotive address to remove from this consist
     */
    @Override
    public synchronized void remove(DccLocoAddress locoAddress) {
        if (contains(locoAddress)) {
            // can not delete the lead or rear loco from a NCE consist
            int position = getPosition(locoAddress);
            if (position == DccConsist.POSITION_LEAD || position == DccConsist.POSITION_TRAIL) {
                log.info("Can not delete lead or rear loco from a NCE consist!");
                notifyConsistListeners(locoAddress, ConsistListener.DELETE_ERROR);
                return;
            }
            // send remove loco from consist to NCE command station
            removeLocoFromConsist(locoAddress.getNumber(), locoAddress.isLongAddress());
            //reset the value in the roster entry for CV19
            resetRosterEntryCVValue(locoAddress);

            // remove from lists
            consistRoster.remove(locoAddress);
            consistPosition.remove(locoAddress);
            consistDir.remove(locoAddress);
            consistList.remove(locoAddress);
            notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
        } else {
            log.error("Loco {} is not part of this consist {}", locoAddress, getConsistAddress());
        }
    }

    /**
     * Drop a locomotive from JMRI's copy of this consist only. Used when the
     * command station memory shows it is no longer there (the consist was
     * changed or killed outside JMRI), so there is nothing to send to the
     * command station, and the lead/rear rule in {@link #remove} does not
     * apply.
     *
     * @param locoAddress the locomotive address to drop
     */
    private synchronized void removeFromJmriOnly(DccLocoAddress locoAddress) {
        if (consistList == null || !contains(locoAddress)) {
            return;
        }
        knownDir.remove(locoAddress);
        resetRosterEntryCVValue(locoAddress);
        consistRoster.remove(locoAddress);
        consistPosition.remove(locoAddress);
        consistDir.remove(locoAddress);
        consistList.remove(locoAddress);
        notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    private void loadConsist(int consistNum) {
        if (consistNum > CONSIST_MAX || consistNum < CONSIST_MIN) {
            log.error("Requesting consist {} out of range", consistNum);
            return;
        }
        _consistNum = (byte) consistNum;
        startReadNCEconsistThread(false);
    }

    public void checkConsist() {
        if (!isValid()) {
            return; // already checking the consist
        }
        setValid(false);
        startReadNCEconsistThread(true);
    }

    private NceReadConsist mb = null;

    private synchronized void startReadNCEconsistThread(boolean check) {
        // read command station memory to get the current consist (can't be a USB, only PH)
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_NONE) {
            mb = new NceReadConsist();
            mb.setName("Read Consist " + _consistNum);
            mb.setConsist(_consistNum);
            mb.setCheck(check);
            mb.start();
        }
    }

    private synchronized void stopReadNCEconsistThread() {
        if (mb != null) {
            try {
                mb.interrupt();
                mb.join();
            } catch (InterruptedException ex) {
                log.warn("stopReadNCEconsistThread interrupted");
            } catch (Throwable t) {
                log.error("stopReadNCEconsistThread caught ", t);
                throw t;
            } finally {
                mb = null;
            }
        }
    }

    public DccLocoAddress getLocoAddressByPosition(int position) {
        DccLocoAddress locoAddress;
        ArrayList<DccLocoAddress> list = getConsistList();
        for (int i = 0; i < list.size(); i++) {
            locoAddress = list.get(i);
            if (getPosition(locoAddress) == position) {
                return locoAddress;
            }
        }
        return null;
    }

    /**
     * Used to determine if consist has been initialized properly.
     *
     * @return true if command station memory has been read for this consist
     *         number.
     */
    public boolean isValid() {
        return _valid;
    }

    private void setValid(boolean valid) {
        _valid = valid;
    }

    /**
     * Adds a loco to the consist
     *
     * @param address The address of the loco to be added
     * @param command There are six NCE commands to add a loco to a consist. Add
     *                Lead, Rear, Mid, and the loco direction 3x2 = 6 commands.
     */
    private void addLocoToConsist(int address, boolean isLong, byte command) {
        if (isLong) {
            address += 0xC000; // set the upper two bits for long addresses
        }
        sendNceBinaryCommand(address, command, _consistNum);
    }

    /**
     * Remove a loco from any consist. The consist number is not supplied to
     * NCE.
     *
     * @param address The address of the loco to be removed
     * @param isLong  true if long address
     */
    private void removeLocoFromConsist(int address, boolean isLong) {
        if (isLong) {
            address += 0xC000; // set the upper two bits for long addresses
        }
        sendNceBinaryCommand(address, NceMessage.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);
    }

    /**
     * Kills consist using lead loco address
     * @param address loco address
     * @param isLong true if long address
     */
    void killConsist(int address, boolean isLong) {
        if (isLong) {
            address += 0xC000; // set the upper two bits for long addresses
        }
        sendNceBinaryCommand(address, NceMessage.LOCO_CMD_KILL_CONSIST, (byte) 0);
    }

    private void sendNceBinaryCommand(int nceAddress, byte nceLocoCmd, byte consistNumber) {
        byte[] bl = NceBinaryCommand.nceLocoCmd(nceAddress, nceLocoCmd, consistNumber);
        sendNceMessage(bl, REPLY_1);
    }

    private void sendNceMessage(byte[] b, int replyLength) {
        NceMessage m = NceMessage.createBinaryMessage(tc, b, replyLength);
        _busy++;
        _replyLen = replyLength; // Expect n byte response
        tc.sendNceMessage(m, this);
    }

    @Override
    public void message(NceMessage m) {
        // not used
    }

    @Override
    public void reply(NceReply r) {
        if (_busy == 0) {
            log.debug("Consist {} read reply not for this consist", _consistNum);
            return;
        }
        if (r.getNumDataElements() != _replyLen) {
            log.error("reply length error, expecting: {} got: {}", _replyLen, r.getNumDataElements());
            return;
        }
        if (_replyLen == 1 && r.getElement(0) == NceMessage.NCE_OKAY) {
            log.debug("Command complete okay for consist {}", getConsistAddress());
        } else {
            log.error("Error, command failed for consist {}", getConsistAddress());
        }
    }

    public class NceReadConsist extends Thread implements jmri.jmrix.nce.NceListener {

        // state machine stuff
        private int _consistNum = 0;
        private int _busy = 0;
        private boolean _validConsist = false;    // true when there's a lead and rear loco in the consist
        private boolean _check = false;    // when true update consist to match NCE CS

        private int _replyLen = 0;      // expected byte length
        private static final int REPLY_16 = 16;  // reply length of 16 bytes expected

        private int _locoNum = LEAD;     // which loco, 0 = lead, 1 = rear, 2 = mid
        private static final int LEAD = 0;
        private static final int REAR = 1;
        private static final int MID = 2;

        public void setConsist(int number) {
            _consistNum = number;
        }

        public void setCheck(boolean check) {
            _check = check;
        }

        // load up the consist lists by lead, rear, and then mid
        @Override
        public void run() {
            try{
                readConsistMemory(_consistNum, LEAD);
                readConsistMemory(_consistNum, REAR);
                readConsistMemory(_consistNum, MID);
                setValid(true);
            } catch (InterruptedException e) {
                // we're done!
            } catch (Throwable t) {
                throw t;
            }
        }

        /**
         * Reads 16 bytes of NCE consist memory based on consist number and loco
         * number 0=lead 1=rear 2=mid
         */
        private void readConsistMemory(int consistNum, int eNum) throws InterruptedException { // throw interrupt upward
            if (consistNum > CONSIST_MAX || consistNum < CONSIST_MIN) {
                log.error("Requesting consist {} out of range", consistNum);
                return;
            }
            // if busy wait
            if (!readWait()) {
                log.error("Time out reading NCE command station consist memory");
                return;
            }
            _locoNum = eNum;
            int nceMemAddr = (consistNum * 2) + tc.csm.getConsistHeadAddr();
            if (eNum == REAR) {
                nceMemAddr = (consistNum * 2) + tc.csm.getConsistTailAddr();
            }
            if (eNum == MID) {
                nceMemAddr = (consistNum * 8) + tc.csm.getConsistMidAddr();
            }
            if (eNum == LEAD || _validConsist) {
                byte[] bl = NceBinaryCommand.accMemoryRead(nceMemAddr);
                sendNceMessage(bl, REPLY_16);
            }
        }

        private void sendNceMessage(byte[] b, int replyLength) {
            NceMessage m = NceMessage.createBinaryMessage(tc, b, replyLength);
            _busy++;
            _replyLen = replyLength; // Expect n byte response
            tc.sendNceMessage(m, this);
        }

        // wait up to 30 sec per read
        private boolean readWait() throws InterruptedException { // throw interrupt upward
            int waitcount = 30;
            while (_busy > 0) {
                synchronized (this) {
                    wait(1000);
                }
                if (waitcount-- < 0) {
                    log.error("read timeout");
                    return false;
                }
            }
            return true;
        }

        @Override
        public void message(NceMessage m) {
            // not used
        }

        @SuppressFBWarnings(value = "NN_NAKED_NOTIFY") // notify not naked
        @Override
        public void reply(NceReply r) {
            if (_busy == 0) {
                log.debug("Consist {} read reply not for this consist", _consistNum);
                return;
            }
            log.debug("Consist {} read reply number {}", _consistNum, _locoNum);
            if (r.getNumDataElements() != _replyLen) {
                log.error("reply length error, expecting: {} got: {}", _replyLen, r.getNumDataElements());
                return;
            }

            // are we checking to see if the consist matches CS memory?
            if (_check) {
                log.debug("Checking {}", _consistNum);
                if (_locoNum == LEAD) {
                    _validConsist = checkLocoConsist(r, 0, DccConsist.POSITION_LEAD); // consist is valid if there's at least a lead & rear loco
                }
                if (_validConsist && _locoNum == REAR) {
                    _validConsist = checkLocoConsist(r, 0, DccConsist.POSITION_TRAIL);
                }

                if (_validConsist && _locoNum == MID) {
                    checkMidLocos(r);
                }

            } else {
                if (_locoNum == LEAD) {
                    _validConsist = addLocoConsist(r, 0, DccConsist.POSITION_LEAD); // consist is valid if there's at least a lead & rear loco
                }
                if (_validConsist && _locoNum == REAR) {
                    _validConsist = addLocoConsist(r, 0, DccConsist.POSITION_TRAIL);
                }

                if (_validConsist && _locoNum == MID) {
                    for (int index = 0; index < 8; index += 2) {
                        addLocoConsist(r, index, consistPosition.size());
                    }
                }
            }

            _busy--;

            // wake up thread
            synchronized (this) {
                notify();
            }
        }

        /*
         * Returns true if loco added to consist
         */
        private boolean addLocoConsist(NceReply r, int index, int position) {
            int address = getLocoAddrText(r, index);
            boolean isLong = getLocoAddressType(r, index); // Long (true) or short (false) address?
            if (address != 0 && address != NCE_REPLACE_PLACEHOLDER) {
                log.debug("Add loco address {} to consist {}", address, _consistNum);
                DccLocoAddress a = new DccLocoAddress(address, isLong);
                restore(a, directionFor(a), position);
                return true;
            }
            return false;
        }

        /*
         * Remove from JMRI's copy every loco at this lead/rear position other
         * than keep (and any NCE Replace placeholder left from before).
         */
        private void dropOthersAt(int position, DccLocoAddress keep) {
            ArrayList<DccLocoAddress> current = getConsistList();
            if (current == null) {
                return;
            }
            for (DccLocoAddress a : new ArrayList<>(current)) {
                if (a.equals(keep)) {
                    continue;
                }
                if (a.getNumber() == NCE_REPLACE_PLACEHOLDER || getPosition(a) == position) {
                    removeFromJmriOnly(a);
                }
            }
        }

        /*
         * Returns false if the command station has no lead loco for this
         * consist (it was killed there), so the rear and mid reads are skipped.
         */
        private boolean checkLocoConsist(NceReply r, int index, int position) {
            int address = getLocoAddrText(r, index);
            boolean isLong = getLocoAddressType(r, index); // Long (true) or short (false) address?
            DccLocoAddress locoAddress = new DccLocoAddress(address, isLong);
            if (address == NCE_REPLACE_PLACEHOLDER) {
                // NCE Consist Editor is part way through a Replace: the slot
                // holds its dummy loco. Leave JMRI's copy alone until the
                // real replacement loco is in.
                log.debug("Replace in progress for consist {} position {}", _consistNum, position);
            } else if (address != 0 && contains(locoAddress)) {
                log.debug("Loco address {} found match for consist {}", locoAddress, _consistNum);
                // drop stale locos also listed at this slot (old file, Replace)
                dropOthersAt(position, locoAddress);
                consistPosition.put(locoAddress, position);
            } else if (address != 0) {
                log.debug("New loco address {} found for consist {}", locoAddress, _consistNum);
                // a different loco now holds this slot (Replace): drop the old one
                dropOthersAt(position, locoAddress);
                restore(locoAddress, directionFor(locoAddress), position);
            } else if (position == DccConsist.POSITION_LEAD) {
                // No lead in command station memory: the consist was cleared
                // outside JMRI. Empty JMRI's copy too. remove() can't be used
                // here: it refuses the lead and rear loco and would send a
                // command to the command station for a consist that is gone.
                log.debug("Consist {} is empty in the command station, clearing it", _consistNum);
                ArrayList<DccLocoAddress> current = getConsistList();
                for (DccLocoAddress a : current == null ? new ArrayList<DccLocoAddress>() : new ArrayList<>(current)) {
                    removeFromJmriOnly(a);
                }
                return false;
            } else {
                log.debug("Found loco address 0 for consist {} index {} position {}", _consistNum, index, position);
                // remove loco by position in consist
                locoAddress = getLocoAddressByPosition(position);
                if (locoAddress != null) {
                    removeFromJmriOnly(locoAddress);
                }
            }
            return true;
        }

        /*
         * Mid locos: the command station keeps up to 4. Add any it has that
         * JMRI doesn't, and drop any JMRI mid loco it no longer has.
         */
        private void checkMidLocos(NceReply r) {
            ArrayList<DccLocoAddress> inCs = new ArrayList<>();
            for (int index = 0; index < 8; index += 2) {
                int address = getLocoAddrText(r, index);
                if (address == 0 || address == NCE_REPLACE_PLACEHOLDER) {
                    continue;
                }
                DccLocoAddress a = new DccLocoAddress(address, getLocoAddressType(r, index));
                inCs.add(a);
                if (!contains(a)) {
                    log.debug("New mid loco address {} found for consist {}", a, _consistNum);
                    restore(a, directionFor(a), consistPosition.size());
                }
            }
            ArrayList<DccLocoAddress> current = getConsistList();
            if (current == null) {
                return; // consist disposed
            }
            for (DccLocoAddress a : new ArrayList<>(current)) {
                int position = getPosition(a);
                if (position != DccConsist.POSITION_LEAD && position != DccConsist.POSITION_TRAIL && !inCs.contains(a)) {
                    log.debug("Mid loco {} no longer in consist {} in the command station", a, _consistNum);
                    removeFromJmriOnly(a);
                }
            }
        }

        private int getLocoAddrText(NceReply r, int index) {
            int rC = r.getElement(index++);
            rC = (rC << 8) & 0x3F00;  // Mask off upper two bits
            int rC_l = r.getElement(index);
            rC_l = rC_l & 0xFF;
            rC = rC + rC_l;
            return rC;
        }

        // get loco address type, returns true if long
        private boolean getLocoAddressType(NceReply r, int index) {
            int rC = r.getElement(index);
            rC = rC & 0xC0; // long address if 2 msb are set
            return rC == 0xC0;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceConsist.class);

}
