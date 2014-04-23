// XBeeLightManager.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.managers.AbstractLightManager;
import jmri.Light;
import jmri.JmriException;

/**
 * Implement light manager for XBee connections
 * <p>
 *
 * @author Paul Bender Copyright (C) 2014
 * @version $Revision$
 */ 
public class XBeeLightManager extends AbstractLightManager {

   protected String prefix = null;

   protected XBeeTrafficController tc = null;

   public XBeeLightManager(XBeeTrafficController controller,String prefix) {
       tc=controller;
       this.prefix=prefix;
   }

   public String getSystemPrefix() { return prefix; }

    // for now, set this to false. multiple additions currently works 
    // partially, but not for all possible cases.
    public boolean allowMultipleAdditions(String systemName) { return false;  }

    public Light createNewLight(String systemName, String userName) {
        XBeeNode curNode = null;
        String name = addressFromSystemName(systemName);
        if( (curNode = (XBeeNode) tc.getNodeFromName(name)) == null )
            if((curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null )
            try {
                curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
            } catch(java.lang.NumberFormatException nfe) {
              // if there was a number format exception, we couldn't
              // find the node.
              curNode = null;
            }
        int pin = pinFromSystemName(systemName);
        if(!curNode.getPinAssigned(pin)) {
           log.debug("Adding sensor to pin " + pin );
           curNode.setPinBean(pin,new XBeeLight(systemName, userName,tc));
           return (XBeeLight) curNode.getPinBean(pin);
        } else {
           log.debug("failed to create light " +systemName);
           return null;
        }
    }

    /**
     * Validate system name for the current hardware configuration
     *   returns 'true' if system name has a valid meaning in current configuration,
     *      else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName){
         if(tc.getNodeFromName(addressFromSystemName(systemName))==null &&
            tc.getNodeFromAddress(addressFromSystemName(systemName))==null ) {
            try {
                if( tc.getNodeFromAddress(Integer.parseInt(addressFromSystemName(systemName))) == null )
                   return false;
                else return(pinFromSystemName(systemName)>=0 &&
                            pinFromSystemName(systemName)<=7 );
            } catch(java.lang.NumberFormatException nfe) {
              // if there was a number format exception, we couldn't
              // find the node.
              return false;
            }

         } else {
            return(pinFromSystemName(systemName)>=0 &&
                   pinFromSystemName(systemName)<=7 );
         }
    }

    private String addressFromSystemName(String systemName) {
        String encoderAddress;

        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or S:light address
            int seperator = systemName.indexOf(":");
            encoderAddress = systemName.substring(getSystemPrefix().length()+1,seperator);
         } else {
            encoderAddress=systemName.substring(getSystemPrefix().length()+1,systemName.length()-1);
        }
        if (log.isDebugEnabled()) log.debug("Converted " + systemName + " to hardware address " + encoderAddress);
        return encoderAddress;
    }

    private int pinFromSystemName(String systemName) {
        int input = 0;
        int iName = 0;

        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or L:light address
            int seperator = systemName.indexOf(":");
            try {
                input = Integer.valueOf(systemName.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
                return -1;
            }
         } else {
            try{
                iName = Integer.parseInt(systemName.substring(getSystemPrefix().length()+1));
                input = iName % 10;
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " Hardware Address to a number");
                return -1;
            }
        }
        if (log.isDebugEnabled()) log.debug("Converted " + systemName + " to pin number" + input);
        return input;
    }

    /**
     * Public method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current
     *   configuration, else returns 'false'
     *   for now, this method always returns 'true'; it is needed for the
     *   Abstract Light class
     */
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    static Logger log = LoggerFactory.getLogger(XBeeLightManager.class.getName());

}
