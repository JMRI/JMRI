/** 
 * NceMessage.java
 *
 * Description:		<describe the NceMessage class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;


// Note:  This handles the "binary" form of command in the NCE spec

public class NceMessage {
	// is this logically an abstract class?

	// create a new one
	public  NceMessage(int i) {
		if (i<1)
			log.error("invalid length in call to ctor");
		_nDataChars = i;
		_dataChars = new int[i];
	}

	public void setOpCode(int i) { _dataChars[0]=i;}
	public int getOpCode() {return _dataChars[0];}
	public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

	// accessors to the bulk data
	public int getNumDataElements() {return _nDataChars;}
	public int getElement(int n) {return _dataChars[n];}
	public void setElement(int n, int v) { _dataChars[n] = v; }

	// display format
	public String toString() {
		String s = "";
		for (int i=0; i<_nDataChars; i++) {
			if (i!=0) s+=" ";
			if (_dataChars[i] < 16) s+="0";
			s+=Integer.toHexString(_dataChars[i]);
		}
		return s;
	}
	
	// diagnose format
	public boolean isKillMain() {
		return getOpCode() == 'K';
	}

	public boolean isEnableMain() {
		return getOpCode() == 'E';
	}
	

	// static methods to return a formatted message
	static public NceMessage getEnableMain() {
		NceMessage m = new NceMessage(1);
		m.setOpCode('E');
		return m;
	}
	
	static public NceMessage getKillMain() {
		NceMessage m = new NceMessage(1);
		m.setOpCode('K');
		return m;
	}
		
	static public NceMessage getProgMode() {
		NceMessage m = new NceMessage(1);
		m.setOpCode('M');
		return m;
	}
	
	static public NceMessage getExitProgMode() {
		NceMessage m = new NceMessage(1);
		m.setOpCode('X');
		return m;
	}

	static public NceMessage getReadCV(int cv) { //Rxxx
		NceMessage m = new NceMessage(4);
		m.setOpCode('R');
		return m;
	}

	static public NceMessage getProgCV(int cv, int val) { //Pxxx xxx
		NceMessage m = new NceMessage(4);
		m.setOpCode('P');
		return m;
	}
		
	// contents (private)
	private int _nDataChars = 0;
	private int _dataChars[] = null;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMessage.class.getName());

}


/* @(#)NceMessage.java */
