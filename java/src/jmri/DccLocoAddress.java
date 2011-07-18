// DccLocoAddress.java

package jmri;


/** 
 * Encapsulate information for a DCC Locomotive Decoder Address.
 *
 * In particular, this handles the "short" (standard) vs
 * "extended" (long) address selection.
 *
 * An address must be one of these, hence short vs long is encoded
 * as a boolean.
 *
 * Once created, the number and long/short status cannot be changed.
 *
 * @author			Bob Jacobsen Copyright (C) 2005
 * @version			$Revision$
 */

public class DccLocoAddress implements LocoAddress {

	public DccLocoAddress(int number, boolean isLong) {
		this.number = number;
		this.isLong = isLong;
	}
	
	public DccLocoAddress(DccLocoAddress l) {
		this.number = l.number;
		this.isLong = l.isLong;
	}

    public boolean equals(Object a) {
        if (a==null) return false;
        try {
            DccLocoAddress other = (DccLocoAddress) a;
            if (this.number != other.number) return false;
            if (this.isLong != other.isLong) return false;
            return true;
        } catch (Exception e) { return false; }
    }
    
    public int hashCode() {
        if (isLong) return 20000+number;
        else return number;
    }
    
    public String toString() {
        if (isLong) return ""+number+"(L)";
        else return ""+number+"(S)";
    }
    
	public boolean isLongAddress() { return isLong; }
	public int getNumber() { return number; }
	
    private int number;
    private boolean isLong;
}


/* @(#)DccLocoAddress.java */
