//JmriSRCPTimeServer.java
package jmri.jmris.srcp;

//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.ClockControl;
import jmri.jmris.AbstractTimeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interface between the JMRI (fast) clock and an SRCP network connection
 *
 * @author Paul Bender Copyright (C) 2013
 * @version $Revision: 23184 $
 */
public class JmriSRCPTimeServer extends AbstractTimeServer{

    private DataOutputStream output;

    private int modelrate=1;
    private int realrate=1;

    public JmriSRCPTimeServer(DataOutputStream outStream) {
        super();
        output=outStream;
    }

    /*
     * Protocol Specific Abstract Functions
     */
    public void sendTime() throws IOException {
          // prepare to format the date as <JulDay> <Hour> <Minute> <Seconds>
          java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyDDD hh mm ss");
          output.writeBytes("100 INFO 0 TIME " + sdf.format(clock.getTime()) + "\n\r");
    }
    
    public void sendRate() throws IOException {
          output.writeBytes("101 INFO 0 TIME " + modelrate + " " + realrate +"\n\r");
    }

    public void sendErrorStatus() throws IOException {
    }

    public void parseTime(String statusString) throws JmriException, IOException {
      // parsing is handled by the SRCP parser; this routine should never
      // be called for SRCP 
   }

    public void parseTime(int JulDay, int Hour, int Minute, int Second) {
       java.util.GregorianCalendar cal = new java.util.GregorianCalendar();
       cal.set(java.util.Calendar.YEAR,JulDay/1000);
       cal.set(java.util.Calendar.DAY_OF_YEAR,JulDay%1000);
       cal.set(java.util.Calendar.HOUR,Hour);
       cal.set(java.util.Calendar.MINUTE,Minute);
       cal.set(java.util.Calendar.SECOND,Second);
       clock.setTime(cal.getTime());
    }

    public void parseRate(String statusString) throws JmriException, IOException{
      // parsing is handled by the SRCP parser; this routine should never
      // be called for SRCP 
    }

    public void parseRate(int modelRate,int realRate) {
       modelrate=modelRate;
       realrate=realRate;
       clock.setRate((double)modelRate/(double)realRate);
    }

    public void stopTime() {
       clock.stopHardwareClock();
       // when the clock stops, we need to notify any
       // waiting timers the clock has stoped. 
    }

static Logger log = LoggerFactory.getLogger(JmriSRCPTimeServer.class.getName());

}
