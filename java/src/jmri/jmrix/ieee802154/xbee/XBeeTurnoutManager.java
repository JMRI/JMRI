// XBeeTurnoutManager.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.managers.AbstractTurnoutManager;
import jmri.Turnout;
import jmri.JmriException;

/**
 * Implement turnout manager for XBee connections
 * <p>
 *
 * @author Paul Bender Copyright (C) 2014
 * @version $Revision$
 */ 
public class XBeeTurnoutManager extends AbstractTurnoutManager {

   protected String prefix = null;

   protected XBeeTrafficController tc = null;

   public XBeeTurnoutManager(XBeeTrafficController controller,String prefix) {
       tc=controller;
       this.prefix=prefix;
   }

   public String getSystemPrefix() { return prefix; }

    // for now, set this to false. multiple additions currently works 
    // partially, but not for all possible cases.
    public boolean allowMultipleAdditions(String systemName) { return false;  }

    public Turnout createNewTurnout(String systemName, String userName) {
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
              log.debug("failed to create turnout " +systemName);
              return null;
            }
        int pin = pinFromSystemName(systemName);
        int pin2 = pin2FromSystemName(systemName);
        if(!curNode.getPinAssigned(pin) &&
          ( pin2 == -1 || !curNode.getPinAssigned(pin2)) ) {
           log.debug("Adding turnout to pin " + pin );
           curNode.setPinBean(pin,new XBeeTurnout(systemName, userName,tc));
           if(pin2 != -1 )
             curNode.setPinBean(pin2,curNode.getPinBean(pin));
           return (XBeeTurnout) curNode.getPinBean(pin);
        } else {
           log.debug("failed to create turnout " +systemName);
           return null;
        }
    }

    @Override
    public String createSystemName(String curAddress,String prefix) throws JmriException {
         return prefix+typeLetter()+curAddress;
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
                            pinFromSystemName(systemName)<=7 && 
                            (pin2FromSystemName(systemName) == -1 ||
                            (pin2FromSystemName(systemName)>=0 &&
                             pin2FromSystemName(systemName)<=7)));
            } catch(java.lang.NumberFormatException nfe) {
              // if there was a number format exception, we couldn't
              // find the node.
              return false;
            }

         } else {
            return(pinFromSystemName(systemName)>=0 &&
                   pinFromSystemName(systemName)<=7 && 
                   (pin2FromSystemName(systemName) == -1 ||
                   (pin2FromSystemName(systemName)>=0 &&
                   pin2FromSystemName(systemName)<=7)));
         }
    }

    private String addressFromSystemName(String systemName) {
        String encoderAddress;

        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or S:turnout address
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
            //Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = systemName.indexOf(":");
            int seperator2 = systemName.indexOf(":",seperator+1);
            int len = systemName.length();
            try {
            	if ((seperator2 >= 0) && (seperator2 <= len)) {
                    input = Integer.valueOf(systemName.substring(seperator+1,seperator2)).intValue();
            	} else {
                    input = Integer.valueOf(systemName.substring(seperator+1,len)).intValue();
            	}
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

    private int pin2FromSystemName(String systemName) {
        int input = 0;
        int iName = 0;

        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = systemName.indexOf(":");
            int seperator2 = systemName.indexOf(":",seperator+1);
            try {
                input = Integer.valueOf(systemName.substring(seperator2+1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
                return -1;
            }
        } else {
            // no ":" means only one pin.
            input = -1;
        }
        if (log.isDebugEnabled()) log.debug("Converted " + systemName + " to pin number" + input);
        return input;
    }

    @Override
    public void deregister(jmri.NamedBean s){
       super.deregister(s);
       // remove the specified turnout from the associated XBee pin.
       String systemName = s.getSystemName();
       String name = addressFromSystemName(systemName);
       int pin = pinFromSystemName(systemName);
       XBeeNode curNode;
       if( (curNode = (XBeeNode) tc.getNodeFromName(name)) == null )
             if( (curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null)
               try {
                   curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
               } catch(java.lang.NumberFormatException nfe) {
                 // if there was a number format exception, we couldn't
                 // find the node.
                 curNode = null;
               }
        if(curNode !=null ) {
           if( curNode.removePinBean(pin,s) )
               log.debug("Removing turnout from pin " + pin );
           else
               log.debug("Failed to removing turnout from pin " + pin );
        }

    }


    static Logger log = LoggerFactory.getLogger(XBeeTurnoutManager.class.getName());

}
