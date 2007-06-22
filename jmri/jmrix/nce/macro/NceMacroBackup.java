// NceMacrobackup.java

package jmri.jmrix.nce.macro;

import javax.swing.*;

import java.io.*;

import jmri.util.StringUtil;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;


/**
 * Backups NCE Macros to a text file format defined by NCE.
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
 * This backup routine uses the same macro data format as NCE.
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.3 $
 */


public class NceMacroBackup extends Thread implements jmri.jmrix.nce.NceListener{
	
	private static final int CS_MACRO_MEM = 0xC800;	// start of NCE CS Macro memory
	private static final int NUM_MACRO = 256;		// there are 256 possible macros
	private static final int MACRO_LNTH = 20;		// 20 bytes per macro
	private static final int REPLY_16 = 16;			// reply length of 16 byte expected
	private static int replyLen = 0;				// expected byte length
	private static int waiting = 0;					// to catch responses not intended for this module
	private boolean secondRead = false;				// when true, another 16 byte read expected
	private static boolean fileValid = false;		// used to flag backup status messages
	
	private static byte[] nceMacroData = new byte [MACRO_LNTH];
	
	public void run() {

		// get file to write to
		JFileChooser fc = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());
		fc.addChoosableFileFilter(new textFilter());
		
		File fs = new File ("NCE macro backup.txt");
		fc.setSelectedFile(fs);
		
		int retVal = fc.showSaveDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // cancelled
		if (fc.getSelectedFile() == null)
			return; // cancelled

		File f = fc.getSelectedFile();
		if (f.exists()) {
			if(JOptionPane.showConfirmDialog(null, "File "
					+ f.getName() + " already exists, overwrite it?",
					"Overwrite file?", JOptionPane.OK_CANCEL_OPTION)!= JOptionPane.OK_OPTION) {
				return;
			}
		}
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(f)),
					true);
		} catch (IOException e) {
			return;
		}
		
		if (JOptionPane.showConfirmDialog(null,
				"Backup can take over a minute, continue?", "NCE Macro",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}

		// now read NCE CS macro memory and write to file
		
		waiting = 0;			// reset in case there was a previous error
		fileValid = true;		// assume we're going to suceed
		String line;			// output string to file

		for (int macroNum = 0; macroNum < NUM_MACRO; macroNum++) {

			getNceMacro(macroNum);

			// wait up to 30 sec for each read pair to complete
			int waitcount = 30;
			while (waiting > 0) {
				synchronized (this) {
					try {
						wait(1000);
					} catch (InterruptedException e) {
					}
				}
				if (waitcount-- < 0) {
					log.error("read timeout");
					fileValid = false;		// need to quit
					macroNum = NUM_MACRO;	// exit "for loop"
					break;
				}
			}

			if (fileValid) {
				line = ":" + Integer.toHexString(CS_MACRO_MEM + (macroNum * MACRO_LNTH));

				for (int i = 0; i < MACRO_LNTH; i++) {
					line += " " + StringUtil.twoHexFromInt(nceMacroData[i++]);
					line +=	StringUtil.twoHexFromInt(nceMacroData[i]);
				}

				if (log.isDebugEnabled()) 
					log.debug("macro " + line);

				fileOut.println(line);
			}
		}
		
		if (fileValid) {
			// NCE file terminator
			line = ":0000";
			fileOut.println(line);
		}

		// Write to disk and close file
		fileOut.flush();
		fileOut.close();

		if (fileValid) {
			JOptionPane.showMessageDialog(null, "Successful Backup!",
					"NCE Macro", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Backup failed", "NCE Macro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Read 20 bytes of NCE CS memory
	private void getNceMacro(int mN) {

		NceMessage m = readMacroMemory(mN, false);
		NceTrafficController.instance().sendNceMessage(m, this);
		NceMessage m2 = readMacroMemory(mN, true);
		NceTrafficController.instance().sendNceMessage(m2, this);

	}

	// Reads 16 bytes of NCE macro memory, and adjusts for second read 
	private NceMessage readMacroMemory(int macroNum, boolean second) {
		if (!second)
			secondRead = second; 		// set flag for 1st receive
		int nceMacroAddr = (macroNum * MACRO_LNTH) + CS_MACRO_MEM;
		if (second) {
			nceMacroAddr += REPLY_16; 	// adjust for second memory read
		}
		replyLen = REPLY_16; 			// Expect 16 byte response
		waiting++;
		byte[] bl = NceBinaryCommand.accMemoryRead(nceMacroAddr);
		NceMessage m = NceMessage.createBinaryMessage(bl, REPLY_16);
		return m;
	}

	public void message(NceMessage m) {
	} // ignore replies

	// this reply always expects two consective reads
	public void reply(NceReply r) {

		if (waiting <= 0) {
			log.error("unexpected response");
			return;
		}
		if (r.getNumDataElements() != replyLen) {
			log.error("reply length incorrect");
			return;
		}

		// first read 16 bytes, second read only 4 bytes needed
		int offset = 0;
		int numBytes = REPLY_16;
		if (secondRead) {
			offset = REPLY_16;
			numBytes = 4;
		}

		for (int i = 0; i < numBytes; i++) {
			nceMacroData[i + offset] = (byte) r.getElement(i);
		}
		waiting--;
		// wake up backup thread
		if (secondRead) {
			synchronized (this) {
				notify();
			}
		}
		secondRead = true; // next read is the next 4 bytes of macro
		
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
			.getInstance(NceMacroBackup.class.getName());
}
