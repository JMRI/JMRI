//JmriSRCPTimeServer.java
package jmri.jmris.srcp;

import java.io.DataOutputStream;
import java.io.IOException;
import jmri.JmriException;
import jmri.TimebaseRateException;
import jmri.jmris.AbstractTimeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * interface between the JMRI (fast) clock and an SRCP network connection
 *
 * @author Paul Bender Copyright (C) 2013
 * @version $Revision: 23184 $
 */
public class JmriSRCPTimeServer extends AbstractTimeServer {

    private final DataOutputStream output;

    private int modelrate = 1;
    private int realrate = 1;

    public JmriSRCPTimeServer(DataOutputStream outStream) {
        super();
        this.timeListener = null;
        output = outStream;
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendTime() throws IOException {
        // prepare to format the date as <JulDay> <Hour> <Minute> <Seconds>
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyDDD hh mm ss");
        output.writeBytes("100 INFO 0 TIME " + sdf.format(timebase.getTime()) + "\n\r");
    }

    @Override
    public void sendRate() throws IOException {
        output.writeBytes("101 INFO 0 TIME " + modelrate + " " + realrate + "\n\r");
    }

    @Override
    public void sendStatus() throws IOException {
        // send "102 INFO 0 TIME" if fastclock stops?
        // sendRate() if fastclock starts?
    }

    @Override
    public void sendErrorStatus() throws IOException {
    }

    @Override
    public void parseTime(String statusString) throws JmriException, IOException {
        // parsing is handled by the SRCP parser; this routine should never
        // be called for SRCP
    }

    public void parseTime(int JulDay, int Hour, int Minute, int Second) {
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar();
        cal.set(java.util.Calendar.YEAR, JulDay / 1000);
        cal.set(java.util.Calendar.DAY_OF_YEAR, JulDay % 1000);
        cal.set(java.util.Calendar.HOUR, Hour);
        cal.set(java.util.Calendar.MINUTE, Minute);
        cal.set(java.util.Calendar.SECOND, Second);
        timebase.userSetTime(cal.getTime());
    }

    @Override
    public void parseRate(String statusString) throws JmriException, IOException {
        // parsing is handled by the SRCP parser; this routine should never
        // be called for SRCP
    }

    public void parseRate(int modelRate, int realRate) {
        modelrate = modelRate;
        realrate = realRate;
        try {
            timebase.userSetRate((double) modelRate / (double) realRate);
        } catch (TimebaseRateException ex) {
            // Although this Exception is declared to be thrown for userSetRate,
            // it is never created in code, so just log it should we begin to see it
            log.error("Something went wrong", ex);
        }
    }

    @Override
    public void stopTime() {
        super.stopTime();
        // when the clock stops, we need to notify any
        // waiting timers the clock has stoped.
    }

    static Logger log = LoggerFactory.getLogger(JmriSRCPTimeServer.class.getName());

}
