
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
    private boolean csOpSwsAreValid;
    private cmdStnOpSwStateType cmdStnOpSwState;
    private int cmdStnOpSwNum;
    private boolean cmdStnOpSwVal;
    private LocoNetMessage lastCmdStationOpSwMessage;
    private LocoNetSystemConnectionMemo memo;
    private ProgListener p;
    private boolean doingWrite;

    public csOpSwAccess(@Nonnull LocoNetSystemConnectionMemo memo, @Nonnull ProgListener p) {
        this.memo = memo;
        this.p = p;
        // listen to the LocoNet
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        csOpSwAccessTimer = null;
        csOpSwValidTimer = null;
        csOpSwsAreValid = false;
        cmdStnOpSwState = cmdStnOpSwStateType.IDLE;
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
                (Integer.parseInt(parts[1]) <= 112)) {
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
                (Integer.parseInt(parts[1]) >= 113)) {
            // invalid request - signal it to the programmer
            if (pL != null) {
                pL.programmingOpReply(0,ProgListener.NotImplemented);
            }
            return;
        }

        // validity check is ok - have a valid command station config variable access.
        LocoNetMessage m;
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
                (val==1)?true:false)) {
            ProgListener temp =p;
            p = null;
            if (temp != null) {
                temp.programmingOpReply(0, ProgListener.ProgrammerBusy);
            }
        }
    }

    public void message(LocoNetMessage m) {
        if (cmdStnOpSwState == cmdStnOpSwStateType.IDLE) {
            return;
        }
        boolean value;
        if ((m.getOpCode() == LnConstants.OPC_SL_RD_DATA) &&
                (m.getElement(1) == 0x0E) &&
                (m.getElement(2) == 0x7f)) {
            log.debug("got slot 127 read data");
            if (cmdStnOpSwState == cmdStnOpSwStateType.QUERY) {
                log.debug("got slot 127 read data in response to OpSw query");
                if ((m.getElement(7) & 0x40) == 0x40) {
                    csOpSwAccessTimer.restart();
                    LocoNetMessage m2 = new LocoNetMessage(new int[] {0xbb, 0x7f, 0x40, 0x00});
                    cmdStnOpSwState = cmdStnOpSwStateType.QUERY_ENHANCED;
                    memo.getLnTrafficController().sendLocoNetMessage(m2);
                    csOpSwAccessTimer.start();
                    return;
                }
                csOpSwAccessTimer.stop();
                cmdStnOpSwState = cmdStnOpSwStateType.HAS_STATE;
                lastCmdStationOpSwMessage = m;  // save a copy of the LocoNet message
                csOpSwsAreValid = true;
                csOpSwValidTimer.start();   // start the "valid data" timer
                if (doingWrite == true) {
                    log.debug("now can finish the write by updating the correct bit...");
                    finishTheWrite();
                } else {
                    value = extractCmdStnOpSw(m, cmdStnOpSwNum);
                    log.debug("now can return the extracted OpSw{} read data ({}) to the programmer", cmdStnOpSwNum, value);
                    ProgListener temp = p;
                    p = null;
                    if (temp != null) {
                        log.debug("Returning data");
                        temp.programmingOpReply(value?1:0, ProgListener.OK);
                    } else {
                        log.debug("no programmer to return the data to.");
                    }
                }
            } else if (cmdStnOpSwState == cmdStnOpSwStateType.QUERY_BEFORE_WRITE) {
                if ((m.getElement(7) & 0x40) == 0x40) {
                    csOpSwAccessTimer.restart();
                    LocoNetMessage m2 = new LocoNetMessage(new int[] {0xbb, 0x7f, 0x40, 0x00});
                    cmdStnOpSwState = cmdStnOpSwStateType.QUERY_ENHANCED_BEFORE_WRITE;
                    memo.getLnTrafficController().sendLocoNetMessage(m2);
                    csOpSwAccessTimer.start();
                    return;
                }
                log.debug("hve received OpSw query before a write; now can process the data modification");
                csOpSwAccessTimer.stop();
                LocoNetMessage m2 = updateOpSwVal(m, cmdStnOpSwNum,
                        cmdStnOpSwVal);
                cmdStnOpSwState = cmdStnOpSwStateType.WRITE;
                log.debug("performing enhanced opsw write: {}",m2.toString());
                memo.getLnTrafficController().sendLocoNetMessage(m2);
                csOpSwAccessTimer.start();
            }
        } else if ((m.getOpCode() == LnConstants.OPC_LONG_ACK) &&
                (m.getElement(1) == 0x6f) &&
                (m.getElement(2) == 0x7f) &&
                (cmdStnOpSwState == cmdStnOpSwStateType.WRITE)) {
            csOpSwAccessTimer.stop();
            cmdStnOpSwState = cmdStnOpSwStateType.HAS_STATE;
            value = extractCmdStnOpSw(lastCmdStationOpSwMessage, cmdStnOpSwNum);
            ProgListener temp = p;
            p = null;
            if (temp != null) {
                temp.programmingOpReply(value?1:0, ProgListener.OK);
            }
        } else if ((m.getOpCode() == 0xe6) &&
                (m.getElement(1) == 0x15) &&
                (m.getElement(2) == 0x00) &&
                (m.getElement(3) == 0x7f)) {
            if (cmdStnOpSwState == cmdStnOpSwStateType.QUERY_ENHANCED)  {
                log.debug("got enhanced slot 127 read data in response to OpSw query");
                csOpSwAccessTimer.stop();
                cmdStnOpSwState = cmdStnOpSwStateType.HAS_STATE;
                lastCmdStationOpSwMessage = m;  // save a copy of the LocoNet message
                csOpSwsAreValid = true;
                csOpSwValidTimer.start();   // start the "valid data" timer
                if (doingWrite == true) {
                    log.debug("now can finish the write by updating the correct bit...");
                    finishTheWrite();
                } else {
                    value = extractCmdStnOpSw(m, cmdStnOpSwNum);
                    log.debug("now can return the extracted OpSw{} read data ({}) to the programmer", cmdStnOpSwNum, value);
                    ProgListener temp = p;
                    p = null;
                    if (temp != null) {
                        log.debug("Returning data");
                        temp.programmingOpReply(value?1:0, ProgListener.OK);
                    } else {
                        log.debug("no programmer to return the data to.");
                    }
                }
            } else if (cmdStnOpSwState == cmdStnOpSwStateType.QUERY_ENHANCED_BEFORE_WRITE) {
                log.debug("hve received enhanced OpSw query before a write; now can process the data modification");
                csOpSwAccessTimer.stop();
                LocoNetMessage m2 = updateOpSwVal(m, cmdStnOpSwNum,
                        cmdStnOpSwVal);
                cmdStnOpSwState =
                        cmdStnOpSwStateType.WRITE;
                log.debug("performing enhanced opsw write: {}",m2.toString());
                memo.getLnTrafficController().sendLocoNetMessage(m2);
                csOpSwAccessTimer.start();
            }
        }
    }

    public void readCmdStationOpSw(int cv) {
        log.debug("readCmdStationOpSw: state is {}, have valid is ",cmdStnOpSwState, csOpSwsAreValid?"true":"false");
        if ((cmdStnOpSwState == cmdStnOpSwStateType.HAS_STATE) &&
                (csOpSwsAreValid == true)) {
            // can re-use previous state - it has not "expired" due to time since read.
            log.debug("readCmdStationOpSw: returning state from previously-stored state for OpSw{}", cv);
            returnCmdStationOpSwVal(cv);
            return;
        } else if ((cmdStnOpSwState == cmdStnOpSwStateType.IDLE) ||
                (cmdStnOpSwState == cmdStnOpSwStateType.HAS_STATE)) {
            // do not have valid data or old data has "expired" due to time since read.
            log.debug("readCmdStationOpSw: attempting to read some CVs");
            updateCmdStnOpSw(cv,false);
            return;
        } else {
            log.debug("readCmdStationOpSw: aborting - cmdStnOpSwState is odd: {}", cmdStnOpSwState);
            ProgListener temp = p;
            p = null;
            temp.programmingOpReply(0, ProgListener.ProgrammerBusy);
        }
    }

    public void returnCmdStationOpSwVal(int cmdStnOpSwNum) {
        boolean returnVal = extractCmdStnOpSw(lastCmdStationOpSwMessage, cmdStnOpSwNum);
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
        log.debug("updateCmdStnOpSw: attempting to query the OpSws when state = ");
        cmdStnOpSwState = cmdStnOpSwStateType.QUERY;
        cmdStnOpSwNum = opSwNum;
        cmdStnOpSwVal = val;
        int[] contents = {LnConstants.OPC_RQ_SL_DATA, 0x7F, 0x0, 0x0};
        memo.getLnTrafficController().sendLocoNetMessage(new LocoNetMessage(contents));
        csOpSwAccessTimer.start();

        return true;
    }

    public boolean extractCmdStnOpSw(LocoNetMessage m, int cmdStnOpSwNum) {
        if (((m.getOpCode() == 0xE7) || (m.getOpCode() == 0xEF)) &&
                ((m.getNumDataElements() == 14)) &&
                (cmdStnOpSwNum >= 1) &&
                (cmdStnOpSwNum <= 64)) {
            log.debug("extractCmdStnOpSw: standard OpSw{} from {}",cmdStnOpSwNum, m);
            int messageByte;
            messageByte = 2 + ((cmdStnOpSwNum+7) >> 3);
            if (cmdStnOpSwNum > 32) {
                messageByte ++;
            }
            int val = m.getElement(messageByte);
            val = (val >> ((cmdStnOpSwNum - 1) & 0x7)) & 0x1;
            return (val == 1);
        } else if (((m.getOpCode() == 0xE6) || (m.getOpCode() == 0xEE)) &&
                (m.getNumDataElements() == 21) &&
                (cmdStnOpSwNum >= 1) &&
                (cmdStnOpSwNum <=112)) {
            log.debug("extractCmdStnOpSw: extended OpSw{} from {}",cmdStnOpSwNum, m);
            int messageByte;
            messageByte = 3 + ((cmdStnOpSwNum+7) >> 3);
            int val = m.getElement(messageByte);
            val = (val >> ((cmdStnOpSwNum - 1) & 0x7)) & 0x1;
            return (val == 1);
        }
        log.debug("extractCmdStnOpSw: failure for OpSw{} from {}",cmdStnOpSwNum, m);

        csOpSwAccessTimer.stop();
        csOpSwValidTimer.stop();
        ProgListener temp = p;
        p = null;
        if (temp != null) {
            temp.programmingOpReply(0, ProgListener.UnknownError);
        }
        cmdStnOpSwState = cmdStnOpSwStateType.IDLE;
        log.warn("illegal command station opsw access: {}. {}, {}.",
                m.getOpCode(),
                m.getNumDataElements(),
                cmdStnOpSwNum);
        return false;
    }

    public LocoNetMessage updateOpSwVal(LocoNetMessage m, int cmdStnOpSwNum, boolean cmdStnOpSwVal) {
        int messageByte;
        log.debug("updateOpSwVal: OpSw{} = {}", cmdStnOpSwNum, cmdStnOpSwVal);
        if (((m.getOpCode() == 0xE7) || (m.getOpCode() == 0xEF)) &&
                (m.getElement(1) == 14) &&
                (cmdStnOpSwNum >= 1) &&
                (cmdStnOpSwNum <= 64)) {
            messageByte = 2 + ((cmdStnOpSwNum+7) >> 3);
            if (cmdStnOpSwNum > 32) {
                messageByte ++;
            }
            int val = m.getElement(messageByte);
            log.debug("updateOpSwVal: working with messageByte {}, value is {}", messageByte, val);
            if (((cmdStnOpSwNum -1) & 0x07) == 7) {
                log.warn("Cannot program OpSw{} account LocoNet encoding limitations.",cmdStnOpSwNum);
                ProgListener temp = p;
                p = null;
                if (temp != null) {
                    temp.programmingOpReply(0, ProgListener.UnknownError);
                }
                return new LocoNetMessage(new int[] {LnConstants.OPC_GPBUSY, 0x0});
            }
            val &= ~(1 << ((cmdStnOpSwNum - 1) & 0x7));
            if (cmdStnOpSwVal == true) {
                val |= 1 << ((cmdStnOpSwNum - 1) & 0x7);
            }
            LocoNetMessage m2 = m;
            log.debug("updateOpSwVal: new value for messageByte{} is {}", messageByte, val);
            m2.setElement(messageByte, val);
            return m2;
        }
        else if (((m.getOpCode() == 0xE6) || (m.getOpCode() == 0xEE)) &&
                (m.getElement(1) == 21) &&
                (cmdStnOpSwNum >= 1) &&
                (cmdStnOpSwNum <= 112)) {
            messageByte = 3 + ((cmdStnOpSwNum+7) >> 3);
            int val = m.getElement(messageByte);
            log.debug("updateOpSwVal: working with messageByte {}, value is {}", messageByte, val);
            if (((cmdStnOpSwNum -1) & 0x07) == 7) {
                log.warn("Cannot program OpSw{} account LocoNet encoding limitations.",cmdStnOpSwNum);
                ProgListener temp = p;
                p = null;
                if (temp != null) {
                    temp.programmingOpReply(0, ProgListener.UnknownError);
                }
                return new LocoNetMessage(new int[] {LnConstants.OPC_GPBUSY, 0x0});
            }
            val &= ~(1 << ((cmdStnOpSwNum - 1) & 0x7));
            if (cmdStnOpSwVal == true) {
                val |= 1 << ((cmdStnOpSwNum - 1) & 0x7);
            }
            LocoNetMessage m2 = m;
            log.debug("updateOpSwVal: new value for messageByte{} is {}", messageByte, val);
            m2.setElement(messageByte, val);
            return m2;
        }
        else {
            log.warn("Cannot program OpSw{}: {} {}.",cmdStnOpSwNum, m.getOpCode(), m.getElement(1));
            ProgListener temp = p;
            p = null;
            if (temp != null) {
                temp.programmingOpReply(0, ProgListener.UnknownError);
            }
            return new LocoNetMessage(new int[] {LnConstants.OPC_GPBUSY, 0x0});
        }
    }

    private void finishTheWrite() {
        cmdStnOpSwState = cmdStnOpSwStateType.WRITE;
        LocoNetMessage m2 = updateOpSwVal(lastCmdStationOpSwMessage,
                cmdStnOpSwNum,
                cmdStnOpSwVal);
        log.debug("gonna send message {}", m2.toString());
        m2.setOpCode(LnConstants.OPC_WR_SL_DATA);
        log.debug("sending LocoNet cmd stn opsw write message {}", m2.toString());
        memo.getLnTrafficController().sendLocoNetMessage(m2);
        lastCmdStationOpSwMessage = m2;
        csOpSwAccessTimer.start();
    }

    private enum cmdStnOpSwStateType {
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
            csOpSwValidTimer = new Timer(10000, (ActionEvent e) -> {
                log.debug("csOpSwValidTimer timed out; invalidating held data!");
                csOpSwsAreValid = false;
                cmdStnOpSwState = cmdStnOpSwStateType.IDLE;
                });
       csOpSwValidTimer.setRepeats(false);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(csOpSwAccess.class);
}
