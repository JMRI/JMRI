/** 
 * LnHexFilePort.java
 *
 * Description:		LnHexFilePort implements a LnPortController via a 
 *					ASCII-hex input file. See below for the file format
 *					There are user-level controls for
 *						send next message
 *						how long to wait between messages
 *
 *					An object of this class should run in a thread
 *					of its own so that it can fill the output pipe as
 *					needed.
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

/**
	The input file is expected to have one message per line. Each line
	can contain as many bytes as needed, each represented by two Hex characters
	and separated by a space. Variable whitespace is not (yet) supported
*/

package LocoMon;

import ErrLoggerJ.ErrLog;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LnHexFilePort 			extends LocoNet.LnPortController implements Runnable {
/* load("filename") fills the contents from a file */
public void load(String filename) {
		//attempt to access the file
		ErrLog.msg(ErrLog.routine, "LnHexFilePort", "load", "Loading data from file "+filename);
		try {
			sFile = new DataInputStream(new FileInputStream(filename));
			}
		catch (Exception e) {
			ErrLog.msg(ErrLog.error, "LnHexFilePort", "load (file)", "Exception: "+e.toString());
			}

		// create the pipe stream for output, also store as the input stream if somebody wants to send
		// (This will emulate the LocoNet echo
		try {
			PipedInputStream tempPipe = new PipedInputStream();
			pin = new DataInputStream(tempPipe);
			outpipe = new DataOutputStream(new PipedOutputStream(tempPipe));
			pout = outpipe;
			}
		catch (Exception e) {
			ErrLog.msg(ErrLog.error, "LnHexFilePort", "load (pipe)", "Exception: "+e.toString());
			}
	}
	
public void run() { // invoked in a new thread
		ErrLog.msg(ErrLog.debugging,"LnHexFilePort","run","entry point");
		// process the input file into the output side of pipe
		try {
			String s;
			byte bval;
			int ival;
			int len;
			while (sFile.available() > 3) {
				// this loop reads one line per turn
				s = sFile.readLine();
				// ErrLog.msg(ErrLog.debugging,"LnHexFilePort","run","string=<"+s+">");
				len = s.length();
				for (int i=0; i<len; i+=3) {
					// parse as hex into integer, then convert to byte
					ival = Integer.valueOf(s.substring(i,i+2),16).intValue();
					// send each byte to the output pipe (input to consumer)
					bval = (byte) ival;
					outpipe.writeByte(bval);
					}
				// finished that line, wait 
				Thread.sleep(delay);
				}
			}
		catch (Exception e) {
			ErrLog.msg(ErrLog.error, "LnHexFilePort","run", "Exception: "+e.toString());
			}
	}

public void setDelay(int newDelay) {
	delay = newDelay;
	}
	
// base class methods
	public DataInputStream getInputStream() {
		if (pin == null) ErrLog.msg(ErrLog.error, "LnHexFilePort", "getInputStream", "called before load(), stream not available");
		return pin;
		}
	
	public DataOutputStream getOutputStream(){
		if (pout == null) ErrLog.msg(ErrLog.error, "LnHexFilePort", "getOutputStream", "called before load(), stream not available");
		return pout;
		}
	
	public boolean status() {return (pout!=null)&(pin!=null);}


// private data

// streams to share with user class
private DataOutputStream pout = null; // this is provided to classes who want to write to us
private DataInputStream pin = null;  // this is provided to class who want data from us

// internal ends of the pipes
private DataOutputStream outpipe = null;  // feed pin
private DataInputStream inpipe = null; // feed pout

// internal access to the input file
DataInputStream sFile = null;

// define operation
private int delay=100;  				// units are milliseconds; default is quiet a busy LocoNet
}


/* @(#)LnHexFilePort.java */
