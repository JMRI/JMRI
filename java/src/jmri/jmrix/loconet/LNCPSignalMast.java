// LNCPSignalMast.java

package jmri.jmrix.loconet;

import org.apache.log4j.Logger;
import jmri.implementation.DccSignalMast;
import jmri.NmraPacket;

/**
 * Extend jmri.SignalMast for signals implemented by an LNCP
 * <P>
 * This implementation writes out to the physical signal when
 * it's commanded to change appearance, and updates its internal state
 * when it hears commands from other places.
 * <p>
 * This is a specific implementation for the RR-cirkits LNCP interface board
 * A more general implementation, which can work
 * with any system(s), is available in {@link jmri.implementation.DccSignalMast}.
 *
 * @author			Kevin Dickerson Copyright (C) 2002
 * @version			$Revision: 17977 $
 */
public class LNCPSignalMast extends DccSignalMast implements LocoNetListener {

    public LNCPSignalMast( String sys, String user ) {
        super(sys, user, "F$lncpsm");
        packetRepeatCount = 1;
        configureFromName(sys);
        init();
    }

    public LNCPSignalMast( String sys ) {
        super(sys, null, "F$lncpsm");
        packetRepeatCount = 1;
        configureFromName(sys);
        init();
    }
    
    void init() {
        if ((c instanceof SlotManager) && (((SlotManager) c).getSystemConnectionMemo()!=null)){
            tc = ((SlotManager)c).getSystemConnectionMemo().getLnTrafficController();
        } else {
            tc = jmri.InstanceManager.getDefault(LnTrafficController.class);
        }
        
        //We cheat, and store the two bytes that make up an NMRA packet for later use in decoding a message from the loconet
        int lowAddr = ((dccSignalDecoderAddress-1) & 0x03 ) ;  // Output Pair Address
        int boardAddr = ((dccSignalDecoderAddress-1) >> 2); // Board Address
        int midAddr =  boardAddr & 0x3F ;
        
        int highAddr = ( (~boardAddr) >> 6) & 0x07;
        
        dccByteAddr1 = ((byte) (0x80 | midAddr ));
        dccByteAddr2 = ((byte) (0x01 | (highAddr<<4) | (lowAddr << 1)));
        tc.addLocoNetListener(~0, this);
    }
    
    LnTrafficController tc;

    
    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //						Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state (or set the commanded state directly)
    public void message(LocoNetMessage l) {
        if(l.getOpCode()!=LnConstants.OPC_IMM_PACKET) {
            return;
        }

        int val7f = l.getElement(2); /* fixed value of 0x7f */
        if(val7f != 0x7f){
            return;
        }
        
        int reps = l.getElement(3);
        int len = ((reps & 0x70) >> 4);
        if(len!=3){
            return;
        }
        int dhi = l.getElement(4);
        int im1 = l.getElement(5);
        int im2 = l.getElement(6);
        int im3 = l.getElement(7);
        
        byte[] packet = new byte[len];
        packet[0] = (byte) (im1 + ((dhi & 0x01) != 0 ? 0x80 : 0));
        packet[1] = (byte) (im2 + ((dhi & 0x02) != 0 ? 0x80 : 0));

        if(myAddress(packet[0], packet[1])){
            packet[2] = (byte) (im3 + ((dhi & 0x04) != 0 ? 0x80 : 0));
            int aspect = packet[2];
            for(String appearance: appearanceToOutput.keySet()){
                if(appearanceToOutput.get(appearance)==aspect){
                    setKnownState(appearance);
                    return;
                }
            }
            log.error("Aspect for id " + aspect + "on signal mast " + this.getDisplayName() + " not found");
        }
    }
    
    public void setAspect(String aspect){
        if(appearanceToOutput.containsKey(aspect)){
            c.sendPacket( NmraPacket.altAccSignalDecoderPkt( dccSignalDecoderAddress, appearanceToOutput.get(aspect) ), packetRepeatCount);
        } else {
            log.warn("Trying to set aspect that has not been configured");
        }
    }
    
    public void setKnownState(String aspect){
        String oldAspect = this.aspect;
        this.aspect = aspect;
        this.speed = (String)getSignalSystem().getProperty(aspect, "speed");
        firePropertyChange("Aspect", oldAspect, aspect);
    }
    
    public void dispose() {
        tc.removeLocoNetListener(~0, this);
    }
    
    byte dccByteAddr1;
    byte dccByteAddr2;
    
    private boolean myAddress(byte a1, byte a2){
        if(a1!=dccByteAddr1)
            return false;
        if(a2!=dccByteAddr2)
            return false;
        return true;
    }

    static Logger log = Logger.getLogger(LNCPSignalMast.class.getName());
    
}


/* @(#)LNCPSignalMast.java */
