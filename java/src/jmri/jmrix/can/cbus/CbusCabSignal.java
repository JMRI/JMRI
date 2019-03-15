package jmri.jmrix.can.cbus;

import jmri.implementation.DefaultCabSignal;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficController;
import jmri.LocoAddress;
import jmri.SignalMast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CBUS implementation of a Cab Signal Object, describing the state of the 
 * track ahead relative to a locomotive with a given address.  This is 
 * effectively a mobile signal mast.
 * <p>
 * Uses an experimental MERG CBUS OPC
 * todo - add semaphore flags
 * todo - add loco speed / block speed
 *
 * @author Steve Young Copyright (C) 2018
 * @author Paul Bender Copyright (C) 2019
 */
public class CbusCabSignal extends DefaultCabSignal {

    private TrafficController tc;

    public CbusCabSignal(CanSystemConnectionMemo memo,LocoAddress address){
       super(address);
       tc = memo.getTrafficController();
       log.debug("created cab signal for {}",address);
    }

    /**
     * A method for cleaning up the cab signal 
     */
    @Override
    public void dispose(){
        super.dispose();
        tc=null;
    }

    /**
     * Forward the command to the layout that sets the displayed signal
     * aspect for this address
     */
    @Override
    protected void forwardAspectToLayout(){
        LocoAddress locoaddr = getCabSignalAddress();
        SignalMast mast = getNextMast();
        
        int locoAddr = locoaddr.getNumber();
        if (locoaddr.getProtocol() == (LocoAddress.Protocol.DCC_LONG)) {
            locoAddr = locoAddr | 0xC000;
        }
        // Calculate the two byte loco address value
        int locoD1 = locoAddr / 256;
        int locoD2 = locoAddr & 0xff;

        int sendAspect1 = 0xff; // default case, unknown.
        int sendAspect2 = 0x00; 
        int sendSpeed = 0xff; // default case, unknown.

        if ( mast != null ) {
            
            // String speed = (String) mast.getSignalSystem().getProperty(mast.getAspect(), "speed");
           
            switch( mast.getAspect() ) {
                case "Danger": // NOI18N
                case "On": // NOI18N
                    sendAspect1 = 0;
                    break;
                case "Caution": // NOI18N
                    sendAspect1 = 1;
                    break;
                case "Preliminary Caution": // NOI18N
                    sendAspect1 = 2;
                    break;
                case "Proceed": // NOI18N
                    sendAspect1 = 3;
                    break;
                case "Off": // NOI18N
                    sendAspect1 = 4;
                    break;
                case "Flash Caution": // NOI18N
                    sendAspect1 = 1;
                    sendAspect2 = 1;
                    break;
                case "Flash Preliminary Caution": // NOI18N
                    sendAspect1 = 2;
                    sendAspect2 = 1;
                    break;
                default: {
                    // if no matching speed in the list above, check for
                    // the constant values in the SignalAppearanceMap.
                    if(mast.getAspect().equals(mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.PERMISSIVE))){
                    sendAspect1 = 0x04;
                    } else if(mast.getAspect().equals(mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER))){
                    sendAspect1 = 0x00;
                    } else if(mast.getAspect().equals(mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD))){
                    sendAspect1 = 0x00;
                    } else if(mast.getAspect().equals(mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK))){
                    sendAspect1 = 0x00; // show nothing;
                    }
                }
            }
        }
        
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(7);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, 0xc2); // experimental cabdata opc
        m.setElement(1, locoD1); // addr hi
        m.setElement(2, locoD2);  // addr low
        m.setElement(3, 1); // datcode type
        m.setElement(4, ( sendAspect1 )); // aspect 1
        m.setElement(5, ( sendAspect2 )); // aspect 2
        m.setElement(6, ( sendSpeed ) ); // speed
        tc.sendCanMessage(m, null);
        
    }

    /**
     * Forward the command to the layout that clears any displayed signal
     * for this address
     */
    @Override
    protected void resetLayoutCabSignal(){
        LocoAddress locoaddr = getCabSignalAddress();
        int locoAddr = locoaddr.getNumber();
        if (locoaddr.getProtocol()==(LocoAddress.Protocol.DCC_LONG)) {
            locoAddr = locoAddr | 0xC000;
        }
        // Calculate the two byte loco address value
        int locoD1 = locoAddr / 256;
        int locoD2 = locoAddr & 0xff;

        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(7);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, 0xc2); // experimental cabdata opc
        m.setElement(1, locoD1); // addr hi
        m.setElement(2, locoD2);  // addr low
        m.setElement(3, 1); // datcode type
        m.setElement(4, ( 0xff )); // aspect 1
        m.setElement(5, ( 0 )); // aspect 2
        m.setElement(6, ( 0xff )); // speed
        tc.sendCanMessage(m, null);
        
    }

    private final static Logger log = LoggerFactory.getLogger(CbusCabSignal.class);

}
