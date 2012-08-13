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
        protocol = LocoAddress.DCC_SHORT;
        if(isLong)
            protocol = LocoAddress.DCC_LONG;
	}
    
    public DccLocoAddress(int number, int protocol){
        this.number = number;
        this.protocol = protocol;
    }
	
	public DccLocoAddress(DccLocoAddress l) {
		this.number = l.number;
        this.protocol = l.protocol;
	}

    public boolean equals(Object a) {
        if (a==null) return false;
        try {
            DccLocoAddress other = (DccLocoAddress) a;
            if (this.number != other.number) return false;
            if (this.protocol != other.protocol) return false;
            return true;
        } catch (Exception e) { return false; }
    }
    
    public int hashCode() {
        switch(protocol){
            case LocoAddress.DCC_SHORT : return number;
            case LocoAddress.DCC_LONG : return 20000+number;
            case LocoAddress.SELECTRIX: return 30000+number;
            case LocoAddress.MOTOROLA: return 40000+number;
            default: return number;
        }
    }
    
    public String toString() {
        switch(protocol){
            case LocoAddress.DCC_SHORT : return ""+number+"(S)";
            case LocoAddress.DCC_LONG : return ""+number+"(L)";
            case LocoAddress.SELECTRIX: return ""+number+"(SX)";
            case LocoAddress.MOTOROLA: return ""+number+"(MM)";
            default: return ""+number+"(D)";
        }
        /*if (isLong) return ""+number+"(L)";
        else return ""+number+"(S)";*/
    }
    
	public boolean isLongAddress() { 
        if(protocol==LocoAddress.DCC_LONG) return true;
        return false;
    }
    
    public int getProtocol() {
        return protocol;
    }
    
    public int getNumber() { return number; }
	
    private int number;
    private int protocol = LocoAddress.DCC;

}


/* @(#)DccLocoAddress.java */
