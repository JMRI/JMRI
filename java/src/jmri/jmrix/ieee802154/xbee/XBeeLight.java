// XBeeLight.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractLight;
import jmri.Light;

/**
 *  Light impelementation for XBee systems.
 *  <p>
 *  @author Paul Bender Copyright (C) 2014
 *  @version $Revision$
 */

public class XBeeLight extends AbstractLight{

    private int address;
    private int baseaddress; /* The XBee Address */
    private int pin;         /* Which DIO pin does this turnout represent. */
    private String systemName;
    private com.rapplogic.xbee.api.XBeeAddress16 xbeeAddress;

    protected XBeeTrafficController tc = null;

    /**
     * Create a Light object, with system and user names and
     * a reference to the traffic controller.
     * <P>
     */
    public XBeeLight(String systemName, String userName,XBeeTrafficController controller) {
        super(systemName,userName);
        tc=controller;
        init(systemName);
    }

    public XBeeLight(String systemName,XBeeTrafficController controller) {
        super(systemName);
        tc=controller;
        init(systemName);
    }

    /**
     * Common initialization for both constructors
     */
    private void init(String id) {
        // store address
        systemName=id;
        String prefix=((XBeeConnectionMemo)(tc.getAdapterMemo())).getLightManager().getSystemPrefix();
        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or L:light address
            int seperator = systemName.indexOf(":");
            try {
                baseaddress = Integer.valueOf(systemName.substring(prefix.length()+1,seperator)).intValue();
                pin = Integer.valueOf(systemName.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
            }
        } else {
           try{
              address = Integer.parseInt(systemName.substring(prefix.length()+1));
              // calculate the base address, the nibble, and the bit to examine
              baseaddress = ((address) / 10);
              pin = ((address)%10);
           } catch (NumberFormatException ex) {
              log.debug("Unable to convert " + systemName + " Hardware Address to a number");
           }
        }
        xbeeAddress=new com.rapplogic.xbee.api.XBeeAddress16((baseaddress&0xff00) >> 8, (baseaddress&0x00ff) );
        if (log.isDebugEnabled())
                log.debug("Created Sensor " + systemName  +
                                  " (Address " + baseaddress +
                                  " D" + pin +
                                  ")");
        // Finally, request the current state from the layout.
        //this.requestUpdateFromLayout();
        //tc.getFeedbackMessageCache().requestCachedStateFromLayout(this);
        // tc.addXBeeListener(this);
    }

    protected void doNewState(int oldState, int newState) {
       // get message 
       XBeeMessage message=XBeeMessage.getRemoteDoutMessage(xbeeAddress,pin,newState==Light.ON);
       // send the message
       tc.sendXBeeMessage(message,null);
    }

    static Logger log = LoggerFactory.getLogger(XBeeLight.class.getName());
}
