/**
 * EasyDccMessage.java
 *
 * Description:		<describe the EasyDccMessage class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Id: EasyDccMessage.java,v 1.2 2002-03-30 19:22:53 jacobsen Exp $
 */

package jmri.jmrix.easydcc;


// Note:  This handles the "binary" form of command in the EasyDcc spec

public class EasyDccMessage {
	// is this logically an abstract class?

	// create a new one
	public  EasyDccMessage(int i) {
		if (i<1)
			log.error("invalid length in call to ctor");
		_nDataChars = i;
		_dataChars = new int[i];
	}

	// copy one
	public  EasyDccMessage(EasyDccMessage m) {
		if (m == null)
			log.error("copy ctor of null message");
		_nDataChars = m._nDataChars;
		_dataChars = new int[_nDataChars];
		for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
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
            s+=(char)_dataChars[i];
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
	static public EasyDccMessage getEnableMain() {
		EasyDccMessage m = new EasyDccMessage(1);
		m.setOpCode('E');
		return m;
	}

	static public EasyDccMessage getKillMain() {
		EasyDccMessage m = new EasyDccMessage(1);
		m.setOpCode('K');
		return m;
	}

	static public EasyDccMessage getProgMode() {
		EasyDccMessage m = new EasyDccMessage(1);
		m.setOpCode('M');
		return m;
	}

	static public EasyDccMessage getExitProgMode() {
		EasyDccMessage m = new EasyDccMessage(1);
		m.setOpCode('X');
		return m;
	}

	static public EasyDccMessage getReadPagedCV(int cv) { //Rxxx
		EasyDccMessage m = new EasyDccMessage(5);
		m.setOpCode('R');
        m.setElement(1, ' ');
		addIntAsThree(cv, m, 2);
		return m;
	}

	static public EasyDccMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
		EasyDccMessage m = new EasyDccMessage(9);
		m.setOpCode('P');
        m.setElement(1, ' ');
		addIntAsThree(cv, m, 2);
		m.setElement(5,' ');
		addIntAsThree(val, m, 6);
		return m;
	}

	static public EasyDccMessage getReadRegister(int reg) { //Vx
		if (reg>8) log.error("register number too large: "+reg);
		EasyDccMessage m = new EasyDccMessage(3);
		m.setOpCode('V');
		String s = ""+reg;
        m.setElement(1, ' ');
		m.setElement(2, s.charAt(s.length()-1));
		return m;
	}

	static public EasyDccMessage getWriteRegister(int reg, int val) { //Sx xxx
		if (reg>8) log.error("register number too large: "+reg);
		EasyDccMessage m = new EasyDccMessage(7);
		m.setOpCode('S');
		String s = ""+reg;
        m.setElement(1, ' ');
		m.setElement(2, s.charAt(s.length()-1));
		m.setElement(3,' ');
		addIntAsThree(val, m, 4);
		return m;
	}

	// contents (private)
	private int _nDataChars = 0;
	private int _dataChars[] = null;

	private static String addIntAsThree(int val, EasyDccMessage m, int offset) {
		String s = ""+val;
		if (s.length() != 3) s = "0"+s;  // handle <10
		if (s.length() != 3) s = "0"+s;  // handle <100
		m.setElement(offset,s.charAt(0));
		m.setElement(offset+1,s.charAt(1));
		m.setElement(offset+2,s.charAt(2));
		return s;
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccMessage.class.getName());

}


/* @(#)EasyDccMessage.java */
