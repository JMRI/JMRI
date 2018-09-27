package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOSample;
import com.digi.xbee.api.io.IOValue;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for XBee connections.
 *
 * @author Paul Bender Copyright (C) 2013
 */
public class XBeeSensor extends AbstractSensor implements IIOSampleReceiveListener {

    private String nodeIdentifier; /* This is a string representation of
     the XBee address in the system name.
     It may be an address or it may be
     the nodeIdentifier string stored in
     the NI parameter on the node.*/

    private int pin;         /* Which DIO pin does this sensor represent. */

    private XBeeNode node = null; // Which node does this belong too.
    private String systemName;

    protected XBeeTrafficController tc = null;

    public XBeeSensor(String systemName, String userName, XBeeTrafficController controller) {
        super(systemName, userName);
        tc = controller;
        init(systemName);
    }

    public XBeeSensor(String systemName, XBeeTrafficController controller) {
        super(systemName);
        tc = controller;
        init(systemName);
    }

    /**
     * Common initialization for both constructors.
     */
    private void init(String id) {
        // store address
        systemName = id;
        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo m = tc.getAdapterMemo();
        if( !(m instanceof XBeeConnectionMemo))
        {
           log.error("Memo associated with the traffic controller is not the right type");
           throw new IllegalArgumentException("Memo associated with the traffic controller is not the right type");
        } else {
           XBeeConnectionMemo memo = (XBeeConnectionMemo) m;
           String prefix = memo.getSensorManager().getSystemPrefix();

           if (systemName.contains(":")) {
               //Address format passed is in the form of encoderAddress:input or S:sensor address
               int seperator = systemName.indexOf(":");
               try {
                   nodeIdentifier = systemName.substring(prefix.length() + 1, seperator);
                   if ((node = (XBeeNode) tc.getNodeFromName(nodeIdentifier)) == null) {
                       if ((node = (XBeeNode) tc.getNodeFromAddress(nodeIdentifier)) == null) {
                           try {
                               node = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(nodeIdentifier));
                           } catch (java.lang.NumberFormatException nfe) {
                               // if there was a number format exception, we couldn't
                               // find the node.
                               node = null;
                           }
                       }
                   }
                   pin = Integer.parseInt(systemName.substring(seperator + 1));
               } catch (NumberFormatException ex) {
                   log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
               }
           } else {
               try {
                   nodeIdentifier = systemName.substring(prefix.length() + 1, id.length() - 1);
                   int address = Integer.parseInt(id.substring(prefix.length() + 1, id.length()));
                   node = (XBeeNode) tc.getNodeFromAddress(address / 10);
                   // calculate the pin to examine
                   pin = ((address) % 10);
               } catch (NumberFormatException ex) {
                   log.debug("Unable to convert " + systemName + " Hardware Address to a number");
               }
           }
           if (log.isDebugEnabled()) {
               log.debug("Created Sensor " + systemName
                    + " (NodeIdentifier " + nodeIdentifier
                    + " ,D" + pin
                    + ")");
           }

           // register to hear XBee IO Sample events.
          tc.getXBee().addIOSampleListener(this);

           // Finally, request the current state from the layout.
           this.requestUpdateFromLayout();
        } 
    }

    /**
     * Request an update on status by sending an XBee message.
     */
    @Override
    public void requestUpdateFromLayout() {
        // Request the sensor status from the XBee Node this sensor is
        // attached to.  
        try  {
           IOValue value = node.getXBee().getDIOValue(IOLine.getDIO(pin));
           if ((value==IOValue.HIGH) ^ _inverted) {
               setOwnState(Sensor.ACTIVE);
           } else {
               setOwnState(Sensor.INACTIVE);
           }
        } catch (TimeoutException toe) {
           log.error("Timeout retrieving IO line value for {} on {}",IOLine.getDIO(pin),node.getXBee());
           // hidden terminal? Make sure the state appears as unknown.
           setOwnState(Sensor.UNKNOWN);
        } catch (InterfaceNotOpenException ino) {
           log.error("Interface Not Open retrieving IO line value for {} on {}",IOLine.getDIO(pin),node.getXBee());
        } catch (XBeeException xbe) {
           log.error("Error retrieving IO line value for {} on {}",IOLine.getDIO(pin),node.getXBee());
        }
    }


    // IIOSampleReceiveListener methods

    @Override
    public synchronized void ioSampleReceived(RemoteXBeeDevice remoteDevice,IOSample ioSample) {
        if (log.isDebugEnabled()) {
            log.debug("received io sample {} from {}", ioSample, remoteDevice);
        }

        XBeeNode sourcenode = (XBeeNode) tc.getNodeFromXBeeDevice(remoteDevice);

        if (node.equals(sourcenode)) {
          if ( ioSample.hasDigitalValues()){
              if ((ioSample.getDigitalValue(IOLine.getDIO(pin))==IOValue.HIGH) ^ _inverted) {
                 setOwnState(Sensor.ACTIVE);
             } else {
                 setOwnState(Sensor.INACTIVE);
             }
          }
        }
        return;
    }

    /**
     * Set the pull resistance.
     * <p>
     * In this default implementation, the input value is ignored.
     *
     * @param r PullResistance value to use
     */
    @Override
    public void setPullResistance(PullResistance r){
       try { 
          node.setPRParameter(pin,r);
       } catch (TimeoutException toe) {
         log.error("Timeout retrieving PR value for {} on {}",IOLine.getDIO(pin),node.getXBee());
       } catch (XBeeException xbe) {
         log.error("Error retrieving PR value for {} on {}",IOLine.getDIO(pin),node.getXBee());
       }
    }

    /**
     * Get the pull resistance.
     *
     * @return the currently set PullResistance value
     */
    @Override
    public PullResistance getPullResistance(){
       try {
          return node.getPRValueForPin(pin);
       } catch (TimeoutException toe) {
         log.error("Timeout retrieving PR value for {} on {}",IOLine.getDIO(pin),node.getXBee());
       } catch (XBeeException xbe) {
         log.error("Error retrieving PR value for {} on {}",IOLine.getDIO(pin),node.getXBee());
       }
       return PullResistance.PULL_UP; // return the default if we get this far.
    }

    @Override
    public void dispose() {
        tc.getXBee().removeIOSampleListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeSensor.class);

}
