//JmriSRCPThrottleServer.java
package jmri.jmris.srcp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Throttle;
import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface between the JMRI Throttles and an SRCP network connection
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPThrottleServer extends jmri.jmris.AbstractThrottleServer {

    private static final Logger log = LoggerFactory.getLogger(JmriSRCPThrottleServer.class);

    private DataOutputStream output;

    public JmriSRCPThrottleServer(DataInputStream inStream, DataOutputStream outStream) {
        super();
        output = outStream;
    }


    /*
     * Protocol Specific Functions
     */
    public void sendStatus() throws IOException {
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
           String StatusString="100 INFO " + bus + "GL" + address + " ";
           StatusString+= isForward?"1 ":"0 ";
           switch(speedStepMode){
                  case DccThrottle.SpeedStepMode14:
                       StatusString+= java.lang.Math.ceil(speedSetting *14) + " " +14;
                       break;
                  case DccThrottle.SpeedStepMode27:
                       StatusString+= java.lang.Math.ceil(speedSetting *27) + " " +27;
                       break;
                  case DccThrottle.SpeedStepMode28:
                       StatusString+= java.lang.Math.ceil(speedSetting *28) + " " +28;
                       break;
                  case DccThrottle.SpeedStepMode128:
                       StatusString+= java.lang.Math.ceil(speedSetting *126) + " " +126;
                       break;
                  default:
                       StatusString+= java.lang.Math.ceil(speedSetting *100) + " " +100;
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

    public void sendErrorStatus() throws IOException{
        TimeStampedOutput.writeTimestamp(output, Bundle.getMessage("Error499"));
    }

    public void parsecommand(String statusString) throws JmriException, IOException {
    }

}
