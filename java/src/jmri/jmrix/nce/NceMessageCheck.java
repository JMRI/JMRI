package jmri.jmrix.nce;

import jmri.JmriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks that a NCE message is valid for the connection type. Throws an
 * exception if message isn't appropriate for the connection type.
 *
 * @author Dan Boudreau Copyright (C) 2010
 * @author ken cameron Copyright (C) 2013
  *
 */
public class NceMessageCheck {

    public static void checkMessage(NceSystemConnectionMemo memo, NceMessage m) throws JmriException {
        if (m != null) {
            switch (m.getOpCode()) {
                /* NCE USB can now address the full range of accessory addresses dboudreau 1/18/2012
                 case NceMessage.ACC_CMD: checkACC_CMD(memo, m);
                 break;*/
                case NceMessage.READ1_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.READ16_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.WRITE_N_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.WRITE1_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.WRITE2_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.WRITE4_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.WRITE8_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.STOP_CLOCK_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.START_CLOCK_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.SET_CLOCK_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.CLOCK_1224_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.CLOCK_RATIO_CMD:
                    checkSerial_CMD(memo, m);
                    break;
                case NceMessage.OPS_PROG_LOCO_CMD:
                    checkOPS_PROG_CMD(memo, m);
                    break;
                case NceMessage.OPS_PROG_ACCY_CMD:
                    checkOPS_PROG_CMD(memo, m);
                    break;
                case NceMessage.USB_MEM_POINTER_CMD:
                    checkUsbMem_CMD(memo, m);
                    break;
                case NceMessage.USB_MEM_READ_CMD:
                    checkUsbMem_CMD(memo, m);
                    break;
                case NceMessage.USB_MEM_WRITE_CMD:
                    checkUsbMem_CMD(memo, m);
                    break;
                default:
                    break;
            }
        }
    }

    /* NCE USB no longer has an accessory address restriction dboudreau 1/18/2012
     private static void checkACC_CMD(NceSystemConnectionMemo memo, NceMessage m) throws JmriException{
     // USB connected to PowerCab or SB3 can only access addresses up to 250
     int number = m.getElement(1);   // high byte address
     number = number*256 + m.getElement(2); // low byte address
     if (number > 250 && 
     (memo.getNceUSB() == NceTrafficController.USB_SYSTEM_POWERCAB 
     || (memo.getNceUSB() == NceTrafficController.USB_SYSTEM_SB3))){
     log.debug("invalid NCE accessory address for USB " + number);
     throw new JmriException("invalid NCE accessory address for USB " + number);
     }
     }
     */
    private static void checkSerial_CMD(NceSystemConnectionMemo memo, NceMessage m) throws JmriException {
        if (memo.getNceUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            String txt = "attempt to send unsupported binary command to NCE USB: " + Integer.toHexString(m.getOpCode()).toUpperCase();
            log.debug(txt);
            throw new JmriException(txt);
        }
    }

    private static void checkOPS_PROG_CMD(NceSystemConnectionMemo memo, NceMessage m) throws JmriException {
        // ONLY USB connected to PowerCab or SB3 can send this message
        if (memo.getNceUsbSystem() != NceTrafficController.USB_SYSTEM_NONE
                && (memo.getNceCmdGroups() & NceTrafficController.CMDS_OPS_PGM) != NceTrafficController.CMDS_NONE) {
            return;
        }
        String txt = "attempt to send unsupported binary command: " + Integer.toHexString(m.getOpCode()).toUpperCase();
        log.debug(txt);
        throw new JmriException(txt);

    }

    private static void checkUsbMem_CMD(NceSystemConnectionMemo memo, NceMessage m) throws JmriException {
        // ONLY 7.* USB connected to >-1.65  can send this message
        if (memo.getNceUsbSystem() != NceTrafficController.USB_SYSTEM_NONE
                && (memo.getNceCmdGroups() & NceTrafficController.CMDS_MEM) != NceTrafficController.CMDS_NONE) {
            return;
        }
        String txt = "attempt to send unsupported binary command: " + Integer.toHexString(m.getOpCode()).toUpperCase();
        log.debug(txt);
        throw new JmriException(txt);
    }

    private final static Logger log = LoggerFactory.getLogger(NceMessageCheck.class);
}


