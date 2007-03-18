// LoadModifier.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the LOAD_MODIFIER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.3 $
 */

class LoadModifier extends SdfMacro {

    public LoadModifier(int modType, int arg1, int arg2, int arg3) {
        this.modType = modType;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }
    
    public String name() {
        return "LOAD_MODIFIER";
    }
    
    int modType;
    int arg1, arg2, arg3;
        
    public int length() { return 4;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xF0) != 0xE0) return null;
        int byte1 = buff.getAtIndexAndInc()&0xFF;
        int byte2 = buff.getAtIndexAndInc()&0xFF;
        int byte3 = buff.getAtIndexAndInc()&0xFF;
        int byte4 = buff.getAtIndexAndInc()&0xFF;
        return new LoadModifier(byte1&0x0F,byte2, byte3, byte4);
    }
    
    String modTypeVal() {
        return jmri.util.StringUtil.getNameFromState(modType, modControlCodes, modControlNames);
    }

    String argVal() {
        String arg1Val = ""+arg1;
        String arg2Val = ""+arg2;
        String arg3Val = ""+arg3;
        return arg1Val+","+arg2Val+","+arg3Val;
    }
    
    public String toString() {
        return linestart+name()+' '+modTypeVal()+","+argVal()+'\n';
    }
}

/* @(#)LoadModifier.java */
