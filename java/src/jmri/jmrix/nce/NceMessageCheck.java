// NceMessageCheck.java

package jmri.jmrix.nce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.JmriException;

/**
 * Checks that a NCE message is valid for the connection type.
 * Throws an exception if message isn't appropriate for the
 * connection type.
 * <P>
 * @author      Dan Boudreau   Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class NceMessageCheck {
	
	public static void checkMessage(NceSystemConnectionMemo memo, NceMessage m) throws JmriException{
        if(m!=null){
            switch(m.getOpCode()){
            /* NCE USB can now address the full range of accessory addresses dboudreau 1/18/2012
            case NceBinaryCommand.ACC_CMD: checkACC_CMD(memo, m);
            break;*/
            case NceBinaryCommand.READ1_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.READ16_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.WRITEn_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.WRITE1_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.WRITE2_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.WRITE4_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.WRITE8_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.STOP_CLOCK_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.START_CLOCK_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.SET_CLOCK_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.CLOCK_1224_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.CLOCK_RATIO_CMD: checkAccessory_CMD(memo, m);
            break;
            case NceBinaryCommand.OPS_PROG_LOCO_CMD: checkOPS_PROG_CMD(memo, m);
            break;
            case NceBinaryCommand.OPS_PROG_ACCY_CMD: checkOPS_PROG_CMD(memo, m);
            }
        }
	}
	
	/* NCE USB no longer has an accessory address restriction dboudreau 1/18/2012
	private static void checkACC_CMD(NceSystemConnectionMemo memo, NceMessage m) throws JmriException{
		// USB connected to PowerCab or SB3 can only access addresses up to 250
		int number = m.getElement(1);			// high byte address
		number = number*256 + m.getElement(2);	// low byte address
		if (number > 250 && 
				(memo.getNceUSB() == NceTrafficController.USB_SYSTEM_POWERCAB 
						|| (memo.getNceUSB() == NceTrafficController.USB_SYSTEM_SB3))){
			log.debug("invalid NCE accessory address for USB " + number);
			throw new JmriException("invalid NCE accessory address for USB " + number);
		}
	}
	*/
	
	private static void checkAccessory_CMD(NceSystemConnectionMemo memo, NceMessage m) throws JmriException{
	   	if (memo.getNceUSB() != NceTrafficController.USB_SYSTEM_NONE){
    		log.debug("attempt to send unsupported binary command to NCE USB");
    		throw new JmriException("attempt to send unsupported binary command to NCE USB");
    	}
	}
	
	private static void checkOPS_PROG_CMD(NceSystemConnectionMemo memo, NceMessage m) throws JmriException{
		// ONLY USB connected to PowerCab or SB3 can send this message
		if (memo.getNceUSB() == NceTrafficController.USB_SYSTEM_POWERCAB
				|| memo.getNceUSB() == NceTrafficController.USB_SYSTEM_SB3)
			return;
		log.debug("attempt to send unsupported binary command");
		throw new JmriException("attempt to send unsupported binary command");

	}
	
	static Logger log = LoggerFactory.getLogger(NceMessageCheck.class.getName());
}

/* @(#)NceMessageCheck.java */
