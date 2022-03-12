package jmri.jmrix;

import java.io.*;

import jmri.SystemConnectionMemo;

/**
 * Scaffold for port controller objects.
 *
 * @author Paul Bender Copyright (C) 2016
 */

public class AbstractPortControllerScaffold extends AbstractPortController {

    private final DataOutputStream ostream;  // Traffic controller writes to this
    private final DataOutputStream tistream; // tests write to this
    private final DataInputStream istream;  // so the traffic controller can read from this
   
    @Override
    public void configure() {
    }

    @Override
    public String getCurrentPortName() {
        return("testport");
    }

    @Override
    public void recover(){
    }

    @Override
    public void connect(){
    }

    public AbstractPortControllerScaffold(SystemConnectionMemo connectionMemo) throws IOException {
        super(connectionMemo);
        PipedInputStream tempPipe = new PipedInputStream();
        ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
        tempPipe = new PipedInputStream();
        istream = new DataInputStream(tempPipe);
        tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
    }

    // returns the InputStream from the port
    @Override
    public DataInputStream getInputStream() {
        return istream;
    }

    // returns the outputStream to the port
    @Override
    public DataOutputStream getOutputStream() {
        return ostream;
    }
    
    /**
     * Get the redirect stream.
     * Data sent to this output stream will appear in the DataInputStream.
     * @return the stream which redirects to the input stream.
     */
    public DataOutputStream getRedirectedToInputStream() {
        return tistream;
    }

    // check that this object is ready to operate
    @Override
    public boolean status() {
        return true;
    }
}

