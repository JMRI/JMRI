/** 
 * LocoNetMessage.java
 *
 * Description:		<describe the LocoNetMessage class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet;

import ErrLoggerJ.ErrLog;

public class LocoNetMessage {
// is this logically an abstract class?

// create a new one
public LocoNetMessage(int i) {
	if (i<1)
		ErrLog.msg(ErrLog.error, "LocoNetMessage","","invalid length in call to ctor");
	_nDataBytes = i;
	_dataBytes = new int[i];
}

public void setOpCode(int i) { _dataBytes[0]=i;}
public int getOpCode() {return _dataBytes[0];}
public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

// accessors to the bulk data
public int getNumDataElements() {return _nDataBytes;}
public int getElement(int n) {return _dataBytes[n];}
public void setElement(int n, int v) { _dataBytes[n] = v; }

// display format
public String toString() {
	String s = "";
	for (int i=0; i<_nDataBytes; i++) s+=Integer.toHexString(_dataBytes[i])+" ";
	return s;
	}
	
// contents (private)
private int _nDataBytes = 0;
private int _dataBytes[] = null;

}


/* @(#)LocoNetMessage.java */
