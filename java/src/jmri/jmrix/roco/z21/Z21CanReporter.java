package jmri.jmrix.roco.z21;

import java.util.ArrayList;
import java.util.function.Predicate;
import jmri.CollectingReporter;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.RailCom;
import jmri.RailComManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Z21CanReporter implements the Reporter Manager interface
 * for Can connected reporters on Roco Z21 systems.
 * <p>
 * Reports from this reporter are of the type jmri.RailCom.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21CanReporter extends jmri.implementation.AbstractRailComReporter implements Z21Listener,CollectingReporter {

    private Z21SystemConnectionMemo _memo = null;

    private int networkID=0; // CAN network ID associated with this reporter's module.
    private int moduleAddress=-1; // User assigned address associated with this reporter's module.
    private int port; // module port (0-7) associated with this reporter.

    private ArrayList<RailCom> idTags;

    /**  
     * Create a new Z21CanReporter.
     *
     * @param systemName the system name of the new reporter.
     * @param userName the user name of the new reporter.
     * @param memo an instance of Z21SystemConnectionMemo this reporter 
     *             is associated with.
     *
     */
    public Z21CanReporter(String systemName,String userName,Z21SystemConnectionMemo memo){
        super(systemName,userName);
        _memo = memo;
        _memo.getTrafficController().addz21Listener(this);
        //Address format passed is in the form of moduleAddress:pin 
        int seperator = systemName.indexOf(":");
        int start = _memo.getSystemPrefix().length() + 1;
        try {
           try{
              moduleAddress = (Integer.parseInt(systemName.substring(start,seperator)));
           } catch (NumberFormatException ex) {
              // didn't parse as a decimal, check to see if network ID 
              // was used instead.
              networkID = (Integer.parseInt(systemName.substring(start,seperator),16));
           }
           port = (Integer.parseInt(systemName.substring(seperator + 1)));
        } catch (NumberFormatException ex) {
           log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
           throw new IllegalArgumentException("requires mm:pp format address.");
        }
        idTags = new ArrayList<RailCom>();
        // request an update from the layout
        //if(networkID!=0){
        //leave commented out for now, causing loop that needs investigation.
        //   _memo.getTrafficController().sendz21Message(Z21Message.getLanCanDetector(networkID),this);
        //}
    }

    // the Z21 Listener interface

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(Z21Reply msg){
         // for incoming messages all the reporter cares about is
         // LAN_CAN_DETECTOR messages.
         if(msg.isCanDetectorMessage()){
            int netID = ( msg.getElement(4)&0xFF) + ((msg.getElement(5)&0xFF) << 8);
            int address = ( msg.getElement(6)&0xFF) + ((msg.getElement(7)&0xFF) << 8);
            if((address != moduleAddress) && (netID != networkID)) {
                return; // not our messge.
            }
            int msgPort = ( msg.getElement(8) & 0xFF);
            if( msgPort != port ) {
                return; // not our messge.
            }
            int type = ( msg.getElement(9) & 0xFF);
            log.debug("reporter message type {}",type);
            if (type >= 0x11 && type <= 0x1f) {
               if(type==0x11) { // restart the list.
                  log.trace("clear list, size {}",idTags.size());
                  idTags.clear();
                  notify(null);
               }
               int value1 = (msg.getElement(10)&0xFF) + ((msg.getElement(11)&0xFF) << 8);
               int value2 = (msg.getElement(12)&0xFF) + ((msg.getElement(13)&0xFF) << 8);
               RailCom tag = getRailComTagFromValue(msg,value1);
               if(tag != null ) {
                  log.trace("add tag {}",tag);
                  notify(tag);
                  idTags.add(tag);
                  // add the tag to the collection
                  tag = getRailComTagFromValue(msg,value2);
                  if(tag != null ) {
                     log.trace("add tag {} ",tag);
                     notify(tag);
                     // add the tag to idTags
                     idTags.add(tag);
                  }
               }
               if(log.isDebugEnabled()){
                  log.debug("after message, new list size {}",idTags.size());
                  int i = 0;
                  for(RailCom id:idTags){
                      log.debug("{}: {}",i++,id);
                  }
               }
            } else if( type == 0x01 ) {
                // status message, not a railcom value, so no report.
                return;
             }
         }
    }

    /*
     * private method to get and update a railcom tag based on the value 
     * bytes from the message.
     */
    private RailCom getRailComTagFromValue(Z21Reply msg,int value){
       DccLocoAddress l = msg.getCanDetectorLocoAddress(value);
       if (l != null ) { // 0 represents end of list or no railcom address.
          // get the first locomotive address from the message.
          log.debug("reporting tag for address 1 {}",l);
          // see if there is a tag for this address.
          RailCom tag = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("" + l.getNumber());
          int direction = (0xC000&value);
          switch (direction) {
             case 0x8000:
                tag.setOrientation(RailCom.ORIENTA);
                break;
             case 0xC000:
                tag.setOrientation(RailCom.ORIENTB);
                break;
             default:
                tag.setOrientation(0);
          }
          return tag;
       } 
       return null; // address in the message indicates end of list.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(Z21Message msg){
         // we don't need to handle outgoing messages, so just ignore them.
    }

    @Override
    public void dispose(){
        super.dispose();
    }

    // the CollectingReporter interface.
    /**
     * {@inheritDoc}
     */
    @Override 
    public java.util.Collection getCollection(){
        return idTags;
    }

    private static final Logger log = LoggerFactory.getLogger(Z21CanReporter.class);

}
