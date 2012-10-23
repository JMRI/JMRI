// AbstractLocoAddress.java

package jmri.implementation;

import jmri.LocoAddress;

/** 
 * Encapsulate information for a basic Locomotive Address.
 *
 *
 * @author			Bob Jacobsen Copyright (C) 2005, 2012
 * @version			$Revision$
 */

public class AbstractLocoAddress implements LocoAddress {

	public AbstractLocoAddress(int number, boolean isLong) {
		this.number = number;
        protocol = LocoAddress.Protocol.DCC_SHORT;
        if(isLong)
            protocol = LocoAddress.Protocol.DCC_LONG;
	}
    
    public AbstractLocoAddress(int number, LocoAddress.Protocol protocol){
        this.number = number;
        this.protocol = protocol;
    }
	
	public AbstractLocoAddress(AbstractLocoAddress l) {
		this.number = l.number;
        this.protocol = l.protocol;
	}

    public boolean equals(Object a) {
        if (a==null) return false;
        try {
            AbstractLocoAddress other = (AbstractLocoAddress) a;
            if (this.number != other.number) return false;
            if (this.protocol != other.protocol) return false;
            return true;
        } catch (Exception e) { return false; }
    }
    
    public int hashCode() {
        switch(protocol){
            case DCC_SHORT :    return (int)(number&0xFFFFFFFF);
            case DCC_LONG :     return (int)(20000+number&0xFFFFFFFF);
            case SELECTRIX:     return (int)(30000+number&0xFFFFFFFF);
            case MOTOROLA:      return (int)(40000+number&0xFFFFFFFF);
            case MFX:           return (int)(50000+number&0xFFFFFFFF);
            case M4:            return (int)(60000+number&0xFFFFFFFF);
            case OPENLCB:       return (int)(70000+number&0xFFFFFFFF);
            default:            return (int)(number&0xFFFFFFFF);
        }
    }
    
    public String toString() {
        switch(protocol){
            case DCC_SHORT : return ""+number+"(S)";
            case DCC_LONG : return ""+number+"(L)";
            case SELECTRIX: return ""+number+"(SX)";
            case MOTOROLA: return ""+number+"(MM)";
            case M4: return ""+number+"(M4)";
            case MFX: return ""+number+"(MFX)";
            case OPENLCB: return ""+number+"(OpenLCB)";
            default: return ""+number+"(D)";
        }
    }
    
	public boolean isLongAddress() { 
        if(protocol==LocoAddress.Protocol.DCC_SHORT)
            return false;
        return true;
    }
    
    public LocoAddress.Protocol getProtocol() {
        return protocol;
    }
    
    public int getNumber() { return (int)number; }
	
    protected long number;
    protected LocoAddress.Protocol protocol = LocoAddress.Protocol.DCC;

}


/* @(#)DefaultLocoAddress.java */
