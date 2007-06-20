// NceMacroRestore.java

package jmri.jmrix.nce.macro;

import javax.swing.*;

import java.io.*;
import jmri.util.StringUtil;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;


/**
 * Restores NCE Macros from a text file defined by NCE.
 * 
 * NCE "Backup macros" dumps the macros into a text file. Each line contains
 * the contents of one macro.  The first macro, 0 starts at address xC800. 
 * The last macro 255 is at address xDBEC.
 * 
 * NCE file format:
 * 
 * :C800 (macro 0: 20 hex chars representing 10 accessories) 
 * :C814 (macro 1: 20 hex chars representing 10 accessories)
 * :C828 (macro 2: 20 hex chars representing 10 accessories)
 *   .
 *   .
 * :DBEC (macro 255: 20 hex chars representing 10 accessories)
 * :0000
 * 
 *  
 * Macro data byte:
 * 
 * bit	     15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
 *                                 _     _  _  _
 *  	      1  0  A  A  A  A  A  A  1  A  A  A  C  D  D  D
 * addr bit         7  6  5  4  3  2    10  9  8     1  0  
 * turnout												   T
 * 
 * By convention, MSB address bits 10 - 8 are one's complement.  NCE macros always set the C bit to 1.
 * The LSB "D" (0) determines if the accessory is to be thrown (0) or closed (1).  The next two bits
 * "D D" are the LSBs of the accessory address. Note that NCE display addresses are 1 greater than 
 * NMRA DCC. Note that address bit 2 isn't supposed to be inverted, but it is the way NCE implemented
 * their macros.
 * 
 * Examples:
 * 
 * 81F8 = accessory 1 thrown
 * 9FFC = accessory 123 thrown
 * B5FD = accessory 211 close
 * BF8F = accessory 2044 close
 * 
 * FF10 = link macro 16 
 * 
 * The restore routine checks that each line of the file begins with the appropriate macro address.
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.2 $
 */


public class NceMacroRestore extends Thread implements jmri.jmrix.nce.NceListener{
	
	private static final int CS_MACRO_MEM = 0xC800;	// start of NCE CS Macro memory 
	private static final int MACRO_LNTH = 20;		// 20 bytes per macro
	private static final int REPLY_1 = 1;			// reply length of 1 byte expected
	private static int replyLen = 0;				// expected byte length
	private static int waiting = 0;					// to catch responses not intended for this module
	private static boolean fileValid = false;		// used to flag status messages
	
	public void run() {

		// Get file to read from
		JFileChooser fc = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());
		fc.addChoosableFileFilter(new textFilter());
		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // cancelled
		if (fc.getSelectedFile() == null)
			return; // cancelled
		File f = fc.getSelectedFile();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return;
		}

		// Now read the file and check the macro address
		waiting = 0;
		fileValid = false;					// in case we break out early
		int curMacro = CS_MACRO_MEM;		// load the start address of the NCE macro memory
		byte[] macroAccy = new byte[20]; 	// NCE Macro data
		String line = " ";

		while (line != null) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				break;
			}
			
			if (line == null){				// while loop does not break out quick enough
				log.error("NCE macro file terminator :0000 not found");
				break;
			}
			if (log.isDebugEnabled()) {
				log.debug("macro " + line);
			}
			// check that each line contains the NCE memory address of the macro
			String macroAddr = ":" + Integer.toHexString(curMacro);
			String[] macroLine = line.split(" ");

			// check for end of macro terminator
			if (macroLine[0].equalsIgnoreCase(":0000")) {
				fileValid = true; // success!
				break;
			}

			if (!macroAddr.equalsIgnoreCase(macroLine[0])) {
				log.error("Restore file selected is not a vaild backup file");
				log.error("Macro addr in restore file should be " + macroAddr
						+ " Macro addr read " + macroLine[0]);
				break;
			}

			// macro file found, give the user the choice to continue
			if (curMacro == CS_MACRO_MEM) {
				if (JOptionPane
						.showConfirmDialog(
								null,
								"Restore file found!  Restore can take over a minute, continue?",
								"NCE Macro", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					break;
				}
			}

			// now read the entire line from the file and create NCE messages
			for (int i = 0; i < 10; i++) {
				int j = i << 1;				// i = word index, j = byte index

				byte b[] = StringUtil.bytesFromHexString(macroLine[i + 1]);

				macroAccy[j] = b[0];
				macroAccy[j + 1] = b[1];
			}

			NceMessage m = writeNceMacroMemory(curMacro, macroAccy, false);
			NceTrafficController.instance().sendNceMessage(m, this);
			NceMessage m2 = writeNceMacroMemory(curMacro, macroAccy, true);
			NceTrafficController.instance().sendNceMessage(m2, this);

			curMacro += MACRO_LNTH;

			// pace the number of messages queued
			if (waiting > 32) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
		}
		try {
			in.close();
		} catch (IOException e) {
		}

		// wait until all writes complete
		int waitcount = 30;
		while (waiting > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if (waitcount-- < 0) {
				log.error("write timeout to NCE macro memory");
				fileValid = false;
				break;
			}
		}

		if (fileValid) {
			JOptionPane.showMessageDialog(null, "Successful Restore!",
					"NCE Macro", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Restore failed", "NCE Macro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// writes 20 bytes of NCE macro memory, and adjusts for second write
	private static NceMessage writeNceMacroMemory(int curMacro, byte[] b,
			boolean second) {

		replyLen = REPLY_1; // Expect 1 byte response
		waiting++;
		byte[] bl;

		// write 4 bytes
		if (second) {
			curMacro += 16; // adjust for second memory
			// write
			bl = NceBinaryCommand.accMemoryWriteN(curMacro, 4);
			int j = bl.length - 16;
			for (int i = 0; i < 4; i++, j++)
				bl[j] = b[i + 16];

			// write 16 bytes	
		} else {
			bl = NceBinaryCommand.accMemoryWriteN(curMacro, 16);
			int j = bl.length - 16;
			for (int i = 0; i < 16; i++, j++)
				bl[j] = b[i];
		}
		NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_1);
		return m;
	}

	public void message(NceMessage m) {
	} // ignore replies

	public void reply(NceReply r) {
		if (log.isDebugEnabled()) {
			log.debug("waiting for " + waiting + " responses ");
		}
		if (waiting <= 0) {
			log.error("unexpected response");
			return;
		}
		waiting--;
		if (r.getNumDataElements() != replyLen) {
			log.error("reply length incorrect");
			return;
		}
		if (replyLen == REPLY_1) {
			// Looking for proper response
			int recChar = r.getElement(0);
			if (recChar != '!')
				log.error("reply incorrect");
		}
	}
	
	private class textFilter extends javax.swing.filechooser.FileFilter {
		
		public boolean accept(File f){
			if (f.isDirectory())
			return true;
			String name = f.getName();
			if (name.matches(".*\\.txt"))
				return true;
			else
				return false;
		}
		
		public String getDescription() {
			return "Text Documents (*.txt)";
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(NceMacroRestore.class.getName());
}
