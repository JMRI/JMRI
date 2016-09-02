package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.RemoteXBeeDevice;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import jmri.jmrix.AbstractPortController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class provides an interface between the XBee messages that are sent 
 * to and from the serial port connected to an XBee device.
 * Remote devices may be sending messages in transparent mode.
 *
 * Some of this code is derived from the XNetSimulator.
 *
 * @Author Paul Bender Copyright (C) 2014
 */
final public class XBeeIOStream extends AbstractPortController implements IDataReceiveListener{

    private DataOutputStream pout = null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // data from xbee written hear
    // ends up in pin.
    private DataInputStream inpipe = null; // data read from this pipe is
    // sent to the XBee.
    private Thread sourceThread;

    private RemoteXBeeDevice remoteXBee;
    private XBeeTrafficController xtc;

    public XBeeIOStream(XBeeNode node, XBeeTrafficController tc) {
        super(tc.getAdapterMemo());
        remoteXBee = node.getXBee();
        try {
            PipedOutputStream tempPipeI = new PipedOutputStream();
            pout = new DataOutputStream(tempPipeI);
            inpipe = new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO = new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        } catch (java.io.IOException e) {
            log.error("init (pipe): Exception: " + e.toString());
            return;
        }


        xtc = tc;
        // register to receive xbee messages from the associated RemoteXBeeDevice.
        xtc.getXBee().addDataListener(this);


        // start the transmit thread
        sourceThread = new Thread(new TransmitThread(remoteXBee, tc, inpipe));
        sourceThread.start();

    }

    // routines defined as abstract in AbstractPortController
    public DataInputStream getInputStream() {
        if (pin == null) {
            log.error("getInputStream called before load(), stream not available");
        }
        return pin;
    }

    public DataOutputStream getOutputStream() {
        if (pout == null) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return pout;
    }

    public void connect() {
    }

    public void configure() {
    }

    public boolean status() {
        return (pout != null && pin != null);
    }

    public String getCurrentPortName() {
        return "NONE";
    }

    @Override
    public boolean getDisabled() {
        return false;
    }

    public void setDisabled(boolean disabled) {
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void recover() {
    }


    /*
     * IDataReceiveListener callback
     */
    @Override
    public void dataReceived(com.digi.xbee.api.models.XBeeMessage xbeeMessage){
        // take received replies and put them in the data output stream
        // if they match the address.
        if(xbeeMessage.getDevice().equals(remoteXBee)){
           try{
              byte data[] = xbeeMessage.getData();
              log.debug("Received {}", data);
              for (int i = 0; i < data.length; i++) {
                 outpipe.write(data[i]);
              }
           } catch (java.io.IOException ioe) {
            log.error("IOException writing serial data from XBee to pipe");
           }
        }
    }

    static private class TransmitThread implements Runnable {

        private RemoteXBeeDevice node = null;
        private XBeeTrafficController xtc = null;
        private DataInputStream pipe = null;

        public TransmitThread(RemoteXBeeDevice n, XBeeTrafficController tc, DataInputStream input) {
            node = n;
            xtc = tc;
            pipe = input;
        }

        public void run() { // start a new thread
            // this thread has one task.  It repeatedly reads from the input pipe
            // and sends data to the XBee.
            if (log.isDebugEnabled()) {
                log.debug("XBee Transmit Thread Started");
            }
            for (;;) {
                // the data we send is required to be a byte array.
                // The maximum number of values we
                // can collect is 100.
                ArrayList<Byte> data = new ArrayList<Byte>();
                try {
                    do {
                       log.debug("Attempting byte read");
                       byte b = pipe.readByte();
                       log.debug("Read Byte: {}", b);
                       data.add(data.size(), b);
                    } while (data.size() < 100 && pipe.available() > 0);
                } catch (java.io.IOException e) {
                   log.error("IOException reading serial data from pipe before sending to XBee");
                }
                byte dataArray[] = new byte[data.size()];
                int i = 0;
                for (Byte n : data) {
                   dataArray[i++] = n;
                }
                if (log.isDebugEnabled()) {
                    log.debug("XBee Thread received message " + dataArray);
                }
                try {
                   xtc.getXBee().sendDataAsync(node,dataArray);
                } catch(com.digi.xbee.api.exceptions.XBeeException xbe){
                  log.error("Exception sending stream data to node {}.",node);
                }
            }
        }

    }

    private final static Logger log = LoggerFactory.getLogger(XBeeIOStream.class.getName());

}

