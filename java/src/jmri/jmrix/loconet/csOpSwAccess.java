
package jmri.jmrix.loconet;

import java.awt.event.ActionEvent;
import javax.annotation.Nonnull;
import javax.swing.Timer;
import jmri.ProgListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.ProgrammerException;

/**
 *
 * @author given
 */
public class csOpSwAccess implements LocoNetListener {

    private Timer csOpSwAccessTimer;
    private Timer csOpSwValidTimer;
    private cmdStnOpSwStateType cmdStnOpSwState;
    private int cmdStnOpSwNum;
    private boolean cmdStnOpSwVal;
    private LocoNetSystemConnectionMemo memo;
    private ProgListener p;
    private boolean doingWrite;
    private int[] opSwBytes;
    private boolean haveValidLowBytes;
    private boolean haveValidHighBytes;

    public csOpSwAccess(@Nonnull LocoNetSystemConnectionMemo memo, @Nonnull ProgListener p) {
        this.memo = memo;
        this.p = p;
        // listen to the LocoNet
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        csOpSwAccessTimer = null;
        csOpSwValidTimer = null;
        cmdStnOpSwState = cmdStnOpSwStateType.IDLE;
        opSwBytes = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        haveValidLowBytes = false;
        haveValidHighBytes = false;
        log.debug("csOpSwAccess constructor");

    }

    public void setProgrammerListener(@Nonnull ProgListener p) {
        this.p = p;
    }

    public void readCsOpSw(String opSw, @Nonnull ProgListener pL) throws ProgrammerException {
        log.debug("reading a cs opsw : {}", opSw);
        // Note: Only get here if decoder xml specifies LocoNetCsOpSwMode.

        if (csOpSwAccessTimer == null) {
            log.debug("initializing timers");
            initializeCsOpSwAccessTimer();
            initializeCsOpSwValidTimer();
        }

        p = pL;
        doingWrite = false;
        log.debug("read Command Station OpSw{}", opSw);

        String[] parts;
        parts = opSw.split("\\.");
        ProgListener temp = pL;
        if ((parts.length == 2) &&
               (parts[0].equals("csOpSw")) &&
                ((Integer.parseInt(parts[1])) >= 1) &&
                (Integer.parseInt(parts[1]) <= 128)) {
            log.trace("splitting CV: {} becomes {} and {}", opSw, parts[0], parts[1]);
            // a valid command station OpSw identifier was found
            log.trace("Valid typeWord = 1; attempting to read OpSw{}.", Integer.parseInt(parts[1]));
            log.trace("starting from state {}", cmdStnOpSwState);
            readCmdStationOpSw(Integer.parseInt(parts[1]));
            return;
        } else {
            log.warn("Cannot perform Cs OpSw access: parts.length={}, parts[]={}",parts.length, parts);
            p = null;
            if (temp != null) {
                temp.programmingOpReply(0,ProgListener.NotImplemented);
            }
            return;
        }
    }

    public void writeCsOpSw(String opSw, int val, @Nonnull ProgListener pL) throws ProgrammerException {
        p = null;
        String[] parts = opSw.split("\\.");
        if (((val != 0) && (val != 1)) ||
                (parts.length != 2) ||
                (!parts[0].equals("csOpSw")) ||
                (Integer.parseInt(parts[1]) <= 0) ||
                (Integer.parseInt(parts[1]) >= 129)) {
            // invalid request - signal it to the programmer
            log.warn("Cannot perform Cs OpSw access: parts.length={}, parts[]={}, val={}",parts.length, parts, val);
            if (pL != null) {
                pL.programmingOpReply(0,ProgListener.NotImplemented);
            }
            return;
        }

        if (csOpSwAccessTimer == null) {
            initializeCsOpSwAccessTimer();
            initializeCsOpSwValidTimer();
        }

        // Command Station OpSws are handled via slot 0x7f.
        p = pL;
        doingWrite = true;
        log.debug("write Command Station OpSw{} as {}", opSw, (val>0)?"c":"t");
        int opSwNum = Integer.parseInt(parts[1]);
        log.debug("CS OpSw number {}", opSwNum);
        if (!updateCmdStnOpSw(opSwNum,
                (val==1))) {
            sendFinalProgrammerReply(-1, ProgListener.ProgrammerBusy);
        }
    }

