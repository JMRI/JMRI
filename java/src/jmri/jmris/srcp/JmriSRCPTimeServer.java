package jmri.jmris.srcp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
 */
public class JmriSRCPTimeServer extends AbstractTimeServer {

    private final DataOutputStream output;

    private int modelrate = 1;
    private int realrate = 1;

    public JmriSRCPTimeServer(DataOutputStream outStream) {
        super();
        output = outStream;
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendTime() throws IOException {
        // prepare to format the date as <JulDay> <Hour> <Minute> <Seconds>
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH mm ss");
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar();
        cal.setTime(timebase.getTime());
        long day = jmri.util.DateUtil.julianDayFromCalendar(cal);
        TimeStampedOutput.writeTimestamp(output, "100 INFO 0 TIME " + day + " " + sdf.format(timebase.getTime()) + "\n\r");
    }

    @Override
    public void sendRate() throws IOException {
        TimeStampedOutput.writeTimestamp(output, "101 INFO 0 TIME " + modelrate + " " + realrate + "\n\r");
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

    public void parseTime(long JulDay, int Hour, int Minute, int Second) {
        java.util.GregorianCalendar cal = jmri.util.DateUtil.calFromJulianDate(JulDay);
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

    public void setAlarm(long JulDay, int Hour, int Minute, int Second) {
        if (log.isDebugEnabled()) {
            log.debug("setting alarm for " + JulDay + " "
                    + Hour + ":" + Minute + ":" + Second);
        }

        java.util.GregorianCalendar cal = jmri.util.DateUtil.calFromJulianDate(JulDay);
        cal.set(java.util.Calendar.HOUR, Hour);
        cal.set(java.util.Calendar.MINUTE, Minute);
        cal.set(java.util.Calendar.SECOND, Second);

        java.util.GregorianCalendar now = new java.util.GregorianCalendar();
        now.setTime(timebase.getTime());
        if (now.after(cal)) {
            try {
                sendTime();
            } catch (IOException ex) {
                log.warn("Unable to send message to client: {}", ex.getMessage());
            }
        } else {
            // add this alarm to the list of alarms.
            if (alarmList == null) {
                alarmList = new java.util.ArrayList<java.util.GregorianCalendar>();
            }
            alarmList.add(cal);
            // and start the timeListener.
            listenToTimebase(true);
            try {
                TimeStampedOutput.writeTimestamp(output, "200 Ok\n\r");
            } catch (IOException ie) {
                log.warn("Unable to send message to client: {}", ie.getMessage());
            }
        }
    }

    private java.util.ArrayList<java.util.GregorianCalendar> alarmList = null;

    private void checkAlarmList() throws IOException {
        if (alarmList == null) {
            return;
        }

        java.util.GregorianCalendar cal = new java.util.GregorianCalendar();
        cal.setTime(timebase.getTime());
        if (log.isDebugEnabled()) {
            log.debug("checking alarms at "
                    + jmri.util.DateUtil.julianDayFromCalendar(cal)
                    + " "
                    + cal.get(java.util.Calendar.HOUR_OF_DAY) + ":"
                    + cal.get(java.util.Calendar.MINUTE) + ":"
                    + cal.get(java.util.Calendar.SECOND));
        }
        java.util.Iterator<java.util.GregorianCalendar> alarm = alarmList.iterator();
        while (alarm.hasNext()) {
            if (cal.after(alarm.next())) {
                sendTime();
                alarm.remove();
            }
        }
    }

    @Override
    public void listenToTimebase(boolean listen) {
        if (listen == false && timeListener == null) {
            return; // nothing to do.
        }
        if (timeListener == null) {
            timeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        if (evt.getPropertyName().equals("minutes")) {
                            checkAlarmList();
                        }
                    } catch (IOException ex) {
                        log.warn("Unable to send message to client: {}", ex.getMessage());
                        timebase.removeMinuteChangeListener(timeListener);
                    }
                }
            };
        }
        if (listen == true) {
            timebase.addMinuteChangeListener(timeListener);
        } else {
            timebase.removeMinuteChangeListener(timeListener);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JmriSRCPTimeServer.class);

}
