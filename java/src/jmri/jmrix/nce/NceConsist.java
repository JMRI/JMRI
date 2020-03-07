package jmri.jmrix.nce;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.implementation.DccConsist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Consist definition for a consist on an NCE system. It uses the NCE
 * specific commands to build a consist.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Daniel Boudreau Copyright (C) 2012
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
        if (consistList.size() > 0) {
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
            if (consistList.size() == 0) {
                // add lead loco
                byte command = NceBinaryCommand.LOCO_CMD_FWD_CONSIST_LEAD;
                if (!directionNormal) {
                    command = NceBinaryCommand.LOCO_CMD_REV_CONSIST_LEAD;
                }
                addLocoToConsist(locoAddress.getNumber(), locoAddress.isLongAddress(), command);
                consistPosition.put(locoAddress, DccConsist.POSITION_LEAD);
            } // Second loco to consist?
            else if (consistList.size() == 1) {
                // add rear loco
                byte command = NceBinaryCommand.LOCO_CMD_FWD_CONSIST_REAR;
                if (!directionNormal) {
                    command = NceBinaryCommand.LOCO_CMD_REV_CONSIST_REAR;
                }
                addLocoToConsist(locoAddress.getNumber(), locoAddress.isLongAddress(), command);
                consistPosition.put(locoAddress, DccConsist.POSITION_TRAIL);
            } else {
                // add mid loco
                byte command = NceBinaryCommand.LOCO_CMD_FWD_CONSIST_MID;
                if (!directionNormal) {
                    command = NceBinaryCommand.LOCO_CMD_REV_CONSIST_MID;
                }
                addLocoToConsist(locoAddress.getNumber(), locoAddress.isLongAddress(), command);
                consistPosition.put(locoAddress, consistPosition.size());
            }
            // add loco to lists
            consistList.add(locoAddress);
            consistDir.put(locoAddress, Boolean.valueOf(directionNormal));
        } else {
            log.error("Loco " + locoAddress + " is already part of this consist " + getConsistAddress());
        }

    }

    public void restore(DccLocoAddress locoAddress, boolean directionNormal, int position) {
        consistPosition.put(locoAddress, position);
        super.restore(locoAddress, directionNormal);
        //notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
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
            log.error("Loco " + locoAddress + " is not part of this consist " + getConsistAddress());
        }
    }

    private void loadConsist(int consistNum) {
        if (consistNum > CONSIST_MAX || consistNum < CONSIST_MIN) {
            log.error("Requesting consist " + consistNum + " out of range");
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

    private void startReadNCEconsistThread(boolean check) {
        // read command station memory to get the current consist (can't be a USB, only PH)
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_NONE) {
            mb = new NceReadConsist();
            mb.setName("Read Consist " + _consistNum);
            mb.setConsist(_consistNum);
            mb.setCheck(check);
            mb.start();
        }
    }

    @SuppressWarnings("deprecation") // Thread.stop not likely to be removed
    private void stopReadNCEconsistThread() {
        if (mb != null) {
            try {
                mb.stop();
                mb.join();
            } catch (InterruptedException ex) {
                log.warn("stopReadNCEconsistThread interrupted");
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
        sendNceBinaryCommand(address, NceBinaryCommand.LOCO_CMD_DELETE_LOCO_CONSIST, (byte) 0);
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
        sendNceBinaryCommand(address, NceBinaryCommand.LOCO_CMD_KILL_CONSIST, (byte) 0);
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
            log.debug("Consist " + _consistNum + " read reply not for this consist");
            return;
        }
        if (r.getNumDataElements() != _replyLen) {
            log.error("reply length error, expecting: " + _replyLen + " got: "
                    + r.getNumDataElements());
            return;
        }
        if (_replyLen == 1 && r.getElement(0) == '!') {
            log.debug("Command complete okay for consist " + getConsistAddress());
        } else {
            log.error("Error, command failed for consist " + getConsistAddress());
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

        private static final int CS_CONSIST_MEM = 0xF500;  // start of NCE CS Consist memory
        private static final int CS_CON_MEM_REAR = 0xF600;  // address of rear consist locos
        private static final int CS_CON_MEM_MID = 0xF700;  // address of mid consist locos

        public void setConsist(int number) {
            _consistNum = number;
        }

        public void setCheck(boolean check) {
            _check = check;
        }

        // load up the consist lists by lead, rear, and then mid
        @Override
        public void run() {
            readConsistMemory(_consistNum, LEAD);
            readConsistMemory(_consistNum, REAR);
            readConsistMemory(_consistNum, MID);
            setValid(true);
        }

        /**
         * Reads 16 bytes of NCE consist memory based on consist number and loco
         * number 0=lead 1=rear 2=mid
         */
        private void readConsistMemory(int consistNum, int eNum) {
            if (consistNum > CONSIST_MAX || consistNum < CONSIST_MIN) {
                log.error("Requesting consist " + consistNum + " out of range");
                return;
            }
            // if busy wait
            if (!readWait()) {
                log.error("Time out reading NCE command station consist memory");
                return;
            }
            _locoNum = eNum;
            int nceMemAddr = (consistNum * 2) + CS_CONSIST_MEM;
            if (eNum == REAR) {
                nceMemAddr = (consistNum * 2) + CS_CON_MEM_REAR;
            }
            if (eNum == MID) {
                nceMemAddr = (consistNum * 8) + CS_CON_MEM_MID;
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
        private boolean readWait() {
            int waitcount = 30;
            while (_busy > 0) {
                synchronized (this) {
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
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
                log.debug("Consist " + _consistNum + " read reply not for this consist");
                return;
            }
            log.debug("Consist " + _consistNum + " read reply number " + _locoNum);
            if (r.getNumDataElements() != _replyLen) {
                log.error("reply length error, expecting: " + _replyLen + " got: "
                        + r.getNumDataElements());
                return;
            }

            // are we checking to see if the consist matches CS memory?
            if (_check) {
                log.debug("Checking " + _consistNum);
                if (_locoNum == LEAD) {
                    _validConsist = checkLocoConsist(r, 0, DccConsist.POSITION_LEAD); // consist is valid if there's at least a lead & rear loco
                }
                if (_validConsist && _locoNum == REAR) {
                    _validConsist = checkLocoConsist(r, 0, DccConsist.POSITION_TRAIL);
                }

                if (_validConsist && _locoNum == MID) {
                    for (int index = 0; index < 8; index = index + 2) {
                        checkLocoConsist(r, index, consistPosition.size());
                    }
                }

            } else {
                if (_locoNum == LEAD) {
                    _validConsist = addLocoConsist(r, 0, DccConsist.POSITION_LEAD); // consist is valid if there's at least a lead & rear loco
                }
                if (_validConsist && _locoNum == REAR) {
                    _validConsist = addLocoConsist(r, 0, DccConsist.POSITION_TRAIL);
                }

                if (_validConsist && _locoNum == MID) {
                    for (int index = 0; index < 8; index = index + 2) {
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
            if (address != 0) {
                log.debug("Add loco address " + address + " to consist " + _consistNum);
                restore(new DccLocoAddress(address, isLong), true, position); // we don't know the direction of the loco
                return true;
            }
            return false;
        }

        private boolean checkLocoConsist(NceReply r, int index, int position) {
            int address = getLocoAddrText(r, index);
            boolean isLong = getLocoAddressType(r, index); // Long (true) or short (false) address?
            DccLocoAddress locoAddress = new DccLocoAddress(address, isLong);
            if (contains(locoAddress)) {
                log.debug("Loco address " + locoAddress + " found match for consist " + _consistNum);
            } else if (address != 0) {
                log.debug("New loco address " + locoAddress + " found for consist " + _consistNum);
                restore(locoAddress, true, position); // we don't know the direction of the loco
            } else {
                log.debug("Found loco address 0 for consist " + _consistNum + " index " + index + " position " + position);
                // remove loco by position in consist
                locoAddress = getLocoAddressByPosition(position);
                if (locoAddress != null) {
                    remove(locoAddress);
                }
            }
            return true;
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
            if (rC == 0xC0) {
                return true;
            } else {
                return false;
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceConsist.class);

}
