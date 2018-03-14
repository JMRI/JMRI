package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Scaffold for port controller objects.
 *
 * @author Paul Bender Copyright (C) 2016
 */

public class AbstractPortControllerScaffold extends AbstractPortController {

    DataOutputStream ostream;  // Traffic controller writes to this
    DataInputStream tostream; // so we can read it from this

    DataOutputStream tistream; // tests write to this
    DataInputStream istream;  // so the traffic controller can read from this
   
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

    public AbstractPortControllerScaffold(SystemConnectionMemo connectionMemo) throws Exception {
        super(connectionMemo);
        PipedInputStream tempPipe;
        tempPipe = new PipedInputStream();
        tostream = new DataInputStream(tempPipe);
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

    // check that this object is ready to operate
    @Override
    public boolean status() {
       return true;
    }
}

