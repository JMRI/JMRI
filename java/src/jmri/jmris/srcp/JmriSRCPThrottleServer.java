package jmri.jmris.srcp;

import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.JmriException;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmris.AbstractThrottleServer;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface between the JMRI Throttles and an SRCP network connection
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPThrottleServer extends AbstractThrottleServer {

    private static final Logger log = LoggerFactory.getLogger(JmriSRCPThrottleServer.class);

    private DataOutputStream output;

    private ArrayList<Integer> busList;
    private ArrayList<LocoAddress> addressList;

    public JmriSRCPThrottleServer(DataInputStream inStream, DataOutputStream outStream) {
        super();
        busList=new ArrayList<>();
        addressList=new ArrayList<>();
        output = outStream;
    }


    /*
     * Protocol Specific Functions
     */
    @Override
    public void sendStatus(LocoAddress l) throws IOException {
        TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error499"));
    }

    /*
     * send the status of the specified throttle address on the specified bus
     * @param bus bus number.
     * @param address locomoitve address.
     */
    public void sendStatus(int bus, int address) throws IOException {
        log.debug("send Status called with bus {} and address {}", bus, address);

        /* translate the bus into a system connection memo */
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo = null;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error412"));
            return;
        }

        /* request the throttle for this particular locomotive address */
        if(memo.provides(jmri.ThrottleManager.class)) {
           ThrottleManager t=memo.get(jmri.ThrottleManager.class);
           // we will use getThrottleInfo to request information about the
           // address, so we need to convert the address to a DccLocoAddress
           // object first.
           DccLocoAddress addr = new DccLocoAddress(address,t.canBeLongAddress(address));
           Boolean isForward=(Boolean)t.getThrottleInfo(addr,"IsForward");
           Float speedSetting=(Float)t.getThrottleInfo(addr,"SpeedSetting");
           Integer speedStepMode=(Integer)t.getThrottleInfo(addr,"SpeedStepMode");
           Boolean f0=(Boolean)t.getThrottleInfo(addr,"F0");
           Boolean f1=(Boolean)t.getThrottleInfo(addr,"F1");
           Boolean f2=(Boolean)t.getThrottleInfo(addr,"F2");
           Boolean f3=(Boolean)t.getThrottleInfo(addr,"F3");
           Boolean f4=(Boolean)t.getThrottleInfo(addr,"F4");
           Boolean f5=(Boolean)t.getThrottleInfo(addr,"F5");
           Boolean f6=(Boolean)t.getThrottleInfo(addr,"F6");
           Boolean f7=(Boolean)t.getThrottleInfo(addr,"F7");
           Boolean f8=(Boolean)t.getThrottleInfo(addr,"F8");
           Boolean f9=(Boolean)t.getThrottleInfo(addr,"F9");
           Boolean f10=(Boolean)t.getThrottleInfo(addr,"F10");
           Boolean f11=(Boolean)t.getThrottleInfo(addr,"F11");
           Boolean f12=(Boolean)t.getThrottleInfo(addr,"F12");
           Boolean f13=(Boolean)t.getThrottleInfo(addr,"F13");
           Boolean f14=(Boolean)t.getThrottleInfo(addr,"F14");
           Boolean f15=(Boolean)t.getThrottleInfo(addr,"F15");
           Boolean f16=(Boolean)t.getThrottleInfo(addr,"F16");
           Boolean f17=(Boolean)t.getThrottleInfo(addr,"F17");
           Boolean f18=(Boolean)t.getThrottleInfo(addr,"F18");
           Boolean f19=(Boolean)t.getThrottleInfo(addr,"F19");
           Boolean f20=(Boolean)t.getThrottleInfo(addr,"F20");
           Boolean f21=(Boolean)t.getThrottleInfo(addr,"F21");
           Boolean f22=(Boolean)t.getThrottleInfo(addr,"F22");
           Boolean f23=(Boolean)t.getThrottleInfo(addr,"F23");
           Boolean f24=(Boolean)t.getThrottleInfo(addr,"F24");
           Boolean f25=(Boolean)t.getThrottleInfo(addr,"F25");
           Boolean f26=(Boolean)t.getThrottleInfo(addr,"F26");
           Boolean f27=(Boolean)t.getThrottleInfo(addr,"F27");
           Boolean f28=(Boolean)t.getThrottleInfo(addr,"F28");
           // and now build the output string to send
           String StatusString="100 INFO " + bus + " GL " + address + " ";
           StatusString+= isForward?"1 ":"0 ";
           switch(speedStepMode){
                  case DccThrottle.SpeedStepMode14:
                       StatusString+= (int)java.lang.Math.ceil(speedSetting *14) + " " +14;
                       break;
                  case DccThrottle.SpeedStepMode27:
                       StatusString+= (int)java.lang.Math.ceil(speedSetting *27) + " " +27;
                       break;
                  case DccThrottle.SpeedStepMode28:
                       StatusString+= (int)java.lang.Math.ceil(speedSetting *28) + " " +28;
                       break;
                  case DccThrottle.SpeedStepMode128:
                       StatusString+= (int)java.lang.Math.ceil(speedSetting *126) + " " +126;
                       break;
                  default:
                       StatusString+= (int)java.lang.Math.ceil(speedSetting *100) + " " +100;
           }
           StatusString+= f0?" 1":" 0";
           StatusString+= f1?" 1":" 0";
           StatusString+= f2?" 1":" 0";
           StatusString+= f3?" 1":" 0";
           StatusString+= f4?" 1":" 0";
           StatusString+= f5?" 1":" 0";
           StatusString+= f6?" 1":" 0";
           StatusString+= f7?" 1":" 0";
           StatusString+= f8?" 1":" 0";
           StatusString+= f9?" 1":" 0";
           StatusString+= f10?" 1":" 0";
           StatusString+= f11?" 1":" 0";
           StatusString+= f12?" 1":" 0";
           StatusString+= f13?" 1":" 0";
           StatusString+= f14?" 1":" 0";
           StatusString+= f15?" 1":" 0";
           StatusString+= f16?" 1":" 0";
           StatusString+= f17?" 1":" 0";
           StatusString+= f18?" 1":" 0";
           StatusString+= f19?" 1":" 0";
           StatusString+= f20?" 1":" 0";
           StatusString+= f21?" 1":" 0";
           StatusString+= f22?" 1":" 0";
           StatusString+= f23?" 1":" 0";
           StatusString+= f24?" 1":" 0";
           StatusString+= f25?" 1":" 0";
           StatusString+= f26?" 1":" 0";
           StatusString+= f27?" 1":" 0";
           StatusString+= f28?" 1":" 0";
           StatusString+= "\n\r";
           TimeStampedOutput.writeTimestamp(output, StatusString);
        } else {
            TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error412"));
            return;
        }
    }

    @Override
    public void sendErrorStatus() throws IOException{
        TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error499"));
    }

    @Override
    public void parsecommand(String statusString) throws JmriException, IOException {
    }

    @Override
    public void sendThrottleFound(jmri.LocoAddress address) throws IOException{
        Integer bus;
        if(addressList.contains(address)){
           bus = (Integer)busList.get(addressList.indexOf(address));
        } else {
           // we didn't request this address.
           return;
        }

        // Build the output string to send
        String StatusString="101 INFO " + bus + " GL " + address.getNumber() + " "; // assume DCC for now.
        StatusString+= address.getProtocol()==jmri.LocoAddress.Protocol.DCC_SHORT?"N 1 28":"N 2 28";
        StatusString+= "\n\r";
        TimeStampedOutput.writeTimestamp(output, StatusString);
    }

    @Override
    public void sendThrottleReleased(jmri.LocoAddress address) throws IOException{
        Integer bus;
        if(addressList.contains(address)){
           bus = (Integer)busList.get(addressList.indexOf(address));
        } else {
           // we didn't request this address.
           return;
        }

        // Build the output string to send
        String StatusString="102 INFO " + bus + " GL " + address.getNumber(); // assume DCC for now.
        StatusString+= address.getProtocol()==jmri.LocoAddress.Protocol.DCC_SHORT?"N 1 28":"N 2 28";
        StatusString+= "\n\r";
        TimeStampedOutput.writeTimestamp(output, StatusString);
    }

    public void initThrottle(int bus, int address, boolean isLong,
                             int speedsteps, int functions) throws IOException {
        log.debug("initThrottle called with bus {} and address {}", bus, address);

        /* translate the bus into a system connection memo */
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo = null;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error412"));
            return;
        }

        /* request the throttle for this particular locomotive address */
        if(memo.provides(jmri.ThrottleManager.class)) {
           ThrottleManager t=memo.get(jmri.ThrottleManager.class);
           // we will use getThrottleInfo to request information about the
           // address, so we need to convert the address to a DccLocoAddress
           // object first.
           DccLocoAddress addr = new DccLocoAddress(address,isLong);
           busList.add(Integer.valueOf(bus));
           addressList.add(addr);
           t.requestThrottle(addr,(ThrottleListener)this);
        }
    }

    public void releaseThrottle(int bus, int address) throws IOException {
        log.debug("releaseThrottle called with bus {} and address {}", bus, address);

        /* translate the bus into a system connection memo */
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo = null;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error412"));
            return;
        }

        /* release the throttle for this particular locomotive address */
        if(memo.provides(jmri.ThrottleManager.class)) {
           ThrottleManager t=memo.get(jmri.ThrottleManager.class);
           DccLocoAddress addr = new DccLocoAddress(address,t.canBeLongAddress(address));
           t.releaseThrottle((DccThrottle)throttleList.get(addressList.indexOf(addr)),this);
           throttleList.remove(addressList.indexOf(addr));
           sendThrottleReleased(addr);
           busList.remove(addressList.indexOf(addr));
           addressList.remove(addr);
        }
    }

    /*
     * Set Throttle Speed and Direction
     *
     * @param bus, bus the throttle is on.
     * @param l address of the locomotive to change speed of.
     * @param speed float representing the speed, -1 for emergency stop.
     * @param isForward boolean, true if forward, false if reverse or
     * undefined.
     */
    public void setThrottleSpeedAndDirection(int bus,int address, float speed, boolean isForward){
        log.debug("Setting Speed for address {} bus {} to {} with direction {}",
                  address,bus,speed,isForward?"forward":"reverse");
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo = null;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            try {
               TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error412"));
            } catch(IOException ioe) {
               log.error("Error writing to network port");
            }
            return;
        }

        /* request the throttle for this particular locomotive address */
        if(memo.provides(jmri.ThrottleManager.class)) {
           ThrottleManager tm=memo.get(jmri.ThrottleManager.class);
           // we will use getThrottleInfo to request information about the
           // address, so we need to convert the address to a DccLocoAddress
           // object first.
           DccLocoAddress addr = new DccLocoAddress(address,tm.canBeLongAddress(address));

           // get the throttle for the address.
           if(addressList.contains(addr)){
               log.debug("Throttle in throttle list");
               Throttle t = (Throttle)throttleList.get(addressList.indexOf(addr));
               // set the speed and direction.
               t.setSpeedSetting(speed);
               t.setIsForward(isForward);
           }
       }
   }

    /*
     * Set Throttle Functions on/off
     *
     * @param bus, bus the throttle is on.
     * @param l address of the locomotive to change speed of.
     * @param fList an ArrayList of boolean values indicating whether the
     *         function is active or not.
     */
    public void setThrottleFunctions(int bus,int address, ArrayList<Boolean> fList ){
        log.debug("Setting Functions for address {} bus {}",
                  address,bus);
        java.util.List<SystemConnectionMemo> list = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        SystemConnectionMemo memo = null;
        try {
            memo = list.get(bus - 1);
        } catch (java.lang.IndexOutOfBoundsException obe) {
            try {
               TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error412"));
            } catch(IOException ioe) {
               log.error("Error writing to network port");
            }
            return;
        }

        /* request the throttle for this particular locomotive address */
        if(memo.provides(jmri.ThrottleManager.class)) {
           ThrottleManager tm=memo.get(jmri.ThrottleManager.class);
           // we will use getThrottleInfo to request information about the
           // address, so we need to convert the address to a DccLocoAddress
           // object first.
           DccLocoAddress addr = new DccLocoAddress(address,tm.canBeLongAddress(address));

           // get the throttle for the address.
           if(addressList.contains(addr)){
               log.debug("Throttle in throttle list");
               Throttle t = (Throttle)throttleList.get(addressList.indexOf(addr));
               for(int i=0;i< fList.size();i++){
                  try {
                     java.lang.reflect.Method setter = t.getClass()
                                      .getMethod("setF"+i,boolean.class);
                     setter.invoke(t,(Boolean) fList.get(i));
                  } catch (java.lang.NoSuchMethodException|
                          java.lang.IllegalAccessException|
                          java.lang.reflect.InvocationTargetException ex1) {
                      ex1.printStackTrace();
                      try {
                         sendErrorStatus();
                      } catch(IOException ioe){
                         log.error("Error writing to network port");
                      }
                  }
               }

           }
       }
   }



    // implementation of ThrottleListener
    @Override
    public void notifyThrottleFound(DccThrottle t){
       log.debug("notified throttle found");
       throttleList.add(t);
       try{
          sendThrottleFound(t.getLocoAddress());
          t.addPropertyChangeListener(new srcpThrottlePropertyChangeListener(this,t, (Integer)busList.get(addressList.indexOf(t.getLocoAddress()))));
       } catch(java.io.IOException ioe){
           //Something failed writing data to the port.
       }
    }




   static class srcpThrottlePropertyChangeListener implements PropertyChangeListener {

      int bus;
      int address;
      JmriSRCPThrottleServer clientServer=null;

      srcpThrottlePropertyChangeListener(JmriSRCPThrottleServer ts,Throttle t,
            int bus ){
            log.debug("property change listener created");
            clientServer=ts;
            this.bus=bus;
            address=t.getLocoAddress().getNumber();
       }

              // update the state of this throttle if any of the properties change
       public void propertyChange(java.beans.PropertyChangeEvent e) {
          if (log.isDebugEnabled()) {
              log.debug("Property change event received " + e.getPropertyName() + " / " + e.getNewValue());
          }
          if (e.getPropertyName().equals("SpeedSetting")) {
             try {
                clientServer.sendStatus(bus,address);
             } catch(IOException ioe){
                log.error("Error writing to network port");
             }
          } else if (e.getPropertyName().equals("SpeedSteps")) {
             try {
                clientServer.sendStatus(bus,address);
             } catch(IOException ioe){
                log.error("Error writing to network port");
             }
          } else {
              for (int i = 0; i <= 28; i++) {
                 if (e.getPropertyName().equals("F" + i)) {
                    try {
                       clientServer.sendStatus(bus,address);
                    } catch(IOException ioe){
                       log.error("Error writing to network port");
                    }
                    break; // stop the loop, only one function property
                    // will be matched.
                 } else if (e.getPropertyName().equals("F" + i + "Momentary")) {
                    try {
                       clientServer.sendStatus(bus,address);
                    } catch(IOException ioe){
                       log.error("Error writing to network port");
                    }
                    break; // stop the loop, only one function property
                    // will be matched.
                 }
              }
          }

       }


   }

}
