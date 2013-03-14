// NceMonBinary.java

package jmri.jmrix.nce.ncemon;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.util.StringUtil;

/**
 * A utility class for formatting NCE binary command and replies into human-readable text.
 * The text for the display comes from NCE's Bincmds.txt published November 2007 and is
 * used with NCE's permission.
 * 
 * @author		Daniel Boudreau Copyright (C) 2012
 * @version 	$Revision: 19264 $
 */

public class NceMonBinary {
	
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.ncemon.NceMonBinaryBundle");
	
	private static final String NEW_LINE = "\n";

	private int replyType = REPLY_UNKNOWN;
	
	private static final int REPLY_UNKNOWN = 0;
	private static final int REPLY_STANDARD = 1;
	private static final int REPLY_DATA = 2;
	private static final int REPLY_ENTER_PROGRAMMING_MODE = 3;
	
	// The standard replies
	private static final int REPLY_ZERO = 0;
	private static final int REPLY_ONE = 1;
	private static final int REPLY_TWO = 2;
	private static final int REPLY_THREE = 3;
	private static final int REPLY_FOUR = 4;
	private static final int REPLY_OK = 0x21;		// !
	
	public String displayMessage(NceMessage m) {
		return parseMessage(m) + NEW_LINE; 
	}
	
	private String parseMessage(NceMessage m){
		// first check for messages that have a standard reply
		replyType = REPLY_STANDARD;
		switch (m.getOpCode() & 0xFF) {
		case (NceMessage.NOP_CMD):
			return rb.getString("NOP_CMD");
		case (NceBinaryCommand.STOP_CLOCK_CMD):
			return rb.getString("STOP_CLOCK_CMD");
		case (NceBinaryCommand.START_CLOCK_CMD):
			return rb.getString("START_CLOCK_CMD");
		case (NceBinaryCommand.SET_CLOCK_CMD): {
			if (m.getNumDataElements() == 3)
				return MessageFormat.format(rb.getString("SET_CLOCK_CMD"),
						new Object[] { m.getElement(1), m.getElement(2) })
						+ NEW_LINE;
			break;
		}
		case (NceBinaryCommand.CLOCK_1224_CMD): {
			if (m.getNumDataElements() == 2) {
				String hr = "12";
				if (m.getElement(1) == 1)
					hr = "24";
				return MessageFormat.format(rb.getString("CLOCK_1224_CMD"),
						new Object[] { hr });
			}
			break;
		}
		case (NceBinaryCommand.CLOCK_RATIO_CMD): {
			if (m.getNumDataElements() == 2)
				return MessageFormat.format(rb.getString("CLOCK_RATIO_CMD"),
						new Object[] { m.getElement(1) });
			break;
		}
		case (NceMessage.ENABLE_MAIN_CMD):
			return rb.getString("ENABLE_MAIN_CMD");
		case (NceMessage.KILL_MAIN_CMD):
			return rb.getString("KILL_MAIN_CMD");
		case (NceBinaryCommand.WRITEn_CMD):{
			if (m.getNumDataElements() == 20)
				return MessageFormat.format(rb.getString("WRITEn_CMD"),
						new Object[] { m.getElement(3), getAddress(m), getDataBytes(m, 4, 16)});
			break;
		}
		// Send n bytes commands 0x93 - 0x96
		case (NceMessage.SENDn_BYTES_CMD + 3):{
			if (m.getNumDataElements() == 5)
				return MessageFormat.format(rb.getString("SENDn_BYTES_CMD"),
						new Object[] {"3", m.getElement(1), getDataBytes(m, 2, 3)});
			break;
		}
		case (NceMessage.SENDn_BYTES_CMD + 4):{
			if (m.getNumDataElements() == 6)
				return MessageFormat.format(rb.getString("SENDn_BYTES_CMD"),
						new Object[] {"4", m.getElement(1), getDataBytes(m, 2, 4)});
			break;
		}
		case (NceMessage.SENDn_BYTES_CMD + 5):{
			if (m.getNumDataElements() == 7)
				return MessageFormat.format(rb.getString("SENDn_BYTES_CMD"),
						new Object[] {"5", m.getElement(1), getDataBytes(m, 2, 5)});
			break;
		}
		case (NceMessage.SENDn_BYTES_CMD + 6):{
			if (m.getNumDataElements() == 8)
				return MessageFormat.format(rb.getString("SENDn_BYTES_CMD"),
						new Object[] {"6", m.getElement(1), getDataBytes(m, 2, 6)});
			break;
		}

		case (NceBinaryCommand.WRITE1_CMD):{
			if (m.getNumDataElements() == 4)
				return MessageFormat.format(rb.getString("WRITE1_CMD"),
						new Object[] {getAddress(m), getDataBytes(m, 3, 1)});
			break;
		}
		case (NceBinaryCommand.WRITE2_CMD):{
			if (m.getNumDataElements() == 5)
				return MessageFormat.format(rb.getString("WRITE2_CMD"),
						new Object[] {getAddress(m), getDataBytes(m, 3, 2)});
			break;	
		}
		case (NceBinaryCommand.WRITE4_CMD):{
			if (m.getNumDataElements() == 7)
				return MessageFormat.format(rb.getString("WRITE4_CMD"),
						new Object[] {getAddress(m), getDataBytes(m, 3, 4)});
			break;	
		}
		case (NceBinaryCommand.WRITE8_CMD):{
			if (m.getNumDataElements() == 11)
				return MessageFormat.format(rb.getString("WRITE8_CMD"),
						new Object[] {getAddress(m), getDataBytes(m, 3, 8)});
			break;	
		}
		case (NceBinaryCommand.MACRO_CMD):{
			if (m.getNumDataElements() == 2)
				return MessageFormat.format(rb.getString("MACRO_CMD"),
						new Object[] {m.getElement(1)});
			break;	
		}
		case (NceMessage.ENTER_PROG_CMD):{
			replyType = REPLY_ENTER_PROGRAMMING_MODE;
			return rb.getString("ENTER_PROG_CMD");
		}
		case (NceMessage.EXIT_PROG_CMD):
			return rb.getString("EXIT_PROG_CMD");
		case (NceMessage.WRITE_PAGED_CV_CMD):{
			if (m.getNumDataElements() == 4)
				return MessageFormat.format(rb.getString("WRITE_PAGED_CV_CMD"),
						new Object[] {getNumber(m), getDataBytes(m, 3, 1)});
			break;
		}
		case (NceBinaryCommand.LOCO_CMD):{
			if (m.getNumDataElements() == 5){
				// byte three is the Op_1
				switch(m.getElement(3)){
				case (1):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_01"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (2):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_02"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (3):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_03"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (4):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_04"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (5):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_05"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (6):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_06"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (7):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_07"),
							new Object[] {getLocoAddress(m), m.getElement(4), getFunctionNumber(m)});
				case (8):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_08"),
							new Object[] {getLocoAddress(m), m.getElement(4), getFunctionNumber(m)});
				case (9):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_09"),
							new Object[] {getLocoAddress(m), m.getElement(4), getFunctionNumber(m)});
				case (0x0A):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_0A"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x0b):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_0B"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x0C):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_0C"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x0D):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_0D"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x0E):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_0E"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x0F):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_0F"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x10):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_10"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x11):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_11"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x12):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_12"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x15):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_15"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x16):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_16"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				case (0x17):
					return MessageFormat.format(rb.getString("LOCO_CMD_Op1_17"),
							new Object[] {getLocoAddress(m), m.getElement(4)});
				}
			}
			break;
		}
		// Queue commands 0xA3 - 0xA5
		case (NceMessage.QUEUEn_BYTES_CMD + 3):{
			if (m.getNumDataElements() == 5)
				return MessageFormat.format(rb.getString("QUEUEn_BYTES_CMD"),
						new Object[] {"3", m.getElement(1), getDataBytes(m, 2, 3)});
			break;
		}
		case (NceMessage.QUEUEn_BYTES_CMD + 4):{
			if (m.getNumDataElements() == 6)
				return MessageFormat.format(rb.getString("QUEUEn_BYTES_CMD"),
						new Object[] {"4", m.getElement(1), getDataBytes(m, 2, 4)});
			break;
		}
		case (NceMessage.QUEUEn_BYTES_CMD + 5):{
			if (m.getNumDataElements() == 7)
				return MessageFormat.format(rb.getString("QUEUEn_BYTES_CMD"),
						new Object[] {"5", m.getElement(1), getDataBytes(m, 2, 5)});
			break;
		}
		case (NceMessage.WRITE_REG_CMD):{
			if (m.getNumDataElements() == 3)
				return MessageFormat.format(rb.getString("WRITE_REG_CMD"),
						new Object[] {m.getElement(1), getDataBytes(m, 2, 1)});
			break;
		}
		case (NceMessage.WRITE_DIR_CV_CMD):{
			if (m.getNumDataElements() == 4)
				return MessageFormat.format(rb.getString("WRITE_DIR_CV_CMD"),
						new Object[] {getNumber(m), getDataBytes(m, 3, 1)});
			break;
		}
		case (NceBinaryCommand.ACC_CMD):{
			if (m.getNumDataElements() == 5){
				// byte three is the Op_1
				switch(m.getElement(3)){
				case (1):
					return MessageFormat.format(rb.getString("ACC_CMD_Op1_01"),
							new Object[] {m.getElement(4)});
				case (3):
					return MessageFormat.format(rb.getString("ACC_CMD_Op1_03"),
							new Object[] {getNumber(m)});
				case (4):
					return MessageFormat.format(rb.getString("ACC_CMD_Op1_04"),
							new Object[] {getNumber(m)});
				case (5):
					return MessageFormat.format(rb.getString("ACC_CMD_Op1_05"),
							new Object[] {getNumber(m), m.getElement(4)});
				}
			}
			break;	
		}
		}	
		// 2nd pass, check for messages that have a data reply
		replyType = REPLY_DATA;
		switch (m.getOpCode() & 0xFF) {
		case (NceBinaryCommand.READ_CLOCK_CMD):
			return rb.getString("READ_CLOCK_CMD");
		case (NceBinaryCommand.READ_AUI4_CMD):{
			if (m.getNumDataElements() == 2)
				return MessageFormat.format(rb.getString("READ_AUI4_CMD"),
						new Object[] {m.getElement(1)});
			break;
		}
		case (NceBinaryCommand.DUMMY_CMD):
			return rb.getString("DUMMY_CMD");
		case (NceBinaryCommand.READ16_CMD):{
			if (m.getNumDataElements() == 3)
				return MessageFormat.format(rb.getString("READ16_CMD"),
						new Object[] {getAddress(m)});
			break;
		}
		case (NceBinaryCommand.READ_AUI2_CMD):{
			if (m.getNumDataElements() == 2)
				return MessageFormat.format(rb.getString("READ_AUI2_CMD"),
						new Object[] {m.getElement(1)});
			break;
		}
		case (NceBinaryCommand.READ1_CMD):{
			if (m.getNumDataElements() == 3)
				return MessageFormat.format(rb.getString("READ1_CMD"),
						new Object[] {getAddress(m)});
			break;
		}
		case (NceMessage.READ_PAGED_CV_CMD):{
			if (m.getNumDataElements() == 3)
				return MessageFormat.format(rb.getString("READ_PAGED_CV_CMD"),
						new Object[] {getNumber(m)});
			break;
		}
		case (NceMessage.READ_REG_CMD):{
			if (m.getNumDataElements() == 2)
				return MessageFormat.format(rb.getString("READ_REG_CMD"),
						new Object[] {m.getElement(1)});
			break;
		}
		case (NceMessage.READ_DIR_CV_CMD):{
			if (m.getNumDataElements() == 3)
				return MessageFormat.format(rb.getString("READ_DIR_CV_CMD"),
						new Object[] {getNumber(m)});
			break;
		}
		case (NceBinaryCommand.SW_REV_CMD):
			return rb.getString("SW_REV_CMD");
		}		
		// this is one we don't know about or haven't coded it up 
		replyType = REPLY_UNKNOWN;
		return MessageFormat.format(rb.getString("BIN_CMD"), new Object[] {m.toString()});
	}
	
	private String getAddress(NceMessage m){
		return StringUtil.twoHexFromInt(m.getElement(1))+StringUtil.twoHexFromInt(m.getElement(2));
	}
	
	private String getDataBytes(NceMessage m, int start, int number){
		StringBuffer sb = new StringBuffer(" ");
		for (int i=start; i<start+number; i++){
			sb.append(StringUtil.twoHexFromInt(m.getElement(i)) + " ");
		}
		return sb.toString();		
	}
	
	private String getNumber(NceMessage m){
		return Integer.toString(m.getElement(1)*256 + m.getElement(2));
	}
	
	private String getLocoAddress(NceMessage m){
		// show address type
		String appendix = " (short)";
		if ((m.getElement(1) & 0xE0) > 0)
			appendix = " (long)";
		return Integer.toString((m.getElement(1) & 0x3F)*256 + m.getElement(2)) + appendix;
	}
	
	private String getFunctionNumber(NceMessage m) {
		// byte three is the Op_1
		switch (m.getElement(3)) {
		case (7): {
			StringBuffer buf = new StringBuffer();
			if ((m.getElement(4) & 0x10) > 0)
				buf.append(rb.getString("F0_ON")+", ");
			else
				buf.append(rb.getString("F0_OFF")+", ");
			if ((m.getElement(4) & 0x01) > 0)
				buf.append(rb.getString("F1_ON")+", ");
			else
				buf.append(rb.getString("F1_OFF")+", ");
			if ((m.getElement(4) & 0x02) > 0)
				buf.append(rb.getString("F2_ON")+", ");
			else
				buf.append(rb.getString("F2_OFF")+", ");
			if ((m.getElement(4) & 0x04) > 0)
				buf.append(rb.getString("F3_ON")+", ");
			else
				buf.append(rb.getString("F3_OFF")+", ");
			if ((m.getElement(4) & 0x08) > 0)
				buf.append(rb.getString("F4_ON"));
			else
				buf.append(rb.getString("F4_OFF"));
			return buf.toString();
		}
		case (8): {
			StringBuffer buf = new StringBuffer();
			if ((m.getElement(4) & 0x01) > 0)
				buf.append(rb.getString("F5_ON")+", ");
			else
				buf.append(rb.getString("F5_OFF")+", ");
			if ((m.getElement(4) & 0x02) > 0)
				buf.append(rb.getString("F6_ON")+", ");
			else
				buf.append(rb.getString("F6_OFF")+", ");
			if ((m.getElement(4) & 0x04) > 0)
				buf.append(rb.getString("F7_ON")+", ");
			else
				buf.append(rb.getString("F7_OFF")+", ");
			if ((m.getElement(4) & 0x08) > 0)
				buf.append(rb.getString("F8_ON"));
			else
				buf.append(rb.getString("F8_OFF"));
			return buf.toString();
		}
		case (9): {
			StringBuffer buf = new StringBuffer();
			if ((m.getElement(4) & 0x01) > 0)
				buf.append(rb.getString("F9_ON")+", ");
			else
				buf.append(rb.getString("F9_OFF")+", ");
			if ((m.getElement(4) & 0x02) > 0)
				buf.append(rb.getString("F10_ON")+", ");
			else
				buf.append(rb.getString("F10_OFF")+", ");
			if ((m.getElement(4) & 0x04) > 0)
				buf.append(rb.getString("F11_ON")+", ");
			else
				buf.append(rb.getString("F11_OFF")+", ");
			if ((m.getElement(4) & 0x08) > 0)
				buf.append(rb.getString("F12_ON"));
			else
				buf.append(rb.getString("F12_OFF"));
			return buf.toString();
		}
			default: return("Error");
		}
	}
	
	public String displayReply(NceReply r){
		return parseReply(r) + NEW_LINE;
	}

	
	private String parseReply(NceReply r){
		switch(replyType){
		case(REPLY_STANDARD):{
			/* standard reply is a single byte
			 * Errors returned: '0'= command not supported
			 * '1'= loco/accy/signal address out of range
			 * '2'= cab address or op code out of range
			 * '3'= CV address or data out of range
			 * '4'= byte count out of range
			 * '!'= command completed successfully
			 */
			if (r.getNumDataElements() == 1){
				switch(r.getOpCode() & 0xFF){
				case(REPLY_ZERO): return rb.getString("NceReplyZero");
				case(REPLY_ONE): return rb.getString("NceReplyOne");
				case(REPLY_TWO): return rb.getString("NceReplyTwo");
				case(REPLY_THREE): return rb.getString("NceReplyThree");
				case(REPLY_FOUR): return rb.getString("NceReplyFour");
				case(REPLY_OK): return rb.getString("NceReplyOK");
				}
			}
			break;
		}
		case(REPLY_ENTER_PROGRAMMING_MODE):{
			/* enter programming mode reply is a single byte
			 * '3'= short circuit
			 * '!'= command completed successfully
			 */
			if (r.getNumDataElements() == 1){
				switch(r.getOpCode() & 0xFF){
				case(REPLY_THREE): return rb.getString("NceReplyThreeProg");
				case(REPLY_OK): return rb.getString("NceReplyOK");
				}
			}
		}
		}
		return MessageFormat.format(rb.getString("NceReply"),new Object[]{r.toString()});
	}
}
