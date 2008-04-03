// Constants.java

package jmri.jmrix.powerline.cp290;


/**
 * Constants and functions specific to the CP290 interface
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.3 $
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
		case 0x05:
			cmdText = "DIM";
			int v = (level * 100) / 16;
			cmdText = cmdText + " " + v + "%";
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
	
}

/* @(#)Constants.java */