    @Override
    public void message(LocoNetMessage m) {
        if (cmdStnOpSwState == cmdStnOpSwStateType.IDLE) {
            return;
        }
        boolean value;
        if ((m.getOpCode() == LnConstants.OPC_SL_RD_DATA) &&
                (m.getElement(1) == 0x0E) &&
                ((m.getElement(2) & 0x7E) == 0x7E) &&
                ((cmdStnOpSwState == cmdStnOpSwStateType.QUERY) ||
                ((cmdStnOpSwState == cmdStnOpSwStateType.QUERY_ENHANCED)))) {
            log.debug("got slot {} read data", m.getElement(2));
            updateStoredOpSwsFromRead(m);
            if ((cmdStnOpSwState == cmdStnOpSwStateType.QUERY) ||
                    (cmdStnOpSwState == cmdStnOpSwStateType.QUERY_ENHANCED)) {
                log.debug("got slot {} read data in response to OpSw query", m.getElement(2));
                if (((m.getElement(7) & 0x40) == 0x40) &&
                        (cmdStnOpSwState == cmdStnOpSwStateType.QUERY)){
                    // attempt to get extended OpSw info
                    csOpSwAccessTimer.restart();
                    LocoNetMessage m2 = new LocoNetMessage(new int[] {0xbb, 0x7e, 0x00, 0x00});
                    cmdStnOpSwState = cmdStnOpSwStateType.QUERY_ENHANCED;
                    memo.getLnTrafficController().sendLocoNetMessage(m2);
                    csOpSwAccessTimer.start();
                    return;
                }
                csOpSwAccessTimer.stop();
                cmdStnOpSwState = cmdStnOpSwStateType.HAS_STATE;
                log.debug("starting valid timer");
                csOpSwValidTimer.start();   // start the "valid data" timer
                if (doingWrite == true) {
                    log.debug("now can finish the write by updating the correct bit...");
                    finishTheWrite();
                } else {
                    if (!(((cmdStnOpSwNum > 0) && (cmdStnOpSwNum < 65) && (haveValidLowBytes)) ||
                            ((cmdStnOpSwNum > 64) && (cmdStnOpSwNum < 129) && (haveValidHighBytes)))) {
                        ProgListener temp = p;
                        p = null;
                        if (temp != null) {
                            log.debug("Aborting - OpSw {} beyond allowed range", cmdStnOpSwNum);
                            temp.programmingOpReply(0, ProgListener.NotImplemented);
                        } else {
                            log.warn("no programmer to which the error condition can be returned.");
                        }
                    } else {
                        value = extractCmdStnOpSw(cmdStnOpSwNum);
                        log.debug("now can return the extracted OpSw{} read data ({}) to the programmer", cmdStnOpSwNum, value);
                        sendFinalProgrammerReply(value?1:0, ProgListener.OK);
                    }
                }
            } else if ((cmdStnOpSwState == cmdStnOpSwStateType.QUERY_BEFORE_WRITE) ||
                    (cmdStnOpSwState == cmdStnOpSwStateType.QUERY_ENHANCED_BEFORE_WRITE)){
                if (((m.getElement(7) & 0x40) == 0x40) &&
                        (cmdStnOpSwState == cmdStnOpSwStateType.QUERY_BEFORE_WRITE)) {
                    csOpSwAccessTimer.restart();
                    LocoNetMessage m2 = new LocoNetMessage(new int[] {0xbb, 0x7e, 0x00, 0x00});
                    cmdStnOpSwState = cmdStnOpSwStateType.QUERY_ENHANCED_BEFORE_WRITE;
                    memo.getLnTrafficController().sendLocoNetMessage(m2);
                    csOpSwAccessTimer.start();
                    return;
                }
                log.debug("have received OpSw query before a write; now can process the data modification");
                csOpSwAccessTimer.stop();
                cmdStnOpSwState = cmdStnOpSwStateType.WRITE;
                LocoNetMessage m2 = updateOpSwVal(cmdStnOpSwNum, cmdStnOpSwVal);
                log.debug("performing enhanced opsw write: {}",m2.toString());
                log.debug("todo; uncomment the send?");
                //memo.getLnTrafficController().sendLocoNetMessage(m2);
                csOpSwAccessTimer.start();
            }
        } else if ((m.getOpCode() == LnConstants.OPC_LONG_ACK) &&
                (m.getElement(1) == 0x6f) &&
                (m.getElement(2) == 0x7f) &&
                (cmdStnOpSwState == cmdStnOpSwStateType.WRITE)) {
            csOpSwAccessTimer.stop();
            cmdStnOpSwState = cmdStnOpSwStateType.HAS_STATE;
            value = extractCmdStnOpSw(cmdStnOpSwNum);
            sendFinalProgrammerReply(value?1:0,ProgListener.OK);
        }
    }

