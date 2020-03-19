package jmri.jmris.srcp;

import jmri.InstanceManager;
import jmri.InstanceManagerDelegate;

import java.sql.Time;
import java.util.Date;

/*
 * The SRCP protocol requires that respose messages include a timestamp based
 * on the fast clock.  This class is a utility class to generate timestamp
 * and send it on to the stream with the output.
 *
 * @author Paul Bender Copyright 2014
 */
public class TimeStampedOutput {

    private final InstanceManagerDelegate instanceManagerDelegate;

    public TimeStampedOutput(){
        this(new InstanceManagerDelegate());
    }

    public TimeStampedOutput(InstanceManagerDelegate instanceManagerDelegate){
        this.instanceManagerDelegate = instanceManagerDelegate;
    }

    static public void writeTimestamp(java.io.DataOutputStream outStream, String s) throws java.io.IOException {
        Date currenttime = InstanceManager.getDefault(jmri.Timebase.class).getTime();
        long time = currenttime.getTime();
        outStream.writeBytes("" + time / 1000 + "." + time % 1000 + " " + s);
    }

}
