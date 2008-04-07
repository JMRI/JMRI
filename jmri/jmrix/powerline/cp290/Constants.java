// Constants.java

package jmri.jmrix.powerline.cp290;


/**
 * Constants and functions specific to the CP290 interface
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.5 $
 */
public class Constants {
	
	/**
	 * Translate Function to Text
	 */
	public static String commandToText(int cmd, int level) {
		String cmdText = "";
		switch (cmd) {
		case 0x02:
			cmdText = "ON";
			break;
		case 0x03:
			cmdText = "OFF";
			break;
		case 0x04:
			cmdText = "recvDIM";
			cmdText = cmdText + " ";
			break;
		case 0x05:
			cmdText = "sendDIM";
			int v2 = (level * 100) / 16;
			cmdText = cmdText + " " + v2 + "%";
			break;
		default:
			cmdText = "Unk Cmd: " + cmd;
			break;
		}
		return(cmdText);
	}
	
	/**
	 * Translate House Code to text
	 */
	public static String houseCodeToText(int hC) {
		String hCode = "";
		switch (hC) {
		case 0x06:
			hCode = "A";
			break;
		case 0x0E:
			hCode = "B";
			break;
		case 0x02:
			hCode = "C";
			break;
		case 0x0A:
			hCode = "D";
			break;
		case 0x01:
			hCode = "E";
			break;
		case 0x09:
			hCode = "F";
			break;
		case 0x05:
			hCode = "G";
			break;
		case 0x0D:
			hCode = "H";
			break;
		case 0x07:
			hCode = "I";
			break;
		case 0x0F:
			hCode = "J";
			break;
		case 0x03:
			hCode = "K";
			break;
		case 0x0B:
			hCode = "L";
			break;
		case 0x00:
			hCode = "M";
			break;
		case 0x08:
			hCode = "N";
			break;
		case 0x04:
			hCode = "O";
			break;
		case 0x0C:
			hCode = "P";
			break;
		default:
			hCode = "Unk hC:" + hC;
			break;
		}
	    return hCode;
	}

	/**
	 * Translate Device Bits to Text
	 */
	public static String deviceToText(int hByte, int lByte) {
		int mask = 0x01;
		int x = lByte;
		String dev = "";
		for (int i = 8; i > 0; i--) {
			if ((x & mask) != 0) {
				dev = dev + " " + i;
			}
			mask = mask << 1;
		}
		mask = 0x01;
		x = hByte;
		for (int i = 16; i > 8; i--) {
			if ((x & mask) != 0) {
				dev = dev + " " + i;
			}
			mask = mask << 1;
		}
		return(dev);
	}
	
	/**
	 * Translate status to text
	 */
	public static String statusToText(int s) {
		String stat = "";
		switch (s) {
		case 0:
			stat = "Interface Powered Off";
			break;
		case 1:
			stat = "Cmd Ok";
			break;
		default:
			stat = "Unk Status: " + s;
			break;
		}
		return(stat);
	}
	
    /**
     * Format a message nicely
     */
    public static String toMonitorString(jmri.jmrix.Message m) {
        // check for valid length
    	String val = "???";
    	int len = m.getNumDataElements();
    	boolean goodSync = true;
    	boolean goodCheckSum = true;
    	int sum = 0;
    	String cmd;
    	String stat;
    	String hCode;
    	String bCode;
    	String dev;
        switch (len) {
        case 7:
        	for (int i = 0; i < 6; i++) {
        		if ((m.getElement(i) & 0xFF) != 0xFF) {
        			goodSync = false;
        		}
        	}
        	val = statusToText(m.getElement(6));
        	break;
        case 12:
        	for (int i = 0; i < 6; i++) {
        		if ((m.getElement(i) & 0xFF) != 0xFF) {
        			goodSync = false;
        		}
        	}
        	for (int i = 7; i < 11; i++) {
        		sum = (sum + (m.getElement(i) &0xFF)) & 0xFF;
        	}
        	stat = statusToText(m.getElement(6));
        	cmd = commandToText(m.getElement(7) & 0x0F, -1);
        	hCode = houseCodeToText((m.getElement(7) >> 4) & 0x0F);
        	dev = deviceToText(m.getElement(8), m.getElement(9));
        	bCode = houseCodeToText((m.getElement(10) >> 4) & 0x0F);
        	if (sum != (m.getElement(11) & 0xFF)) {
        		goodCheckSum = false;
        	}
        	val = "Cmd Echo: " + cmd + " stat: " + stat + " House: " + hCode + " Device:" + dev + " base: " + bCode;
        	if (!goodSync) {
        		val = val + " BAD SYNC";
        	}
        	if (!goodCheckSum) {
        		val = val + " BAD CHECKSUM: " + (m.getElement(11) & 0xFF) + " vs " + sum;
        	}
        	break;
        case 22:
        	for (int i = 0; i < 16; i++) {
        		if ((m.getElement(i) & 0xFF) != 0xFF) {
        			goodSync = false;
        		}
        	}
        	for (int i = 17; i < 21; i++) {
        		sum = (sum + (m.getElement(i) & 0xFF)) & 0xFF;
        	}
        	cmd = commandToText((m.getElement(17) & 0x0F), ((m.getElement(17) & 0xF0) >> 4));
        	hCode = houseCodeToText((m.getElement(18) >> 4) & 0x0F);
        	dev = deviceToText(m.getElement(19), m.getElement(20));
        	if (sum != (m.getElement(21) & 0xFF)) {
        		goodCheckSum = false;
        	}
        	val = cmd + " House: " + hCode + " Device:" + dev;
        	if (!goodSync) {
        		val = val + " BAD SYNC";
        	}
        	if (!goodCheckSum) {
        		val = val + " BAD CHECKSUM: " + (m.getElement(21) & 0xFF) + " vs " + sum;
        	}
        	break;
        default:
        	val = "UNK " + m.toString();
        	break;
        }
        return val;
    }
}

/* @(#)Constants.java */