    public void readCmdStationOpSw(int cv) {
        log.debug("readCmdStationOpSw: state is {}, have lowvalid {}, have highvalid {}, asking for OpSw{}",
                cmdStnOpSwState, haveValidLowBytes?"true":"false",
                haveValidHighBytes?"true":"false", cv);
        if (cmdStnOpSwState == cmdStnOpSwStateType.HAS_STATE) {
            if ((((cv > 0) && (cv < 65) && haveValidLowBytes)) ||
                (((cv > 64) && (cv < 129) && haveValidHighBytes))) {
                // can re-use previous state - it has not "expired" due to time since read.
            log.debug("readCmdStationOpSw: returning state from previously-stored state for OpSw{}", cv);
                returnCmdStationOpSwVal(cv);
            } else {
                log.warn("Cannot perform Cs OpSw access of OpSw {} account out-of-range for this command station.",cv);
                sendFinalProgrammerReply(-1,ProgListener.NotImplemented);
            }
        } else if ((cmdStnOpSwState == cmdStnOpSwStateType.IDLE) ||
                (cmdStnOpSwState == cmdStnOpSwStateType.HAS_STATE)) {
            // do not have valid data or old data has "expired" due to time since read.
            // Need to send a slot 127 (and 126, as appropriate) read to LocoNet
            log.debug("readCmdStationOpSw: attempting to read some CVs");
            updateCmdStnOpSw(cv,false);
        } else {
            log.warn("readCmdStationOpSw: aborting - cmdStnOpSwState is odd: {}", cmdStnOpSwState);
            sendFinalProgrammerReply(-1,ProgListener.UnknownError);
        }
    }

    public void returnCmdStationOpSwVal(int cmdStnOpSwNum) {
        boolean returnVal = extractCmdStnOpSw(cmdStnOpSwNum);
        if (p != null) {
            // extractCmdStnOpSw did not find an erroneous condition
            log.debug("returnCmdStationOpSwVal: Returning OpSw{} value of {}", cmdStnOpSwNum, returnVal);
            p.programmingOpReply(returnVal?1:0, ProgListener.OK);
        }
    }

    public boolean updateCmdStnOpSw(int opSwNum, boolean val) {
        if (cmdStnOpSwState == cmdStnOpSwStateType.HAS_STATE) {
            if (!doingWrite) {
                log.debug("updateCmdStnOpSw: should already have OpSw values from previous read.");
                return false;
            } else {
                cmdStnOpSwVal = val;
                cmdStnOpSwNum = opSwNum;
                finishTheWrite();
                return true;
            }
        }
        if (cmdStnOpSwState != cmdStnOpSwStateType.IDLE)  {
            log.debug("updateCmdStnOpSw: cannot query OpSw values from state {}", cmdStnOpSwState);
            return false;
        }
        log.debug("updateCmdStnOpSw: attempting to query the OpSws when state = {}", cmdStnOpSwState);
        cmdStnOpSwState = cmdStnOpSwStateType.QUERY;
        cmdStnOpSwNum = opSwNum;
        cmdStnOpSwVal = val;
        int[] contents = {LnConstants.OPC_RQ_SL_DATA, 0x7F, 0x0, 0x0};
        memo.getLnTrafficController().sendLocoNetMessage(new LocoNetMessage(contents));
        csOpSwAccessTimer.start();

        return true;
    }

