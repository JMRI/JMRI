/**
 * LnSensorAddress.java
 *
 * Description:		utilities for handling sensor addresses
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version
 */

package jmri.jmrix.loconet;

public class LnSensorAddress {

	int _low;
	int _high;
	int _as;

	boolean _valid;

	public LnSensorAddress(int sw1, int sw2) {
		_as = sw2&0x40;		// should be a LocoNet constant?
		_high = sw2&0x0F;
		_low = sw1&0x7F;
		_valid = true;
	}

	public LnSensorAddress(String s) {
		_valid = false;

		// check valid
		if (s.substring(0,2).equals("LS")) {
			// parse out and decode the name
			if (s.charAt(s.length()-1)=='A') {
				// DS54 addressing, Aux input
				_as = 0x40;
				int n = Integer.parseInt(s.substring(2, s.length()-1));
				_high = n/128;
				_low = n&0x7F;
				_valid = true;
			} else if (s.charAt(s.length()-1)=='S') {
				// DS54 addressing, Switch input
				_as = 0x00;
				int n = Integer.parseInt(s.substring(2, s.length()-1));
				_high = n/128;
				_low = n&0x7F;
				_valid = true;
			} else {
				char c = s.charAt(s.length()-2);
				if (c>='A' && c<='D') {
					// BDL16 addressing
					int d=0;
					switch (c) {
						case 'A': d = 0; break;
						case 'B': d = 1; break;
						case 'C': d = 2; break;
						case 'D': d = 3; break;
					}
					int n = Integer.parseInt(s.substring(2, s.length()-2))*16+d*4
								+Integer.parseInt(s.substring(s.length()-1, s.length()));
					_high = n/128;
					_low = (n&0x7F)/2;
					_as = (n&0x01)*0x40;
					_valid = true;
				} else {
					// assume that its LSnnn style
					int n = Integer.parseInt(s.substring(2, s.length()));
					_high = n/256;
					_low = (n&0xFE)/2;
					_as = (n&0x01)*0x40;
					_valid = true;
				}
			}
		} else {
			// didn't find a leading LS, complain
			log.error("Can't parse sensor address string: "+s);
		}
	}

	// convenient calculations
	public boolean matchAddress(int a1, int a2) { // a1 is byte 1 of ln msg, a2 is byte 2
		if (getHighBits() != (a2&0x0f)) return false;
		if (getLowBits() != (a1&0x7f)) return false;
		if (getASBit() != (a2&0x40)) return false;
		return true;
	}

	public int asInt() {
		return _high*256+_low*2+(_as!=0 ? 1 : 0);
	}

	// accessors for parsed data
	public int getLowBits() 	{ return _low; }
	public int getHighBits() 	{ return _high; }
	public int getASBit() 		{ return _as; }
	public boolean isValid()    { return _valid; }

    public String toString() {
        return getNumericAddress()+":"
                +getDS54Address()+":"
                +getBDL16Address();
    }

	// accessors for formatted names
	public String getNumericAddress() {
		return "LS"+asInt();
	}

	public String getDS54Address() {
		if (_as != 0 )
			return "LS"+(_high*128+_low)+"A";
		else
			return "LS"+(_high*128+_low)+"S";
	}

	public String getBDL16Address() {
		String letter = null;
		String digit = null;
		int n;

		switch (asInt()&0x03) {
			case 0: digit = "0"; break;
			case 1: digit = "1"; break;
			case 2: digit = "2"; break;
			case 3: digit = "3"; break;
			default: digit = "X"; log.error("Unexpected digit value: "+asInt());
		}
		switch ( (asInt()&0x0C)/4 ) {
			case 0: letter = "A"; break;
			case 1: letter = "B"; break;
			case 2: letter = "C"; break;
			case 3: letter = "D"; break;
			default: letter = "X"; log.error("Unexpected letter value: "+asInt());
		}
		return "LS"+(asInt()/16)+letter+digit;
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorAddress.class.getName());

}


/* @(#)LnSensorAddress.java */
