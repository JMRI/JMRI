package jmri.jmris.srcp;

import jmri.InstanceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/*
 * The SRCP protocol requires that response messages include a timestamp based
 * on the fast clock.  This class is a utility class to generate timestamp
 * and send it on to the stream with the output.
 *
 * @author Paul Bender Copyright 2014,2020
 */
public class TimeStampedOutput extends OutputStream {

    private final OutputStream outputStream;

    public TimeStampedOutput(OutputStream outputStream){
        super();
        this.outputStream = outputStream;
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
       outputStream.write(bytes,i,i1);
    }

    @Override
    public synchronized void write(byte[] bytes) throws IOException {
        Date currentTime = InstanceManager.getDefault(jmri.Timebase.class).getTime();
        long time = currentTime.getTime();
        String timeString = String.format("%s.%s ",time/1000,time%1000);
        byte[] outputBytes = new byte[timeString.length() + bytes.length];
        System.arraycopy(timeString.getBytes(),0,outputBytes,0,timeString.length());
        System.arraycopy(bytes,0, outputBytes,timeString.length(),bytes.length);
        outputStream.write(outputBytes);
    }

    @Override
    public void write(int i) throws IOException {
        outputStream.write(i);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

}