    public boolean extractCmdStnOpSw(int cmdStnOpSwNum) {

        if (((cmdStnOpSwNum > 0) && (cmdStnOpSwNum < 65) && (haveValidLowBytes)) ||
                ((cmdStnOpSwNum > 64) && (cmdStnOpSwNum < 129) && (haveValidHighBytes))){

            log.debug("attempting to extract value for OpSw {} with haveValidLowBytes {} and haveValidHighBytes {}",
                    cmdStnOpSwNum, haveValidLowBytes, haveValidHighBytes);
            int msgByte = (cmdStnOpSwNum-1) /8;
            int bitpos = (cmdStnOpSwNum-1)-(8*msgByte);
            boolean retval = (((opSwBytes[msgByte] >> bitpos) & 1) == 1);
            log.debug("extractCmdStnOpSw: opsw{} from bit {} of opSwByte[{}]={} gives {}", cmdStnOpSwNum, bitpos, msgByte, opSwBytes[msgByte], retval);
            return retval;
        } else {
            log.debug("failing extract account problem with cmdStnOpSwNum={}, haveValidLowBytes {} and haveValidHighBytes {}",
                cmdStnOpSwNum, haveValidLowBytes, haveValidHighBytes);
            csOpSwAccessTimer.stop();
            csOpSwValidTimer.stop();
            sendFinalProgrammerReply(-1,ProgListener.UnknownError);
            return false;
        }
    }

    public LocoNetMessage updateOpSwVal(int cmdStnOpSwNum, boolean cmdStnOpSwVal) {
        if (((cmdStnOpSwNum -1) & 0x07) == 7) {
            log.warn("Cannot program OpSw{} account LocoNet encoding limitations.",cmdStnOpSwNum);
            sendFinalProgrammerReply(-1,ProgListener.UnknownError);
            return new LocoNetMessage(new int[] {LnConstants.OPC_GPBUSY, 0x0});
        } else if ((cmdStnOpSwNum < 1) || (cmdStnOpSwNum > 128))  {
            log.warn("Cannot program OpSw{} account OpSw number out of range.",cmdStnOpSwNum);
            sendFinalProgrammerReply(-1, ProgListener.NotImplemented);
            return new LocoNetMessage(new int[] {LnConstants.OPC_GPBUSY, 0x0});
        }
        int messageByte;
        log.debug("updateOpSwVal: OpSw{} = {}", cmdStnOpSwNum, cmdStnOpSwVal);
        changeOpSwBytes(cmdStnOpSwNum, cmdStnOpSwVal);
        LocoNetMessage m = new LocoNetMessage(14);
        m.setOpCode(0xEF);
        m.setElement(1, 0x0e);
        m.setElement(2, (cmdStnOpSwNum >= 65)?0x7E:0x7F);

        m.setElement(3, opSwBytes[0+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(4, opSwBytes[1+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(5, opSwBytes[2+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(6, opSwBytes[3+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(7, 0);
        m.setElement(8, opSwBytes[4+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(9, opSwBytes[5+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(10, opSwBytes[6+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(11, opSwBytes[7+(8*(cmdStnOpSwNum>64?1:0))]);
        m.setElement(12, 0);
        m.setElement(13, 0);
        return m;
    }

    private void finishTheWrite() {
        cmdStnOpSwState = cmdStnOpSwStateType.WRITE;
        LocoNetMessage m2 = updateOpSwVal(cmdStnOpSwNum,
                cmdStnOpSwVal);
        if (m2.getNumDataElements() == 2) {
            // failure - no message provided - must be out-of-range opsw number
            sendFinalProgrammerReply(-1, ProgListener.UnknownError);
            return;
        }

        m2.setOpCode(LnConstants.OPC_WR_SL_DATA);
        log.debug("finish the write sending LocoNet cmd stn opsw write message {}, length={}", m2.toString(), m2.getNumDataElements());
        memo.getLnTrafficController().sendLocoNetMessage(m2);
        csOpSwAccessTimer.start();
    }
    
    private void sendFinalProgrammerReply(int val, int response) {
        log.debug("returning response {} with value {} to programmer", response, val);
            ProgListener temp = p;
            p = null;
            if (temp != null) {
                temp.programmingOpReply(val, response);
            }

    }

    enum cmdStnOpSwStateType {
        IDLE,
        QUERY,
        QUERY_ENHANCED,
        QUERY_BEFORE_WRITE,
        QUERY_ENHANCED_BEFORE_WRITE,
        WRITE,
        HAS_STATE}

    void initializeCsOpSwAccessTimer() {
        if (csOpSwAccessTimer == null) {
            csOpSwAccessTimer = new Timer(500, (ActionEvent e) -> {
                log.debug("csOpSwAccessTimer timed out!");
                ProgListener temp = p;
                p = null;
                if (temp != null) {
                    temp.programmingOpReply(0, ProgListener.FailedTimeout);
                }
            });
        csOpSwAccessTimer.setRepeats(false);
        }
    }

    void initializeCsOpSwValidTimer() {
        if (csOpSwValidTimer == null) {
            csOpSwValidTimer = new Timer(1000, (ActionEvent e) -> {
                log.debug("csOpSwValidTimer timed out; invalidating held data!");
                haveValidLowBytes = false;
                haveValidHighBytes = false;
                cmdStnOpSwState = cmdStnOpSwStateType.IDLE;
                });
       csOpSwValidTimer.setRepeats(false);
        }
    }
    private void updateStoredOpSwsFromRead(LocoNetMessage m) {
        if ((m.getOpCode() == 0xE7) &&
                (m.getElement(1) == 0x0e) &&
                (m.getElement(2) == 0x7f)) {
            opSwBytes[0] = m.getElement(3);
            opSwBytes[1] = m.getElement(4);
            opSwBytes[2] = m.getElement(5);
            opSwBytes[3] = m.getElement(6);
            opSwBytes[4] = m.getElement(8);
            opSwBytes[5] = m.getElement(9);
            opSwBytes[6] = m.getElement(10);
            opSwBytes[7] = m.getElement(11);
            opSwBytes[8] = 0;
            opSwBytes[10] = 0;
            opSwBytes[11] = 0;
            opSwBytes[12] = 0;
            opSwBytes[13] = 0;
            opSwBytes[14] = 0;
            opSwBytes[15] = 0;
            haveValidLowBytes = true;
            haveValidHighBytes = false;
        } else if ((m.getOpCode() == 0xE7) &&
                (m.getElement(1) == 0x0e) &&
                (m.getElement(2) == 0x7e)) {
            opSwBytes[8] = m.getElement(3);
            opSwBytes[9] = m.getElement(4);
            opSwBytes[10] = m.getElement(5);
            opSwBytes[11] = m.getElement(6);
            opSwBytes[12] = m.getElement(8);
            opSwBytes[13] = m.getElement(9);
            opSwBytes[14] = m.getElement(10);
            opSwBytes[15] = m.getElement(11);
            haveValidHighBytes = true;
        }
    }
    private void changeOpSwBytes(int cmdStnOpSwNum, boolean cmdStnOpSwVal) {
        log.debug("updating OpSw{} to {}", cmdStnOpSwNum, cmdStnOpSwVal);
        int msgByte = (cmdStnOpSwNum-1) / 8;
        int bitpos = (cmdStnOpSwNum-1)-(8*msgByte);
        int newVal = (opSwBytes[msgByte] & (~(1<<bitpos))) | ((cmdStnOpSwVal?1:0)<<bitpos);
        log.debug("updating OpSwBytes[{}] from {} to {}", msgByte, opSwBytes[msgByte], newVal);
        opSwBytes[msgByte] = newVal;
    }
    
    // accessor
    public cmdStnOpSwStateType getState() {
        return cmdStnOpSwState;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(csOpSwAccess.class);
}
